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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Dieser Pathfinder sucht Wege innerhalb von freien Flächen um (bewegliche)
 * Hindernisse herum.
 */
public class SubSectorPathfinder {
    
    /**
     * Sucht einen Weg auf Freiflächen (FreePolygon) um ein Hindernis herum.
     * Beachtet weitere Hindernisse auf der "Umleitung".
     * Sucht die Route nur bis zum nächsten Ziel.
     * Der Mover darf sich nicht bereits auf einer Umleitung befinden,
     * diese muss ggf vorher gelöscht worden sein.
     * @param mover
     * @param obstacle
     * @return 
     */
    static List<Node> searchDiversion(Moveable mover, Moveable obstacle) {
        /**
         * Wegsuche in 2 Schritten:
         * 1. Aufbauen eines geeigneten Graphen, der das gesamte Problem enthält.
         * 2. Suchen einer Route in diesem Graphen mittels A* (A-Star).
         */
        // Aufbauen des Graphen:
        ArrayList<SubSectorNode> graph = new ArrayList<SubSectorNode>(); // Der Graph selber
        LinkedList<Moveable> openObstacles = new LinkedList<Moveable>(); // Die Liste mit noch zu untersuchenden Knoten
        ArrayList<Moveable> closedObstacles = new ArrayList<Moveable>(); // Bearbeitetet Knoten
        
        openObstacles.add(obstacle); // Startpunkt des Graphen.
        closedObstacles.add(mover); // Wird im Graphen nicht mitberücksichtigt.
        double radius = mover.getRadius();
        
        while (!openObstacles.isEmpty()) {
            // Neues Element aus der Liste holen und als bearbeitet markieren.
            Moveable work = openObstacles.poll();
            closedObstacles.add(work);
            // Muss mit jedem Knoten verbunden werden, mit dem es sich schneidet.
            for (int i = 0; i < graph.size(); i++) {
                
            }
        }
        
        throw new UnsupportedOperationException("not yet implemented.");
    }
    
    
    /**
     * Ein Knoten des Graphen
     */
    private class SubSectorNode {
        
    }
    
    /**
     * Eine Kante des Graphen
     */
    private class SubSectorEdge {
        
    }
    
    
}
