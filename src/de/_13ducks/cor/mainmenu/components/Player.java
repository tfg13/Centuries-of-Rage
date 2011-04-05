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
package de._13ducks.cor.mainmenu.components;

import org.newdawn.slick.Color;
import de._13ducks.cor.mainmenu.MainMenu;

/**
 * Ein Spielerplatz in der Lobby
 *
 * @author michael
 */
public class Player extends Container {

    /**
     * Ist der Spieler bereit?
     */
    private boolean ready;
    /**
     * Der Name des Spielers
     */
    private String name;
    /**
     * Das Label mit dem Spielernamen
     */
    private Label nameLabel;

    /**
     * Konstruktor
     * erzeugt einen "leeren" Spieler
     */
    public Player(MainMenu m, double x, double y) {
        super(m, x, y, 35, 6);
        ready = false;
        name = "";
        super.fadeIn();


        super.addComponent(new Frame(m, (float) x, (float) y, 43.5f, 6.0f));

        nameLabel = new Label(m, (float) x, (float) y, 10, 8, name, Color.black);
        super.addComponent(nameLabel);

        super.addComponent(new CheckBox(m, (int) x + 30, (int) y + 1, "/img/mainmenu/checkbox-normal.png", "img/mainmenu/checkbox-active.png") {

            @Override
            public void checkboxChanged() {
                if (this.isChecked()) {
                    ready = true;
                    nameLabel.setColor(Color.green);
                } else {
                    ready = false;
                    nameLabel.setColor(Color.black);
                }
            }
        });
    }

    /**
     * Setter für name
     * @param name
     */
    public void setPlayerName(String name) {
        this.name = name;
        nameLabel.setName(name);
    }

    /**
     * Getter für name
     */
    public String getPlayerName() {
        return name;
    }

    /**
     * Getter für ready
     * @return - true wenn der Spieler bereit ist
     */
    public boolean isReady() {
        return this.ready;
    }
}
