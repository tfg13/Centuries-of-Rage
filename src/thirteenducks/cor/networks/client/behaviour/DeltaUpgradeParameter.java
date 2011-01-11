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

package thirteenducks.cor.networks.client.behaviour;

/**
 * Eine Sammlung von Änderungen, die bei einem Delta-Uptrade durchgeführt werden sollen.
 *
 * @author tfg
 */
public class DeltaUpgradeParameter implements Cloneable {

    // Allgemein (außer global)
    public String newTex;
    public int maxhitpointsup;     // Max-Hitpoint-Upgrade erhöht automatisch auch die Hitpoints
    public int hitpointsup;        // Nicht über Maximum erhöhbar.
    public String newarmortype;    //Geänderte Rüstungsklasse der Einheit
    public int antiheavyinfup;     //Extraschaden gegen schwere Infanterie
    public int antilightinfup;     //Extraschaden gegen leichte Infanterie
    public int antikavup;          //Extraschaden gegen Kavallerie
    public int antivehicleup;      //Extraschaden gegen Fahrzeuge
    public int antitankup;         //Extraschaden gegen Panzer
    public int antiairup;          //Extraschaden gegen Flugzeuge
    public int antibuildingup;     //Extraschaden gegen Gebäude
    public int toAnimDesc;         //Animator einer anderen Einheit übernehmen
    public int visrangeup;         //Änderung der Sichtweite

    // Unit only
    public double speedup;            //Geschwindigkeit erhöhen
    public boolean harv;           //Ernten erlauben?
    public int damageup;           //Erhöhung des Schadens
    public int rangeup;            //Erhöhung der Reichweite
    public int bulletspeedup;
    public String newBulletTex;

    // Gebäude only
    public boolean changeOffsetX;
    public boolean changeOffsetY;
    public int newOffsetX;
    public int newOffsetY;
    public double harvRateup; // Änderung der Gebäude-Ernterate
    public int maxIntraup;
    public int limitupall; //Erhöht Truppenlimit von allen Häusern
    public int healup; //Erhöht Heilsrate

    // Global only
    public int harvrate1up;
    public int harvrate2up;
    public int harvrate3up;
    public int harvrate4up;
    public int harvrate5up;
    public int limitup; // Truppenlimit
    public int res1up; //Ressourcen erhalten
    public int res2up;
    public int res3up;
    public int res4up;
    public int res5up;


    // Welche Desc soll geändert werden
    public int moddesc;
    public boolean modunit;
    public boolean global; // Allgemeine Änderungen, nicht Einheiten-Spezifisch

    @Override
    public DeltaUpgradeParameter clone() throws CloneNotSupportedException {
        return (DeltaUpgradeParameter) super.clone();
    }
}
