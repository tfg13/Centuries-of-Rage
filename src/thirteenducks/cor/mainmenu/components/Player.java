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
package thirteenducks.cor.mainmenu.components;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.mainmenu.MainMenu;

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
        super(m, x, y, 35, 10);
        ready = false;
        name = "";


        nameLabel = new Label(m, (float) x + 1, (float) y + 1, 10, 8, name, Color.black);
        super.addComponent(nameLabel);
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
