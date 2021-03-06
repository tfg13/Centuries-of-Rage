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
package de._13ducks.cor.map;

import java.util.ArrayList;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Unit;

/**
 *
 */
public class ServerMapElement extends AbstractMapElement {

    /**
     * Hält das Haupt-Objekt fest, das auf diesem Feld steht.
     */
    private GameObject permRef;
    /**
     * Hält Referenzen auf alle GameObjects, die sich gerade "auf" diesem Feld befinden.
     * (Über dieses Feld gefunden werden sollen)
     */
    private ArrayList<GameObject> moveRefs;
    /**
     * Reservierungssystem.
     * Sagt, wie lange dieses Feld reserviert ist.
     */
    private long reservedUntil;
    /**
     * Reservierungssystem.
     * Sagt, wer dieses Feld gerade reserviert hat.
     */
    private GameObject reserver;

    @Override
    public boolean isReserved() {
        if (System.currentTimeMillis() < reservedUntil) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public GameObject getReserver() {
        if (System.currentTimeMillis() < reservedUntil) {
            return reserver;
        } else {
            return null;
        }
    }

    @Override
    public void setReserved(long reserveUntil, GameObject go) {
        reservedUntil = reserveUntil;
        reserver = go;
    }

    @Override
    public void deleteReservation() {
        reservedUntil = 0;
        reserver = null;
    }

    public ServerMapElement() {
        collision = collision.free;
        moveRefs = new ArrayList<GameObject>();
    }

    @Override
    public String getGround_tex() {
        throw new UnsupportedOperationException("Not supported for Server!");
    }

    @Override
    public void setGround_tex(String ground_tex) {
        throw new UnsupportedOperationException("Not supported for Server!");
    }

    @Override
    public String getFix_tex() {
        throw new UnsupportedOperationException("Not supported for Server!");
    }

    @Override
    public void setFix_tex(String fix_tex) {
        throw new UnsupportedOperationException("Not supported for Server!");
    }

    @Override
    public boolean validGroundTarget(GameObject obj) {
        switch (collision) {
            case unreachable:
            case blocked:
                return false;
            case free:
                return true;
            case occupied:
                return obj.getPlayerId() == moveRefs.get(0).getPlayerId();
        }
        return false;
    }

    @Override
    public boolean validGroundPath(GameObject obj) {
        switch (collision) {
            case unreachable:
                return false;
            case free:
                return true;
            case blocked:
                return obj.getPlayerId() == permRef.getPlayerId() && permRef instanceof Unit;
            case occupied:
                return obj.getPlayerId() == moveRefs.get(0).getPlayerId();
        }
        return false;
    }

    @Override
    public boolean validGroundPathWhilePlanning(GameObject obj) {
        switch (collision) {
            case unreachable:
                return false;
            case free:
            case occupied:
                return true;
            case blocked:
                return permRef instanceof Unit;
        }
        return false;
    }

    /**
     * Registriert das angegebene Object als langfristigen Besetzer dieses Feldes.
     * Das kann eine stehende Einheit oder ein Gebäude sein.
     * @param obj das zu registrierende Object
     * @return den neuen collisions-status in int-Darstellung
     */
    @Override
    public int addPermanentObject(GameObject obj) {
        if (collision != collision.unreachable) {
            if (permRef == null) {
                permRef = obj;
                collision = collision.blocked;
            }
            return 2;
        }
        return 1;
    }

    /**
     * Entfernt den langfristigen Besetzer dieses Feldes wieder.
     * Sollte keiner Existieren, passiert gar nix.
     */
    @Override
    public int removePermanentObject(GameObject obj) {
        if (collision != collision.unreachable) {
            if (permRef != null && permRef.equals(obj)) {
                permRef = null;
                if (moveRefs.isEmpty()) {
                    collision = collision.free;
                    return 0;
                } else {
                    collision = collision.occupied;
                    return 3;
                }
            } else {
                if (permRef == null) {
                    if (moveRefs.isEmpty()) {
                        collision = collision.free;
                        return 0;
                    } else {
                        collision = collision.occupied;
                        return 3;
                    }
                }
                return 2;
            }
        }
        return 1;
    }

    /**
     * Registriert das angegebene Object als kurzfristigen Besetzter dieses Feldes.
     * In der Regel Einheiten, die gerade über diese Feld laufen.
     * @param obj das zu registrierende Objekt
     */
    @Override
    public int addTempObject(GameObject obj) {
        if (collision != collision.unreachable) {
            if (!moveRefs.contains(obj)) {
                moveRefs.add(obj);
            }
            if (permRef == null) {
                collision = collision.occupied;
                return 3;
            } else {
                return 2;
            }
        }
        return 1;
    }

    /**
     * Enfernt das angegeneme Object wieder von diesem Feld, sofern es überhaupt da war.
     * @param obj das zu entfernende objekt
     */
    @Override
    public int removeTempObject(GameObject obj) {
        if (collision != collision.unreachable) {
            moveRefs.remove(obj);
            if (moveRefs.isEmpty()) {
                if (permRef == null) {
                    collision = collision.free;
                    return 0;
                } else {
                    return 2;
                }
            } else {
                if (permRef == null) {
                    return 3;
                } else {
                    return 2;
                }
            }
        }
        return 1;
    }
}
