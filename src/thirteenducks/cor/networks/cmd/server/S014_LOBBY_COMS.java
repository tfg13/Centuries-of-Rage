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

import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.game.NetPlayer.races;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Kommunikation der Lobby.
 * Spezielle Syntax, sie Client-Befehl
 */
public class S014_LOBBY_COMS extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        String tmp = handler.receiveString(data);
        if (!tmp.equals("")) {

            String forward = tmp;

            String signaltype = tmp.substring(0, 1);        // gibt an, was für eine nachricht das ist (z.B. 1 - playerjoin; 7 -  chat; ...)
            String args = tmp.substring(1);

            // Name-Request:
            if (signaltype.equals("N")) {
                handler.client.nickName = rgi.netctrl.workplayername(args);
                handler.sendString("N" + handler.client.nickName, (byte) 14);
                return;
            } else {
                // Nachricht weiterleiten:
                rgi.netctrl.servercore.broadcastStringSynchronized(tmp, (byte) 14);
            }

            //System.out.println("Serverint: " + tmp);


            // Spielerbeitritt:
            if (signaltype.equals("1")) {
                if (rgi.netctrl.servSock.getLocalSocketAddress().toString().equals(handler.clientSock.getLocalAddress().toString())) {
                    handler.client.isHost = true;
                }

                float randcol = (float) Math.random();
                java.awt.Color randomcol = java.awt.Color.getHSBColor(randcol, 1.0f, 1.0f);
                handler.client.color = new org.newdawn.slick.Color(randomcol.getRGB());
                handler.client.lobbyRace = races.random;

                // Die komplette Spielerliste durchgeben (für den neuen):
                for (int i = 0; i < rgi.netctrl.clientconnection.size(); i++) {
                    NetPlayer player = rgi.netctrl.clientconnection.get(i).client;

                    // Spielerbeitritt melden:
                    rgi.netctrl.servercore.broadcastStringSynchronized("1" + player.nickName, (byte) 14);

                    // Volk durchgeben:
                    rgi.netctrl.servercore.broadcastStringSynchronized("9" + player.lobbyRace + player.nickName, (byte) 14);

                    // playerId des Spielers durchgeben:
                    rgi.netctrl.servercore.broadcastStringSynchronized("T" + player.playerId + player.nickName, (byte) 14);

                    // Spielerfarbe melden:
                    // Zufällige Spielerfarbe auswürfeln und einstellen:
                    rgi.netctrl.servercore.broadcastStringSynchronized("6#" + (((player.color.getAlpha() & 0xFF) << 24) | ((player.color.getRed() & 0xFF) << 16) | ((player.color.getGreen() & 0xFF) << 8) | ((player.color.getBlue() & 0xFF) << 0)) + "#" + player.nickName, (byte) 14);
                    //rgi.netctrl.servercore.broadcastStringSynchronized("6" + player.lobbyColour + player.nickName, (byte) 14);

                    // Spielerstatus melden:
                    if (player.isReady == true) {
                        rgi.netctrl.servercore.broadcastStringSynchronized("3" + player.nickName, (byte) 14);
                    } else {
                        rgi.netctrl.servercore.broadcastStringSynchronized("4" + player.nickName, (byte) 14);
                    }

                    // Den Host bekanntmachen:
                    if (player.isHost == true) {
                        rgi.netctrl.servercore.broadcastStringSynchronized("8" + player.nickName, (byte) 14);
                    }
                }
            }




            // Spieleraustritt
            if (signaltype.equals("2")) {
                handler.client.isReady = false;
                if (args.equals(handler.client.nickName)) {
                    rgi.netctrl.disconnectDetected(handler, false);
                }


            }




            // Spieler bereit
            if (signaltype.equals("3")) {
                handler.client.isReady = true;

                // checken, ob alle bereit sind:
                boolean allReady = true;
                for (int i = 0; i < rgi.netctrl.clientconnection.size(); i++) {
                    if (rgi.netctrl.clientconnection.get(i).client.isReady == false) {
                        allReady = false;
                    }
                }

                // Wenn alle bereit sind wird das Spiel gestartet:
                if (allReady == true) {
                    rgi.netctrl.servercore.ready = true;
                    rgi.netctrl.servercore.broadcastStringSynchronized("7Spiel wird gestartet...", (byte) 14);
                }
            }

            // Spieler nicht mehr bereit
            if (signaltype.equals("4")) {
                handler.client.isReady = false;
            }




            // Teamwechsel
            if (signaltype.equals("5")) {
                String newteam = args.substring(1, 2);
                // TODO: aktivieren wenn teams eingeführt wurden
                //handler.client.team = newteam.charAt(0);
            }

            // Farbwechsel
            if (signaltype.equals("6")) {
                String intcol = args.substring(1, args.lastIndexOf("#"));
                java.awt.Color col = new java.awt.Color(Integer.parseInt(intcol));
                handler.client.color = new org.newdawn.slick.Color(col.getRed(), col.getGreen(), col.getBlue());
            }

            // Host-Bekanntgabe
            if (signaltype.equals("8")) {
                handler.client.isHost = true;
            }

            // Volkwechsel
            if (signaltype.equals("9")) {
                String newrace = args.substring(0, 1);
                handler.client.lobbyRace = Integer.parseInt(newrace);
            }


        }
    }
}
