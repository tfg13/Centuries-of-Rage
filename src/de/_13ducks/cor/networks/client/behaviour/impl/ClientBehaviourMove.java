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
package de._13ducks.cor.networks.client.behaviour.impl;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.server.movement.Vector;
import de._13ducks.cor.networks.client.behaviour.ClientBehaviour;

/**
 * Der Clientmover bewegt die Einheiten gemäß den Befehlen des Servers auf dem Client.
 * Jede Einheit hat ihren eingenen Clientmover.
 * 
 */
public class ClientBehaviourMove extends ClientBehaviour {
    
    /**
     * Das derzeitige Bewegungsziel der Einheit.
     */
    private FloatingPointPosition target;
    /**
     * Die Einheit, die von diesem Behaviour verwaltet wird
     */
    private Unit caster2;
    /**
     * Die derzeitige Bewegungsgeschwindigkeit
     */
    private double speed;
    /**
     * Stoppen?
     */
    private FloatingPointPosition stopPos = null;
    /**
     * Wann wurde die Bewegung zuletzt berechnet? (in nanosekunden)
     */
    private long lastTick;
    
    
    public ClientBehaviourMove(ClientCore.InnerClient rgi, Unit caster2) {
        super(rgi, caster2, 1, 5, false);
        this.caster2 = caster2;
    }

    @Override
    public synchronized void execute() {
        // Auto-Ende:
        if (target == null || speed <= 0) {
            deactivate();
            return;
        }
        // Wir laufen also.
        // Aktuelle Position berechnen:
        FloatingPointPosition oldPos = caster2.getPrecisePosition();
        Vector vec = target.subtract(oldPos).toVector();
        vec.normalizeMe();
        long ticktime = System.nanoTime();
        vec.multiplyMe((ticktime - lastTick) / 1000000000.0 * speed);
        FloatingPointPosition newpos = vec.toFPP().add(oldPos);
        
        if (!newpos.toVector().isValid()) {
            // Das geht so nicht, abbrechen und gleich nochmal!
            System.out.println("CLIENT-Move: Invalid position, will try again next tick");
            trigger();
            return;
        }
        
        // Ziel schon erreicht?
        Vector nextVec = target.subtract(newpos).toVector();
        if (vec.isOpposite(nextVec)) {
            // ZIEL!
            // Wir sind warscheinlich drüber - egal einfach auf dem Ziel halten.
            caster2.setMainPosition(target);
            target = null;
            stopPos = null; // Es ist wohl besser auf dem Ziel zu stoppen als kurz dahinter!
            deactivate();
        } else {
            // Sofort stoppen?
            if (stopPos != null) {
                caster2.setMainPosition(stopPos);
                target = null;
                stopPos = null;
                deactivate();
            } else {
                // Weiterlaufen
                caster2.setMainPosition(newpos);
                lastTick = System.nanoTime();
            }
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }
    
    public synchronized void newMoveVec(double speed, FloatingPointPosition target) {
        this.speed = speed;
        this.target = target;
        lastTick = System.nanoTime();
        activate();
    }
    
    @Override
    public void activate() {
        active = true;
        trigger();
    }
    
    @Override
    public void deactivate() {
        active = false;
    }

    /**
     * Stoppt die Einheit sofort auf der angegeben Position
     * (innerhalb eines Ticks)
     * @param pos 
     */
    public synchronized void stopAt(FloatingPointPosition pos) {
        stopPos = pos;
        trigger();
    }
}
