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
import org.newdawn.slick.AppletGameContainer.Container;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import thirteenducks.cor.mainmenu.MainMenu;

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

    public ScaledImage(MainMenu mainMenuReference, int x, int y, double width, double height, String imagepath) {
        super(mainMenuReference, x, y, width, height);

        imagePath = imagepath;
    }

    @Override
    public void init(GameContainer c) {
        try {
            image = new Image(imagePath);

            // Das Bild auf die Größe dieser Komponente skalieren:
            image = image.getScaledCopy(getWidth(), getHeight());

        } catch (SlickException ex) {
            ex.printStackTrace();
            System.out.print("MainMenu: Error loading image " + imagePath + " !");
        }
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(image, this.getX1(), this.getY1());
    }

    @Override
    public void setAlpha(float alpha) {
        image.setAlpha(alpha);
    }
}