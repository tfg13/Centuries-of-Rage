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

import de._13ducks.cor.game.server.ServerCore.InnerServer;
import de._13ducks.cor.networks.server.ServerNetController.ServerHandler;
import de._13ducks.cor.networks.cmd.ServerCommand;

/**
 * Der Client fordert Daten an.
 */
public class S009_REQUEST_DATA extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Request vom Client, er will Daten.
        int request = rgi.readInt(data, 1);
        switch (request) {
            case 1: // Map
            case 2: // DESC
            case 3: // Abilitys
                // Zeug holen
                byte[] buffer = null;
                if (request == 3) {
                    buffer = rgi.netmap.abBuffer;
                } else if (request == 2) {
                    buffer = rgi.netmap.descBuffer;
                } else if (request == 1) {
                    buffer = rgi.netmap.mapBuffer;
                }
                // Ability-Transfer ankündigen
                handler.sendDATA(rgi.packetFactory((byte) 9, request, buffer.length, 0, 0));
                // Transferieren
                int i = 0;
                while (true) {
                    // Richtig auffüllen
                    int rest = buffer.length - i;
                    if (rest >= 16) {
                        // Alles vollmachen und ab
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], buffer[i + 9], buffer[i + 10], buffer[i + 11], buffer[i + 12], buffer[i + 13], buffer[i + 14], buffer[i + 15]));
                    } else if (rest == 15) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], buffer[i + 9], buffer[i + 10], buffer[i + 11], buffer[i + 12], buffer[i + 13], buffer[i + 14], (byte) 'a'));
                    } else if (rest == 14) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], buffer[i + 9], buffer[i + 10], buffer[i + 11], buffer[i + 12], buffer[i + 13], (byte) 'a', (byte) 'a'));
                    } else if (rest == 13) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], buffer[i + 9], buffer[i + 10], buffer[i + 11], buffer[i + 12], (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 12) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], buffer[i + 9], buffer[i + 10], buffer[i + 11], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 11) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], buffer[i + 9], buffer[i + 10], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 10) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], buffer[i + 9], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 9) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], buffer[i + 8], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 8) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], buffer[i + 7], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 7) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], buffer[i + 6], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 6) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], buffer[i + 5], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 5) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], buffer[i + 4], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 4) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 3) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], buffer[i + 2], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 2) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], buffer[i + 1], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest == 1) {
                        handler.sendDATA(rgi.packetFactory((byte) 8, buffer[i], (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a', (byte) 'a'));
                    } else if (rest <= 0) {
                        break;
                    }
                    i += 16;
                }
                return;
        }
    }
}
