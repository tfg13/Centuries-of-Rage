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
import de._13ducks.cor.game.Moveable;
import de._13ducks.cor.game.server.ServerPathfinder;
import java.util.ArrayList;
import java.util.List;

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
    private ArrayList<GroupMember> myMovers;
    /**
     * Die aktuelle MovementMap
     */
    private MovementMap moveMap;
    
    public GroupManager(MovementMap moveMap) {
        myMovers = new ArrayList<GroupMember>();
        this.moveMap = moveMap;
    }

    /**
     * Löscht eine Einheit aus der Gruppe heraus.
     * Wenn sie gar nicht drin war, passiert nichts.
     * @param mover die zu löschende Einheit
     */
    public synchronized void remove(Moveable mover) {
        myMovers.remove(new GroupMember(mover));
    }

    /**
     * Fügt die Einheit zu dieser Gruppe hinzu, falls sie noch nicht drin ist.
     * @param mover
     */
    public synchronized void add(Moveable mover) {
        GroupMember tempmem = new GroupMember(mover);
        if (!myMovers.contains(tempmem)) {
            myMovers.add(tempmem);
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
        // Route planen
        Node targetNode = moveMap.nearestSectorNode(target);
        for (GroupMember member : myMovers) {
            Node startNode = moveMap.nearestSectorNode(member.getMover());
            List<Node> path = ServerPathfinder.findPath(startNode, targetNode);
            if (path != null) {
                // Weg setzen
                for (Node node : path) {
                    member.addWaypoint(node.toFPP());
                }
            }
            // Loslaufen lassen
            member.getMover().getLowLevelManager().setTargetVector(member.popWaypoint(), member.getMover().getSpeed());
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

    /**
     * Eine LowLevelManager hat sein Wegziel erreicht und will wissen, wie die Route weitergeht
     * Gibt false zurück wenns nicht weitergeht und liefert true zurück, wenns weiter geht und das
     * neue Ziel schon gesetzt wurde.
     * @return true, wenn neues Ziel gesetzt sonst false
     */
    public boolean reachedTarget(Moveable mover) {
        GroupMember member = memberForMover(mover);
        FloatingPointPosition nextPoint = member.popWaypoint();
        if (nextPoint != null) {
            mover.getLowLevelManager().setTargetVector(nextPoint);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Sucht den GroupMember zu einem Mover raus
     * @param mover
     * @return 
     */
    private GroupMember memberForMover(Moveable mover) {
        for (GroupMember member : myMovers) {
            if (member.getMover().equals(mover)) {
                return member;
            }
        }
        return null;
    }
}
