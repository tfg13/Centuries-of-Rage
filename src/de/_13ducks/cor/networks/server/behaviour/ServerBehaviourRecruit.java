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
package de._13ducks.cor.networks.server.behaviour;

import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Position;
import de._13ducks.cor.game.Unit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author tfg
 */
public class ServerBehaviourRecruit extends ServerBehaviour {

    List<Integer> loop;   // Die Bauschleife
    List<Integer> durations; // Die Dauer
    long start;                // Startzeitpunkt der aktuellen Einheit
    long pause;                // Der Zeitpunkt, zu dem das Bahaviour pausiert wurde
    float progress;            // Der aktuelle Fortschritt

    public ServerBehaviourRecruit(ServerCore.InnerServer newinner, GameObject caster, int callsPerSecond) {
        super(newinner, caster, 6, callsPerSecond, false);
        loop = Collections.synchronizedList(new ArrayList<Integer>());
        durations = Collections.synchronizedList(new ArrayList<Integer>());
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
        if (loop.size() == 0) {
            // Nichts weiter unternehmen, das ist ok so.
        }
    }

    @Override
    public void execute() {
        if (!loop.isEmpty()) {
            System.out.println("AddMe: Check for W-Status (Server-Recruit-Loop)");
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
                // Neue Unit Adden
                Unit unit = rgi.netmap.getDescUnit(caster.getPlayerId(), loop.get(0));
                if (unit != null) {
                    unit.setPlayerId(caster.getPlayerId());
                    rgi.serverstats.trackUnitrecruit(unit.getPlayerId());
                    // Auf der Richtigen Seite vom Gebäude spawnen
                    Position spawnPosition = caster.getSpawnPosition(unit, rgi);
                    FloatingPointPosition precisePosition = new FloatingPointPosition(spawnPosition);
                    unit.setMainPosition(this.rgi.netmap.getMoveMap().aroundMe(precisePosition, unit.getRadius()));
                    /*    if (caster.waypoint != null) {
                    try {
                    Building b = (Building) caster;
                    unit.position = b.getNextFreeField(caster.waypoint, rgi);
                    } catch (ClassCastException ex) {
                    }
                    } else {
                    unit.position = caster.position.aroundMe(1, rgi);
                    } */
                    rgi.netmap.addUnit(unit);
                    if (caster.getWaypoint() != null) {
                        System.out.println("Try move GO to waypoint!");
                    }
                    /*  if (caster.waypoint != null) {
                    // Ham wir was besseres zu tun als auf ein leeres Feld rennen (z.B. Ressource oder Gebäude)?
                    if (caster.wayRessource != null) {
                    // Diese Ressource oder eine gleichen Typs abernten
                    // Das soll der Client machen, nur der hat ResRefs
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 43, unit.netID, caster.wayRessource.netID, caster.waypoint.X, caster.waypoint.Y));
                    } else if (caster.wayBuilding != null) {
                    // In dieses Erntegebäude oder in eines gleichen Typs gehen
                    // Soll der Client machen
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 43, unit.netID, caster.wayBuilding.netID, caster.waypoint.X, caster.waypoint.Y));
                    } else {
                    // Da hin rennen
                    rgi.moveMan.humanSingleMove(unit, caster.waypoint.aroundMe(0, rgi), true);
                    }
                    } */
                }
                // Dieses Löschen
                loop.remove(0);
                durations.remove(0);
                start = System.currentTimeMillis();
            }
        } else {
            // Wenn Gebäude, dann auf nicht Arbeiten setzen
           /* try {
            Building b = (Building) caster;
            b.isWorking = false;
            } catch (ClassCastException ex) {
            } */
            System.out.println("AddMe: Check for W-Status (Server-Recruit-Loop)");
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
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 20, caster.netID, descid, duration, 0));
                if (loop.size() == 1) {
                    start = System.currentTimeMillis();
                }
                this.activate();
                break;
            case 22:
                // Job löschen
                int index = loop.lastIndexOf(rgi.readInt(packet, 2));
                if (index != -1) {
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 22, caster.netID, rgi.readInt(packet, 2), 0, 0));
                    loop.remove(index);
                    durations.remove(index);
                }
                break;
        }
    }

    @Override
    public void pause() {
        pause = System.currentTimeMillis();
    }

    @Override
    public void unpause() {
    }
}
