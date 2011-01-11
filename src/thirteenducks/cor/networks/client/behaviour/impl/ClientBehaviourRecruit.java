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

import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import thirteenducks.cor.game.ability.AbilityRecruit;
import thirteenducks.cor.networks.client.behaviour.ShowsProgress;

/**
 *
 * @author tfg
 */
public class ClientBehaviourRecruit extends ClientBehaviour implements ShowsProgress {

    List<Integer> loop;   // Die Bauschleife
    List<Integer> durations; // Die Dauer
    long start;                // Startzeitpunkt der aktuellen Einheit
    long pause;                // Der Zeitpunkt, zu dem das Bahaviour pausiert wurde
    float progress;            // Der aktuelle Fortschritt

    public ClientBehaviourRecruit(ClientCore.InnerClient newinner, GameObject caster, int callsPerSecond) {
        super(newinner, caster, 6, callsPerSecond, false);
        loop = Collections.synchronizedList(new ArrayList<Integer>());
        durations = Collections.synchronizedList(new ArrayList<Integer>());
    }

    @Override
    public void execute() {
        if (!loop.isEmpty()) {
            // Wenn Gebäude, dann auf Arbeiten setzen
            try {
                Building b = (Building) caster;
                b.isWorking = true;
                // An die andern Schicken, damit die Animation gesetzt wird.
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 50, 2, b.netID, 1, 0));
            } catch (ClassCastException ex) {
            }
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
            long duration = durations.get(0);
            // Fortschritt
            this.progress = (float) (passed * 1.0 / duration);
            if (passed > duration) {
                // Fertig
                // Dieses Löschen
                if (caster.playerId == rgi.game.getOwnPlayer().playerId) {
                }
                loop.remove(0);
                durations.remove(0);
                start = System.currentTimeMillis();
            }
        } else {
            // Wenn Gebäude, dann auf nicht Arbeiten setzen
            try {
                Building b = (Building) caster;
                b.isWorking = false;
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 50, 2, b.netID, 0, 0));
            } catch (ClassCastException ex) {
            }
            this.deactivate();
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
        byte cmd = packet[0];
        switch (cmd) {
            case 20:
                // Neuen Job adden:
                int descid = rgi.readInt(packet, 2);
                int duration = rgi.readInt(packet, 3);
                loop.add(descid);
                durations.add(duration);
                if (loop.size() == 1) {
                    start = System.currentTimeMillis();
                }
                this.activate();
                AbilityRecruit abr = caster.getRecruitAbility(descid);
                // Ressourcen abziehen
                rgi.game.getOwnPlayer().res1 -= abr.costs[0];
                rgi.game.getOwnPlayer().res2 -= abr.costs[1];
                rgi.game.getOwnPlayer().res3 -= abr.costs[2];
                rgi.game.getOwnPlayer().res4 -= abr.costs[3];
                rgi.game.getOwnPlayer().res5 -= abr.costs[4];

                // Truppenlimit setzen
                Unit u = rgi.mapModule.getDescUnit(descid, -1, 0);
                if (u.limit > 0) {
                    rgi.game.getOwnPlayer().currentlimit += u.limit;
                }
                abr.behaviour = this;
                rgi.rogGraphics.triggerUpdateHud();
                break;
            case 22:
                // Job löschen
                int index = loop.lastIndexOf(rgi.readInt(packet, 2));
                if (index != -1) {
                    loop.remove(index);
                    durations.remove(index);
                    // Ressourcen wieder adden - falls es überhaupt lief
                    AbilityRecruit abbr = caster.getRecruitAbility(rgi.readInt(packet, 2));
                    rgi.game.getOwnPlayer().res1 += abbr.costs[0];
                    rgi.game.getOwnPlayer().res2 += abbr.costs[1];
                    rgi.game.getOwnPlayer().res3 += abbr.costs[2];
                    rgi.game.getOwnPlayer().res4 += abbr.costs[3];
                    rgi.game.getOwnPlayer().res5 += abbr.costs[4];

                    // Truppenlimit wieder abziehen
                    Unit ur = rgi.mapModule.getDescUnit(abbr.descTypeId, -1, 0);
                    if (ur.limit > 0) {
                        rgi.game.getOwnPlayer().currentlimit -= ur.limit;
                    }
                }
        }
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
        return (this.isActive() && !this.loop.isEmpty() && this.loop.contains(descTypeId));
    }

    @Override
    public float getProgress(int descTypeId) {
        if (loop.get(0).equals(new Integer(descTypeId))) {
            return progress;
        } else {
            return 0.0f;
        }
    }

    @Override
    public boolean showNumber(int descTypeId) {
        return (loop.indexOf(descTypeId) != loop.lastIndexOf(descTypeId));
    }

    @Override
    public int getNumber(int descTypeId) {
        int occ = 0;
        for (Integer i : loop) {
            if (i.equals(new Integer(descTypeId))) {
                occ++;
            }
        }
        return occ;
    }

    /**
     * Truppenlimit freigeben
     */
    public void freeLimits() {
        while (!durations.isEmpty()) {
            int last = loop.remove(0);
            durations.remove(0);
            // Ressourcen gibts nicht zurück - Pech und selber Schuld

            // Truppenlimit wieder abziehen
            Unit ur = rgi.mapModule.getDescUnit(last, -1, 0);
            if (ur.limit > 0) {
                rgi.game.getOwnPlayer().currentlimit -= ur.limit;
            }
        }
    }
}
