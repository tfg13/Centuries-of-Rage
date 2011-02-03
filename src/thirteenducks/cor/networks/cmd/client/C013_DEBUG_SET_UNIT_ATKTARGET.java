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
 * Setzt das Angriffsziel einer Einheit auch beim Client.
 * Eigentlich braucht der Client das nicht, das ist nur für graphische Debug-
 * Geschichten gedacht.
 */
public class C013_DEBUG_SET_UNIT_ATKTARGET extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Einheitenangriffziele - Debug
        // Einheit suchen
      /*  Unit unit13 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
        if (unit13 != null) {
            // Ziel suchen
            if (rgi.readInt(data, 2) != 0) {
                GameObject go13 = rgi.mapModule.getGameObjectviaID(rgi.readInt(data, 2));
                if (go13 != null) {
                    // Setzen
                    unit13.attacktarget = go13;
                } else {
                    System.out.println("FixMe: GO ID mismatch (cmd13) (minor)");
                }
            } else {
                unit13.attacktarget = null;
            }
        } else {
            System.out.println("FixMe: Unit ID mismatch (cmd13) (minor)");
        } */
        System.out.println("AddMe: ReImplement set DEBUG-ATK");
    }
}
