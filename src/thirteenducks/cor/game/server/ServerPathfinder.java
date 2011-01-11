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
// Wegfindung
// von 2nd Calc
package thirteenducks.cor.game.server;

import thirteenducks.cor.map.CoRMapElement.collision;
import java.util.*;
import org.apache.commons.collections.buffer.PriorityBuffer;
import thirteenducks.cor.game.Position;

public class ServerPathfinder {

    ServerCore.InnerServer rgi;
    protected PriorityBuffer open = new PriorityBuffer();      // Liste für entdeckte Felder
    protected LinkedHashSet<Position> containopen = new LinkedHashSet<Position>();             // Auch für entdeckte Felder, hiermit kann viel schneller festgestellt werden, ob ein bestimmtes Feld schon enthalten ist.
    protected LinkedHashSet<Position> closed = new LinkedHashSet<Position>();    // Liste für fertig bearbeitete Felder
    private Position[][] RogP;

    public ServerPathfinder(ServerCore.InnerServer inner) {
        rgi = inner;
    }

    public synchronized ArrayList<Position> findPath(Position start, Position ziel, int playerId, boolean allowDifferentTarget) {

        if (start == null || ziel == null) {
            System.out.println("FixMe: SPathfinder, irregular call: " + start + "-->" + ziel);
            return null;
        }

        int cost_t = 0;    //Movement Kosten (gerade 5, diagonal 7, wird später festgelegt)
        int sizeofmapX = rgi.netmap.getMapSizeX();   //die Größe der Map als Variable speichern
        int sizeofmapY = rgi.netmap.getMapSizeY();

        open.clear();   //Listen löschen
        containopen.clear();
        closed.clear();

        RogP = new Position[sizeofmapX][sizeofmapY];
        for (int x = 0; x < sizeofmapX; x = x + 2) {  //alle vorhandenen Felder der Karte
            for (int y = 0; y < sizeofmapY; y = y + 2) {
                RogP[x][y] = new Position(x, y);
            }
        }
        for (int x = 1; x < sizeofmapX; x = x + 2) {
            for (int y = 1; y < sizeofmapY; y = y + 2) {
                RogP[x][y] = new Position(x, y);
            }
        }
        start.cost = 0;   //Kosten für das Startfeld (von dem aus berechnet wird) sind natürlich 0
        open.add(start);  //Startfeld in die openlist
        containopen.add(start);
        ziel.parent = null;    //"Vorgängerfeld" vom Zielfeld noch nicht bekannt

        if (rgi.netmap.getCollision(ziel.X, ziel.Y) == collision.blocked || rgi.netmap.checkFieldReservation(ziel.X, ziel.Y)) {
            if (allowDifferentTarget) {
                ziel = ziel.aroundMe(1, rgi);
            } else {
                return null;
            }
        }

        for (int j = 0; j < 40000; j++) {		//Anzahl der maximalen Durchläufe, bis Wegfindung aufgibt

            if (open.isEmpty()) {   //Abbruch, wenn openlist leer ist => es gibt keinen Weg
                return null;
            }

            // Sortieren nicht mehr nötig, PriorityBuffer bewahrt die Felder in der Reihenfolge ihrer Priority - also dem F-Wert auf
            Position current = (Position) open.remove();		//der Eintrag aus der openlist mit dem niedrigesten F-Wert rausholen und gleich löschen
            containopen.remove(current);
            if (current.equals(ziel)) {	//Abbruch, weil Weg von Start nach Ziel gefunden wurde
                ziel.parent = current.parent;   //"Vorgängerfeld" von Ziel bekannt
                break;
            }

            // Aus der open wurde current bereits gelöscht, jetzt in die closed verschieben
            closed.add(current);

            for (int x = -2; x < 3; x++) {			//Die 8 Nachbarfelder suchen
                for (int y = -2; y < 3; y++) {
                    if (Math.abs(x) + Math.abs(y) != 2) {
                        continue;
                    }

                    int nx = x + current.X;		//x und y Wert fuer aktuelles Nachbarfeld
                    int ny = y + current.Y;

                    if ((nx > 0) && (ny > 0) && (nx < sizeofmapX) && (ny < sizeofmapY)) {
                        if (!rgi.netmap.isGroundCollidingForUnit(nx, ny, playerId)) {		//überprüfen, ob Feld begehbar ist und innerhalb der Map ist

                            Position neighbour = RogP[nx][ny];

                            if (closed.contains(neighbour)) {		//Felder in ClosedList müssen nicht überprüft werden
                                continue;
                            }

                            if (neighbour.X == current.X || neighbour.Y == current.Y) { //-> diagonal (über die Ecken)
                                cost_t = 7; //Kosten für diagonale Bewegung

                                int diax = 0;
                                int diay = 0;
                                int dia2x = 0;
                                int dia2y = 0;

                                if (neighbour.X == current.X) {	// Zwischenfelder für horizontale diagonale Bewegung (haha)
                                    diay = (neighbour.Y + current.Y) / 2;
                                    dia2y = (neighbour.Y + current.Y) / 2;
                                    diax = neighbour.X - 1;
                                    dia2x = neighbour.X + 1;
                                } else {
                                    diax = (neighbour.X + current.X) / 2;	// Zwischenfelder für vertikale Bewegung
                                    dia2x = (neighbour.X + current.X) / 2;
                                    diay = neighbour.Y - 1;
                                    dia2y = neighbour.Y + 1;
                                }
                                if (rgi.netmap.isGroundCollidingForUnit(diax, diay, playerId) || rgi.netmap.isGroundCollidingForUnit(dia2x, dia2y, playerId)) {
                                    continue;	//abbrechen, wenn Zwischenfelder blockiert sind (-> keine Ecken schneiden)
                                }

                            } else {
                                cost_t = 5; //Kosten für gerades Nachbarfeld
                            }

                            if (containopen.contains(neighbour)) {         //Wenn sich das Feld in der openlist befindet, muss berechnet werden, ob es einen kürzeren Weg gibt

                                if (current.cost + cost_t < neighbour.cost) {		//kürzerer Weg gefunden?

                                    neighbour.cost = current.cost + cost_t;         //-> Wegkosten neu berechnen
                                    neighbour.valF = neighbour.cost + neighbour.heuristic;  //F-Wert, besteht aus Wegkosten vom Start + Luftlinie zum Ziel
                                    neighbour.parent = current; //aktuelles Feld wird zum Vorgängerfeld
                                }
                            } else {
                                neighbour.cost = current.cost + cost_t;
                                neighbour.heuristic = (Math.abs((ziel.X - neighbour.X)) + Math.abs((ziel.Y - neighbour.Y))) * 3;	// geschätzte Distanz zum Ziel
                                //Die Zahl am Ende der Berechnung ist der Aufwand der Wegsuche
                                //5 ist schnell, 4 normal, 3 dauert lange

                                neighbour.parent = current;						// Parent ist die RogPosition, von dem der aktuelle entdeckt wurde
                                neighbour.valF = neighbour.cost + neighbour.heuristic;  //F-Wert, besteht aus Wegkosten vom Start aus + Luftlinie zum Ziel
                                open.add(neighbour);    // in openlist hinzufügen
                                containopen.add(neighbour);
                            }
                        }
                    }
                }
            }
        }

        if (ziel.parent == null) {		//kein Weg gefunden
            return null;
        }

        ArrayList<Position> pathrev = new ArrayList();   //Pfad aus parents erstellen, von Ziel nach Start
        Position target = ziel;
        while (!target.equals(start)) {
            pathrev.add(RogP[target.X][target.Y]);
            target = target.parent;
        }
        pathrev.add(start);

        ArrayList<Position> path = new ArrayList();	//Pfad umkehren, sodass er von Start nach Ziel ist
        for (int k = pathrev.size() - 1; k >= 0; k--) {
            path.add(pathrev.get(k));
        }

        return path;					//Pfad zurückgeben
    }
}
