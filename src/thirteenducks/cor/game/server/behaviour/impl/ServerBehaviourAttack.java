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
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import java.util.Timer;
import java.util.TimerTask;
import thirteenducks.cor.game.Position;

/**
 * Hier sollte etwas stehten
 *
 * @author tfg
 */
public class ServerBehaviourAttack extends ServerBehaviour {

    Unit caster2;
    double lastdistance;
    public static final int maxHuntDistance = 10;
    boolean idleMode = true;// Bei Gebäudeangriff:
    boolean useNP = false;
    boolean nPcalced = false;
    boolean instantAtk = false; // Wird gesetzt, wenn der Cooldown bereits abgelaufen ist, die Einheit aber noch nicht kämpfen konnte. Dann darf die Einheit sofort nach dem Stehenbleiben draufhauen
    Position np;

    public ServerBehaviourAttack(ServerCore.InnerServer newinner, Unit caster) {
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
        // Draufhauen
        // Echter Angriff
        final GameObject workingAtk = caster2.attacktarget;
        if (!caster2.isMoving()) {
            instantAtk = false;
            if (workingAtk != null) {
                if (workingAtk.alive && !hides(workingAtk) && !rgi.game.areAllies(caster2, workingAtk)) {
                    // Nah genug dran?
                    if (useNP && !nPcalced) {
                        refreshNearestBuildingPosition();
                    }
                    if (useNP) {
                        lastdistance = caster2.position.getDistance(np);
                    } else {
                        lastdistance = caster2.position.getDistance(workingAtk.position);
                    }
                    if (lastdistance <= caster2.getRange()) {
                        // Einheit muss sich wehren - Falls es ne Unit ist
                        // Units wehren sich nur, wenn sie nicht im Flie-Modus sind
                        int damage = 0;
                        if (workingAtk.getClass().equals(Unit.class)) {
                            Unit victim = (Unit) workingAtk;
                            if (victim.attacktarget == null) { // Antwort nur erzwingen, wenn kein Ziel
                                // Das hier angreiffen
                                if (!victim.moveManager.fleeing) {
                                    victim.attackManager.attackUnit(caster2);
                                }
                            }
                            // Damage dealen, dabei Rüstungstyp und Extraschaden beachten
                            if (workingAtk.armortype.equals("lightinf")) {
                                damage = caster2.getDamage() * caster2.antilightinf / 100;
                            } else if (workingAtk.armortype.equals("heavyinf")) {
                                damage = caster2.getDamage() * caster2.antiheavyinf / 100;
                            } else if (workingAtk.armortype.equals("kav")) {
                                damage = caster2.getDamage() * caster2.antikav / 100;
                            } else if (workingAtk.armortype.equals("vehicle")) {
                                damage = caster2.getDamage() * Math.max(caster2.antivehicle, caster2.antitank) / 100;
                            } else if (workingAtk.armortype.equals("tank")) {
                                damage = caster2.getDamage() * caster2.antitank / 100;
                            } else if (workingAtk.armortype.equals("air")) {
                                damage = caster2.getDamage() * caster2.antiair / 100;
                            } else {
                                damage = caster2.getDamage();
                            }
                            // Flugzeit des Geschosses in ms
                            int atkdelay = 0;
                            // Fernkampf?
                            if (caster2.getRange() > 2) {
                                if (useNP) {
                                    atkdelay = (int) (caster2.position.getDistance(np) * 1000 / caster2.getBulletspeed());
                                } else {
                                    atkdelay = (int) (caster2.position.getDistance(workingAtk.position) * 1000 / caster2.getBulletspeed());
                                }
                            } else {
                                // Nahkampf
                                atkdelay = caster2.getAtkdelay();
                            }
                            // Damage dealen
                            if (atkdelay == 0) {
                                workingAtk.hitpoints -= damage;
                                if (workingAtk.getHitpoints() <= 0) {
                                    // Tot
                                    rgi.netmap.killUnit((Unit) workingAtk, caster2.playerId);
                                    // Wenn das Atk-Behaviour noch kein neues Ziel hat, dann eines suchen
                                    if (caster2.getRange() <= 2) {
                                        rgi.moveMan.searchNewAtkPosForMeele(caster2);
                                    } else {
                                        rgi.moveMan.searchNewAtkPosForRange(caster2);
                                    }
                                }
                            } else {
                                // delayen
                                final GameObject victim2 = workingAtk;
                                final int dmg = damage;
                                new Timer().schedule(new TimerTask() {

                                    @Override
                                    public void run() {
                                        victim2.hitpoints -= dmg;
                                        if (victim2.getHitpoints() <= 0) {
                                            // Tot
                                            rgi.netmap.killUnit((Unit) victim2, caster2.playerId);
                                            // Wenn das Atk-Behaviour noch kein neues Ziel hat, dann eines suchen
                                            if (caster2.getRange() <= 2) {
                                                rgi.moveMan.searchNewAtkPosForMeele(caster2);
                                            } else {
                                                rgi.moveMan.searchNewAtkPosForRange(caster2);
                                            }
                                        }
                                    }
                                }, atkdelay);
                            }
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 39, caster2.netID, workingAtk.netID, damage, atkdelay));
                        } else {
                            damage = caster2.getDamage() * caster2.antibuilding / 100;
                            // Flugzeit des Geschosses in ms
                            int atkdelay = 0;
                            // Fernkampf?
                            if (caster2.getRange() > 2) {
                                if (useNP) {
                                    atkdelay = (int) (caster2.position.getDistance(np) * 1000 / caster2.getBulletspeed());
                                } else {
                                    atkdelay = (int) (caster2.position.getDistance(workingAtk.position) * 1000 / caster2.getBulletspeed());
                                }
                            } else {
                                // Nahkampf
                                atkdelay = caster2.getAtkdelay();
                            }
                            // Damage dealen
                            if (atkdelay == 0) {
                                workingAtk.hitpoints -= damage;
                                if (!workingAtk.ready) {
                                    ((Building) workingAtk).damageWhileContruction += damage;
                                }
                                if (workingAtk.getHitpoints() <= 0) {
                                    // Tot
                                    rgi.netmap.killBuilding((Building) workingAtk, caster2.playerId);
                                    // Wenn das Atk-Behaviour noch kein neues Ziel hat, dann eines suchen
                                    if (caster2.getRange() <= 2) {
                                        rgi.moveMan.searchNewAtkPosForMeele(caster2);
                                    } else {
                                        rgi.moveMan.searchNewAtkPosForRange(caster2);
                                    }
                                }
                            } else {
                                // delayen
                                final GameObject victim = workingAtk;
                                final int dmg = damage;
                                new Timer().schedule(new TimerTask() {

                                    @Override
                                    public void run() {
                                        victim.hitpoints -= dmg;
                                        if (!victim.ready) {
                                            ((Building) victim).damageWhileContruction += dmg;
                                        }
                                        if (victim.getHitpoints() <= 0) {
                                            // Tot
                                            rgi.netmap.killBuilding((Building) victim, caster2.playerId);
                                            // Wenn das Atk-Behaviour noch kein neues Ziel hat, dann eines suchen
                                            if (caster2.getRange() <= 2) {
                                                rgi.moveMan.searchNewAtkPosForMeele(caster2);
                                            } else {
                                                rgi.moveMan.searchNewAtkPosForRange(caster2);
                                            }
                                        }
                                    }
                                }, atkdelay);
                            }
                            // An den Client senden
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 39, caster2.netID, workingAtk.netID, damage, atkdelay));
                        }
                    } else {
                        // Zu weit weg
                        if (caster2.getRange() <= 2) { // Nur Nahkampf
                            rgi.moveMan.searchNewAtkPosForMeele(caster2);
                            // Hier sofort aufhören, denn es könnte:
                            // 1. Eine andere Position zugewiesen worden sein, zu der wir schon hinlaufen
                            // 2. Eine andere Einheit als Ziel erklärt worden sein, die wir direkt angreiffen können
                            // 3. Eine andere Einheit als Ziel erklärt worden sein, zu der wir jetzt hinlaufen
                            return;
                        } else {
                            rgi.moveMan.searchNewAtkPosForRange(caster2);
                            return;
                        }
                    }
                } else {
                    // Ziel lebt nimmer oder versteckt oder verbündet, neues suchen
                    caster2.attacktarget = null;
                    // Dem Client für Debug mitteilen
                    if (rgi.isInDebugMode()) {
                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster2.netID, 0, 0, 0));
                    }
                    // Server soll eine neues suchen, der kann das besser:
                    if (caster2.getRange() <= 2) {
                        rgi.moveMan.searchNewAtkPosForMeele(caster2);
                    } else {
                        rgi.moveMan.searchNewAtkPosForRange(caster2);
                    }
                }
            } else {
                // Abschalten
                this.setIdle(true, true);
            }
        } else {
            // Wir laufen, wärend wir hätten Prügeln können
            instantAtk = true; // Sofortiges Draufhauen nach dem Stehenbleiben erlauben
        }


    }

    /**
     * Untersucht, ob es eine Einheit ist, die sich versteckt hat.
     * @param obj
     */
    protected boolean hides(GameObject obj) {
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

    public void attackUnit(Unit victim) {
        // Diese Einheit jetzt zum Angriffsziel erklären
        caster2.attacktarget = victim;
        useNP = false;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster2.netID, victim.netID, 0, 0));
        }
    }

    public void attackBuilding(Building victim) {
        // Dieses Gebäude zum Angriffsziel erklären
        caster2.attacktarget = victim;
        useNP = true;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster2.netID, victim.netID, 0, 0));
        }
    }

    public void attackMoveUnit(Unit victim) {
        // Einheitenangriff mit festgelegtem Angriffspunkt
        // Einheit läuft bereits...
        caster2.attacktarget = victim;
        useNP = false;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster2.netID, victim.netID, 0, 0));
        }
    }

    public void attackMoveBuilding(Building victim) {
        // Gebäudeangriff mit festgelegtem Angriffspunkt
        // Einheit läuft bereits
        caster2.attacktarget = victim;
        useNP = true;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster2.netID, victim.netID, 0, 0));
        }
    }

    public void ialUnit(Unit victim) {
        // Einheit läuft bereits
        caster2.attacktarget = victim;
        useNP = false;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster2.netID, victim.netID, 0, 0));
        }
    }

    public void ialBuilding(Building victim) {
        // Gebäudeangriff mit festgelegtem Angriffspunkt
        // Einheit läuft bereits
        caster2.attacktarget = victim;
        useNP = true;
        nPcalced = false;
        this.setIdle(false, true);
        // Dem Client für Debug mitteilen
        if (rgi.isInDebugMode()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 13, caster2.netID, victim.netID, 0, 0));
        }
    }

    // Wird aufgerufen, wenn die Einheit angehalten hat
    public void moveStopped() {
        if (useNP) {
            // Vorsichtshalber neu berechnen
            nPcalced = false;
        }
        if (caster2.attacktarget == null || !caster2.attacktarget.alive) {
            // Idle aktiviern, falls wir davor geflohen sind
            this.setIdle(true, false);
        } else {
            // Eventuell sofort draufhauen
            if (instantAtk) {
                this.nextUse = System.currentTimeMillis();
            }
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
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 40, caster2.netID, 1, 0, 0));
        } else {
            if (modifyState) {
                this.activate();
            }
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 40, caster2.netID, 2, 0, 0));
        }
    }

    /**
     * Interne Methode, findet die nächstgelegene Position eines Gebäudes, damit die Range-Berechnungen stimmen.
     * Angepasst auf das IAL-System
     * Bei Einheiten wird einfach die normale Position gesetzt
     */
    protected void refreshNearestBuildingPosition() {
        Building victim = null;
        if (caster2.attacktarget.getClass().equals(Building.class)) {
            victim = (Building) caster2.attacktarget;
        } else {
            np = caster2.attacktarget.position;
            nPcalced = true;
            return;
        }
        int mx = caster2.position.X;
        int my = caster2.position.Y;
        //Gebäude-Mitte finden:
        float bx = 0;
        float by = 0;
        //Z1
        //Einfach die Hälfte als Mitte nehmen
        bx = victim.position.X + ((victim.z1 - 1) * 1.0f / 2);
        by = victim.position.Y - ((victim.z1 - 1) * 1.0f / 2);
        //Z2
        // Einfach die Hälfte als Mitte nehmen
        bx += ((victim.z2 - 1) * 1.0f / 2);
        by += ((victim.z2 - 1) * 1.0f / 2);
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
            np = new Position(victim.position.X + (victim.z1 - 1), victim.position.Y - (victim.z1 - 1));
        } else if (deg < 67.5) {
            np = new Position(victim.position.X + (victim.z1 - 1) + ((victim.z2 - 1) / 2), victim.position.Y - (victim.z1 - 1) + ((victim.z2 - 1) / 2));
        } else if (deg < 115.5) {
            np = new Position(victim.position.X + (victim.z1 - 1) + (victim.z2 - 1), victim.position.Y - (victim.z1 - 1) + (victim.z2 - 1));
        } else if (deg < 160.5) {
            np = new Position(victim.position.X + ((victim.z1 - 1) / 2) + (victim.z2 - 1), victim.position.Y - ((victim.z1 - 1) / 2) + (victim.z2 - 1));
        } else if (deg < 205.5) {
            np = new Position(victim.position.X + (victim.z2 - 1), victim.position.Y + (victim.z2 - 1));
        } else if (deg < 250.5) {
            np = new Position(victim.position.X + ((victim.z2 - 1) / 2), victim.position.Y + ((victim.z2 - 1) / 2));
        } else if (deg < 295.5) {
            np = victim.position;
        } else if (deg < 340.5) {
            np = new Position(victim.position.X + ((victim.z1 - 1) / 2), victim.position.Y - ((victim.z1 - 1) / 2));
        } else {
            np = new Position(victim.position.X + (victim.z1 - 1), victim.position.Y - (victim.z1 - 1));
        }
        nPcalced = true;
    }
}
