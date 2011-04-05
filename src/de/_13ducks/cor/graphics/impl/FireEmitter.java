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
package de._13ducks.cor.graphics.impl;

import org.newdawn.slick.Image;
import org.newdawn.slick.particles.Particle;
import org.newdawn.slick.particles.ParticleEmitter;
import org.newdawn.slick.particles.ParticleSystem;

/**
 * Ein Feuer-Partikeleffekt
 *
 * @author tfg
 */
public class FireEmitter implements ParticleEmitter {

    /** The x coordinate of the center of the fire effect */
    private int x;
    /** The y coordinate of the center of the fire effect */
    private int y;
    /** The particle emission rate */
    private int interval = 70;
    /** Time til the next particle */
    private int timer;
    /** The size of the initial particles */
    private float size = 5;

    /**
     * Erstellt einen neuen Feuer-Effekt, passt das Feuer an das Gebäude an, sofern die ID in der fireList steht
     * @param descType
     */
    public FireEmitter() {
    }


    /**
     * Erstellt einen neuen Feuer-Effekt, passt das Feuer an das Gebäude an, sofern die ID in der fireList steht
     * @param descType
     */
    public FireEmitter(int size, int x, int y) {
        this.size = size;
        this.x = x;
        this.y = y;
    }


    public void update(ParticleSystem system, int delta) {
        timer -= delta;
        if (timer <= 0) {
            timer = interval;
            Particle p = system.getNewParticle(this, 500);
            p.setColor(1, 1, 1, 0.7f);
            p.setPosition(x, y);
            p.setSize(size);
            float vx = (float) (-0.02f + (Math.random() * 0.04f));
            float vy = (float) (-(Math.random() * 0.15f));
            p.setVelocity(vx, vy, 1.1f);
        }
    }

    public boolean completed() {
        return false;
    }

    public void wrapUp() {
    }

    public void updateParticle(Particle particle, int delta) {
        if (particle.getLife() > 300) {
            particle.adjustSize(0.07f * delta);
        } else {
            particle.adjustSize(-0.04f * delta * (size / 40.0f));
        }
        float c = 0.004f * delta;
        particle.adjustColor(0, -c / 2, -c * 2, -c / 4);
    }

    public boolean isEnabled() {
        return true;
    }

    public void setEnabled(boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean useAdditive() {
        return false;
    }

    public Image getImage() {
        return null;
    }

    public boolean isOriented() {
        return false;
    }

    public boolean usePoints(ParticleSystem system) {
        return false;
    }

    public void resetState() {
    }
}
