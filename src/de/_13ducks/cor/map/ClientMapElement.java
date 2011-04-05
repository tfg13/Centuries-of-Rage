package de._13ducks.cor.map;

import de._13ducks.cor.game.GameObject;

/**
 * Ein Mapelement des Clients.
 * Dieses hat keine Methoden zur Kollisions und Referenzenverwaltung, dafür aber Strings für Texturen.
 */
public class ClientMapElement extends AbstractMapElement {

    /**
     * Die Bodentextur. Das alte HashMap-System wurde abgeschafft, das frisst nur unnötig viel Speicher
     */
    private String ground_tex;
    /**
     * Die Fix-Textur. Für feste Sachen, die nach dem Boden gezeichnet werden müssen (z.B. Klippen)
     */
    private String fix_tex;

    @Override
    public boolean isReserved() {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public GameObject getReserver() {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public void setReserved(long reserveFor, GameObject go) {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public void deleteReservation() {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    /**
     * Die Bodentextur. Das alte HashMap-System wurde abgeschafft, das frisst nur unnötig viel Speicher
     * @return the ground_tex
     */
    @Override
    public String getGround_tex() {
        return ground_tex;
    }

    /**
     * Die Bodentextur. Das alte HashMap-System wurde abgeschafft, das frisst nur unnötig viel Speicher
     * @param ground_tex the ground_tex to set
     */
    @Override
    public void setGround_tex(String ground_tex) {
        this.ground_tex = ground_tex;
    }

    /**
     * Die Fix-Textur. Für feste Sachen, die nach dem Boden gezeichnet werden müssen (z.B. Klippen)
     * @return the fix_tex
     */
    @Override
    public String getFix_tex() {
        return fix_tex;
    }

    /**
     * Die Fix-Textur. Für feste Sachen, die nach dem Boden gezeichnet werden müssen (z.B. Klippen)
     * @param fix_tex the fix_tex to set
     */
    @Override
    public void setFix_tex(String fix_tex) {
        this.fix_tex = fix_tex;
    }

    @Override
    public boolean validGroundTarget(GameObject obj) {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public boolean validGroundPath(GameObject obj) {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public int addPermanentObject(GameObject obj) {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public int removePermanentObject() {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public int addTempObject(GameObject obj) {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public int removeTempObject(GameObject obj) {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

    @Override
    public boolean validGroundPathWhilePlanning(GameObject obj) {
        throw new UnsupportedOperationException("Not supported for Client!");
    }

}
