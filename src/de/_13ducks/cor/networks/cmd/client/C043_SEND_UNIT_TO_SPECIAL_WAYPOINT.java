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
package de._13ducks.cor.networks.cmd.client;

import de._13ducks.cor.game.client.ClientCore.InnerClient;
import de._13ducks.cor.networks.client.ClientNetController.ClientHandler;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.networks.cmd.ClientCommand;

/**
 * Schickt eine Einheit zu ihrem speziellen Wegpunkt, das ist:
 * Eine Ressource (dann erntet die Einheit sofort los)
 * Ein Ressourcen-Gebäude, dann erntet die Einheit dort sofort
 */
public class C043_SEND_UNIT_TO_SPECIAL_WAYPOINT extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
       /* // Einheit als Startpunkt zu Ressource/Gebäude schicken
        // Unit suchen
        Unit unit43 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
        if (unit43 != null) {
            // Ziel Ressource oder Gebäude?
            Ressource res43 = rgi.mapModule.getRessourceviaID(rgi.readInt(data, 2));
            if (res43 != null) {
                // Zu dieser oder einer Ähnlichen Ressource schicken
                if (res43.readyForAnotherHarvester(rgi)) {
                    unit43.goHarvest(res43, rgi);
                } else {
                    // Benachbarte suchen
                    Ressource potRes = unit43.ressourceAroundMe(res43.getType(), 10, rgi);
                    if (potRes != null) {
                        unit43.goHarvest(potRes, rgi);
                    } else {
                        // Dann halt wenigstens in die Nähe laufen
                        unit43.sendToPosition(rgi.readPosition(data, 2), rgi, true);
                    }
                }
            } else {
                // RessourcenGebäude versuchen
                Building b43 = rgi.mapModule.getBuildingviaID(rgi.readInt(data, 2));
                if (b43.accepts == Building.ACCEPTS_ALL || unit43.canHarvest) {
                    if (b43 != null) {
                        // Zu diesem oder einem ähnlichen Ressourcengebäude schicken
                        if (b43.intraFree() > 0) {
                            b43.goIntra(unit43, rgi);
                        } else {
                            // Ein alternatives Gebäude suchen
                            boolean found = false;
                            for (int i = 0; i < rgi.mapModule.buildingList.size(); i++) {
                                Building pot = rgi.mapModule.buildingList.get(i);
                                if (pot.playerId == rgi.game.getOwnPlayer().playerId && pot.descTypeId == b43.descTypeId && pot.intraFree() > 0) {
                                    pot.goIntra(unit43, rgi);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Wenn nichts gefunden zur Defaultposition gehen
                                unit43.sendToPosition(rgi.readPosition(data, 2), rgi, true);

                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("FixMe: Unit ID mismatch (cmd43)");
        }
        * */


        System.out.println("AddMe: Send Unit to Special Waypoint?");
    }
}
