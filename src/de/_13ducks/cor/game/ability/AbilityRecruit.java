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
package de._13ducks.cor.game.ability;

import de._13ducks.cor.game.ability.Ability;
import de._13ducks.cor.game.GameObject;

/**
 *
 * @author tfg
 */
public class AbilityRecruit extends Ability {

    float progress;  // Der Fortschritt
    public int descTypeId;  // Die DESC-ID des zu bauenden Gebäudes
    public int duration;    // Dauer des Bauens bei einem Bauarbeiter

    public AbilityRecruit(int descId) {
        super(descId);
        type = Ability.ABILITY_RECRUIT;
        cooldown = 0.0;
    }

    @Override
    public void perform(final GameObject caster) {
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 20, caster.netID, descTypeId, duration, 0));
    }

    @Override
    public AbilityRecruit clone() throws CloneNotSupportedException {
        AbilityRecruit b = (AbilityRecruit) super.clone();
        return b;
    }

    @Override
    public void antiperform(GameObject caster) {
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 22, caster.netID, descTypeId, 0, 0));
    }
}
