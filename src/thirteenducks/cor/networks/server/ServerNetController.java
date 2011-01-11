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
package thirteenducks.cor.networks.server;

import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.NetPlayer.races;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Der Server-Netzwerkcontroller
 *
 * Hält Verbindung mit allen Clients
 * Verschickt Daten und GameCommands an alle
 *
 * Empfängt Daten und Commands und leitet sie an den Server-Gamecontroller weiter.
 */
public class ServerNetController implements Runnable {

    ServerCore.InnerServer rgi;
    public ArrayList<ServerHandler> clientconnection;
    public ServerSocket servSock;
    String hostname;            // Der Name des Hosts
    public ServerCore servercore;

    public ServerNetController(ServerCore.InnerServer newinner, ServerCore servercore_t) {
        servercore = servercore_t;
        rgi = newinner;
        clientconnection = new ArrayList<ServerHandler>();
        boolean ready = false;

    }

    @Override
    public void run() {
        int port = 39264;
        if (rgi.configs.containsKey("port")) {
            port = Integer.parseInt(rgi.configs.get("port").toString());
        }
        try {
            servSock = new ServerSocket(port);

            System.out.println("Server online, waiting for clients");
            rgi.logger("[ServNet]: Server online, waiting for clients");

            while (true) {
                // Server-Mainloop
                // Verbindung herstellen - Methode blockt (!)
                Socket clientSocket = servSock.accept();
                // Verbindung steht
                // Neue Handler erzeugen und zuweisen:
                clientconnection.add(new ServerHandler(this, clientSocket, rgi.game.addPlayer()));

                rgi.logger("New client: " + clientSocket.getInetAddress());
                System.out.println("Connected to " + clientSocket);

            }
        } catch (IOException ex) {
            if (servSock != null) {
                // Sollte ok sein, der ist jetzt halt zu
                System.out.println("Socket closed.");
            } else {
                System.out.println("Error: Can't create ServerSocket at port " + port);
                rgi.logger("Error: Can't create ServerSocket at port " + port);
                rgi.logger(ex);
                JOptionPane.showMessageDialog(new JFrame(), "Can't create server: Port " + port + " already in use (another CoR-Server running?)", "port blocked", JOptionPane.ERROR_MESSAGE);
                rgi.shutdown(3);
            }
        }


        try {
            hostname = (String) rgi.configs.get("playername");
        } catch (Exception ex) {
            System.out.print("FEHLER: kein Spielername in servercfg definiert, ganz schlimm!\n");
            ex.printStackTrace();
        }
    }

    /**
     * Aufrufen, damit der Server keine neuen Clients mehr annimmt.
     */
    public void closeAcception() {
        if (servSock != null) {
            try {
                servSock.close();
            } catch (IOException ex) {
                System.out.println("FixMe: Error acceptionSocket");
            }
        }
    }

    /**
     * Lässt die Clients das Spiel laden.
     * Die Clients zeigen jetzt bereits den Ladebildschirm an.
     * Anschließend werden alle Module geladen.
     */
    public void loadGame() {
        broadcastDATA(rgi.packetFactory((byte) 7, rgi.isInDebugMode() ? 1 : 0, 0, 0, 0));
    }

    /**
     * Bereitet den Start des Spiels vor.
     * Weist die Clients an, schonmal die Grafik hochzufahren, während der Server startet
     * Bereitet so viel wie möglich vor, damit das Spiel anschließend überall gleichzeitig starten kann.
     *
     * Muss NACH loadMap und VOR startGame aufgerufen werden!!!
     */
    public void initGame(byte numberofPlayers) {
        // Es soll losgehen - Befehl (3) an die Clients schicken
        broadcastDATA(rgi.packetFactory((byte) 3, numberofPlayers, rgi.isInDebugMode() ? 1 : 2, 0, 0));
        // Selber Vorbereitungen treffen
        rgi.game.prepareStart(numberofPlayers);
    }

    /**
     * Staret das Spiel jetzt endgültig.
     * Sollte von Clients in unter 500ms ausgeführt werden.
     *
     * Muss NACH loadMap und NACH initGame aufgerufen werden!!!
     */
    public void startGame() {
        // Jetzt das GO! schicken
        broadcastDATA(rgi.packetFactory((byte) 4, 0, 0, 0, 0));
    }

    /**
     * Lässt eine Einheit auf allen Clients loslaufen.
     * Durch paralelle Übertragung laufen die Einheiten auch bei langen Wegen
     * praktisch Zeitgleich los
     *
     * Sollte nur von ServerBehaviours aufgerufen werden, die die Bewegung auch weiter verarbeiten.
     * Diese Funktion stellt keinerlei Serverinterne Bearbeitung zur Verfügung (!)
     *
     * @param netID Die netId der RogUnit (!!!!) die laufen soll
     * @param path Der Pfad, den sie nehmen soll, Position wird notfalls überschrieben.
     * @param asAppend Ob der Pfad einfach übernommen oder weitergelaufen werden soll
     */
    public synchronized void broadcastMove(int netID, ArrayList<Position> path, boolean asAppend) {
        // Checken
        if (path != null && path.size() > 1) {
            // Kommando, netId & Ziel schicken
            this.broadcastDATA(rgi.packetFactory((byte) 23, netID, 0, path.get(path.size() - 1).X, path.get(path.size() - 1).Y));
            // Jetzt den Pfad als ganzes schicken
            int cmd = 24;
            if (asAppend) {
                cmd = 25;
            }
            boolean sendFinal = false;
            for (int i = 0; i < path.size(); i += 2) {
                int x1 = path.get(i).X;
                int y1 = path.get(i).Y;
                int x2 = 0;
                int y2 = 0;
                // Noch eins da?
                if ((i + 1) < path.size()) {
                    x2 = path.get(i + 1).X;
                    y2 = path.get(i + 1).Y;
                    sendFinal = true;
                } else {
                    x2 = -1;
                    y2 = -1;
                    sendFinal = false;
                }
                // Senden
                this.broadcastDATA(rgi.packetFactory((byte) cmd, x1, y1, x2, y2));
            }
            // Noch ein Abschlusspaket?
            this.broadcastDATA(rgi.packetFactory((byte) cmd, -1, -1, 0, 0));
        } else {
            System.out.println("FixMe: Broadcast Move: irregular call!");
        }
    }

    public synchronized void broadcastString(String s, byte cmdid) {
        // Sendet einen String als aufeinanderfolgenede DATA-Packete
        // In ein Datenpacket passen  Zeichen
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
            // Daten ok - an alle Senden
            for (int i = 0; i < clientconnection.size(); i++) {
                ServerHandler sh = clientconnection.get(i);
                try {
                    sh.sendDATA(cmd);
                } catch (RuntimeException ex) {
                    // Diesen Client wurde entfernt - den counter 1 runter setzen, damit alle alles kriegen
                    i--;
                }
            }
        } else {
            System.out.println("Irregular DATA-Pack," + " length: " + cmd.length + " first char: " + cmd[0]);
        }
        return false;
    }

    /**
     * Sendet eine Text-Nachricht an alle Spieler (via Chat)
     * Mit der from-Variable kann eingestellt werden, von wem die Nachricht kommen soll:
     * Positive Zahlen: Spieler mit der jeweiligen Nummer.
     * -1 = (Server)
     * -2 = (Game)
     * @param message
     * @param from
     */
    public void broadcastMessage(String message, int from) {
        broadcastDATA(rgi.packetFactory((byte) 45, from, 0, 0, 0));
        broadcastString(message, (byte) 44);
    }

    public void disconnectDetected(ServerHandler servhan, boolean error) {
        System.out.println("Client disconnected " + servhan.clientSock);
        rgi.logger("Client disconnected " + servhan.clientSock);
        // Client weg, Handler löschen
        servhan.active = false;
        try {
            servhan.clientSock.close();
        } catch (IOException ex) {
            System.out.println("NCRITICAL: Client disconnect error!");
        }
        clientconnection.remove(servhan);
        // Überhaupt noch jemand da?
        if (clientconnection.isEmpty()) {
            // Nö, Server auschalten
            rgi.logger("No more clients left, server shutting down");
            System.out.println("No more clients left, server shutting down");
            rgi.shutdown(0);
        } else {
            // Den übrigen mitteilen - falls das Spiel schon läuft
            if (servhan.loadStatus > 2) {
                this.broadcastDATA(rgi.packetFactory((byte) 45, -1, 0, 0, 0));
                this.broadcastString(servhan.client.nickName + " left the game " + (error ? "(lost connection to client)" : "(disconnected by user)"), (byte) 44);
                // Vermerken, dass dieser Spieler verloren hat
                servhan.client.setFinished(true);
                // Alle Einheiten und Gebäude dieses Spielers entfernen:
                rgi.netmap.playerLeft(servhan.client.playerId);
            }
        }
    }

    public class ServerHandler implements Runnable {

        public int loadStatus = 0;
        ServerNetController snc;        // Referenz auf den ServerNetController
        BufferedInputStream ins;
        BufferedOutputStream out;
        public Socket clientSock;
        public NetPlayer client;
        Thread t;
        public boolean active = true;
        public Unit temp04;
        public Position temp05;
        public GameObject temp08;
        public ArrayList<Position> temp06;
        public ArrayList<Unit> temp07;
        public Position temps052_1;
        public ArrayList<Unit> temps052_2;
        public int moveMode;
        public String stringb44 = "";
        public String receivebuffer = "";  // Empfangspuffer für receiveString()
        private ServerCommand[] cmdMap;

        public void sendDATA(byte[] b) {
            try {
                out.write(b, 0, b.length);
                //ackmanager.sendSignal(b);
                out.flush();
            } catch (IOException ex) {
                rgi.logger("[DNetSend]: Transmission error:");
                rgi.logger(ex);
                // Client offline:
                disconnectDetected(this, true);
                throw new RuntimeException(); // Das ist das Signal, dass ein Client entfernt wurde
            }
        }

        /**
         * Sendet einen String nur an diesen Client
         */
        public synchronized void sendString(String s, byte cmdid) {
            // Sendet einen String als aufeinanderfolgenede DATA-Packete
            // In ein Datenpacket passen  Zeichen
            char[] ca = s.toCharArray();
            // Jetzt Daten in Packet umtüten
            for (int i = 0; i < ca.length; i += 8) {
                // Daten verpacken - noch genug da?
                int rest = ca.length - i;
                if (rest > 7) {
                    // Noch genug da, einfach raushauen.
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | ca[i + 5]), (int) ((ca[i + 6] << 16) | ca[i + 7])));
                    if (rest == 8) {
                        // Bei Rest == 8 noch manuell das Stop mitschicken, sonst ist kein Zeichen mehr da uns es wird nie geschickt
                        broadcastDATA(rgi.packetFactory(cmdid, (int) (('\0' << 16) | '\0'), 0, 0, 0));
                    }
                } else if (rest == 7) {
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | ca[i + 5]), (int) ((ca[i + 6] << 16) | '\0')));
                } else if (rest == 6) {
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | ca[i + 5]), '\0'));
                } else if (rest == 5) {
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), (int) ((ca[i + 4] << 16) | '\0'), 0));
                } else if (rest == 4) {
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | ca[i + 3]), '\0', 0));
                } else if (rest == 3) {
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), (int) ((ca[i + 2] << 16) | '\0'), 0, 0));
                } else if (rest == 2) {
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | ca[i + 1]), '\0', 0, 0));
                } else if (rest == 1) {
                    sendDATA(rgi.packetFactory(cmdid, (int) ((ca[i] << 16) | '\0'), 0, 0, 0));
                }
            }
        }

        public ServerHandler(ServerNetController snc_t, Socket socket, NetPlayer client) {
            try {
                snc = snc_t;        // Referenz auf ServerNetController setzen
                this.client = client;
                clientSock = socket;
                clientSock.setKeepAlive(true);
                ins = new BufferedInputStream(clientSock.getInputStream());
                out = new BufferedOutputStream(clientSock.getOutputStream());
                //ackmanager = new ServerACKManager(rgi);


                temp06 = new ArrayList<Position>();

                temp07 = new ArrayList<Unit>();

                cmdMap = new ServerCommand[55];

                initCmdArr();
                t = new Thread(this);
                t.start();
                // Spieler verwalten (an client schicken)
                sendDATA(rgi.packetFactory((byte) 5, client.playerId, 0, 0, 0));
            } catch (Exception ex) {
                // Fehler beim Verbinden
                System.out.println("Failed to create new clienthandlerDATA");
                rgi.logger("Failed to create new clienthandlerDATA");
                rgi.logger(ex);
            }
        }

        /**
         * Alle Kommandos laden
         */
        private void initCmdArr() {

            cmdMap[1] = new thirteenducks.cor.networks.cmd.server.S001_LOAD_MAP_INIT();
            cmdMap[3] = new thirteenducks.cor.networks.cmd.server.S003_PREP_START();
            cmdMap[6] = new thirteenducks.cor.networks.cmd.server.S006_TOGGLE_PAUSE();
            cmdMap[7] = new thirteenducks.cor.networks.cmd.server.S007_LOAD_GAME();
            cmdMap[9] = new thirteenducks.cor.networks.cmd.server.S009_REQUEST_DATA();
            cmdMap[10] = new thirteenducks.cor.networks.cmd.server.S010_CLIENT_OFFLINE();
            cmdMap[14] = new thirteenducks.cor.networks.cmd.server.S014_LOBBY_COMS();
            cmdMap[16] = new thirteenducks.cor.networks.cmd.server.S016_CONSTRUCT_STOP();
            cmdMap[17] = new thirteenducks.cor.networks.cmd.server.S017_CONSTRUCT_START();
            cmdMap[18] = new thirteenducks.cor.networks.cmd.server.S018_ADD_BUILDING();
            cmdMap[20] = new thirteenducks.cor.networks.cmd.server.S020_RECRUIT_ADD();
            cmdMap[22] = new thirteenducks.cor.networks.cmd.server.S022_RECRUIT_DEL();
            cmdMap[23] = new thirteenducks.cor.networks.cmd.server.S023_MOVE_UNIT();
            cmdMap[26] = new thirteenducks.cor.networks.cmd.server.S026_MOVE_UNIT_TO_BUILDING();
            cmdMap[27] = new thirteenducks.cor.networks.cmd.server.S027_SET_WAYPOINT();
            cmdMap[28] = new thirteenducks.cor.networks.cmd.server.S028_KILL_UNIT();
            cmdMap[29] = new thirteenducks.cor.networks.cmd.server.S029_UNIT_ATTACK_GO();
            cmdMap[30] = new thirteenducks.cor.networks.cmd.server.S030_KILL_BUILDING();
            cmdMap[32] = new thirteenducks.cor.networks.cmd.server.S032_GROUP_ATTACK_GO();
            cmdMap[33] = new thirteenducks.cor.networks.cmd.server.S033_KILL_RES();
            cmdMap[34] = new thirteenducks.cor.networks.cmd.server.S034_HARVEST_START();
            cmdMap[35] = new thirteenducks.cor.networks.cmd.server.S035_HARVEST_STOP();
            cmdMap[36] = new thirteenducks.cor.networks.cmd.server.S036_UPGRADE_TODESC();
            cmdMap[37] = new thirteenducks.cor.networks.cmd.server.S037_UPGRADE_DELTA();
            cmdMap[38] = new thirteenducks.cor.networks.cmd.server.S038_UPGRADE_EPOCHE();
            cmdMap[41] = new thirteenducks.cor.networks.cmd.server.S041_UNIT_JUMP();
            cmdMap[42] = new thirteenducks.cor.networks.cmd.server.S042_EVACUATE_UNIT();
            cmdMap[44] = new thirteenducks.cor.networks.cmd.server.S044_CHAT_MESSAGE();
            cmdMap[45] = new thirteenducks.cor.networks.cmd.server.S045_INIT_CHAT();
            cmdMap[46] = new thirteenducks.cor.networks.cmd.server.S046_TRANSFER_STATISTICS();
            cmdMap[47] = new thirteenducks.cor.networks.cmd.server.S047_BUILDING_ATTACK_GO();
            cmdMap[50] = new thirteenducks.cor.networks.cmd.server.S050_SET_ANIM();
            cmdMap[51] = new thirteenducks.cor.networks.cmd.server.S051_SET_TEAMS();
            cmdMap[52] = new thirteenducks.cor.networks.cmd.server.S052_MOVE_GROUP();

        }

        /*void sendACK(byte command) {
        // Sendet eine Empfangsbestätigung, passend zum Kommando
        byte[] data = new byte[17];
        // ACK
        data[0] = -128;
        // Wiederholung des Kommandos
        data[1] = command;
        // Senden
        sendDATA(data, false);
        } */
        @Override
        public void run() {
            System.out.println("[Server]: DATAConnection established");

            byte[] data = new byte[17];

            while (true) {
                try {
                    if (!active) {
                        break;
                    }
                    for (int i = 0; i < 17; i++) {
                        data[i] = (byte) ins.read();
                    }
                    /* if (data[0] == 0) {
                    // Client disconnect...
                    System.out.println("dataoff");
                    this.active = false;
                    disconnectDetected(this);
                    } */
                } catch (IOException ex) {
                    System.out.println("Receving ERROR - Ende!");
                    ex.printStackTrace();
                    rgi.logger("Receving ERROR - Ende!");
                    rgi.logger(ex);
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

                        cmdMap[cmd].process(data, this, rgi);

                    } catch (NullPointerException ex) {
                        System.out.println("S-NetCtrl: Got unknown command (" + cmd + ")");
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("S-NetCtrl: Got unknown command (" + cmd + ")");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
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
    }

    public String workplayername(String name) {
        for (ServerHandler sh : this.clientconnection) {
            if (sh.client.nickName.equals(name)) {
                name += "(fake)";
                workplayername(name);
            } else {
                return name;
            }
        }
        return null; // dürfte nicht vorkommen
    }
}// Klassenende

