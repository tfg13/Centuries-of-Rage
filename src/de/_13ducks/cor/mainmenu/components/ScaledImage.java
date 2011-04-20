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

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import de._13ducks.cor.mainmenu.MainMenu;

/**
 * Ein Bild
 * Das geladede Bild wird auf die Größe dieser Komponente skaliert
 *
 * @author michael
 */
public class ScaledImage extends Component {

    /**
     * Das Bild, das gerendert wird
     */
    Image image;
    /**
     * Pfad des zu ladenden Bilds
     */
    String imagePath;
    /**
     * Die Farbe die zum rendern des Bildes verwendet wird
     */
    Color color;

    public ScaledImage(MainMenu mainMenuReference, double x, double y, double width, double height, String imagepath) {
        super(mainMenuReference, x, y, width, height);

        imagePath = imagepath;
        
        image = getMainMenu().getImgMap().get(imagePath).getImage();
        image = image.getScaledCopy(getWidth(), getHeight());
    }

  

    @Override
    public void render(Graphics g) {
        if (color != null) {
            g.drawImage(image, this.getX1(), this.getY1(), color);
        } else {
            g.drawImage(image, this.getX1(), this.getY1());
        }
    }

    /**
     * Setzt de nAlpha-Wert und steuert damit die Transparenz des Bildes
     * @param alpha - der neue Transparenz-Wert
     */
    @Override
    public void setAlpha(float alpha) {
        image.setAlpha(alpha);
    }

    /**
     * Setzt die Farbe, die als Filter zum Zeichnen verwendet wird
     * @param theColor - Die Farbe die zum rendern des Bildes benutzt wird
     */
    public void setColor(Color color) {
        this.color = color;
    }
}
