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

import org.newdawn.slick.Graphics;

/**
 * Objekte die Renderable sind, können direkt von der Grafikengine gezeichnet
 * werden.
 * In der Regel bestimmen sie ihre Position selbst und werden direkt
 * auf den Monitor gezeichnet.
 */
public interface Renderable {

    /**
     * Rendert diese Element.
     * Entweder direkt auf den Bildschirm, ansonsten auf den Grafikkontext g des
     * Bildschirms.
     *
     * Renderables zeichen sich nur im Game-Bereich, nicht im Hud!
     * Bestimmte vorgänge müssen zeitnah berechnet und dann gezeichnet werden,
     * damit z.B. Bewegungen flüssig dargestellt werden können.
     * Daher können auch bestimmte Berechnugen im Rahmen der renderSprite() durchgeführt werden.
     *
     * @param g der Graphics - Kontext des Bildschirms (Fensters)
     * @param fullResX - die volle x-auflösung
     * @param fullResY - die volle y-auflösung
     * @param positionX - derzeitige Scoll-Position in Feldern der Linken Oberen Ecke
     * @param positionY - derzeitige Scoll-Position in Feldern der Linken Oberen Ecke
     * @param viewX - Die Anzahl sichtbarer Felder
     * @param viewY - Die Anzahl sichtbarer Felder
     * @param hudX - Die X - Koordinate des Huds (beschränkt den Spiel-Bereich.)
     */
    public void renderSprite(Graphics g, int fullResX, int fullResY, int positionX, int positionY, int viewX, int viewY, int hudX);

}
