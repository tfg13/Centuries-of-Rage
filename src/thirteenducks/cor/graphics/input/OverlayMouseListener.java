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

package thirteenducks.cor.graphics.input;

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
     */
    private int catch1X;
    /**
     * Die Y-Koordinate der oberen, linken Ecke des catch-Bereichs
     */
    private int catch1Y;
    /**
     * Die X-Koordinate der unteren, rechten Ecke des catch-Bereichs
     */
    private int catch2X;
    /**
     * Die Y-Koordinate der unteren, rechten Ecke des catch-Bereichs
     */
    private int catch2Y;

    /**
     * Die X-Koordinate der oberen, linken Ecke des catch-Bereichs
     * @return the catch1X
     */
    public int getCatch1X() {
        return catch1X;
    }

    /**
     * Die Y-Koordinate der oberen, linken Ecke des catch-Bereichs
     * @return the catch1Y
     */
    public int getCatch1Y() {
        return catch1Y;
    }

    /**
     * Die X-Koordinate der unteren, rechten Ecke des catch-Bereichs
     * @return the catch2X
     */
    public int getCatch2X() {
        return catch2X;
    }

    /**
     * Die Y-Koordinate der unteren, rechten Ecke des catch-Bereichs
     * @return the catch2Y
     */
    public int getCatch2Y() {
        return catch2Y;
    }



}
