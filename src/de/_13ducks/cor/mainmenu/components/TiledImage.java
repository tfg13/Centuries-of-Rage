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

import de._13ducks.cor.graphics.Renderer;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.mainmenu.MainMenu;
import org.lwjgl.util.Dimension;

/**
 * Ein gekacheltes Bild
 * Die Fläche der Komponente wird mit dem geladenen Bild im Kachelmuster gefüllt
 *
 * @author michael
 */
public class TiledImage extends Component {

    /**
     * Pfad des zu ladenden Bilds
     */
    private String imagePath;

    /**
     * Konstruktor
     *
     * @param mainMenuReference - Hauptmenü-Referenz
     * @param x
     * @param y
     * @param width
     * @param height
     * @param imagepath         - Pfad des Bildes
     */
    public TiledImage(MainMenu mainMenuReference, double x, double y, double width, double height, String imagepath) {
        super(mainMenuReference, x, y, width, height);

        imagePath = imagepath;
    }

    @Override
    public void render(Graphics g) {
        Renderer.fillRectTiled(g, imagePath, getX1(), getY1(), getWidth(), getHeight(), 0, 0);
    }
}
