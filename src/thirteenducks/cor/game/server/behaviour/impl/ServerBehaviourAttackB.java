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
package thirteenducks.cor.game.server.behaviour.impl;

import thirteenducks.cor.game.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import java.util.Timer;
import java.util.TimerTask;
import thirteenducks.cor.game.Position;

//Angriff für Gebäude
public class ServerBehaviourAttackB extends ServerBehaviour {

    Building caster2;
    double distance;
    boolean idleMode = true;

    public ServerBehaviourAttackB(ServerCore.InnerServer newinner, Building caster) {
        super(newinner, caster, 2, caster.cooldownmax > 0 ? 1000.0 / caster.cooldownmax : 1, true);
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
        // Schiessen
        GameObject workingAtk = caster2.attacktarget;
        if (workingAtk != null) {
            if (workingAtk.alive && !hides(workingAtk) && !rgi.game.areAllies(caster2, workingAtk)) {
                // Nah genug dran?
                float bx = 0;
                float by = 0;
                bx = caster2.position.X + ((caster2.z1 - 1) * 1.0f / 2);
                by = caster2.position.Y - ((caster2.z1 - 1) * 1.0f / 2);
                bx += ((caster2.z2 - 1) * 1.0f / 2);
                by += ((caster2.z2 - 1) * 1.0f / 2);
                Position Mitte1 = new Position((int) bx, (int) by);
                distance = workingAtk.position.getDistance(Mitte1);
                if (distance <= caster2.range) {
                    // Einheit muss sich wehren - Falls es ne Unit ist
                    // Units wehren sich nur, wenn sie nicht im Flie-Modus sind
                    int damage = 0;

                    if (workingAtk.getClass().equals(Unit.class)) {
                        Unit victim = (Unit) workingAtk;
                        if (victim.attacktarget == null) { // Antwort nur erzwingen, wenn kein Ziel
                            // Das hier angreiffen
                            if (!victim.moveManager.fleeing) {
				rgi.moveMan.humanSingleAttack(victim, caster2);
                            }
                        }
                        // Damage dealen, dabei Rüstungstyp und Extraschaden beachten
                        if (workingAtk.armortype.equals("lightinf")) {
                            damage = caster2.damage * caster2.antilightinf / 100;
                        } else if (workingAtk.armortype.equals("heavyinf")) {
                            damage = caster2.damage * caster2.antiheavyinf / 100;
                        } else if (workingAtk.armortype.equals("kav")) {
                            damage = caster2.damage * caster2.antikav / 100;
                        } else if (workingAtk.armortype.equals("vehicle")) {
                            damage = caster2.damage * Math.max(caster2.antivehicle, caster2.antitank) / 100;
                        } else if (workingAtk.armortype.equals("tank")) {
                            damage = caster2.damage * caster2.antitank / 100;
                        } else if (workingAtk.armortype.equals("air")) {
                            damage = caster2.damage * caster2.antiair / 100;
                        } else {
                            damage = caster2.damage;
                        }
                        // Flugzeit des Geschosses in ms
                        int atkdelay = (int) (Mitte1.getDistance(workingAtk.position) * 1000 / caster2.bulletspeed);

                        // Damage dealen
                        if (atkdelay == 0) {
                            workingAtk.hitpoints -= damage;
                            if (workingAtk.hitpoints <= 0) {
                                // Ja, Tod
                                rgi.netmap.killUnit((Unit) workingAtk, caster2.playerId);
                            }
                        } else {
                            // delayen
                            final GameObject victim2 = workingAtk;
                            final int dmg = damage;
                            new Timer().schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    victim2.hitpoints -= dmg;
                                    if (victim2.hitpoints <= 0) {
                                        // Ja, Tod 
                                        rgi.netmap.killUnit((Unit) victim2, caster2.playerId);
                                    }
                                }
                            }, atkdelay);
                        }
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 49, caster2.netID, workingAtk.netID, damage, atkdelay));
                    } else {
                        Building Geb = (Building) workingAtk;
                        float mx = 0;
                        float my = 0;
                        mx = Geb.position.X + ((Geb.z1 - 1) * 1.0f / 2);
                        my = Geb.position.Y - ((Geb.z1 - 1) * 1.0f / 2);
                        mx += ((Geb.z2 - 1) * 1.0f / 2);
                        my += ((Geb.z2 - 1) * 1.0f / 2);
                        Position Mitte2 = new Position((int) mx, (int) my);
                        damage = caster2.damage * caster2.antibuilding / 100;

                        // Flugzeit des Geschosses in ms
                        int atkdelay = 0;
                        //if (useNP) {
                        //    atkdelay = (int) (caster2.position.getDistance(np) * 1000 / caster2.bulletspeed);
                        //} else {
                        atkdelay = (int) (Mitte1.getDistance(Mitte2) * 1000 / caster2.bulletspeed);
                        //}

                        // Damage dealen
                        if (atkdelay == 0) {
                            workingAtk.hitpoints -= damage;
                            if (workingAtk.hitpoints <= 0) {
                                // Ja, Tod
                                rgi.netmap.killBuilding((Building) workingAtk, caster2.playerId);
                            }
                        } else {
                            // delayen
                            final GameObject victim = workingAtk;
                            final int dmg = damage;
                            new Timer().schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    victim.hitpoints -= dmg;
                                    if (victim.hitpoints <= 0) {
                                        // Ja, Tod
                                        rgi.netmap.killBuilding((Building) victim, caster2.playerId);
                                    }
                                }
                            }, atkdelay);
                        }
                        // An den Client senden
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 49, caster2.netID, workingAtk.netID, damage, atkdelay));
                    }

                    // Tötet das die Einheit?

                } else {
                    // Ziel zu weit weg
                    workingAtk = null;
                    // Dem Client für Debug mitteilen
                    if (rgi.isInDebugMode()) {
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 48, caster2.netID, 0, 0, 0));
                    }
                    this.setIdle(true, true);
                }
            } else {
                // Ziel lebt nimmer oder versteckt, neues suchen
                caster2.attacktarget = null;
                // Dem Client für Debug mitteilen
                if (rgi.isInDebugMode()) {
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 48, caster2.netID, 0, 0, 0));
                }
                this.setIdle(true, true);
            }
        } else {
            // Abschalten
            this.setIdle(true, true);
        }


    }

    /**
     * Untersucht, ob es eine Einheit ist, die sich versteckt hat.
     * @param obj
     */
    private boolean hides(GameObject obj) {
        if (obj.getClass().equals(Unit.class)) {
            if (((Unit) obj).isIntra) {
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

    public void attackUnit(Unit victim, boolean autoSelected) {
        // Diese Einheit jetzt zum Angriffsziel erklären
        caster2.attacktarget = victim;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 48, caster2.netID, victim.netID, 0, 0));
        }
    }

    public void attackBuilding(Building victim, boolean autoSelected) {
        // Dieses Gebäude zum Angriffsziel erklären
        caster2.attacktarget = victim;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 48, caster2.netID, victim.netID, 0, 0));
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
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 47, caster2.netID, 1, 0, 0));
        } else {
            if (modifyState) {
                this.activate();
            }
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 47, caster2.netID, 2, 0, 0));
        }
    }
}
