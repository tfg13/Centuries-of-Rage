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
package de._13ducks.cor.game.server;

import de._13ducks.cor.networks.server.ServerNetController;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.NetPlayer.races;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import de._13ducks.cor.game.Core;
import de._13ducks.cor.game.NetPlayer;

/**
 * Der Server-Kern
 *
 * Startet einen Server mit den dazugehörigen Server-Modulen etc...
 *
 * Erzeugt ein seperates Logfile (server_log.txt)
 *
 * @author tfg
 */
public class ServerCore extends Core {

    ServerCore.InnerServer rgi;
    ServerNetController servNet;
    ServerMapModule mapMod;
    ServerGameController gamectrl;
    ServerPathfinder spath;
    ServerStatistics sstat;	//Statistik
    ServerMoveManager smoveman;
    public boolean ready;              // gibt an, ob das Spiel gestartet werden soll.

    public ServerCore(boolean debug, String Mapname) {
        debugmode = debug;

        rgi = new ServerCore.InnerServer();

        initLogger();

        rgi.logger("[Core] Reading config...");
        if (debugmode) {
            System.out.println("Configuration:");
        }

        // Konfigurationsdatei lesen

        cfgvalues = new HashMap();
        File cfgFile = new File("server_cfg.txt");
        try {
            
            FileReader cfgReader = new FileReader(cfgFile);
            BufferedReader reader = new BufferedReader(cfgReader);
            String zeile = null;
            int i = 0; // Anzahl der Durchläufe zählen
            while ((zeile = reader.readLine()) != null) {
                // Liest Zeile fuer Zeile, jetzt auswerten und in Variablen
                // schreiben
                int indexgleich = zeile.indexOf('='); // Istgleich suchen
                if (indexgleich == -1) {
                } else {
                    String v1 = zeile.substring(0, indexgleich); // Vor dem =
                    // rauschneiden
                    String v2 = zeile.substring(indexgleich + 1); // Nach dem
                    // =
                    // rausschneiden
                    System.out.println(v1 + " = " + v2);
                    cfgvalues.put(v1, v2);
                    if (debugmode) { // Im Debugmode alles ausgeben, zum nachvollziehen
                        rgi.logger("[Core-Log] " + v1 + "=" + v2);
                    }

                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            // cfg-Datei nicht gefunden - inakzeptabel!
            rgi.logger("[Core-ERROR] Configfile (server_cfg.txt) not found, creating new one...");
            try {
                cfgFile.createNewFile();
            } catch (IOException ex) {
                System.out.print("Error creating server_cfg.txt .");
            }
            //rgi.shutdown(1);
        } catch (IOException e2) {
            // Inakzeptabel
            e2.printStackTrace();
            rgi.logger("[Core-ERROR] Critical I/O Error");
            rgi.shutdown(1);
        }

        // Läuft als Server

        rgi.logger("[CoreInit]: Starting server mode...");

        // Module laden

        rgi.logger("[CoreInit]: Loading modules...");

        rgi.logger("[CoreInit]: Loading pathfinder...");

        spath = new ServerPathfinder(rgi);

        rgi.logger("[CoreInit]: Loading gamecontroller");

        gamectrl = new ServerGameController(rgi);

        // Netzwerk

        rgi.logger("[CoreInit]: Loading serverNetController");

        servNet = new ServerNetController(rgi, this);

        rgi.logger("[CoreInit]: Loading serverMapmodul");

        mapMod = new ServerMapModule(rgi);

	rgi.logger("[CoreInit]: Loading serverStatistics");
	
	sstat = new ServerStatistics(rgi);

        rgi.logger("[CoreInit]: Loading serverMoveManager");

	smoveman = new ServerMoveManager();

        // Alle Module geladen, starten

        rgi.initInner();

        rgi.logger("[Core]: Initializing modules...");

        mapMod.initModule();

        rgi.logger("[Core]: Init done, starting Server...");

        Thread t = new Thread(servNet);
        t.start();


        rgi.logger("[Core]: Server running");
        try {
            // Auf Startsignal warten
            while (!this.ready) {
                Thread.sleep(1000);
            }
            // Jetzt darf niemand mehr rein
            servNet.closeAcception();

            // Zufallsvölker bestimmen und allen Clients mitteilen:
            for (NetPlayer p : this.gamectrl.playerList) {
                if (p.lobbyRace == races.random) {
                    if (Math.rint(Math.random()) == 1) {
                        p.lobbyRace = 1;
                        this.servNet.broadcastString(("91" + p.nickName), (byte) 14);
                    } else {
                        p.lobbyRace = 2;
                        this.servNet.broadcastString(("92" + p.nickName), (byte) 14);
                    }
                }
            }

            // Clients vorbereiten
            servNet.loadGame();
            // Warte darauf, das alle soweit sind
            waitForStatus(1);
            // Spiel laden
            mapMod.loadMap(Mapname);
            waitForStatus(2);
            // Game vorbereiten
            servNet.initGame((byte) 8);


            // Starteinheiten & Gebäude an das gewählte Volk anpassen:
            for (NetPlayer player : this.gamectrl.playerList) {
                // Gebäude:
                for (Building building : this.rgi.netmap.buildingList) {
                    if (building.getPlayerId() == player.playerId) {
                        if (player.lobbyRace == races.undead) {
                            building.performUpgrade(rgi, 1001);
                        } else if (player.lobbyRace == races.human) {
                            building.performUpgrade(rgi, 1);
                        }
                    }
                }

                // Einheiten:
                for (Unit unit : this.rgi.netmap.unitList) {
                    if (unit.getPlayerId() == player.playerId) {
                        if (player.lobbyRace == races.undead) {
                            // 401=human worker, 1401=undead worker
                            if (unit.getDescTypeId() == 401) {
                                unit.performUpgrade(rgi, 1401);
                            }
                            // 402=human scout, 1402=undead mage
                            if (unit.getDescTypeId() == 402) {
                                unit.performUpgrade(rgi, 1402);
                            }


                        } else if (player.lobbyRace == races.human) {
                            // 1401=undead worker, 401=human worker
                            if (unit.getDescTypeId() == 1401) {
                                unit.performUpgrade(rgi, 401);
                            }
                            // 1402=undead mage, 402=human scout
                            if (unit.getDescTypeId() == 1402) {
                                unit.performUpgrade(rgi, 402);
                            }
                        }
                    }
                }
            }


            waitForStatus(3);
            servNet.startGame();
        } catch (InterruptedException ex) {
        }


    }

    /**
     * Synchronisierte Funktipon zum Strings senden
     * wird für die Lobby gebraucht, vielleicht später auch mal für ingame-chat
     * @param s     - zu sendender String
     * @param cmdid - command-id
     */
    public synchronized void broadcastStringSynchronized(String s, byte cmdid) {

        s += "\0";
        this.servNet.broadcastString(s, cmdid);
    }

    private void waitForStatus(int status) throws InterruptedException {
        while (true) {
            Thread.sleep(50);
            boolean go = true;
            for (ServerNetController.ServerHandler servhan : servNet.clientconnection) {
                if (servhan.loadStatus < status) {
                    go = false;
                    break;
                }
            }
            if (go) {
                break;
            }
        }
    }

    @Override
    public void initLogger() {
        // Erstellt ein neues Logfile
        try {
            FileWriter logcreator = new FileWriter("server_log.txt");
            logcreator.close();
        } catch (IOException ex) {
            // Warscheinlich darf man das nicht, den Adminmodus emfehlen
            JOptionPane.showMessageDialog(new JFrame(), "Cannot write to logfile. Please start CoR as Administrator", "admin required", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            rgi.shutdown(2);
        }

    }

    public class InnerServer extends Core.CoreInner {

        public ServerNetController netctrl;
        public ServerMapModule netmap;
        public ServerGameController game;
        public ServerPathfinder pathfinder;
	public ServerStatistics serverstats;
        String lastlog = "";
        public ServerMoveManager moveMan;

        @Override
        public void initInner() {
            super.initInner();
            netctrl = servNet;
            netmap = mapMod;
            game = gamectrl;
            pathfinder = spath;
	    serverstats = sstat;
            moveMan = smoveman;
        }

        @Override
        public void logger(String x) {
            if (!lastlog.equals(x)) { // Nachrichten nicht mehrfach speichern
                lastlog = x;
                // Schreibt den Inhalt des Strings zusammen mit dem Zeitpunkt in die
                // log-Datei
                try {
                    FileWriter logwriter = new FileWriter("server_log.txt", true);
                    String temp = String.format("%tc", new Date()) + " - " + x + "\n";
                    logwriter.append(temp);
                    logwriter.flush();
                    logwriter.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    shutdown(2);
                }
            }
        }

        @Override
        public void logger(Throwable t) {
            // Nimmt Exceptions an und schreibt den Stacktrace ins
            // logfile
            try {
                if (debugmode) {
                    System.out.println("ERROR!!!! More info in logfile...");
                }
                FileWriter logwriter = new FileWriter("server_log.txt", true);
                logwriter.append('\n' + String.format("%tc", new Date()) + " - ");
                logwriter.append("[JavaError]:   " + t.toString() + '\n');
                StackTraceElement[] errorArray;
                errorArray = t.getStackTrace();
                for (int i = 0; i < errorArray.length; i++) {
                    logwriter.append("            " + errorArray[i].toString() + '\n');
                }
                logwriter.flush();
                logwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                shutdown(2);
            }

        }
    }
}
