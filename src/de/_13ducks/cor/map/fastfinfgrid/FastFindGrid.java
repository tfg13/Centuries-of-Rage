/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.map.fastfinfgrid;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Unit;
import java.util.ArrayList;

/**
 * Ein Raster zum schnellen Finden von Objekten nahe einer bestimmten Position
 *
 * @author michael
 */
public class FastFindGrid {

    /**
     * Konstruktor, initialisiert das Raster
     * @param mapSizeX - Breite der Karte
     * @param mapSizeY - Hähe der Karte
     * @param cellSize - Die Größe der einzelnen Zellen
     *                   sollte dem maximalen Suchradius entsprechen oder leicht Größer sein
     * 
     */
    public FastFindGrid(int mapSizeX, int mapSizeY, double cellSize) {
    }

    /**
     * Gibt eine Liste mit Einheiten zurück, die einen Zellradius von der angegebenen Position entfernt sind
     * @param position - die Position, um die gesucht wird
     * @return         - Eine Liste der Einheiten um diese Position
     */
    public ArrayList<Unit> getUnitsAroundPoint(FloatingPointPosition position) {
        return null;
    }

    /**
     * Fügt ein Objekt dem Raster hinzu
     * Gibt die Zelle, in der das Objekt steht, zurück
     * @param object - das hinzuzufügende Objekt
     * @return       - die Zelle, in der das Objekt steht
     */
    public Cell addObject(Traceable object) {
        return null;
    }

    /**
     * Entfernt ein Objekt aus dem Raster, etwa wenn es zerstört wurde
     */
    public void removeObject(Traceable object) {
    }

    /**
     * Wird aufgerufen, wenn ein Objekt bewegt wurde
     * Gibt die zelle zurück, in der das Objekt jetzt steht
     * @param movedObject - das Objekt, das bewegt wurde
     * @return            - die Zelle, in der das Objekt jetzt steht
     */
    public Cell getNewCell(Traceable movedObject) {
        return null;
    }
}
