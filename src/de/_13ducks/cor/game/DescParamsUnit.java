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
 * Beschreibt alle Einheiten-Einstellungen, die aus den descType-Dateien eingelesen werden.
 * Einem Einheiten kann beim Erstellen diese Beschreibung gegeben werden, dann werden die Werte entsprechend gesetzt.
 * Ein nachträgliches Ändern ist nur im Rahmen von Upgrades möglich.
 * Verwaltet auch Abilitys & Grafikdaten
 * Es existieren getter und setter für alle Parameter.
 * Einheiten, mit diesen Parametern erstellt wurden bekommen nachträgliche Änderungen nicht mit.
 *
 * Dies ist eine erweiterte Version von DescParamsBuilding. Es genügt die Verwendung dieser Klasse für Einheiten, alle
 * GO-Parameter werden mitverwaltet.
 */
public class DescParamsUnit extends DescParamsGO {

    /**
     * Die Geschwindigkeit der Einheit in Feldern pro Sekunde.
     */
    private double speed;
    /**
     * Die Größe. 2 bedeutet 2x2, 3 bedeutet 3x3 etc.
     * 2x2 ist default.
     */
    private int size = 2;

    /**
     * Die Geschwindigkeit der Einheit in Feldern pro Sekunde.
     * @return the speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Die Geschwindigkeit der Einheit in Feldern pro Sekunde.
     * @param speed the speed to set
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public DescParamsUnit() {
        super();
    }

    /**
     * Die Größe. 2 bedeutet 2x2, 3 bedeutet 3x3 etc.
     * 2x2 ist default.
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * Die Größe. 2 bedeutet 2x2, 3 bedeutet 3x3 etc.
     * 2x2 ist default.
     * @param size the size to set
     */
    public void setSize(int size) {
        this.size = size;
    }

}
