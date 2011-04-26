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
import java.util.List;
import org.newdawn.slick.Color;

/**
 * Ein freies Vieleck. Ein Teil des Movement-Map-Graphen
 */
public class FreePolygon {

    /**
     * Eine Liste mit allen Nodes, die auf einer Kante dieses Polygons liegen oder die eine Ecke darstellen.
     */
    private List<Node> myNodes;
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
     * Registriert sich automatisch bei den Nodes als zugehöriger Polygon. Registriert sich NICHT als Nachbar!
     * @param nodes beliebig viele Nodes, mindestens 3
     */
    public FreePolygon(Node... nodes) {
        if (nodes == null || nodes.length < 3) {
            throw new IllegalArgumentException("At least three nodes requried!");
        }
        myNodes = new ArrayList<Node>();
        neighbors = new ArrayList<FreePolygon>();
        myNodes.addAll(Arrays.asList(nodes));
        for (Node node : myNodes) {
            node.addPolygon(this);
        }

        color = new Color((int) (Math.random() * 265.0), (int) (Math.random() * 265.0), (int) (Math.random() * 265.0), 100);
    }

    public List<Node> getNodesForDebug() {
        return Collections.unmodifiableList(myNodes);
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
}
