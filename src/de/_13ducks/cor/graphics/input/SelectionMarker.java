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

import de._13ducks.cor.game.Position;

/**
 * Repräsentiert Positionen, auf denen etwas selektierbar ist.
 * Wird verwendet, um die SelektionsMap des Clients aktuell zu halten.
 * Kann die SelektionsMap updaten.
 * Dabei wird dieser Marker je nach verwendetem Konstuktor seinen Besitzer:
 * - Nur Eintragen (für neue IGE's)
 * - Nur Löschen (zum Entfernen)
 * - Updated (beides) (für Positionsänderungen)
 * @author tfg
 */
public class SelectionMarker {

    Position[] oldPositions;
    Position[] newPositions;
    InteractableGameElement owner;

    /**
     * Erzeugt einen neuen Selektionsmarker mit den gegebenen Positionen.
     * Owner müss übergeben werde, von den andern beiden darf maximal eines null sein.
     * @param owner Das IGE, dessen Selektionspositionen geändert werden.
     * @param oldPos Die alten Positionen, (sofern existent)
     * @param newPos Die neuen Positionen, (sofern existent)
     */
    public SelectionMarker(InteractableGameElement owner, Position[] oldPos, Position[] newPos) {
        this.owner = owner;
        this.oldPositions = oldPos;
        this.newPositions = newPos;
    }

    /**
     * Updated die gegebene selectionMap.
     * Dazu werden alle alten Referenzen gelöscht - fall es welche gab.
     * Dann werden alle neuen Referenzen gesetzt - fall es welche gibt.
     * Diese Funktion kann also adden, löschen und updaten.
     * @param selectionMap
     */
    public void updateSelectionMap(SelectionMap selectionMap) {
        if (oldPositions != null) {
            for (Position pos : oldPositions) {
                selectionMap.removeIGE(pos.getX(), pos.getY(), owner);
            }
        }
        if (newPositions != null) {
            for (Position pos : newPositions) {
                selectionMap.addIGE(pos.getX(), pos.getY(), owner);
            }
        }
    }

}
