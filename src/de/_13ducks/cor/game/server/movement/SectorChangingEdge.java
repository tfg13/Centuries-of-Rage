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
 * Diese spezielle Kante wird vom Low/MidLevel-Bewegungssystem verwendet,
 * um zu erkennen, wann eine Sektorgrenze überschritten wurde.
 */
public class SectorChangingEdge extends Edge {
    
    private FreePolygon poly1;
    private FreePolygon poly2;
    
    public SectorChangingEdge(Node start, Node end, FreePolygon p1, FreePolygon p2) {
        super(start, end);
        poly1 = p1;
        poly2 = p2;
    }
    
    /**
     * Liefert den Polygon auf der anderen Seite der Grenze.
     * Dazu muss der Polygon, auf der Seite, von der man kommt angegeben werden.
     * @param from der Polygon, von dem man kommt
     * @return der andere Polygon
     */
    FreePolygon getNext(FreePolygon from) {
        if (poly1.equals(from)) {
            return poly2;
        } else {
            return poly1;
        }
    }
    
    
}
