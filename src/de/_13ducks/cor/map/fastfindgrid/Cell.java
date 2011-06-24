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

import java.util.ArrayList;

/**
 * Eine Zelle der Rasters
 * @author michael
 */
public class Cell {

    /**
     * Die Traceables, die in dieser Zelle sind
     */
    public ArrayList<Traceable> inhabitants;
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
