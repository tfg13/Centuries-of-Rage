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
import de._13ducks.cor.game.SimplePosition;

/**
 * Eine Kante aus 2 Polygonen.
 * Wird für Polygon-Merge Berechnungen verwendet.
 * Die Kante hat zwar einen Start und einen Zielpolygon,
 * bei den meisten Berechnungen spielt die Richtung
 * aber keine Rolle. Deshalb ignorieren die meisten Methoden
 * die Richtung, so z.B. die equals Methode.
 */
public class Edge {

    /**
     * Der Startknoten dieser Kante.
     * Im Prinzip einfach irgendein Knoten, da die Richtung meistens egal ist.
     */
    private Node start;
    /**
     * Der Endknoten dieser Kante.
     * Im Prinzip einfach irgendein Knoten, da die Richtung meistens egal ist.
     */
    private Node end;

    /**
     * Erzeugt eine neue Kante mit den angegebenen Start- und Zielpunkt.
     * In der Regel ist die Reihenfolge egal, Start- und Zielpunkt als vertauschbar.
     * @param start Der erste Knoten der Kante
     * @param end Der zweite Knoten der Kante
     */
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
     * Returns the start (1st) node of this edge
     * @return the start (1st) node of this edge
     */
    public Node getStart() {
        return start;
    }

    /**
     * Resturn the end (2nd) node of this edge
     * @return the end (2nd) node of this edge
     */
    public Node getEnd() {
        return end;
    }

    /**
     * Findet heraus, sich diese und die gegebenen Kante schneiden.
     * Das Verhalten für gleiche Kanten ist undefiniert.
     * Sollte der Schnittpunkt genau auf dem Ende liegen, sagt diese Methode true.
     * @param edge die andere Kante
     * @return true, wenn ein Schnittpunkt existiert, der auf beiden Kanten liegt, sonst false.
     */
    public boolean intersectsWithEndsAllowed(Edge edge) {
        return intersectionWithEndsAllowed(edge) != null;
    }

    public SimplePosition intersectionWithEndsAllowed(Edge edge) {
        // Beide Richtungsvektoren berechnen:
        Vector me = new Vector(end.getX() - start.getX(), end.getY() - start.getY());
        Vector other = new Vector(edge.end.getX() - edge.start.getX(), edge.end.getY() - edge.start.getY());
        // Gibts einen Schnittpunkt?
        Vector intersection = me.intersectionWith(start.toVector(), edge.start.toVector(), other);
        if (intersection != null) {
            // Liegt dieser Schnittpunkt auf beiden Kante?
            if (intersection.getX() >= Math.min(start.getX(), end.getX()) && intersection.getX() <= Math.max(start.getX(), end.getX()) && intersection.getY() >= Math.min(start.getY(), end.getY()) && intersection.getY() <= Math.max(start.getY(), end.getY())) {
                if (intersection.getX() >= Math.min(edge.start.getX(), edge.end.getX()) && intersection.getX() <= Math.max(edge.start.getX(), edge.end.getX()) && intersection.getY() >= Math.min(edge.start.getY(), edge.end.getY()) && intersection.getY() <= Math.max(edge.start.getY(), edge.end.getY())) {
                    return intersection;
                }
            }
        }
        return null;
    }

    /**
     * Findet heraus, sich diese und die gegebenen Kante schneiden.
     * Das Verhalten für gleiche Kanten ist undefiniert.
     * Sollte der Schnittpunkt genau auf dem Ende liegen, sagt diese Methode false.
     * @param edge die andere Kante
     * @return true, wenn ein Schnittpunkt existiert, der auf beiden Kanten liegt, sonst false.
     */
    public boolean intersectsWithEndsNotAllowed(Edge edge) {
        return intersectionWithEndsAllowed(edge) != null;
    }

    public SimplePosition intersectionWithEndsNotAllowed(Edge edge) {
        // Beide Richtungsvektoren berechnen:
        Vector me = new Vector(end.getX() - start.getX(), end.getY() - start.getY());
        Vector other = new Vector(edge.end.getX() - edge.start.getX(), edge.end.getY() - edge.start.getY());
        // Gibts einen Schnittpunkt?
        Vector intersection = me.intersectionWith(start.toVector(), edge.start.toVector(), other);
        if (intersection != null) {
            // Liegt dieser Schnittpunkt auf beiden Kante?
            if (intersection.getX() >= Math.min(start.getX(), end.getX()) && intersection.getX() <= Math.max(start.getX(), end.getX()) && intersection.getY() >= Math.min(start.getY(), end.getY()) && intersection.getY() <= Math.max(start.getY(), end.getY())) {
                if (intersection.getX() >= Math.min(edge.start.getX(), edge.end.getX()) && intersection.getX() <= Math.max(edge.start.getX(), edge.end.getX()) && intersection.getY() >= Math.min(edge.start.getY(), edge.end.getY()) && intersection.getY() <= Math.max(edge.start.getY(), edge.end.getY())) {
                    // Liegts genau auf den Ecken?
                    if (intersection.equals(end.toVector()) || intersection.equals(start.toVector()) || intersection.equals(edge.start.toVector()) || intersection.equals(edge.end.toVector())) {
                        return null; // Dann nicht!
                    }
                    return intersection;
                }
            }
        }
        return null;
    }

    /**
     * Findet den Schnittpunkt zwischen dieser und der gegebenen Edge, sofern er existiert.
     * Nimmt dazu unendlich lange Edges an.
     * @param edge Die andere Kante
     * @return Der Schnittpunkt oder null.
     */
    public SimplePosition endlessIntersection(Edge edge) {
        // Beide Richtungsvektoren berechnen:
        Vector me = new Vector(end.getX() - start.getX(), end.getY() - start.getY());
        Vector other = new Vector(edge.end.getX() - edge.start.getX(), edge.end.getY() - edge.start.getY());
        // Gibts einen Schnittpunkt?
        Vector inter = me.intersectionWith(start.toVector(), edge.start.toVector(), other);
        if (inter.isValid()) {
            return inter;
        }
        return null;
    }

    /**
     * Findet für Punkte, die auf dieser Kante liegen würden, wenn sie unendlich wäre
     * heraus, ob sie auch auf dieser endlich langen Kante liegen.
     * @param onLine Die Position. MUSS (!) auf der unendlich langen Linie liegen
     * @return true, wenn drauf (ecken zählen mit)
     */
    public boolean partOf(SimplePosition onLine) {
        // Liegt dieser Schnittpunkt auf beiden Kante?
        if (onLine.x() >= Math.min(start.x(), end.x()) && onLine.x() <= Math.max(start.x(), end.x()) && onLine.y() >= Math.min(start.getY(), end.getY()) && onLine.y() <= Math.max(start.getY(), end.getY())) {
            return true;
        }
        return false;
    }

    /**
     * Erzeugt einen neuen Knoten, der auf den Mittelpunkt dieser Kante zeigt.
     * @return einen neuen Knoten, der auf den Mittelpunkt dieser Kante zeigt.
     */
    public Node getCenter() {
        Vector vec = new Vector(start.getX() - end.getX(), start.getY() - end.getY());
        vec.multiplyMe(0.5);
        vec.addToMe(end.toVector());
        return new Node(vec.getX(), vec.getY());
    }

    @Override
    public String toString() {
        return start + "-->" + end;
    }

    /**
     * Findet heraus, ob der eine Punkt auf der einen, der andere auf der anderen Seite dieser Linie liegen - 
     * oder ob beide auf der gleichen Seite sind.
     * Gibt true, wenn die Seiten unterschiedlich sind.
     * @param pos1 Position 1
     * @param pos2 Position 
     * @return 
     */
    boolean sidesDiffer(SimplePosition pos1, SimplePosition pos2) {
        // Linie ziehen:
        Edge direct = new Edge(pos1.toNode(), pos2.toNode());
        // Schnittpunkt suchen
        return intersectsWithEndsAllowed(direct);

    }
}
