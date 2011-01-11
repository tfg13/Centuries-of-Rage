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
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Überträgt bis zu 8 Zeichen des Dateinamens der Map.
 * Wird bei Bedarf wiederholt.
 * Ende mit \0
 */
public class C002_LOAD_MAP_PATH extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Name für Map laden (1)
        char c;
        for (int i = 1; i < 9; i++) {
            c = rgi.readChar(data, i);
            if (c != '\0') {
                handler.temp02 = handler.temp02 + c;
            } else {
                // Fertig, map jetzt laden
                System.out.println("Loading map: " + handler.temp02);
                // Map laden
                rgi.mapModule.loadMap(handler.temp02, handler.temp01);
                handler.temp01 = 0;
                handler.temp02 = "";
                break;
            }
        }
    }
}
