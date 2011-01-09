/*
 *  Copyright 2008, 2009, 2010:
 *   Tobias Fleig (tfg[AT]online[DOT]de),
 *   Michael Haas (mekhar[AT]gmx[DOT]de),
 *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
 *
 *  - All rights reserved -
 *
 *
 *  This file is part of Centuries of Rage.
 *
 *  Centuries of Rage is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Centuries of Rage is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package thirteenducks.cor.game.client;

import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.ability.AbilityUpgrade.upgradeaffects;
import thirteenducks.cor.game.ability.AbilityUpgrade.upgradetype;
import thirteenducks.cor.map.CoRMapElement.collision;
import thirteenducks.cor.game.Unit.orders;
import java.io.*;
import java.util.*;
import java.security.*;
import jonelo.jacksum.*;
import jonelo.jacksum.algorithm.*;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourHarvest;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourIdle;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourProduce;
import thirteenducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
import thirteenducks.cor.graphics.GraphicsRenderable;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.AbilityBuild;
import thirteenducks.cor.game.ability.AbilityIntraManager;
import thirteenducks.cor.game.ability.AbilityRecruit;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.graphics.BuildingAnimator;
import thirteenducks.cor.graphics.UnitAnimator;
import thirteenducks.cor.map.CoRMap;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.MapIO;

/**
 * Das MapModul auf der Client-Seite
 * Liest .map - Dateien und verifiziert, ob sie mit der Server-Version übereinstimmen
 * Verwaltet die 3 großen ArrayLists (Einheiten, Gebäude, Ressourcen)
 * Verwaltet die netID - HashMap
 *
 * @author tfg
 */
public class ClientMapModule {

    ClientCore.InnerClient rgi;
    boolean syntaxWarned = false; // Ob schon eine Warnung wegen Syntax-Fehlern angezeigt wurde.
    public List<Unit> unitList;
    public List<Building> buildingList;
    public List<Ressource> resList;
    public Vector<GraphicsRenderable> allList;
    HashMap<Integer, GameObject> netIDList;
    public CoRMap theMap;
    public byte[] descSettings;        // Die Settings. Bekommen wir vom Server geschickt.
    public byte[] abilitySettings;     // Die Settings. Bekommen wir vom Server geschickt.
    public byte[] mapData;             // Die Map, falls ein Senden vom Server nötig war.
    File mapFile;
    String mapFileName;
    public boolean[][] serverCollision; // Kollisionsdaten vom Server, debug only

    public ClientMapModule(ClientCore.InnerClient in) {
        rgi = in;
        netIDList = new HashMap<Integer, GameObject>();
    }

    /**
     * Findet heraus, ob bereits eine Map geladen wurde
     * (testet auf map != null)
     * @return
     */
    public boolean mapLoaded() {
        return theMap != null;
    }

    public void initModule() {
        rgi.rogGraphics.setLoadStatus(1);
        // Einstellungsdateien anfordern.
        // Abilitys anfordern:
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 9, 3, 0, 0, 0));
    }

    /**
     * Aufrufen, nachdem abilitySettings reinkopiert wurde
     */
    public void gotAbilities() {
        rgi.rogGraphics.setLoadStatus(2);
        // DESC anfordern
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 9, 2, 0, 0, 0));
    }

    /**
     * Aufrufen, nachdem descSettings reinkopiert wurde
     */
    public void gotDesc() {
        rgi.rogGraphics.setLoadStatus(3);
        // Restlichen Init laufen lassen.
        rgi.logger("[MapModule]: Loading Abilities...");
        HashMap<Integer, Ability> descAbilities = new HashMap<Integer, Ability>();
        // Read abilitys
        BufferedReader bdescReader = null;
        try {
            bdescReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(abilitySettings)));
            String zeile = null;
            // Einlesen
            boolean inDesc = false;
            AbilityBuild ab = null;
            AbilityRecruit ar = null;
            AbilityUpgrade au = null;
            byte type = 0;
            int id = 0;
            int line = 0;
            while ((zeile = bdescReader.readLine()) != null) {
                line++;
                // Zeile interpretieren
                if (!zeile.isEmpty()) {
                    char first = zeile.charAt(0);
                    if (first == '#') {
                        // Kommentar, ignorieren
                        continue;
                    }

                    // Sind wir grad in der Klammer ?
                    if (inDesc) {
                        // Beim = trennen
                        int indexgleich = zeile.indexOf('='); // Istgleich suchen
                        if (indexgleich != -1) {
                            String v1 = zeile.substring(0, indexgleich);
                            String v2 = zeile.substring(indexgleich + 1);
                            int indexraute = v2.indexOf('#'); //Kommentar am Ende der Zeile?
                            if (indexraute != -1) {
                                v2 = v2.substring(0, indexraute - 1);
                            }

                            if (type == 0) {
                                // Typ suchen
                                if (v1.equals("type")) {
                                    if ("build".equals(v2)) {
                                        // Typ RogGameObjectAbilityBuild
                                        ab = new AbilityBuild(id);
                                        ab.rgi = rgi;
                                        ab.symbols = new String[9];
                                        type = 1;
                                    } else if ("recruit".equals(v2)) {
                                        // Type RogGameObjectAbilityRecruit
                                        ar = new AbilityRecruit(id);
                                        ar.rgi = rgi;
                                        ar.symbols = new String[9];
                                        type = 2;
                                    } else if ("upgrade".equals(v2)) {
                                        // Type RogGameObjectAbilityUrgrade
                                        au = new AbilityUpgrade(id);
                                        au.rgi = rgi;
                                        au.symbols = new String[9];
                                        type = 3;
                                    }
                                }
                            } else if (type == 1) {
                                if ("shownname".equals(v1)) {
                                    ab.setName(v2);
                                } else if ("reference".equals(v1)) {
                                    if (v2.startsWith("B")) {
                                        ab.setDescTypeId(saveStrtoInt(v2.substring(1), zeile, line));
                                    }
                                } else if ("cooldown".equals(v1)) {
                                    ab.setCooldown(saveStrtoDouble(v2, zeile, line));
                                } else if ("duration".equals(v1)) {
                                    ab.setDuration(saveStrtoInt(v2, zeile, line));
                                } else if ("depends".equals(v1)) {
                                    if (v2.startsWith("B")) {
                                        ab.addDependB(Integer.parseInt(v2.substring(1)));
                                    } else if (v2.startsWith("U")) {
                                        ab.addDependU(Integer.parseInt(v2.substring(1)));
                                    } else if (v2.startsWith("A")) {
                                        ab.addDependA(Integer.parseInt(v2.substring(1)));
                                    }
                                } else if ("epoche".equals(v1)) {
                                    ab.epoche = saveStrtoInt(v2, zeile, line);
                                } else if ("e0tex".equals(v1)) {
                                    ab.symbols[0] = v2;
                                } else if ("e1tex".equals(v1)) {
                                    ab.symbols[1] = v2;
                                } else if ("e2tex".equals(v1)) {
                                    ab.symbols[2] = v2;
                                } else if ("e3tex".equals(v1)) {
                                    ab.symbols[3] = v2;
                                } else if ("e4tex".equals(v1)) {
                                    ab.symbols[4] = v2;
                                } else if ("e5tex".equals(v1)) {
                                    ab.symbols[5] = v2;
                                } else if ("e6tex".equals(v1)) {
                                    ab.symbols[6] = v2;
                                } else if ("e7tex".equals(v1)) {
                                    ab.symbols[7] = v2;
                                } else if ("e8tex".equals(v1)) {
                                    ab.symbols[8] = v2;
                                } else if ("cost1".equals(v1)) {
                                    ab.costs[0] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost2".equals(v1)) {
                                    ab.costs[1] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost3".equals(v1)) {
                                    ab.costs[2] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost4".equals(v1)) {
                                    ab.costs[3] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost5".equals(v1)) {
                                    ab.costs[4] = saveStrtoInt(v2, zeile, line);
                                } else if ("locked".equals(v1)) {
                                    if (v2.contains("true")) {
                                        ab.invisibleLocked = true;
                                    }
                                }
                            } else if (type == 2) {
                                if ("shownname".equals(v1)) {
                                    ar.setName(v2);
                                } else if ("reference".equals(v1)) {
                                    if (v2.startsWith("U")) {
                                        ar.descTypeId = saveStrtoInt(v2.substring(1), zeile, line);
                                    }
                                } else if ("cooldown".equals(v1)) {
                                    ar.setCooldown(saveStrtoDouble(v2, zeile, line));
                                } else if ("duration".equals(v1)) {
                                    ar.duration = saveStrtoInt(v2, zeile, line);
                                } else if ("depends".equals(v1)) {
                                    if (v2.startsWith("B")) {
                                        ar.addDependB(Integer.parseInt(v2.substring(1)));
                                    } else if (v2.startsWith("U")) {
                                        ar.addDependU(Integer.parseInt(v2.substring(1)));
                                    } else if (v2.startsWith("A")) {
                                        ar.addDependA(Integer.parseInt(v2.substring(1)));
                                    }
                                } else if ("epoche".equals(v1)) {
                                    ar.epoche = saveStrtoInt(v2, zeile, line);
                                } else if ("e0tex".equals(v1)) {
                                    ar.symbols[0] = v2;
                                } else if ("e1tex".equals(v1)) {
                                    ar.symbols[1] = v2;
                                } else if ("e2tex".equals(v1)) {
                                    ar.symbols[2] = v2;
                                } else if ("e3tex".equals(v1)) {
                                    ar.symbols[3] = v2;
                                } else if ("e4tex".equals(v1)) {
                                    ar.symbols[4] = v2;
                                } else if ("e5tex".equals(v1)) {
                                    ar.symbols[5] = v2;
                                } else if ("e6tex".equals(v1)) {
                                    ar.symbols[6] = v2;
                                } else if ("e7tex".equals(v1)) {
                                    ar.symbols[7] = v2;
                                } else if ("e8tex".equals(v1)) {
                                    ar.symbols[8] = v2;
                                } else if ("cost1".equals(v1)) {
                                    ar.costs[0] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost2".equals(v1)) {
                                    ar.costs[1] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost3".equals(v1)) {
                                    ar.costs[2] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost4".equals(v1)) {
                                    ar.costs[3] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost5".equals(v1)) {
                                    ar.costs[4] = saveStrtoInt(v2, zeile, line);
                                } else if ("locked".equals(v1)) {
                                    if (v2.contains("true")) {
                                        ar.invisibleLocked = true;
                                    }
                                }
                            } else if (type == 3) {
                                if ("shownname".equals(v1)) {
                                    au.setName(v2);
                                } else if ("uptype".equals(v1)) {
                                    if (v2.equals("epoche")) {
                                        au.kind = upgradetype.epoche;
                                    } else if (v2.equals("upgrade")) {
                                        au.kind = upgradetype.upgrade;
                                    } else if (v2.equals("deltaupgrade")) {
                                        au.kind = upgradetype.deltaupgrade;
                                    }
                                } else if ("affects".equals(v1)) {
                                    if (v2.contains("all")) {
                                        au.affects = upgradeaffects.all;
                                    } else if (v2.contains("fresh")) {
                                        au.affects = upgradeaffects.fresh;
                                    } else if (v2.contains("old")) {
                                        au.affects = upgradeaffects.old;
                                    } else if (v2.contains("self")) {
                                        au.affects = upgradeaffects.self;
                                        au.useForAll = true;
                                    }
                                } else if ("transformTo".equals(v1)) {
                                    au.transTo = saveStrtoInt(v2, zeile, line);
                                } else if ("reference".equals(v1)) {
                                    if (v2.startsWith("U")) {
                                        au.descTypeIdU = saveStrtoInt(v2.substring(1), zeile, line);
                                    } else if (v2.startsWith("B")) {
                                        au.descTypeIdB = saveStrtoInt(v2.substring(1), zeile, line);
                                    }
                                } else if ("duration".equals(v1)) {
                                    au.duration = saveStrtoInt(v2, zeile, line);
                                } else if ("depends".equals(v1)) {
                                    if (v2.startsWith("B")) {
                                        au.addDependB(Integer.parseInt(v2.substring(1)));
                                    } else if (v2.startsWith("U")) {
                                        au.addDependU(Integer.parseInt(v2.substring(1)));
                                    } else if (v2.startsWith("A")) {
                                        au.addDependA(Integer.parseInt(v2.substring(1)));
                                    }
                                } else if ("epoche".equals(v1)) {
                                    au.epoche = saveStrtoInt(v2, zeile, line);
                                } else if ("e0tex".equals(v1)) {
                                    au.symbols[0] = v2;
                                } else if ("e1tex".equals(v1)) {
                                    au.symbols[1] = v2;
                                } else if ("e2tex".equals(v1)) {
                                    au.symbols[2] = v2;
                                } else if ("e3tex".equals(v1)) {
                                    au.symbols[3] = v2;
                                } else if ("e4tex".equals(v1)) {
                                    au.symbols[4] = v2;
                                } else if ("e5tex".equals(v1)) {
                                    au.symbols[5] = v2;
                                } else if ("e6tex".equals(v1)) {
                                    au.symbols[6] = v2;
                                } else if ("e7tex".equals(v1)) {
                                    au.symbols[7] = v2;
                                } else if ("e8tex".equals(v1)) {
                                    au.symbols[8] = v2;
                                } else if ("cost1".equals(v1)) {
                                    au.costs[0] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost2".equals(v1)) {
                                    au.costs[1] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost3".equals(v1)) {
                                    au.costs[2] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost4".equals(v1)) {
                                    au.costs[3] = saveStrtoInt(v2, zeile, line);
                                } else if ("cost5".equals(v1)) {
                                    au.costs[4] = saveStrtoInt(v2, zeile, line);
                                } else if ("Gdesc".equals(v1)) {
                                    au.gdesc = v2;
                                } else if ("newTex".equals(v1)) {
                                    au.newTex = v2;
                                } else if ("newarmortype".equals(v1)) {
                                    au.newarmortype = v2;
                                } else if ("hitpointsup".equals(v1)) {
                                    au.hitpointsup = saveStrtoInt(v2, zeile, line);
                                } else if ("maxhitpointsup".equals(v1)) {
                                    au.maxhitpointsup = saveStrtoInt(v2, zeile, line);
                                } else if ("antiheavyinfup".equals(v1)) {
                                    au.antiheavyinfup = saveStrtoInt(v2, zeile, line);
                                } else if ("antilightinfup".equals(v1)) {
                                    au.antilightinfup = saveStrtoInt(v2, zeile, line);
                                } else if ("antiairup".equals(v1)) {
                                    au.antiairup = saveStrtoInt(v2, zeile, line);
                                } else if ("antibuildingup".equals(v1)) {
                                    au.antibuildingup = saveStrtoInt(v2, zeile, line);
                                } else if ("antikavup".equals(v1)) {
                                    au.antikavup = saveStrtoInt(v2, zeile, line);
                                } else if ("antitankup".equals(v1)) {
                                    au.antitankup = saveStrtoInt(v2, zeile, line);
                                } else if ("antivehicleup".equals(v1)) {
                                    au.antivehicleup = saveStrtoInt(v2, zeile, line);
                                } else if ("damageup".equals(v1)) {
                                    au.damageup = saveStrtoInt(v2, zeile, line);
                                } else if ("toAnimDesc".equals(v1)) {
                                    au.toAnimDesc = saveStrtoInt(v2, zeile, line);
                                } else if ("damageup".equals(v1)) {
                                    au.damageup = saveStrtoInt(v2, zeile, line);
                                } else if ("bulletspeedup".equals(v1)) {
                                    au.damageup = saveStrtoInt(v2, zeile, line);
                                } else if ("newBulletTex".equals(v1)) {
                                    au.newBulletTex = v2;
                                } else if ("rangeup".equals(v1)) {
                                    au.rangeup = saveStrtoInt(v2, zeile, line);
                                } else if ("speedup".equals(v1)) {
                                    au.speedup = saveStrtoDouble(v2, zeile, line);
                                } else if ("visrangeup".equals(v1)) {
                                    au.visrangeup = saveStrtoInt(v2, zeile, line);
                                } else if ("offsetX".equals(v1)) {
                                    au.newOffsetX = saveStrtoInt(v2, zeile, line);
                                    au.changeOffsetX = true;
                                } else if ("offsetY".equals(v1)) {
                                    au.newOffsetY = saveStrtoInt(v2, zeile, line);
                                    au.changeOffsetY = true;
                                } else if ("harvrateup".equals(v1)) {
                                    au.harvRateup = saveStrtoDouble(v2, zeile, line);
                                } else if ("maxIntraup".equals(v1)) {
                                    au.maxIntraup = saveStrtoInt(v2, zeile, line);
                                } else if ("limitupone".equals(v1)) {
                                    au.limitupone = saveStrtoInt(v2, zeile, line);
                                } else if ("harv".equals(v1)) {
                                    if (v2.contains("true")) {
                                        au.harv = true;
                                    }
                                } else if ("allowMultipleUses".equals(v1)) {
                                    if (v2.contains("true")) {
                                        au.allowMultipleUses = true;
                                    }
                                } else if ("locked".equals(v1)) {
                                    if (v2.contains("true")) {
                                        au.invisibleLocked = true;
                                    }
                                } else if ("unlock".equals(v1)) {
                                    au.unlock.add(saveStrtoInt(v2, zeile, line));
                                } else if ("lock".equals(v1)) {
                                    au.lock.add(saveStrtoInt(v2, zeile, line));
                                } else if ("toEpoche".equals(v1)) {
                                    au.toEpoche = saveStrtoInt(v2, zeile, line);
                                } else if (v1.startsWith("B")) {
                                    int desc = saveStrtoInt(v1.substring(1, v1.indexOf(".")), zeile, line);
                                    if (desc != 0) {
                                        // Wenn dazu noch kein Eintrag da ist, dann einen Anlegen
                                        if (!au.edelta.containsKey("B" + desc)) {
                                            DeltaUpgradeParameter p = new DeltaUpgradeParameter();
                                            p.moddesc = desc;
                                            p.modunit = false;
                                            au.edelta.put("B" + desc, p);
                                        }
                                        // Eintragen
                                        String v1s = v1.substring(v1.indexOf(".") + 1);
                                        DeltaUpgradeParameter dup = au.edelta.get("B" + desc);
                                        if ("newTex".equals(v1s)) {
                                            dup.newTex = v2;
                                        } else if ("newarmortype".equals(v1s)) {
                                            dup.newarmortype = v2;
                                        } else if ("hitpointsup".equals(v1s)) {
                                            dup.hitpointsup = saveStrtoInt(v2, zeile, line);
                                        } else if ("maxhitpointsup".equals(v1s)) {
                                            dup.maxhitpointsup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antiheavyinfup".equals(v1s)) {
                                            dup.antiheavyinfup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antilightinfup".equals(v1s)) {
                                            dup.antilightinfup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antiairup".equals(v1s)) {
                                            dup.antiairup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antibuildingup".equals(v1s)) {
                                            dup.antibuildingup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antikavup".equals(v1s)) {
                                            dup.antikavup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antitankup".equals(v1s)) {
                                            dup.antitankup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antivehicleup".equals(v1s)) {
                                            dup.antivehicleup = saveStrtoInt(v2, zeile, line);
                                        } else if ("damageup".equals(v1s)) {
                                            dup.damageup = saveStrtoInt(v2, zeile, line);
                                        } else if ("visrangeup".equals(v1s)) {
                                            dup.visrangeup = saveStrtoInt(v2, zeile, line);
                                        } else if ("toAnimDesc".equals(v1s)) {
                                            dup.toAnimDesc = saveStrtoInt(v2, zeile, line);
                                        } else if ("offsetX".equals(v1s)) {
                                            dup.newOffsetX = saveStrtoInt(v2, zeile, line);
                                            dup.changeOffsetX = true;
                                        } else if ("offsetY".equals(v1s)) {
                                            dup.newOffsetY = saveStrtoInt(v2, zeile, line);
                                            dup.changeOffsetY = true;
                                        } else if ("harvrateup".equals(v1s)) {
                                            dup.harvRateup = saveStrtoDouble(v2, zeile, line);
                                        } else if ("maxIntraup".equals(v1s)) {
                                            dup.maxIntraup = saveStrtoInt(v2, zeile, line);
                                        } else if ("limitupall".equals(v1s)) {
                                            dup.limitupall = saveStrtoInt(v2, zeile, line);
                                        }
                                    }
                                } else if (v1.startsWith("U")) {
                                    int desc = saveStrtoInt(v1.substring(1, v1.indexOf(".")), zeile, line);
                                    if (desc != 0) {
                                        // Wenn dazu noch kein Eintrag da ist, dann einen Anlegen
                                        if (!au.edelta.containsKey("U" + desc)) {
                                            DeltaUpgradeParameter p = new DeltaUpgradeParameter();
                                            p.moddesc = desc;
                                            p.modunit = true;
                                            au.edelta.put("U" + desc, p);
                                        }
                                        // Eintragen
                                        String v1s = v1.substring(v1.indexOf(".") + 1);
                                        DeltaUpgradeParameter dup = au.edelta.get("U" + desc);
                                        if ("newTex".equals(v1s)) {
                                            dup.newTex = v2;
                                        } else if ("newarmortype".equals(v1s)) {
                                            dup.newarmortype = v2;
                                        } else if ("hitpointsup".equals(v1s)) {
                                            dup.hitpointsup = saveStrtoInt(v2, zeile, line);
                                        } else if ("maxhitpointsup".equals(v1s)) {
                                            dup.maxhitpointsup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antiheavyinfup".equals(v1s)) {
                                            dup.antiheavyinfup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antilightinfup".equals(v1s)) {
                                            dup.antilightinfup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antiairup".equals(v1s)) {
                                            dup.antiairup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antibuildingup".equals(v1s)) {
                                            dup.antibuildingup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antikavup".equals(v1s)) {
                                            dup.antikavup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antitankup".equals(v1s)) {
                                            dup.antitankup = saveStrtoInt(v2, zeile, line);
                                        } else if ("antivehicleup".equals(v1s)) {
                                            dup.antivehicleup = saveStrtoInt(v2, zeile, line);
                                        } else if ("damageup".equals(v1s)) {
                                            dup.damageup = saveStrtoInt(v2, zeile, line);
                                        } else if ("visrangeup".equals(v1s)) {
                                            dup.visrangeup = saveStrtoInt(v2, zeile, line);
                                        } else if ("bulletspeedup".equals(v1)) {
                                            dup.damageup = saveStrtoInt(v2, zeile, line);
                                        } else if ("newBulletTex".equals(v1)) {
                                            dup.newBulletTex = v2;
                                        } else if ("toAnimDesc".equals(v1s)) {
                                            dup.toAnimDesc = saveStrtoInt(v2, zeile, line);
                                        } else if ("damageup".equals(v1s)) {
                                            dup.damageup = saveStrtoInt(v2, zeile, line);
                                        } else if ("rangeup".equals(v1s)) {
                                            dup.rangeup = saveStrtoInt(v2, zeile, line);
                                        } else if ("speedup".equals(v1s)) {
                                            dup.speedup = saveStrtoDouble(v2, zeile, line);
                                        } else if ("harv".equals(v1s)) {
                                            if (v2.contains("true")) {
                                                dup.harv = true;
                                            }
                                        }
                                    }
                                } else if (v1.startsWith("G")) {
                                    // Wenn dazu noch kein Eintrag da ist, dann einen Anlegen
                                    if (!au.edelta.containsKey("G")) {
                                        DeltaUpgradeParameter p = new DeltaUpgradeParameter();
                                        p.global = true;
                                        au.edelta.put("G", p);
                                    }
                                    // Eintragen
                                    String v1s = v1.substring(v1.indexOf(".") + 1);
                                    DeltaUpgradeParameter dup = au.edelta.get("G");
                                    if ("harvrate1up".equals(v1s)) {
                                        dup.harvrate1up = saveStrtoInt(v2, zeile, line);
                                    } else if ("harvrate2up".equals(v1s)) {
                                        dup.harvrate2up = saveStrtoInt(v2, zeile, line);
                                    } else if ("harvrate3up".equals(v1s)) {
                                        dup.harvrate3up = saveStrtoInt(v2, zeile, line);
                                    } else if ("harvrate4up".equals(v1s)) {
                                        dup.harvrate4up = saveStrtoInt(v2, zeile, line);
                                    } else if ("harvrate5up".equals(v1s)) {
                                        dup.harvrate5up = saveStrtoInt(v2, zeile, line);
                                    } else if ("limitup".equals(v1s)) {
                                        dup.limitup = saveStrtoInt(v2, zeile, line);
                                    } else if ("res1up".equals(v1s)) {
                                        dup.res1up = saveStrtoInt(v2, zeile, line);
                                    } else if ("res2up".equals(v1s)) {
                                        dup.res2up = saveStrtoInt(v2, zeile, line);
                                    } else if ("res3up".equals(v1s)) {
                                        dup.res3up = saveStrtoInt(v2, zeile, line);
                                    } else if ("res4up".equals(v1s)) {
                                        dup.res4up = saveStrtoInt(v2, zeile, line);
                                    } else if ("res5up".equals(v1s)) {
                                        dup.res5up = saveStrtoInt(v2, zeile, line);
                                    }
                                }
                            }
                        }
                    }
                }

                if (zeile.contains("}")) {
                    // Fertig - einfügen
                    if (ab != null) {
                        descAbilities.put(id, ab);
                    } else if (ar != null) {
                        descAbilities.put(id, ar);
                    } else if (au != null) {
                        descAbilities.put(id, au);
                    }
                    ab = null;
                    ar = null;
                    au = null;
                    inDesc = false;
                }

                if (zeile.contains("{")) {
                    // Neuer Eintrag
                    inDesc = true;
                    int indexL2 = zeile.lastIndexOf(" ");
                    String v3 = zeile.substring(0, indexL2);
                    id = Integer.parseInt(v3);
                    type = 0;
                }
            }

        } catch (FileNotFoundException ex) {
            rgi.logger(ex);
            rgi.logger("[MapModule][ERROR]: Can't open ablitylist");
            //rgi.rogGraphics.displayError("Fähigkeitenbeschreibung \"game/abilities\" nicht gefunden");
        } catch (IOException ex) {
            rgi.logger(ex);
            rgi.logger("[MapModule][ERROR]: Can't read abilitylist!");
            //rgi.rogGraphics.displayError("Fähigkeitenbeschreibung \"game/abilities\" kann nicht gelesen werden");
        } finally {
            try {
                bdescReader.close();
            } catch (IOException ex) {
            }
        }


        // Reinschreiben
        rgi.game.getOwnPlayer().clientDescAbilities = descAbilities;

        rgi.logger("[MapModule]: Loading Unit-/Building-Types...");
        HashMap<Integer, Unit> descUnit = new HashMap<Integer, Unit>();
        HashMap<Integer, Building> descBuilding = new HashMap<Integer, Building>();

        // Read game/descTypes
        BufferedReader ddescReader = null;
        try {
            ddescReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(descSettings)));
            String zeile = null;
            // Einlesen
            boolean inDesc = false;
            String mode = null;
            Building rB = null;
            Unit rU = null;
            int id = 0;
            int line = 0;
            while ((zeile = ddescReader.readLine()) != null) {
                line++;
                // Zeile interpretieren
                if (!zeile.isEmpty()) {
                    char first = zeile.charAt(0);
                    if (first == '#') {
                        // Kommentar, ignorieren
                        continue;
                    }

                    // Sind wir grad in der Klammer ?
                    if (inDesc) {
                        // Beim = trennen
                        int indexgleich = zeile.indexOf('='); // Istgleich suchen
                        if (indexgleich != -1) {
                            String v1 = zeile.substring(0, indexgleich);
                            String v2 = zeile.substring(indexgleich + 1);

                            int indexraute = v2.indexOf('#'); //Kommentar am Ende der Zeile?
                            if (indexraute != -1) {
                                v2 = v2.substring(0, indexraute - 1);
                            }

                            if (mode.equals("B")) {
                                // Gebäude
                                if (v1.equals("name")) {
                                    rB.name = v2;
                                } else if (v1.equals("defaultTexture")) {
                                    rB.defaultTexture = v2;
                                } else if (v1.equals("hitpoints")) {
                                    rB.hitpoints = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("maxhitpoints")) {
                                    rB.maxhitpoints = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("Gdesc")) {
                                    rB.Gdesc = v2;
                                } else if (v1.equals("Gimg")) {
                                    rB.Gimg = v2;
                                } else if (v1.equals("offsetX")) {
                                    rB.offsetX = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("offsetY")) {
                                    rB.offsetY = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("z1")) {
                                    rB.z1 = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("z2")) {
                                    rB.z2 = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("maxIntra")) {
                                    rB.maxIntra = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("harvests")) {
                                    rB.harvests = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("harvRate")) {
                                    rB.harvRate = saveStrtoDouble(v2, zeile, line);
                                } else if (v1.equals("range")) {
                                    rB.range = saveStrtoDouble(v2, zeile, line);
                                } else if (v1.equals("damage")) {
                                    rB.damage = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("cooldownmax")) {
                                    rB.cooldownmax = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("bulletspeed")) {
                                    rB.bulletspeed = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("bullettexture")) {
                                    rB.bullettexture = v2;
                                } else if (v1.equals("atkdelay")) {
                                    rB.atkdelay = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antiair")) {
                                    rB.antiair = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antibuilding")) {
                                    rB.antibuilding = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antiheavyinf")) {
                                    rB.antiheavyinf = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antikav")) {
                                    rB.antikav = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antilightinf")) {
                                    rB.antilightinf = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antitank")) {
                                    rB.antitank = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antivehicle")) {
                                    rB.antivehicle = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("accepts")) {
                                    if ("all".equals(v2)) {
                                        rB.accepts = Building.ACCEPTS_ALL;
                                    }
                                } else if (v1.equals("ability")) {
                                    Ability ra = descAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
                                    if (ra != null) {
                                        rB.abilitys.add(ra);
                                    }
                                } else if (v1.equals("visrange")) {
                                    rB.visrange = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("limit")) {
                                    rB.limit = saveStrtoInt(v2, zeile, line);
                                }
                            } else if (mode.equals("U")) {
                                // Einheiten
                                if (v1.equals("name")) {
                                    rU.name = v2;
                                } else if (v1.equals("defaultTexture")) {
                                    rU.graphicsdata.defaultTexture = v2;
                                } else if (v1.equals("hitpoints")) {
                                    rU.hitpoints = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("maxhitpoints")) {
                                    rU.maxhitpoints = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("Gdesc")) {
                                    rU.Gdesc = v2;
                                } else if (v1.equals("Gimg")) {
                                    rU.Gimg = v2;
                                } else if (v1.equals("Gpro")) {
                                    rU.Gpro = v2;
                                } else if (v1.equals("Gcon")) {
                                    rU.Gcon = v2;
                                } else if (v1.equals("speed")) {
                                    rU.speed = saveStrtoDouble(v2, zeile, line);
                                } else if (v1.equals("range")) {
                                    rU.range = saveStrtoDouble(v2, zeile, line);
                                } else if (v1.equals("damage")) {
                                    rU.damage = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("cooldownmax")) {
                                    rU.cooldownmax = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("bulletspeed")) {
                                    rU.bulletspeed = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("bullettexture")) {
                                    rU.bullettexture = v2;
                                } else if (v1.equals("atkdelay")) {
                                    rU.atkdelay = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("armortype")) {
                                    rU.armortype = v2;
                                } else if (v1.equals("antiheavyinf")) {
                                    rU.antiheavyinf = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antilightinf")) {
                                    rU.antilightinf = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antikav")) {
                                    rU.antikav = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antivehicle")) {
                                    rU.antivehicle = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antitank")) {
                                    rU.antitank = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antiair")) {
                                    rU.antiair = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antibuilding")) {
                                    rU.antibuilding = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("idlerange")) {
                                    rU.idlerange = saveStrtoDouble(v2, zeile, line);
                                } else if (v1.equals("ability")) {
                                    Ability ra = descAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
                                    if (ra != null) {
                                        rU.abilitys.add(ra);
                                    }
                                } else if ("harvester".equals(v1)) {
                                    // Einheit kann ernten:
                                    rU.canHarvest = true;
                                } else if (v1.equals("visrange")) {
                                    rU.visrange = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("limit")) {
                                    rU.limit = saveStrtoInt(v2, zeile, line);
                                }

                            }
                        }
                    }

                    if (first == 'U') {
                        // Neue Einheit
                        rU = new Unit(0, 0, -1);
                        inDesc = true;
                        int indexL1 = zeile.indexOf(" ");
                        int indexL2 = zeile.lastIndexOf(" ");
                        String v3 = zeile.substring(indexL1 + 1, indexL2);
                        id = Integer.parseInt(v3);
                        mode = "U";
                    } else if (first == 'B') {
                        // Neues Gebäude
                        rB = new Building(0, 0, -1);
                        inDesc = true;
                        int indexL1 = zeile.indexOf(" ");
                        int indexL2 = zeile.lastIndexOf(" ");
                        String v3 = zeile.substring(indexL1 + 1, indexL2);
                        id = Integer.parseInt(v3);
                        mode = "B";
                    } else if (first == '}') {
                        // Fertig, in HashMap speichern
                        if (mode.equals("U")) {
                            rU.descTypeId = id;
                            descUnit.put(id, rU);
                            inDesc = false;
                        } else if (mode.equals("B")) {
                            rB.descTypeId = id;
                            descBuilding.put(id, rB);
                            inDesc = false;
                        }
                    }
                }
            }
            rgi.logger("[RogMapModule]: RogMapModule is ready to rock! (init completed)");
        } catch (FileNotFoundException ex) {
            rgi.logger(ex);
            rgi.logger("[RogMapModule][ERROR]: Can't load descTypes!");
            //rgi.rogGraphics.displayError("Einheiten/Gebäudebeschreibung nicht gefunden! (game/descTypes)");
        } catch (IOException ex) {
            rgi.logger(ex);
            rgi.logger("[RogMapModule][ERROR]: Can't read descTypes!");
            //rgi.rogGraphics.displayError("Einheiten/Gebäudebeschreibung kann nicht gelesen werden! (game/descTypes)");
        } finally {
            try {
                ddescReader.close();
            } catch (IOException ex) {
            }
        }


        // Übernehmen:
        rgi.game.getOwnPlayer().descBuilding = descBuilding;
        rgi.game.getOwnPlayer().descUnit = descUnit;

        // Jetzt weiter machen:
        rgi.rogGraphics.content.initState = 1;
        //rgi.rogGraphics.initModule();
    }

    public void loadMap(String mapName, int targetHash) {
        mapFileName = mapName;
        boolean sendIt = false;
        // Läd die angegebene Map-Datei
        rgi.logger("[MapModul] Loading Map...");
        boolean fileAvailable = false;
        mapFile = new File(mapName);
        if (!mapFile.exists()) {
            // Datei nicht gefunden - Fehler loggen
            System.out.println("[MapModul]: Map not found! Map: <" + mapName + ">" + " will ask Server to send it.");
            rgi.logger("[MapModul]: Map not found! Map: <" + mapName + ">" + " will ask Server to send it.");
            sendIt = true;
        } else {
            try {
                // Checksumme gleich berechnen:
                AbstractChecksum checksum = JacksumAPI.getChecksumInstance("adler32");
                try {
                    checksum.readFile(mapName);
                } catch (IOException ex) {
                    System.out.println("FixMe: Checksum-Error??");
                }
                int mapHash = (int) checksum.getValue();
                if (mapHash != targetHash) {
                    System.out.println("[MapModul]: Map mismatch" + " will ask Server to send its version of this map.");
                    rgi.logger("[MapModul][ERROR]: Map mismatch" + " will ask Server to send its version of this map.");
                    sendIt = true;
                }
            } catch (NoSuchAlgorithmException ex) {
                System.out.println("CriticalFixMe: Can't calc checksum, Adler32 not available!");
            }
        }
        // Müss mer se noch holen?
        if (sendIt) {
            rgi.rogGraphics.setLoadStatus(7);
            //Maprequest senden
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 9, 1, 0, 0, 0));
        } else {
            gotMap();
        }

    }

    public void receivedMap() {
        try {
            // Erhaltene Map speichern
            FileOutputStream outp = new java.io.FileOutputStream(mapFile);
            outp.write(mapData);
            outp.flush();
            outp.close();
        } catch (IOException ex) {
        }
        gotMap();
    }

    public void gotMap() {
        rgi.rogGraphics.setLoadStatus(8);

        theMap = MapIO.readMap(mapFile.getPath());

        if (rgi.isInDebugMode()) {
            serverCollision = new boolean[theMap.getMapSizeX()][theMap.getMapSizeY()];
        }

        unitList = Collections.synchronizedList((ArrayList<Unit>) theMap.getMapPoperty("UNIT_LIST"));
        buildingList = Collections.synchronizedList((ArrayList<Building>) theMap.getMapPoperty("BUILDING_LIST"));
        resList = Collections.synchronizedList((ArrayList<Ressource>) theMap.getMapPoperty("RES_LIST"));
        refreshUnits();
        refreshBuildings();
        rgi.logger("[MapModul] Map \"" + mapFileName + "\" loaded.");
        createIDList();
        rgi.rogGraphics.activateMap(theMap.getVisMap());
        rgi.rogGraphics.updateBuildings(buildingList);
        rgi.rogGraphics.updateUnits(unitList);
        rgi.rogGraphics.content.updateRessources(resList);
        createAllList();
        rgi.game.registerBuildingList(buildingList);
        rgi.game.registerUnitList(unitList);
        rgi.game.registerRessourceList(resList);
        // Fertig, mitteilen
        rgi.rogGraphics.triggerStatusWaiting();
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 1, 0, 0, 0, 0));
    }

    private void createAllList() {
        // Erstellt eine Liste, in der Einheiten, Gebäude und Ressourcen enthalten sind.
        // Für Grafik only
        if (allList == null) {
            allList = new Vector<GraphicsRenderable>();
        } else {
            allList.clear();
        }
        for (Unit unit : unitList) {
            allList.add(unit);
        }
        for (Building building : buildingList) {
            allList.add(building);
        }
        for (Ressource res : resList) {
            allList.add(res);
        }
    }

    /**
     * Load server collision map
     */
    private void loadCollisionMap() {
        for (int x = 0; x < theMap.getMapSizeX(); x++) {
            for (int y = 0; y < theMap.getMapSizeY(); y++) {
                if (x % 2 != y % 2) {
                    continue;
                }
                serverCollision[x][y] = isGroundColliding(x, y);
            }
        }
    }

    private void createIDList() {
        // Verlässt sich darauf, dass der MapEditor/RandomMapGenerator korrekte netIDs vergeben hat...
        netIDList.clear();
        for (Unit unit : unitList) {
            netIDList.put(unit.netID, unit);
        }
        for (Building building : buildingList) {
            netIDList.put(building.netID, building);
        }
        for (Ressource ressource : resList) {
            netIDList.put(ressource.netID, ressource);
        }
    }

    public void initUnitRefLists(int numberOfPlayers) {
        for (int x = 0; x < theMap.visMap.length; x++) {
            for (int y = 0; y < theMap.visMap[0].length; y++) {
                if ((x + y) % 2 == 1) {
                    continue;
                }
                theMap.visMap[x][y].unitref = new Unit[numberOfPlayers + 1];
            }
        }
    }

    private void refreshUnits() {
        // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogUnits zu haben
        // Ausserdem werden alle (gespeicherten) Parameter überschrieben, das macht Änderungen einfacher (man ändert nur die desctypes und muss nicht immer neue maps machen)
        HashMap<Integer, Unit> descUnit = rgi.game.getOwnPlayer().descUnit;
        for (Unit unit : unitList) {
            // Alle Parameter kopieren
            unit.copyPropertiesFrom(descUnit.get(unit.descTypeId));
        }
    }

    private void refreshBuildings() {
        // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogBuildings zu haben
        // Ausserdem werden alle (gespeicherten) Parameter überschrieben, das macht Änderungen einfacher (man ändert nur die desctypes und muss nicht immer neue maps machen)
        HashMap<Integer, Building> descBuilding = rgi.game.getOwnPlayer().descBuilding;
        for (Building building : buildingList) {
            building.copyPropertiesFrom(descBuilding.get(building.descTypeId));
        }
    }

    private int saveStrtoInt(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        int i;
        try {
            i = Integer.parseInt(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            // Ging nicht, Syntax-Fehler ins Logfile schreiben
            rgi.logger("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number! - Defaulting to 1");
            if (!syntaxWarned) {
                // Anzeigen
                //rgi.rogGraphics.displayWarning("Einer oder mehrere Syntax-Fehler in \"game/descType\" \n eventuell sind Einheiten oder Gebäude falsch! \n Mehr Informationen im Logfile.");
                syntaxWarned = true;
            }
            return 1;
        }
    }

    private double saveStrtoDouble(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        double i;
        try {
            i = Double.parseDouble(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            // Ging nicht, Syntax-Fehler ins Logfile schreiben
            rgi.logger("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number! - Defaulting to 1");
            if (!syntaxWarned) {
                // Anzeigen
                //rgi.rogGraphics.displayWarning("Einer oder mehrere Syntax-Fehler in \"game/descType\" \n eventuell sind Einheiten oder Gebäude falsch! \n Mehr Informationen im Logfile.");
                syntaxWarned = true;
            }
            return 1;
        }
    }

    /**
     * Gibt die Einheitenreferenz eines Feldes zurück.
     */
    public Unit getUnitRef(Position pos, int playerId) {
        try {
            return theMap.visMap[pos.X][pos.Y].unitref[playerId];
        } catch (Exception ex) {
            return null;
        }
    }

    public Unit getEnemyUnitRef(Position pos, int ownPlayerId) {
        for (int i = 1; i < rgi.game.playerList.size(); i++) {
            if (i == rgi.game.getOwnPlayer().playerId || rgi.game.areAllies(rgi.game.getPlayer(i), rgi.game.getOwnPlayer())) {
                continue;
            }
            try {
                Unit unit = theMap.visMap[pos.X][pos.Y].unitref[i];
                if (unit != null && unit.isCompletelyActivated) {
                    // Wartung
                    if (!unit.alive) {
                        // Referenz löschen, andere suchen
                        theMap.visMap[pos.X][pos.Y].unitref[i] = null;
                        continue;
                    }
                    return unit;
                }
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * Überprüft, ob das Feld irgendeine Referenz einer beliebigen Einheit hat
     * @param pos
     * @return
     */
    public boolean hasUnitRef(Position pos) {
        return hasUnitRef(pos.X, pos.Y);
    }

    /**
     * Überprüft, ob das Feld irgendeine Referenz einer beliebigen Einheit hat
     * @param pos
     * @return
     */
    public boolean hasUnitRef(int x, int y) {
        for (int i = 1; i < rgi.game.playerList.size(); i++) {
            try {
                Unit unit = theMap.visMap[x][y].unitref[i];
                if (unit != null && unit.isCompletelyActivated) {
                    // Wartung
                    if (!unit.alive) {
                        // Referenz löschen, andere suchen
                        theMap.visMap[x][y].unitref[i] = null;
                        continue;
                    }
                    return true;
                }
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    /**
     * Gibt die Ressourcenreferenz eines Feldes zurück.
     */
    public Ressource getResRef(Position pos) {
        try {
            return theMap.visMap[pos.X][pos.Y].resref;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Setzt die Einheitenreferenz eines Feldes.
     */
    public void setUnitRef(Position pos, Unit unit, int playerId) {
        try {
            theMap.visMap[pos.X][pos.Y].unitref[playerId] = unit;
        } catch (Exception ex) {
            System.out.println("SetUnitRef-ERROR: " + pos + "|" + unit);
            rgi.logger(ex);
        }
    }

    /**
     * Setzt die Einheitenreferenz eines Feldes.
     */
    public void setResRef(Position pos, Ressource res) {
        try {
            theMap.visMap[pos.X][pos.Y].resref = res;
        } catch (Exception ex) {
            System.out.println("SetResRef-ERROR: " + pos + "|" + res);
            rgi.logger(ex);
        }
    }

    public boolean isValidUnitDesc(int desc) {
        if (rgi.game.getOwnPlayer().descUnit.get(desc) != null) {
            return true;
        }
        return false;
    }

    public boolean isValidBuildingDesc(int desc) {
        if (rgi.game.getOwnPlayer().descBuilding.get(desc) != null) {
            return true;
        }
        return false;
    }

    public void insertUnitAnimator(int desc, UnitAnimator rgua) {
        // Speichert einen vorkonfigurierten Animator in die DESC-Datenbank ein
        rgi.game.getOwnPlayer().descUnit.get(desc).anim = rgua;
    }

    public void insertBuildingAnimator(int desc, BuildingAnimator rgba) {
        // Speichert eien vorkonfigurierten Animator in die DESC-Datenbank ein
        rgi.game.getOwnPlayer().descBuilding.get(desc).anim = rgba;
    }

    public List<Unit> getUnitList() {
        // Liefert die Einheitenliste zurück.
        return unitList;
    }

    public List<Building> getBuildingList() {
        // Gibt die Gebäudeliste zurück
        return buildingList;
    }

    public List<Ressource> getRessourceList() {
        // Gibt die Ressourcenliste zurück
        return resList;
    }

    public Unit getDescUnit(int descId, int newNetID, int playerId) {
        try {
            Unit unit = rgi.game.getPlayer(playerId).descUnit.get(descId).clone(newNetID);
            ArrayList<Ability> list = new ArrayList<Ability>();
            for (Ability ab : unit.abilitys) {
                list.add(rgi.game.getPlayer(playerId).clientDescAbilities.get(ab.myId));
            }
            unit.abilitys = list;
            return unit;
        } catch (Exception ex) {
            System.out.println("FixMe: Cloning-Error Unit");
            return null;
        }
    }

    public Building getDescBuilding(int descId, int newNetID, int playerId) {
        try {
            Building building = rgi.game.getPlayer(playerId).descBuilding.get(descId).clone(newNetID);
            ArrayList<Ability> list = new ArrayList<Ability>();
            for (Ability ab : building.abilitys) {
                list.add(rgi.game.getPlayer(playerId).clientDescAbilities.get(ab.myId));
            }
            building.abilitys = list;
            return building;
        } catch (Exception ex) {
            System.out.println("FixMe: Cloning-Error Building");
            return null;
        }
    }

    public int getMapSizeX() {
        return theMap.getMapSizeX();
    }

    public int getMapSizeY() {
        return theMap.getMapSizeY();
    }

    /**
     * Führt die Globalen Paramter des DeltaUpgradeParamter aus.
     * Dies funktioniert nur, wenn global=true ist.
     * Dann werden aber alle andern Parameter ignoriert.
     * Daher funktionieren diese Upgrades nur als multidelta/epoche
     * @param para
     */
    public void performGlobalUpgrade(DeltaUpgradeParameter para) {
        NetPlayer me = rgi.game.getOwnPlayer();
        me.harvspeeds[1] += para.harvrate1up;
        me.harvspeeds[2] += para.harvrate2up;
        me.harvspeeds[3] += para.harvrate3up;
        me.harvspeeds[4] += para.harvrate4up;
        me.harvspeeds[5] += para.harvrate5up;
        me.maxlimit += para.limitup;
        me.res1 += para.res1up;
        me.res2 += para.res2up;
        me.res3 += para.res3up;
        me.res4 += para.res4up;
        me.res5 += para.res5up;
    }

    /**
     * Sucht eine Einheit anhand ihrer netID.
     *
     * @param netID - Die netID der Einheit.
     * @return RogUnit, falls sie gefunden wurde, sonst null.
     */
    public Unit getUnitviaID(int netID) {
        try {
            return (Unit) netIDList.get(netID);
        } catch (Exception ex) {
            // Gibts net, falscher Typ etc...
            return null;
        }
    }

    /**
     * Sucht ein Gebäude anhand seiner netID.
     *
     * @param netID - Die netID des Gebäudes.
     * @return RogBiulding, falls gefunden, sonst null.
     */
    public Building getBuildingviaID(int netID) {
        try {
            return (Building) netIDList.get(netID);
        } catch (Exception ex) {
            // Gibts net, falscher Typ etc...
            return null;
        }
    }

    /**
     * Sucht eine Ressource anhand ihrer netID.
     *
     * @param netID - Die netID der Ressource.
     * @return RogRessource, falls sie gefunden wurde, sonst null.
     */
    public Ressource getRessourceviaID(int netID) {
        try {
            return (Ressource) netIDList.get(netID);
        } catch (Exception ex) {
            // Gibts net, falscher Typ etc...
            return null;
        }
    }

    /**
     * Sucht ein GameObject anhand seiner netID.
     *
     * @param netID - Die netID des GameObjects.
     * @return RogGameObjekt, falls sie gefunden wurde, sonst null.
     */
    public GameObject getGameObjectviaID(int netID) {
        try {
            return (GameObject) netIDList.get(netID);
        } catch (Exception ex) {
            // Gibts net, falscher Typ etc...
            return null;
        }
    }

    /**
     * Fügt ein Gebäude zum Spiel hinzu.
     * Darf nur vom NetController aufgerufen werden!
     * @param b
     */
    public void addBuilding(Building b) {
        try {
            rgi.rogGraphics.content.allListLock.lock();
            this.allList.add(b);
        } finally {
            rgi.rogGraphics.content.allListLock.unlock();
        }
        this.buildingList.add(b);

        if (netIDList.containsKey(b.netID)) {
            throw new java.lang.UnknownError("Critical ID mismatch, overwriting netID-Entry");
        }
        this.netIDList.put(b.netID, b);

        b.abilitys.add(0, new AbilityIntraManager(b, rgi));

        b.cbehaviours.add(new ClientBehaviourProduce(rgi, b));

        // In Abhängigkeitsliste einfügen
        if (b.ready && b.playerId == rgi.game.getOwnPlayer().playerId) {
            if (!rgi.game.getOwnPlayer().bList.contains(b.descTypeId)) {
                rgi.game.getOwnPlayer().bList.add(b.descTypeId);
            }
        }

        // Kollision einfügen
        for (int z1 = 0; z1 < b.z1; z1++) {
            for (int z2 = 0; z2 < b.z2; z2++) {
                CoRMapElement tEle = theMap.visMap[(int) b.position.X + z1 + z2][(int) b.position.Y - z1 + z2];
                tEle.setCollision(collision.blocked);
            }
        }

        // Selektionsschatten einfügen
        if (!rgi.isAIClient) {
            rgi.rogGraphics.content.placeGO(b);
            rgi.rogGraphics.builingsChanged();
        }
    }

    public collision getCollision(int x, int y) {
        // Prüft ob ein Feld blockiert, also ob Einheiten drauf laufen können
        try {
            return theMap.visMap[x][y].getCollision();
        } catch (Exception ex) {
            return collision.blocked;
        }
    }

    public collision getCollision(Position pos) {
        // Prüft ob ein Feld blockiert, also ob Einheiten drauf laufen können
        try {
            return theMap.visMap[pos.X][pos.Y].getCollision();
        } catch (Exception ex) {
            return collision.blocked;
        }
    }

    public void setCollision(int x, int y, collision blocking) {
        // Setzt die Kollision
        try {
            theMap.visMap[x][y].setCollision(blocking);
        } catch (Exception ex) {
            System.out.println("SetCollision-Error: " + x + "||" + y);
            rgi.logger(ex);
        }
    }

    public void setCollision(Position position, collision blocking) {
        // Setzt die Kollision
        if (position != null) {
            try {
                theMap.visMap[position.X][position.Y].setCollision(blocking);
            } catch (Exception ex) {
                System.out.println("SetCollision-Error: " + position);
                rgi.logger(ex);
            }
        }
    }

    /**
     * Sucht ein Gebäude anhand seiner Position
     * Bitte nur selten benutzen, da langsam.
     * @param position Die Position des Gebäudes (das Feld ganz links)
     * @return RogBuilding, wenn gefunden, sonst null
     */
    public Building findBuilingViaPosition(Position position) {
        for (int i = 0; i < buildingList.size(); i++) {
            Building b = buildingList.get(i);
            if (b.position.equals(position)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Fügt eine Einheit
     * Darf nur vom NetController aufgerufen werden!
     * @param b
     */
    public void addUnit(Unit u) {
        try {
            rgi.rogGraphics.content.allListLock.lock();
            this.allList.add(u);
        } finally {
            rgi.rogGraphics.content.allListLock.unlock();
        }
        this.unitList.add(u);

        if (netIDList.containsKey(u.netID)) {
            throw new java.lang.UnknownError("Critical ID mismatch, overwriting netID-Entry");
        }
        this.netIDList.put(u.netID, u);

        // In Abhängigkeitsliste einfügen
        if (u.playerId == rgi.game.getOwnPlayer().playerId) {
            if (!rgi.game.getOwnPlayer().uList.contains(u.descTypeId)) {
                rgi.game.getOwnPlayer().uList.add(u.descTypeId);
            }
        }

        // Behaviours adden
        ClientBehaviourHarvest harvb = new ClientBehaviourHarvest(rgi, u);
        ClientBehaviourIdle idleb = new ClientBehaviourIdle(rgi, u);
        u.cbehaviours.add(harvb);
        u.cbehaviours.add(idleb);

        // Kollision einfügen
        this.setCollision(u.position, collision.occupied);
        this.setUnitRef(u.position, u, u.playerId);

        // Truppenlimit hochsetzen
        if (u.playerId == rgi.game.getOwnPlayer().playerId) {
            if (u.limit < 0) {
                rgi.game.getOwnPlayer().maxlimit -= u.limit;
            }
        }
    }

    /**
     * Findet heraus, ob das Feld für Bodeneinheiten nicht begehbar ist
     * @return true bedeutet, dass es besetzt ist
     */
    public boolean isGroundColliding(Position pos) {
        try {
            return (theMap.visMap[pos.X][pos.Y].collision != collision.free);
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Findet heraus, ob das Feld für Bodeneinheiten nicht begehbar ist
     * spezielle Version für Einheiten, die durch eigene durchlaufen können
     * @return true bedeutet, dass es besetzt ist
     */
    public boolean isGroundCollidingForUnit(Position pos, int playerId) {
        try {
            if (theMap.visMap[pos.X][pos.Y].collision != collision.free) {
                if (theMap.visMap[pos.X][pos.Y].collision == collision.occupied && theMap.visMap[pos.X][pos.Y].unitref[playerId] != null) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Findet heraus, ob das Feld für Bodeneinheiten nicht begehbar ist
     * spezielle Version für Einheiten, die durch eigene durchlaufen können
     * @return true bedeutet, dass es besetzt ist
     */
    public boolean isGroundCollidingForUnit(int x, int y, int playerId) {
        try {
            if (theMap.visMap[x][y].collision != collision.free) {
                if (theMap.visMap[x][y].collision == collision.occupied && theMap.visMap[x][y].unitref[playerId] != null) {
                    return false;
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Findet heraus, ob das Feld für Bodeneinheiten nicht begehbar ist
     * @return true bedeutet, dass es besetzt ist
     */
    public boolean isGroundColliding(int x, int y) {
        try {
            return (theMap.visMap[x][y].collision != collision.free);
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Findet heraus, ob ein Feld für Lufteinheiten nicht "begehbar" ist
     * @param pos
     * @return true bedeutet, es ist NICHT begehbar
     */
    public boolean isAirColliding(Position pos) {
        try {
            return (theMap.visMap[pos.X][pos.Y].collision == collision.unreachable);
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Findet heraus, ob ein Feld für Lufteinheiten nicht "begehbar" ist
     * @return true bedeutet, es ist NICHT begehbar
     */
    public boolean isAirColliding(int x, int y) {
        try {
            return (theMap.visMap[x][y].collision == collision.unreachable);
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * Spielt eine Todesanimation ab und entfernt die Einheit aus dem Spiel.
     * @param modunit
     */
    public void unitKilled(Unit unit) {
        if (unit != null) {
            unit.destroy();
            // Truppenlimit
            if (unit.playerId == rgi.game.getOwnPlayer().playerId) {
                if (unit.limit > 0) {
                    rgi.game.getOwnPlayer().currentlimit -= unit.limit;
                } else if (unit.limit < 0) {
                    rgi.game.getOwnPlayer().maxlimit += unit.limit;
                }
            }

            rgi.rogGraphics.notifyUnitDieing(unit);
            this.unitList.remove(unit);
            this.netIDList.remove(unit.netID);
            rgi.rogGraphics.inputM.selected.remove(unit);
            if (rgi.rogGraphics.content.tempInfoObj == unit) {
                rgi.rogGraphics.content.tempInfoObj = null;
            }
        }
    }

    /**
     * Simple Hilfsmethode, die Damage dealt.
     * Für Bulletsystem.
     * Einheit wird NICHT entfernt, wenn sie stirbt!!!
     * Das kann nur der Server.
     * @param victim
     * @param damage
     */
    public void dealDamage(GameObject victim, int damage) {
        victim.hitpoints -= damage;
        if (victim.hitpoints < 0) {
            victim.hitpoints = 0;
        }
        if (victim.getClass().equals(Building.class)) {
            if (!victim.ready) {
                ((Building) victim).damageWhileContruction += damage;
            }
            rgi.rogGraphics.content.fireMan.buildingHit((Building) victim, rgi.rogGraphics.content.epoche);
        }
    }

    /**
     * Löscht die übergebenen Einheiten/Gebäude (killt sie)
     * Vorsicht, es dürfen nur ENTWEDER eine oder mehrere Einheiten ODER ein Gebäude enthalten sein
     * Mischungen oder mehrerer Gebäude sind nicht zulässig!
     * @param selected
     */
    public void deleteSelected(ArrayList<GameObject> selected) {
        // Löscht alle derzeit selektierten Einheiten / das derzeit selektierte Gebäude
        if (!selected.isEmpty()) {
            if (selected.get(0).getClass().equals(Unit.class)) {
                // Einheiten - Löschbefehl an Server schicken
                for (int i = 0; i < selected.size(); i++) {
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 28, selected.get(i).netID, 0, 0, 0));
                }
            } else if (selected.get(0).getClass().equals(Building.class)) {
                // Gebäude - Löschbefehl an Server schicken
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 30, selected.get(0).netID, 0, 0, 0));
            }
        }
    }

    /**
     * Spielt eine Zerstöranimation ab und entfernt das Gebäude aus dem Spiel.
     * @param building
     */
    public void buildingKilled(Building building) {
        // Truppenlimit
        if (building.playerId == rgi.game.getOwnPlayer().playerId) {
            if (building.limit > 0) {
                rgi.game.getOwnPlayer().currentlimit -= building.limit;
            } else if (building.limit < 0) {
                rgi.game.getOwnPlayer().maxlimit += building.limit;
            }
        }
        if (building != null && !rgi.isAIClient) {
            building.destroy();
            // Für eigene Gebäude den Sichtbereich auf erkundet setzen
            if (building.playerId == rgi.game.getOwnPlayer().playerId) {
                rgi.rogGraphics.content.cutDieingBuildingSight(building);
            }
            // Jetzt löschen
            this.buildingList.remove(building);
            this.netIDList.remove(building.netID);
            rgi.rogGraphics.notifyBuildingDieing(building);
            rgi.rogGraphics.inputM.selected.remove(building);
            if (rgi.rogGraphics.content.tempInfoObj == building) {
                rgi.rogGraphics.content.tempInfoObj = null;
            }
            // Selektion entfernen
            rgi.rogGraphics.content.deleteGO(building);
            // Effekte entfernen
        }
    }

    /**
     * Wird VOM BEHAVIOUR aufgerufen, wenn eine Ressource fertig abgeerntet ist.
     * @param res
     */
    public void ressourceFullyHarvested(Ressource res) {
        // Senden, das muss der Server broadcasten

        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 33, res.netID, 0, 0, 0));
    }

    /**
     * Wird aufgerufen, wenn der Server Befehl zum Löschen der Ressource gibt.
     * @param res
     */
    public void killRessource(Ressource res) {
        if (res != null) {
            if (res.getType() < 5) {
                res.destroy();
                ArrayList<GameObject> harvs = res.getAllHarvesters();
                // Alle vorhandenen Ernter löschen (freigeben)
                res.removeAllHarvesters();

                this.resList.remove(res);

                // Referenz löschen
                setResRef(res.position, null);

                // Versuchen, für die Ernter neue Ressourcen zu finden:
                if (res.getType() < 3) {
                    for (GameObject obj : harvs) {
                        if (obj != null) {
                            try {
                                Unit unit = (Unit) obj;
                                Ressource potres = unit.ressourceAroundMe(res.getType(), 10, rgi);
                                if (potres != null) {
                                    unit.goHarvest(potres, rgi);
                                }
                            } catch (ClassCastException ex) {
                            }
                        }
                    }
                } else {
                    // Ernter sollen nichtmehr fuchteln
                    for (GameObject obj : harvs) {
                        if (obj != null) {
                            Unit unit = (Unit) obj;
                            unit.order = orders.idle;
                        }
                    }
                }

                // Kollision löschen

                this.setCollision(res.position, collision.free);
                if (res.getType() > 2) {
                    this.setCollision(res.position.X + 2, res.position.Y, collision.free);
                    this.setCollision(res.position.X + 1, res.position.Y - 1, collision.free);
                    this.setCollision(res.position.X + 1, res.position.Y + 1, collision.free);
                }

                // Selektion löschen

                rgi.rogGraphics.content.deleteGO(res);

                // Löschen

                try {
                    rgi.rogGraphics.content.allListLock.lock();
                    this.allList.remove(res);
                } finally {
                    rgi.rogGraphics.content.allListLock.unlock();
                }
                this.netIDList.remove(res.netID);
                rgi.rogGraphics.inputM.selected.remove(res);
                if (rgi.rogGraphics.content.tempInfoObj == res) {
                    rgi.rogGraphics.content.tempInfoObj = null;
                }

                rgi.rogGraphics.builingsChanged();
            }
        }
    }

    /**
     * Verwaltet komplexere toDESC-Upgrades
     *
     * @param mode old=2, new=3, all=4
     * @param caster Der dieses Upgrade ausführt
     * @param fromDesc Von welcher Desc...
     * @param toDesc ... auf welche upgraden?
     */
    public void manageComplexUpgrades(int mode, GameObject caster, int fromDesc, int toDesc, int playerId) {
        boolean units = false;
        // Änderungen suchen:
        if (fromDesc < 0) {
            // Negative Werte sind Einheiten
            fromDesc *= -1;
            units = true;
        }

        // Wer denn alles?
        if (mode == 2) {
            // Vorhandene
            if (units) {
                for (Unit unit : unitList) {
                    if (unit.descTypeId == fromDesc && unit.playerId == playerId) {
                        unit.performUpgrade(rgi, toDesc);
                    }
                }
            } else {
                for (Building building : buildingList) {
                    if (building.descTypeId == fromDesc && building.playerId == playerId) {
                        building.performUpgrade(rgi, toDesc);
                    }
                }
            }
        } else if (mode == 3) {
            // Zukünftige
            if (units) {
                Unit updesc = rgi.game.getPlayer(playerId).descUnit.get(toDesc);
                rgi.game.getPlayer(playerId).descUnit.put(fromDesc, updesc);
            } else {
                rgi.game.getPlayer(playerId).descBuilding.put(fromDesc, rgi.game.getPlayer(playerId).descBuilding.get(toDesc));
            }
        } else if (mode == 4) {
            // Alle
            if (units) {
                rgi.game.getPlayer(playerId).descUnit.put(fromDesc, rgi.game.getPlayer(playerId).descUnit.get(toDesc));
            } else {
                rgi.game.getPlayer(playerId).descBuilding.put(fromDesc, rgi.game.getPlayer(playerId).descBuilding.get(toDesc));
            }
            if (units) {
                for (Unit unit : unitList) {
                    if (unit.descTypeId == fromDesc && unit.playerId == playerId) {
                        unit.performUpgrade(rgi, toDesc);
                    }
                }
            } else {
                for (Building building : buildingList) {
                    if (building.descTypeId == fromDesc && building.playerId == playerId) {
                        building.performUpgrade(rgi, toDesc);
                    }

                }
            }
        }
    }

    /**
     * Entsperrt eine Ability
     * @param ability Die Ability, die entsperrt werden soll
     */
    public void unlockAbility(int ability) {
        // Für alle unlocken
        NetPlayer player = rgi.game.getOwnPlayer();
        player.clientDescAbilities.get(ability).invisibleLocked = false;
        for (Unit unit : unitList) {
            Ability abi = unit.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = false;
            }
        }
        for (Building building : buildingList) {
            Ability abi = building.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = false;
            }
        }
    }

    /**
     * Sperrt eine Ability
     * @param ability Die Ability, die gesperrt werden soll
     */
    public void lockAbility(int ability) {
        // Für alle unlocken
        NetPlayer player = rgi.game.getOwnPlayer();
        player.clientDescAbilities.get(ability).invisibleLocked = true;
        for (Unit unit : unitList) {
            Ability abi = unit.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = true;
            }
        }
        for (Building building : buildingList) {
            Ability abi = building.getAbility(ability);
            if (abi != null) {
                abi.invisibleLocked = true;
            }
        }
    }
}
