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
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Evakuiert eine Einheit wieder aus einem Gebäude
 */
public class C042_EVACUATE_UNIT extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Unit wieder evakuieren
        Building building42 = rgi.mapModule.getBuildingviaID(rgi.readInt(data, 1));
        if (building42 != null) {
            // Einheit suchen
            Unit unit42 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 2));
            Position pos42 = new Position(rgi.readInt(data, 3), rgi.readInt(data, 4));
            if (unit42 != null) {
                if (pos42 != null) {
                    building42.removeUnit(unit42, pos42, rgi);
                } else {
                    System.out.println("FixMe: Position ID mismatch (cmd42)");
                }
            } else {
                System.out.println("FixMe: Unit ID mismatch (cmd42)");
            }
        } else {
            System.out.println("FixMe: Building ID mismatch (cmd42)");
        }
    }
}
