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
        ArrayList<Moveable> closedObstacles = new ArrayList<Moveable>(); // Bearbeitete Knoten
        
        openObstacles.add(obstacle); // Startpunkt des Graphen.
        closedObstacles.add(mover); // Wird im Graphen nicht mitberücksichtigt.
        double radius = mover.getRadius();
        
        while (!openObstacles.isEmpty()) {
            // Neues Element aus der Liste holen und als bearbeitet markieren.
            Moveable work = openObstacles.poll();
            closedObstacles.add(work);
            SubSectorNode next = new SubSectorPathfinder.SubSectorNode(work.getPrecisePosition().x(), work.getPrecisePosition().y(), work.getRadius()); 
            // Mit Graph vernetzen
            for (SubSectorNode node : graph) {
                if (node.inColRange(next, radius)) {
                    
                }
            }
        }
        
        throw new UnsupportedOperationException("not yet implemented.");
    }
    
    
    /**
     * Ein Knoten des Graphen
     */
    private static class SubSectorNode {
        
        /**
         * Koordinaten
         */
        private double x;
        /**
         * Koordinaten
         */
        private double y;
        /**
         * Der Radius dieses Hindernisses selbst
         */
        private double radius;
        
        SubSectorNode(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        /**
         * @return the x
         */
        double getX() {
            return x;
        }

        /**
         * @param x the x to set
         */
        void setX(double x) {
            this.x = x;
        }

        /**
         * @return the y
         */
        double getY() {
            return y;
        }

        /**
         * @param y the y to set
         */
        void setY(double y) {
            this.y = y;
        }

        /**
         * Findet heraus, ob sich die Lauflinie dieses Knoten des Graphen mit dem
         * gegeben schneidet.
         * @param next Der andere Knoten
         * @param moveRadius Der Radius des Objektes, das dazwischen noch durch passen soll
         * @return true, wenn sie sich schneiden
         */
        private boolean inColRange(SubSectorNode next, double moveRadius) {
            double dist = Math.sqrt((x - next.x) * (x - next.x) + (y - next.y) * (y - next.y));
            if (dist <= radius + next.radius + moveRadius + moveRadius) {
                return true;
            }
            return false;
        }

        /**
         * @return the radius
         */
        double getRadius() {
            return radius;
        }
        
    }
    
    /**
     * Eine Kante des Graphen
     */
    private class SubSectorEdge {
        
    }
    
    
}
