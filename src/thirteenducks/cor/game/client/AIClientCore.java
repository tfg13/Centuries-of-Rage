/*
 *  Copyright 2008, 2009, 2010, 2011:
 *   Tobias Fleig (tfg[AT]online[DOT]de),
 *   Michael Hase (mekhar[AT]gmx[DOT]de),
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
package thirteenducks.cor.game.client;

import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.client.ClientGameController;
import thirteenducks.cor.game.client.ClientStatistics;
import thirteenducks.cor.game.client.ClientMapModule;
import thirteenducks.cor.networks.client.ClientNetController;
import java.awt.Dimension;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.SlickException;
import thirteenducks.cor.graphics.ClientChat;
import thirteenducks.cor.networks.client.ClientNetController;
import thirteenducks.cor.networks.lobby.Lobby;

/**
 * Der Client-Core
 *
 * Startet CoR im Client-Modus
 *
 * Erzeugt ein eigenes Logfile (client_log.txt)
 *
 * @author tfg
 */
public class AIClientCore extends ClientCore {

//    RogAIClientCore.InnerClient rgi;
//    RogGraphics rGraphics;
//    /*RogSound rSound;
//    RogGameLogic rGameLogic;
//    NetClientMapModule rMap;
//    Pathfinder rPathfinder;
//    RogMainMenu rMainMenu; */
//    ClientNetController netController;
//    ClientGameController gamectrl;
//    ClientMapModule mapMod;
//    ClientPathfinder cpath;
//    ClientChat cchat;
//    String playername;
//    RogLobby lobby;
//    ClientStatistics cs;

    public AIClientCore(boolean debug, InetAddress connectTo, int port, String playername_t, DisplayMode mode, boolean fullScreen, HashMap newcfg) throws SlickException {

        
        playername = "AI:"+playername_t;
        lobby = new Lobby();
        cfgvalues = newcfg;


        // Hier beginnt der Code richtig zu laufen
        // Einstellungen aus Startoptionen übernehmen
        debugmode = debug;

        rgi = new AIClientCore.InnerClient(super.playername);

        super.gamectrl = new ClientGameController(rgi);


        //gamectrl = new ClientGameController(rgi);


        // Neues Logfile anlegen (altes Löschen)
        initLogger();
        rgi.logger("[Core] Init RoG-Core");
        if (debugmode) {
            rgi.logger("[Core] Debug-Mode active");
        } else {
            rgi.logger("[Core] Debug-Mode off");
        }

        //Module Laden

        rgi.logger("[CoreInit] Loading Modules & SubModules");

        rgi.logger("[CoreInit] Loading clientgamecontroller...");


        // gamecrtl wird weiter oben initialisiert
        //gamectrl = new ClientGameController(rgi);

        rgi.logger("[CoreInit] Loading graphicsengine");

        rGraphics = new thirteenducks.cor.graphics.CoreGraphics(rgi, new Dimension(mode.getWidth(), mode.getHeight()), fullScreen);

        rgi.logger("[CoreInit]: Loading lobby...");
        // Lobby initialisieren und anzeigen:

        super.lobby.setVisible(true);

        rgi.logger("[CoreInit]: Loading client-netcontroller");

        super.netController = new ClientNetController(rgi, lobby);


        super.mapMod = new ClientMapModule(rgi);

        super.cchat = new ClientChat(rgi);

	super.cs = new ClientStatistics(rgi);


        rgi.initInner();

        /*

        mapMod.initModule();

        rGraphics.initModule();
        rGraphics.initSubs();

         */

        rgi.logger("[Core]: Connecting to server...");

        if (super.netController.connectTo(connectTo, port)) {
            super.lobby.initlobby(netController, rgi);
        } else {
            // Das geht so nicht, das ging nicht
            // Alles wieder abschalten
            super.lobby.dispose();
            rGraphics.destroy();
            throw new java.lang.RuntimeException("IP invalid/unreachable or server not running");
        }


    }

    @Override
    public void initLogger() {
        // Erstellt ein neues Logfile
        try {
            FileWriter logcreator = new FileWriter("client_log.txt");
            logcreator.close();
        } catch (IOException ex) {
            // Warscheinlich darf man das nicht, den Adminmodus emfehlen
            JOptionPane.showMessageDialog(new JFrame(), "Cannot write to logfile. Please start CoR as Administrator", "admin required", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            rgi.shutdown(2);
        }

    }

    public class InnerClient extends ClientCore.InnerClient {

//        RogGraphics rogGraphics;
//        ClientMapModule mapModule;
//        ClientNetController netctrl;
//        ClientGameController game;
//        ClientPathfinder pathfinder;
//        ClientChat chat;
//	ClientStatistics clientstats;
//        String playername;
//        //RogPathfinder rogPathfinder;
//        //RogGameLogic rogGameLogic;
//        //RogSound rogSound;
//        String lastlog = "";

        private InnerClient(String playername_t) {
            playername = playername_t;
        }

        @Override
        public void initInner() {
            super.initInner();
            rogGraphics = rGraphics;
            super.mapModule = mapMod;
            super.netctrl = netController;
            super.game = gamectrl;
            super.chat = cchat;
	    super.clientstats = cs;
            //rogGameLogic = rGameLogic;
            //rogSound = rSound; */
        }

        @Override
        public void logger(String x) {
            if (!lastlog.equals(x)) { // Nachrichten nicht mehrfach speichern
                lastlog = x;
                // Schreibt den Inhalt des Strings zusammen mit dem Zeitpunkt in die
                // log-Datei
                try {
                    FileWriter logwriter = new FileWriter("client_log.txt", true);
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
                FileWriter logwriter = new FileWriter("client_log.txt", true);
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
