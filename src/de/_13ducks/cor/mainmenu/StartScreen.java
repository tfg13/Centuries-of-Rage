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

import de._13ducks.cor.graphics.GraphicsImage;
import de._13ducks.cor.mainmenu.components.Container;
import de._13ducks.cor.mainmenu.components.ImageButton;
import de._13ducks.cor.mainmenu.components.MenuSlogans;
import de._13ducks.cor.mainmenu.components.ScaledImage;
import de._13ducks.cor.mainmenu.components.TiledImage;
import de._13ducks.cor.tools.randommapbuilder.RandomMapBuilder;
import java.util.HashMap;
/**
 * Der Startbildschirm, der als erstes angezeigt wird
 *
 * @author michael
 */
public class StartScreen extends Container {

    /**
     * MainMenu-Referenz
     */
    MainMenu mainMenu;

    /**
     * Konstruktor
     *
     * @param m     Referenz auf das Hauptmenü
     */
    public StartScreen(MainMenu m, HashMap<String, GraphicsImage> imgMap) {
        super(m, 15, 85, 80, 8);

        mainMenu = m;

         /**********************************************************************
         * Slogans:
         *
         *********************************************************************/
        MenuSlogans slogans = new MenuSlogans(mainMenu, 0, 0, 100, 100, imgMap);
        super.addComponent(slogans);

        // Hintergrund:
        super.addComponent(new ScaledImage(mainMenu, -20, 85, 140, 8, "img/mainmenu/buttonnew.png"));


        // Einzelspieler:
        super.addComponent(new ImageButton(mainMenu, 16, 86, 13, 6, "img/mainmenu/buttonnew.png", "Singleplayer") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                // @TODO ert mal singleplayer programmieren....
            }
        });

        // Join Game Button:
        super.addComponent(new ImageButton(mainMenu, 30, 86, 13, 6, "img/mainmenu/buttonnew.png", "Multiplayer") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                mainMenu.getMenu("multiplayerscreen").activate();

                deactivate();

            }
        });

        // Options Button:
        super.addComponent(new ImageButton(mainMenu, 44, 86, 13, 6, "img/mainmenu/buttonnew.png", "Options") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });

        // Map Editor
        super.addComponent(new ImageButton(mainMenu, 58, 86, 13, 6, "img/mainmenu/buttonnew.png", "Map Editor") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                RandomMapBuilder.newMap((byte) 4, (byte) 0, (byte) 0, (byte) 4);
            }
        });

        // Quit Game Button:
        super.addComponent(new ImageButton(mainMenu, 72, 86, 13, 6, "img/mainmenu/buttonnew.png", "Quit") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                System.exit(0);
            }
        });

        // Schild

        super.addComponent(new ScaledImage(mainMenu, 11.20833333, 28.666666, 26.1458333, 23.58333333, "img/mainmenu/sign1.png"));
        super.addComponent(new ScaledImage(mainMenu, 37.3, 28.666666, 23.54166667, 23.58333333, "img/mainmenu/sign2.png"));
        super.addComponent(new TiledImage(mainMenu, 0, 28.666666, 11.20833333, 4.1666666, "img/mainmenu/balken.png"));

        // Blätter

        super.addComponent(new ScaledImage(mainMenu, 19.4, -14.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -3, -5.9, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 6.9, -4.3, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 10.3, -0.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 0.3, 2.3, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -6.9, 6.3, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 14.9, -8.0, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 6.6,  9.0, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -3.3, 18.6, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 4.6, 21.0, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -4.2, 26.4, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 3.5, 30.9, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -4.2, 33.9, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 1.1, 42.5, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -2.6, 52.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -6.1, 55.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, 1.3, 60.6, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -3.4, 67.8, 13.333, 21.333, "img/mainmenu/blatt.png"));
        super.addComponent(new ScaledImage(mainMenu, -7.7, 76.1, 13.333, 21.333, "img/mainmenu/blatt.png"));
    }
}
