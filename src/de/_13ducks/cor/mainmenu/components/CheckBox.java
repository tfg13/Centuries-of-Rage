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

import de._13ducks.cor.graphics.Renderer;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.mainmenu.MainMenu;

/**
 * Eine Checkbox
 * 
 * @author michael
 */
public abstract class CheckBox extends Component {

    /**
     * Der Pfad des "normal"-Bildes
     */
    private String normalImagePath;
    /**
     * Der Pfad des "aktiv"-Bildes
     */
    private String activeImagePath;
    /**
     * Ist die checkbox aktiv?
     */
    private boolean checked;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das MainMenu
     * @param x                     X-Position des Buttons
     * @param y                     Y-Position des Buttons
     * @param imagepath             Pfad des Bildes im Normalzustand
     * @param activeimagepath       Pfad des Bildes für den aktivierten Zustand
     */
    public CheckBox(MainMenu mainMenuReference, double x, double y, String imagepath, String activeimagepath) {
        super(mainMenuReference, x, y, 1, 1);
        normalImagePath = imagepath;
        activeImagePath = activeimagepath;
        checked = false;

        // Die Maße an das Bild anpassen:
        this.setX2(getX1() + Renderer.getImageInfo(normalImagePath).getWidth());
        this.setY2(getY1() + Renderer.getImageInfo(normalImagePath).getHeight());
    }

    public CheckBox(MainMenu mainMenuReference, double x, double y) {
        // Einfach mit Defaultwerten weiterleiten
        this(mainMenuReference, x, y, "img/mainmenu/nrdy.png", "img/mainmenu/rdy.png");
    }

   

    @Override
    public void render(Graphics g) {
        // @TODO: hervorhebung bei mousehover
        if (checked) {
            Renderer.drawImage(activeImagePath, getX1(), getY1());
        } else {
            Renderer.drawImage(normalImagePath, getX1(), getY1());
        }
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        checked = !checked;
        checkboxChanged();
    }

    /**
     * Setzt den Status der Checkbox
     * @param active_t      Neuer Status
     */
    void setChecked(boolean active_t) {
        checked = active_t;
    }

    /**
     * Fragt de nstatus der Checkbox ab
     * @return  true, wenn die Checkbox angehackt ist, sonst false
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Wird aufgerufen wenn die CheckBox angeklickt wird
     */
    public abstract void checkboxChanged();
}
