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
package de._13ducks.cor.networks.behaviour;

import de._13ducks.cor.game.NetPlayer;
import de._13ducks.cor.game.client.ClientCore;

/**
 * Berechnet Ressourcen-Sammelrate fpr einen bestimmten Spieler
 * @author 2nd
 */
public class GlobalBehaviourProduceClient extends GlobalBehaviour {
    double prodrate = 0.0;
    long lastupdate = -1;

    public GlobalBehaviourProduceClient (ClientCore.InnerClient newinner,  int callsPerSecond) {
        super(newinner.game.getOwnPlayer(), callsPerSecond, false);
    }
    
    @Override
    public void execute() {
        long timenow = System.currentTimeMillis();
        if (lastupdate == -1) {
            // wenn noch nicht initialisiert
            lastupdate = timenow;
        }
        long timediff = timenow - lastupdate;
        lastupdate = timenow;
        player.res1 += prodrate * timediff / 1000;
        //System.out.println("GloBhvPro " + player.playerId + " " + player.res1);
    }

    @Override
    public void activate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deactivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void gotSignal(byte[] packet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void incrementProdrate(double bla) {
        // erstmal Ressourcen aktualisieren
        this.externalExecute();
        // Sammelrate erhöhen
        prodrate += bla;
        // an Client senden
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 58, Float.floatToIntBits((float) prodrate), Float.floatToIntBits((float) player.res1), 0, 0));
    }
}
