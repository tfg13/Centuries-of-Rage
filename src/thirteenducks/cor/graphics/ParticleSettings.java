/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.graphics;

import java.util.ArrayList;
import thirteenducks.cor.graphics.impl.FireEmitter;

/**
 *
 * Eine kleine Hilfsklasse zum Speichern von eingelesenen Partikeldaten
 * Die Klasse speichert die Einstellungen für ein bestimmtes Gebäude
 *
 * @author tfg
 */
public class ParticleSettings {

    private ArrayList<SubSetting>[] subs;

    /**
     * Fügt eine eingelesene Einstellung hinzu.
     * Wenn schon 5 da sind, wir das einfach ignoriert
     */
    public void addSetting(int epoche, int size, int posX, int posY) {
        ArrayList<SubSetting> eSub = null;

        try {
            eSub = subs[epoche];
        } catch (Exception ex) {
        }
        if (eSub == null) {
            // Neu anlegen
            eSub = new ArrayList<SubSetting>();
            subs[epoche] = eSub;
        }
        if (eSub.size() < 5) {
            eSub.add(new SubSetting(size, posX, posY));
        }
    }

    public int getMaxFires(int epoche) {
        ArrayList<SubSetting> eSub = null;
        try {
            eSub = subs[epoche];
        } catch (Exception ex) {
        }
        if (eSub != null) {
            return eSub.size();
        }
        return 0;
    }

    public FireEmitter getSetting(int index, int epoche) {
        ArrayList<SubSetting> eSub = null;
        try {
            eSub = subs[epoche];
        } catch (Exception ex) {
        }
        if (eSub != null) {
            SubSetting set = eSub.get(index);
            return new FireEmitter(set.size, set.posX, set.posY);
        }
        return null;

    }

    /**
     * Erstellt ein neues ParticleSettings
     */
    public ParticleSettings() {
        subs = new ArrayList[5];
    }

    /**
     * Die eigentliche Datenklasse
     */
    private class SubSetting {

        private int epoche;
        private int size;
        private int posX;
        private int posY;

        public SubSetting(int size, int posX, int posY) {
            this.size = size;
            this.posX = posX;
            this.posY = posY;
        }
    }
}
