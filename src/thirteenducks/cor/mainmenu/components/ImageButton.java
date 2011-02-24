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
    ScaledImage image;
    /**
     * Der Text, der auf dem Button steht
     */
    Label label;
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

        label = new Label(mainMenuReference, x+1.85f, y+1.8f, text, Color.yellow);
        image = new ScaledImage(mainMenuReference, x, y, width, height, imagepath);

    }

    @Override
    public void init(GameContainer c) {
        image.init(c);
        label.init(c);
    }

    @Override
    public void render(Graphics g) {

        image.render(g);
        label.render(g);
    }

    @Override
    public void mouseHoverChanged(boolean newstate) {
        if (newstate) {
            image.setColor(Color.orange);
        } else {
            image.setColor(null);
        }
    }

    @Override
    public abstract void mouseClicked(int button, int x, int y, int clickCount);
}
