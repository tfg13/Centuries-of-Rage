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
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.Moveable;
import de._13ducks.cor.game.SimplePosition;
import java.util.ArrayList;

/**
 * Speichert die Position beliebig vieler Einheiten, und kann gefragt werden,
 * ob sie sich geändert hat.
 * Dient zur Abschätzung, ob/wann sich teure, neue Umleitungssuchen lohnen.
 * 
 * @author Tulius <tobifleig@gmail.com>
 */
public class ObstaclePattern {
   
    
    /**
     * Die derzeitige getrackten Einheiten
     */
    private ArrayList<FrozenUnit> trackedUnits;

    public ObstaclePattern() {
        trackedUnits = new ArrayList<FrozenUnit>();
    }
    
    /**
     * Fügt eine neues Moveable hinzu und speichert sofort seine Position.
     * @param mover der neue mover
     */
    public void freezeUnit(Moveable mover) {
        trackedUnits.add(new FrozenUnit(mover));
    }
    
    /**
     * Findet heraus, ob sich die eingefrohrenen Einheiten bewegt haben.
     * (eine einzelne bewegte reicht für true)
     * @return true, wenn mindestens eine bewegt
     */
    public boolean positionsChanged() {
        for (FrozenUnit u : trackedUnits) {
            if (!u.checkFrozen()) {
                return true;
            }
        }
        
        return false;
    }
    
    
    
    private class FrozenUnit {
        
        /**
         * Die eingefrohrene (gespeicherte) Einheitenposition
         */
        private final SimplePosition frozenPos;
        /**
         * Der eingefrohrene Mover
         */
        private final Moveable frozenMover;
        
        /**
         * Friert eine Einheitenposition sofort ein
         * @param mover 
         */
        private FrozenUnit(Moveable mover) {
            frozenPos = mover.getPrecisePosition().toVector(); // Klont die Position
            frozenMover = mover;
        }
        
        /**
         * Überprüft, ob Position noch stimmt.
         * @return true, wenn noch richtig
         */
        private boolean checkFrozen() {
            return frozenPos.equals(frozenMover.getPrecisePosition());
        }
    }
    
}
