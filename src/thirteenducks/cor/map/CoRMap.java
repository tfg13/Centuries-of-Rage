/*
 *  Copyright 2008, 2009, 2010:
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

import thirteenducks.cor.map.CoRMapElement.collision;
import java.io.*;
import java.util.HashMap;

/**
 *
 * @author tfg
 * Klasse beschreibt eine Map - Objekt wird vom RogMapEditor erstellt und in eine Datei serialisiert - Das RogGraphics Modul kann Maps wieder öffnen
 *
 */
public class CoRMap implements Serializable {

    static final long serialVersionUID = -5532503078112972431L;
    // Variablen
    public CoRMapElement[][] visMap; // Die Map
    private HashMap<String,Object> mapStuff; // Infos in Form von Objekten über die Map
    public String mapName; // Name der Map, Datei und Programmintern
    private int xSize;
    private int ySize;

    public CoRMap(int x, int y, String name, CoRMapElement[][] levelOneMap) {
        // Initiiert eine neue Map
        // name name der Map und der Datei

        // Go!
        // Array Ebene 1 Initialisieren
        visMap = levelOneMap;
        // Name übernehmen
        mapName = name;
        // isTown übernehmen
        // Größe Übernehmen
        xSize = x;
        ySize = y;
        // Andere Sachen
        mapStuff = new HashMap(5);

    }


    @Deprecated
    public int getMapSize(String side) {
        // Liefert den x (side = x ) oder y-Wert (side = y) zurück
        if (side.equals("x") || side.equals("X")) {
            return xSize;
        } else {
            return ySize;
        }
    }

    public int getMapSizeX() {
        return xSize;
    }

    public int getMapSizeY() {
        return ySize;
    }

    /**
     * @deprecated  RogMapModule ruft jetzt direkt RogMap.getvismap[x][y].getcollision auf
     */
    public collision isBlocking(int x, int y) {
        // Bestimmt, ob ein bestimmtes Feld nicht begehbar ist
        try {
        return visMap[x][y].getCollision();
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            return collision.blocked;
        } catch (java.lang.NullPointerException ex) {
            return collision.free;
        }
    }

    public void changeElement(int x, int y, CoRMapElement elem) {
        // Tauscht ein Mapelement gegen das neue aus
        visMap[x][y] = elem;
    }

    public void changeElementProperty(int x, int y, String prop, String value) {
        // Ändet die Eigenschaften eines Feldes
        visMap[x][y].setProperty(prop, value);
    }

    public void deleteElementProperty(int x, int y, String prop) {
        // Löscht eine Eigenschaft
        visMap[x][y].deleteProperty(prop);
    }

        public void changeElementObjectProperty(int x, int y, String prop, String value) {
        // Ändet die Eigenschaften eines Feldes
        visMap[x][y].setObjectProperty(prop, value);
    }

    public void deleteElementObjectProperty(int x, int y, String prop) {
        // Löscht eine Eigenschaft
        visMap[x][y].deleteObjectProperty(prop);
    }

    public String getElementProperty(int x, int y, String prop) {
        return visMap[x][y].getProperty(prop);
    }

    public Object getElementObjectProperty(int x, int y, String prop) {
        return visMap[x][y].getObjectProperty(prop);
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

    public CoRMapElement[][] getVisMap() {
        // Damit kommt man an den Inhalt der Map
        return visMap;
    }
}


