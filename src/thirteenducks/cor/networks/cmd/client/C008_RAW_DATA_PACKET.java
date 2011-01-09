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
 * Ein wiederholt gesendetes Datenpacket, das reine binärdaten enthält.
 * Muss duch Cmd09 angekündigt werden, damit die Größe bekannt ist.
 */
public class C008_RAW_DATA_PACKET extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Datenpaket, in den Puffer schreiben
        for (int i = 1; i < 17; i++) {
            handler.temp08[handler.temp08index] = data[i];
            handler.temp08index++;
            if (handler.temp08index == handler.temp08.length) {
                break;
            }
        }
        if (handler.temp08index == handler.temp08.length) {
            // Fertig
            if (handler.temp09 == 3) {
                rgi.mapModule.abilitySettings = handler.temp08;
                rgi.mapModule.gotAbilities();
            } else if (handler.temp09 == 2) {
                rgi.mapModule.descSettings = handler.temp08;
                rgi.mapModule.gotDesc();
            } else if (handler.temp09 == 1) {
                rgi.mapModule.mapData = handler.temp08;
                rgi.mapModule.receivedMap();
            }
        }
    }
}
