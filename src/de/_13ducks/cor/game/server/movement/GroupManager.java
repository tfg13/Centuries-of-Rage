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

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Unit;
import java.util.ArrayList;

/**
 * MidLevel-Movemanagement
 * 
 * Verwaltet die Positionierung der einzelnen Einheiten innerhalb einer kleineren Gruppe.
 * Diese entscheiden, wie sich Einheitengruppen vor dem Gegner aufstellen,
 * wie sie sich während dem Kampf verhalten, und ob sie davor und danach
 * eine Formation einnehmen.
 * Für jede Gruppe existiere ein GroupManager.
 * Dieser verwaltet die Pfade und Ziele seiner Einheiten.
 * Die tatsächliche Bewegung unterliegt der exklusiven Kontrolle des LowLevelManagers der
 * einzelnen Einheiten. Der GroupManager kann nur eine Richtung und ein Ziel vorgeben.
 */
public class GroupManager {

    /**
     * Alle Einheiten, die zur Zeit in dieser Gruppe sind.
     */
    private ArrayList<Unit> myUnits;
    
    public GroupManager() {
        myUnits = new ArrayList<Unit>();
    }

    /**
     * Löscht eine Einheit aus der Gruppe heraus.
     * Wenn sie gar nicht drin war, passiert nichts.
     * @param unit die zu löschende Einheit
     */
    public synchronized void remove(Unit unit) {
        myUnits.remove(unit);
    }

    /**
     * Fügt die Einheit zu dieser Gruppe hinzu, falls sie noch nicht drin ist.
     * @param unit
     */
    public synchronized void add(Unit unit) {
        if (!myUnits.contains(unit)) {
            myUnits.add(unit);
        }
    }

    /**
     * Lässt die Gruppe an dieses Ziel laufen.
     * Laufen bedeutet aggressives Vorrücken.
     * Alle Einheiten laufen mit der gleichen Geschwindigkeit.
     * runTo aufrufen, für nicht-aggressives Vorrücken, jeder so schnell wie er kann.
     * @param target
     */
    public synchronized void goTo(FloatingPointPosition target) {
        // TODO: Ziele, Formation verwalten!
        for (Unit unit : myUnits) {
            unit.getLowLevelManager().setTargetVector(target, unit.getSpeed());
        }
    }

    /**
     * Lässt die Gruppe an dieses Ziel rennen.
     * Rennen bedeutet flüchten, alle Feinde ignorieren, das Ziel um jeden Preis erreichen.
     * Jede Einheit läuft mit ihrer individuellen Maximalgeschwindigkeit
     * goTo aufrufen, für geordnetes, aggressives Vorrücken
     * @param target
     */
    public synchronized void runTo(FloatingPointPosition target) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}