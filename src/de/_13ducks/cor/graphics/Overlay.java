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
 * Eine Grafik-Komponente, die über den GAME-Bereich gezeichnet wird.
 * Solche Overlays sind:
 * Chat
 * Teamwahl
 * Ingame-Menü
 * Statistik
 * etc.
 *
 * Das HUD ist KEIN Overlay. Overlays überlagern nur den Game-Bereich
 *
 * Der Game-Bereich wird unter dem Overlay normal weitergezeichnet, was Transparenzeffekte ermöglicht.
 * Jedes Overlay hat selbst zu entscheiden, wann und was es von sich zeichnet.
 *
 * Es kann sich dafür beim Inputmodul für bestimmte Keystrokes Events bestellen, und sich dann z.B. einblenden. (kommt noch)
 * Ein normales Overlay reagiert NICHT auf Input irgendeiner Art,
 * es blendet sich automatisch aus oder benutzt nur wenige Events des Input-Moduls
 *
 * Für komplexe Inputschema (wie z.B. der Chat) kann der InputMode überschrieben werden (kommt noch)
 *
 * Ein Overlay bezieht seine Informationen inner, sofern das erforderlich ist.
 *
 *
 * @author tfg
 */
public interface Overlay {

    /**
     * Zeichnet das Overlay in den Grafikkontext g.
     * Achtung: Verwendet teilweise auch direktes Zeichnen auf den Bildschirm (!!!)
     *
     * @param g
     * @param fullResX
     * @param fullResY
     * @param hudX
     */
    public abstract void renderOverlay(Graphics g, int fullResX, int fullResY);

}
