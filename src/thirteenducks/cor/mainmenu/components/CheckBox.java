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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import thirteenducks.cor.mainmenu.MainMenu;

/**
 * Eine Checkbox
 * 
 * @author michael
 */
public class CheckBox extends Component {

    /**
     * Der Pfad des "normal"-Bildes
     */
    String normalImagePath;
    /**
     * Der Pfad des "aktiv"-Bildes
     */
    String activeImagePath;
    /**
     * Das Bild im Normalzustand
     */
    Image normalImage;
    /**
     * Das Bild im aktiven Zustand
     */
    Image activeImage;
    /**
     * Ist die checkbox aktiv?
     */
    private boolean checked;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das MAinMenu
     * @param x                     X-Position des Buttons
     * @param y                     Y-Position des Buttons
     * @param label                 Der Pfad des Bildes
     */
    public CheckBox(MainMenu mainMenuReference, int x, int y, String imagepath, String activeimagepath) {
        super(mainMenuReference, x, y, 1, 1);
        normalImagePath = imagepath;
        activeImagePath = activeimagepath;
        checked = false;
    }

    @Override
    public void init(GameContainer c) {

        try {
            normalImage = new Image(normalImagePath);
            activeImage = new Image(activeImagePath);
        } catch (SlickException ex) {
            ex.printStackTrace();
        }

        // Die Maße an das Bild anpassen:
        this.x2 = getX1() + normalImage.getWidth();
        this.y2 = getY1() + normalImage.getHeight();
    }

    @Override
    public void render(Graphics g) {
        // @TODO: hervorhebung bei mousehover
        if (checked) {
            g.drawImage(activeImage, getX1(), getY1());
        } else {
            g.drawImage(normalImage, getX1(), getY1());
        }
    }

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        checked = !checked;
    }

    /**
     * Setzt den Status der Checkbox
     * @param active_t      Neuer Status
     */
    void setChecked(boolean active_t) {
        checked = active_t;
    }

    /**
     * Getter für checked
     * @return  true, wenn die Checkbox angehackt ist, sonst false
     */
    public boolean isChecked() {
        return checked;
    }
}
