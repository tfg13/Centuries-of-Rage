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

import de._13ducks.cor.mainmenu.components.Container;
import de._13ducks.cor.mainmenu.components.Frame;
import de._13ducks.cor.mainmenu.components.ImageButton;
import de._13ducks.cor.mainmenu.components.TextBox;
import de._13ducks.cor.mainmenu.components.TiledImage;

/**
 * Der Mehrspieler-Bildschirm
 * enthält den ServerBrowser zum joinen und einen Button zum Server starten
 *
 * @author michael
 */
public class MultiplayerScreen extends Container {

    /**
     * Die Textbox für Join to IP
     */
    private TextBox serverBox;

    /**
     * Konstruktor
     * @param m - Hauptmenü-Referenz
     */
    public MultiplayerScreen(final MainMenu m) {
        super(m, 0, 0, 100, 100);

        // Hintergrund:
        super.addComponent(new TiledImage(m, 10, 10, 80, 70, "img/mainmenu/rost.png"));

        // Rahmen:
        super.addComponent(new Frame(m, 10, 10, 80, 70));

        // Rahmen für serverauswahl
        super.addComponent(new Frame(m, 15, 14, 71, 54));

        // Ip-Eingabefeld:
        serverBox = new TextBox(m, 14, 71);
        super.addComponent(serverBox);

        // Join-Button:
        super.addComponent(new ImageButton(m, 45, 70, 12, 6, "img/mainmenu/buttonnew.png", "Join") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                super.getMainMenu().joinServer(serverBox.getText());
            }

            @Override
            public void keyPressed(int key, char c) {
                if (key == 28) {
                    super.getMainMenu().joinServer(serverBox.getText());
                }
                System.out.println(key);
            }
        });

        // Start-Servr-Button:
        super.addComponent(new ImageButton(m, 60, 70, 12, 6, "img/mainmenu/buttonnew.png", "Start Server") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                // @TODO: Server Starten, Lobby anzeigen
                this.getMainMenu().getMenu("startserverscreen").fadeIn();
                fadeOut();
            }
        });

        // zurück-button:
        super.addComponent(new ImageButton(m, 75, 70, 12, 6, "img/mainmenu/buttonnew.png", "Back") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                super.getMainMenu().getMenu("startscreen").fadeIn();
                fadeOut();
            }
        });
    }
}
