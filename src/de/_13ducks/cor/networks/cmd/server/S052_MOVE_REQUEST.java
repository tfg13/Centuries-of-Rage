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

import java.util.ArrayList;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.ServerCore.InnerServer;
import de._13ducks.cor.networks.cmd.ServerCommand;
import de._13ducks.cor.networks.server.ServerNetController.ServerHandler;

/**
 * Der Client möchte eine oder mehrere Einheiten bewegen.
 *
 * Befehl wird wiederholt, um alle Einheiten der Gruppe zu benennen
 *
 * @author tfg
 */
public class S052_MOVE_REQUEST extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Position schon gesetzt? Wenn nicht, suchen
        int sindex = 1;
        if (handler.temps052_1 == null) {
            // Ziel suchen
            handler.temps052_1 = rgi.readFloatingPointPosition(data, 1);
            handler.temps052_2 = new ArrayList<Unit>();
            sindex = 3;
            // Hack, negative X-Koordinate bedeuten Fluchtmodus
            if (handler.temps052_1.getfX() < 0) {
                handler.temps052_1.setfX(handler.temps052_1.getfX() * -1);
                handler.temps052_movemode = true;
            } else {
                handler.temps052_movemode = false;
            }
        }
        // Einheiten auslesen. Wenn keine mehr da sind, wars das letzte Packet, dann losschicken
        int id = 0;
        while (sindex < 5 && (id = rgi.readInt(data, sindex)) != 0) {
            // Ist das ne Unit?
            Unit mover = rgi.netmap.getUnitviaID(id);
            if (mover != null) {
                handler.temps052_2.add(mover);
            } else {
                System.out.println("FixMe: Unit ID mismatch (cmd52s) + ID: " + id);
            }
            sindex++;
        }
        // Kommt noch was?
        if (id == 0) {
            // Nein, fertig, bewegen
            rgi.moveMan.moveRequest(handler.temps052_1, handler.temps052_2, handler.temps052_movemode);
            handler.temps052_1 = null;
        }
    }
}
