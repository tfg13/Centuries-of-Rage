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
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Lässt eine Einheit in ein Gebäude jumpen
 */
public class C041_UNIT_JUMP extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Jumpen, in Gebäude
        // Unit und Ziel suchen
        Unit unit41 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
        if (unit41 != null) {
            Building building41 = rgi.mapModule.getBuildingviaID(rgi.readInt(data, 2));
            if (building41 != null) {
                // Jumpen
                if (building41.intraFree() > 0) {
                    building41.addIntra(unit41, rgi);
                }
            } else {
                System.out.println("FixMe: Building ID mismatch (cmd40)");
            }
        } else {
            System.out.println("FixMe: Unit ID mismatch (cmd40)");
        }
    }
}
