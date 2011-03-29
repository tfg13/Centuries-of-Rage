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
/**
 * Repräsentiert ein Feld im 2-Dimensionalen Raum.
 * Arbeitet mit dem bekannten Rauten-nur jedes zweite Feld-System.
 *
 **/
package thirteenducks.cor.game;

//import elementcorp.rog.RogMapElement.collision;
import thirteenducks.cor.game.server.ServerCore;
import java.util.ArrayList;
import java.io.*;

public class Position implements Comparable<Position>, Serializable, Cloneable {

    public static final int AROUNDME_CIRCMODE_FULL_CIRCLE = 1;
    public static final int AROUNDME_CIRCMODE_HALF_CIRCLE = 2;
    public static final int AROUNDME_COLMODE_GROUNDTARGET = 10;
    public static final int AROUNDME_COLMODE_GROUNDPATH = 11;
    public static final int AROUNDME_COLMODE_GROUNDPATHPLANNING = 12;
    protected int X;                                    //X-Koodrinate
    protected int Y;
    private int cost;
    private int heuristic;
    private int valF;
    private Position parent;                          //Das Feld von dem man kommt

    //Konstruktor
    public Position(int x, int y) {
        X = x;
        Y = y;
    }

    /**
     * Überprüft, ob eine Position erlaubt ist, also legal Koordinaten hat.
     * Checkt nur ob die Position überhaupt legal ist, Fragen bezüglich der Mapgrenzen beantwortet das Mapmodul
     * @return
     */
    public boolean valid() {
        return (X % 2 == Y % 2);
    }

    //Gibt die 8 direkten Nachbarfelder zurück:
    //[Tobi]: Braucht das überhaupt irgenjemand?
    public ArrayList<Position> neighbors() {
        ArrayList<Position> temp = new ArrayList();
        temp.clear();


        temp.add(new Position(this.X + 1, this.Y + 1));     //rechts unten

        temp.add(new Position(this.X - 1, this.Y - 1));     //links oben

        temp.add(new Position(this.X + 1, this.Y - 1));     //rechts oben

        temp.add(new Position(this.X - 1, this.Y + 1));     //links unten

        temp.add(new Position(this.X - 2, this.Y));

        temp.add(new Position(this.X + 2, this.Y));

        temp.add(new Position(this.X, this.Y + 2));

        temp.add(new Position(this.X, this.Y - 2));

        return temp;
    }

    /**
     * Gibt ein möglichst nahes Nachbarfeld zurück, das keine Kollision hat, also nicht blockiert UND nicht besetzt ist
     *
     *
     * @param i Das wievielte Nachbarfeld gesucht ist - Im Zweifelsfall 1, 0 kann auch sich selbst zurückgeben
     * @return Das Nachbarfeld oder dieses selbst, wenn i = 0 war.
     */
    public Position aroundMe(int i, ServerCore.InnerServer inner, GameObject forObject) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (!inner.netmap.isGroundColliding(this.X, this.Y, forObject) && !inner.netmap.checkFieldReservation(this.X, this.Y)) {
                return this;
            } else {
                i = 1;
            }
        }
        // Das i-te freie Nachbarfeld suchen
        if (i < 1) {
            return null;
        }
        int kreis = 1;
        int limit = 1000;
        // Startfeld des Kreises:
        Position kreismember = new Position(this.X, this.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            limit--;
            if (limit == 0) {
                System.out.println("Warning: FixMe: AroundMe reached its limit for " + this + " aborting.");
                return null;
            }
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.Y -= 2;
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            } else {
                // Sprung in den nächsten Kreis
                kreismember.X--;
                kreismember.Y -= 3;
                k = 0;
                i = i - (kreis * 8);
                kreis++;
            }
            if (inner.netmap.isGroundColliding(kreismember.X, kreismember.Y, forObject) || inner.netmap.checkFieldReservation(kreismember.X, kreismember.Y)) {
                i++;
            }
        }
        return kreismember;


    }

    /**
     * Gibt ein möglichst nahes Nachbarfeld zurück, das keine Kollision hat, also nicht blockiert UND nicht besetzt ist
     *
     *
     * @param i Das wievielte Nachbarfeld gesucht ist - Im Zweifelsfall 1, 0 kann auch sich selbst zurückgeben
     * @return Das Nachbarfeld oder dieses selbst, wenn i = 0 war.
     */
    public Position aroundMe(int i, ServerCore.InnerServer inner, int limit, GameObject forObject) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (!inner.netmap.isGroundColliding(this.X, this.Y, forObject) && !inner.netmap.checkFieldReservation(this.X, this.Y)) {
                return this;
            } else {
                i = 1;
            }
        }
        // Das i-te freie Nachbarfeld suchen
        if (i < 1) {
            return null;
        }
        int kreis = 1;
        // Startfeld des Kreises:
        Position kreismember = new Position(this.X, this.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            limit--;
            if (limit == 0) {
                return null;
            }
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.Y -= 2;
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            } else {
                // Sprung in den nächsten Kreis
                kreismember.X--;
                kreismember.Y -= 3;
                k = 0;
                i = i - (kreis * 8);
                kreis++;
            }
            if (inner.netmap.isGroundColliding(kreismember.X, kreismember.Y, forObject) || inner.netmap.checkFieldReservation(kreismember.X, kreismember.Y)) {
                i++;
            }
        }
        return kreismember;


    }

    /**
     * Gibt ein möglichst nahes Nachbarfeld zurück, das keine Kollision hat, also nicht blockiert ist UND nicht besetzt ist.
     * Die
     *
     * @param i Das wievielte Nachbarfeld gesucht ist - Im Zweifelsfall 1, 0 wenn auch das Feld selber Antwort sein kann
     * @param vector Die Richtung, in der die Einheiten um das Ziel platziert werden sollen - muss als Vector angegeben werden
     * @return Das Zielfeld, im normalfall ein freies Nachbarfeld, eventuell dieses selbst (bei i = 0)
     */
    public Position aroundMe(int i, ServerCore.InnerServer inner, Position vector, GameObject forObject) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (!inner.netmap.isGroundColliding(this.X, this.Y, forObject) && !inner.netmap.checkFieldReservation(this.X, this.Y)) {
                return this;
            } else {
                i = 1;
            }
        }
        // Werte checken
        if (vector.X < -1 || vector.X > 1 || vector.Y < -1 || vector.Y > 1) {
            return null;
        }
        // Das i-te freie Nachbarfeld suchen
        if (i < 1) {
            return null;
        }
        int kreis = 1;
        int limit = 1000;
        // Startfeld des Kreises:
        Position kreismember = new Position(this.X, this.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            limit--;
            if (limit == 0) {
                System.out.println("Warning: FixMe: AroundMe reached its limit for " + this + " vector " + vector);
                return null;
            }
            // Es gibt vier Schritte, welcher ist als nächster dran?
            int nxt = transVec(vector, k, kreis);
            if (k == 0) {
                // Zum allerersten Feld springen - hängt vom Vector ab
                if (vector.X == 0) {
                    if (vector.Y == -1) {
                        kreismember.Y -= 2;
                    } else {
                        kreismember.Y += 2;
                    }
                } else if (vector.X == 1) {
                    if (vector.Y == -1) {
                        kreismember.X++;
                        kreismember.Y--;
                    } else if (vector.Y == 0) {
                        kreismember.X += 2;
                    } else {
                        kreismember.X++;
                        kreismember.Y++;
                    }
                } else if (vector.X == -1) {
                    if (vector.Y == -1) {
                        kreismember.X--;
                        kreismember.Y--;
                    } else if (vector.Y == 0) {
                        kreismember.X -= 2;
                    } else {
                        kreismember.X--;
                        kreismember.Y++;
                    }
                }
            } else if (nxt != 0 && nxt <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (nxt != 0 && nxt <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (nxt != 0 && nxt <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (nxt != 0 && nxt <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            }
            if (k > ((kreis * 7) + kreis - 1)) {
                // Sprung in den nächsten Kreis - Richtung hängt vom Vector ab
                if (vector.X == 0) {
                    if (vector.Y == -1) {
                        kreismember.X--;
                        kreismember.Y -= 3;
                    } else {
                        kreismember.X++;
                        kreismember.Y += 3;
                    }
                } else if (vector.X == 1) {
                    if (vector.Y == -1) {
                        kreismember.Y -= 2;
                    } else if (vector.Y == 0) {
                        kreismember.X += 3;
                        kreismember.Y--;
                    } else {
                        kreismember.X += 2;
                    }
                } else if (vector.X == -1) {
                    if (vector.Y == -1) {
                        kreismember.X -= 2;
                    } else if (vector.Y == 0) {
                        kreismember.X -= 3;
                        kreismember.Y++;
                    } else {
                        kreismember.Y += 2;
                    }
                }
                k = 0;
                i = i - (kreis * 8);
                kreis++;
            }
            if (inner.netmap.isGroundColliding(kreismember.X, kreismember.Y, forObject) || inner.netmap.checkFieldReservation(kreismember.X, kreismember.Y)) {
                i++;
            }
        }
        return kreismember;
    }

    /**
     * Die ultimative aroundMe-Inkarnation ist da!
     * Sucht freie Flächen kreisförmig um eine bestimmte Position.
     * Arbeitet mit Zuordungspositionen.
     * Bietet 2 Modi: ganze und halbe Kreise.
     * Bei ganzen Kreisen wird ringsherum gesucht,
     * bei halben nur im halbkreis vom Mittelpunkt in die Richtung, in die der Vektor zeigt.
     * Bei ganzen Kreisen darf der Vektor null sein.
     * Wirft IllegalArgumentExceptions bei falscher Benutzung.
     * Muss auf einer gültigen Position aufgerufen werden.
     * @param vector Die Richtung, in der zuerst gesucht werden soll (zwingend bei Halbkreisen, sonst optional)
     * @param requiredSpace Ein GameObject, für das Platz gesucht wird
     * @param selfCheck Die eigene Position auch überprüfen?
     * @param limit Die maximale Iterations-Zahl. 0 angeben für Hardcode-default (normalerweise ausreichend)
     * @param aroundMode Der Kreismodus: Ganze oder halbe Kreise
     * @param colMode Der Kollisionsmodus: GroundTarget, GroundPath, GroundPathPlanning
     * @param reservation Reservierungen überprüfen?
     * @param rgi Der inner Server für Kollisionsabfragen
     * @return Zuordnungsposition, fall gefunden, sonst null
     */
    public Position aroundMePlus(Position vector, GameObject requiredSpace, boolean selfCheck, int limit, int aroundMode, int colMode, boolean reservation, ServerCore.InnerServer rgi) {
        if (requiredSpace == null) {
            throw new IllegalArgumentException("GameObject must not be null!");
        }
        if (aroundMode != AROUNDME_CIRCMODE_FULL_CIRCLE && aroundMode != AROUNDME_CIRCMODE_HALF_CIRCLE) {
            throw new IllegalArgumentException("Illegal Circle-mode selected!");
        }
        if (colMode != AROUNDME_COLMODE_GROUNDPATH && colMode != AROUNDME_COLMODE_GROUNDPATHPLANNING && colMode != AROUNDME_COLMODE_GROUNDTARGET) {
            throw new IllegalArgumentException("Illegal Collision-mode selected!");
        }
        if (rgi == null) {
            throw new IllegalArgumentException("InnerServer must not be null!");
        }
        if (vector == null && aroundMode == AROUNDME_CIRCMODE_HALF_CIRCLE) {
            throw new IllegalArgumentException("Vector must no be null if half-circle-mode is selected!");
        }
        if (!this.valid()) {
            throw new IllegalArgumentException("aroundMePlus must be called on a valid position!");
        }
        // Werte checken
        if (vector.X < -1 || vector.X > 1 || vector.Y < -1 || vector.Y > 1) {
            throw new IllegalArgumentException("Invalid vector!");
        }

        // Compute arguments
        if (limit <= 0) {
            limit = 10000; // Defaulting
        }
        if (vector == null) {
            vector = new Position(0, -1);
        }

        // Self-Check if requested
        if (selfCheck) {
            if (!checkCol(X, Y, requiredSpace, rgi, colMode, reservation)) {
                return this;
            }
        }
        int i = 1;
        int kreis = 1;
        // Startfeld des Kreises:
        Position kreismember = new Position(this.X, this.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            limit--;
            if (limit == 0) {
                System.out.println("Warning: FixMe: AroundMe reached its limit for " + this + " vector " + vector);
                return null;
            }
            // Es gibt vier Schritte, welcher ist als nächster dran?
            int nxt = transVec(vector, k, kreis);
            if (k == 0) {
                // Zum allerersten Feld springen - hängt vom Vector ab
                if (vector.X == 0) {
                    if (vector.Y == -1) {
                        kreismember.Y -= 2;
                    } else {
                        kreismember.Y += 2;
                    }
                } else if (vector.X == 1) {
                    if (vector.Y == -1) {
                        kreismember.X++;
                        kreismember.Y--;
                    } else if (vector.Y == 0) {
                        kreismember.X += 2;
                    } else {
                        kreismember.X++;
                        kreismember.Y++;
                    }
                } else if (vector.X == -1) {
                    if (vector.Y == -1) {
                        kreismember.X--;
                        kreismember.Y--;
                    } else if (vector.Y == 0) {
                        kreismember.X -= 2;
                    } else {
                        kreismember.X--;
                        kreismember.Y++;
                    }
                }
            } else if (nxt != 0 && nxt <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (nxt != 0 && nxt <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (nxt != 0 && nxt <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (nxt != 0 && nxt <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            }
            if (k > ((kreis * 7) + kreis - 1)) {
                // Sprung in den nächsten Kreis - Richtung hängt vom Vector ab
                if (vector.X == 0) {
                    if (vector.Y == -1) {
                        kreismember.X--;
                        kreismember.Y -= 3;
                    } else {
                        kreismember.X++;
                        kreismember.Y += 3;
                    }
                } else if (vector.X == 1) {
                    if (vector.Y == -1) {
                        kreismember.Y -= 2;
                    } else if (vector.Y == 0) {
                        kreismember.X += 3;
                        kreismember.Y--;
                    } else {
                        kreismember.X += 2;
                    }
                } else if (vector.X == -1) {
                    if (vector.Y == -1) {
                        kreismember.X -= 2;
                    } else if (vector.Y == 0) {
                        kreismember.X -= 3;
                        kreismember.Y++;
                    } else {
                        kreismember.Y += 2;
                    }
                }
                k = 0;
                i = i - (kreis * 8);
                kreis++;
            }
            if (aroundMode == AROUNDME_CIRCMODE_HALF_CIRCLE && k >= kreis * 2 + 1 && k < (kreis * 6)) {
                i++;
            } else {
                if (checkCol(kreismember.getX(), kreismember.getY(), requiredSpace, rgi, colMode, reservation)) {
                    i++;
                }
            }
        }
        return kreismember;
    }

    /**
     * Checkt die Kollision auf dem gegebenen Feld &
     * auf allen Feldern, die sich auf Grund der Fläche des Objekts übergeben
     * (Gegebenes Feld wird als Zuordnungsposition verwendet)
     * @param x x-koordinate
     * @param y y-koordinate
     * @param forObject das objekt, für das geprüft wird
     * @param rgi servercore
     * @param mode der collisions-check-modus
     * @param checkReserved ob die reservierung überprüft wird
     * @return true, wenn NICHT FREI (=Hinderniss)
     */
    private boolean checkCol(int x, int y, GameObject forObject, ServerCore.InnerServer rgi, int mode, boolean checkReserved) {
        boolean result = false;
        Position diff = forObject.getMainPosition().subtract(new Position(x, y));
        for (Position pos : forObject.getPositions()) {
            pos = pos.subtract(diff);
            switch (mode) {
                case AROUNDME_COLMODE_GROUNDPATH:
                    result |= rgi.netmap.isGroundCollidingForMove(pos.getX(), pos.getY(), forObject);
                    break;
                case AROUNDME_COLMODE_GROUNDPATHPLANNING:
                    result |= rgi.netmap.isGroundCollidingForMovePlanning(pos, forObject);
                    break;
                case AROUNDME_COLMODE_GROUNDTARGET:
                    result |= rgi.netmap.isGroundColliding(pos, forObject);
                    break;
            }
            if (checkReserved) {
                result |= rgi.netmap.checkFieldReservation(pos);
            }
        }
        return result;
    }

    private int transVec(Position vector, int k, int kreis) {
        // Spezialfunktion für die gerichtete AroundMe
        // Bei Sprungbedingung nicht verschieben
        if (k > ((kreis * 7) + kreis - 1)) {
            return 0;
        }
        // Verschiebung gemäß dem Vector feststellen
        int diff = 0;
        if (vector.X == 0) {
            if (vector.Y == 1) {
                diff = 4 * kreis;
            }
        } else if (vector.X == -1) {
            if (vector.Y == -1) {
                diff = 1 * kreis;
            } else if (vector.Y == 0) {
                diff = 2 * kreis;
            } else if (vector.Y == 1) {
                diff = 3 * kreis;
            }
        } else if (vector.X == 1) {
            if (vector.Y == -1) {
                diff = 7 * kreis;
            } else if (vector.Y == 0) {
                diff = 6 * kreis;
            } else if (vector.Y == 1) {
                diff = 5 * kreis;
            }
        }
        // Wert könnte zu hoch sein - Maximalwert besimmen
        int max = 7 * kreis;
        int real = k + diff;
        if (real == (max + kreis)) {
            real -= 1;
        } else if (real > (max + kreis)) {
            real -= (max + kreis);
        }
        // Fehlenden Sprung erkennen und überbrücken

        return real;
    }

    /**
     * Zieht die mitgegebene Position von der Position ab, auf der es aufgerufen wird
     */
    public Position subtract(Position pos) {
        return new Position(this.X - pos.X, this.Y - pos.Y);
    }

    /**
     * Addiert eine Position zu dieser hier
     */
    public Position add(Position pos) {
        return new Position(this.X + pos.X, this.Y + pos.Y);
    }

    public boolean equals(Position p2) {
        if (p2 == null) {
            return false;
        }
        if (X != p2.X) {
            return false;
        }
        if (Y != p2.Y) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object p2) {
        if (p2.getClass().equals(this.getClass())) {
            Position pos = (Position) p2;
            if (X != pos.X) {
                return false;
            }
            if (Y != pos.Y) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.X;
        hash = 89 * hash + this.Y;
        return hash;
    }

    @Override
    public int compareTo(Position f) {
        if (f.getValF() > this.getValF()) {
            return -1;
        } else if (f.getValF() < getValF()) {
            return 1;
        } else {
            return 0;
        }
    }

    public double getDistance(Position pos) {
        // Gibt die Entfernung zwischen dem übergebenen Punkt und diesem hier zurück
        return Math.sqrt(Math.pow(pos.X - this.X, 2) + Math.pow(pos.Y - this.Y, 2));
    }

    public Position transformToVector() {
        // Liefert den RogPosition zurück, in Vectordarstellung, maximale Werte 1 bis -1
        Position newvec = new Position(0, 0);
        if (this.X > 0) {
            newvec.X = 1;
        } else if (this.X < 0) {
            newvec.X = -1;
        } else {
            newvec.X = 0;
        }
        if (this.Y > 0) {
            newvec.Y = 1;
        } else if (this.Y < 0) {
            newvec.Y = -1;
        } else {
            newvec.Y = 0;
        }
        return newvec;
    }

    /**
     * Gibt eine 4 Wege-Vector zurück
     * Die Vectordarstellung wird hier anteilig bestimmt.
     * Eine wirklich gute Darstellung der Wirklichkeit gibt nur eine Kombination auf beidne ToVector Methoden
     *
     * Der 4-Wege Vektor kennt nur 4 Richtungen, keine 8 wie transformToVector()
     *
     */
    public Position transformToStraightVector() {
        // Anteilsmäßig runden
        if (Math.abs(this.X) > Math.abs(this.Y)) {
            // Nach X runden
            if (this.X > 0) {
                return new Position(1, 0);
            } else if (this.X == 0) {
                return new Position(0, 0);
            } else {
                return new Position(-1, 0);
            }
        } else {
            // Y Runden
            if (this.Y > 0) {
                return new Position(0, 1);
            } else if (this.Y == 0) {
                return new Position(0, 0);
            } else {
                return new Position(0, -1);
            }
        }
    }

    /**
     * Liefert einen 4-Wege Vektor in Diagonaldarstellung zurück
     * @return RogPosition (1,1)(1,-1)(-1,1) oder (-1,-1) bzw. (0,0) wenns nicht geklappt hat.
     */
    public Position transformToDiagonalVector() {
        // Anteilsmäßig runden
        if (this.X > 0 && this.Y > 0) {
            return new Position(1, 1);
        } else if (this.X > 0 && this.Y < 0) {
            return new Position(1, -1);
        } else if (this.X < 0 && this.Y > 0) {
            return new Position(-1, 1);
        } else if (this.X < 0 && this.Y < 0) {
            return new Position(-1, -1);
        }
        return new Position(0, 0);
    }

    /**
     * Wandelt diese RogPosition in einen int-Vector um
     * Int-Vektoren beginnen oben mit 1 und laufen im Uhrzeigersinn herum
     * @return int Der Vector
     */
    public int transformToIntVector() {
        if (this.X > 0) {
            if (this.Y > 0) {
                return 4;
            } else if (this.Y < 0) {
                return 2;
            }
            return 3;
        } else if (this.X < 0) {
            if (this.Y > 0) {
                return 6;
            } else if (this.Y < 0) {
                return 8;
            }
            return 7;
        }
        if (this.Y > 0) {
            return 5;
        } else if (this.Y < 0) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return this.X + "|" + this.Y;
    }

    @Override
    public Position clone() throws CloneNotSupportedException {
        return (Position) super.clone();
    }

    /**
     * @return the X
     */
    public int getX() {
        return X;
    }

    /**
     * @param X the X to set
     */
    public void setX(int X) {
        this.X = X;
    }

    /**
     * @return the Y
     */
    public int getY() {
        return Y;
    }

    /**
     * @param Y the Y to set
     */
    public void setY(int Y) {
        this.Y = Y;
    }

    /**
     * @return the cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * @return the heuristic
     */
    public int getHeuristic() {
        return heuristic;
    }

    /**
     * @param heuristic the heuristic to set
     */
    public void setHeuristic(int heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * @return the valF
     */
    public int getValF() {
        return valF;
    }

    /**
     * @param valF the valF to set
     */
    public void setValF(int valF) {
        this.valF = valF;
    }

    /**
     * @return the parent
     */
    public Position getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Position parent) {
        this.parent = parent;
    }
}
