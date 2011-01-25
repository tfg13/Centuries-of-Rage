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
package thirteenducks.cor.networks.cmd.client;

import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.game.ability.AbilityUpgrade.upgradeaffects;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Ein DELTA-Upgrade das einzelne Werte verändern kann, soll ausgeführt werden.
 */
public class C037_UPGRADE_DELTA extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Deltaupgrade
        //GameObject suchen
        GameObject go37 = rgi.mapModule.getGameObjectviaID(rgi.readInt(data, 1));
        if (go37 != null) {
            // Ability suchen
            AbilityUpgrade up = (AbilityUpgrade) rgi.game.getPlayer(go37.getPlayerId()).clientDescAbilities.get(rgi.readInt(data, 2));
            if (up != null) {
                // Jetzt performen:
                if (up.affects == upgradeaffects.self) {
                    go37.performDeltaUpgrade(rgi, up);
                } else if (up.affects == upgradeaffects.old) {
                    if (up.descTypeIdU != 0) {
                        for (Unit unit37 : rgi.mapModule.unitList) {
                            if (unit37.getDescTypeId() == up.descTypeIdU && unit37.getPlayerId() == go37.getPlayerId()) {
                                unit37.performDeltaUpgrade(rgi, up);
                            }
                        }
                    } else {
                        for (Building building37 : rgi.mapModule.buildingList) {
                            if (building37.getDescTypeId() == up.descTypeIdB && building37.getPlayerId() == go37.getPlayerId()) {
                                building37.performDeltaUpgrade(rgi, up);
                            }
                        }
                    }
                } else if (up.affects == upgradeaffects.fresh) {
                    if (up.descTypeIdU != 0) {
                        rgi.game.getPlayer(go37.getPlayerId()).descUnit.get(up.descTypeIdU).performDeltaUpgrade(rgi, up);
                    } else {
                        rgi.game.getPlayer(go37.getPlayerId()).descBuilding.get(up.descTypeIdB).performDeltaUpgrade(rgi, up);
                    }
                } else if (up.affects == upgradeaffects.all) {
                    if (up.descTypeIdU != 0) {
                        for (Unit unit37 : rgi.mapModule.unitList) {
                            if (unit37.getDescTypeId() == up.descTypeIdU && unit37.getPlayerId() == go37.getPlayerId()) {
                                unit37.performDeltaUpgrade(rgi, up);
                            }
                        }
                    } else {
                        for (Building building37 : rgi.mapModule.buildingList) {
                            if (building37.getDescTypeId() == up.descTypeIdB && building37.getPlayerId() == go37.getPlayerId()) {
                                building37.performDeltaUpgrade(rgi, up);
                            }
                        }
                    }
                    if (up.descTypeIdU != 0) {
                        rgi.game.getPlayer(go37.getPlayerId()).descUnit.get(up.descTypeIdU).performDeltaUpgrade(rgi, up);
                    } else {
                        rgi.game.getPlayer(go37.getPlayerId()).descBuilding.get(up.descTypeIdB).performDeltaUpgrade(rgi, up);
                    }
                }
            } else {
                System.out.println("FixMe: Ability ID mismatch (cmd37)");
            }
        } else {
            System.out.println("FixMe: GameObject ID mismatch (cmd37)");
        }
    }
}
