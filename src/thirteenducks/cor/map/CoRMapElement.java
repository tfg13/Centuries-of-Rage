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


package thirteenducks.cor.map;

import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author tfg
 *
 * Dies ist ein Feld in der Grafik - Untergrund plus Objekt darauf
 *
 */
public class CoRMapElement implements Serializable {

    //static final long serialVersionUID = -1492121271480641828L; // OUTDATED

    /**
     * Kollisionsstatus
     * free:        freies Feld
     * blocked:     von Gegenstand/Gebäude blockiert
     * occupied:    von Einheit besetzt
     * unreachable  unerreichbar z.B. Randfelder
     */
    public enum collision{free, blocked, occupied, unreachable};

    public collision collision;
    public Unit[] unitref; // Referenz auf die Einheit die das Feld gerade besetzt - Array mit Spielern

    private HashMap<String,String> properties; // Eigenschaften für das Feld, Textur, Misc etc...
    private HashMap<String,Object> objProperties; // Alle Eigenschaften, die als Objekt gespeichert werden.

    private long reservedUntil;                 // Felder können kurzfristig reserviert werden
    private Unit reserver;


    /**
     * Setzt den Kollisionsstatus.
     */
    public void setCollision(collision col) {
        collision = col;
    }

    /**
     * Überprüft, ob dieses Feld gerade Reserviert ist.
     *
     * Bereits ausgelaufene Reservierungen werden automatisch berücksichtigt.
     * @return
     */
    public boolean isReserved() {
        long l = System.currentTimeMillis();
        if (System.currentTimeMillis() < reservedUntil) {

            return true;
        } else {
            return false;
        }
    }

    /**
     * Liefert die Einheit, die dieses Feld reserviert hat, oder null wenn nicht
     * reserviert bzw. abgelaufen
     * @return
     */
    public Unit getReserver() {
        if (System.currentTimeMillis() < reservedUntil) {
            return reserver;
        } else {
            return null;
        }
    }

    /**
     * Setzt (Überschreibt) die Resevierung für ein Feld.
     * @param unitspeed Die speed-Variable der Einheit
     */
    public void setReserved(long reserveFor, Unit unit) {
        reservedUntil = System.currentTimeMillis() + reserveFor;
        reserver = unit;
    }

    /**
     * Löscht die Reservierung wieder
     * Beim regulären Ankommen nicht notwendig, da ist ja die Zeit ausgelaufen
     */
    public void deleteReservation() {
        reservedUntil = 0;
        reserver = null;
    }

    /**
     * Gibt den Kollisionsstatus zurück.
     */
   public collision getCollision() {
        return collision;
    }

    protected void setProperty(String prop, String value) {
        // Setzt neue Werte in die Eigenschaften HashMap
        properties.put(prop, value);
    }

    public String getProperty(String prop) {
        // Liefert den Wert der zu prop passt, zurück
        return properties.get(prop);
    }

    protected Object getObjectProperty(String prop) {
        return objProperties.get(prop);
    }

    protected void setObjectProperty(String prop, String obj) {
        objProperties.put(prop, obj);
    }

    protected void deleteObjectProperty(String prop) {
        objProperties.remove(prop);
    }

    protected void deleteProperty(String prop) {
        // Löscht die Eigenschaft
        properties.remove(prop);
    }

    protected boolean hasProperty(String prop) {
        // Überpfüft, ob die Eigenschaft vorhanden ist
        return properties.containsKey(prop);
    }

    public CoRMapElement() {
        // Konstruktor
        // Neue HashMaps anlegen
        properties = new HashMap();
        objProperties = new HashMap();
        collision = collision.free;
    }
}