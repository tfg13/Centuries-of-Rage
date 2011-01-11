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
package thirteenducks.cor.game.server.behaviour.impl;

// Heilende Gebäude

import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;

public class ServerBehaviourHeal extends ServerBehaviour {

    Building caster2;
    double distance;

    public ServerBehaviourHeal(ServerCore.InnerServer newinner, Building caster) {
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
	float bx = 0;
	float by = 0;
	bx = caster2.position.X + ((caster2.z1 - 1) * 1.0f / 2);
	by = caster2.position.Y - ((caster2.z1 - 1) * 1.0f / 2);
	bx += ((caster2.z2 - 1) * 1.0f / 2);
	by += ((caster2.z2 - 1) * 1.0f / 2);
	Position Mitte1 = new Position((int) bx, (int) by);

	int kreis = 2;
	// Startfeld des Kreises:
	Position kreismember = Mitte1;
	if (Mitte1.X % 2 != Mitte1.Y % 2) {
	    kreismember.Y--;
	    kreis = 3;
	}
	// Jetzt im Kreis herum gehen
	for (int k = 0; true; k++) {
	    // Es gibt vier Schritte, welcher ist als nächster dran?
	    if (k == 0) {
		// Zum allerersten Feld springen
		kreismember.Y -= 2;
	    } else if (k <= (kreis)) {
		// Der nach links unten
		kreismember.X--;
		kreismember.Y++;
	    } else if (k <= (kreis * 2)) {
		// rechts unten
		kreismember.X++;
		kreismember.Y++;
	    } else if (k <= (kreis * 3)) {
		// rechts oben
		kreismember.X++;
		kreismember.Y--;
	    } else if (k <= ((kreis * 4) - 1)) {
		// links oben
		kreismember.X--;
		kreismember.Y--;
	    } else {
		// Sprung in den nächsten Kreis
		kreismember.X--;
		kreismember.Y -= 3;
		k = 0;
		kreis += 2;
		// Suchende Erreicht?
		if (kreis > 7 + caster2.z2) {
		    break;
		}
	    }
	    // Ist dieses Feld NICHT geeignet?
	    try {
		Unit Einheit = rgi.netmap.getUnitRef(kreismember, caster2.playerId);
		if (Einheit == null || !Einheit.alive) {
		} else {
		    if (Einheit.hitpoints + caster2.heal <= Einheit.maxhitpoints) {
			Einheit.hitpoints += caster2.heal;
			rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, Einheit.netID, Einheit.playerId, Einheit.hitpoints, 0));
		    } else if (Einheit.hitpoints < Einheit.maxhitpoints){
			Einheit.hitpoints = Einheit.maxhitpoints;
			rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, Einheit.netID, Einheit.playerId, Einheit.hitpoints, 0));
		    }
		}
	    } catch (Exception ex) {
	    }
	}

    }

    @Override
    public void gotSignal(byte[] packet) {
	throw new UnsupportedOperationException("The Voices are talking to me.");
    }

    public void pause() {
    }

    public void unpause() {
    }
}
