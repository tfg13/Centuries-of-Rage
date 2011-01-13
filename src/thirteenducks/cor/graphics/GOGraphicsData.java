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

import java.io.*;

public class GOGraphicsData implements Serializable, Cloneable {

    /**
     * Zeitpunkt, bei dem mit draufhauen begonnen wird (automatisch verwaltet vom Kampfsystem)
     */
    private long atkStart = 0;
    /**
     * Läuft gerade eine Angriffsanimation? (automatisch verwaltet vom Kampfsystem)
     */
    private boolean atkAnim = false;
    /**
     * Abstand vom tatsächlichen Zeichenursprung der großen Textur zum originalen Ursprung des Zuordnungsfeldes.
     * Angabe in Feldern, muss durch 2 teilbar sein.
     * X-Richtung (nach oben)
     */
    private int offsetX = 0;
    /**
     * Abstand vom tatsächlichen Zeichenursprung der großen Textur zum originalen Ursprung des Zuordnungsfeldes.
     * Angabe in Feldern, muss durch 2 teilbar sein.
     * Y-Richtung (nach links)
     */
    private int offsetY = 0;

    public String defaultTexture = null;
    public String hudTexture = null;

    public String getTexture() {
        return defaultTexture;
    }

    public void setTexture(String newTex) {
        defaultTexture = newTex;
    }

    @Override
    public GOGraphicsData clone() {
        try {
            return (GOGraphicsData) super.clone();
        } catch (CloneNotSupportedException ex) { // Passiert im Läbe net..
            return null;
        }
    }
}
