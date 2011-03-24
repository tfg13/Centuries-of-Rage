package thirteenducks.cor.map;

import java.util.ArrayList;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;

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
                return obj.getPlayerId() == permRef.getPlayerId() && permRef instanceof Unit;
            case occupied:
                return obj.getPlayerId() == moveRefs.get(0).getPlayerId();
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
    public int removePermanentObject() {
        if (collision != collision.unreachable) {
            permRef = null;
            if (moveRefs.isEmpty()) {
                collision = collision.free;
                return 0;
            } else {
                collision = collision.occupied;
                return 3;
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
