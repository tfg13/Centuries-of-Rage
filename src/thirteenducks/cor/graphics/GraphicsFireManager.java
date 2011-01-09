/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Image;
import org.newdawn.slick.particles.ParticleEmitter;
import org.newdawn.slick.particles.ParticleSystem;
import thirteenducks.cor.graphics.impl.FireEmitter;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.client.ClientCore;

/**
 * Steuert die Feuereffekte auf Häusern
 *
 * @author tfg
 */
public class GraphicsFireManager {

    HashMap<Building, ParticleSystem> fireMap;
    Image particle;
    ClientCore.InnerClient rgi;
    HashMap<Integer, ParticleSettings> fireSettings;

    /**
     * Muss immer aufgerufen werden, wenn ein Gebäude Schaden erleidet.
     * Aber nachdem der Schaden berchnet wurde.
     * Auch, wenn das Gebäude "gestorben" ist, damit der Effekt gelöscht wird
     * @param b
     */
    public void buildingHit(Building b, int epoche) {

        // Natürlich nicht für gestorbene
        if (b.hitpoints > 0) {

            ParticleSettings settings = fireSettings.get(b.descTypeId);
            if (settings != null) {
                // Es gibt einstellungen, haben wir schon einen Partikelemitter für dieses Gebäude?
                ParticleSystem sys = fireMap.get(b);
                if (sys == null) {
                    sys = new ParticleSystem(particle);
                    fireMap.put(b, sys);
                }
                // Wie viel Feuer sind aktiv und wie viele haben wir?
                int fires = sys.getEmitterCount();
                int targetFires = settings.getMaxFires(epoche);

                int useDamage = b.hitpoints;
                if (!b.ready) { // Gebäude, die gerade Gebäud werden, haben ein anderes Schadenslevel
                    useDamage = b.hitpoints - b.damageWhileContruction;
                }

                // Berechnen, wie viele Feuer wir beim derzeitigen Gesundheitszustand haben sollten
                // Feuer-Erhöhungen werden gleichmäßig über die Energispanne von 10-80% veteilt.
                int fireStart = (int) (0.8 * b.maxhitpoints);
                int fireAddIntervall = (int) (0.7 * b.maxhitpoints / targetFires);
                int setFires = (fireStart - useDamage) / fireAddIntervall;

                // Wenn wir weniger haben, als wir sollten, müssen wir welche hinzufügen
                while (fires < setFires) {
                    // Einmal adden
                    sys.addEmitter(settings.getSetting(fires, epoche));
                    fires++;
                }
            }

        } else {
            // Gestorben
            removeBuilding(b);
        }
    }

    /**
     * Muss aufgerufen werden, wenn sich die Epoche geändert hat
     * Dann werden die Feuer neu auf die Gebäude verteilt (was notwendig ist, weil sich ja die Textur geändert hat)
     * @param newEpoche
     */
    public void epocheChanged(int newEpoche, List<Building> buildingList) {
        fireMap.clear();
        for (int i = 0; i < buildingList.size(); i++) {
            buildingHit(buildingList.get(i), newEpoche);
        }
    }

    // Aufrufen, wenn das Gebäude weg ist, damit der Effekt entfernt wird
    private void removeBuilding(Building b) {
        try {
            fireMap.remove(b);
        } catch (NullPointerException ex) {
        }

    }

    public void renderFireEffects(List<Building> buildings, int delta, int positionX, int positionY) {
        for (int i = 0; i < buildings.size(); i++) {
            Building b = buildings.get(i);
            ParticleSystem sys = fireMap.get(b);
            if (sys != null) {
                // Sichtbar?
                if (rgi.game.getOwnPlayer().playerId == b.playerId || rgi.game.shareSight(b, rgi.game.getOwnPlayer()) || b.wasSeen) {
                    // Grundsätzlich ja, aber ist es jetzt im Moment sichtbar?
                    if (rgi.rogGraphics.isInSight(b.position.X, b.position.Y)) {
                        // Effekt rendern
                        sys.update(delta);
                        sys.render((b.position.X - b.offsetX - positionX) * 20, (b.position.Y - b.offsetY - positionY) * 15);
                    }
                }
            }
        }
    }

    public GraphicsFireManager(Image particle, ClientCore.InnerClient newinner) {
        fireMap = new HashMap<Building, ParticleSystem>();
        fireSettings = new HashMap<Integer, ParticleSettings>();
        this.particle = particle;
        rgi = newinner;

        // Effektdaten einlesen
        readEffectData();
    }

    /**
     * Liest die Daten aus /game/effectdata/particles ein
     */
    private void readEffectData() {
        BufferedReader itr = null;
        try {
            FileReader reader = new FileReader(new File("game/effectdata/particles"));
            itr = new BufferedReader(reader);
            String inputv;
            while ((inputv = itr.readLine()) != null) {
                if (inputv.startsWith("#")) {
                    continue; // Kommentar
                }
                String[] vals = inputv.split(" ");
                // Int auslesen und Settings suchen/erstellen
                int desc = Integer.parseInt(vals[0]);
                if (!fireSettings.containsKey(desc)) {
                    fireSettings.put(desc, new ParticleSettings());
                }
                fireSettings.get(desc).addSetting(Integer.parseInt(vals[1]), Integer.parseInt(vals[2]), Integer.parseInt(vals[3]), Integer.parseInt(vals[4]));
            }

        } catch (FileNotFoundException ex) {
            rgi.logger("ERROR: Missing file: game/effectdata/particles");
        } catch (IOException ex) {
        } finally {
            try {
                itr.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Erzeugt einen neuen Emitter zur gegebenen descTypeID
     * @param descTypeID
     * @return
     */
    private FireEmitter createEmitter(Building b) {

        // Settings laden - ohne gehts nicht+
        ParticleSettings settings = fireSettings.get(b.descTypeId);
        if (settings != null) {
            // Es gibt einstellungen, haben wir schon einen Partikelemitter für dieses Gebäude?
            ParticleSystem sys = fireMap.get(b);
            if (sys == null) {
                sys = new ParticleSystem(particle);
            }

        }


        return new FireEmitter();
    }
}
