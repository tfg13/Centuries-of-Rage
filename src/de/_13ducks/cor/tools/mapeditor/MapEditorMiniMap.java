/*
 *  Copyright 2008, 2009, 2010, 2011:
 *   Tobias Fleig (tfg@online.de),
 *   Michael Haas (mekhar@gmx.de),
 *   Johannes Kattinger (johanneskattinger@gmx.de)
 *  - All rights reserved -
 *
 *
 *  This file is part of Reign of Godwars.
 *
 *  Reign of Godwars is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Reign of Godwars is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Reign of Godwars.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de._13ducks.cor.tools.mapeditor;

import de._13ducks.cor.map.CoRMap;
import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import javax.swing.*;
import de._13ducks.cor.graphics.GraphicsComponent;
import de._13ducks.cor.graphics.CoRImage;

/**
 *
 * @author tfg
 */
public class MapEditorMiniMap extends JLabel {

    CoRMap map;
    Image baseMap;
    HashMap<String, CoRImage> imgMap;
    boolean viewOnly = false;
    GraphicsComponent content;

    @Override
    public void paintComponent(Graphics g) {
        if (map != null) {
            Graphics2D g2 = (Graphics2D) g;
            // baseMap neu bauen
            if (!viewOnly || baseMap == null) {
                baseMap = new BufferedImage(map.getMapSizeX() * 2, map.getMapSizeY() * 2, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g3 = (Graphics2D) baseMap.getGraphics();
                for (int x = 0; x < map.getMapSizeX(); x++) {
                    for (int y = 0; y < map.getMapSizeX(); y++) {
                        if ((x + y) % 2 == 1) {
                            continue;
                        } else {
                            try {
                                CoRImage tex = imgMap.get(map.getVisMap()[x][y].getGround_tex());
                                if (tex != null) {
                                    int color = tex.getImage().getRGB(20, 20);
                                    g3.setColor(new java.awt.Color(color));
                                    g3.fillRect(x * 2, y * 2, 4, 4);
                                }
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            }

            // Jetzt alles auf die Minimap-Größe ausbauen
            g2.drawImage(baseMap, 0, 0, this.getWidth(), this.getHeight(), 0, 0, baseMap.getWidth(this), baseMap.getHeight(this), this);

            // Ansicht
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect((int) (1.0 * content.positionX / content.sizeX * this.getWidth()), (int) (1.0 * content.positionY / content.sizeY * this.getHeight()), (int) (1.0 * (content.viewX) / content.sizeX * this.getWidth()), (int) (1.0 * content.viewY / content.sizeY * this.getHeight()));
        }
    }

    public void refreshView() {
        viewOnly = true;
        this.paintComponent(this.getGraphics());
        viewOnly = false;
    }
}
