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
import de._13ducks.cor.game.server.Server;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
            for (SubSectorObstacle obst : graph) {
                obst.removeNearNodes(next, radius);
            }
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
            List<Moveable> moversAround = Server.getInnerServer().moveMan.moveMap.moversAround(work, (work.getRadius() + radius) * 2);
            for (Moveable pmove : moversAround) {
                if (!closedObstacles.contains(pmove) && !openObstacles.contains(pmove)) {
                    openObstacles.add(pmove);
                }
            }
        }

        // Jetzt drüber laufen und Graph aufbauen:
        for (SubSectorObstacle obst : graph) {
            // Vorgensweise:
            // In jedem Hinderniss die Linie entlanglaufen und Knoten mit Kanten verbinden.
            // Ein Knoten darf auf einem Kreis immer nur in eine Richtung gehen.
            // (das sollte mithilfe seiner beiden, bekannten hindernisse recht einfach sein)
            // Die Länge des Kreissegments lässt sich einfach mithilfe des winkels ausrechnen (Math.atan2(y,x)
            // Dann darf der A*. Bzw. Dijkstra, A* ist hier schon fast Overkill.
            // Alle Knoten ihrem Bogenmaß nach sortieren.
            obst.sortNodes();
            obst.interConnectNodes(radius);
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

        /**
         * Löscht alle Knoten, die zu nahe an next dran sind.
         * @param next 
         */
        private void removeNearNodes(SubSectorObstacle next, double radius) {
            for (int i = 0; i < nodes.size(); i++) {
                SubSectorNode node = nodes.get(i);
                if (moveCircleContains(node, radius)) {
                    nodes.remove(i--);
                }
            }
        }

        /**
         * Alle Knoten ihrem Bogenmaß nach sortieren
         */
        private void sortNodes() {
            Collections.sort(nodes, new Comparator<SubSectorNode>() {

                @Override
                public int compare(SubSectorNode o1, SubSectorNode o2) {
                    double t1 = Math.atan2(y - o1.y, x - o1.x);
                    double t2 = Math.atan2(y - o2.y, x - o2.x);
                    if (t1 > t2) {
                        return 1;
                    } else if (t1 < t2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }

        /**
         * Verbindet die Knoten intern.
         * Setzt sortierte Knotenliste voraus.
         */
        private void interConnectNodes(double radius) {
            SubSectorNode start = nodes.peekFirst();
            Iterator<SubSectorNode> iter = nodes.iterator();
            SubSectorNode current = start;
            iter.next(); // Eines übersprigen
            // Immer versuchen, mit dem nächsten zu verbinden.
            while (iter.hasNext() && current != null) {
                SubSectorNode work = iter.next();
                // Versuche current mit work zu verbinden.
                // Geht nur, wenn current in + und work in - Richtung verbunden werden dürfen.
                // Richtungen bestimmen
                boolean cDirection = calcDirection(current, radius);
                boolean wDirection = calcDirection(work, radius);
                // Verbinden, wenn c + und w - ist.
                if (cDirection & !wDirection) {
                    // Verbinden
                    buildEdge(current, work);
                }
                current = work;
            }
            // Am Ende noch versuchen den letzen mit dem Start zu verbinden:
            if (calcDirection(nodes.getLast(), radius) & !calcDirection(nodes.getFirst(), radius)) {
                buildEdge(nodes.getLast(), nodes.getFirst());
            }
        }

        /**
         * Berechnet, ob ein Knoten in Plus (true) oder Minus-Richtung laufen darf.
         * @param node Der Knoten
         * @return true, wenn in Plus-Richtung.
         */
        private boolean calcDirection(SubSectorNode node, double radius) {
            // "Anderes" Hinderniss finden
            SubSectorObstacle other = node.otherObstacle(this);
            // Abstand vom anderen zu Node berechnen:
            double dist = Math.sqrt((node.x - other.x) * (node.x - other.x) + (node.y - other.y) * (node.y - other.y));
            // Ein kleines Stück in plus-Richtung weiter gehen:
            double tetha = Math.atan2(y - node.y, x - node.x);
            tetha += 0.1;
            if (tetha > 2 * Math.PI) {
                tetha = 0.1;
            }
            // Punkt hier berechnen:
            Vector newVec = new Vector(Math.cos(tetha), Math.sin(tetha));
            newVec.normalize().multiply(other.radius + radius);
            // Abstand hier berechnen:
            double dist2 = Math.sqrt((newVec.x() - other.x) * (newVec.x() - other.x) + (newVec.y() - other.y) * (newVec.y() - other.y));
            // Wenn Abstand größer geworden, dann darf man in Plus gehen
            if (dist2 > dist) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof SubSectorObstacle) {
                SubSectorObstacle obst = (SubSectorObstacle) o;
                if (Math.abs(x - obst.x) < 0.001 && Math.abs(y - obst.y) < 0.001) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Verbindet current mit work.
         * @param current
         * @param work 
         */
        private void buildEdge(SubSectorNode current, SubSectorNode work) {
            double length = Math.atan2(y - work.y, x - work.x) - Math.atan2(y - current.y, x - current.x);
            SubSectorEdge edge = new SubSectorEdge(current, work, length);
            current.addEdge(edge);
            work.addEdge(edge);
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
        /**
         * Kanten zu anderen Knoten
         */
        private ArrayList<SubSectorEdge> myEdges;

        SubSectorNode(double x, double y, SubSectorObstacle... owner) {
            this.x = x;
            this.y = y;
            myObstacle = new ArrayList<SubSectorObstacle>();
            myObstacle.addAll(Arrays.asList(owner));
            myEdges = new ArrayList<SubSectorEdge>();
        }

        /**
         * Fügt die Kante zu diesem Knoten hinzu
         * Fall sie schon bekannt ist passiert nichts.
         * @param edge die neue Kante
         */
        private void addEdge(SubSectorEdge edge) {
            if (!myEdges.contains(edge)) {
                myEdges.add(edge);
            }
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

        /**
         * Findet in einem Knoten mit 2 Hindernissen das andere heraus
         * (also nicht das, das übergeben wurde)
         * @param obst
         * @return 
         */
        private SubSectorObstacle otherObstacle(SubSectorObstacle obst) {
            if (myObstacle.get(0).equals(obst)) {
                return myObstacle.get(1);
            } else {
                return myObstacle.get(0);
            }
        }
    }

    private static class SubSectorEdge {

        /**
         * Die beiden Knoten.
         */
        private SubSectorNode n1, n2;
        /**
         * Die Länge dieser Kante ("das Kantengewicht")
         */
        private double length;

        SubSectorEdge(SubSectorNode n1, SubSectorNode n2, double lenght) {
            this.n1 = n1;
            this.n2 = n2;
            this.length = lenght;
        }
    }
}
