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


package thirteenducks.cor.map;

import java.util.HashMap;

/**
 * Die Map.
 * Enthält und speichert Texturen, Einheiten etc.
 * Kann im Gegensatz zu früheren Versionen nichtmehr serialisiert werden.
 * Zum Speichern wird nurnoch das RMAP-System verwendet.
 */
public class CoRMap {
    // Variablen
    private AbstractMapElement[][] visMap; // Die Map
    private HashMap<String,Object> mapStuff; // Infos in Form von Objekten über die Map
    private String mapName; // Name der Map, Datei und Programmintern
    private int xSize;
    private int ySize;
    private int playernumber;
    private int nextNetID = 1;

    public CoRMap(int x, int y, String name, AbstractMapElement[][] levelOneMap) {
        // Initiiert eine neue Map
        // name name der Map und der Datei

        // Go!
        // Array Ebene 1 Initialisieren
        visMap = levelOneMap;
        // Name übernehmen
        mapName = name;
        // Größe Übernehmen
        xSize = x;
        ySize = y;
        // Andere Sachen
        mapStuff = new HashMap(5);

    }

    public int getMapSizeX() {
        return xSize;
    }

    public int getMapSizeY() {
        return ySize;
    }

    public Object getMapPoperty(String prop) {
        return mapStuff.get(prop);
    }

    public void setMapProperty(String prop, Object obj) {
        mapStuff.put(prop, obj);
    }

    public void deleteMapProperty(String prop) {
        mapStuff.remove(prop);
    }

    public String getPath() {
        // Liefert den Speicherort zurück
        // Speicherort ist Name
        return "map/" + mapName + ".map";
    }

    public AbstractMapElement[][] getVisMap() {
        // Damit kommt man an den Inhalt der Map
        return visMap;
    }

    /**
     * @return the playernumber
     */
    public int getPlayernumber() {
	return playernumber;
    }

    /**
     * @param playernumber the playernumber to set
     */
    public void setPlayernumber(int playernumber) {
	this.playernumber = playernumber;
    }

    public int getNewNetID() {
	nextNetID++;
	return (nextNetID - 1);
    }

    /**
     * @return the mapName
     */
    public String getMapName() {
        return mapName;
    }
}


