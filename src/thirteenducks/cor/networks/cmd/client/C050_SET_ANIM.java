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
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Setzt bestimmte Animationen auf allen Clients
 */
public class C050_SET_ANIM extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Animationsdaten
        // Typ laden
        int animType = rgi.readInt(data, 1);
        if (animType == 1) {
            // Richtung ändern
            // Nur Einheiten haben Richtungen:
            Unit unit50 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 2));
            if (unit50 != null) {
                if (unit50.playerId != rgi.game.getOwnPlayer().playerId) {
                    if (unit50.anim != null) {
                        unit50.anim.dir = rgi.readInt(data, 3);
                    }
                }
            } else {
                System.out.println("FixMe: Unit ID mismatch (cmd50-1)");
            }
        } else if (animType == 2) {
            // Gebäude-Modus setzen
            Building building50 = rgi.mapModule.getBuildingviaID(rgi.readInt(data, 2));
            if (building50 != null) {
                // Nur fremde
                if (building50.playerId != rgi.game.getOwnPlayer().playerId) {
                    building50.isWorking = rgi.readInt(data, 3) == 1;
                }
            } else {
                System.out.println("FixMe: Building ID mismatch (cmd50-2)");
            }
        }
    }
}
