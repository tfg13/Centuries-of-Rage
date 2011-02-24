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
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.mainmenu.MainMenu;

/**
 * Label - Zeigt Text an
 * @TODO: Die textgröße mit g.scale() anpassen (die größe wird dann im konstruktor mitgegeben)
 *
 * @author michael
 */
public class Label extends Component {

    /**
     * Der Text des Labels
     */
    String labelText;
    /**
     * Die Farbe, in der der Text geschrieben wird
     */
    Color color;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das Hauptmenü
     * @param x                     X-Position des Labels
     * @param y                     Y-Position der Labels
     * @param text                  Text des Labels
     */
    public Label(MainMenu mainMenuReference, float x, float y, String text, Color color_t) {
        super(mainMenuReference, x, y, 1, 1);

        labelText = text;
        color = color_t;
    }

    @Override
    public void init(GameContainer c) {

        // Breite und Höhe anpassen:
        int width = c.getGraphics().getFont().getWidth(labelText);
        int height = c.getGraphics().getFont().getHeight(labelText);

        this.setX2(getX1() + width);
        this.setY2(getY1() + height);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(color);

        g.drawString(labelText, getX1(), getY1());
    }
}
