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
package de._13ducks.cor.networks.cmd.client;

import de._13ducks.cor.game.client.ClientCore.InnerClient;
import de._13ducks.cor.networks.client.ClientNetController.ClientHandler;
import de._13ducks.cor.game.NetPlayer;
import de._13ducks.cor.networks.cmd.ClientCommand;

/**
 * Änderungen an den Teams
 */
public class C051_SET_TEAMS extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Team-Daten
        // Beide Spieler und Statusänderung holen
        NetPlayer player511 = rgi.game.getPlayer(rgi.readInt(data, 1));
        NetPlayer player512 = rgi.game.getPlayer(rgi.readInt(data, 2));
        if (player511 != null && player512 != null) {
            int status51 = rgi.readInt(data, 3);
            switch (status51) {
                case 1:
                    // Anfrage setzen
                    if (!player512.invitations.contains(player511)) {
                        player512.invitations.add(player511);
                    }
                    if (player512.playerId == rgi.game.getOwnPlayer().playerId) {
                        rgi.chat.addMessage("You were invited by " + player511.nickName, -2);
                    }
                    break;
                case 2:
                    // Ally akzeptieren (=setzen)
                    if (!player511.allies.contains(player512)) {
                        player511.allies.add(player512);
                    }
                    if (!player512.allies.contains(player511)) {
                        player512.allies.add(player511);
                    }
                    // Sicht auch gleich setzen
                    if (!player511.visAllies.contains(player512)) {
                        player511.visAllies.add(player512);
                    }
                    if (!player512.visAllies.contains(player511)) {
                        player512.visAllies.add(player511);
                    }
                    // Einladungen löschen
                    player511.invitations.remove(player512);
                    player512.invitations.remove(player511);
                    break;
                case 3:
                    // Vis setzen
                    if (!player511.visAllies.contains(player512)) {
                        player511.visAllies.add(player512);
                    }
                    if (!player512.visAllies.contains(player511)) {
                        player512.visAllies.add(player511);
                    }
                    break;
                case 4:
                    // Ally löschen
                    player511.allies.remove(player512);
                    player512.allies.remove(player511);
                    // Sicht auch löschen
                    player511.visAllies.remove(player512);
                    player512.visAllies.remove(player511);
                    break;
                case 5:
                    // Vis löschen
                    player511.visAllies.remove(player512);
                    player512.visAllies.remove(player511);
                    break;
            }
            // Gebäude - Fow-Update triggern
            rgi.rogGraphics.builingsChanged();
        }
    }
}
