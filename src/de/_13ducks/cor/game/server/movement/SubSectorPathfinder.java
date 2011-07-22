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
import java.util.Arrays;
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
        ArrayList<SubSectorObstacle> graph = new ArrayList<SubSectorObstacle>(); // Der Graph selber
        LinkedList<Moveable> openObstacles = new LinkedList<Moveable>(); // Die Liste mit noch zu untersuchenden Knoten
        ArrayList<Moveable> closedObstacles = new ArrayList<Moveable>(); // Bearbeitete Knoten
        
        openObstacles.add(obstacle); // Startpunkt des Graphen.
        closedObstacles.add(mover); // Wird im Graphen nicht mitberücksichtigt.
        double radius = mover.getRadius();
        
        while (!openObstacles.isEmpty()) {
            // Neues Element aus der Liste holen und als bearbeitet markieren.
            Moveable work = openObstacles.poll();
            closedObstacles.add(work);
            SubSectorObstacle next = new SubSectorPathfinder.SubSectorObstacle(work.getPrecisePosition().x(), work.getPrecisePosition().y(), work.getRadius()); 
            // Zuerst alle Punkte des Graphen löschen, die jetzt nichtmehr erreichbar sind:
            System.out.println("TODO: Del unreachable nodes");
            // Mit Graph vernetzen
            for (SubSectorObstacle node : graph) {
                if (node.inColRange(next, radius)) {
                    // Schnittpunkte suchen
                    SubSectorNode[] intersections = node.calcIntersections(next, radius);
                    for (SubSectorNode n2 : intersections) {
                        boolean reachable = true;
                        for (SubSectorObstacle o : graph) {
                            if (o.moveCircleContains(n2, radius)) {
                                reachable = false;
                                break;
                            }
                        }
                        if (reachable) {
                            // Schnittpunkt einbauen
                            next.addNode(n2);
                            node.addNode(n2);
                        }
                    }
                }
            }
            // Weitere Hindernisse suchen, die jetzt relevant sind.
            System.out.println("TODO: Search new obstacles");
        }
        
        throw new UnsupportedOperationException("not yet implemented.");
    }
    
    
    /**
     * Ein Knoten des Graphen
     */
    private static class SubSectorObstacle {
        
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
        /**
         * Die Knoten dieses SubSectorObstacles
         */
        private LinkedList<SubSectorNode> nodes;
        
        
        SubSectorObstacle(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            nodes = new LinkedList<SubSectorNode>();
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
         * gegebenen schneidet.
         * @param next Der andere Knoten
         * @param moveRadius Der Radius des Objektes, das dazwischen noch durch passen soll
         * @return true, wenn sie sich schneiden
         */
        private boolean inColRange(SubSectorObstacle next, double moveRadius) {
            double dist = Math.sqrt((x - next.x) * (x - next.x) + (y - next.y) * (y - next.y));
            if (dist < radius + next.radius + moveRadius + moveRadius) {
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

        /**
         * Berechnet die Schnittpunkte der Lauflinien dieses und des gegebenen
         * Hindernisses
         * Setzt voraus, dass es zwei Schnittpunkte gibt. Das Verhalten dieser
         * Methode ist in anderen Fällen nicht definiert.
         * @param next Das andere Hinderniss
         * @param radius Der Radius (die Größe) des Movers, der noch zwischendurch passen muss.
         * @return SubSectorNode[] mit 2 Einträgen
         */
        private SubSectorNode[] calcIntersections(SubSectorObstacle next, double radius) {
            // Zuerst Mittelpunkt der Linie durch beide Schnittpunkte berechnen:
            Vector direct = new Vector(next.x - x, next.y - y);
            Vector z1 = direct.normalize().multiply(this.radius + radius);
            Vector z2 = direct.normalize().multiply(direct.length() - (next.radius + radius));
            Vector mid = direct.normalize().multiply((z1.length() + z2.length()) / 2.0);
            // Senkrechten Vektor und seine Länge berechnen:
            Vector ortho = new Vector(direct.y(), -direct.x());
            ortho.normalize().multiply(Math.sqrt(((this.radius + radius) * (this.radius + radius)) - (mid.length() * mid.length())));
            // Schnittpunkte ausrechnen:
            SubSectorNode[] intersections = new SubSectorNode[2];
            Vector posMid = new Vector(x + mid.x(), y + mid.y()); // Positionsvektor des Mittelpunkts
            Vector s1 = posMid.add(ortho);
            Vector s2 = posMid.add(ortho.getInverted());
            intersections[0] = new SubSectorNode(s1.x(), s1.y(), this, next);
            intersections[1] = new SubSectorNode(s2.x(), s2.y(), this, next);
            return intersections;
        }

        /**
         * Findet heraus, ob der gegebene Punkt innerhalb des Laufkreises liegt,
         * also zu nahe dran ist.
         * @param n2 Der zu untersuchende Punkt
         * @return true, wenn zu nahe dran
         */
        private boolean moveCircleContains(SubSectorNode n2, double radius) {
            double dist = Math.sqrt((x - n2.x) * (x - n2.x) + (y - n2.y) * (y - n2.y));
            if (dist < radius + this.radius) {
                return true;
            }
            return false;
        }

        /**
         * Fügt einen Knoten in die Kreislinie dieses Obstacles ein
         * @param n2 
         */
        private void addNode(SubSectorNode n2) {
            if (nodes.contains(n2)) {
                System.out.println("ERROR: Adding same Node again!!");
            } else {
                nodes.add(n2);
            }
        }
        
    }
    
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
         * Auf wessen Laufkreis liegt dieses Hinderniss?
         */
        private ArrayList<SubSectorObstacle> myObstacle;
        
        SubSectorNode(double x, double y, SubSectorObstacle... owner) {
            this.x = x;
            this.y = y;
            myObstacle = new ArrayList<SubSectorObstacle>();
            myObstacle.addAll(Arrays.asList(owner));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
            return hash;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof SubSectorNode) {
                SubSectorNode node = (SubSectorNode) o;
                if (Math.abs(x - node.x) < 0.001 && Math.abs(y - node.y) < 0.001) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    
}
