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

import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.networks.client.behaviour.ShowsProgress;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.GameObject;

/**
 * Dieses Behaviour wartet, bis die "Bauzeit" abgeschlossen ist, und schickt dann das Signal an den Server
 *
 * @author tfg
 */
public class ClientBehaviourUpgrade extends ClientBehaviour implements ShowsProgress {

    int duration;   // Dauer
    long start;     // Startzeit
    long pause;     // Pausezeit
    float progress; // Fortschritt
    public AbilityUpgrade ability;

    public ClientBehaviourUpgrade(ClientCore.InnerClient newinner, GameObject caster, AbilityUpgrade ab) {
        super(newinner, caster, 8, 20, true);
        start = System.currentTimeMillis();
        pause = 0;
        ability = ab;
        duration = ability.duration;
    }

    @Override
    public void execute() {
        // Mit der Bauschleife weitermachen
        long now = System.currentTimeMillis();
        int passed = 0;
        if (pause != 0) {
            int pausetime = (int) (now - pause);
            passed = (int) (now - start - pausetime);
            pause = 0;
            start = now - passed;
        } else {
            passed = (int) (now - start);
        }
        // Fortschritt
        this.progress = (float) (passed * 1.0 / duration);
        if (passed > duration) {
            // Fertig
            // Ist die Ability Mittlerweile gesperrt? (weil es gleichzeitung wo anders erforscht wurde?)
            if (!ability.invisibleLocked) {
                // Unlocks abhandeln:
                for (Integer i : ability.unlock) {
                    rgi.mapModule.unlockAbility(i);
                }
                for (Integer i : ability.lock) {
                    rgi.mapModule.lockAbility(i);
                }
                // "Normale" Abhängigkeiten auflösen
                if (!rgi.game.getOwnPlayer().aList.contains(ability.myId)) {
                    rgi.game.getOwnPlayer().aList.add(ability.myId);
                }
                if (ability.kind == AbilityUpgrade.upgradetype.upgrade) {
                    //toDesc-Upgrade
                    int descVal = ability.descTypeIdB;
                    if (descVal == 0) {
                        descVal = -1 * ability.descTypeIdU;
                    }
                    if (ability.affects == AbilityUpgrade.upgradeaffects.self) {
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 36, caster.netID, descVal, 1, ability.transTo));
                    } else if (ability.affects == AbilityUpgrade.upgradeaffects.old) {
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 36, caster.netID, descVal, 2, ability.transTo));
                    } else if (ability.affects == AbilityUpgrade.upgradeaffects.fresh) {
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 36, caster.netID, descVal, 3, ability.transTo));
                    } else if (ability.affects == AbilityUpgrade.upgradeaffects.all) {
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 36, caster.netID, descVal, 4, ability.transTo));
                    }
                } else if (ability.kind == AbilityUpgrade.upgradetype.deltaupgrade) {
                    // Delta
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 37, caster.netID, ability.myId, 0, 0));
                } else if (ability.kind == AbilityUpgrade.upgradetype.epoche) {
                    // Epochen-Upgrade
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 38, ability.myId, caster.getPlayerId(), 0, 0));
                }
            }
            // Selber löschen
            caster.removeClientBehaviour(this);
            this.ability.behaviour = null;
            if (!this.ability.allowMultipleUses) {
                this.ability.alreadyUsed = true;
            }
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
        System.out.println("FixMe: Irregular call at gotSignal - BehaviourUpgrade");
    }

    @Override
    public void pause() {
        pause = System.currentTimeMillis();
    }

    @Override
    public void unpause() {
    }

    @Override
    public boolean showProgess(int descTypeId) {
        return true;
    }

    @Override
    public float getProgress(int descTypeId) {
        return progress;
    }

    @Override
    public boolean showNumber(int descTypeId) {
        return false;
    }

    @Override
    public int getNumber(int descTypeId) {
        return 1;
    }
}
