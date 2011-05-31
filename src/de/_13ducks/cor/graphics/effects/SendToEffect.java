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
package de._13ducks.cor.graphics.effects;

import de._13ducks.cor.graphics.GraphicsContent;
import de._13ducks.cor.graphics.GraphicsImage;
import java.util.Map;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

/**
 * Der Effekt, der angezeigt wird, wenn mit selektierten Einheiten auf den Boden rechtsgeklickt wird.
 */
public class SendToEffect extends SkyEffect {
    
    private static final int DURATION = 500;
    private static final int OFFSET_X = -16;
    private static final int OFFSET_Y = -32;
    
    /**
     * Die x-Koordinate dieses Effekts
     */
    private double x;
    /**
     * Die y-Koordinate dieses Effekts
     */
    private double y;
    /**
     * Zu diesem Zeitpunkt ist der Effekt zu Ende
     */
    private long finish;
    
    public SendToEffect(double x, double y) {
        this.x = x * GraphicsContent.FIELD_HALF_X + GraphicsContent.OFFSET_PRECISE_X;
        this.y = y * GraphicsContent.FIELD_HALF_Y + GraphicsContent.OFFSET_PRECISE_Y;
        finish = System.currentTimeMillis() + DURATION;
    }

    @Override
    public void renderSkyEffect(Graphics g, double scrollX, double scrollY, Map<String, GraphicsImage> imgMap) {
        Image img = imgMap.get("img/game/send.png").getImage();
        img.draw((float) (x - scrollX) + OFFSET_X, (float) (y - scrollY) + OFFSET_Y);
    }

    @Override
    public boolean isVisible(double scrollX, double scrollY, int resX, int resY) {
        return scrollX <= x && x <= (scrollX + resX) && scrollY <= y && y <= (scrollY + resY);
    }

    @Override
    public boolean isDone() {
        return System.currentTimeMillis() >= finish;
    }
    
}
