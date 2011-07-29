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

import org.newdawn.slick.Graphics;

/**
 * Ein Wrapper für Overlays, die sich am Rand des Bildes befinden,
 * und auf Kommando in das Bild reinsliden. Und natürlich wieder raus.
 * Die Rendermethode des ursprünglichen Overlays muss NICHT verändert werden.
 * Sie sollte das Overlay einfach genau dorthin rendern, wo es voll eingefahren ist.
 * Dieser Wrapper verschiebt alles, was das Overlay rendert in eine beliebige Richtung.
 * 
 * Derzeit verschiebt dieses Implementierung NICHT die Klick-Zone!!!
 */
public class SlideInOverlay implements Overlay {

    private Overlay overlay;
    private int moveOutX;
    private int moveOutY;
    private int millis;
    private boolean out = false;
    private long moveStart;

    /**
     * Erzeugt ein neues SlideInOverlay für das gegebenen Overlay.
     * Das Overlay muss vollständig eingeblendet sein, wenn die Verschiebung gegen null geht.
     * Anfangs ist das Overlay vollständig einfahren (volle Verschiebung)
     * 
     * Dieses SlideInOverlay gibt dem übergebenen Overlay automatisch eine Referenz auf sich selbst,
     * falls diese SlideInController implementiert.
     * 
     * @param overlay - Das Overlay, das verschoben werden soll.
     * @param moveOutX - X-Verschiebung, mit der das Overlay ganz draußen ist.
     * @param moveOutY - Y-Verschiebung, mit der dsa Overlay ganz draußen ist.
     * @param millis  - Zeit für ein SlideIn / SlideOut
     */
    public SlideInOverlay(Overlay overlay, int moveOutX, int moveOutY, int millis) {
        this.overlay = overlay;
        this.moveOutX = moveOutX;
        this.moveOutY = moveOutY;
        this.millis = millis;
        
        if (overlay instanceof SlideInController) {
            ((SlideInController) overlay).addSlideIn(this);
        }
    }

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
        int movX = 0;
        int movY = 0;
        if (moveStart + millis > System.currentTimeMillis()) {
            double t = 1.0 * (System.currentTimeMillis() - moveStart) / millis;
            double progress = ((101 / (1 + Math.pow(Math.E, -.1 * 101 * t) * (101 / 1 - 1))) - 1) / 100.0;
            if (out) {
                progress = 1 - progress;
            }
            movX = (int) (moveOutX * progress);
            movY = (int) (moveOutY * progress);
        } else {
            if (!out) {
                movX = moveOutX;
                movY = moveOutY;
            }
        }
        
        g.pushTransform();
        g.translate(movX, movY);
        overlay.renderOverlay(g, fullResX, fullResY);
        g.popTransform();
    }

    public boolean isOut() {
        return out;
    }

    public void slideOut() {
        moveStart = System.currentTimeMillis();
        out = true;
    }

    public void slideIn() {
        moveStart = System.currentTimeMillis();
        out = false;
    }
}
