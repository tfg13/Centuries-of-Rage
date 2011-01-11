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
import thirteenducks.cor.networks.client.ClientNetController;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Befiehlt dem Client eine Map zu laden.
 * Dieses Packet intiiert nur die Übertragung des Map-Namens.
 */
public class C001_LOAD_MAP_INIT extends ClientCommand {

    @Override
    public void process(byte[] data, ClientNetController.ClientHandler handler, InnerClient rgi) {
        rgi.rogGraphics.setLoadStatus(6);
        // Map laden
        int maphash = rgi.readInt(data, 1);
        if (maphash != 0) {
            System.out.println("Loading map, hash: " + maphash);
            handler.temp01 = maphash;
            handler.temp02 = "";
        }
    }
}
