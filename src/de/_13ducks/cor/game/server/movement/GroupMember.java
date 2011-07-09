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
import java.util.LinkedList;

/**
 * Ein Member einer Gruppe.
 */
public class GroupMember {

    private Moveable mover;
    private LinkedList<SimplePosition> path;
    private LinkedList<SectorChangingEdge> sectorBorders;
    private Node lastStart;

    GroupMember(Moveable mover) {
        this.mover = mover;
        path = new LinkedList<SimplePosition>();
        sectorBorders = new LinkedList<SectorChangingEdge>();
    }

    /**
     * @return the mover
     */
    Moveable getMover() {
        return mover;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.mover != null ? this.mover.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GroupMember) {
            GroupMember g = (GroupMember) o;
            return g.mover.equals(mover);
        }
        return false;
    }

    /**
     * Setzt den Startpunkt für eine Bewegung. Muss für die Sektorgrenzenberechnung
     * bekannt sein.
     */
    void newWay() {
        lastStart = mover.getPrecisePosition().toNode();
        lastStart.addPolygon(mover.getMyPoly());
    }

    /**
     * Fügt einen neuen Wegpunkt für diese Einheit ein.
     * Der Wegpunkt wird an das ende des geplanten Weges gesetzt.
     * Der Wegpunkt wird nur eingefügt, wenn er nicht schon am Ende ist.
     * @param waypoint 
     */
    void addWaypoint(SimplePosition waypoint) {
        if (path.isEmpty() || !path.getLast().equals(waypoint)) {
            // Eventuell den Vorgänger löschen
            if (!path.isEmpty()) {
                SimplePosition last = path.getLast();
                SimplePosition preLast = path.size() > 1 ? path.get(path.size() - 2) : lastStart;
                Vector lastVec = new Vector(last.x() - preLast.x(), last.y() - preLast.y()).normalize();
                Vector nextVec = new Vector(waypoint.x() - last.x(), waypoint.y() - last.y()).normalize();
                // Ersetzen, wenn Position last auf gerader Strecke liegt, also die Vektoren die gleiche Richtung haben
                if (Math.abs(lastVec.x() - nextVec.x()) < 0.01 && Math.abs(lastVec.y() - nextVec.y()) < 0.01) { // Dies ist die normale equals mit mehr Toleranz gegen Rundungsfehler
                    path.removeLast();
                }
                Node n1 = (Node) preLast;
                Node n2 = (Node) last;
                Node n3 = (Node) waypoint;
                Vector ortho1 = new Vector(n2.y() - n1.y(), n1.x() - n2.x()).normalize();
                Vector ortho2 = new Vector(n3.y() - n2.y(), n2.x() - n3.x()).normalize();
                Vector ortho = ortho1.add(ortho2);
                sectorBorders.add(new SectorChangingEdge(n2.toVector().add(ortho).toNode(), n2.toVector().add(ortho.getInverted()).toNode(), commonSector(n1, n2), commonSector(n2, n3)));
            }
            path.add(waypoint);
        }
    }

    /**
     * Löscht alle zukünftigen Wegpunkte.
     */
    void clearWaypoints() {
        path.clear();
    }

    /**
     * Holt den nächsten Wegpunkt dieser Einheit.
     * Lösch ihn anschließend aus der Route.
     * @return 
     */
    SimplePosition popWaypoint() {
        return path.pollFirst();
    }

    /**
     * Wird regelmäßig vom LowLevel aufgerufen, um die nächste Sektorgrenze zu suchen.
     * @return 
     */
    Edge nextSectorBorder() {
        return sectorBorders.element();
    }

    /**
     * Wird vom LowLevel aufgerufen, um anzuzeigen, dass eine weitere Sektorgrenze überschritten wurde.
     */
    void borderCrossed() {
        sectorBorders.pollFirst();
    }

    /**
     * Findet einen Sektor, den beide Knoten gemeinsam haben
     * @param n1 Knoten 1
     * @param n2 Knoten 2
     */
    private FreePolygon commonSector(Node n1, Node n2) {
        for (FreePolygon poly : n1.getPolygons()) {
            if (n2.getPolygons().contains(poly)) {
                return poly;
            }
        }
        return null;
    }
}
