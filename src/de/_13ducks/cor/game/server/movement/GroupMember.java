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
import java.util.LinkedList;

/**
 * Ein Member einer Gruppe.
 */
public class GroupMember {
    
    private Moveable mover;
    private LinkedList<FloatingPointPosition> path;
    
    public GroupMember(Moveable mover) {
        this.mover = mover;
        path = new LinkedList<FloatingPointPosition>();
    }

    /**
     * @return the mover
     */
    public Moveable getMover() {
        return mover;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.mover != null ? this.mover.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof GroupMember) {
            GroupMember g = (GroupMember) o;
            return g.mover.equals(mover);
        }
        return false;
    }
    
    /**
     * Fügt einen neuen Wegpunkt für diese Einheit ein.
     * Der Wegpunkt wird an das ende des geplanten Weges gesetzt.
     * Der Wegpunkt wird nur eingefügt, wenn er nicht schon am Ende ist.
     * @param waypoint 
     */
    public void addWaypoint(FloatingPointPosition waypoint) {
        if (path.isEmpty() || !path.getLast().equals(waypoint)) {
            path.add(waypoint);
        }
    }
    
    /**
     * Löscht alle zukünftigen Wegpunkte.
     */
    public void clearWaypoints() {
        path.clear();
    }
    
    /**
     * Holt den nächsten Wegpunkt dieser Einheit.
     * Lösch ihn anschließend aus der Route.
     * @return 
     */
    public FloatingPointPosition popWaypoint() {
        return path.pollFirst();
    }
    
}
