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
import java.util.List;

/**
 * Ein Knoten. Zwischen diesen werden Vielecke aufgespannt (FreePolygon)
 * Ein Knoten gehört dabei zu mehreren Polygonen.
 * Mit Hilfe dieser Knoten sucht ein A* den besten Weg.
 */
public class Node {

    /**
     * Diese Listen enthält alle Polygone, auf deren Kanten dieser Polygon liegt oder deren Ecken er markiert.
     */
    private List<FreePolygon> myPolys;
    /**
     * Die absolute x-Koordinate dieses Polygons
     */
    private final double x;
    /**
     * Die absolute y-Koordinate dieses Polygons
     */
    private final double y;

    /**
     * Erzeugt einen neuen Knoten mit den angegebenen Koordinaten.
     * Die Koordinaten dürfen nicht negativ sein, sonst gibts ne Exception!
     * @param x Die X-Koordinate
     * @param y Die Y-Koordinate
     */
    public Node(double x, double y) {
        this.x = x;
        this.y = y;
        myPolys = new ArrayList<FreePolygon>();
    }

    /**
     * Registriert einen Polygon bei diesem Knoten.
     * Der Knoten weiß dann in Zunkunft, dass er auf einer der Kanten dieses Polygons liegt (oder diese als Ecke aufspannt)
     * Polygon darf nicht null sein. (IllegalArgumentException)
     * Wenn dieser Node den Polygon bereits kennt passiert nichts.
     * @param poly der neue Polygon
     */
    public void addPolygon(FreePolygon poly) {
        if (poly == null) {
            throw new IllegalArgumentException("Poly must not be null!");
        }
        if (!myPolys.contains(poly)) {
            myPolys.add(poly);
        }
    }

    /**
     * Löscht einen registrierten Polygon wieder.
     * Sollte dieser Knoten den Polygon gar nicht kennen, passiert nichts.
     * @param poly der alte Polygon
     */
    public void removePoly(FreePolygon poly) {
        myPolys.remove(poly);
    }

    /**
     * Die absolute x-Koordinate dieses Polygons
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * Die absolute y-Koordinate dieses Polygons
     * @return the y
     */
    public double getY() {
        return y;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 53 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Node) {
            Node n = (Node) o;
            // Bei Fließkomma-Vergleichen immer eine Toleranz zulassen, wegen den Rundungsfehlern.
            if (Math.abs(n.x - this.x) < 0.01 && Math.abs(n.y - this.y) < 0.01) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return x + "/" + y;
    }
}
