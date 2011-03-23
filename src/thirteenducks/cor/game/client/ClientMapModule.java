/*
 *  Copyright 2008, 2009, 2010, 2011:
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
import java.io.*;
import java.util.*;
import java.security.*;
import jonelo.jacksum.*;
import jonelo.jacksum.algorithm.*;
import org.newdawn.slick.Input;
import thirteenducks.cor.game.BehaviourProcessor;
import thirteenducks.cor.game.DescParamsBuilding;
import thirteenducks.cor.game.DescParamsUnit;
import thirteenducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.game.PlayersBuilding;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.AbilityBuild;
import thirteenducks.cor.game.ability.AbilityIntraManager;
import thirteenducks.cor.game.ability.AbilityRecruit;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.graphics.BuildingAnimator;
import thirteenducks.cor.graphics.UnitAnimator;
import thirteenducks.cor.map.CoRMap;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Unit2x2;
import thirteenducks.cor.graphics.Sprite;
import thirteenducks.cor.graphics.input.InteractableGameElement;
import thirteenducks.cor.map.MapIO;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourMove;

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
    public List<Sprite> allList;
    HashMap<Integer, GameObject> netIDList;
    public CoRMap theMap;
    public byte[] descSettings;        // Die Settings. Bekommen wir vom Server geschickt.
    public byte[] abilitySettings;     // Die Settings. Bekommen wir vom Server geschickt.
    public byte[] mapData;             // Die Map, falls ein Senden vom Server nötig war.
    File mapFile;
    String mapFileName;
    /**
     * Server-Kollision (nur für Debug)
     * 0 = Frei
     * 1 = Unerreichbar
     * 2 = Blockiert
     * 3 = Besetzt (drüberlaufen)
     */
    public int[][] serverCollision;

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
            DescParamsBuilding rB = null;
            DescParamsUnit rU = null;
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
                                    rB.setDescName(v2);
                                } else if (v1.equals("defaultTexture")) {
                                    rB.getGraphicsData().defaultTexture = v2;
                                } else if (v1.equals("hitpoints")) {
                                    rB.setHitpoints(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("maxhitpoints")) {
                                    rB.setMaxhitpoints(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("Gdesc")) {
                                    rB.setDescDescription(v2);
                                } else if (v1.equals("offsetX")) {
                                    rB.getGraphicsData().offsetX = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("offsetY")) {
                                    rB.getGraphicsData().offsetY = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("z1")) {
                                    rB.setZ1(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("z2")) {
                                    rB.setZ2(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("maxIntra")) {
                                    rB.setMaxIntra(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("harvests")) {
                                    rB.setHarvests(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("harvRate")) {
                                    rB.setHarvRate(saveStrtoDouble(v2, zeile, line));
                                } else if (v1.equals("range")) {
                                    rB.setRange(saveStrtoDouble(v2, zeile, line));
                                } else if (v1.equals("damage")) {
                                    rB.setDamage(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("cooldownmax")) {
                                    rB.setFireDelay(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("bulletspeed")) {
                                    rB.setBulletspeed(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("bullettexture")) {
                                    rB.setBullettexture(v2);
                                } else if (v1.equals("atkdelay")) {
                                    rB.setAtkdelay(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("antiair")) {
                                    int[] damageFactors = rB.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_AIR] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antibuilding")) {
                                    int[] damageFactors = rB.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_BUILDING] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antiheavyinf")) {
                                    int[] damageFactors = rB.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_HEAVYINF] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antikav")) {
                                    int[] damageFactors = rB.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_KAV] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antilightinf")) {
                                    int[] damageFactors = rB.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_LIGHTINF] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antitank")) {
                                    int[] damageFactors = rB.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_TANK] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antivehicle")) {
                                    int[] damageFactors = rB.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_VEHICLE] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("accepts")) {
                                    if ("all".equals(v2)) {
                                        rB.setAccepts(Building.ACCEPTS_ALL);
                                    }
                                } else if (v1.equals("ability")) {
                                    Ability ra = descAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
                                    if (ra != null) {
                                        rB.getAbilitys().add(ra);
                                    }
                                } else if (v1.equals("visrange")) {
                                    rB.setVisrange(saveStrtoInt(v2, zeile, line));
                                }
                            } else if (mode.equals("U")) {
                                // Einheiten
                                if (v1.equals("name")) {
                                    rU.setDescName(v2);
                                } else if (v1.equals("defaultTexture")) {
                                    rU.getGraphicsData().defaultTexture = v2;
                                } else if (v1.equals("hitpoints")) {
                                    rU.setHitpoints(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("maxhitpoints")) {
                                    rU.setMaxhitpoints(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("Gdesc")) {
                                    rU.setDescDescription(v2);
                                } else if (v1.equals("Gpro")) {
                                    rU.setDescPro(v2);
                                } else if (v1.equals("Gcon")) {
                                    rU.setDescCon(v2);
                                } else if (v1.equals("speed")) {
                                    rU.setSpeed(saveStrtoDouble(v2, zeile, line));
                                } else if (v1.equals("range")) {
                                    rU.setRange(saveStrtoDouble(v2, zeile, line));
                                } else if (v1.equals("damage")) {
                                    rU.setDamage(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("cooldownmax")) {
                                    rU.setFireDelay(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("bulletspeed")) {
                                    rU.setBulletspeed(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("bullettexture")) {
                                    rU.setBullettexture(v2);
                                } else if (v1.equals("atkdelay")) {
                                    rU.setAtkdelay(saveStrtoInt(v2, zeile, line));
                                } else if (v1.equals("armortype")) {
                                    if ("lightinf".equals(v2)) {
                                        rU.setArmorType(GameObject.ARMORTYPE_LIGHTINF);
                                    } else if ("heavyinf".equals(v2)) {
                                        rU.setArmorType(GameObject.ARMORTYPE_HEAVYINF);
                                    } else if ("air".equals(v2)) {
                                        rU.setArmorType(GameObject.ARMORTYPE_AIR);
                                    } else if ("kav".equals(v2)) {
                                        rU.setArmorType(GameObject.ARMORTYPE_KAV);
                                    } else if ("tank".equals(v2)) {
                                        rU.setArmorType(GameObject.ARMORTYPE_TANK);
                                    } else if ("vehicle".equals(v2)) {
                                        rU.setArmorType(GameObject.ARMORTYPE_VEHICLE);
                                    }
                                } else if (v1.equals("antiair")) {
                                    int[] damageFactors = rU.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_AIR] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antibuilding")) {
                                    int[] damageFactors = rU.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_BUILDING] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antiheavyinf")) {
                                    int[] damageFactors = rU.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_HEAVYINF] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antikav")) {
                                    int[] damageFactors = rU.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_KAV] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antilightinf")) {
                                    int[] damageFactors = rU.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_LIGHTINF] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antitank")) {
                                    int[] damageFactors = rU.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_TANK] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("antivehicle")) {
                                    int[] damageFactors = rU.getDamageFactors();
                                    damageFactors[GameObject.ARMORTYPE_VEHICLE] = saveStrtoInt(v2, zeile, line);
                                } else if (v1.equals("ability")) {
                                    Ability ra = descAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
                                    if (ra != null) {
                                        rU.getAbilitys().add(ra);
                                    }
                                } else if (v1.equals("visrange")) {
                                    rU.setVisrange(saveStrtoInt(v2, zeile, line));
                                }
                            }
                        }
                    }

                    if (first == 'U') {
                        // Neue Einheit
                        rU = new DescParamsUnit();
                        inDesc = true;
                        int indexL1 = zeile.indexOf(" ");
                        int indexL2 = zeile.lastIndexOf(" ");
                        String v3 = zeile.substring(indexL1 + 1, indexL2);
                        id = Integer.parseInt(v3);
                        mode = "U";
                    } else if (first == 'B') {
                        // Neues Gebäude
                        rB = new DescParamsBuilding();
                        inDesc = true;
                        int indexL1 = zeile.indexOf(" ");
                        int indexL2 = zeile.lastIndexOf(" ");
                        String v3 = zeile.substring(indexL1 + 1, indexL2);
                        id = Integer.parseInt(v3);
                        mode = "B";
                    } else if (first == '}') {
                        // Fertig, in HashMap speichern
                        if (mode.equals("U")) {
                            rU.setDescTypeId(id);
                            descUnit.put(id, new Unit2x2(rU));
                            inDesc = false;
                        } else if (mode.equals("B")) {
                            rB.setDescTypeId(id);
                            descBuilding.put(id, new PlayersBuilding(rB));
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

        theMap = MapIO.readMap(mapFile.getPath(), MapIO.MODE_CLIENT);

        if (rgi.isInDebugMode()) {
            serverCollision = new int[theMap.getMapSizeX()][theMap.getMapSizeY()];
        }

        unitList = Collections.synchronizedList((ArrayList<Unit>) theMap.getMapPoperty("UNIT_LIST"));
        buildingList = Collections.synchronizedList((ArrayList<Building>) theMap.getMapPoperty("BUILDING_LIST"));
        refreshUnits();
        refreshBuildings();
        rgi.logger("[MapModul] Map \"" + mapFileName + "\" loaded.");
        createIDList();
        rgi.rogGraphics.activateMap(theMap.getVisMap());
        createAllLists();
        // Fertig, mitteilen
        rgi.rogGraphics.triggerStatusWaiting();
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 1, 0, 0, 0, 0));
    }

    private void createAllLists() {
        // Erstellt eine Liste, in der Einheiten, Gebäude und Ressourcen enthalten sind.
        // Für Grafik only
        if (allList == null) {
            allList = new ArrayList<Sprite>();
        } else {
            allList.clear();
        }
        // Input
        List<InteractableGameElement> igelist = new ArrayList<InteractableGameElement>();
        // Game
        List<BehaviourProcessor> procList = new ArrayList<BehaviourProcessor>();
        for (Unit unit : unitList) {
            igelist.add(unit);
            allList.add(unit);
            procList.add(unit);
        }
        for (Building building : buildingList) {
            allList.add(building);
            igelist.add(building);
            procList.add(building);
        }
        rgi.rogGraphics.inputM.setIGEs(igelist);
        rgi.game.registerAllList(procList);
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
    }

    private void refreshUnits() {
        // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogUnits zu haben
        // Ausserdem werden alle (gespeicherten) Parameter überschrieben, das macht Änderungen einfacher (man ändert nur die desctypes und muss nicht immer neue maps machen)
        HashMap<Integer, Unit> descUnit = rgi.game.getOwnPlayer().descUnit;
        for (Unit unit : unitList) {
            // Alle Parameter kopieren
            unit.copyPropertiesFrom(descUnit.get(unit.getDescTypeId()));
            unit.addClientBehaviour(new ClientBehaviourMove(rgi, unit));
        }
    }

    private void refreshBuildings() {
        // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogBuildings zu haben
        // Ausserdem werden alle (gespeicherten) Parameter überschrieben, das macht Änderungen einfacher (man ändert nur die desctypes und muss nicht immer neue maps machen)
        HashMap<Integer, Building> descBuilding = rgi.game.getOwnPlayer().descBuilding;
        for (Building building : buildingList) {
            building.copyPropertiesFrom(descBuilding.get(building.getDescTypeId()));
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
        System.out.println("AddMe: Insert Unit Animator");
        // rgi.game.getOwnPlayer().descUnit.get(desc).anim = rgua;
    }

    public void insertBuildingAnimator(int desc, BuildingAnimator rgba) {
        // Speichert eien vorkonfigurierten Animator in die DESC-Datenbank ein
        System.out.println("AddMe: Insert Building Animator");
        // rgi.game.getOwnPlayer().descBuilding.get(desc).anim = rgba;
    }

    public List<Unit> getUnitList() {
        // Liefert die Einheitenliste zurück.
        return unitList;
    }

    public List<Building> getBuildingList() {
        // Gibt die Gebäudeliste zurück
        return buildingList;
    }

    public Unit getDescUnit(int descId, int newNetID, int playerId) {
        try {
            Unit unit = (Unit) rgi.game.getPlayer(playerId).descUnit.get(descId).getCopy(newNetID);
            ArrayList<Ability> list = new ArrayList<Ability>();
            List<Ability> unitsAbList = unit.getAbilitys();
            for (Ability ab : unitsAbList) {
                list.add(rgi.game.getPlayer(playerId).clientDescAbilities.get(ab.myId));
            }
            unit.setAbilitys(list);
            return unit;
        } catch (Exception ex) {
            System.out.println("FixMe: Cloning-Error Unit");
            return null;
        }
    }

    public Building getDescBuilding(int descId, int newNetID, int playerId) {
        try {
            Building building = (Building) rgi.game.getPlayer(playerId).descBuilding.get(descId).getCopy(newNetID);
            ArrayList<Ability> list = new ArrayList<Ability>();
            List<Ability> buildingsAbList = building.getAbilitys();
            for (Ability ab : buildingsAbList) {
                list.add(rgi.game.getPlayer(playerId).clientDescAbilities.get(ab.myId));
            }
            building.setAbilitys(list);
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
        rgi.game.addGO(b);
        rgi.rogGraphics.inputM.addGO(b);

        if (netIDList.containsKey(b.netID)) {
            throw new java.lang.UnknownError("Critical ID mismatch, overwriting netID-Entry");
        }
        this.netIDList.put(b.netID, b);

        b.addAbility(new AbilityIntraManager(b, rgi));

        // In Abhängigkeitsliste einfügen
        if (b.getLifeStatus() == GameObject.LIFESTATUS_ALIVE && b.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
            if (!rgi.game.getOwnPlayer().bList.contains(b.getDescTypeId())) {
                rgi.game.getOwnPlayer().bList.add(b.getDescTypeId());
            }
        }

        // Selektionsschatten einfügen
        if (!rgi.isAIClient) {
            rgi.rogGraphics.builingsChanged();
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
            if (b.getMainPosition().equals(position)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Fügt eine Einheit zum Spiel hinzu
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

        rgi.game.addGO(u);
        rgi.rogGraphics.inputM.addGO(u);

        if (netIDList.containsKey(u.netID)) {
            throw new java.lang.UnknownError("Critical ID mismatch, overwriting netID-Entry");
        }
        this.netIDList.put(u.netID, u);

        // In Abhängigkeitsliste einfügen
        if (u.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
            if (!rgi.game.getOwnPlayer().uList.contains(u.getDescTypeId())) {
                rgi.game.getOwnPlayer().uList.add(u.getDescTypeId());
            }
        }
        
        u.addClientBehaviour(new ClientBehaviourMove(rgi, u));
    }

    /**
     * Spielt eine Todesanimation ab und entfernt die Einheit aus dem Spiel.
     * @param modunit
     */
    public void unitKilled(Unit unit) {
        if (unit != null) {

            rgi.rogGraphics.notifyUnitDieing(unit);
            this.unitList.remove(unit);
            this.netIDList.remove(unit.netID);
            rgi.game.removeGO(unit);
            rgi.rogGraphics.inputM.removeGO(unit);
            rgi.rogGraphics.inputM.removeFromSelection(unit);
        }
    }

    /**
     * Simple Hilfsmethode, die Damage dealt.
     * Für Bulletsystem.
     * Einheit wird NICHT entfernt, wenn sie stirbt!!!
     * Das kann nur der Server.
     * @param victim
     * @param damage
     * @deprecated
     */
    public void dealDamage(GameObject victim, int damage) {
        victim.dealDamage(damage);
    }

    /**
     * Löscht die übergebenen Einheiten/Gebäude (killt sie)
     * Vorsicht, es dürfen nur ENTWEDER eine oder mehrere Einheiten ODER ein Gebäude enthalten sein
     * Mischungen oder mehrerer Gebäude sind nicht zulässig!
     * @param selected
     */
    public void deleteSelected(List<InteractableGameElement> selected) {
        // Löscht alle derzeit selektierten Einheiten / das derzeit selektierte Gebäude
        if (!selected.isEmpty()) {
            for (int i = 0; i < selected.size(); i++) {
                selected.get(i).keyCommand(Input.KEY_DELETE, '\0');
            }
        }
    }

    /**
     * Spielt eine Zerstöranimation ab und entfernt das Gebäude aus dem Spiel.
     * @param building
     */
    public void buildingKilled(Building building) {
        if (building != null && !rgi.isAIClient) {
            building.kill();
            // Für eigene Gebäude den Sichtbereich auf erkundet setzen
            if (building.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
                rgi.rogGraphics.content.cutDieingBuildingSight(building);
            }
            // Jetzt löschen
            this.buildingList.remove(building);
            this.netIDList.remove(building.netID);
            rgi.game.removeGO(building);
            rgi.rogGraphics.inputM.removeGO(building);
            rgi.rogGraphics.notifyBuildingDieing(building);
            rgi.rogGraphics.inputM.removeFromSelection(building);
            // Effekte entfernen
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
                    if (unit.getDescTypeId() == fromDesc && unit.getPlayerId() == playerId) {
                        unit.performUpgrade(rgi, toDesc);
                    }
                }
            } else {
                for (Building building : buildingList) {
                    if (building.getDescTypeId() == fromDesc && building.getPlayerId() == playerId) {
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
                    if (unit.getDescTypeId() == fromDesc && unit.getPlayerId() == playerId) {
                        unit.performUpgrade(rgi, toDesc);
                    }
                }
            } else {
                for (Building building : buildingList) {
                    if (building.getDescTypeId() == fromDesc && building.getPlayerId() == playerId) {
                        building.performUpgrade(rgi, toDesc);
                    }

                }
            }
        }
    }

    /**
     * Berechnet, ob 2 GOs sich derzeit direkt berühren.
     * @param obj1 Das erste Objekt
     * @param obj2 Das zweite Objekt
     * @return true, wenn sie direkt benachbart sind (ecken zählen)
     */
    public boolean directNeighbor(GameObject obj1, GameObject obj2) {
        final Position[] pos1 = obj1.getPositions();
        final Position[] pos2 = obj2.getPositions();

        // Diese Methode ist nicht sehr schön, aber es sollte funktionieren:
        for (Position from : pos1) {
            for (Position to : pos2) {
                int dx = from.getX() - to.getX();
                int dy = from.getY() - to.getY();
                if ((dx >= -1 && dx <= 1 && dy >= -1 && dy <= 1) || (dx >= -2 && dx <= 2 && dy == 0) || (dx == 0 && dy >= -2 && dy <= 2)) {
                    return true;
                }
            }
        }

        return false;
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
