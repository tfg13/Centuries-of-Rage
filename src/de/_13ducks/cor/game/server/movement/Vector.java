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

import de._13ducks.cor.game.FloatingPointPosition;

/**
 * Ein (double) Richtungsvektor.
 * Einfacher, aber weniger flexibel als FPP
 */
public class Vector {

    public static final Vector NULL = new Vector(0,0);
    
    private double x;
    private double y;

    /**
     * Erstell einen neuen Vektor mit den angegebenen Koordinaten
     * @param x x
     * @param y y
     */
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Wandelt den Vektor in eine FPP um.
     * @return der Vektor als FPP.
     */
    public FloatingPointPosition toFloatingPointPosition() {
        return new FloatingPointPosition(x, y);
    }

    /**
     * Den Vektor mit einem Skalar multiplizieren.
     * Verändert direkt den Vector!
     * @param factor 
     */
    public void multiply(double factor) {
        x *= factor;
        y *= factor;
    }

    /**
     * Normiert den Vektor
     * Verändert direkt den Vector!
     */
    public void normalize() {
        double fact = length();
        x *= 1 / fact;
        y *= 1 / fact;
    }

    /**
     * Berechnet die Länge des Vektors
     * @return die Länge des Vektors
     */
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * True, wenn der gleiche Vektor, nur in die andere Richtung zeigend
     */
    public boolean isOpposite(Vector vec) {
        return this.equals(new Vector(-vec.x, -vec.y));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vector) {
            Vector vec = (Vector) o;
            return (Math.abs(this.x - vec.x) < 0.001 && Math.abs(this.y - vec.y) < 0.001);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        return hash;
    }
    
    @Override
    public String toString() {
        return "v:" + x + "|" + y;
    }
}
