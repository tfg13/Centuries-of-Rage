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
package thirteenducks.cor.mainmenu;

import org.newdawn.slick.SlickException;

/**
 * Der Centuries of Rage Starter
 * Startet das Hauptmenü
 *
 * @TODO: prüfen, ob CoR überhaupt auf diesem System läuft, fehlende Komponenten (lwjgl, ...) auflisten etc
 *
 * @author  michael
 */
public class CorStarter {

    /**
     * Einstiegspunkt
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        new CorStarter();
    }

    /**
     * Konstruktor
     * liest di cfg ein und zeigt das Startsplash
     */
    public CorStarter() {
        // cfg einlesen
        new Splash(this).setVisible(true);
    }

    /**
     * Konstruktor
     * liest die Bildschirmauflösung aus und startet das grafische Hauptmenü
     */
    public void startMainMenu(String resolution, boolean fullScreen) {
        try {
            int resX = Integer.parseInt(resolution.substring(0, resolution.indexOf("*")));
            int resY = Integer.parseInt(resolution.substring(resolution.indexOf("*") + 1, resolution.length()));

            // Hauptmenü erstellen:
            MainMenu smm = new MainMenu(new MainMenuGraphics(), resX, resY, fullScreen);
        } catch (SlickException ex) {
            ex.printStackTrace();
        }
    }
}
