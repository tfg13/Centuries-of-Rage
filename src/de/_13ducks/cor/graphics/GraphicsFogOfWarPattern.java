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

package de._13ducks.cor.graphics;

/**
 *
 * @author tfg
 */
public class GraphicsFogOfWarPattern {

    private boolean[][][] pattern; // Die Schablonen

    public GraphicsFogOfWarPattern() {
        generatePattern();
    }

    /**
     * Erstellt alle Schablonen
     */
    private void generatePattern() {
        // Leere Schablonen erstellen:
        pattern = new boolean[20][][];
        for (int i = 0; i < 20; i++) {
            pattern[i] = new boolean[80][];
            for (int o = 0; o < 80; o++) {
                pattern[i][o] = new boolean[80];
                for (int p = 0; p < 80; p++) {
                    pattern[i][o][p] = false;
                }
            }
        }
        // Pattern auffüllen:
        for (int i = 0; i < 20; i++) {
            drawCircle(i, pattern[i]);
        }
    }

    private void drawCircle(double r, boolean[][] sub) {
        // Startwerte:
        int x = 80;
        int y = 80;
        r *= 2;
        for (int a = 0; a < x; a++) {
            for (int b = 0; b < y; b++) {
                if (a % 2 != b % 2) {
                    continue;
                }
                double dx = (x / 2) - a;
                double dy = (y / 2) - b;
                if (Math.sqrt((dx * dx) + (dy * dy)) < r) {
                    sub[a][b] = true;
                }
            }
        }
    }

    public void printPattern(int pat) {
        for (int x = 0; x < 80; x++) {
            for (int y = 0; y < 80; y++) {
                if (pattern[pat][x][y]) {
                    System.out.print("#");
                } else {
                    System.out.print("0");
                }
            }
            System.out.println();
        }
    }

    /**
     * Liefert ein sichtweite-Pattern zurück, falls range zwischen 1 und 20 (einschließlich beiden)
     * @param range
     * @return
     */
    public boolean[][] getPattern(int range) {
        if (range > 0 && range < 21) {
            return pattern[range];
        }
        return null;
    }
}
