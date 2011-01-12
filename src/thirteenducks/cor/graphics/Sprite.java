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

import org.newdawn.slick.Graphics;
import thirteenducks.cor.game.Position;

/**
 * Ein Sprite ist ein zum Spiel gehörendes Objekt.
 * Die Grafikengine zeichnet Sprites automatisch, sofern sie in Sichtweite sind etc.
 */
public interface Sprite {

    /**
     * Zeichnet dieses Sprite nach x, y auf den Bildschirm.
     * Verwendet häufig direktes Render (ohne g)
     * x und y müssen die Zeichenkoordinaten der Zuordnungsposition sein.
     * @param g Der Grafikkontext auf den gezeichnet werden kann. Man kann auch direkt auf den Screen zeichnen
     * @param x Die x-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     * @param y Die y-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     */
    public void renderSprite(Graphics g, int x, int y);

    /**
     * Liefert alle Position zurück, auf denen sich tatsächlich etwas von diesem Objekt befindet.
     * Wird z.B. für den FoW-Abgleich verwendet.
     * Liefert keine bestimmte Sortierung der Felder
     * @return alle Positionen, auf denen sich etwas sichbares (wichtiges) diese Objekts befindet.
     */
    public Position[] getVisisbilityPositions();
    /**
     * Findet heraus, ob dieses Sprite auch im vollen 100% dunklen FoW gezeichnet werden soll.
     * @return true, wenn im 100% FoW zeichnen
     */
    public boolean renderInFullFog();
    /**
     * Findet heraus, ob dieses Sprite auch im halben 50% grauen FoW gezeichnet werden soll.
     * @return true, wenn im 50% FoW zeichnen
     */
    public boolean renderInHalfFog();
}