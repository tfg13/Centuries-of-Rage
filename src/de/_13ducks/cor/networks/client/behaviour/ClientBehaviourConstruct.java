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
package de._13ducks.cor.networks.client.behaviour;

import de._13ducks.cor.networks.client.behaviour.ShowsProgress;
import de._13ducks.cor.networks.client.behaviour.ClientBehaviour;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.GameObject;

/**
 * Das Gebäude bauen Client Behaviour
 *
 * Dieses Verhalten lässt ein GameObjekt ein (anderes!) Building aufbauen.
 *
 * ID = 5
 *
 * @author tfg
 */
public class ClientBehaviourConstruct extends ClientBehaviour implements ShowsProgress {

    int duration;         // Die Dauer des Bauauftrags
    long start;           // Die Startzeit
    Building building; // Das Gebäude, das gebaut wird
    long pause = 0;       // Ob und wie lange der Bau gestoppt wird

    @Override
    public void execute() {
            // Befindet sich die Einheit unmittelbar neben der Baustelle?
            if (!building.isAroundMe(caster.getMainPosition())) {
                // Das ist net gut - abbrechen
                this.deactivate();
                return;
            }
            if (start == 0 && pause == 0) {
                // Neue Startzeit, es geht los
                this.start = (long) (-(building.getBuildprogress() * duration) + System.currentTimeMillis());
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
                // Symtombekämpfung aufgrund von Faulheit
                start = System.currentTimeMillis();
            }
            if (fortschritt >= 1) {
                // Gebäude fertig, jetzt kriegts die volle Sichtweite
                building.setVisrange(rgi.mapModule.getDescBuilding(building.getDescTypeId(), -1, building.getPlayerId()).getVisrange());
                rgi.rogGraphics.builingsChanged();
                // Wenn das Gebäude das Truppenlimit erhöht, dann jetzt eintragen
                // zur Playerliste hinzufügen
                rgi.game.registerBuilding(caster.getPlayerId(), building);

                // Behaviour abschalten
                this.deactivate();
                fortschritt = 1;
            }
            // Soviel Energie adden:
            building.setHitpoints((int) (fortschritt * building.getMaxhitpoints() / 4 * 3) + building.getMaxhitpoints() / 4 - building.getDamageWhileContruction());

            // Gebäude-Fortschritt einstellen
            building.setBuildprogress(fortschritt);
        }

    public ClientBehaviourConstruct(ClientCore.InnerClient newinner, GameObject caster, int callsPerSecond, boolean createAsActive) {
        super(newinner, caster, 5, callsPerSecond, createAsActive);
    }

    @Override
    public boolean showProgess(int descTypeId) {
        if (this.building.getDescTypeId() == descTypeId && this.building.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
            return this.isActive();
        }
        return false;
    }

    @Override
    public float getProgress(int descTypeId) {
        return (float) building.getBuildprogress();
    }

    @Override
    public boolean showNumber(int descTypeId) {
        return false;
    }

    @Override
    public int getNumber(int descTypeId) {
        return 0;
    }

    @Override
    public void gotSignal(byte[] packet) {
        byte cmd = packet[0];

        switch (cmd) {
            case 15:
                // Das ist nicht notwendig, das ClientBehaviour merkt das schon selber...
                System.out.println("FixMe: Got useless signal (15) - not critical, please ignore...");
                /*
                // Gebäude fertig gebaut - Einstellen
                if (building.netID == rgi.readInt(packet, 2)) {
                // Alles ok, passt, fertig stellen
                building.ready = true;
                this.deactivate();
                } else {
                // Kann das vorkommen?
                System.out.println("FixMe: Falsches Gebäude zugeordnet, Cmd: 15");
                }
                 * */
                break;
            case 16:
                // Bau stoppen:
                if (building.netID == rgi.readInt(packet, 2)) {
                    //this.pause = System.currentTimeMillis();
                    this.deactivate();
                } else {
                    // Sollte nicht passieren
                    System.out.println("FixMe: Building-ID mismatch, Cmd: 16");
                }
                break;
            case 17:
                // Bau starten/weiter
                // Alten Bau wieder für neuen Arbeiter freimachen
                int netID = rgi.readInt(packet, 2);
                if (building == null || building.netID != netID) {
                    // Umstellen, Gebäude suchen
                    building = rgi.mapModule.getBuildingviaID(netID);
                    if (building != null) {
                        // Es geht, aktivieren
                        this.duration = rgi.readInt(packet, 3);
                        start = 0;
                        this.activate();
                        caster.getBuildAbility(building.getDescTypeId()).behaviour = this;
                    } else {
                        System.out.println("FixMe: Building ID mismatch, Cmd: 17");
                    }
                } else {
                    // Weiter:
                    start = 0;
                    this.activate();
                    caster.getBuildAbility(building.getDescTypeId()).behaviour = this;
                }
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

    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * getter für Building
     * @return
     */
    public Building getBuilding() {
        return building;
    }
}
