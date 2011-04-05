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
package de._13ducks.cor.networks.cmd.server;

import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.server.ServerCore.InnerServer;
import de._13ducks.cor.networks.server.ServerNetController.ServerHandler;
import de._13ducks.cor.networks.cmd.ServerCommand;

/**
 * Gebäude hinzufügen (als Baustelle)
 */
public class S018_ADD_BUILDING extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Gebäude adden, als Baustelle
        // Holen:
        Building b = rgi.netmap.getDescBuilding(handler.client.playerId, rgi.readInt(data, 2));
        if (b != null) {
            b.setMainPosition(rgi.readPosition(data, 2));
            b.setPlayerId(handler.client.playerId);
            b.setHitpoints(b.getMaxhitpoints() / 4);
            rgi.netmap.addBuildingAsSite(b);
        } else {
            System.out.println("FixMe: Invalid ID'S (cmd18)");
        }
    }
}
