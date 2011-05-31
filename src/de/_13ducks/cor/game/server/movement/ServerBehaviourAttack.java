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

import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;

/**
 * Das Server-Angriffsbehaviour
 * Jede Einheit hat ihr eigenes.
 * Läuft normalerweise immer und koordiniert alles, was mit Angriff zu tun hat.
 * Also sowohl das Suchen und Verfolgen, als auch das reine Kämpfen.
 * Dieses Behaviour muss über alle Änderungen des Bewegungsmodus informiert werden, damit es sich entsprechend verhalten kann.
 * Das Behaviour hat Grundlegend 2 Modi:
 * - Normal. Die Einheit kämpft gerade, läuft zu einem Kampf oder steht herum und hält nach feinden Ausschau.
 * - F-Mode. (Flucht/Focus). Die Einheit läuft ohne sich zu wehren auf ihr Ziel zu. Dieser Modus wird automatisch verlassen, 
 *              wenn das Ziel erreicht/besiegt ist. Vorher nicht.
 */
public class ServerBehaviourAttack extends ServerBehaviour {

    public ServerBehaviourAttack(Unit caster, ServerCore.InnerServer inner) {
        super(inner, caster, 2, 5, true);
    }

    @Override
    public void execute() {
        System.out.println("ATK-Tick");
    }

    @Override
    public void activate() {
        // Ignore, dieses Behaviour wird nicht angehalten.
    }

    @Override
    public void deactivate() {
        // Ignore, dieses Behaviour wird nicht angehalten.
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
