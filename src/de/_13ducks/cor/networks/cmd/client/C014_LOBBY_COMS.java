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
 * Lobby-Befehle
 * Lobby-Packete verwenden eine eigene Syntax (1=cmd, 2-8=data)
 * Möglicherweise werden diese auch wiederholt.
 */
public class C014_LOBBY_COMS extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        String tmp = rgi.netctrl.receiveString(data);
        if (!tmp.equals("")) {


            String signaltype = tmp.substring(0, 1);        // gibt an, was für eine nachricht das ist (z.B. 1 - playerjoin; 7 -  chat; ...)
            String args = tmp.substring(1);

            // Name-Request
            if (signaltype.equals("N")) {
                rgi.netctrl.lobby.playername = args;                       // den vom Server vorgeschlagenen Namen verwenden
                rgi.playername = args;
                rgi.game.getOwnPlayer().nickName = args;                // "    "   "   "   "   "   "   "   "   "   "   "

                rgi.netctrl.lobby.send("1" + rgi.netctrl.lobby.playername);   // join
            }

            // Spielerbeitritt:
            if (signaltype.equals("1")) {

                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player == null) {
                    NetPlayer newPlayer = new NetPlayer(rgi);
                    newPlayer.nickName = args;
                    rgi.netctrl.lobby.addPlayer(args);

                    rgi.game.playerList.add(newPlayer);
                }
            }

            // playerId Durchsage:
            if (signaltype.equals("T")) {
                int id = Integer.parseInt(args.substring(0, 1)); // playerId feststellen
                args = args.substring(1);                       // playerId rausschneiden
                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player != null) {
                    player.playerId = id;
                }
            }

            // Spieleraustritt
            if (signaltype.equals("2")) {
                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player != null) {
                    // Beenden, wenn dieser Client oder der Host das Spiel verlasen hat:
                    if (args.equals(rgi.netctrl.lobby.playername) || player.isHost) {
                        rgi.netctrl.disconnectDetected(handler);
                        System.exit(0);
                    } else {
                        rgi.netctrl.lobby.removePlayer(args);
                        rgi.game.playerList.remove(player);
                    }
                }
            }

            // Spieler bereit
            if (signaltype.equals("3")) {
                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player != null) {
                    player.isReady = true;
                    rgi.netctrl.lobby.changePlayerStatus(args, true);

                }
            }

            // Spieler nicht (mehr) bereit
            if (signaltype.equals("4")) {
                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player != null) {
                    player.isReady = false;
                    rgi.netctrl.lobby.changePlayerStatus(args, false);
                }
            }

            // Teamwechsel
            if (signaltype.equals("5")) {
                String newteam = args.substring(0, 1);
                args = args.substring(1);
                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player != null) {

                    rgi.netctrl.lobby.changePlayerTeam(args, Integer.parseInt(newteam));
                }
            }

            // Farbwechsel
            if (signaltype.equals("6")) {
                String intcol = args.substring(1, args.lastIndexOf("#"));
                java.awt.Color col = new java.awt.Color(Integer.parseInt(intcol));
                String newcolour = args.substring(0, 1);
                NetPlayer player = rgi.netctrl.getPlayer(args.substring(args.lastIndexOf("#") + 1));
                if (player != null) {
                    player.color = new org.newdawn.slick.Color(col.getRed(), col.getGreen(), col.getBlue());
                    rgi.netctrl.lobby.changePlayerColor(args.substring(args.lastIndexOf("#") + 1), intcol);
                }
            }


            // Chat
            if (signaltype.equals("7")) {
                rgi.netctrl.lobby.chatMessage(args);
            }

            // Host
            if (signaltype.equals("8")) {
                //System.out.print("HOSTHOSTHOST: " + args + "\n");
                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player != null) {
                    player.isHost = true;
                    rgi.netctrl.lobby.setHostPlayer(args);
                }
            }

            // Rassenwechsel
            if (signaltype.equals("9")) {
                String newrace = args.substring(0, 1);
                args = args.substring(1);


                NetPlayer player = rgi.netctrl.getPlayer(args);
                if (player != null) {
                    player.lobbyRace = Integer.parseInt(newrace);
                }
                rgi.netctrl.lobby.changePlayerRace(args, Integer.parseInt(newrace));
            }
        }
    }
}
