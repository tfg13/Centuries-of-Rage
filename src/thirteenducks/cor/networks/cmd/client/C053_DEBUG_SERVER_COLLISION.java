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

import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Reine Debug-Infos. Überträgt die Kollisionsdaten vom Server.
 * Damit wird eine visuelle Darstellung auf dem Client möglich,
 * mit der ein Debuggen der Server-Kollision vereinfacht wird.
 */
public class C053_DEBUG_SERVER_COLLISION extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        Position pos = rgi.readPosition(data, 1);
        if (pos != null) {
            try {
                if (rgi.readInt(data, 3) == 1) {
                    rgi.mapModule.serverCollision[pos.getX()][pos.getY()] = true;
                } else {
                    rgi.mapModule.serverCollision[pos.getX()][pos.getY()] = false;
                }
            } catch (Exception ex) {
            }
        }
    }
}
