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

import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.ImageButton;
import thirteenducks.cor.mainmenu.components.ScaledImage;

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
    public StartScreen(MainMenu m) {
        super(m, 15, 85, 80, 8);

        mainMenu = m;

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
                mainMenu.getMenu("multiplayerscreen").fadeIn();

                fadeOut();

            }
        });

        // Options Button:
        super.addComponent(new ImageButton(mainMenu, 44, 86, 13, 6, "img/mainmenu/buttonnew.png", "OPTIONS") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });

        // Map Editor
        super.addComponent(new ImageButton(mainMenu, 58, 86, 13, 6, "img/mainmenu/buttonnew.png", "Map Editor") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                mainMenu.getMenu("randommapbuilderscreen").fadeIn();
                fadeOut();
            }
        });

        // Quit Game Button:
        super.addComponent(new ImageButton(mainMenu, 72, 86, 13, 6, "img/mainmenu/buttonnew.png", "QUIT") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                System.exit(0);
            }
        });

        // TEST Lobby Button
        super.addComponent(new ImageButton(mainMenu, 90, 86, 13, 6, "img/mainmenu/buttonnew.png", "LOBBY") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                mainMenu.getMenu("lobbyscreen").fadeIn();
                fadeOut();
            }
        });
    }
}
