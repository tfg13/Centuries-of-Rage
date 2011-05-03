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
package de._13ducks.cor.mainmenu;

import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.mainmenu.components.*;
import java.util.HashMap;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.graphics.GraphicsImage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.newdawn.slick.Input;

/**
 * Grafisches Hauptmenü
 *
 * @author michael
 */
public class MainMenu extends Container {

    /**
     * Die einzelnen Menüs
     */
    HashMap<String, Container> menus;
    /**
     * X-Bildschirmauflösung
     */
    private int resX;
    /**
     * Y-Bildschirmauflösung
     */
    private int resY;
    /**
     * ClientCore, zum Spiel starten etc.
     */
    private ClientCore core;
    /**
     * Lobby-Referenz
     */
    private LobbyScreen lobbyScreen;
    /**
     * Referenz auf die ImageMap
     */
    private HashMap<String, GraphicsImage> imgMap;

    /**
     * Konstruktor
     */
    public MainMenu(int resX, int resY, ClientCore clientcore, HashMap<String, GraphicsImage> imgMap) {
        super(null, 0, 0, (double) resX, (double) resY);

        this.imgMap = imgMap;
        super.setMainMenuReference(this);

        this.resX = resX;
        this.resY = resY;
        this.core = clientcore;

        menus = new HashMap<String, Container>();

        /**
         * Komponenten initialisieren:
         */
        initComponents();
    }

    @Override
    public void keyPressed(int key, char c) {
        if (key == Input.KEY_F10) {
            System.exit(0);
        }
    }

    /**
     * Initialisiert die Komponenten des Hauptmenüs
     */
    private void initComponents() {


        /**********************************************************************
         * Hintergrund:
         *
         *********************************************************************/
        MenuBackground background = new MenuBackground(this, 0, 0, 100, 100, imgMap);
        super.addComponent(background);


        /**********************************************************************
         * Menüs:
         *********************************************************************/
        // Hauptmenü:
        Container startScreen = new StartScreen(this);
        menus.put("startscreen", startScreen);
        super.addComponent(startScreen);
        startScreen.activate();

        // StartServerscreen
        Container startServerScreen = new StartServerScreen(this);
        menus.put("startserverscreen", startServerScreen);
        super.addComponent(startServerScreen);
        startServerScreen.deactivate();

        // RandomMapBuilder
        Container randomMapBuilderScreen = new RandomMapBuilderScreen(this);
        menus.put("randommapbuilderscreen", randomMapBuilderScreen);
        super.addComponent(randomMapBuilderScreen);
        randomMapBuilderScreen.deactivate();

        // Mehrspieler:
        Container MultiplayerScreen = new MultiplayerScreen(this);
        menus.put("multiplayerscreen", MultiplayerScreen);
        super.addComponent(MultiplayerScreen);
        MultiplayerScreen.deactivate();

        // Lobby
        lobbyScreen = new LobbyScreen(this);
        menus.put("lobbyscreen", lobbyScreen);
        super.addComponent(lobbyScreen);
        lobbyScreen.deactivate();



        // Koordinatenanzeige:
        super.addComponent(new CoordinateView(this));
    }

    /**
     * Gibt ein bestimmtes Menü zurück
     *
     * @param name - Name des Menüs
     * @return     - Das Menü 
     */
    public Container getMenu(String name) {
        return menus.get(name);
    }

    /**
     * Getter für X-Auflösung
     * @return X-Auflösung
     */
    public int getResX() {
        return resX;
    }

    /**
     * Getter für Y-Auflösung
     * @return Y-Auflösung
     */
    public int getResY() {
        return resY;
    }

    /**
     * Startet einen Server
     *
     * @param debug - soll der Server im Debug-Modus gestartet werden?
     * @param map   - der Name der Map, z.B. "/map/main/Randommap.map"
     */
    public void startServer(final boolean debug, final String map) {
        System.out.println("Starting Server with debug=" + debug + " and map=" + map);

        Thread serverThread = new Thread(new Runnable() {

            public void run() {
                new ServerCore(debug, map);
            }
        });

        serverThread.setName("serverThread");

        serverThread.start();
    }

    /**
     * Tritt einer Partie bei
     * @return true, wenns geklappt hat
     */
    public boolean joinServer(String server) {
        System.out.println("Joining Server...");
        // @TODO: ClientCore.joinServer() rufen, bei erfolg lobby anzeigen
        final InetAddress adress;
        try {
            adress = InetAddress.getByName(server);
        } catch (UnknownHostException ex) {
            return false;
        }
        final int port = 39264;// default port

        core.joinServer("unknownP", adress, port);
        return true;
    }

    /**
     * Getter für ClientCore
     * primär für die Lobby
     * @return - ClientCore-Referenz
     */
    public ClientCore getClientCore() {
        return core;
    }

    /**
     * Gibt die Lobby zurück
     * @return - die Lobby
     */
    public LobbyScreen getLobby() {
        return lobbyScreen;
    }

    /**
     * zeigt die Lobby an
     */
    public void showlobby() {
        for (Container c : menus.values()) {
            c.deactivate();
        }
        lobbyScreen.activate();
    }

    /**
     * Getter für ImageMAp
     * @return die imgMap
     */
    public HashMap<String, GraphicsImage> getImgMap() {
        return imgMap;
    }
}
