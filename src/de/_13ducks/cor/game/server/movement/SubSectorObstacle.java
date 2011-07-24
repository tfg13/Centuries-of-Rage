/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.game.server.movement;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Ein Knoten des Graphen
 */
public class SubSectorObstacle {

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
    boolean inColRange(SubSectorObstacle next, double moveRadius) {
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
    SubSectorNode[] calcIntersections(SubSectorObstacle next, double radius) {
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
    boolean moveCircleContains(SubSectorNode n2, double radius) {
        double dist = Math.sqrt((x - n2.getX()) * (x - n2.getX()) + (y - n2.getY()) * (y - n2.getY()));
        if (dist < radius + this.radius) {
            return true;
        }
        return false;
    }

    /**
     * Fügt einen Knoten in die Kreislinie dieses Obstacles ein
     * @param n2 
     */
    void addNode(SubSectorNode n2) {
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
    void removeNearNodes(SubSectorObstacle next, double radius) {
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
    void sortNodes() {
        Collections.sort(nodes, new Comparator<SubSectorNode>() {

            @Override
            public int compare(SubSectorNode o1, SubSectorNode o2) {
                double t1 = Math.atan2(y - o1.getY(), x - o1.getX());
                double t2 = Math.atan2(y - o2.getY(), x - o2.getX());
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
    void interConnectNodes(double radius) {
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
    boolean calcDirection(SubSectorNode node, double radius) {
        // "Anderes" Hinderniss finden
        SubSectorObstacle other = node.otherObstacle(this);
        // Abstand vom anderen zu Node berechnen:
        double dist = Math.sqrt((node.getX() - other.x) * (node.getX() - other.x) + (node.getY() - other.y) * (node.getY() - other.y));
        // Ein kleines Stück in plus-Richtung weiter gehen:
        double tetha = Math.atan2(y - node.getY(), x - node.getX());
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    /**
     * Verbindet current mit work.
     * @param current
     * @param work 
     */
    void buildEdge(SubSectorNode current, SubSectorNode work) {
        double length = Math.atan2(y - work.getY(), x - work.getX()) - Math.atan2(y - current.getY(), x - current.getX());
        SubSectorEdge edge = new SubSectorEdge(current, work, length);
        current.addEdge(edge);
        work.addEdge(edge);
    }

    /**
     * @return the nodes
     */
    LinkedList<SubSectorNode> getNodes() {
        return nodes;
    }
}