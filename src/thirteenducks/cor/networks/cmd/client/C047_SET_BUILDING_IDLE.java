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
package thirteenducks.cor.networks.cmd.client;

import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Aktiviert/Deaktiviert das Client-Zielsuchen für Gebäude
 */
public class C047_SET_BUILDING_IDLE extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Idle an, nur für eigene Buildings
        Building b47 = rgi.mapModule.getBuildingviaID(rgi.readInt(data, 1));
        if (b47 != null) {
            if (b47.playerId == rgi.game.getOwnPlayer().playerId) {
                if (rgi.readInt(data, 2) == 1) {
                    b47.getbehaviourC(10).activate();
                } else {
                    b47.getbehaviourC(10).deactivate();
                }
            }
        } else {
            System.out.println("FixMe: Building ID mismatch (cmd40)");
        }
    }
}
