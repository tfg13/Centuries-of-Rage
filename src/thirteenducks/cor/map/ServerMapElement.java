package thirteenducks.cor.map;

import java.util.ArrayList;
import thirteenducks.cor.game.GameObject;

/**
 *
 */
public class ServerMapElement extends AbstractMapElement {

    /**
     * Die Kollision wird automatisch verwaltet.
     * Es können GameObjects zugeordnet werden, dann wird die Kollision automatisch verwaltet.
     * Es gibt nur einen setter für unreachable (Mapränder, Klippen, nicht entfernbare Sachen).
     */
    private collision collision;
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
     *
     */
    private GameObject reserver;

    @Override
    public void setUnreachable(boolean unreachable) {
        if (unreachable) {
            collision = collision.unreachable;
        } else {
            collision = collision.free;
        }
    }

    @Override
    public boolean isReserved() {
        long l = System.currentTimeMillis();
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
    public void setReserved(long reserveFor, GameObject go) {
        reservedUntil = System.currentTimeMillis() + reserveFor;
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
                return obj.getPlayerId() == permRef.getPlayerId();
            case occupied:
                return obj.getPlayerId() == moveRefs.get(0).getPlayerId();
        }
        return false;
    }

    /**
     * Registriert das angegebene Object als langfristigen Besetzer dieses Feldes.
     * Das kann eine stehende Einheit oder ein Gebäude sein.
     * Der Return-Wert gibt an, ob es geklappt hat oder nicht.
     * Falles es nicht klappt, liegt es vermutlich daran, dass nur ein Object registriert werden kann.
     * @param obj das zu registrierende Object
     * @return true, wenns geklappt hat, sonst false
     */
    public boolean addPermanentObject(GameObject obj) {
        if (collision != collision.unreachable) {
            if (permRef == null) {
                permRef = obj;
                collision = collision.blocked;
                return true;
            }
        }
        return false;
    }

    /**
     * Entfernt den langfristigen Besetzer dieses Feldes wieder.
     * Sollte keiner Existieren, passiert gar nix.
     */
    public void removePermanentObject() {
        if (collision != collision.unreachable) {
            if (moveRefs.isEmpty()) {
                collision = collision.free;
            } else {
                collision = collision.occupied;
            }
            permRef = null;
        }
    }

    /**
     * Registriert das angegebene Object als kurzfristigen Besetzter dieses Feldes.
     * In der Regel Einheiten, die gerade über diese Feld laufen.
     * @param obj das zu registrierende Objekt
     */
    public void addTempObject(GameObject obj) {
        if (collision != collision.unreachable) {
            if (!moveRefs.contains(obj)) {
                moveRefs.add(obj);
                if (permRef == null) {
                    collision = collision.occupied;
                }
            }
        }
    }

    /**
     * Enfernt das angegeneme Object wieder von diesem Feld, sofern es überhaupt da war.
     * @param obj das zu entfernende objekt
     */
    public void removeTempObject(GameObject obj) {
        if (collision != collision.unreachable) {
            moveRefs.remove(obj);
            if (moveRefs.isEmpty()) {
                if (permRef == null) {
                    collision = collision.free;
                }
            }
        }
    }
}
