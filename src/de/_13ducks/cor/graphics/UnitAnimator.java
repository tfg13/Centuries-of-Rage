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

import org.newdawn.slick.Image;
import java.io.Serializable;

public class UnitAnimator implements Serializable, Cloneable {


    // Managed die Animationen von Einheiten komplett:
    // -Stellt Berechnungen an, um die Framerate anzupassen
    // -Enthält Referenzen zu den AnimationsBildern
    private boolean animidle = false;                      // Einheit wird Animiert, während sie stillsteht
    private boolean animmoving = false;                    // Einheit wird Animiert, während sie läuft
    private boolean animattacking = false;                 // Einheit wird Animiert, während sie angreift
    private boolean animdieing = false;                    // Einheit wird Animiert, wenn sie stirbt
    private boolean animharvesting = false;                    // Einheit wird Animiert, wenn sie stirbt
    private transient Image[][] idleanim;            // Das Array mit den Bildern der Stillsteh-Animation
    private transient Image[][] movinganim;          // Das Array mit den Bildern der Bewegungs-Animation
    private transient Image[][] harvanim;          // Das Array mit den Bildern der Bewegungs-Animation
    private transient Image[][] attackinganim;       // Das Array mit den Bildern der Angriffs-Animation
    private transient Image[] dieinganim;          // Das Array mit den Bildern der Sterbe-Animation
    private int idlefps;                                   // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int movingfps;                                 // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int attackingfps;                              // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int dieingfps;                                 // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int harvfps;                                 // Die Geschwindigkeit, in der die Animation abgespielt wird.
    private int mode;                                      // Der derzeitige Animationsmodus ist...
    public static final int ANIM_IDLE = 1;                 // ... Stillstand
    public static final int ANIM_MOVING = 2;               // ... Bewegen
    public static final int ANIM_ATTACKING = 3;            // ... Angreifen
    public static final int ANIM_DIEING = 4;               // ... Sterben
    public static final int ANIM_HARVESTING = 5;               // ... Sterben
    private long starttime;                                // Wann wude die Animation gestartet?
    public int dir;                                        // Welche Richtung?

    // Getter - für das Grafikmodul
    public boolean isIdleAnimated() {
        return animidle;
    }

    public boolean isMovingAnimated() {
        return animmoving;
    }

    public boolean isAttackingAnimated() {
        return animattacking;
    }

    public boolean isDieingAnimated() {
        return animdieing;
    }

    public boolean isHarvestingAnimated() {
        return animdieing;
    }

    // Setter
    public void addIdle(int framerate, Image[][] anim) {
        animidle = true;
        idlefps = 1000 / framerate;
        idleanim = anim;
    }

    public void addMoving(int framerate, Image[][] anim) {
        animmoving = true;
        movingfps = 1000 / framerate;
        movinganim = anim;
    }

    public void addAttacking(int framerate, Image[][] anim) {
        animattacking = true;
        attackingfps = 1000 / framerate;
        attackinganim = anim;
    }

    public void addDieing(int framerate, Image[] anim) {
        animdieing = true;
        dieingfps = 1000 / framerate;
        dieinganim = anim;
    }

    public void addHarvesting(int framerate, Image[][] anim) {
        animharvesting = true;
        harvfps = 1000 / framerate;
        harvanim = anim;
    }

    public Image getNextIdleFrame() {
        // Schon gestartet?
        if (mode != ANIM_IDLE) {
            // Nein, erst umstellen
            mode = ANIM_IDLE;
            starttime = System.currentTimeMillis();
        }
        // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
        long now = System.currentTimeMillis() - starttime;
        int frame = (int) (now / idlefps);
        if (frame > (idleanim[0].length) - 1) {
            frame = 0;
            starttime = System.currentTimeMillis();
        }
        //System.out.println("Frame: " + frame);
        try {
            return idleanim[dir][frame];
        } catch (Exception ex) {
            return idleanim[0][frame];
        }
    }

    public Image getNextMovingFrame() {
        // Schon gestartet?
        if (mode != ANIM_MOVING) {
            // Nein, erst umstellen
            mode = ANIM_MOVING;
            starttime = System.currentTimeMillis();
        }
        // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
        long now = System.currentTimeMillis() - starttime;
        int frame = (int) (now / movingfps);
        if (frame > (movinganim[0].length) - 1) {
            frame = 0;
            starttime = System.currentTimeMillis();
        }
        try {
            return movinganim[dir][frame];
        } catch (Exception ex) {
            return movinganim[0][frame];
        }
    }

    public Image getNextAttackingFrame() {
        // Schon gestartet?
        if (mode != ANIM_ATTACKING) {
            // Nein, erst umstellen
            mode = ANIM_ATTACKING;
            starttime = System.currentTimeMillis();
        }
        // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
        long now = System.currentTimeMillis() - starttime;
        int frame = (int) (now / attackingfps);
        if (frame > (attackinganim[0].length) - 1) {
            // Nicht wieder von vorne anfangen, sondern die ATK-Anim abschalten lassen
            return null;
        }
        try {
            return attackinganim[dir][frame];
        } catch (Exception ex) {
            return attackinganim[0][frame];
        }
    }

    public Image getNextDieingFrame() {
        // Schon gestartet?
        if (mode != ANIM_DIEING) {
            // Nein, erst umstellen
            mode = ANIM_DIEING;
            starttime = System.currentTimeMillis();
        }
        // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
        long now = System.currentTimeMillis() - starttime;
        int frame = (int) (now / dieingfps);
        if (frame > (dieinganim.length) - 1) {
            // Hier nicht wieder von vorne anfangen, sonder immer den letzten Frame zeigen ,die Einheit wird eh gleich gelöscht
            frame = dieinganim.length - 1;
        }
        return dieinganim[frame];
    }

    public int getDieingDuration() {
        // Berechnet die Laufzeit, für normale Animationen wurscht, deshalb nur für die Sterbe
        return (int) ((dieinganim.length - 1) * dieingfps);
    }

    public Image getNextHarvestingFrame() {
        // Schon gestartet?
        if (mode != ANIM_HARVESTING) {
            // Nein, erst umstellen
            mode = ANIM_HARVESTING;
            starttime = System.currentTimeMillis();
        }
        // Derzeitigen Frame durch Framerate, startPunkt und letzten Frame bestimmen
        long now = System.currentTimeMillis() - starttime;
        int frame = (int) (now / harvfps);
        if (frame > (harvanim[0].length) - 1) {
            frame = 0;
            starttime = System.currentTimeMillis();
        }
        try {
            return harvanim[dir][frame];
        } catch (Exception ex) {
            return harvanim[0][frame];
        }
    }

    @Override
    public UnitAnimator clone() {
        try {
            // Genau so, und nicht anders!
            // Die BufferdImage-Arrays werden nicht mirgeklont und bleiben daher so wie sie sind (abhängig).
            // Das spart viel Speicherplatz
            return (UnitAnimator) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
