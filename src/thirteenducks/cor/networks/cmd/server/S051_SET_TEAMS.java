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

import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Setzt die Teams
 */
public class S051_SET_TEAMS extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        // Team-Daten, teilweise (Anfragen) nur weiterleiten, großteils aber verarbeiten.
        // Beide Spieler und Statusänderung holen
        NetPlayer player511 = rgi.game.getPlayer(rgi.readInt(data, 1));
        NetPlayer player512 = rgi.game.getPlayer(rgi.readInt(data, 2));
        if (player511 != null && player512 != null) {
            int status51 = rgi.readInt(data, 3);
            switch (status51) {
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
                    // Allen Mitteilen
                    rgi.netctrl.broadcastMessage(player511.nickName + " and " + player512.nickName + " are friends now", -2);
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
                    rgi.netctrl.broadcastMessage(player511.nickName + " and " + player512.nickName + " are no longer allied", -2);
                    break;
                case 5:
                    // Vis löschen
                    player511.visAllies.remove(player512);
                    player512.visAllies.remove(player511);
                    break;
            }
            // Weiterleiten
            rgi.netctrl.broadcastDATA(data);
        }
    }
}
