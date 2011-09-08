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
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Moveable;
import de._13ducks.cor.game.SimplePosition;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;

/**
 * Ermöglicht es Moveables sich bewegenden Zielen zu folgen.
 * Hierzu überwacht dieses Behaviour die Bewegungen des Ziels genau
 * und manipuliert den Pfad/das Ziel des LowLevelBehaviours so, dass das
 * Ziel auf einer möglichst kurzen Route erreicht wird.
 * 
 * Ist nur aktiv, wenn tatsächlich jemand verfolgt wird.
 */
public class ServerBehaviourFollow extends ServerBehaviour {

    private static final double delta_dist_threshold = 0.1;
    private Moveable target;
    private Unit caster2;
    private FloatingPointPosition lastPosition;
    private FreePolygon lastPolygon;
    private SimplePosition lastTarget;
    // Wird gespeichert - dient zum automatischen Abschalten
    private GameObject atkTarget;

    public ServerBehaviourFollow(Unit caster, ServerCore.InnerServer rgi) {
        super(rgi, caster, 3, 5, false);
        this.caster2 = caster;
    }

    public void setTarget(Moveable target) {
        this.target = target;
        // Falls die Einheit noch läuft sofort stoppen
        if (caster2.getLowLevelManager().isMoving()) {
            caster2.getTopLevelManager().stopForFollow(caster2);
        }
        // Einheit steht jetzt.
        // Ziel setzen
        caster2.getTopLevelManager().followMove(target.getPrecisePosition(), caster2);
        // Target merken
        atkTarget = caster2.getAtkManager().getCurrentTarget();
        // lastPosition setzten
        lastPosition = target.getPrecisePosition().toFPP();
        lastTarget = target.getLowLevelManager().getTarget();
        active = true;
    }

    @Override
    public void activate() {
        // Steuert sich selbst;
    }

    @Override
    public void deactivate() {
        // Steuert sich selbst
    }

    @Override
    public void execute() {
        // Ist das, was wir tun noch ok?
        GameObject curTar = caster2.getAtkManager().getCurrentTarget();
        if ((atkTarget != null && atkTarget.equals(curTar)) || (curTar != null && curTar.equals(atkTarget) || (curTar == null && atkTarget == null))) {
            if (!caster2.getLowLevelManager().isMoving()) {
                active = false;
                return;
            }
            // Wenn wir hier sind sollen wir weiter verfolgen
            // Herausfinden, ob der Kurs geändert werden muss
            boolean refreshRoute = false;

            if (!lastTarget.equals(target.getLowLevelManager().getTarget())) {
                // Polygon gewechselt --> neue Route
                refreshRoute = true;
            } else {
                // Wie stark hat sich die Position geändert?
                double change = lastPosition.subtract(target.getPrecisePosition()).toVector().length();
                // Wie weit sind wir weg
                double ownDistance = caster2.getPrecisePosition().subtract(target.getPrecisePosition()).toVector().length();
                // Je näher wir dran sind, desto empfindlicher reagieren wir auf Änderungen
                if (change / ownDistance > delta_dist_threshold) {
                    // Änderung gravierend genug für Neuberechnung
                    refreshRoute = true;
                }
            }

            if (refreshRoute) {
                caster2.getTopLevelManager().followMove(target.getPrecisePosition(),  caster2);
                lastPosition = target.getPrecisePosition().toFPP();
                lastTarget = target.getLowLevelManager().getTarget();
            }

        } else {
            active = false;
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
        // Verwenden wird nicht
    }

    @Override
    public void pause() {
        // Betrifft uns nicht
    }

    @Override
    public void unpause() {
        // Betrifft uns nicht
    }
}
