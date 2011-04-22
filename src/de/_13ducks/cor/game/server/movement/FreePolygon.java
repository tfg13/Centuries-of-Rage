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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ein freies Vieleck. Ein Teil des Movement-Map-Graphen
 */
public class FreePolygon {

    /**
     * Eine Liste mit allen Nodes, die auf einer Kante dieses Polygons liegen oder die eine Ecke darstellen.
     */
    private List<Node> myNodes;

    /**
     * Erzeugt einen neues Vieleck mit den angegebenen Knoten als Eckpunkten.
     * Testet NICHT, ob das Vieleck auch konvex ist (muss es normalerweise sein)
     * Wirft eine Exception, wenn Parameter null sind oder weniger als 3 geliefert werden.
     * Registriert sich automatisch bei den Polygonen als zugehöriger Node
     * @param nodes beliebig viele Nodes, mindestens 3
     */
    public FreePolygon(Node... nodes) {
        if (nodes == null || nodes.length < 3) {
            throw new IllegalArgumentException("At least three nodes requried!");
        }
        myNodes = new ArrayList<Node>();
        myNodes.addAll(Arrays.asList(nodes));
        for (Node node : myNodes) {
            node.addPolygon(this);
        }
    }
}
