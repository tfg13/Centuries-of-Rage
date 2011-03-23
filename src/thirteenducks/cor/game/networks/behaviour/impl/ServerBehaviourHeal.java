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
package thirteenducks.cor.game.networks.behaviour.impl;

// Heilende Gebäude

import thirteenducks.cor.game.Position;
import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.game.GameObject;

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

	int kreis = 2;
	// Startfeld des Kreises:
	Position kreismember = Mitte1;
	if (Mitte1.getX() % 2 != Mitte1.getY() % 2) {
            kreismember.setY(kreismember.getY() - 1);
	    kreis = 3;
	}
        System.out.println("Reimplement healing!");
	// Jetzt im Kreis herum gehen
	/*for (int k = 0; true; k++) {
	    // Es gibt vier Schritte, welcher ist als nächster dran?
	    if (k == 0) {
		// Zum allerersten Feld springen
		kreismember.setY(kreismember.getY() - 2);
	    } else if (k <= (kreis)) {
		// Der nach links unten
		kreismember.setX(kreismember.getX() - 1);
		kreismember.setY(kreismember.getY() + 1);
	    } else if (k <= (kreis * 2)) {
		// rechts unten
		kreismember.setX(kreismember.getX() + 1);
		kreismember.setY(kreismember.getY() + 1);
	    } else if (k <= (kreis * 3)) {
		// rechts oben
		kreismember.setX(kreismember.getX() + 1);
		kreismember.setY(kreismember.getY() - 1);
	    } else if (k <= ((kreis * 4) - 1)) {
		// links oben
		kreismember.setX(kreismember.getX() - 1);
		kreismember.setY(kreismember.getY() - 1);
	    } else {
		// Sprung in den nächsten Kreis
		kreismember.setX(kreismember.getX() - 1);
		kreismember.setY(kreismember.getY() - 3);
		k = 0;
		kreis += 2;
		// Suchende Erreicht?
		if (kreis > 8) {
		    break;
		}
	    }
	    // Ist dieses Feld NICHT geeignet?
	    try {
		Unit Einheit = rgi.netmap.getUnitRef(kreismember, caster2.getPlayerId());
		if (Einheit == null || Einheit.getLifeStatus() != GameObject.LIFESTATUS_ALIVE) {
		} else {
		    if (Einheit.getHitpoints() + caster2.getHealRate() <= Einheit.getMaxhitpoints()) {
                        Einheit.healTo(Einheit.getHitpoints() + Einheit.getHealRate());
			rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, Einheit.netID, Einheit.getPlayerId(), Einheit.getHitpoints(), 0));
		    } else if (Einheit.getHitpoints() < Einheit.getMaxhitpoints()){
                        Einheit.healTo(Einheit.getMaxhitpoints());
			rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, Einheit.netID, Einheit.getPlayerId(), Einheit.getHitpoints(), 0));
		    }
		}
	    } catch (Exception ex) {
	    }
	} */

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
