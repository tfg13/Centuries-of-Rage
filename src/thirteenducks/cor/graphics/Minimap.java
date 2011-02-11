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
package thirteenducks.cor.graphics;

import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import thirteenducks.cor.map.CoRMapElement;

/**
 * Die Minimap.
 */
public class Minimap extends Overlay {

    /**
     * Die Map ist derzeit oben rechts.
     */
    public static final int EDGE_TOP_RIGHT = 0;
    /**
     * Die Map ist derzeit oben links.
     */
    public static final int EDGE_TOP_LEFT = 1;
    /**
     * Die Map ist derzeit unten rechts.
     */
    public static final int EDGE_BOTTOM_RIGHT = 2;
    /**
     * Die Map ist derzeit unten links.
     */
    public static final int EDGE_BOTTOM_LEFT = 3;
    /**
     * Die Default-Größe der Minimap.
     * Der Spieler kann die Größe der Minimap nachträglich ändern.
     * Dieser Wert wird mit der X-Auflösung multipliziert.
     */
    public static final double DEFAULT_SIZEFACTOR_X = 0.25;
    /**
     * Die Default-Größe der Minimap.
     * Der Spieler kann die Größe der Minimap nachträglich ändern.
     * Dieser Wert wird mit der Y-Auflösung multipliziert.
     */
    public static final double DEFAULT_SIZEFACTOR_Y = 0.25;
    /**
     * Das Bild der Minimap
     */
    private Image map;
    /**
     * Die aktuelle Größe der Minimap in X-Richtung
     */
    private int sizeX;
    /**
     * Die aktuelle Größe der Minimap in Y-Richtung
     */
    private int sizeY;
    /**
     * Die derzeitige Ecke
     */
    private int edge = 0;

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
        int dx = 0;
        int dy = 0;
        switch (edge) {
            case EDGE_TOP_RIGHT:
                dx = fullResX - sizeX;
                break;
            case EDGE_BOTTOM_LEFT:
                dy = fullResY - sizeY;
                break;
            case EDGE_BOTTOM_RIGHT:
                dx = fullResX - sizeX;
                dy = fullResY - sizeY;
                break;
        }
        map.draw(dx, dy, sizeX, sizeY);
    }

    private void createMinimap(CoRMapElement[][] visMap, Map<String, GraphicsImage> imgMap) {
        try {
            // Erstellt einen neue Basis-Minimap
            map = new Image(visMap.length * 2, visMap[0].length * 2);
            Graphics tempGra = map.getGraphics();
            // Skalierungsfaktor berechnen
            for (int x = 0; x < visMap.length; x++) {
                for (int y = 0; y < visMap[0].length; y++) {
                    if ((x + y) % 2 == 1) {
                        continue;
                    } else {
                        try {
                            GraphicsImage tex = imgMap.get(visMap[x][y].getProperty("ground_tex"));
                            if (tex != null) {
                                Color pcol = tex.getImage().getColor(20, 20);
                                tempGra.setColor(pcol);
                                tempGra.fillRect(x * 2, y * 2, 4, 4);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
            tempGra.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
