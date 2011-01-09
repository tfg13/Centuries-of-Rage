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
import thirteenducks.cor.map.CoRMapElement.collision;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Unit.orders;

/**
 * Das Behaviour für die Einheitenbewegung.
 * Server only, es gibt kein zugehörigies ClientBehaviour
 *
 * Verwaltet die Bewegung selbstständig.
 * Weicht aus, wenn neue Hindernisse auftauchen (Gebäude, STEHENDE Einheiten)
 * Handelt Routenänderungen damit die Einheit noch bis zum nächsten Feld weiter läuft
 *
 * Versendet automatisch alle geänderten Bewegungsinformationen an die Clients.
 *
 * Darf nicht umgangen werden.
 * Alle Bewegungsbefehle müssen an dieses Behaviour gesendet werden.
 *
 * @author tfg
 */
public class ServerBehaviourMove extends ServerBehaviour {

    Unit caster2;
    boolean reservedTarget = false;
    public boolean fleeing = false;

    public ServerBehaviourMove(ServerCore.InnerServer newinner, Unit caster) {
        super(newinner, caster, 1, 5, false);
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

        synchronized (caster2.pathSync) {

            boolean gotError = false;
            int trys = 0;

            do {
                trys++;
                gotError = false;
                try {
                    double length = caster2.getWayLength();
                    long passedTime = 0;
                    if (caster2.movePaused) {
                        passedTime = caster2.pauseTime - caster2.startTime;
                    } else {
                        passedTime = System.currentTimeMillis() - caster2.startTime;
                    }
                    double passedWay = passedTime * caster2.speed / 1000;
                    // Schon fertig?
                    if (passedWay >= length) {
                        // Fertig, Bewegung stoppen
                        caster2.position = caster2.movingtarget;
                        caster2.movingtarget = null;
                        caster2.path = null;
                        caster2.order = orders.idle;
                        rgi.netmap.setCollision(caster2.position, collision.occupied);
                        rgi.netmap.setUnitRef(caster2.position, caster2, caster2.playerId);
                        caster2.attackManager.moveStopped();
                        this.deactivate();
                        // Jumpen?
                        if (caster2.jumpTo != 0) {
                            // Jump-Ziel noch da?
                            Building jumptar = rgi.netmap.getBuildingviaID(caster2.jumpTo);
                            if (jumptar != null && jumptar.alive && jumptar.ready && jumptar.intraFree() > 0 && jumptar.playerId == caster2.playerId) {
                                // Jumpen lassen
                                jumptar.addIntra(caster2, rgi);
                                // Broadcast
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 41, caster2.netID, jumptar.netID, 0, 0));
                            }
                            caster2.jumpTo = 0;
                            caster2.jumpJustSet = false;
                        }
                        return;
                    }
                    // Zuletzt erreichten Wegpunkt finden
                    if (passedWay >= caster2.nextwaypoint) {
                        // Sind wir einen weiter oder mehrere
                        int weiter = 1;
                        while (passedWay > caster2.pathOrder.get(caster2.lastwaypoint + 1 + weiter)) {
                            weiter++;
                        }
                        caster2.lastwaypoint += weiter;
                        if (caster2.changespeedto != 0) {
                            // Ja, alte Felder löschen & neu berechnen
                            for (; caster2.lastwaypoint > 0; caster2.lastwaypoint--) {
                                caster2.path.remove(0);
                            }
                            caster2.speed = caster2.changespeedto;
                            caster2.changespeedto = 0;
                            caster2.lastwaypoint = 0;
                            caster2.startTime = System.currentTimeMillis();
                            caster2.calcWayLength();
                        }
                        caster2.nextwaypoint = caster2.pathOrder.get(caster2.lastwaypoint + 1);
                        caster2.position = caster2.path.get(caster2.lastwaypoint);

                        // Ziel in Reichweite? (für Anhalten)
                        if (!this.fleeing && caster2.attacktarget != null && caster2.attackManager.active && caster2.attacktarget.alive && !caster2.attackManager.hides(caster2.attacktarget)) {
                            // Nah genug dran?
                            if (caster2.attackManager.useNP && !caster2.attackManager.nPcalced) {
                                caster2.attackManager.refreshNearestBuildingPosition();
                            }
                            if (caster2.attackManager.useNP) {
                                caster2.attackManager.lastdistance = caster2.position.getDistance(caster2.attackManager.np);
                            } else {
                                caster2.attackManager.lastdistance = caster2.position.getDistance(caster2.attacktarget.position);
                            }
                            if (caster2.attackManager.lastdistance <= caster2.range) {
                                // Anhalten
                                rgi.moveMan.autoMoveStop(caster2);
                            }
                        }
                        // Neuer Wegpunkt! - Berechnungen durchführen:

                        // Ziel nichtmehr frei?

                        /*          if (rgi.netmap.isGroundColliding(caster2.movingtarget) || (!reservedTarget && rgi.netmap.checkFieldReservation(caster2.movingtarget))) {
                        if (caster2.jumpTo == 0) { // Nicht bei jumps
                        // Weg zu neuem Ziel berechnen
                        RogPosition pos = caster2.movingtarget.aroundMe(1, rgi);
                        // Reservierung nur löschen, wenn wir die selber schon eingetragen haben
                        if (reservedTarget) {
                        rgi.netmap.deleteFieldReservation(caster2.movingtarget);
                        reservedTarget = false;
                        }
                        boolean ret = false;
                        if (caster2.position.getDistance(pos) < 100) {
                        ret = caster2.moveToPosition(pos, rgi, true);
                        } else {
                        ret = caster2.moveToPosition(pos, rgi, false);
                        }
                        if (!ret) {
                        // Das Ausweichen ging nicht, die Einheit ist ziemlich "gelockt".
                        // Versuche die Einheit da wo sie derzeit ist anzuhalten
                        System.out.println("FixMe: Units target blocked, but can't change path! Trying to stop...");
                        boolean ret2 = caster2.moveToPosition(caster2.position.aroundMe(0, rgi), rgi, true);
                        if (!ret2) {
                        // Da hilft nix mehr...
                        System.out.println("FixMe: Unit can't move anywhere, this may result in 2 units on one field - SRY!");
                        }
                        }
                        }
                        } */

                        // Einheit noch nicht am Ziel angekommen - übernächstes Feld frei?
                        // Fürt zu sinnlosem rumgerenne - son mist
                        // (Nächstes braucht man nicht zu prüfen, da rennen wir ja eh schon hin...

                        /*if (caster2.lastwaypoint < caster2.path.size() - 3) {
                        // Feld prüfen
                        if (rgi.netmap.isGroundColliding(caster2.path.get(caster2.lastwaypoint + 2))) {
                        // Blockiert, Weg neu suchen
                        //System.out.println(System.currentTimeMillis() + ": Way to " + caster2.movingtarget + " is blocked, re-calcing...");
                        //caster2.moveToPosition(caster2.movingtarget, rgi);
                        }
                        } */

                    }
                } catch (Exception ex) {
                    // Fehler, mit Ziemlicher Sicherheit wurde was von nem anderen Thread geändert, während dieser durchlief.
                    // Darum einfach noch mal versuchen
                    if (trys < 2) {
                        gotError = true;
                        System.out.println("Error in SMoveBehaviour, trying again...");
                        rgi.logger(ex);
                    } else {
                        gotError = false;
                        System.out.println("Critical: 2nd try in SMoveBehaviour didn't help.");
                    }
                }
            } while (gotError);
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
    }

    @Override
    public void pause() {
        caster2.pause();
    }

    @Override
    public void unpause() {
        caster2.unpause();
    }
}
