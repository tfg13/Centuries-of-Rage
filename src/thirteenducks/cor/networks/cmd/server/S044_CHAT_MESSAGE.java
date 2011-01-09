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
 * Übertragung der Chat-Message
 */
public class S044_CHAT_MESSAGE extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Chat
        char c44;
        for (int i = 1; i < 9; i++) {
            c44 = rgi.readChar(data, i);
            if (c44 != '\0') {
                handler.stringb44 = handler.stringb44 + c44;
            } else {
                // Fertig, Chat-Input an die Clients verschicken
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 45, handler.client.playerId, 0, 0, 0));
                rgi.netctrl.broadcastString(handler.stringb44, (byte) 44);
                handler.stringb44 = "";
                break;
            }
        }
    }
}
