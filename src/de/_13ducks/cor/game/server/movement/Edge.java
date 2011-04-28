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

/**
 * Eine Kante aus 2 Polygonen. Wird für Polygon-Adden verwendet
 */
public class Edge {

    private Node start, end;

    public Edge(Node start, Node end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge e = (Edge) o;
            // Achtung, beide Richtungen beachten!
            return (e.getStart().equals(this.getStart()) && e.getEnd().equals(this.getEnd())) || (e.getStart().equals(this.getEnd()) && e.getEnd().equals(this.getStart()));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.getStart() != null ? this.getStart().hashCode() : 0);
        hash = 79 * hash + (this.getEnd() != null ? this.getEnd().hashCode() : 0);
        return hash;
    }

    /**
     * @return the start
     */
    public Node getStart() {
        return start;
    }

    /**
     * @return the end
     */
    public Node getEnd() {
        return end;
    }

}
