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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.newdawn.slick.Color;

/**
 * Ein freies Vieleck. Ein Teil des Movement-Map-Graphen
 */
public class FreePolygon {

    /**
     * Eine Liste mit allen Nodes, die auf einer Kante dieses Polygons liegen oder die eine Ecke darstellen.
     */
    private LinkedList<Node> myNodes;
    /**
     * Die Farbe dieses Polygons, nur für Debug-Output
     */
    private Color color;
    /**
     * Die bekannten Nachbarn dieses Polygons
     */
    private List<FreePolygon> neighbors;

    /**
     * Erzeugt einen neues Vieleck mit den angegebenen Knoten als Eckpunkten.
     * Testet NICHT, ob das Vieleck auch konvex ist (muss es normalerweise sein)
     * Wirft eine Exception, wenn Parameter null sind oder weniger als 3 geliefert werden.
     * Registriert sich NICHT als Nachbar! (auch nicht, wenn registerNodes true ist!)
     * @param registerNodes Ob dieses neue Polygon bei seinen Nodes registriert werden soll.
     * @param nodes beliebig viele Nodes, mindestens 3
     */
    public FreePolygon(boolean registerNodes, Node... nodes) {
        if (nodes == null || nodes.length < 3) {
            throw new IllegalArgumentException("At least three nodes requried!");
        }
        myNodes = new LinkedList<Node>();
        neighbors = new ArrayList<FreePolygon>();
        myNodes.addAll(Arrays.asList(nodes));
        if (registerNodes) {
            registerNodes();
        }

        color = new Color((int) (Math.random() * 265.0), (int) (Math.random() * 265.0), (int) (Math.random() * 265.0), 100);
    }

    /**
     * Liefert einen Temporären Polygon, der die Verbindung dieses mit dem gegebenen Polygon darstellt.
     * Funktioniert nur, wenn beiden beiden Polygone genau eine gemeinsame Kante (aufgespannt von mindestens 2 gemeinsamen Nodes)
     * haben. Die Resultate in Fällen von mehreren gemeinsamen , aber nicht direkt zusammenhängenden Kanten sind undefiniert.
     * @param poly1 Polygon 1
     * @param poly2 Polygon 2
     * @return einen tempörären Polygon, der gemerged ist.
     */
    public static FreePolygon getMergedCopy(FreePolygon poly1, FreePolygon poly2) {
        // Hier muss rumgepfuscht werden, kopieren
        LinkedList<Node> poly1Nodes = (LinkedList<Node>) poly1.myNodes.clone();
        LinkedList<Node> poly2Nodes = (LinkedList<Node>) poly2.myNodes.clone();
        // Gemeinsame holen
        List<Node> intersection = intersectingNodes(poly1, poly2); // In der Reihenfolge des ersten (wichtig!)
        while (intersection.size() > 2) {
            // Rauslöschen, wir brauchen nur 2
            Node removed = intersection.remove(1);// "Mitte"
            poly1Nodes.remove(removed);
            poly2Nodes.remove(removed);
        }
        Node n1 = intersection.get(0);
        Node n2 = intersection.get(1);
        LinkedList<Node> newPoly = new LinkedList<Node>();
        // Vorbereiten der Listen. Die zweite Liste muss n1 und n2 in umgedrehter Reihenfolge enthalten.
        // Reihenfolge vom zweiten prüfen:
        int n1index = poly2Nodes.indexOf(n1);
        if (n1index + 1 >= poly2Nodes.size()) {
            n1index = -1;
        }
        if (poly2Nodes.get(n1index + 1).equals(n2)) {
            // Liste muss umgedreht werden!
            Collections.reverse(poly2Nodes);
        }
        // Die Listen müssen noch weiter vorbereitet werden: Die Verbindung von n1 nach n2 darf nicht über der Ende-Erstes grenze liegen
        // Notfalls rotieren
        while (poly1Nodes.indexOf(n1) > poly1Nodes.indexOf(n2)) {
            Collections.rotate(poly1Nodes, 1); // Sollte normalerweise nur ein mal passieren
        }
        while (poly2Nodes.indexOf(n1) < poly2Nodes.indexOf(n2)) {
            Collections.rotate(poly2Nodes, 1); // Sollte normalerweise nur ein mal passieren
        }
        // Jetzt zusammenbauen
        // Zuerst vom ersten bis zur ersten Intersection (exkl)
        newPoly.addAll(poly1Nodes.subList(0, poly1Nodes.indexOf(n1)));
        // Jetzt hinzufügen
        newPoly.addAll(poly2Nodes.subList(poly2Nodes.indexOf(n1), poly2Nodes.indexOf(n2)));
        // Noch den Rest:
        newPoly.addAll(poly1Nodes.subList(poly1Nodes.indexOf(n2), poly1Nodes.size()));

        // Fertig, Liste erstellen
        return new FreePolygon(false, newPoly.toArray(new Node[0]));
    }

    /**
     * Liefert eine Liste mit allen gemeinsamen Nodes zweier Polygone
     * @param poly1 Polygon 1
     * @param poly2 Polygon 2
     * @return eine Liste mit allen gemeinsamen Nodes zweier Polygone
     */
    private static List<Node> intersectingNodes(FreePolygon poly1, FreePolygon poly2) {
        LinkedList<Node> returnList = new LinkedList<Node>();
        for (Node node : poly1.myNodes) {
            if (poly2.myNodes.contains(node)) {
                returnList.add(node);
            }
        }
        return returnList;
    }

    public List<Node> getNodesForDebug() {
        return Collections.unmodifiableList(myNodes);
    }

    /**
     * Registriert den Polygon bei seinen Nodes.
     * Normalerweise macht dies der Konstruktor automatisch (wenn mit true aufgerufen)
     * Sonst kann man es hier nachholen, z.B. wenn man den temporären Polygon behalten möchte.
     */
    public final void registerNodes() {
        for (Node node : myNodes) {
            node.addPolygon(this);
        }
    }

    /**
     * Die Farbe diese Polygons (debug only)
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Überprüft, ob der gefragte Polygon ein Nachbar dieses Feldes ist.
     * Erkennt auch Nachbarn, die nicht als Nachbarn registiert sind (z.B. zur Erstellung der Liste, Vergleich mit temporären etc.)
     * Die Nodes müssen aber wissen, dass sie beide Polygone beinhalten!
     * Sucht nach echten Nachbarn mit geteilter Kante, nicht nur übers Ecke.
     * @param poly der zu Untersuchende Polygon
     * @return true, wenn Nachbar, false wenn nicht.
     */
    public boolean isNeighbor(FreePolygon poly) {
        if (neighbors.contains(poly)) {
            return true;
        } else {
            // Manuelle Suche
            int number = 0;
            for (Node node : this.myNodes) {
                if (poly.myNodes.contains(node)) {
                    number++;
                }
            }
            // Fertig mit der Suche.
            if (number >= 2) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns a List containing all neighbors of this Polygon
     * @return a List containing all neighbors of this Polygon
     */
    public List<FreePolygon> getNeighbors() {
        return Collections.unmodifiableList(neighbors);
    }

    /**
     * Registriert einen Polygon als Nachbar, falls noch nicht registriert
     * @param poly der neue Nachbar
     */
    public void registerNeighbor(FreePolygon poly) {
        if (!neighbors.contains(poly)) {
            neighbors.add(poly);
        }
    }

    @Override
    public String toString() {
        String ret = "Poly: [";
        for (Node node : myNodes) {
            ret += " " + node;
        }
        return ret + "]";
    }

    /**
     * Prüft, ob dieses Polygon konvex oder konkav ist.
     * True heißt konvex.
     * @return true, wenn konvex.
     */
    public boolean isConvex() {
        boolean rechts = false;
        boolean links = false;
        for (int i = 0; i < myNodes.size(); i++) {
            // Die 3 Nodes holen
            Node n1 = myNodes.get(i > 0 ? i - 1 : myNodes.size() - 1);
            Node n2 = myNodes.get(i);
            Node n3 = myNodes.get(i < myNodes.size() - 1 ? i + 1 : 0);
            // Rechts oder Links abbiegen?
            // XY Richtung suchen:
            double vecX = n2.getX() - n1.getX();
            double vecY = n2.getY() - n1.getY();
            // y = mx + c
            double m = vecY / vecX;
            double c = n2.getY() - m * n2.getX();
            double checkY = n3.getX() * m + c;
            if (checkY > n3.getY()) {
                // Drüber
                if (vecX >= 0) {
                    links |= true;
                } else {
                    rechts |= true;
                }
            } else {
                // Drunter
                if (vecX >= 0) {
                    rechts |= true;
                } else {
                    links |= true;
                }
            }
        }

        return rechts ^ links;
    }
}
