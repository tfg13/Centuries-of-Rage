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
import org.newdawn.slick.Graphics;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.graphics.GraphicsImage;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
    /**
     * Render-Funktion
     * rendert alle SubKomponenten
     */
    public void render(Graphics g) {
        for (Component c : super.getComponents()) {
            c.render(g);
        }
    }

  
    @Override
    public void mouseMoved(int x, int y) {
        for (Component c : super.getComponents()) {
            c.mouseMoved(x, y);
        }
    }

    @Override
    /**
     * Wird bei Mausklicks aufgerufen
     */
    public void mouseClicked(int button, int x, int y, int clickCount) {
        for (Component c : super.getComponents()) {
            c.mouseClickedAnywhere(button, x, y, clickCount);
        }
    }

    @Override
    /**
     * Wird bei Tastendruck aufgerufen
     */
    public void keyPressed(int key, char c) {
        for (Component comp : super.getComponents()) {
            comp.keyPressed(key, c);
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
        startScreen.fadeIn();

        // StartServerscreen
        Container startServerScreen = new StartServerScreen(this);
        menus.put("startserverscreen", startServerScreen);
        super.addComponent(startServerScreen);
        startServerScreen.fadeOut();

        // RandomMapBuilder
        Container randomMapBuilderScreen = new RandomMapBuilderScreen(this);
        menus.put("randommapbuilderscreen", randomMapBuilderScreen);
        super.addComponent(randomMapBuilderScreen);
        randomMapBuilderScreen.fadeOut();

        // Mehrspieler:
        Container MultiplayerScreen = new MultiplayerScreen(this);
        menus.put("multiplayerscreen", MultiplayerScreen);
        super.addComponent(MultiplayerScreen);
        MultiplayerScreen.fadeOut();

        // Lobby
        lobbyScreen = new LobbyScreen(this);
        menus.put("lobbyscreen", lobbyScreen);
        super.addComponent(lobbyScreen);
        lobbyScreen.fadeOut();

        // Schild

        super.addComponent(new ScaledImage(this, 11.20833333, 28.666666, 26.1458333, 23.58333333, "img/mainmenu/sign1.png"));
        super.addComponent(new ScaledImage(this, 37.34375, 28.666666, 23.54166667, 23.58333333, "img/mainmenu/sign2.png"));
        super.addComponent(new TiledImage(this, 0, 28.666666, 11.20833333, 4.1666666, "img/mainmenu/balken.png"));

        // Blätter

        super.addComponent(new ScaledImage(this, 19.4, -14.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -3, -5.9, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 6.9, -4.3, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 10.3, -0.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 0.3, 2.3, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -6.9, 6.3, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 14.9, -8.0, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 6.6,  9.0, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -3.3, 18.6, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 4.6, 21.0, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -4.2, 26.4, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 3.5, 30.9, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -4.2, 33.9, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 1.1, 42.5, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -2.6, 52.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -6.1, 55.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, 1.3, 60.6, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -3.4, 67.8, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(this, -7.7, 76.1, 13.333, 21.333, "img/mainmenu/blatt.png"));

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
            c.fadeOut();
        }
        lobbyScreen.fadeIn();
    }

    /**
     * Getter für ImageMAp
     * @return die imgMap
     */
    public HashMap<String, GraphicsImage> getImgMap() {
        return imgMap;
    }
}
