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
import org.newdawn.slick.Image;
import thirteenducks.cor.mainmenu.MainMenu;

/**
 * Ein Button
 * 
 * @author michael
 */
public abstract class TestButton extends Component {

    /**
     * Der Text, der auf dem Button steht
     */
    String text;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das MAinMenu
     * @param x                     X-Position des Buttons
     * @param y                     Y-Position des Buttons
     * @param label                 Der Text der auf dem Button steht
     */
    public TestButton(MainMenu mainMenuReference, int x, int y, String label) {
        super(mainMenuReference, x, y, (label.length()), 3);
        text = label;
    }

    @Override
    public void render(Graphics g) {


        if (isMouseHover()) {
            g.setColor(Color.lightGray);
        } else {
            g.setColor(Color.gray);
        }
        g.fillRect(getX1(), getY1(), getX2() - getX1(), getY2() - getY1());


        if (isMouseHover()) {
            g.setColor(Color.red);
        } else {
            g.setColor(Color.black);
        }
        g.drawLine(getX1(), getY1(), getX2(), getY1());
        g.drawLine(getX1(), getY2(), getX2(), getY2());
        g.drawLine(getX1(), getY1(), getX1(), getY2());
        g.drawLine(getX2(), getY1(), getX2(), getY2());

        g.drawString(text, getX1() + 5, getY1() + 3);

    }

    @Override
    public abstract void mouseClicked(int button, int x, int y, int clickCount);
}
