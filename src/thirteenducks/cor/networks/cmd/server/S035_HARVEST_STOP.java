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
package thirteenducks.cor.networks.cmd.server;

import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Stoppt das Ernten
 */
public class S035_HARVEST_STOP extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Stoppe Ernten
        // Ressource suchen
        Ressource res35 = rgi.netmap.getRessourceviaID(rgi.readInt(data, 1));
        if (res35 != null) {
            // Ernter suchen
            Unit unit35 = rgi.netmap.getUnitviaID(rgi.readInt(data, 2));
            if (unit35 != null) {
                // Alles soweit ok, weiter schicken
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 35, res35.netID, unit35.netID, 0, 0));
            } else {
                System.out.println("FixMe: Unit ID mismatch (cmd35)");
            }
        } else {
            System.out.println("FixMe: ResourceID mismatch (cmd35)");
        }
    }
}
