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
import java.util.List;

/**
 * Ein Member einer Gruppe.
 */
public class GroupMember {

    private Moveable mover;
    private LinkedList<SimplePosition> path;
    private LinkedList<DiversionWaypoint> diversion;
    private LinkedList<SectorChangingEdge> sectorBorders;
    private Node lastStart;
    private FreePolygon lastPoly;
    private SimplePosition lastTarget;
    /**
     * Der letzte Versuch eine Umleitung zu berechnen
     */
    private ObstaclePattern lastObstPattern;
    /**
     * Der was war beim gespeicherten Pattern das primärziel?
     */
    private Moveable lastPrimObstacle;
    /**
     * Hat es geklappt?
     */
    private boolean lastObstacleTry;
    
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
        lastPoly = null;
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
                    // lastStart muss auch geändert werden (wichtig für Fall size() == 1). Sonst funktioniert commonSector nicht.
                    lastStart = (Node) path.removeLast();
                }
                Node n1 = (Node) preLast;
                Node n2 = (Node) last;
                Node n3 = (Node) waypoint;
                Vector ortho1 = new Vector(n2.y() - n1.y(), n1.x() - n2.x()).normalize();
                Vector ortho2 = new Vector(n3.y() - n2.y(), n2.x() - n3.x()).normalize();
                Vector ortho = ortho1.add(ortho2);
                FreePolygon nextPoly = commonSector(n2, n3);
                sectorBorders.add(new SectorChangingEdge(n2.toVector().add(ortho).toNode(), n2.toVector().add(ortho.getInverted()).toNode(), lastPoly, nextPoly));
                lastPoly = nextPoly;
            } else {
                lastPoly = commonSector(lastStart, (Node) waypoint);
                if (lastPoly == null) {
                    // Sonderbehandlung, kann passieren, wenn Einheiten ungünstig auf einer Kante startet
                    // Erstmal nur eine Warnmeldung und kein Spezialfall, eventuell wird dieser Fehler durch das neue Cell-System verhindert.
                    System.out.println("[WARN][MOVECALC]: Path calculation problem!");
                }
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
        lastTarget = path.pollFirst();
        return lastTarget;
    }

    /**
     * Wird regelmäßig vom LowLevel aufgerufen, um die nächste Sektorgrenze zu suchen.
     * @return 
     */
    SectorChangingEdge nextSectorBorder() {
        return sectorBorders.peekFirst();
    }

    /**
     * Wird vom LowLevel aufgerufen, um anzuzeigen, dass eine weitere Sektorgrenze überschritten wurde.
     */
    SectorChangingEdge borderCrossed() {
        return sectorBorders.pollFirst();
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

    /**
     * Eine LowLevelManager hat sein Wegziel erreicht und will wissen, wie die Route weitergeht
     * Gibt false zurück wenns nicht weitergeht und liefert true zurück, wenns weiter geht und das
     * neue Ziel schon gesetzt wurde.
     * @return true, wenn neues Ziel gesetzt sonst false
     */
    public boolean reachedTarget(Moveable mover) {
        if (diversion != null && !diversion.isEmpty()) {
            DiversionWaypoint nextPoint = diversion.pollFirst();
            mover.getLowLevelManager().setTargetVector(nextPoint.pos, nextPoint.arc, nextPoint.arcDirection, nextPoint.arcCenter);
            return true;
        } else {
            SimplePosition nextPoint = popWaypoint();
            if (nextPoint != null) {
                mover.getLowLevelManager().setTargetVector(nextPoint, false, false, null);
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Setzt eine Umleitung.
     * Löscht eine alte Umleitung, falls vorhanden.
     * Wird dem Mover befehlen, sofort zur ersten Position der Umleitung zu laufen.
     * Der Mover muss sich zurzeit auf dem Startpunkt der Route befinden!
     * @param edges 
     */
    void setDiversion(List<SubSectorEdge> edges, Moveable mover) {
        diversion = new LinkedList<DiversionWaypoint>();
        SubSectorNode current = new SubSectorNode(mover.getPrecisePosition().x(), mover.getPrecisePosition().y()); 
        // Startpunkt muss nicht in Liste!
        for (SubSectorEdge edge : edges) {
            SubSectorNode target = edge.getOther(current);
            if (edge.isArc()) {
                diversion.add(new DiversionWaypoint(new Vector(target.getX(), target.getY()),
                        edge.isFrom(current),
                        new Vector(edge.getObst().getX(), edge.getObst().getY())));
            } else {
                diversion.add(new DiversionWaypoint(new Vector(target.getX(), target.getY())));
            }
            current = target;
        }
        // Loslaufen
        reachedTarget(mover);
    }

    /**
     * Liefert das zuletzt eingestellte Ziel, das nicht Teil einer Umleitung war.
     * @return das zuletzt eingestellte Ziel, das nicht Teil einer Umleitung war.
     */
    SimplePosition lastRealWaypoint() {
        return lastTarget;
    }

    /**
     * @return the lastObst
     */
    ObstaclePattern getLastObst() {
        return lastObstPattern;
    }

    /**
     * @param lastObst the lastObst to set
     */
    void saveDiversion(ObstaclePattern lastObst, Moveable primObstacle, boolean result) {
        this.lastObstPattern = lastObst;
        this.lastPrimObstacle = primObstacle;
        this.lastObstacleTry = result;
    }

    /**
     * Aufrufen, um die Umleitung abzubrechen und wieder den direkten Weg zu versuchen
     */
    void nextNonDiversion(Moveable mover) {
        diversion = null;
        reachedTarget(mover);
    }

    private class DiversionWaypoint {

        /**
         * Die eigentliche Position dieses Wegpunkts
         */
        private SimplePosition pos;
        /**
         * Soll diese Position auf einer Kurve angesteuert werden?
         */
        private boolean arc;
        /**
         * Die Richtung der Kreisbewegung
         */
        private boolean arcDirection;
        /**
         * Das Zentrum der Kreisbewegung
         */
        private SimplePosition arcCenter;

        private DiversionWaypoint(SimplePosition pos) {
            this.pos = pos;
        }

        private DiversionWaypoint(SimplePosition pos, boolean arcDirection, SimplePosition arcCenter) {
            this(pos);
            this.arc = true;
            this.arcDirection = arcDirection;
            this.arcCenter = arcCenter;
        }
    }
}
