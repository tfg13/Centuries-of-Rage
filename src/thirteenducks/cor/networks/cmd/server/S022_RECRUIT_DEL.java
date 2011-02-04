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

import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.networks.behaviour.impl.ServerBehaviourRecruit;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Löscht eine Einheit aus der Bauschleife
 */
public class S022_RECRUIT_DEL extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Behaviour Nr 6 - Rekrutieren
        try {
            GameObject caster = rgi.netmap.getGameObjectviaID(rgi.readInt(data, 1));
            // Behaviour ID 6
            ServerBehaviour be = caster.getServerBehaviour(6);
            if (be == null) {
                // Neu anlegen
                be = new ServerBehaviourRecruit(rgi, caster, 20);
                caster.addServerBehaviour(be);
            }
            be.gotSignal(data);
        } catch (Exception ex) {
            System.out.println("FixMe: Object ID mismatch (cmd22)");
        }
    }
}
