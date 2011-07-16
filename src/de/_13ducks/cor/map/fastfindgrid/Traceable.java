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
package de._13ducks.cor.map.fastfindgrid;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Unit;

/**
 * Objekte, die im Schnellfinderaster (fastfindgrid) eingetragen werden, müssen dieses Interface implementieren.
 * @author michael
 */
public interface Traceable {

    /**
     * Gibt die Zelle des Objekts zurück.
     * @note: Die Zelle eines Objekts kann bei der Erstellung per FastFindGrid.addObject() berechnet werden.
     *        Wenn das Objekt sich bewegt, muss die Zelle per FastFindGrid.getNewCell() aktualisiert werden.
     * 
     * @return - die Zelle, in der das Objekt gerade steht
     */
    public abstract Cell getCell();

    /**
     * Setzt die Zelle, in der die EInheit steht
     */
    public abstract void setCell(Cell newCell);

    /**
     * Gibt die Position des Objekts zurück
     * @return - die Position des Objekts
     */
    public abstract FloatingPointPosition getPosition();

    /**
     * Gibt die Einheit zurück, die diesem Traceable zugeordnet ist
     * @return - die zugehörige Einheit
     */
    public abstract Unit getUnit();

    void mouseHovered();
}
