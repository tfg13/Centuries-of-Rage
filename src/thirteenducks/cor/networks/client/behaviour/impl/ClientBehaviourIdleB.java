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

// Idle für Gebäude

import thirteenducks.cor.game.Position;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;

public class ClientBehaviourIdleB extends ClientBehaviour {

    Building caster2;

    public ClientBehaviourIdleB(ClientCore.InnerClient inner, Building newcaster) {
	super(inner, newcaster, 10, 2, true);
	caster2 = newcaster;
    }

    @Override
    public void execute() {
	float bx = 0;
	float by = 0;
	bx = caster2.position.X + ((caster2.z1 - 1) * 1.0f / 2);
	by = caster2.position.Y - ((caster2.z1 - 1) * 1.0f / 2);
	bx += ((caster2.z2 - 1) * 1.0f / 2);
	by += ((caster2.z2 - 1) * 1.0f / 2);
	Position Middle = new Position((int) bx, (int) by);
	//System.out.print(" " + caster2.position.X + " ");
	if (caster2.playerId == rgi.game.getOwnPlayer().playerId) {
	    // Idle-Mode, Gegend absuchen
	    Unit enemy = caster2.enemyAroundMe((int) (caster2.range * 1.41 + 1), Middle, rgi);
	    if (enemy != null) {
		// Angreifen
		this.deactivate();
		rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 47, caster2.netID, enemy.netID, 1, 0));
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
