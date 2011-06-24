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
package de._13ducks.cor.map.fastfindgrid;

import de._13ducks.cor.game.FloatingPointPosition;
import java.util.ArrayList;

/**
 * Ein Raster zum schnellen Finden von Objekten nahe einer bestimmten Position
 *
 * @author michael
 */
public class FastFindGrid {

    /**
     * Das Rastah!
     */
    private Cell grid[][];
    /**
     * Die Größe einer Zelle
     */
    private double cellSize;

    /**
     * Konstruktor, initialisiert das Raster
     * @param mapSizeX - Breite der Karte
     * @param mapSizeY - Höhe der Karte
     * @param cellSize - Die Größe der einzelnen Zellen
     *                   sollte dem maximalen Suchradius entsprechen oder leicht Größer sein
     * 
     */
    public FastFindGrid(int mapSizeX, int mapSizeY, double cellSize) {

        this.cellSize = cellSize;

        // Rastergröße bestimmen:
        int gridX = (int) (mapSizeX / cellSize);
        int gridY = (int) (mapSizeY / cellSize);

        // Raster erstellen:
        grid = new Cell[gridX][gridY];

        // Zellen erstellen:
        for (int x = 0; x < gridX; x++) {
            for (int y = 0; y < gridY; y++) {
                grid[x][y] = new Cell();
            }
        }

        // Nachbarzellen eintragen:
        for (int x = 0; x < gridX; x++) {
            for (int y = 0; y < gridY; y++) {
                for (int nx = -1; nx < 2; nx++) {
                    for (int ny = -1; ny < 2; ny++) {
                        try {
                            grid[x][y].addNeighbor(grid[x + nx][y + ny]);
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Gibt eine Liste mit Einheiten zurück, die einen Zellradius von der angegebenen Position entfernt sind
     * @param position - die Position, um die gesucht wird
     * @return         - Eine Liste der Einheiten um diese Position
     */
    public ArrayList<Traceable> getTraceablesAroundPoint(FloatingPointPosition position) {

        // Welche Zelle enthält die Position?
        Cell theCell = getCellByPosition(position);

        return theCell.getTraceablesAroundMe();
    }

    /**
     * Fügt ein Objekt dem Raster hinzu
     * Gibt die Zelle, in der das Objekt steht, zurück
     * @param object - das hinzuzufügende Objekt
     * @return       - die Zelle, in der das Objekt steht
     */
    public Cell addObject(Traceable object) {
        Cell theCell = getCellByPosition(object.getPosition());

        theCell.addInhabitant(object);

        return theCell;
    }

    /**
     * Entfernt ein Objekt aus dem Raster, etwa wenn es zerstört wurde
     */
    public void removeObject(Traceable object) {
        object.getCell().removeInhabitant(object);
    }

    /**
     * Wird aufgerufen, wenn ein Objekt bewegt wurde
     * Gibt die zelle zurück, in der das Objekt jetzt steht
     * @param movedObject - das Objekt, das bewegt wurde
     * @return            - die Zelle, in der das Objekt jetzt steht
     */
    public Cell getNewCell(Traceable movedObject) {

        Cell theCell = getCellByPosition(movedObject.getPosition());

        // ist das Traceable jetzt in einer anderen Zelle?
        if (theCell != movedObject.getCell()) {
            movedObject.getCell().removeInhabitant(movedObject);
            theCell.addInhabitant(movedObject);
        }
        return theCell;
    }

    /**
     * Gibt die Zelle zurück, die die angegebene Position enthält
     * @param position - Die Positione deren Zelle gesucht wird
     * @return         - die Zelle die die angegebene Position enthält
     */
    private Cell getCellByPosition(FloatingPointPosition position) {

        int xCell = (int) (position.getfX() / cellSize);
        int yCell = (int) (position.getfY() / cellSize);

        return grid[xCell][yCell];
    }
}
