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

import java.util.ArrayList;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.cmd.ServerCommand;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;

/**
 * 
 * Mehrfachbewegung. Dieser neue Befehl ist nötig, weil der Server jetzt die Bewegungen einheitlich berechnet.
 * Da bei einer Gruppenbewegung erst alle Felder aller Einheiten freigegeben werden müssen, muss der Server sofort
 * Alle Einheiten kenne, die zur Gruppe gehören. Ein Hintereinander Schicken von einzelnen Bewegungsbefehlen reicht nicht mehr.
 *
 * Befehl wird wiederholt, um alle Einheiten der Gruppe zu benennen
 *
 * @author tfg
 */
public class S052_MOVE_GROUP extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Position schon gesetzt? Wenn nicht, suchen
        int sindex = 1;
        if (handler.temps052_1 == null) {
            // Ziel suchen
            handler.temps052_1 = rgi.readPosition(data, 1);
            handler.temps052_2 = new ArrayList<Unit>();
            sindex = 3;
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
            rgi.moveMan.humanGroupMove(handler.temps052_1, handler.temps052_2);
            handler.temps052_1 = null;
        }
    }
}
