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
package de._13ducks.cor.networks.client.behaviour;

import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.client.ClientGameController;

/**
 * Die UDB-Version des ClientbehaviourMove.
 * Liest die Einheitenpositionen vom Server aus, irgnoriert Netzwerk-Input.
 */
public class UDBClientBehaviourMove extends ClientBehaviourMove {

    public UDBClientBehaviourMove(ClientCore.InnerClient rgi, Unit caster2) {
        super(rgi, caster2);
        active = true; // Immer an.
    }
    
    @Override
    public synchronized void execute() {
        caster2.setMainPosition(ClientGameController.udbServer.netmap.getUnitviaID(caster2.netID).getPrecisePosition());
    }
    

    @Override
    public void gotSignal(byte[] packet) {
        // Netzwerkinput ignorieren
    }
    
    @Override
    public void activate() {
        // Immer an
    }

    @Override
    public void deactivate() {
        // Immer an
    }
}
