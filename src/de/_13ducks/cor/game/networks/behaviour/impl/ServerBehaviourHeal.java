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
package de._13ducks.cor.game.networks.behaviour.impl;

// Heilende Gebäude
import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Position;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.Server;
import de._13ducks.cor.map.fastfindgrid.Traceable;
import java.util.ArrayList;

public class ServerBehaviourHeal extends ServerBehaviour {

    GameObject caster2;
    double distance;

    public ServerBehaviourHeal(ServerCore.InnerServer newinner, GameObject caster) {
        super(newinner, caster, 7, 1, true);
        caster2 = caster;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void execute() {

        // Gebäudemitte suchen
        Position Mitte1 = caster2.getCentralPosition();
        FloatingPointPosition MitteFP = new FloatingPointPosition(Mitte1);

        int kreis = 2;
        // Startfeld des Kreises:
        Position kreismember = Mitte1;
        if (Mitte1.getX() % 2 != Mitte1.getY() % 2) {
            kreismember.setY(kreismember.getY() - 1);
            kreis = 3;
        }

        // Unit1en in Umgebung suchen
        ArrayList<Traceable> Traceables = Server.getInnerServer().netmap.getFastFindGrid().getTraceablesAroundPoint(MitteFP);

        for (int i = 0; i < Traceables.size(); i++) {
            Unit Unit1 = Traceables.get(i).getUnit();
            double dx = Unit1.getPosition().getfX() - MitteFP.getfX();
            double dy = Unit1.getPosition().getfY() - MitteFP.getfY();
            // Einheit im Radius?
            if (Math.sqrt(dx * dx + dy * dy) < 30) {
                // Eigener Spieler?
                if (caster2.getPlayerId() == Unit1.getPlayerId()) {
                    // Überhaupt richtig existierend?
                    if (Unit1 != null && Unit1.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
                        if (Unit1.getHitpoints() + caster2.getHealRate() <= Unit1.getMaxhitpoints()) {
                            Unit1.healTo(Unit1.getHitpoints() + caster2.getHealRate());
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, Unit1.netID, Unit1.getPlayerId(), Unit1.getHitpoints(), 0));
                        } else if (Unit1.getHitpoints() < Unit1.getMaxhitpoints()) {
                            Unit1.healTo(Unit1.getMaxhitpoints());
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, Unit1.netID, Unit1.getPlayerId(), Unit1.getHitpoints(), 0));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
        throw new UnsupportedOperationException("The Voices are talking to me.");
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }
}
