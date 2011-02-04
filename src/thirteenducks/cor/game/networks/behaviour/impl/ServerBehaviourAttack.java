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
package thirteenducks.cor.game.networks.behaviour.impl;

import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Position;

/**
 * Hier sollte etwas stehten
 *
 */
public class ServerBehaviourAttack extends ServerBehaviour {

    double lastdistance;
    public static final int maxHuntDistance = 10;
    public GameObject atkTarget;
    boolean idleMode = true;// Bei Gebäudeangriff:
    boolean useNP = false;
    boolean nPcalced = false;
    Position np;

    public ServerBehaviourAttack(ServerCore.InnerServer newinner, GameObject caster) {
        super(newinner, caster, 2, caster.getFireDelay() > 0 ? 1000.0 / caster.getFireDelay() : 1, true);
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
        // Reimplement
        System.out.println("Reimplement fighting!");
    }

    /**
     * Untersucht, ob es eine Einheit ist, die sich versteckt hat.
     * @param obj
     */
    protected boolean hides(GameObject obj) {
        if (obj.getClass().equals(Unit.class)) {
            if (((Unit) obj).isIntra()) {
                return true;
            }
        }
        return false;
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

    public void attackUnit(Unit victim) {
        // Diese Einheit jetzt zum Angriffsziel erklären
        caster.attackManager.atkTarget = victim;
        useNP = false;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster.netID, victim.netID, 0, 0));
        }
    }

    public void attackBuilding(Building victim) {
        // Dieses Gebäude zum Angriffsziel erklären
        caster.attackManager.atkTarget = victim;
        useNP = true;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster.netID, victim.netID, 0, 0));
        }
    }

    public void attackMoveUnit(Unit victim) {
        // Einheitenangriff mit festgelegtem Angriffspunkt
        // Einheit läuft bereits...
        caster.attackManager.atkTarget = victim;
        useNP = false;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster.netID, victim.netID, 0, 0));
        }
    }

    public void attackMoveBuilding(Building victim) {
        // Gebäudeangriff mit festgelegtem Angriffspunkt
        // Einheit läuft bereits
        caster.attackManager.atkTarget = victim;
        useNP = true;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster.netID, victim.netID, 0, 0));
        }
    }

    public void ialUnit(Unit victim) {
        // Einheit läuft bereits
        caster.attackManager.atkTarget = victim;
        useNP = false;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster.netID, victim.netID, 0, 0));
        }
    }

    public void ialBuilding(Building victim) {
        // Gebäudeangriff mit festgelegtem Angriffspunkt
        // Einheit läuft bereits
        caster.attackManager.atkTarget = victim;
        useNP = true;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster.netID, victim.netID, 0, 0));
        }
    }

    // Wird aufgerufen, wenn die Einheit angehalten hat
    public void moveStopped() {
        if (useNP) {
            // Vorsichtshalber neu berechnen
            nPcalced = false;
        }
        if (caster.attackManager.atkTarget == null || caster.getLifeStatus() != GameObject.LIFESTATUS_ALIVE) {
            // Idle aktiviern, falls wir davor geflohen sind
            this.setIdle(true, false);
        }
    }

    /**
     * Schält auf Idle-Modus um (oder wieder zurück)
     * Das Idle-Behaviour liegt auf dem Client, da es recht viel Leistung frisst.
     * @param idle
     */
    public void setIdle(boolean idle, boolean modifyState) {
        if (idle) {
            if (modifyState) {
                this.deactivate();
            }
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 40, caster.netID, 1, 0, 0));
        } else {
            if (modifyState) {
                this.activate();
            }
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 40, caster.netID, 2, 0, 0));
        }
    }

    /**
     * Interne Methode, findet die nächstgelegene Position eines Gebäudes, damit die Range-Berechnungen stimmen.
     * Angepasst auf das IAL-System
     * Bei Einheiten wird einfach die normale Position gesetzt
     */
    protected void refreshNearestBuildingPosition() {
        Building victim = null;
        if (caster.attackManager.atkTarget.getClass().equals(Building.class)) {
            victim = (Building) caster.attackManager.atkTarget;
        } else {
            np = caster.attackManager.atkTarget.getMainPosition();
            nPcalced = true;
            return;
        }
        int mx = caster.getMainPosition().getX();
        int my = caster.getMainPosition().getY();
        //Gebäude-Mitte finden:
        float bx = 0;
        float by = 0;
        //Z1
        //Einfach die Hälfte als Mitte nehmen
        bx = victim.getMainPosition().getX() + ((victim.getZ1() - 1) * 1.0f / 2);
        by = victim.getMainPosition().getY() - ((victim.getZ1() - 1) * 1.0f / 2);
        //Z2
        // Einfach die Hälfte als Mitte nehmen
        bx += ((victim.getZ2() - 1) * 1.0f / 2);
        by += ((victim.getZ2() - 1) * 1.0f / 2);
        // Gebäude-Mitte gefunden
        // Winkel berechnen:
        float deg = (float) Math.atan((mx - bx) / (by - my));
        // Bogenmaß in Grad umrechnen (Bogenmaß ist böse (!))
        deg = (float) (deg / Math.PI * 180);
        // In 360Grad System umrechnen (falls negativ)
        if (deg < 0) {
            deg = 360 + deg;
        }
        // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
        if (mx > bx && my > by) {
            deg -= 180;
        } else if (mx < bx && my > by) {
            deg += 180;
        }
        if (deg == 0 || deg == -0) {
            if (my > by) {
                deg = 180;
            }
        }
        // Zuteilung suchen (Ecke/Gerade(und welche?)
        if (deg < 22.5) {
            np = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1), victim.getMainPosition().getY() - (victim.getZ1() - 1));
        } else if (deg < 67.5) {
            np = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1) + ((victim.getZ2() - 1) / 2), victim.getMainPosition().getY() - (victim.getZ1() - 1) + ((victim.getZ2() - 1) / 2));
        } else if (deg < 115.5) {
            np = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1) + (victim.getZ2() - 1), victim.getMainPosition().getY() - (victim.getZ1() - 1) + (victim.getZ2() - 1));
        } else if (deg < 160.5) {
            np = new Position(victim.getMainPosition().getX() + ((victim.getZ1() - 1) / 2) + (victim.getZ2() - 1), victim.getMainPosition().getY() - ((victim.getZ1() - 1) / 2) + (victim.getZ2() - 1));
        } else if (deg < 205.5) {
            np = new Position(victim.getMainPosition().getX() + (victim.getZ2() - 1), victim.getMainPosition().getY() + (victim.getZ2() - 1));
        } else if (deg < 250.5) {
            np = new Position(victim.getMainPosition().getX() + ((victim.getZ2() - 1) / 2), victim.getMainPosition().getY() + ((victim.getZ2() - 1) / 2));
        } else if (deg < 295.5) {
            np = victim.getMainPosition();
        } else if (deg < 340.5) {
            np = new Position(victim.getMainPosition().getX() + ((victim.getZ1() - 1) / 2), victim.getMainPosition().getY() - ((victim.getZ1() - 1) / 2));
        } else {
            np = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1), victim.getMainPosition().getY() - (victim.getZ1() - 1));
        }
        nPcalced = true;
    }
}
