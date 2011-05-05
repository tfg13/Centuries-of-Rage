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
package de._13ducks.cor.networks.cmd.client;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.client.ClientCore.InnerClient;
import de._13ducks.cor.networks.client.ClientNetController.ClientHandler;
import de._13ducks.cor.networks.cmd.ClientCommand;

/**
 * Einheiten-Vektorbewegung
 */
public class C023_MOVE_UNIT extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Einheite suchen
        Unit unit23 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
        if (unit23 != null) {
            // Geschwindigkeit und Ziel rausbasteln
            float speed = Float.intBitsToFloat(rgi.readInt(data, 2));
            FloatingPointPosition pos = new FloatingPointPosition(Float.intBitsToFloat(rgi.readInt(data, 3)), Float.intBitsToFloat(rgi.readInt(data, 4)));
            System.out.println(unit23 + " moves with speed: " + speed + " to " + pos);
        } else {
            System.out.println("Unit id mismatch (cmd 23)");
        }
    }
    
}
