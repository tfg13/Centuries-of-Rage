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

package thirteenducks.cor.graphics.input;

import java.util.LinkedList;

/**
 * Die SelektionsMap des Clients.
 * Im Prinzip ein großes Array mit beliebig vielen Einträgen pro Feld.
 * @author tfg
 */
public class SelectionMap {

    /**
     * Die eigentliche Map.
     */
    private LinkedList<InteractableGameElement>[][] map;

    /**
     * Erstellt eine neue SelectionMap
     */
    public SelectionMap(int dimX, int dimY) {
        map = new LinkedList[dimX][dimY];
    }

    /**
     * Trägt ein IGE an der gegebenen Position in die Map ein.
     * Sorgt NICHT (!) dafür, dass das IGE nicht mehrfach eingetragen wird.
     * @param x die X-Koordinate
     * @param y die Y-Koordinate
     * @param ige das IGE, das eingetragen werden soll
     */
    public void addIGE(int x, int y, InteractableGameElement ige) {
        LinkedList<InteractableGameElement> list = map[x][y];
        if (list == null) {
            list = new LinkedList<InteractableGameElement>();
            map[x][y] = list;
        }
        list.add(ige);
    }

    /**
     * Entfernt einen IGE-Eintrag des gegebenen Elements an der der gegebenen Position aus der Map.
     * Achtung! Entfernt nur EINEN Eintrag. Muss mehrfach aufgerufen werden, sollten mehrere da sein.
     * Tut nichts, wenn gar kein Eintrag vorhanden ist.
     * @param x die X-Koordinate
     * @param y die Y-Koordinate
     * @param ige das IGE, das entfernt werden soll.
     */
    public void removeIGE(int x, int y, InteractableGameElement ige) {
        LinkedList<InteractableGameElement> list = map[x][y];
        if (list != null) {
            list.remove(ige);
        }
    }

    /**
     * Liefert alle IGE's an dieser Stelle, die vom angegebenen Team sind.
     * @param cx die X-Koordinate
     * @param cy die Y-Koordinate
     * @param playerId die PlayerId der gesuchten Einheiten
     * @return alle IGE's an der angegebenen Stelle, die vom angegebenen Team sind.
     */
    InteractableGameElement[] getIGEsWithTeamAt(int cx, int cy, int playerId) {
        
    }





}
