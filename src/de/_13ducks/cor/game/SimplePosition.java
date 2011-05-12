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
package de._13ducks.cor.game;

import de._13ducks.cor.game.server.movement.Node;
import de._13ducks.cor.game.server.movement.Vector;

/**
 * Ein  ganz simples Positions-Interface. Kann nur Positionen aufnehmen
 */
public interface SimplePosition {
    
    /**
     * Die X-Koordinate
     * @return 
     */
    public double x();
    /**
     * Die y-Koordinate
     * @return 
     */
    public double y();
    /**
     * Als Vector
     * @return 
     */
    public Vector toVector();
    /**
     * Als FPP
     * @return 
     */
    public FloatingPointPosition toFPP();
    /**
     * Als Node
     * @return 
     */
    public Node toNode();
}
