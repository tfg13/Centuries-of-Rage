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

import java.util.ArrayList;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Startet eine Wegübertragung
 */
public class C023_INIT_MOVE extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Bewegung einleiten
        handler.temp04 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
        if (handler.temp04 != null) {
            handler.temp05 = rgi.readPosition(data, 2);
            handler.temp06 = new ArrayList<Position>();
            if (!handler.temp04.jumpJustSet) {
                handler.temp04.jumpTo = 0;
            } else {
                handler.temp04.jumpJustSet = false;
            }
        } else {
            System.out.println("FixMe: Unit ID mismatch (cmd23)");
        }
    }
}
