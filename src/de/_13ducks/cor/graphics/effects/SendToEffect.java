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
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

/**
 * Der Effekt, der angezeigt wird, wenn mit selektierten Einheiten auf den Boden rechtsgeklickt wird.
 */
public class SendToEffect extends SkyEffect {

    public static final int MODE_GOTO = 1;
    public static final int MODE_RUNTO = 2;
    public static final int MODE_ATK = 3;
    public static final int MODE_FOCUSATK = 4;
    /**
     * So lange dauert der Effekt der Pfeilspitze
     */
    private static final int DURATION = 300;
    /**
     * Wieviel länger der Dot zu sehen ist
     */
    private static final int DOT_MULT = 3;
    /**
     * Zeigt auf Mitte der Pfeilspitze
     */
    private static final int OFFSET_X = -16;
    /**
     * Zeigt auf Mitte der Pfeilspitze
     */
    private static final int OFFSET_Y = -32;
    /**
     * Das Bild enthält 4 Teilbilder (verschiedene Farben). X-Zeiger
     */
    private final int imgX;
    /**
     * Die Farbe des Kreises
     */
    private final Color color;
    /**
     * Die x-Koordinate dieses Effekts
     */
    private double x;
    /**
     * Die y-Koordinate dieses Effekts
     */
    private double y;
    /**
     * Start-Zeitpunkt
     */
    private long start;
    /**
     * Zu diesem Zeitpunkt ist der Effekt zu Ende
     */
    private long finish;

    public SendToEffect(double x, double y, int mode) {
        switch (mode) {
            case MODE_RUNTO:
                imgX = 32;
                color = new Color(62, 132, 187);
                break;
            case MODE_ATK:
                color = new Color(187, 62, 62);
                imgX = 64;
                break;
            case MODE_FOCUSATK:
                imgX = 96;
                color = new Color(187, 62, 180);
                break;
            default: // MODE_GOTO
                imgX = 0;
                color = new Color(0, 175, 1);
                break;
        }
        this.x = x * GraphicsContent.FIELD_HALF_X + GraphicsContent.OFFSET_PRECISE_X;
        this.y = y * GraphicsContent.FIELD_HALF_Y + GraphicsContent.OFFSET_PRECISE_Y;
        finish = System.currentTimeMillis() + (DURATION * DOT_MULT);
        start = System.currentTimeMillis();
    }

    @Override
    public void renderSkyEffect(Graphics g, double scrollX, double scrollY, Map<String, GraphicsImage> imgMap) {
        g.setColor(color);
        g.setLineWidth(2);
        g.fillOval((float) (x - scrollX) - 5, (float) (y - scrollY) - 3, 10, 6);

        // Pfeil&Kreis sind nur kurz sichtbar
        if (System.currentTimeMillis() < (start + DURATION)) {
            Image img = imgMap.get("img/game/send.png").getImage();
            int animPixel = (int) (((1.0 * System.currentTimeMillis() - start) / DURATION) * 32);
            float rX = (float) (x - scrollX) + OFFSET_X;
            float rY = (float) (y - scrollY) + OFFSET_Y;
            g.drawOval((float) (x - scrollX) - 5 - ((32 - animPixel) / 1.5f), (float) (y - scrollY) - 3 - ((32 - animPixel) / 2), 10 + ((32 - animPixel) / 0.75f), 6 + ((32 - animPixel)));
            img.draw(rX + (animPixel / 2), rY + animPixel, rX + (img.getWidth() / 4) - (animPixel / 2), rY + img.getHeight(), imgX, 0, imgX + (img.getWidth() / 4), 32 - animPixel);
        }
    }

    @Override
    public boolean isVisible(double scrollX, double scrollY, int resX, int resY) {
        return scrollX <= x && x <= (scrollX + resX) && scrollY <= y && y <= (scrollY + resY);
    }

    @Override
    public boolean isDone() {
        return System.currentTimeMillis() >= finish;
    }
    
    /**
     * Stoppt den Effekt sofort
     */
    public void kill() {
        finish = 0;
    }
}
