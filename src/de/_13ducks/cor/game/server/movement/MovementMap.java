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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.Position;
import de._13ducks.cor.map.CoRMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stellt die Map als (ungerichteten) Graph von konvexen Vielecken (Polygonen) dar.
 */
public class MovementMap {

    /**
     * Privater Konstruktor. CreateMovementMap verwenden!
     */
    private MovementMap(CoRMap map, List<Building> blocked) {
        polys = new ArrayList<FreePolygon>();
        nodes = new ArrayList<Node>();

        try {

            long time = System.currentTimeMillis();

            GeometryFactory fact = new GeometryFactory();

            // Aussen-Shape machen
            Coordinate[] outer = new Coordinate[]{new Coordinate(0, 0), new Coordinate(map.getMapSizeX(), 0), new Coordinate(0, map.getMapSizeY()), new Coordinate(map.getMapSizeX(), map.getMapSizeY()), new Coordinate(0, 0)};
            CoordinateSequence outerSeq = new CoordinateArraySequence(outer);

            ArrayList<LinearRing> holes = new ArrayList<LinearRing>();

            for (Building building : blocked) {
                Position[] vis = building.getVisisbilityPositions();
                // Achtung: Bei Oben, Unten und Rechts wird was hinzugezählt, weil ja nicht der Anfangspunkt des Feldes (mitte links) gemeint ist, sondern z.B. das Ende
                Coordinate[] loch = new Coordinate[]{new Coordinate(vis[0].getX(), vis[0].getY()), new Coordinate(vis[1].getX() + 1, vis[1].getY() - 1), new Coordinate(vis[2].getX() + 1, vis[2].getY() + 1), new Coordinate(vis[3].getX() + 2, vis[3].getY()), new Coordinate(vis[0].getX(), vis[0].getY())};
                CoordinateSequence seq = new CoordinateArraySequence(loch);
                holes.add(new LinearRing(seq, fact));
            }

            DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();

            Polygon poly = fact.createPolygon(new LinearRing(outerSeq, fact), holes.toArray(new LinearRing[0]));

            builder.setSites(poly);

            GeometryCollection col = (GeometryCollection) builder.getTriangles(fact);

            for (int i = 0; i < col.getNumGeometries(); i++) {
                Polygon cPoly = (Polygon) col.getGeometryN(i);
                Coordinate[] coords = cPoly.getCoordinates();
                // Schauen, ob die Nodes schon exisiteren, sonst neue nehmen
                FreePolygon myPolygon = new FreePolygon(true, getKnownOrNew(coords[0].x, coords[0].y), getKnownOrNew(coords[1].x, coords[1].y), getKnownOrNew(coords[2].x, coords[2].y));
                addPoly(myPolygon);
            }

            System.out.println("Movemap calculation took: " + (System.currentTimeMillis() - time) + " ms");

            //

            time = System.currentTimeMillis();



        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    private List<FreePolygon> polys;
    private List<Node> nodes;

    /**
     * Erstellt eine neue MovementMap.
     * Erzeugt die interne Graphenstruktur aus konvexen Polygonen
     * @param map die CoRMap, um die es geht.
     * @param blocked alle Positionen, die nicht verwendet werden können
     * @return die fertige MovementMap
     */
    public static MovementMap createMovementMap(CoRMap map, List<Building> blocked) {
        MovementMap moveMap = new MovementMap(map, blocked);
        return moveMap;
    }

    /**
     * Checkt, ob ein Polygon zur Liste hinzugefügt werden darf, verwaltet Nachbarschaftsbeziehungen und fügt ihn schließlich hinzu
     * @param poly ein neuer Polygon
     */
    private void addPoly(FreePolygon poly) {
        //@TODO: Auf Löcher testen
        // Nachbarn suchen
        for (FreePolygon freePoly : polys) {
            // Als Nachbar eintragen?
            if (freePoly.isNeighbor(poly)) {
                freePoly.registerNeighbor(poly);
                poly.registerNeighbor(freePoly);
            }
        }
        // Jetzt adden
        polys.add(poly);
        System.out.println("Convex: " + poly.isConvex());
    }

    /**
     * Schaut nach, ob der Knoten bereits bekannt ist. Wenn nicht, wird ein neuer angelegt.
     * @param x Die X-Koordinate des zu suchenden Knoten
     * @param y Die Y-Koordinate des zu suchenden Knoten
     * @return Der neue Knoten, entweder aus der Datenbank oder ein ganz neuer
     */
    private Node getKnownOrNew(double x, double y) {
        int index = nodes.indexOf(new Node(x, y));
        Node newnode = null;
        if (index != -1) {
            newnode = nodes.get(index);
        } else {
            newnode = new Node(x, y);
        }
        nodes.add(newnode);
        return newnode;
    }

    public List<FreePolygon> getPolysForDebug() {
        return Collections.unmodifiableList(polys);
    }
}
