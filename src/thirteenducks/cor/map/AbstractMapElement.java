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

import thirteenducks.cor.map.AbstractMapElement.collision;
import thirteenducks.cor.game.GameObject;

/**
 *
 * Dies ist ein Feld in der Grafik - Untergrund plus Objekt darauf
 *
 */
public abstract class AbstractMapElement {

    /**
     * Kollisionsstatus
     * free:        freies Feld
     * blocked:     "permanent" besetzt von Einheit/Gebäude
     * occupied:    Einheit geht gerade drüber
     * unreachable  unerreichbar z.B. Randfelder
     */
    public enum collision{free, blocked, occupied, unreachable};

    /**
     * Überprüft, ob dieses Feld gerade Reserviert ist.
     *
     * Bereits ausgelaufene Reservierungen werden automatisch berücksichtigt.
     * @return
     */
    public abstract boolean isReserved();

    /**
     * Liefert die Einheit, die dieses Feld reserviert hat, oder null wenn nicht
     * reserviert bzw. abgelaufen
     * @return
     */
    public abstract GameObject getReserver();

    /**
     * Setzt (Überschreibt) die Resevierung für ein Feld.
     */
    public abstract void setReserved(long reserveFor, GameObject go);

    /**
     * Löscht die Reservierung wieder
     * Beim regulären Ankommen nicht notwendig, da ist ja die Zeit ausgelaufen
     */
    public abstract void deleteReservation();

    /**
     * Setzt ein Feld auf "Unerreichbar" (oder schält das wieder ab).
     * Vorsicht: Darf im laufenden Spiel nicht verändert werden!!!
     * @param unreachable true, wenn für immer unerreichbar (Maprand, Klippen, Fix, etc.)
     */
    public abstract void setUnreachable(boolean unreachable);

    /**
     * Die Bodentextur. Das alte HashMap-System wurde abgeschafft, das frisst nur unnötig viel Speicher
     * @return the ground_tex
     */
    public abstract String getGround_tex();

    /**
     * Die Bodentextur. Das alte HashMap-System wurde abgeschafft, das frisst nur unnötig viel Speicher
     * @param ground_tex the ground_tex to set
     */
    public abstract void setGround_tex(String ground_tex);
    /**
     * Die Fix-Textur. Für feste Sachen, die nach dem Boden gezeichnet werden müssen (z.B. Klippen)
     * @return the fix_tex
     */
    public abstract String getFix_tex();

    /**
     * Die Fix-Textur. Für feste Sachen, die nach dem Boden gezeichnet werden müssen (z.B. Klippen)
     * @param fix_tex the fix_tex to set
     */
    public abstract void setFix_tex(String fix_tex);

    /**
     * Findet heraus, ob dieses Feld derzeit ein gültiges Ziel für das gegebene Bodenobjekt ist.
     * Defaultmäßig kann nur ein Object auf jedem Feld stehen.
     * @param obj Für dieses Objekt wird der Kollisionszustand ermittelt
     * @return true, wenn als Ziel derzeit in Ordnung, false, wenn nicht.
     */
    public abstract boolean validGroundTarget(GameObject obj);

    /**
     * Findet heraus, ob dieses Feld derzeit ein gültige Bewegungsposition für das gegebene Objekt ist.
     * Defaultmäßig können Units durch verbündete Einheiten durchlaufen, nicht aber durch Feinde.
     * @param obj Für dieses Objekt wird  der Kollisionszustand ermittelt.
     * @return true, wenn drübergelaufen werden darf, false wenn nicht.
     */
    public abstract boolean validGroundPath(GameObject obj);

    /**
     * Registriert das angegebene Object als langfristigen Besetzer dieses Feldes.
     * Das kann eine stehende Einheit oder ein Gebäude sein.
     * Der Return-Wert gibt an, ob es geklappt hat oder nicht.
     * Falles es nicht klappt, liegt es vermutlich daran, dass nur ein Object registriert werden kann.
     * @param obj das zu registrierende Object
     * @return true, wenns geklappt hat, sonst false
     */
    public abstract boolean addPermanentObject(GameObject obj);

    /**
     * Entfernt den langfristigen Besetzer dieses Feldes wieder.
     * Sollte keiner Existieren, passiert gar nix.
     */
    public abstract void removePermanentObject();

    /**
     * Registriert das angegebene Object als kurzfristigen Besetzter dieses Feldes.
     * In der Regel Einheiten, die gerade über diese Feld laufen.
     * @param obj das zu registrierende Objekt
     */
    public abstract void addTempObject(GameObject obj);

    /**
     * Enfernt das angegeneme Object wieder von diesem Feld, sofern es überhaupt da war.
     * @param obj das zu entfernende objekt
     */
    public abstract void removeTempObject(GameObject obj);

}