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
package thirteenducks.cor.mainmenu;

import java.util.HashMap;
import thirteenducks.cor.mainmenu.components.AnimatedImage;
import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.CoordinateView;
import thirteenducks.cor.mainmenu.components.Frame;

/**
 * Hauptmenü
 *
 * @author michael
 */
public class TheMainMenu extends Container {

    /**
     * X-Bildschirmauflösung
     */
    private int resX;
    /**
     * Y-Bildschirmauflösung
     */
    private int resY;
    /**
     * Die Lobby
     */
    private Container lobbyScreen;

    /**
     * Konstruktor
     *
     * @param resX - X-Auflösung
     * @param resY - Y-Auflösung
     */
    public TheMainMenu(int resX, int resY) {
        super(null, 1, 1, 100, 100); // Dummy, funktioniert nicht


        /**********************************************************************
         * Hintergrund:
         *********************************************************************/
        // Animierter Hintergrund:
        super.addComponent(new AnimatedImage(this, "/img/mainmenu/test.png"));

        // Rahmen:
        super.addComponent(new Frame(this, 0, 0, 99.9999f, 99.9999f));

        // Mauskoordiaten anzeigen:
        super.addComponent(new CoordinateView(this));


        /**********************************************************************
         * Menüs:
         *********************************************************************/
        // Hauptmenü:
        Container startScreen = new StartScreen(this);
        startScreen.setName("startscreen");
        super.addComponent(startScreen);
        startScreen.fadeIn();

        // RandomMapBuilder
        Container randomMapBuilderScreen = new RandomMapBuilderScreen(this);
        randomMapBuilderScreen.setName("randommapbuilderscreen");
        super.addComponent(randomMapBuilderScreen);
        randomMapBuilderScreen.fadeOut();

        // Mehrspieler:
        Container multiplayerScreen = new MultiplayerScreen(this);
        multiplayerScreen.setName("multiplayerscreen");
        super.addComponent(multiplayerScreen);
        multiplayerScreen.fadeOut();

        // Lobby
        lobbyScreen = new LobbyScreen(this);
        lobbyScreen.setName("lobbyscreen");
        super.addComponent(lobbyScreen);
        lobbyScreen.fadeOut();



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
     * Gibt die Lobby zurück, für die Client-Lobby Kommunikation
     *
     * @return - das LobbySreen-Objekt
     */
    public Container getLobby() {
        return lobbyScreen;
    }
}
