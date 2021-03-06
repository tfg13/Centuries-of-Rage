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
package de._13ducks.cor.graphics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.newdawn.slick.Image;
import org.newdawn.slick.particles.ParticleSystem;
import de._13ducks.cor.graphics.impl.FireEmitter;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.client.ClientCore;

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
        if (b.getHitpoints() > 0) {

            ParticleSettings settings = fireSettings.get(b.getDescTypeId());
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

                int useDamage = b.getHitpoints();
                if (b.getLifeStatus() == GameObject.LIFESTATUS_UNBORN) { // Gebäude, die gerade Gebäud werden, haben ein anderes Schadenslevel
                    useDamage = b.getHitpoints() - b.getDamageWhileContruction();
                }

                // Berechnen, wie viele Feuer wir beim derzeitigen Gesundheitszustand haben sollten
                // Feuer-Erhöhungen werden gleichmäßig über die Energispanne von 10-80% veteilt.
                int fireStart = (int) (0.8 * b.getMaxhitpoints());
                int fireAddIntervall = (int) (0.7 * b.getMaxhitpoints() / targetFires);
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
                if (rgi.game.getOwnPlayer().playerId == b.getPlayerId() || rgi.game.shareSight(b, rgi.game.getOwnPlayer())) {
                    // Grundsätzlich ja, aber ist es jetzt im Moment sichtbar?
                    if (rgi.rogGraphics.isInSight(b.getMainPosition().getX(), b.getMainPosition().getY())) {
                        // Effekt rendern
                        sys.update(delta);
                        sys.render((b.getMainPosition().getX() - positionX) * 20, (b.getMainPosition().getY() - positionY) * 15);
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
        ParticleSettings settings = fireSettings.get(b.getDescTypeId());
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
