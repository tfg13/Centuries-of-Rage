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
import thirteenducks.cor.game.Unit.orders;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Startet das Ernten
 */
public class C034_HARVEST_START extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Beginne zu Ernten
        // Ressource suchen
        Ressource res34 = rgi.mapModule.getRessourceviaID(rgi.readInt(data, 1));
        if (res34 != null) {
            // Ernter suchen
            Unit unit34 = rgi.mapModule.getUnitviaID(rgi.readInt(data, 2));
            if (unit34 != null) {
                // Alles ok, ernten, falls wir das sind, sonst nur order setzen
                if (unit34.playerId == rgi.game.getOwnPlayer().playerId) {
                    // Wir sind das, Ernte-Behaviour loslaufen lassen
                    ClientBehaviourHarvest harvb = (ClientBehaviourHarvest) unit34.getbehaviourC(7);
                    if (!unit34.isMoving()) {
                        harvb.startHarvesting(unit34.position, res34);
                    } else {
                        harvb.startHarvesting(unit34.movingtarget, res34);
                    }
                } else {
                    unit34.order = orders.harvest;
                }
                // Drehen, falls wir uns nicht bewegen
                if (!unit34.isMoving()) {
                    unit34.anim.dir = res34.position.subtract(unit34.position).transformToIntVector();
                }
            } else {
                System.out.println("FixMe: Unit ID mismatch (cmd34)");
            }
        } else {
            System.out.println("FixMe: ResourceID mismatch (cmd34)");
        }
    }
}
