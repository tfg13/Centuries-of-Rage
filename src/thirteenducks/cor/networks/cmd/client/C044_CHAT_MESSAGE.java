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
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Beinhaltet den Nachrichtentexg einer Chat-Nachricht. Kann wiederholt werden.
 * Ende mit \0
 */
public class C044_CHAT_MESSAGE extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Chat
        char c44;
        for (int i = 1; i < 9; i++) {
            c44 = rgi.readChar(data, i);
            if (c44 != '\0') {
                handler.stringb44 = handler.stringb44 + c44;
            } else {
                // Fertig, Chat-Input verarbeiten
                if (rgi.isAIClient) {
                    rgi.aiModule.chatMessageEvent(handler.stringb44, handler.playerId45);
                } else {
                    rgi.chat.addMessage(handler.stringb44, handler.playerId45);
                }
                handler.stringb44 = "";
                break;
            }
        }
    }
}
