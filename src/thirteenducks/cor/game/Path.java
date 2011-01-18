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
package thirteenducks.cor.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Weg ist eine Folge von Feldern, die eine Einheit entlang laufen kann.
 * Enthält Variablen und Mechanismen, um die aktuelle Position der Einheit auf dem Weg zu bestimmen/speichern
 */
public class Path {

    /**
     * Die Position auf der die Bewegung begonnen hat.
     */
    private Position startPos;
    /**
     * Die Position auf der die Bewegung enden soll.
     */
    private Position targetPos;
    /**
     * Der Zeitpunkt, zu dem die Einheit losgelaufen ist.
     */
    private long moveStartTime;
    /**
     * Die Weglänge - Echte Einheiten.
     */
    private double length;
    /**
     * Wurde die Bewegung pausiert? (Pause/Speichermodus)
     */
    private boolean movePaused;
    /**
     * Der Zeitpunkt des Pausierens
     */
    private long movePauseTime;
    /**
     * Wurde die Weglänge bereits berechnet?
     * Wenn true steht auch path zur Verfügung
     */
    private boolean pathComputed;
    /**
     * Die Einheit soll am Ende des Weges in ein Gebäude springen
     */
    private int jumpTo = 0;
    /**
     * Wurde die Sprunganweisung eben erst gesetzt?
     * Die Sprunganweisung überlebt genau einen Bewegungsbefehl, dann wird sie gelöscht.
     */
    private boolean jumpJustSet = false;
    /**
     * Der Index des zuletzt erreichten Wegpunktes
     */
    private int lastWayPoint;
    /**
     * Die Strecke bis zum nächsten Wegpunkt
     */
    private double nextWayPointDist;
    /**
     * Der eigentliche Weg.
     * Muss erst berechnet werden, steht zur Verfügung, wenn pathLengthCalced = true
     */
    private List<PathElement> path;

    /**
     * Berechnet die Länge des Weges dieser Einheit.
     * Die berechnete Weglänge wird in length gespeichert.
     */
    private synchronized void computePath(List<Position> newPath) {
        pathComputed = false;
        length = 0;
        path = new ArrayList<PathElement>();
        path.add(new PathElement(newPath.get(0)));
        for (int i = 1; i < newPath.size(); i++) {
            Position pos = newPath.get(i);
            Position old = newPath.get(i - 1);
            // Richtung berechnen
            int vec = pos.subtract(old).transformToIntVector();
            // Strecke berechnen, mit Pytagoras
            double abschnitt = Math.sqrt(Math.pow(Math.abs(old.getX() - pos.getX()), 2) + Math.pow(Math.abs(old.getX() - pos.getY()), 2));
            length = length + abschnitt;
            path.add(new PathElement(pos, length, vec));
        }
        pathComputed = true;
    }

    /**
     * @return the startPos
     */
    public Position getStartPos() {
        return startPos;
    }

    /**
     * @return the targetPos
     */
    public Position getTargetPos() {
        return targetPos;
    }

    /**
     * Überschreibt den Pfad und lässt die Einheit vom Beginn loslaufen
     * @param newPath der neue Weg
     */
    public synchronized void overwritePath(List<Position> newPath) {
        startPos = newPath.get(0);
        targetPos = newPath.get(newPath.size() - 1);
        lastWayPoint = 0;
        computePath(newPath);
        this.nextWayPointDist = path.get(1).getDistance();
        moveStartTime = System.currentTimeMillis();
    }

    /**
     * Versucht, den Weg "seamless" zu ändern.
     * Da diese Änderung während der Bewegung erfolgt, kann es sein, dass die Einheit noch bis zum nächsten Feld weiter läuft
     * @param newPath
     */
    public synchronized void switchPath(ArrayList<Position> newPath) {
        targetPos = newPath.get(newPath.size() - 1);
        computePath(newPath);
        try {
            if (lastWayPoint + 1 > path.size() - 1) {
                // Automatisch zurückstellen, falls Überschreitung festgestellt.
                lastWayPoint = path.size() - 2;
                System.out.println("WARNING: Setting unit back on Path-Switch, probably causes Pfusch");
            }
        } catch (Exception ex) {
            System.out.println("ERROR: Problems switching Path:");
            ex.printStackTrace();
        }
        nextWayPointDist = path.get(lastWayPoint + 1).getDistance();
    }

    /**
     * Mini-Klasse, um mehrere Werte zu Speichern.
     * Speichert den Wegpunkt, die Strecke bis dort hin und die Richtung bis dort hin.
     */
    class PathElement {

        /**
         * Das eigentliche "Feld" dieses Wegelements
         */
        private Position pos;
        /**
         * Die Distanz vom Anfang des Weges bis hier her.
         */
        private double distance;
        /**
         * Die Richtung vom letzten Feld zu diesem.
         * Im 8-ter System der Grafikengine angegeben.
         */
        private int direction;

        /**
         * Erstellt eine Wegposition ohne Richtung und Entfernung.
         * Für den Anfang von Wegen
         * @param get
         */
        private PathElement(Position get) {
            pos = get;
        }

        /**
         * Erstellt eine reguläre Wegposition mit den angegebenen Parametern.
         * @param pos Position dieses Feldes
         * @param length die Strecke bis zu diesem Feld
         * @param vec die Richtung, aus der man kommt
         */
        private PathElement(Position pos, double length, int vec) {
            this.pos = pos;
            distance = length;
            direction = vec;
        }

        /**
         * @return the pos
         */
        public Position getPos() {
            return pos;
        }

        /**
         * @return the distance
         */
        public double getDistance() {
            return distance;
        }

        /**
         * @return the direction
         */
        public int getDirection() {
            return direction;
        }
    }
}
