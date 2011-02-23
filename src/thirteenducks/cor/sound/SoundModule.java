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
package thirteenducks.cor.sound;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

/**
 * Soundmodul
 * Erzeugt Geräusche.
 *
 * Das Soundmodul liest alle *.ogg-Dateien aus dem Sound-Ordner ein.
 * Mit playSound() können die Sounds dann abgespielt werden.
 * 
 * @author michael
 */
public class SoundModule {

    /**
     * Liste der geladenen Sounds
     */
    ArrayList<RogSoundModulePlayer> players;
    /**
     * gibt an, ob COR auf lautlos gestellt wurde
     */
    boolean muted = false;

    /**
     * Konstruktor
     */
    public SoundModule() {
        players = new ArrayList<RogSoundModulePlayer>();
        this.initModule();
    }

    /**
     * Initialisiert das Soundmodul
     * lädt alle *.ogg Dateien aus /sound
     */
    private void initModule() {
        // Dateien im sound-Ordner einlesen:
        File soundfolder = new File("sound");
        File soundfiles[] = soundfolder.listFiles(new FilenameFilter() {    // Nur .OGG und .ogg Dateien einlesen

            @Override
            public boolean accept(File dir, String name) {
                File testFile = new File(dir.getPath() + "/" + name);
                return testFile.isFile() && (testFile.getName().endsWith(".ogg") || testFile.getName().endsWith(".OGG"));
            }
        });

        // Liste mit Sounds initialisieren:
        for (int i = 0; i < soundfiles.length; i++) {
            players.add(new RogSoundModulePlayer(soundfiles[i].getName(), "./sound/" + soundfiles[i].getName()));
        }
    }

    /**
     * Schält das Muten um.
     * Vorsicht: Sounds werden nicht gemuted, sondern abgebrochen!
     * Nach dem unmuten werden also nur Sounds abgespielt, die neu dazu kommen.
     * Die Hintergrundmusik ist also nach einmaligem Muten weg.
     */
    public void toggleMute() {
        muted = !muted;
        if (muted) {
            // Alle anhalten
            for (RogSoundModulePlayer p : players) {
                if (p.getSound().playing()) {
                    p.getSound().stop();
                }
            }
        }
    }

    /**
     * Einen Sound für immer wiederholen
     * 
     * @param name      Name des Sounds
     */
    public void loopSound(String name) {
        if (!muted) {
            for (RogSoundModulePlayer p : players) {
                if (p.getMyname().equals(name)) {
                    if ("wolfe.ogg".equals(name)) {
                        p.getSound().loop(1.0f, 0.5f);
                    } else {
                        p.getSound().loop();
                    }
                    return;
                }
            }
            System.out.println("[Soundmodule]: Sound " + name + " not found.");
        }
    }

    /**
     * Einen Sound abspielen
     * @param name : der Name der abzuspielenden Datei im Sound-Ordner
     */
    public void playSound(String name) {
        if (!muted) {
            for (RogSoundModulePlayer p : players) {
                if (p.getMyname().equals(name)) {
                    p.getSound().play();
                    return;
                }
            }
            System.out.println("[Soundmodule]: Sound " + name + " not found.");
        }
    }

    /**
     * Hilfsklasse, enthält einen geladenen Sound
     */
    private class RogSoundModulePlayer {

        /**
         * Sound-Objekt
         * Das ist die org.newdawn.slick-Repräsentation unseres Sounds
         */
        private Sound sound;
        /**
         * Der Name des Sounds, entspricht dem Dateinamen
         */
        private String myname;

        /**
         * Konstruktor
         *
         * @param name       Name der Datei / des Sounds (z.B. "musik.ogg")
         * @param path       Pfad der Datei (z.B. "/sound/musik.ogg")
         */
        RogSoundModulePlayer(String name, String path) {
            myname = name;
            try {
                sound = new Sound(path);
            } catch (SlickException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Getter für sound
         * @return      das sound-Objekt
         */
        public Sound getSound() {
            return sound;
        }

        /**
         * Setter für sound
         * @param sound     neuer Sound
         */
        public void setSound(Sound sound) {
            this.sound = sound;
        }

        /**
         * Getter für den Namen des Sounds
         * @return      der Name des Sounds (entspricht dem Dateinamen)
         */
        public String getMyname() {
            return myname;
        }

        /**
         * Setter für den Soundnamen
         * @param myname    der neue Name
         */
        public void setMyname(String myname) {
            this.myname = myname;
        }
    }
}


