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
public class TextBox extends Component {

    /**
     * Der Text der Textbox
     */
    private String text;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das MAinMenu
     * @param x                     X-Position des Buttons
     * @param y                     Y-Position des Buttons
     * @param label                 Der Pfad des Bildes
     */
    public TextBox(MainMenu mainMenuReference, int x, int y, String text_t) {
        super(mainMenuReference, x, y, 30,7);
        text = text_t;

    }

    @Override
    public void render(Graphics g) {

        // Kasten mit Hintergrundfarbe füllen:
        g.setColor(Color.gray);
        g.fillRect(getX1(), getY1(), getX2(), getY2());

        g.setColor(Color.black);

        // Rahmen zeichnen:
        g.drawLine(getX1(), getY1(), getX2(), getY1());
        g.drawLine(getX2(), getY1(), getX2(), getY2());
        g.drawLine(getX1(), getY2(), getX2(), getY2());
        g.drawLine(getX1(), getY1(), getX1(), getY2());

        // Text zeichnen:
        g.drawString(text, getX1() + 5, getY1() + 5);
    }

    @Override
    public void keyPressed(int key, char c) {
        // Bei return(14) das letzte Zeichen entfernen, ansonsten den entsprechenden Buchstaben dranhängen:
        if (key != 14) {
            text += c;
        } else {
            if (text.length() > 0) {
                text = text.substring(0, text.length() - 1);
            }
        }
    }
}
