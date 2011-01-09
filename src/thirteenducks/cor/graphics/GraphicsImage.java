/*
 *  Copyright 2008, 2009, 2010:
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


package thirteenducks.cor.graphics;

import java.awt.image.BufferedImage;
import org.newdawn.slick.Image;



/**
 *
 * @author tfg
 */
public class GraphicsImage {

    private String imgName; // Name des Bildes, zum wiederfinden
    private Image image;

    public GraphicsImage(Image b) {
        image = b;
    }

    public String getImageName() {
        return imgName;
    }

    public void setImageName(String newName) {
        imgName = newName;
    }

    public Image getImage() {
        return image;
    }

}
