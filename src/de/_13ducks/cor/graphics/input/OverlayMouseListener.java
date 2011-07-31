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
package de._13ducks.cor.graphics.input;

import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;

/**
 * Ein solcher Listener kann von Overlays beim Inputmodul registriert werden.
 * Dann leitet das Inputmodul alle Klicks (auch mouseMoves) die über diesem Overlay liegen an selbiges weiter.
 * Die Mausaktionen werden dann ausschließlich an das Overlay gesendet und nicht vom eigentlichen Spiel verarbeitet.
 *
 * Mithilfe dieses Listeners kann ein Overlay genau einen rechteckigen Bereich "abdecken".
 * Die vom Inputmodul übermittelten Koodinaten sind relativ zur linken oberen Ecke des abgedeckten Felds (also im Regelfall auch zum Overlay-Zeichenursprung)
 */
public abstract class OverlayMouseListener implements MouseListener {

    /**
     * Die X-Koordinate der oberen, linken Ecke des catch-Bereichs
     * @return the catch1X
     */
    public abstract int getCatch1X();

    /**
     * Die Y-Koordinate der oberen, linken Ecke des catch-Bereichs
     * @return the catch1Y
     */
    public abstract int getCatch1Y();
    /**
     * Die X-Koordinate der unteren, rechten Ecke des catch-Bereichs
     * @return the catch2X
     */
    public abstract int getCatch2X();

    /**
     * Die Y-Koordinate der unteren, rechten Ecke des catch-Bereichs
     * @return the catch2Y
     */
    public abstract int getCatch2Y();

    /**
     * Die Maus wurde auf dem Overlay bewegt.
     * @param x
     * @param y
     */
    public abstract void mouseMoved(int x, int y);

    /**
     * Die Maus wurde auf dem Overlay mit einer gedrückten Taste bewegt
     * @param x
     * @param y
     */
    public abstract void mouseDragged(int x, int y);
    /**
     * Die Maus wurde vom Overlay runter bewegt.
     */
    public abstract void mouseRemoved();


    @Override
    public boolean isAcceptingInput() {
        return true;
    }

    /*
     * Diese Methoden gehen das Overlay nichts an
     */

    @Override
    public void mouseClicked(int i, int i1, int i2, int i3) {
    }

    @Override
    public void setInput(Input input) {
    }

    @Override
    public void inputEnded() {
    }

    @Override
    public void inputStarted() {
    }

    @Override
    public void mouseMoved(int i, int i1, int i2, int i3) {
    }

    @Override
    public void mouseDragged(int i, int i1, int i2, int i3) {
    }
}
