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
package thirteenducks.cor.networks.client.behaviour.impl;

import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Unit.orders;

/**
 *
 * @author tfg
 */
public class ClientBehaviourIdle extends ClientBehaviour {

    Unit caster2;

    public ClientBehaviourIdle(ClientCore.InnerClient inner, Unit newcaster) {
        super(inner, newcaster, 4, 2, true);
        caster2 = newcaster;
    }

    @Override
    public void execute() {
        if (caster2.playerId == rgi.game.getOwnPlayer().playerId) {
            // Idle-Mode, Gegend absuchen
            if (!caster2.isIntra && !caster2.order.equals(orders.harvest)) {
                int searchRange = 5;
                // Die 1.41 ist das umrechnen von Kreisen in Abstände
                if (caster2.range / 1.41 > 5) {
                    searchRange = (int) (caster2.range / 1.41);
                }
                Unit enemy = caster2.enemyAroundMe(searchRange, rgi);
                if (enemy != null) {
                    // Zum Angriff übergehen
                    this.deactivate();
                    // Tatsächlich angreiffen
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 29, caster2.netID, enemy.netID, 1, 0));
                }
            }
        } else {
            this.deactivate();
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
}
