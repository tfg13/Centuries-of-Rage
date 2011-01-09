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
package thirteenducks.cor.networks.cmd.server;

import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.ability.ServerAbilityUpgrade;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Ein DELTA-Upgrade soll ausgeführt werden.
 */
public class S037_UPGRADE_DELTA extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Deltaupgrade
        // GameObject suchen
        GameObject go37 = rgi.netmap.getGameObjectviaID(rgi.readInt(data, 1));
        if (go37 != null) {
            // Ability suchen
            ServerAbilityUpgrade up = rgi.game.getPlayer(go37.playerId).serverDescAbilities.get(rgi.readInt(data, 2));
            if (up != null) {
                up.perform(go37);
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 37, go37.netID, rgi.readInt(data, 2), 0, 0));
            } else {
                System.out.println("FixMe: Ability ID mismatch (cmd37)");
            }
        } else {
            System.out.println("FixMe: GameObject ID mismatch (cmd37)");
        }
    }
}
