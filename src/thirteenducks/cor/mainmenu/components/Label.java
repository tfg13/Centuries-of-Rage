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
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import thirteenducks.cor.graphics.FontManager;
import thirteenducks.cor.mainmenu.MainMenu;

/**
 * Label - Zeigt Text an
 * @TODO: Die textgröße mit g.scale() anpassen (die größe wird dann im konstruktor mitgegeben) und den Text zentrieren
 *
 * @author michael
 */
public class Label extends Component {

    /**
     * Der Text des Labels
     */
    private String labelText;
    /**
     * Die Farbe, in der der Text geschrieben wird
     */
    private Color color;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das Hauptmenü
     * @param x                     X-Position des Labels
     * @param y                     Y-Position der Labels
     * @param text                  Text des Labels
     */
    public Label(MainMenu mainMenuReference, double x, double y, double width, double height, String text, Color color_t) {
        super(mainMenuReference, x, y, width, height);

        labelText = text;
        color = color_t;


    }

    @Override
    public void init(GameContainer c) {
    }

    @Override
    public void render(Graphics g) {

        g.setColor(color);
        g.setFont(FontManager.getFont0());

        // Der Text soll zentriert gerendert werden, also Textlänge berechnen:
        int textWidth = g.getFont().getWidth(labelText);

        int space = ((this.getWidth() - textWidth) / 2);

        g.drawString(labelText, getX1() + space, getY1());
    }

    /**
     * Setzt die Textfarbe
     * @param color - die gewünschte Textfarbe
     */
    public void setColor(Color color) {
        this.color = color;
    }
}
