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

import de._13ducks.cor.mainmenu.MainMenu;
import java.util.ArrayList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 * Ein Dropdown-Steuerelement
 * @author michael
 */
public class Dropdown extends Component {

    /**
     * Die Liste aller Items des Dropdowns
     */
    private ArrayList<String> items;
    /**
     * Der Index des ausgewählten Objekts
     */
    private int selectedIndex;
    /**
     * Der Index des ersten angezeigten Elements
     */
    private int firstItem;
    /**
     * Der Index des letzten angezeigten Items
     */
    private int lastItem;
    /**
     * Die Höhe des Dropdown-Menüs
     */
    private int dropdownMenuHeight;
    /**
     * Gibt an, ob das Dropdown-Menü gerade "ausgefahren" ist
     */
    private boolean showMenu;

    /**
     * Konstruktor
     * @param m - Hauptmenü-Referenz
     * @param x - X-Koordinate
     * @param y - Y-Koordinate
     * @param width - Die Breite des Dropdowns
     * @param items - Eine ArrayList mit den Items, die das Dropdown anbieten soll
     */
    public Dropdown(MainMenu m, double x, double y, double width, ArrayList<String> items) {
        super(m, x, y, width, 4.5);

        this.items = items;

        selectedIndex = 0;
        showMenu = false;



        firstItem = 0;
        if (this.items.size() < 8) {
            lastItem = this.items.size();
        } else {
            lastItem = 8;
        }
    }

    @Override
    public void render(Graphics g) {
        if (showMenu) {
            // Dropdown-Menü anzeigen:

            dropdownMenuHeight = g.getFont().getLineHeight() * 8;

            g.setColor(Color.lightGray);
            g.fillRect(this.getX1(), this.getY1(), this.getWidth(), dropdownMenuHeight);
            g.setColor(Color.darkGray);
            g.drawRect(this.getX1(), this.getY1(), this.getWidth(), dropdownMenuHeight);

            for (int i = firstItem; i < lastItem; i++) {
                g.drawString(items.get(i), this.getX1() + 1, this.getY1() + g.getFont().getLineHeight() * (i - firstItem));
            }
        } else {
            // Nur das ausgewählte Element zeigen:
            g.setColor(Color.lightGray);
            g.fillRect(this.getX1(), this.getY1(), this.getWidth(), g.getFont().getLineHeight() + 2);
            g.setColor(Color.darkGray);
            g.drawRect(this.getX1(), this.getY1(), this.getWidth(), g.getFont().getLineHeight() + 2);
            g.drawString(getSelectedItem(), this.getX1() + 1, this.getY1() + 1);
        }
    }

    /**
     * gibt das ausgewählte Element zurück
     * @return
     */
    public String getSelectedItem() {
        return items.get(selectedIndex);
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (this.showMenu == false) {
            showMenu();
        }
    }

    @Override
    public void mouseClickedAnywhere(int button, int x, int y, int clickCount) {
        if (this.showMenu == true) {
            hideMenu();

        }
    }

    /**
     * verbirgt das Dropdown-Menü
     */
    public void hideMenu() {
        this.showMenu = false;
        this.setRelativeHeight(10);
    }

    /**
     * zeigt das Dropdown-Menü an
     */
    private void showMenu() {
        this.showMenu = true;
        this.setAbsoluteHeight(dropdownMenuHeight);
    }
}
