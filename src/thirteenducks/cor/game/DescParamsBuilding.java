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
package thirteenducks.cor.game;

/**
 * Beschreibt alle Gebäude-Einstellungen, die aus den descType-Dateien eingelesen werden.
 * Einem Gebäude kann beim Erstellen diese Beschreibung gegeben werden, dann werden die Werte entsprechend gesetzt.
 * Ein nachträgliches Ändern ist nur im Rahmen von Upgrades möglich.
 * Verwaltet auch Abilitys & Grafikdaten
 * Es existieren getter und setter für alle Parameter.
 * Gebäude, mit diesen Parametern erstellt wurden bekommen nachträgliche Änderungen nicht mit.
 *
 * Dies ist eine erweiterte Version von DescParamsBuilding. Es genügt die Verwendung dieser Klasse für Gebäude, alle
 * GO-Parameter werden mitverwaltet.
 */
public class DescParamsBuilding extends DescParamsGO {

    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts oben.
     * Ausgehend von der Zuordnungsposition ganz links.
     */
    private int z1 = 2;
    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts unten.
     * Ausgehend von der Zuordnungsposition ganz links.
     */
    private int z2 = 2;
    /**
     * Zeigt an, welche Ressource dieses Gebäude produziert, solange es Arbeiter beherbergt.
     * @deprecated
     */
    private int harvests = 0;
    /**
     * Gibt die Anzahl freier Slots an (also wieviel Einheiten das Gebäude betreten können)
     */
    private int maxIntra = 0;
    /**
     * Gibt die  Ernterate pro interner Einheit an
     */
    private double harvRate = 0.0;
    /**
     * Gibt an, welche Einheiten akzeptiert werden.
     */
    private int accepts = Building.ACCEPTS_HARVESTERS_ONLY;

    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts oben.
     * Ausgehend von der Zuordnungsposition ganz links.
     * @return the z1
     */
    public int getZ1() {
        return z1;
    }

    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts oben.
     * Ausgehend von der Zuordnungsposition ganz links.
     * @param z1 the z1 to set
     */
    public void setZ1(int z1) {
        this.z1 = z1;
    }

    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts unten.
     * Ausgehend von der Zuordnungsposition ganz links.
     * @return the z2
     */
    public int getZ2() {
        return z2;
    }

    /**
     * Größe dieses Gebäudes in Feldern Richtung rechts unten.
     * Ausgehend von der Zuordnungsposition ganz links.
     * @param z2 the z2 to set
     */
    public void setZ2(int z2) {
        this.z2 = z2;
    }

    /**
     * Zeigt an, welche Ressource dieses Gebäude produziert, solange es Arbeiter beherbergt.
     * @deprecated
     * @return the harvests
     */
    public int getHarvests() {
        return harvests;
    }

    /**
     * Zeigt an, welche Ressource dieses Gebäude produziert, solange es Arbeiter beherbergt.
     * @deprecated
     * @param harvests the harvests to set
     */
    public void setHarvests(int harvests) {
        this.harvests = harvests;
    }

    /**
     * Gibt die Anzahl freier Slots an (also wieviel Einheiten das Gebäude betreten können)
     * @return the maxIntra
     */
    public int getMaxIntra() {
        return maxIntra;
    }

    /**
     * Gibt die Anzahl freier Slots an (also wieviel Einheiten das Gebäude betreten können)
     * @param maxIntra the maxIntra to set
     */
    public void setMaxIntra(int maxIntra) {
        this.maxIntra = maxIntra;
    }

    /**
     * Gibt die  Ernterate pro interner Einheit an
     * @return the harvRate
     */
    public double getHarvRate() {
        return harvRate;
    }

    /**
     * Gibt die  Ernterate pro interner Einheit an
     * @param harvRate the harvRate to set
     */
    public void setHarvRate(double harvRate) {
        this.harvRate = harvRate;
    }

    /**
     * Gibt an, welche Einheiten akzeptiert werden.
     * @return the accepts
     */
    public int getAccepts() {
        return accepts;
    }

    /**
     * Gibt an, welche Einheiten akzeptiert werden.
     * @param accepts the accepts to set
     */
    public void setAccepts(int accepts) {
        this.accepts = accepts;
    }

    public DescParamsBuilding() {
        super();
    }
}
