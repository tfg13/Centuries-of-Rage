/*
 *  Copyright 2008, 2009, 2010:
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
package thirteenducks.cor.game.ability;

import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import org.newdawn.slick.Color;
import thirteenducks.cor.networks.client.behaviour.ShowsProgress;

/**
 *
 * @author tfg
 */
public class AbilityIntraManager extends Ability implements ShowsProgress {

    public Building caster2;

    public AbilityIntraManager(Building caster2n, ClientCore.InnerClient newinner) {
        super(-2);
        this.type = Ability.ABILITY_INTRA;
        this.cooldown = 0.0;
        this.showCosts = false;
        frameColor = Color.red;
        caster2 = caster2n;
        this.symbols = new String[9];
        this.name = "Evacuate Unit(s)";
        this.behaviour = this;
        rgi = newinner;
    }

    public void updateIntra() {
        if (caster2.intraUnits.size() > 0) {
            this.symbols[0] = caster2.intraUnits.get(0).graphicsdata.defaultTexture;
        }
    }

    @Override
    public void perform(GameObject caster) {
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 42, caster2.netID, 1, 0, 0));
    }

    @Override
    public void antiperform(GameObject caster) {
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 42, caster2.netID, 2, 0, 0));
    }

    @Override
    public boolean isVisible() {
        return (caster2.intraUnits.size() > 0);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean showProgess(int descTypeId) {
        return true;
    }

    @Override
    public float getProgress(int descTypeId) {
        return 1.0f;
    }

    @Override
    public boolean showNumber(int descTypeId) {
        return true;
    }

    @Override
    public int getNumber(int descTypeId) {
        return caster2.intraUnits.size();
    }
}
