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
import de._13ducks.cor.game.SimplePosition;
import de._13ducks.cor.game.server.Server;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.collections.buffer.PriorityBuffer;

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
    static List<SubSectorEdge> searchDiversion(Moveable mover, Moveable obstacle, SimplePosition target, ObstaclePattern p) {
        // Vorüberprüfung: Ist das Ziel überhaupt noch frei?
        List<Moveable> moversAroundTarget = Server.getInnerServer().moveMan.moveMap.moversAroundPoint(target.toFPP(), mover.getRadius() + 5);
        moversAroundTarget.remove(mover); // Falls drin
        for (Moveable m : moversAroundTarget) {
            if (m.getPrecisePosition().getDistance(target.toFPP()) < m.getRadius() + mover.getRadius() + ServerBehaviourMove.MIN_DISTANCE) {
                System.out.println("No div, target blocked!");
                return null;
            }
        }
        
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
        p.freezeUnit(obstacle);
        closedObstacles.add(mover); // Wird im Graphen nicht mitberücksichtigt.
        double radius = mover.getRadius() + ServerBehaviourMove.MIN_DISTANCE;

        while (!openObstacles.isEmpty()) {
            // Neues Element aus der Liste holen und als bearbeitet markieren.
            Moveable work = openObstacles.poll();
            closedObstacles.add(work);
            SubSectorObstacle next = new SubSectorObstacle(work.getPrecisePosition().x(), work.getPrecisePosition().y(), work.getRadius());
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
                            if (o.equals(node)) {
                                continue; // Um den gehts jetzt ja gerade, natürlich liegen wir auf diesem Kreis
                            }
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
            // Bearbeitetes selbst in Graph einfügen
            graph.add(next);
            // Weitere Hindernisse suchen, die jetzt relevant sind.
            List<Moveable> moversAround = Server.getInnerServer().moveMan.moveMap.moversAround(work, (work.getRadius() + radius) * 2);
            for (Moveable pmove : moversAround) {
                if (!closedObstacles.contains(pmove) && !openObstacles.contains(pmove)) {
                    openObstacles.add(pmove);
                    p.freezeUnit(pmove);
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

        // Start- und Zielknoten einbauen und mit dem Graph vernetzten.
        SubSectorNode startNode = new SubSectorNode(mover.getPrecisePosition().x(), mover.getPrecisePosition().y());
        SubSectorNode targetNode = new SubSectorNode(target.x(), target.y());
        double min = Double.POSITIVE_INFINITY;
        SubSectorObstacle minObstacle = null;
        for (SubSectorObstacle obst : graph) {
            double newdist = Math.sqrt((obst.getX() - startNode.getX()) * (obst.getX() - startNode.getX()) + (obst.getY() - startNode.getY()) * (obst.getY() - startNode.getY()));
            newdist -= obst.getRadius() + radius; // Es interessiert uns der nächstmögliche Kreis, nicht das nächste Hinderniss
            if (newdist < min) {
                min = newdist;
                minObstacle = obst;
            }
        }
        // Punkt auf Laufkreis finden
        Vector direct = new Vector(startNode.getX() - minObstacle.getX(), startNode.getY() - minObstacle.getY());
        direct = direct.normalize().multiply(minObstacle.getRadius() + radius);

        SubSectorNode minNode = new SubSectorNode(minObstacle.getX() + direct.getX(), minObstacle.getY() + direct.getY(), minObstacle);

        // In das Hinderniss integrieren:
        minObstacle.lateIntegrateNode(minNode);
        SubSectorEdge startEdge = new SubSectorEdge(startNode, minNode, min);

        if (!startNode.equals(minNode)) {

            startNode.addEdge(startEdge);
            minNode.addEdge(startEdge);

        } else {
            // Wir stehen schon auf dem minNode.
            // Die Einsprungkante ist nicht notwendig.
            startNode = minNode;
        }


        double min2 = Double.POSITIVE_INFINITY;
        SubSectorObstacle minObstacle2 = null;
        for (SubSectorObstacle obst : graph) {
            double newdist = Math.sqrt((obst.getX() - targetNode.getX()) * (obst.getX() - targetNode.getX()) + (obst.getY() - targetNode.getY()) * (obst.getY() - targetNode.getY()));
            newdist -= obst.getRadius() + radius; // Es interessiert uns der nächstmögliche Kreis, nicht das nächste Hinderniss
            if (newdist < min2) {
                min2 = newdist;
                minObstacle2 = obst;
            }
        }
        // Punkt auf Laufkreis finden
        Vector direct2 = new Vector(targetNode.getX() - minObstacle2.getX(), targetNode.getY() - minObstacle2.getY());
        direct2 = direct2.normalize().multiply(minObstacle2.getRadius() + radius);

        SubSectorNode minNode2 = new SubSectorNode(minObstacle2.getX() + direct2.getX(), minObstacle2.getY() + direct2.getY(), minObstacle2);

        // In das Hinderniss integrieren:
        minObstacle2.lateIntegrateNode(minNode2);
        SubSectorEdge targetEdge = new SubSectorEdge(minNode2, targetNode, min2);

        if (!targetNode.equals(minNode2)) {

            targetNode.addEdge(targetEdge);
            minNode2.addEdge(targetEdge);

        } else {
            // Das Ziel ist schon auf dem Laufkreis.
            // Die Aussprungkante ist nicht nötig.
            targetNode = minNode2;
        }
        
        /**
         * Hier jetzt einen Weg suchen von startNode nach targetNode.
         * Die Kanten sind in node.myEdges
         * Die Ziele bekommt man mit edge.getOther(startNode)
         * Die Länge (Wegkosten) stehen in edge.length (vorsicht: double-Wert!)
         */
        PriorityBuffer open = new PriorityBuffer();      // Liste für entdeckte Knoten
        LinkedHashSet<SubSectorNode> containopen = new LinkedHashSet<SubSectorNode>();  // Auch für entdeckte Knoten, hiermit kann viel schneller festgestellt werden, ob ein bestimmter Knoten schon enthalten ist.
        LinkedHashSet<SubSectorNode> closed = new LinkedHashSet<SubSectorNode>();    // Liste für fertig bearbeitete Knoten

        double cost_t = 0;    //Movement Kosten (gerade 5, diagonal 7, wird später festgelegt)

        open.add(startNode);

        while (open.size() > 0) {
            SubSectorNode current = (SubSectorNode) open.remove();
            containopen.remove(current);

            if (current.equals(targetNode)) {	//Abbruch, weil Weg von Start nach Ziel gefunden wurde
                //targetNode.setParent(current.getParent());   //"Vorgängerfeld" von Ziel bekannt
                break;
            }

            // Aus der open wurde current bereits gelöscht, jetzt in die closed verschieben
            closed.add(current);

            ArrayList<SubSectorEdge> neighbors = current.getMyEdges();

            for (SubSectorEdge edge : neighbors) {

                SubSectorNode node = edge.getOther(current);

                if (closed.contains(node)) {
                    continue;
                }

                // Kosten dort hin berechnen
                cost_t = edge.getLength();

                if (containopen.contains(node)) {         //Wenn sich der Knoten in der openlist befindet, muss berechnet werden, ob es einen kürzeren Weg gibt

                    if (current.getCost() + cost_t < node.getCost()) {		//kürzerer Weg gefunden?

                        node.setCost(current.getCost() + cost_t);         //-> Wegkosten neu berechnen
                        //node.setValF(node.cost + node.getHeuristic());  //F-Wert, besteht aus Wegkosten vom Start + Luftlinie zum Ziel
                        node.setParent(current); //aktuelles Feld wird zum Vorgängerfeld
                    }
                } else {
                    node.setCost(current.getCost() + cost_t);
                    //node.setHeuristic(Math.sqrt(Math.pow(Math.abs((targetNode.getX() - node.getX())), 2) + Math.pow(Math.abs((targetNode.getY() - node.getY())), 2)));	// geschätzte Distanz zum Ziel
                    //Die Zahl am Ende der Berechnung ist der Aufwand der Wegsuche
                    //5 ist schnell, 4 normal, 3 dauert lange

                    node.setParent(current);						// Parent ist die RogPosition, von dem der aktuelle entdeckt wurde
                    //node.setValF(node.cost + node.getHeuristic());  //F-Wert, besteht aus Wegkosten vom Start aus + Luftlinie zum Ziel
                    open.add(node);    // in openlist hinzufügen
                    containopen.add(node);
                }
            }
        }

        if (targetNode.getParent() == null) {		//kein Weg gefunden
            return null;
        }

        ArrayList<SubSectorNode> pathrev = new ArrayList<SubSectorNode>();   //Pfad aus parents erstellen, von Ziel nach Start
        while (targetNode != startNode) { // Dieser Referenz-Vergleich ist ABSICHT!!!
            pathrev.add(targetNode);
            targetNode = targetNode.getParent();
        }
        pathrev.add(startNode);

        ArrayList<SubSectorNode> path = new ArrayList<SubSectorNode>();	//Pfad umkehren, sodass er von Start nach Ziel ist
        for (int k = pathrev.size() - 1; k >= 0; k--) {
            path.add(pathrev.get(k));
        }

        // Nachbearbeitung:
        // Wir brauchen eine Kanten-Liste mit arc/direct Informationen

        ArrayList<SubSectorEdge> finalPath = new ArrayList<SubSectorEdge>();
        for (int i = 0; i < path.size() - 1; i++) {
            SubSectorNode from = path.get(i);
            SubSectorNode to = path.get(i + 1);
            SubSectorEdge edge = shortestCommonEdge(from, to);
            if (edge != null) {
                finalPath.add(edge);
            } else {
                throw new RuntimeException("ERROR Cannot find edge from " + from + " to " + to + " but it is part of the calculated path!!!");
            }
        }

        return finalPath;					//Pfad zurückgeben
    }

    private static SubSectorEdge shortestCommonEdge(SubSectorNode from, SubSectorNode to) {
        ArrayList<SubSectorEdge> toEdges = to.getMyEdges();
        SubSectorEdge shortesCommon = null;
        double minLength = Double.POSITIVE_INFINITY;
        for (SubSectorEdge edge : from.getMyEdges()) {
            if (toEdges.contains(edge)) {
                if (edge.getLength() < minLength) {
                    shortesCommon = edge;
                    minLength = edge.getLength();
                }
            }
        }
        return shortesCommon;
    }
}
