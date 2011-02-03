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

import thirteenducks.cor.game.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.map.CoRMapElement.collision;
import thirteenducks.cor.game.Unit;

/**
 * Das Behaviour für die Einheitenbewegung.
 * Server only, es gibt kein zugehörigies ClientBehaviour
 *
 * Verwaltet die Bewegung selbstständig.
 * Weicht aus, wenn neue Hindernisse auftauchen (Gebäude, STEHENDE Einheiten)
 * Handelt Routenänderungen damit die Einheit noch bis zum nächsten Feld weiter läuft
 *
 * Versendet automatisch alle geänderten Bewegungsinformationen an die Clients.
 *
 * Darf nicht umgangen werden.
 * Alle Bewegungsbefehle müssen an dieses Behaviour gesendet werden.
 *
 * @author tfg
 */
public class ServerBehaviourMove extends ServerBehaviour {

    Unit caster2;
    boolean reservedTarget = false;
    public boolean fleeing = false;

    public ServerBehaviourMove(ServerCore.InnerServer newinner, Unit caster) {
        super(newinner, caster, 1, 5, false);
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

            caster2.serverManagePath(rgi);
        
    }

    @Override
    public void gotSignal(byte[] packet) {
    }

    @Override
    public void pause() {
        caster2.pause();
    }

    @Override
    public void unpause() {
        caster2.unpause();
    }
}
