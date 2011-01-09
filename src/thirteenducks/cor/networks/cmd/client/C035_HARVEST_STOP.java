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

import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourHarvest;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Unit.actions;
import thirteenducks.cor.game.Unit.orders;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Stoppt das Ernten einer Einheit
 */
public class C035_HARVEST_STOP extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Stoppe Ernten
        // Ressource suchen
        Ressource res35 = rgi.mapModule.getRessourceviaID(rgi.readInt(data, 1));
        if (res35 != null) {
            // Ernter suchen
            Unit unit35 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 2));
            if (unit35 != null) {
                // Alles soweit ok
                // Sind wir das selber?
                if (unit35.playerId == rgi.game.getOwnPlayer().playerId) {
                    // Ja, Behaviour benachrichtigen
                    ((ClientBehaviourHarvest) unit35.getbehaviourC(7)).stopHarvesting();
                    unit35.action = actions.nothing;
                    
                } else {
                    // Order ändern
                    if (unit35.isMoving()) {
                        unit35.order = orders.move;
                    } else {
                        unit35.order = orders.idle;
                    }
                }
            } else {
                System.out.println("FixMe: Unit ID mismatch (cmd35)");
            }
        } else {
            System.out.println("FixMe: ResourceID mismatch (cmd35)");
        }
    }
}
