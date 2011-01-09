/*
 *  Copyright 2008, 2009, 2010:
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

package thirteenducks.cor.graphics;

import org.newdawn.slick.Image;
import java.io.Serializable;

/**
 * Der Animatoinsmanager für Gebäude.
 * Besonderheit hier ist, dass an die Spielerfarbe angepasste Animationen verwendet werden können.
 * Deshalb liegen die BufferedImage[]-AnimationsArray zusätzlich noch in einer Hashmap.
 * Es gibt derzeit 2 Verhalten:
 * Idle
 * Working (wird nochnicht angesprochen)
 *
 * @author tfg
 */
public class BuildingAnimator implements Serializable, Cloneable {

    private boolean animidle = false;                                        // Gebäude wird Animiert, während es wartet
    private boolean animworking = false;                                     // Gebäude wird Animiert, während es arbeitet
    private boolean animdieing = false;                                      // Gebäude wird animiert, während es stirbt
    private transient Image[] idleanims;                           // Das Array mit den Bildern der Stillsteh-Animation
    private transient Image[] workinganims;                        // Das Array mit den Bildern der Arbeit-Animation
    private transient Image[] dieinganims;                         // Das Array mit den Bildern der Sterbe-Animation
    private int idlefps;                                                     // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int workingfps;                                                  // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int dieingfps;                                                   // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int mode;                                                        // Der derzeitige Animationsmodus ist...
    public static final int ANIM_IDLE = 1;                                   // ... Stillstand
    public static final int ANIM_WORKING = 2;                                // ... Arbeiten
    public static final int ANIM_DIEING = 3;                                 // ... Sterben
    private long starttime;                                                  // Wann wurde die Animation gestartet?

    // Getter - für das Grafikmodul
    public boolean isIdleAnimated() {
        return animidle;
    }

    public boolean isWorkingAnimated() {
        return animworking;
    }

    public boolean isDieingAnimated() {
        return animdieing;
    }

    public void addIdle(int framerate, Image[] anims) {
        if (anims.length > 0) {
            animidle = true;
            idlefps = 1000 / framerate;
            idleanims = anims;
        }
    }

    public void addWorking(int framerate, Image[] anims) {
        if (anims.length > 0) {
            animworking = true;
            workingfps = 1000 / framerate;
            workinganims = anims;
        }
    }

    public void addDieing(int framerate, Image[] anims) {
        if (anims.length > 0) {

            animdieing = true;
            dieingfps = 1000 / framerate;
            dieinganims = anims;
        }
    }

    public Image getNextIdleFrame(int playerId) {
        // PlayerId ok?
        if (playerId >= 0 && playerId <= 9) {
            // Schon gestartet?
            if (mode != ANIM_IDLE) {
                // Nein, erst umstellen
                mode = ANIM_IDLE;
                starttime = System.currentTimeMillis();
            }
            // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
            long now = System.currentTimeMillis() - starttime;
            int frame = (int) (now / idlefps);
            if (frame > (idleanims.length) - 1) {
                frame = 0;
                starttime = System.currentTimeMillis();
            }
            return idleanims[frame];
        }
        // PlayerId nicht ok
        return null;
    }

    public Image getNextWorkingFrame(int playerId) {
        // PlayerId ok?
        if (playerId >= 0 && playerId <= 9) {
            // Schon gestartet?
            if (mode != ANIM_WORKING) {
                // Nein, erst umstellen
                mode = ANIM_WORKING;
                starttime = System.currentTimeMillis();
            }
            // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
            long now = System.currentTimeMillis() - starttime;
            int frame = (int) (now / workingfps);
            if (frame > (workinganims.length) - 1) {
                frame = 0;
                starttime = System.currentTimeMillis();
            }
            return workinganims[frame];
        }
        // Player nicht ok
        return null;
    }

    public Image getNextDieingFrame(int playerId) {
        // Player ok?
        if (playerId >= 0 && playerId <= 9) {
            // Schon gestartet?
            if (mode != ANIM_DIEING) {
                // Nein, erst umstellen
                mode = ANIM_DIEING;
                starttime = System.currentTimeMillis();
            }
            // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
            long now = System.currentTimeMillis() - starttime;
            int frame = (int) (now / dieingfps);
            if (frame > (dieinganims.length) - 1) {
                // Hier nicht wieder anfangen, sondern immer den letzten halten
                frame = dieinganims.length - 1;
            }
            return dieinganims[frame];
        }

        // Player nicht ok
        return null;
    }

    public int getDieingDuration() {
        // Berechnet die Laufzeit, für normale Animationen wurscht, deshalb nur für die Sterbe
        return (int) ((dieinganims.length - 1) * dieingfps);
    }

    @Override
    public BuildingAnimator clone() {
        try {
            // Genau so, und nicht anders!
            // Die BufferdImage-Arrays werden nicht mirgeklont und bleiben daher so wie sie sind (abhängig).
            // Das spart viel Speicherplatz
            return (BuildingAnimator) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
