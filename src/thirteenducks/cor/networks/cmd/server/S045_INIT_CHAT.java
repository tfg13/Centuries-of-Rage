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
package thirteenducks.cor.networks.cmd.server;

import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Spezial-Chat einleiten
 */
public class S045_INIT_CHAT extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Special-Chat
        int type45 = rgi.readInt(data, 1);
        int dta45 = rgi.readInt(data, 2);
        if (type45 == -2) {
            // Ein Client hat ne neue Epoche erreicht
            // Das allen Sagen
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 45, -2, 0, 0, 0));
            rgi.netctrl.broadcastString(handler.client.nickName + " reached epoche " + dta45, (byte) 44);
        }
    }
}
