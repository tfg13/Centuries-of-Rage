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
import thirteenducks.cor.mainmenu.components.TextBox;

/**
 * Der Server-beitreten-Bildschirm
 * bietet einfache Connect-to-IP-Funktion
 *
 * @TODO: Serverbrowser
 *
 * @author michael
 */
public class JoinServerScreen extends Container {

    /**
     * MainMenu-Referenz
     */
    MainMenu mainMenu;
    /**
     * Das Texteingabefeld für IP oder Servername
     */
    TextBox textBox;

    /**
     * Konstruktor
     *
     * @param m     Referenz auf das Hauptmenü
     */
    public JoinServerScreen(MainMenu m) {
        super(m, 15, 30, 70, 40);

        mainMenu = m;

        textBox = new TextBox(mainMenu, 30, 30);
        super.addComponent(textBox);

        super.addComponent(new ImageButton(mainMenu, 40, 40, 10, 8, "img/mainmenu/buttonnew.png", "Join") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                System.out.println("Joining Game: " + textBox.getText());
                fadeOut();
            }
        });
    }
}
