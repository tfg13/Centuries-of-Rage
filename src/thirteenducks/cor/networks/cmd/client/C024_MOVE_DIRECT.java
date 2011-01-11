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
import thirteenducks.cor.game.Position;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Ein Weg für eine direkte Bewegung
 */
public class C024_MOVE_DIRECT extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Weg empfangen & anhängen - für DirectMove
        for (int i = 1; i < 3; i++) {
            Position tpos = rgi.readPosition(data, i);
            if (tpos != null) {
                if (tpos.X == -1 && tpos.Y == -1) {
                    // Ende der Übertragung
                    // Loslaufen
                    handler.temp04.applyNewPath(rgi, handler.temp06);
                    break;
                } else {
                    handler.temp06.add(tpos);
                }
            } else {
                System.out.println("FixMe: Error transmitting Path (24) - aborting");
            }
        }
    }
}
