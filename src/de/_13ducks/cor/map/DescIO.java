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
 */
package de._13ducks.cor.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.DescParamsBuilding;
import de._13ducks.cor.game.DescParamsUnit;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.Unit2x2;
import de._13ducks.cor.game.Unit3x3;
import de._13ducks.cor.game.ability.Ability;
import de._13ducks.cor.game.ability.AbilityBuild;
import de._13ducks.cor.game.ability.AbilityRecruit;
import de._13ducks.cor.game.ability.AbilityUpgrade;
import de._13ducks.cor.game.ability.AbilityUpgrade.upgradetype;
import de._13ducks.cor.game.ability.ServerAbilityUpgrade;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Einlese-Klasse für Einheiten, Gebäude und Abilities
 * @author Johannes
 */
public class DescIO {

    public static GameDescParams readDescFromStream(byte[] descBuffer, byte[] abBuffer, int mapMode, ClientCore.InnerClient rgi, ServerCore.InnerServer serverrgi) {
        GameDescParams Returndingens = new GameDescParams(); // Wird am Ende Returned


        HashMap<Integer, Unit> descTypeUnit = new HashMap<Integer, Unit>();
        HashMap<Integer, Building> descTypeBuilding = new HashMap<Integer, Building>();
        HashMap<Integer, Ability> descAbilities = new HashMap<Integer, Ability>();
        HashMap<Integer, ServerAbilityUpgrade> descTypeAbilities = new HashMap<Integer, ServerAbilityUpgrade>();

        // Read abilitys
        BufferedReader bdescReader = null;

            // Read game/descTypes
            try {
                bdescReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(abBuffer)));
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
                                            au.affects = de._13ducks.cor.game.ability.AbilityUpgrade.upgradeaffects.all;
                                        } else if (v2.contains("fresh")) {
                                            au.affects = de._13ducks.cor.game.ability.AbilityUpgrade.upgradeaffects.fresh;
                                        } else if (v2.contains("old")) {
                                            au.affects = de._13ducks.cor.game.ability.AbilityUpgrade.upgradeaffects.old;
                                        } else if (v2.contains("self")) {
                                            au.affects = de._13ducks.cor.game.ability.AbilityUpgrade.upgradeaffects.self;
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
                //rgi.rogGraphics.displayError("Fähigkeitenbeschreibung \"game/abilities\" nicht gefunden");
            } catch (IOException ex) {
                //rgi.rogGraphics.displayError("Fähigkeitenbeschreibung \"game/abilities\" kann nicht gelesen werden");
            } finally {
                try {
                    bdescReader.close();
                } catch (IOException ex) {
                }
            }
        

        //Server?
            // Read abilitys
            try {
                bdescReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(abBuffer)));
                String zeile = null;
                // Einlesen
                boolean inDesc = false;
                ServerAbilityUpgrade au = null;
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
                                        if ("upgrade".equals(v2)) {
                                            // Type RogGameObjectAbilityUrgrade
                                            au = new ServerAbilityUpgrade(id);
                                            au.rgi = serverrgi;
                                            type = 3;
                                        }
                                    }
                                } else if (type == 3) {
                                    if ("affects".equals(v1)) {
                                        if (v2.contains("all")) {
                                            au.affects = de._13ducks.cor.game.ability.ServerAbilityUpgrade.upgradeaffects.all;
                                        } else if (v2.contains("fresh")) {
                                            au.affects = de._13ducks.cor.game.ability.ServerAbilityUpgrade.upgradeaffects.fresh;
                                        } else if (v2.contains("old")) {
                                            au.affects = de._13ducks.cor.game.ability.ServerAbilityUpgrade.upgradeaffects.old;
                                        } else if (v2.contains("self")) {
                                            au.affects = de._13ducks.cor.game.ability.ServerAbilityUpgrade.upgradeaffects.self;
                                        }
                                    } else if ("uptype".equals(v1)) {
                                        if (v2.equals("epoche")) {
                                            au.epocheUpgrade = true;
                                        }
                                    } else if ("reference".equals(v1)) {
                                        if (v2.startsWith("U")) {
                                            au.descTypeIdU = saveStrtoInt(v2.substring(1), zeile, line);
                                        } else if (v2.startsWith("B")) {
                                            au.descTypeIdB = saveStrtoInt(v2.substring(1), zeile, line);
                                        }
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
                                    } else if ("maxIntraup".equals(v1)) {
                                        au.maxIntraup = saveStrtoInt(v2, zeile, line);
                                    } else if ("damageup".equals(v1)) {
                                        au.damageup = saveStrtoInt(v2, zeile, line);
                                    } else if ("bulletspeedup".equals(v1)) {
                                        au.bulletspeedup = saveStrtoInt(v2, zeile, line);
                                    } else if ("rangeup".equals(v1)) {
                                        au.rangeup = saveStrtoInt(v2, zeile, line);
                                    } else if ("speedup".equals(v1)) {
                                        au.speedup = saveStrtoDouble(v2, zeile, line);
                                    } else if ("harv".equals(v1)) {
                                        if (v2.contains("true")) {
                                            au.harv = true;
                                        }
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
                                            } else if ("toAnimDesc".equals(v1s)) {
                                                dup.toAnimDesc = saveStrtoInt(v2, zeile, line);
                                            } else if ("maxIntraup".equals(v1s)) {
                                                dup.maxIntraup = saveStrtoInt(v2, zeile, line);
                                            } else if ("healup".equals(v1s)) {
                                                dup.healup = saveStrtoInt(v2, zeile, line);
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
                                            } else if ("bulletspeedup".equals(v1)) {
                                                dup.bulletspeedup = saveStrtoInt(v2, zeile, line);
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
                                    }
                                }
                            }
                        }
                    }

                    if (zeile.contains("}")) {
                        // Fertig - einfügen
                        if (au != null) {
                            descTypeAbilities.put(id, au);
                        }
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
            } catch (IOException ex) {
            }
        


            // Read game/descTypes

            try {
                bdescReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(descBuffer)));
                String zeile = null;
                // Einlesen
                boolean inDesc = false;
                String mode = null;
                DescParamsBuilding rB = null;
                DescParamsUnit rU = null;
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
                                    } else if (v1.equals("healrate")) {
                                        rB.setHealRate(saveStrtoInt(v2, zeile, line));
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
                                    } else if (v1.equals("size")) {
                                        if ("3x3".equals(v2)) {
                                            rU.setSize(3);
                                        }
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
                                descTypeUnit.put(id, rU.getSize() == 3 ? new Unit3x3(rU) : new Unit2x2(rU));
                                inDesc = false;
                            } else if (mode.equals("B")) {
                                rB.setDescTypeId(id);
                                descTypeBuilding.put(id, new Building(rB));
                                inDesc = false;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException fnfe) {
            } catch (IOException ioe) {
            }

        
        Returndingens.setBuildings(descTypeBuilding);
        Returndingens.setUnits(descTypeUnit);
        Returndingens.setAbilities(descAbilities);
        Returndingens.setServerabilities(descTypeAbilities);

        return Returndingens;
    }

    public static GameDescParams readDesc(String path, int mapMode, ClientCore.InnerClient rgi, ServerCore.InnerServer serverrgi) {

        byte[] abBuffer;
        byte[] descBuffer;

        // Dateiliste erstellen

        ArrayList<File> olist = new ArrayList<File>();
        ArrayList<File> flist = new ArrayList<File>();
        ArrayList<File> dlist = new ArrayList<File>();
        ArrayList<File> alist = new ArrayList<File>();

        File OrdnerSuchen = new File("game/epoch/"); //Unterordner in "game" suchen
        File[] Ordner = OrdnerSuchen.listFiles();
        for (File ord : Ordner) {
            if (ord.isDirectory() && !ord.getName().startsWith(".") && !ord.getName().startsWith(".")) {
                olist.add(ord);
            }
        }

        for (int i = 0; i < olist.size(); i++) { //Dateien in den Unterordnern suchen
            String Unterordner = olist.get(i).toString();
            File gameOrdner = new File(Unterordner);
            File[] files = gameOrdner.listFiles();
            flist.addAll(Arrays.asList(files));
        }

        for (int i = 0; i < flist.size(); i++) {
            File file = flist.get(i);
            if (file.getName().endsWith("~") || file.getName().startsWith(".")) { // Sicherungsdateien und unsichtbares Aussortieren
                flist.remove(i);
                i--;
            } else {
                if (file.getName().startsWith("a") || file.getName().startsWith("A")) {
                    alist.add(file);
                } else if (file.getName().startsWith("d") || file.getName().startsWith("D")) {
                    dlist.add(file);
                }
            }
        }
        // Abilitys einmal reinladen für die Clients
        FileInputStream stream = null;
        // Puffer-Größe berechnen
        int buffSize = 0;
        for (File abFile : alist) {
            buffSize += abFile.length();
            buffSize++; // Zeilensprung
        }
        abBuffer = new byte[buffSize];
        int runIndex = 0;
        for (File abFile : alist) {
            try {
                stream = new FileInputStream(abFile);
                while (stream.available() > 0) {
                    abBuffer[runIndex] = (byte) stream.read();
                    runIndex++;
                }
                // Zeilensprung
                abBuffer[runIndex] = '\n';
                runIndex++;
            } catch (FileNotFoundException ex) {

            } catch (IOException ex) {

            } finally {
                try {
                    stream.close();
                } catch (IOException ex) {
                }
            }
        }
        // Einglesen, normal weitermachen.

        // DESC einmal reinladen für die Clients

        FileInputStream stream2 = null;
        // Puffer-Größe berechnen
        int buffSize2 = 0;
        for (File deFile : dlist) {
            buffSize2 += deFile.length();
            buffSize2 += 1; // Einen Zeilensprung einfügen
        }
        descBuffer = new byte[buffSize2];
        int runIndex2 = 0;
        for (File deFile : dlist) {
            try {
                stream = new FileInputStream(deFile);
                while (stream.available() > 0) {
                    descBuffer[runIndex2] = (byte) stream.read();
                    runIndex2++;
                }
                // Zeilensprung
                descBuffer[runIndex2] = '\n';
                runIndex2++;
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            } finally {
                try {
                    stream.close();
                } catch (IOException ex) {
                }
            }
        }

        GameDescParams returnparams = readDescFromStream(descBuffer, abBuffer, mapMode, rgi, serverrgi);
        return returnparams;
    }

    private static int saveStrtoInt(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        int i;
        try {
            i = Integer.parseInt(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            return 1;
        }
    }

    private static double saveStrtoDouble(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        double i;
        try {
            i = Double.parseDouble(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            return 1;
        }
    }
}
