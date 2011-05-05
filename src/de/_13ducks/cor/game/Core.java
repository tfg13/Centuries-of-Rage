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
package de._13ducks.cor.game;

import java.util.*;

/**
 * Die Core-Superklasse, kann nicht direkt verwendet werden.
 *
 */
public abstract class Core {

    public boolean debugmode;
    public HashMap<String, String> cfgvalues;

    /* public static void main(String args[]) {
    // Es geht los!
    
    boolean debug = true; // DEVELOPEMENT --> Debug is always on...
    
    // Argumente auswerten:
    
    if (args.length > 0) {
    // Wenn mehr als 0 Startargumente da sind, dann diese auslesen
    String arg;
    for (int i = 0; i < args.length; i++) {
    // Für jedes Argument einmal durchlaufen
    arg = args[i];
    if (arg.equals("-debug")) {
    // Debug-Mode an
    debug = true;
    System.out.println("Debug-Mode aktiviert!");
    } else {
    System.out.println("Unknown Option: " + arg);
    }
    
    }
    
    }
    
    // Genug Speicher (RAM)
    
    
    
    // Wenn Argumente da waren wurden sie jetzt ausgelesen, also los, Objekt erstellen
    
    RogMainMenu rMainMenu = new RogMainMenu();
    rMainMenu.main();
    
    } */
    public abstract void initLogger();

    public abstract class CoreInner {
        // Innere Klasse, damit die Module aufgaben zu den Warteschlangen hinzufügen können
        // Die Module müssen gegenseitig Zugriff haben, also hier her damit

        public HashMap configs;
        private boolean debug;

        public boolean isInDebugMode() {
            return debug;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

        public void initInner() {
            debug = debugmode;
            configs = cfgvalues;
        }

        // Die (überladenen) Logging-Methoden
        public abstract void logger(String x);

        public abstract void logger(Throwable t);

        public void shutdown(int errorlevel) {
            // Schält das Programm nach einem Fehler, oder ganz natürlich ab.
            // Behandlung für die verschiedenen Zustände
            if (errorlevel != 2) {
                if (errorlevel != 1) {
                    // Normal
                    logger("[Core]: Shutting down...");
                } else {
                    logger("[Core]: Critical Error reported, shutting down CoR...");
                }
            } else {
                // 2 Bedeutet, dass nicht auf das logfile geschrieben werden kann.
                System.out.println("CANNOT WRITE TO LOGFILE!!!");
            }
            // Mit angegenem Fehler beenden
            System.exit(errorlevel);
        }

        public byte[] packetFactory(byte cmdid, int data, int data2, long long3) {
            byte[] r = new byte[17];
            r[0] = cmdid;
            for (int i = 0; i < 4; i++) {
                int shift = i << 3; // i * 8
                r[4 - i] = (byte) ((data & (0xff << shift)) >>> shift);
            }
            for (int i = 0; i < 4; i++) {
                int shift = i << 3; // i * 8
                r[8 - i] = (byte) ((data2 & (0xff << shift)) >>> shift);
            }
            r[9] = (byte) (long3 >>> 56);
            r[10] = (byte) (long3 >>> 48);
            r[11] = (byte) (long3 >>> 40);
            r[12] = (byte) (long3 >>> 32);
            r[13] = (byte) (long3 >>> 24);
            r[14] = (byte) (long3 >>> 16);
            r[15] = (byte) (long3 >>> 8);
            r[16] = (byte) (long3 >>> 0);
            return r;
        }

        public byte[] packetFactory(byte cmdid, int data, int data2, int data3, int data4) {
            byte[] r = new byte[17];
            r[0] = cmdid;
            for (int i = 0; i < 4; i++) {
                int shift = i << 3; // i * 8
                r[4 - i] = (byte) ((data & (0xff << shift)) >>> shift);
            }
            for (int i = 0; i < 4; i++) {
                int shift = i << 3; // i * 8
                r[8 - i] = (byte) ((data2 & (0xff << shift)) >>> shift);
            }
            for (int i = 0; i < 4; i++) {
                int shift = i << 3; // i * 8
                r[12 - i] = (byte) ((data3 & (0xff << shift)) >>> shift);
            }
            for (int i = 0; i < 4; i++) {
                int shift = i << 3; // i * 8
                r[16 - i] = (byte) ((data4 & (0xff << shift)) >>> shift);
            }
            return r;
        }

        public byte[] packetFactory(byte cmdid, byte d0, byte d1, byte d2, byte d3, byte d4, byte d5, byte d6, byte d7, byte d8, byte d9, byte d10, byte d11, byte d12, byte d13, byte d14, byte d15) {
            byte[] r = new byte[17];
            r[0] = cmdid;
            r[1] = d0;
            r[2] = d1;
            r[3] = d2;
            r[4] = d3;
            r[5] = d4;
            r[6] = d5;
            r[7] = d6;
            r[8] = d7;
            r[9] = d8;
            r[10] = d9;
            r[11] = d10;
            r[12] = d11;
            r[13] = d12;
            r[14] = d13;
            r[15] = d14;
            r[16] = d15;
            return r;
        }

        public int readInt(byte[] b, int number) {
            if (b.length != 17 || b[0] == 0) {
                System.out.println("ERROR: Packetsyntax mismatch! (int)");
                return 0;
            }
            int retval = 0;
            for (int i = 0; i < 4; ++i) {
                retval |= (b[(number * 4) - i] & 0xff) << (i << 3);
            }
            return retval;
        }

        public long readLong2(byte[] b) {
            if (b.length != 17 || b[0] == 0) {
                System.out.println("ERROR: Packetsyntax mismatch! (long)");
                return 0;
            }
            return (((long) b[9] << 56)
                    + ((long) (b[10] & 255) << 48)
                    + ((long) (b[11] & 255) << 40)
                    + ((long) (b[12] & 255) << 32)
                    + ((long) (b[13] & 255) << 24)
                    + ((b[14] & 255) << 16)
                    + ((b[15] & 255) << 8)
                    + ((b[16] & 255) << 0));
        }

        public char readChar(byte[] b, int number) {
            if (number < 1 || number > 8) {
                System.out.println("FixMe: readChar-Aufruf mit unzulässiger Nummer (" + number + ")");
            }
            if (b.length != 17 || b[0] == 0) {
                System.out.println("ERROR: Packetsyntax mismatch! (char" + number + ")");
                return 0;
            }
            char retval = 0;
            for (int i = 0; i < 2; ++i) {
                retval |= (b[(number * 2) - i] & 0xff) << (i << 3);
            }
            return retval;
        }

        /**
         * Liest eine RogPosition aus einem Datenpacket ein.
         * @param b eine byte[17]
         * @param number 1, oder 2
         * @return Die gefundene Position
         */
        public Position readPosition(byte[] b, int number) {
            int x = 0;
            for (int i = 0; i < 4; ++i) {
                x |= (b[-4 + (number * 8) - i] & 0xff) << (i << 3);
            }
            int y = 0;
            for (int i = 0; i < 4; ++i) {
                y |= (b[(number * 8) - i] & 0xff) << (i << 3);
            }
            return new Position(x, y);
        }

        /**
         * Wandelt ein Byte Array in ein int um.
         * Liefert 0 zurück, falls es nicht klappt.
         * @param b Array of bytes, length 4
         * @param endindex index des letzen Bytes des ints
         * @return Das errechnete int
         */
        public int transInt(byte[] b, int endindex) {
            if (b.length < (endindex - 1)) {
                System.out.println("ERROR: Packetsyntax mismatch! (tint)");
                return 0;
            }
            int retval = 0;
            for (int i = 0; i < 4; ++i) {
                retval |= (b[endindex - i] & 0xff) << (i << 3);
            }
            return retval;
        }

        /**
         * Komprimiert Wege durch Weglassen der geraden Abschnitte
         * Erreicht durchschnittlich 80-90% Kompressionsrate
         * Besonders effizient bei langen Routen (> 8 Felder)
         * @param path Der zu komprimierende Pfad
         */
        public void compressPath(ArrayList<Position> path) {
            if (path != null && path.size() > 2) {
                int oldsize = path.size();
                // Durchlaufen, gerade Abschnitte löschen
                int i = 1;
                Position lastPos = path.get(0);
                Position currentPos = path.get(1);
                Position nextPos = path.get(2);
                while (true) {
                    // Ist das Feld in der Mitte entbehrlich?
                    // Das ist der Fall, wenn die Vektoren gleich sind
                    if (lastPos.subtract(currentPos).transformToVector().equals(currentPos.subtract(nextPos).transformToVector())) {
                        // Die Mitte rauslöschen
                        path.remove(i);
                        // Gehts noch weiter?
                        if (path.size() > i + 1) {
                            // Neu zuweisen
                            currentPos = nextPos;
                            nextPos = path.get(i + 1);
                        } else {
                            // Fertig
                            break;
                        }
                    } else {
                        // Gehts noch weiter?
                        if (path.size() > i + 2) {
                            // Nicht entbehrlich, weiter:
                            lastPos = path.get(i);
                            currentPos = path.get(i + 1);
                            nextPos = path.get(i + 2);
                            i++;
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Dekomprimiert Pfade, die von compressPath komprimiert wurden
         * @param path
         */
        public void extractPath(ArrayList<Position> path) {
            if (path != null && path.size() > 1) {
                int i = 1;
                Position lastPos = path.get(0);
                Position currentPos = path.get(1);
                while (true) {
                    // Abstand prüfen
                    Position diff = currentPos.subtract(lastPos);
                    // Felder adden?, Richtung?
                    if (((diff.getX() > 2 || diff.getX() < -2) && diff.getY() == 0) || ((diff.getY() > 2 || diff.getY() < -2) && diff.getX() == 0) || ((diff.getX() > 1 || diff.getX() < -1) && (diff.getY() > 1 || diff.getY() < -1))) {
                        // Es ist kein gültiger Schritt, Richtung rausfinden, in der Geadded werden muss
                        // Vektordarstellung, zum Addieren
                        diff = diff.transformToVector();
                        // Umformen, damit mans addieren kann (die geraden müssen doppelt sein)
                        if (diff.getX() == 1 && diff.getY() == 0) {
                            diff.setX(diff.getX() + 1);
                        } else if (diff.getX() == 0 && diff.getY() == 1) {
                            diff.setY(diff.getY() + 1);
                        } else if (diff.getX() == -1 && diff.getY() == 0) {
                            diff.setX(diff.getX() - 1);
                        } else if (diff.getY() == -1 && diff.getX() == 0) {
                            diff.setY(diff.getY() - 1);
                        }
                        // Adden
                        diff = lastPos.add(diff);
                        path.add(i, diff);
                    }
                    i++;
                    // Fertig?
                    if (path.size() < i + 1) {
                        // Fertig, auspacken
                        return;
                    } else {
                        // Hochzählen
                        lastPos = path.get(i - 1);
                        currentPos = path.get(i);
                    }
                }
            }
        }

        /**
         * Lagert einen Job in einen externen Thread aus
         *
         * @param r der Job, der Ausgelagert werden soll
         * @param name Der Name des Threads
         */
        @Deprecated
        void outsourceJob(Runnable r, String name) {
            Thread t = new Thread(r);
            t.setName(name);
            t.start();
        }
    }
}
