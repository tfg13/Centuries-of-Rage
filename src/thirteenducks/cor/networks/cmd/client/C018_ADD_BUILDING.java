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
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Fügt ein Gebäude (als Baustelle) dem Spiel hinzu
 */
public class C018_ADD_BUILDING extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Gebäude adden, als Baustelle
        // Holen:
        Building b = rgi.mapModule.getDescBuilding(rgi.readInt(data, 2), rgi.readInt(data, 1), rgi.game.getOwnPlayer().playerId);
        if (b != null) {
            b.setMainPosition(rgi.readPosition(data, 2));
            System.out.println("AddMe: Set Lifestatus to unborn!");
            //b.ready = false;
            System.out.println("AddMe: Set Buildings hitpoints");
            //b.hitpoints = b.getMaxhitpoints() / 4;

            rgi.mapModule.addBuilding(b);
        } else {
            System.out.println("FixMe: Invalid ID'S (cmd18)");
        }
    }
}
