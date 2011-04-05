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
package de._13ducks.cor.game.networks.behaviour.impl;

import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Unit;

/**
 * Das Gebäude-Bauen Server Behaviour
 *
 * ID = 5;
 *
 * @author tfg
 */
public class ServerBehaviourConstruct extends ServerBehaviour {

    int duration;         // Die Dauer des Bauauftrags
    long start;           // Die Startzeit
    Building building; // Das Gebäude, das gebaut wird
    long pause = 0;       // Ob und wie lange der Bau gestoppt wird

    @Override
    public void execute() {
        // Nur für Units:
        Unit caster2;
        try {
            caster2 = (Unit) caster;
        } catch (java.lang.ClassCastException ex) {
            return;
        }
        if (start == 0 && pause == 0) {
            // Neue Startzeit, es geht los
            this.start = (long) (-(building.getBuildprogress() * duration) + System.currentTimeMillis());
            caster2.attackManager.setIdle(false, false);
            caster2.attackManager.deactivate();
        }
        // Baut das Gebäude weiter
        // Zeit bestimmen, die bereits vergangen ist
        long now = System.currentTimeMillis();
        int passed = 0;
        if (pause == 0) {
            passed = (int) (now - start);
        } else {
            int pausetime = (int) (now - pause);
            passed = (int) (now - start - pausetime);
            pause = 0;
            start = now - passed;
        }
        // Prozentsatz des Fortschritts ausrechnen
        double fortschritt = 1.0 * passed / duration;
        if (fortschritt < 0) {
            // Symthombekämpfung aufgrund von Faulheit
            start = System.currentTimeMillis();
        }
        if (fortschritt >= 1) {
            // zur Playerliste hinzufügen
            rgi.game.registerBuilding(caster.getPlayerId(), building);
            if (building.getHealRate() != 0) {
                ServerBehaviourHeal healb = new ServerBehaviourHeal(rgi, building);
                building.addServerBehaviour(healb);
            }
            // Behaviour abschalten
            this.deactivate();
            rgi.serverstats.trackBuildingbuilt(caster.getPlayerId());
            fortschritt = 1;
            // Einheit nach Zielen in der Umgebung suchen lassen
            caster2.attackManager.atkTarget = null;
            caster2.attackManager.activate();
        }
        // Soviel Energie adden:
        building.setHitpoints((int) (fortschritt * building.getMaxhitpoints() / 4 * 3) + building.getMaxhitpoints() / 4 - building.getDamageWhileContruction());
        // Gebäude-Fortschritt einstellen
        building.setBuildprogress(fortschritt);
    }

    public ServerBehaviourConstruct(ServerCore.InnerServer newinner, Unit caster, int callsPerSecond) {
        super(newinner, caster, 5, callsPerSecond, false);
    }

    @Override
    public void activate() {
        this.active = true;
        // An alle Broadcasten
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 17, caster.netID, building.netID, this.duration, 0));
    }

    @Override
    public void deactivate() {
        this.active = false;
        // Wird abgeschaltet, Status zum Client übertragen
        // Rausfinden, obs fertig war:
        if (building != null && building.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
            // Es war ferig, das Fertig-Signal schicken
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 15, caster.netID, building.netID, 0, 0));
        } else if (building != null) {
            // Es war nicht fertig, bauen wird angehalten
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 16, caster.netID, building.netID, 0, 0));
            //this.pause = System.currentTimeMillis();
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
        byte cmd = packet[0];

        switch (cmd) {
            case 16:
                // Anhalten
                this.deactivate();
                break;
            case 17:
                // Starte
                building = rgi.netmap.getBuildingviaID(rgi.readInt(packet, 2));
                duration = rgi.readInt(packet, 3);
                this.start = 0;
                this.activate();
                break;
        }
    }

    @Override
    public void pause() {
        // Zeit merken:
        // Ansonsten ist es egal, ist schon pausiert.
        if (pause == 0) {
            pause = System.currentTimeMillis();
        }
    }

    @Override
    public void unpause() {
    }
}
