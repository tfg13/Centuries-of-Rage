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
 * Ein Button
 * 
 * @author michael
 */
public abstract class ImageButton extends Component {

    /**
     * Der Pfad des Bildes
     */
    String imagePath;
    /**
     * Das Bild
     */
    Image image;
    /**
     * Der Text, der auf dem Button stehen soll
     */
    String text;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das MAinMenu
     * @param x                     X-Position des Buttons
     * @param y                     Y-Position des Buttons
     * @param label                 Der Pfad des Bildes
     * @param text                  Der Text, der auf dem Button stehen soll
     */
    public ImageButton(MainMenu mainMenuReference, int x, int y, int width, int height, String imagepath, String text) {
        super(mainMenuReference, x, y, width, height);
        imagePath = imagepath;

    }

    @Override
    public void init(GameContainer c) {

        try {
            image = new Image(imagePath);

            image = image.getScaledCopy(getWidth(), getHeight());
        } catch (SlickException ex) {
            ex.printStackTrace();
        }

        // Die Maße an das Bild anpassen:
        this.x2 = getX1() + image.getWidth();
        this.y2 = getY1() + image.getHeight();
    }

    @Override
    public void render(Graphics g) {

        if (isMouseHover()) {
            g.drawImage(image, getX1(), getY1());
        } else {
            g.drawImage(image, getX1(), getY1());
        }
    }

    @Override
    public abstract void mouseClicked(int button, int x, int y, int clickCount);
}
