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
package thirteenducks.cor.networks.client;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.networks.lobby.Lobby;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Der Client-Netzcontroller
 * 
 * Dieser hält die Verbindung zum Server und Verarbeitet die Datenpakete
 *
 * @author tfg
 */
public class ClientNetController {

    public ClientCore.InnerClient rgi;
    Socket gamedatasock;
    ClientHandler cdh;
    public Lobby lobby;                 // Die Lobby
    String receivebuffer = "";      // Empfangspuffer für receiveString()
    public InetAddress connectionTarget;    // Die Adresse des Servers (für Host-Erkennung)

    public ClientNetController(ClientCore.InnerClient newinner, Lobby lobby_t) {
        rgi = newinner;
        lobby = lobby_t;
    }

    /**
     * Versucht, sich zum Server zu verbinden.
     *
     * Baut eine vollständige Vebindung auf,
     * also 2 Sockets, eine für DATA und
     * eine für COMMANDS
     */
    public boolean connectTo(InetAddress ip, int port) {
        connectionTarget = ip;
        // Versuche zu connecten
        rgi.logger("[NetController]: Connecting to: " + ip + ":" + port);

        gamedatasock = new Socket();

        // Timeout ermitteln
        int timeout = 5000;
        if (rgi.configs.containsKey("connect_timeout")) {
            timeout = Integer.parseInt(rgi.configs.get("connect_timeout").toString());
        }

        try {
            cdh = new ClientHandler(this);

            gamedatasock.connect(new InetSocketAddress(ip, port), timeout);

            cdh.connect(new BufferedInputStream(gamedatasock.getInputStream()), new BufferedOutputStream(gamedatasock.getOutputStream()));

            rgi.logger("[NetController]: Connection established.");
        } catch (IOException ex) {
            // Kann Verbindung nicht aufbauen
            System.out.println("[NetController]: Can't connect!");
            rgi.logger("[NetController]: Can't connect:");
            rgi.logger(ex);
            return false;
        }

        // Verbindung steht!
        return true;
    }

    public void broadcastString(String s, byte cmdid) {
        // Sendet einen String als aufeinanderfolgenede DATA-Packete
        // In ein Datenpacket passen 8 Zeichen
        char[] ca = s.toCharArray();
        // Jetzt Daten in Packet umtüten
        for (int i = 0; i < ca.length; i += 8) {
            // Daten verpacken - noch genug da?
            int rest = ca.length - i;
            if (rest > 7) {
                // Noch genug da, einfach raushauen.
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | ca[i + 5]), (int) ((ca[i + 6] << 16) | ca[i + 7])));
                if (rest == 8) {
                    // Bei Rest == 8 noch manuell das Stop mitschicken, sonst ist kein Zeichen mehr da uns es wird nie geschickt
                    broadcastDATA(rgi.packetFactory(cmdid, (int) (('\0' << 16) | '\0'), 0, 0, 0));
                }
            } else if (rest == 7) {
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | ca[i + 5]), (int) ((ca[i + 6] << 16) | '\0')));
            } else if (rest == 6) {
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | ca[i + 5]), '\0'));
            } else if (rest == 5) {
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | '\0'), 0));
            } else if (rest == 4) {
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), '\0', 0));
            } else if (rest == 3) {
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | '\0'), 0, 0));
            } else if (rest == 2) {
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), '\0', 0, 0));
            } else if (rest == 1) {
                broadcastDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | '\0'), 0, 0, 0));
            }
        }
    }

    public boolean broadcastDATA(byte[] cmd) {
        // Prüfen
        if (cmd.length == 17 && cmd[0] != 0) {
            // Daten ok senden
            cdh.sendDATA(cmd);
        } else {
            System.out.println("Irregular DATA-pack," + " Length: " + cmd.length + " first Character: " + cmd[0]);
        }
        return false;
    }

    public void disconnectDetected(ClientHandler servhan) {
        // Server weg, FATAL
        servhan.active = false;

        System.out.println("FATAL: Server disconnected - exiting");
        rgi.logger("FATAL: Server disconnected - exiting");
        rgi.shutdown(4);


    }

    public class ClientHandler implements Runnable {

        BufferedInputStream inStream;
        BufferedOutputStream outStream;
        //ServerACKManager ackmanager;
        Thread t;
        boolean active = true;
        boolean endShown = false; // Ob dem Benutzer schon eine Fehlermeldung angezeigt wurde.
        public int temp01;
        public String temp02;
        public byte[] temp03;
        public Unit temp04;
        public Position temp05;
        public ArrayList<Position> temp06;
        public byte[] temp08;
        public int temp08index;
        public int temp09;
        public String stringb44 = "";
        public int playerId45;
        ClientNetController snc;
        ClientCommand[] cmdMap;

        public ClientHandler(ClientNetController snc_) {
            snc = snc_;
            //ackmanager = new ServerACKManager(rgi);

            temp06 = new ArrayList<Position>();
            cmdMap = new ClientCommand[55];

            initCmdArr();
        }

        public void connect(BufferedInputStream ninStream, BufferedOutputStream noutStream) {
            inStream = ninStream;
            outStream = noutStream;

            t = new Thread(this);
            t.start();
        }

        /**
         * Läd alle Befehle und fügt sie in die cmdMap ein.
         */
        private void initCmdArr() {

            cmdMap[1] = new thirteenducks.cor.networks.cmd.client.C001_LOAD_MAP_INIT();
            cmdMap[2] = new thirteenducks.cor.networks.cmd.client.C002_LOAD_MAP_PATH();
            cmdMap[3] = new thirteenducks.cor.networks.cmd.client.C003_PREP_START();
            cmdMap[4] = new thirteenducks.cor.networks.cmd.client.C004_START_GAME();
            cmdMap[5] = new thirteenducks.cor.networks.cmd.client.C005_SET_CLIENT_PARAM();
            cmdMap[6] = new thirteenducks.cor.networks.cmd.client.C006_TOGGLE_PAUSE();
            cmdMap[7] = new thirteenducks.cor.networks.cmd.client.C007_LOAD_GAME();
            cmdMap[8] = new thirteenducks.cor.networks.cmd.client.C008_RAW_DATA_PACKET();
            cmdMap[9] = new thirteenducks.cor.networks.cmd.client.C009_INIT_RAW_DATA();
            cmdMap[11] = new thirteenducks.cor.networks.cmd.client.C011_ERROR();
            cmdMap[12] = new thirteenducks.cor.networks.cmd.client.C012_GAME_OVER();
            cmdMap[13] = new thirteenducks.cor.networks.cmd.client.C013_DEBUG_SET_UNIT_ATKTARGET();
            cmdMap[14] = new thirteenducks.cor.networks.cmd.client.C014_LOBBY_COMS();
            cmdMap[15] = new thirteenducks.cor.networks.cmd.client.C015_CONSTRUCT_DONE();
            cmdMap[16] = new thirteenducks.cor.networks.cmd.client.C016_CONSTRUCT_STOP();
            cmdMap[17] = new thirteenducks.cor.networks.cmd.client.C017_CONSTRUCT_START();
            cmdMap[18] = new thirteenducks.cor.networks.cmd.client.C018_ADD_BUILDING();
            cmdMap[19] = new thirteenducks.cor.networks.cmd.client.C019_SET_GO_PARAM();
            cmdMap[20] = new thirteenducks.cor.networks.cmd.client.C020_RECRUIT_ADD();
            cmdMap[21] = new thirteenducks.cor.networks.cmd.client.C021_ADD_UNIT();
            cmdMap[22] = new thirteenducks.cor.networks.cmd.client.C022_RECRUIT_DEL();
            cmdMap[23] = new thirteenducks.cor.networks.cmd.client.C023_INIT_MOVE();
            cmdMap[24] = new thirteenducks.cor.networks.cmd.client.C024_MOVE_DIRECT();
            cmdMap[25] = new thirteenducks.cor.networks.cmd.client.C025_MOVE_SWITCH();
            cmdMap[27] = new thirteenducks.cor.networks.cmd.client.C027_SET_WAYPOINT();
            cmdMap[28] = new thirteenducks.cor.networks.cmd.client.C028_KILL_UNIT();
            cmdMap[30] = new thirteenducks.cor.networks.cmd.client.C030_KILL_BUILDING();
            cmdMap[33] = new thirteenducks.cor.networks.cmd.client.C033_KILL_RES();
            cmdMap[34] = new thirteenducks.cor.networks.cmd.client.C034_HARVEST_START();
            cmdMap[35] = new thirteenducks.cor.networks.cmd.client.C035_HARVEST_STOP();
            cmdMap[36] = new thirteenducks.cor.networks.cmd.client.C036_UPGRADE_TODESC();
            cmdMap[37] = new thirteenducks.cor.networks.cmd.client.C037_UPGRADE_DELTA();
            cmdMap[38] = new thirteenducks.cor.networks.cmd.client.C038_UPGRADE_EPOCHE();
            cmdMap[39] = new thirteenducks.cor.networks.cmd.client.C039_DEAL_DAMAGE();
            cmdMap[40] = new thirteenducks.cor.networks.cmd.client.C040_SET_UNIT_IDLE();
            cmdMap[41] = new thirteenducks.cor.networks.cmd.client.C041_UNIT_JUMP();
            cmdMap[42] = new thirteenducks.cor.networks.cmd.client.C042_EVACUATE_UNIT();
            cmdMap[43] = new thirteenducks.cor.networks.cmd.client.C043_SEND_UNIT_TO_SPECIAL_WAYPOINT();
            cmdMap[44] = new thirteenducks.cor.networks.cmd.client.C044_CHAT_MESSAGE();
            cmdMap[45] = new thirteenducks.cor.networks.cmd.client.C045_INIT_CHAT();
            cmdMap[46] = new thirteenducks.cor.networks.cmd.client.C046_TRANSFER_STATISTICS();
            cmdMap[47] = new thirteenducks.cor.networks.cmd.client.C047_SET_BUILDING_IDLE();
            cmdMap[48] = new thirteenducks.cor.networks.cmd.client.C048_DEBUG_SET_BUILDING_ATKTARGET();
            cmdMap[49] = new thirteenducks.cor.networks.cmd.client.C049_DEAL_DAMAGE_BUILDINGATK();
            cmdMap[50] = new thirteenducks.cor.networks.cmd.client.C050_SET_ANIM();
            cmdMap[51] = new thirteenducks.cor.networks.cmd.client.C051_SET_TEAMS();
        }

        void sendDATA(byte[] b) {
            try {
                outStream.write(b, 0, b.length);
                //ackmanager.sendSignal(b);
                outStream.flush();
            } catch (IOException ex) {
                System.out.println("DTRANSMISSION-ERROR!");
                rgi.logger("[DNetSend]: Transmission error:");
                rgi.logger(ex);
                active = false;
                if (!endShown) {
                    endShown = true;
                    JOptionPane.showMessageDialog(new JFrame(), "lost connection to server. exiting", "Net-Error", JOptionPane.ERROR_MESSAGE);
                }
                System.exit(5);
            }
        }

        @Override
        public void run() {
            byte[] data = new byte[17];

            while (true) {
                try {
                    if (!active) {
                        break;
                    }
                    for (int i = 0; i < 17; i++) {
                        data[i] = (byte) inStream.read();
                    }
                    /*if (data[0] == 0) {
                    System.out.println("dataoff");
                    this.active = false;
                    disconnectDetected(this);
                    } */
                } catch (IOException ex) {
                    System.out.println("Error recieving data - exiting");
                    ex.printStackTrace();
                    rgi.logger("Error recieving data - exiting");
                    rgi.logger(ex);
                    if (!endShown) {
                        endShown = true;
                        JOptionPane.showMessageDialog(new JFrame(), "lost connection to server. exiting", "Net-Error", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                }

                // War das ein ACK?

                /*if (data[0] == -128) {
                ackmanager.gotACK(data[1]);
                continue;
                }*/

                // ACK senden

                //sendACK(data[0]);

                // Verarbeiten

                byte cmd = data[0];

                if (cmd != -1) {
                    try {

                        cmdMap[cmd].process(data, cdh, rgi);

                    } catch (NullPointerException ex) {
                        System.out.println("C-NetCtrl: Got unknown command (" + cmd + ")");
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("C-NetCtrl: Got unknown command (" + cmd + ")");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Sendet eine Bewegungsanfrage an den Server.
     * Dieser berechnet die Route und lässt die Einheit danach ohne Verzögerung
     * auf allen Clients loslaufen.
     *
     * @param netID Die netId der RogUnit (!!!!) die laufen soll
     * @param path Der Pfad, den sie nehmen soll
     * @param asAppend Ob der Pfad einfach übernommen oder weitergelaufen werden soll
     */
    public synchronized void broadcastMove(int netID, Position target, boolean allowDifferentTarget) {
        // Kommando, netId & Ziel schicken
        this.broadcastDATA(rgi.packetFactory((byte) 23, netID, allowDifferentTarget ? 0 : 1, target.getX(), target.getY()));
    }

    /**
     * Empfängt einen TeilstringString und speichert in im Empfangspuffer. Wenn der String mit "\0" beendet wird, gibt receiveString() den gesamten String zurück.
     * Verwendet receivebuffer als Empfangspuffer.
     * @return - "" (leerer String) wenn nur ein Teilstring empfangen wurde, ansonsten der gesendete String
     */
    public String receiveString(byte data[]) {
        char c;
        for (int i = 1; i < 9; i++) {
            c = rgi.readChar(data, i);
            if (c != '\0') {
                receivebuffer += c;
            } else {
                String retstr = receivebuffer;
                receivebuffer = "";
                return retstr;
            }
        }
        return "";
    }

    /**
     * Durchsucht die Spielerliste nach einem Spieler mit dem angegebenen Namen.
     * Gibt das entsprechende NetPlayer-Objekt zurück oder null wenn keies gefunden wurde.
     * @param name  der gesuchte Name
     * @return:     der gesuchte NetPlayer oder null wenn kein entsprechender vorhanden ist
     */
    public NetPlayer getPlayer(String name) {
        for (NetPlayer player : this.cdh.snc.rgi.game.playerList) {
            if (player.nickName.equals(name)) {
                return player;
            }
        }
        System.out.println("ClientNetController: NetPlayer not found!");
        return null;
    }
}// Klassenende

