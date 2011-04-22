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

package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.Position;
import de._13ducks.cor.map.CoRMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stellt die Map als (ungerichteten) Graph von konvexen Vielecken (Polygonen) dar.
 */
public class MovementMap {

    /**
     * Privater Konstruktor. CreateMovementMap verwenden!
     */
    private MovementMap() {
        polys = new ArrayList<FreePolygon>();
    }
    private List<FreePolygon> polys;

    /**
     * Erstellt eine neue MovementMap.
     * Erzeugt die interne Graphenstruktur aus konvexen Polygonen
     * @param map die CoRMap, um die es geht.
     * @param blocked alle Positionen, die nicht verwendet werden können
     * @return die fertige MovementMap
     */
    public static MovementMap createMovementMap(CoRMap map, List<Position> blocked) {
        MovementMap moveMap = new MovementMap();
        // Test: Ganz billig: einen fetten Kasten erzeugen
        Node lo = new Node(0, 0);
        Node ro = new Node(map.getMapSizeX(), 0);
        Node lu = new Node(0, map.getMapSizeY());
        Node ru = new Node(map.getMapSizeX(), map.getMapSizeY());

        FreePolygon bigPoly = new FreePolygon(lo, ro, ru, lu);

        moveMap.polys.add(bigPoly);
        
        return moveMap;
    }

    public List<FreePolygon> getPolysForDebug() {
        return Collections.unmodifiableList(polys);
    }

}
