/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.map.fastfinfgrid;

import java.util.ArrayList;

/**
 * Eine Zelle der Rasters
 * @author michael
 */
public class Cell {

    /**
     * Die Traceables, die in dieser Zelle sind
     */
    private ArrayList<Traceable> inhabitants;
    /**
     * Die Nachbarzellen dieser Zelle *und* diese Zelle selber
     * (Diese Zelle ist auch in der Liste)
     */
    private ArrayList<Cell> neighbors;

    public Cell() {
        inhabitants = new ArrayList<Traceable>();
        neighbors = new ArrayList<Cell>();
    }

    /**
     * Fügt der Zelle einen neuen Nachbarn hinzu
     * @param neighbor
     */
    public void addNeighbor(Cell neighbor) {
        neighbors.add(neighbor);
    }

    /**
     * Fügt der Zelle einen neuen Bewohner hinzu
     * @param newInhabitant - das Traceable das jetzt in der Zelle ist
     */
    public void addInhabitant(Traceable newInhabitant) {
        this.inhabitants.add(newInhabitant);
    }

    /**
     * Entfernt einen Bewohner
     * @param inhabitant - das zu entfernende Tracable
     */
    public void removeInhabitant(Traceable inhabitant) {
        this.inhabitants.remove(inhabitant);
    }

    /**
     * Gibt eine Liste mit allen Traceables in dieser und den Nachbarzellen zurück
     * @return - s.o.
     */
    public ArrayList<Traceable> getTraceablesAroundMe() {
        // Die RückgabeListe:
        ArrayList<Traceable> ret = new ArrayList<Traceable>();

        // Die Traceables der Nachbarn:
        // (diese Zelle ist auch in der Nachbar-Liste)
        for (int i = 0; i < neighbors.size(); i++) {
            for (int j = 0; j < neighbors.get(i).inhabitants.size(); j++) {
                ret.add(neighbors.get(i).inhabitants.get(j));
            }
        }

        // und zurückgeben:
        return ret;
    }
}
