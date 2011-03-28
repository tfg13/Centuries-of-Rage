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
 * Der Rahmen um den Ganzen Bildschirm
 *
 * @author michael
 */
public class Frame extends Component {

    /**
     * Konstruktor
     * @param m     Hauptmenü-Referenz
     */
    public Frame(MainMenu m, double x, double y, double width, double height) {
        super(m, x, y, width, height);
    }

    /**
     * Redner-Funktion
     *
     * Zeichnet den Rahmen
     */
    @Override
    public void render(Graphics g) {


        // 1. Rahmen:
        g.setColor(Color.black);

        g.drawLine(this.getX1(), this.getY1(), this.getX2(), this.getY1());
        g.drawLine(this.getX1(), this.getY1(), this.getX1(), this.getY2());
        g.drawLine(this.getX1(), this.getY2(), this.getX2(), this.getY2());
        g.drawLine(this.getX2(), this.getY1(), this.getX2(), this.getY2());


        // 2. Rahmen:
        g.setColor(Color.gray);

        g.drawLine(this.getX1() + 1 + 1, this.getY1() + 1 + 1, this.getX2() - 1, this.getY1() + 1);
        g.drawLine(this.getX1() + 1, this.getY1() + 1, this.getX1() + 1, this.getY2() - 1);
        g.drawLine(this.getX1() + 1, this.getY2() - 1, this.getX2() - 1, this.getY2() - 1);
        g.drawLine(this.getX2() - 1, this.getY1() + 1, this.getX2() - 1, this.getY2() - 1);

        // 3. Rahmen:
        g.setColor(Color.black);


        g.drawLine(this.getX1() + 2 + 1, this.getY1() + 2 + 1, this.getX2() - 2, this.getY1() + 2);
        g.drawLine(this.getX1() + 2, this.getY1() + 2, this.getX1() + 2, this.getY2() - 2);
        g.drawLine(this.getX1() + 2, this.getY2() - 2, this.getX2() - 2, this.getY2() - 2);
        g.drawLine(this.getX2() - 2, this.getY1() + 2, this.getX2() - 2, this.getY2() - 2);



    }
}
