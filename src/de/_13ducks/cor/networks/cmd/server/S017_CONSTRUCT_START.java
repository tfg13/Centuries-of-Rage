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

import de._13ducks.cor.game.Unit;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import de._13ducks.cor.game.networks.behaviour.impl.ServerBehaviourConstruct;
import de._13ducks.cor.game.server.ServerCore.InnerServer;
import de._13ducks.cor.networks.server.ServerNetController.ServerHandler;
import de._13ducks.cor.networks.cmd.ServerCommand;

/**
 * Startet ein Bauvorhaben / lässt es weiter bauen.
 */
public class S017_CONSTRUCT_START extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Alles Gebäudebau, verarbeiten
        // Unit suchen - int1
        try {
            Unit unit = rgi.netmap.getUnitviaID(rgi.readInt(data, 1));
            // Behaviour ID 5
            ServerBehaviour be = unit.getServerBehaviour(5);
            if (be == null) {
                // Neu anlegen
                be = new ServerBehaviourConstruct(rgi, unit, 2);
                unit.addServerBehaviour(be);
            }
            be.gotSignal(data);
        } catch (Exception ex) {
            System.out.println("FixMe: Unit ID mismatch (cmd15,16)");
        }
    }
}
