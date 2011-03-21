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

import java.net.InetAddress;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.mainmenu.components.CheckBox;
import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.ImageButton;
import thirteenducks.cor.mainmenu.components.TextBox;
import thirteenducks.cor.mainmenu.components.TiledImage;

/**
 * Hier kann man einen Server starten
 *
 * @author michael
 */
public class StartServerScreen extends Container {

    /**
     * Die Debug-CheckBox
     * damit kann man steuern ob der Server im Debug-Modus gestartet wird
     */
    CheckBox myCheckBox;
    /**
     * Die Textbox, mit der provisorisch die Map ausgewählt wird
     */
    TextBox myTextBox;

    /**
     * Konstruktor
     *
     * @param m     Referenz auf das Hauptmenü
     */
    public StartServerScreen(MainMenu m) {
        super(m, 15, 15, 80, 80);

        // Hintergrund:
        super.addComponent(new TiledImage(super.getMainMenu(), 30, 30, 30, 30, "/img/mainmenu/rost.png"));

        // Die Debug-Checkbox:
        myCheckBox = new CheckBox(super.getMainMenu(), 40, 40, "/img/mainmenu/checkbox-normal.png", "/img/mainmenu/checkbox-active.png");
        super.addComponent(myCheckBox);

        // MapWahl-Textbox (provisorisch)
        myTextBox = new TextBox(super.getMainMenu(), 40, 60);
        myTextBox.setText("Randommap.map");

        // Der Start-Button
        super.addComponent(new ImageButton(super.getMainMenu(), 40, 50, 13, 6, "/img/mainmenu/buttonnew.png", "Start Server") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                super.getMainMenu().getMenu("startscreen").fadeIn();

                // @TODO: Server Starten und selber joinen

                boolean debug = myCheckBox.isChecked();
                String map = "/map/main/" + myTextBox.getText();

                super.getMainMenu().startServer(debug, map);
                super.getMainMenu().joinServer();



                fadeOut();

            }
        });
    }
}
