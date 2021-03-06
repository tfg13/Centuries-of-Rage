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

package de._13ducks.cor.graphics;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.game.Hideable;
import de._13ducks.cor.game.Pauseable;
import de._13ducks.cor.game.Position;

/**
 * Ein Sprite ist ein zum Spiel gehörendes Objekt.
 * Die Grafikengine zeichnet Sprites automatisch, sofern sie in Sichtweite sind etc.
 */
public interface Sprite extends Comparable<Sprite>, Pauseable, Hideable {

    /**
     * Zeichnet dieses Sprite nach x, y auf den Bildschirm.
     * Verwendet häufig direktes Render (ohne g)
     * x und y müssen die Zeichenkoordinaten der Zuordnungsposition sein.
     * @param g Der Grafikkontext auf den gezeichnet werden kann. Man kann auch direkt auf den Screen zeichnen
     * @param x Die x-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     * @param y Die y-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     * @param scrollX die Verschiebung des derzeitigen Bildausschnittes in Fließkomma-Koordinaten
     * @param scrollY die Verschiebung des derzeitigen Bildausschnittes in Fließkomma-Koordinaten
     * @param imgMap Die Map mit allen verfügbaren Texturen
     * @Color spriteColor die (Team/Spieler) Farbe, mit der sich dieses Sprite zeichnen soll, falls benötigt.
     */
    public void renderSprite(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor);

    /**
     * Zeichnet die Bodeneffekte dieses Sprites.
     * Bodeneffekte werden vor den eigentlichen Texturen gezeichnet und sind vor allem für Markierungen gedacht.
     * x und y müssen die Zeichenkoordinaten des Zuordnungsfeldes sein.
     * @param g Der Grafikkontext auf den gezeichnet werden kann. Man kann auch direkt auf den Screen zeichnen.
     * @param x Die x-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     * @param y Die y-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     * @param scrollX die Verschiebung des derzeitigen Bildausschnittes in Fließkomma-Koordinaten
     * @param scrollY die Verschiebung des derzeitigen Bildausschnittes in Fließkomma-Koordinaten
     * @param imgMap Die Map mit allen verfügbaren Texturen
     * @param spriteColor die (Team/Spieler) Farbe, mit der sich dieses Sprite zeichnen soll, falls benötigt.
     */
    public void renderGroundEffect(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor);
    
    /**
     * Zeichnet die Skyeffects dieses Sprites.
     * Skyeffects werden nach dem eigentlichen Zeichnen verarbeitet.
     * x und y müssen die Zeichenkoordinaten des Zuordnungsfeldes sein.
     * @param g Der Grafikkontext auf den gezeichnet werden kann. Man kann auch direkt auf den Screen zeichnen.
     * @param x Die x-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     * @param y Die y-Zeichenkoordinate des Zuordnungsfeldes auf dem Bildschirm
     * @param scrollX die Verschiebung des derzeitigen Bildausschnittes in Fließkomma-Koordinaten
     * @param scrollY die Verschiebung des derzeitigen Bildausschnittes in Fließkomma-Koordinaten
     * @param imgMap Die Map mit allen verfügbaren Texturen
     * @param spriteColor die (Team/Spieler) Farbe, mit der sich dieses Sprite zeichnen soll, falls benötigt.
     */
    public void renderSkyEffect(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor);

    /**
     * Liefert die Position, mit derer die scroll-Rechnung durchgeführt werden kann.
     * Das Grafiksystem wird diese Position verwenden, um die in der renderSprite-Methode übergebenen Koordinaten zu berechnen.
     * In der Regel ist dies die Zuordnungsposition.
     * @return
     */
    public Position getMainPositionForRenderOrigin();

    /**
     * Liefert alle Position zurück, auf denen sich tatsächlich etwas von diesem Objekt befindet.
     * Wird z.B. für den FoW-Abgleich verwendet.
     * Liefert keine bestimmte Sortierung der Felder
     * @return alle Positionen, auf denen sich etwas sichbares (wichtiges) diese Objekts befindet.
     */
    public Position[] getVisisbilityPositions();

    /**
     * Liefert eine Position, anhand derer dieses Sprite mit anderen verglichen werden kann.
     * Wird benötigt, um die Perspektiven-Reihenfolge ausrechnen zu können.
     * Bei Quardatischen Objekten sollte das in der Regel die Zuordnungsposition sein.
     * @return eine Position, anhand derer dieses Sprite mit anderen verglichen werden kann.
     */
    public Position getSortPosition();

    /**
     * Sprites können bestimmte Dinge in ihrer eigenen Farbe rendern.
     * Diese wird vom Grafiksystem an die renderMethoden mitgeliefert.
     * Hierzu fragt das Grafiksystem die gewünschte ColorID ab.
     * In der Regel ist das die playerId.
     * Sprites, die keine Farbe haben/verwenden, sollten 0 zurückgeben.
     * @return
     */
    public int getColorId();

    /**
     *
     * @param g
     * @param x
     * @param y
     * @param spriteColor
     */
    public void renderMinimapMarker(Graphics g, int x, int y, Color spriteColor);
}