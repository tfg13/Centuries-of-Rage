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

/**
 * Dies ist eine Fließpunktposition, die durch eine Kompatibilitätsschicht
 * auch überall dort eingesetzt werden kann, wo "normale" Positionen verwendet werden.
 * Diese Position wird beispielsweise vom MapBuilder verwendet.
 **/
public class FloatingPointPosition extends Position {

    /**
     * Die X-Koordinate als Fließkommazahl
     */
    private double fX;
    /**
     * Die Y-Koordinate als Fließkommazahl
     */
    private double fY;

    /**
     * Erzeugt eine neue Position aus den angegebenen Koordinaten
     * @param x Die X-Koordinate
     * @param y Die Y-Koordinate
     */
    public FloatingPointPosition(double x, double y) {
	super((int) x, (int) y);
	fX = x;
	fY = y;
    }
    /**
     * Erzeugt eine neue Fließkommapositiona aus der übergebenen Position.
     * Die Position wird nur einmalig kopiert, nicht permanen gelinkt.
     * @param pos Die Feld-Position
     */
    public FloatingPointPosition(Position pos) {
        super(pos.getX(), pos.getY());
        fX = pos.getX();
        fY = pos.getY();
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