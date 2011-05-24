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
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Moveable;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import java.util.List;

/**
 * Lässt Einheitenbatzen auseinanderdriften.
 * Verhindert Klumpenbildung
 */
public class UnitDrifter extends ServerBehaviour {

    private static final double MOVE_DRIFT_FACTOR = 1.0;
    
    private Unit caster2;
    

    public UnitDrifter(Unit caster2, MovementMap mover, ServerCore.InnerServer rgi) {
        super(rgi, caster2, 3, 1, true);
        this.caster2 = caster2;
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void execute() {
        // Das Verhalten unterscheidet sich deutlich zwischen stehenden und laufenden:
        if (caster2.getLowLevelManager().isMoving()) {
            // Drift-Vektor berechnen:
            // Einheiten in der Nähe suchen:
            double searchDist = caster2.getRadius() * 2;
            List<Moveable> colliding = caster2.moversAroundMe(searchDist);
            Vector drift = Vector.ZERO.toVector(); // Kopiert den Vektor
            FloatingPointPosition me = caster2.getPrecisePosition();
            for (Moveable mover : colliding) {
                FloatingPointPosition pos = mover.getPrecisePosition();
                Vector vect = new Vector(me.x() - pos.x(), me.y() - pos.y());
                drift.addToMe(vect.multiply(MOVE_DRIFT_FACTOR / searchDist));
            }
            // Normalisieren und setzten
            if (!drift.equals(caster2.getLowLevelManager().getDriftVector())) {
                caster2.getLowLevelManager().setDrift(drift);
            }
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
    }

    public void pause() {
    }

    public void unpause() {
    }
}
