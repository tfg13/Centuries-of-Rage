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


package thirteenducks.cor.graphics.input;

/**
 * Ein Spezieller Input-Modi. Wird z.B. für das Gebäude-Bausystem eingesetzt
 *
 * @author tfg
 */
public abstract class CoRInputMode {

    public boolean useMouseMotion;

    public CoRInputMode(boolean useMoveListener) {
        useMouseMotion = useMoveListener;
    }

    public abstract void mouseMoved(int oldx, int oldy, int newx, int newy);    // Wird bei jeder Mausbewegung aufgerufen, falls useMoveListener im Konstruktor true war
    public abstract void mouseKlicked(int button, int x, int y, int clickCount);  // Wird aufgerufen, wenn geklickt wurde
    public abstract void startMode();                 // Startet diesen Selektionsmodi
    public abstract void endMode();                   // Stoppt diesen Modi
}
