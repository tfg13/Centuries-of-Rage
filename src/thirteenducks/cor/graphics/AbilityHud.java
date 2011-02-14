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

import java.util.List;
import java.util.Map;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.ability.Ability;

/**
 * Die Fähigkeiten-Anzeige des Huds
 */
public class AbilityHud extends Overlay {

    /**
     * Die größe der (quadratischen) Fähigkeiten-Icons
     */
    public static final int ICON_SIZE_XY = 40;

    /**
     * Das GO dessen Fähigkeiten derzeit angezeigt werden.
     */
    private GameObject object;
    /**
     * Die derzeitige X-Zeichenkoordinate
     */
    private int dx;
    /**
     * Die derzeitige Y-Zeichenkoordinate
     */
    private int dy;


    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY, Map<String,GraphicsImage> imgMap) {
        if (object != null) {
            List<Ability> abList = object.getAbilitys();
            for (int i = 0; i < abList.size(); i++) {
                Ability ab = abList.get(i);
                String tex = ab.symbols[0];
                if (tex != null) {
                    GraphicsImage img = imgMap.get(tex);
                    if (img != null) {
                        img.getImage().draw(dx + (i * ICON_SIZE_XY), dy, ICON_SIZE_XY, ICON_SIZE_XY);
                    }
                }
            }
        }
    }

    public void setActiveObject(GameObject obj) {
        object = obj;
    }

}
