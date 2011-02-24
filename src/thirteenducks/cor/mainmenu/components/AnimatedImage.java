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

import org.newdawn.slick.BigImage;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import thirteenducks.cor.mainmenu.MainMenu;

/**
 * Animiertes Bild
 * Zeichnet ein Bild und verschiebt es langsam nach links.
 * Das Bild sollte länger als die Auflösung sein und die gleiche Höhe haben.
 *
 * @note: diese Klasse wird warscheinlich abgeschafft oder grob umstrukturiert,
 * da nicht alle java-Plattformen riesige Bilder unterstützen
 *
 * @author michael
 */
public class AnimatedImage extends Component {

    /**
     * Pfad des Bildes
     */
    private String imagePath;
    /**
     * Das Bild, das die Animation enthält
     */
    private Image image;
    /**
     * Gibt die Verschiebung des Bilds an
     */
    private int frame;
    /**
     * Zähler für frameverschiebung
     */
    private long time;

    /**
     * Konstruktor
     * 
     * @param mainMenuReference     Referenz auf MainMenu
     */
    public AnimatedImage(MainMenu mainMenuReference, String imagepath) {
        super(mainMenuReference, 0, 0, 100, 100);
        imagePath = imagepath;
        frame = 0;
        time = System.currentTimeMillis();
    }

    @Override
    public void init(GameContainer c) {
        try {
            image = new BigImage(imagePath);
        } catch (SlickException ex) {
            ex.printStackTrace();
            System.out.print("MainMenu: Error loading image");
        }

    }

    @Override
    public void render(Graphics g) {

        g.drawImage(image, 0, 0, 1024, 768, frame, 0, frame + 1024, 768);

        if ((frame + 1024) > image.getWidth()) {
            g.drawImage(image, image.getWidth() - frame, 0, 1024, 768, 0, 0, 1024 - (image.getWidth() - frame), 768);

            if (frame > image.getWidth()) {
                frame = 0;
            }
        }

        if ((System.currentTimeMillis() - time) > 40) {
            frame++;
            time = System.currentTimeMillis();
        }
    }
}
