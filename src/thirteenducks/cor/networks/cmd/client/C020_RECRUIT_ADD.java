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

import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourRecruit;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Fügt einen Rekrutierungsbefehl hinzu
 */
public class C020_RECRUIT_ADD extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Behaviour Nr 6 - Rekrutieren
        try {
            GameObject caster = rgi.mapModule.getGameObjectviaID(rgi.readInt(data, 1));
            if (caster.playerId == rgi.game.getOwnPlayer().playerId) {
                // Behaviour ID 6
                ClientBehaviour be = caster.getbehaviourC(6);
                if (be == null) {
                    // Neu anlegen
                    be = new ClientBehaviourRecruit(rgi, caster, 20);
                    caster.cbehaviours.add(be);
                }
                be.gotSignal(data);
            }
        } catch (Exception ex) {
            System.out.println("FixMe: Object ID mismatch (cmd20)");
        }
    }
}
