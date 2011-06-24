/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.map.fastfinfgrid;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Unit;

/**
 * Objekte, die im Schnellfinderaster (fastfindgrid) eingetragen werden, müssen dieses Interface implementieren.
 * @author michael
 */
public interface Traceable {

    /**
     * Gibt die Zelle des Objekts zurück.
     * @note: Die Zelle eines Objekts kann bei der Erstellung per FastFindGrid.addObject() berechnet werden.
     *        Wenn das Objekt sich bewegt, muss die Zelle per FastFindGrid.getNewCell() aktualisiert werden.
     * 
     * @return - die Zelle, in der das Objekt gerade steht
     */
    public abstract Cell getCell();

    /**
     * Gibt die Position des Objekts zurück
     * @return - die Position des Objekts
     */
    public abstract FloatingPointPosition getPosition();

    /**
     * Gibt die Einheit zurück, die diesem Traceable zugeordnet ist
     * @return - die zugehörige Einheit
     */
    public abstract Unit getUnit();
}
