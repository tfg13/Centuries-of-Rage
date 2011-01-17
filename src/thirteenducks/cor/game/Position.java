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
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.util.ArrayList;
import java.io.*;

public class Position implements Comparable<Position>, Serializable, Cloneable {

    public int X;                                    //X-Koodrinate
    public int Y;                                    //Y-Koordinate
    public int cost;
    public int heuristic;
    public int valF;
    public Position parent;                          //Das Feld von dem man kommt

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
    public Position aroundMe(int i, ServerCore.InnerServer inner) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (!inner.netmap.isGroundColliding(this.X, this.Y) && !inner.netmap.checkFieldReservation(this.X, this.Y)) {
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
            if (inner.netmap.isGroundColliding(kreismember.X, kreismember.Y) || inner.netmap.checkFieldReservation(kreismember.X, kreismember.Y)) {
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
    public Position aroundMe(int i, ServerCore.InnerServer inner, int limit) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (!inner.netmap.isGroundColliding(this.X, this.Y) && !inner.netmap.checkFieldReservation(this.X, this.Y)) {
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
            if (inner.netmap.isGroundColliding(kreismember.X, kreismember.Y) || inner.netmap.checkFieldReservation(kreismember.X, kreismember.Y)) {
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
    public Position aroundMe(int i, ServerCore.InnerServer inner, Position vector) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (!inner.netmap.isGroundColliding(this.X, this.Y) && !inner.netmap.checkFieldReservation(this.X, this.Y)) {
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
            if (inner.netmap.isGroundColliding(kreismember.X, kreismember.Y) || inner.netmap.checkFieldReservation(kreismember.X, kreismember.Y)) {
                i++;
            }
        }
        return kreismember;

    }

    /**
     * Sucht freie Felder um diese Position.
     * @param i Das wievielte freie Feld. (Bei 0 auch sich selbst)
     * @param inner
     * @param limit Wie lange suchen (default ist 1000)
     * @return
     */
    public Position aroundMe(int i, ClientCore.InnerClient inner, int limit) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (inner.mapModule.getCollision(this.X, this.Y).equals(collision.free)) {
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
                System.out.println("Warning: FixMe: AroundMe reached its limit for " + this);
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
            if (inner.mapModule.getCollision(kreismember.X, kreismember.Y) != collision.free) {
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
    public Position aroundMe(int i, ClientCore.InnerClient inner) {
        return aroundMe(i, inner, 1000);
    }

    /**
     * Gibt ein möglichst nahes Nachbarfeld zurück, das keine Kollision hat, also nicht blockiert ist UND nicht besetzt ist.
     * Die
     *
     * @param i Das wievielte Nachbarfeld gesucht ist - Im Zweifelsfall 1, 0 wenn auch das Feld selber Antwort sein kann
     * @param vector Die Richtung, in der die Einheiten um das Ziel platziert werden sollen - muss als Vector angegeben werden
     * @return Das Zielfeld, im normalfall ein freies Nachbarfeld, eventuell dieses selbst (bei i = 0)
     */
    public Position aroundMe(int i, ClientCore.InnerClient inner, Position vector) {
        // Diese Position als Startfeld überhaupt zulässig?
        if ((this.X + this.Y) % 2 == 1) {
            System.out.println("FixMe: Someone trys to find the neighbour of an invalid field! Field: (" + this + ")");
            return null;
        }
        if (i == 0) {
            if (inner.mapModule.getCollision(this.X, this.Y).equals(collision.free)) {
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
                System.out.println("Warning: FixMe: reached its limit for " + this + " vector " + vector);
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
            if (inner.mapModule.getCollision(kreismember.X, kreismember.Y) != collision.free) {
                i++;
            }
        }
        return kreismember;

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
        if (f.valF > this.valF) {
            return -1;
        } else if (f.valF < valF) {
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
}
