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
/**
 * Wie Position, aber mit double statt int
 **/

package de._13ducks.cor.game;

import de._13ducks.cor.game.Position;

public class FloatingPointPosition extends Position {

    private double fX;
    private double fY;

    public FloatingPointPosition(double x, double y) {
	super((int) x, (int) y);
	fX = x;
	fY = y;

    }

    /**
     * @param X the X to set
     */
    @Override
    public void setX(int X) {
        this.X = X;
	fX = X;
    }

    /**
     * @param Y the Y to set
     */
    @Override
    public void setY(int Y) {
        this.Y = Y;
	fY = Y;
    }

    /**
     * @return the fX
     */
    public double getfX() {
	return fX;
    }

    /**
     * @param fX the fX to set
     */
    public void setfX(double fX) {
	this.fX = fX;
	this.X = (int) fX;
    }

    /**
     * @return the fY
     */
    public double getfY() {
	return fY;
    }

    /**
     * @param fY the fY to set
     */
    public void setfY(double fY) {
	this.fY = fY;
	this.Y = (int) fY;
    }


}