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

import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Das Spiel ist zu Ende, dieses Packet verrät, wer Gewonnen/Verloren hat
 */
public class C012_GAME_OVER extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Client besiegt/gewinnt
        NetPlayer player12 = rgi.game.getPlayer(rgi.readInt(data, 1));
        if (player12 != null) {
            if (rgi.readInt(data, 2) == 1) {
                // FAIL
                player12.setFinished(true);
                // Sind wir das?
                if (player12.playerId == rgi.game.getOwnPlayer().playerId) {
                    // Oje, wir haben verloren - bitter
                    rgi.game.defeat();
                }
            } else if (rgi.readInt(data, 2) == 2) {
                // SUCCESS
                player12.setFinished(true);
                // Sind wir das?
                if (player12.playerId == rgi.game.getOwnPlayer().playerId) {
                    // HURRA
                    rgi.game.win();
                } else {
                    rgi.game.done();
                }
            }
        }
    }
}
