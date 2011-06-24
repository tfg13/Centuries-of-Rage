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

import org.newdawn.slick.Graphics;

/**
 * Ein Effekt, der über fast alles andere drüber gelegt wird.
 */
public abstract class SkyEffect {
    
    /**
     * Zeichnet den SkyEffect. Wird nur aufgerufen, wenn der Effekt auch sichtbar ist.
     * @param g der Grafikkontext. Bilder können auch direkt gezeichnet werden.
     * @param scrollX Die X-Verschiebung des derzeitigen Bildschirmausschnittes in Fließkommakoordinaten
     * @param scrollY Die Y-Verschiebung des derzeitigen Bildschrimausschnittes in Fließkommakoordinaten
     * @param imgMap Map mit allen verfügbaren Bildern
     */
    public abstract void renderSkyEffect(Graphics g, double scrollX, double scrollY);
    
    /**
     * Findet heraus, ob der Effekt derzeit in Sichtweite ist, also gezeichnet werden soll.
     */
    public abstract boolean isVisible(double scrollX, double scrollY, int resX, int resY);
    
    /**
     * Wird vor jedem Rendern abgefragt, hiermit kann ein Effekt signalisieren, dass er fertig ist, also gelöscht werden kann.
     * Gibt der Effekt hier true zurück wird er nichtmehr aufgerufen und sofort gelöscht.
     * @return true, wenn Effekt zu Ende, sonst false.
     */
    public abstract boolean isDone();
}
