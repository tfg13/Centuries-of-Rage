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
package thirteenducks.cor.networks.client.behaviour.impl;

import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Unit.actions;
import thirteenducks.cor.game.Unit.orders;

/**
 *
 * @author tfg
 */
public class ClientBehaviourHarvest extends ClientBehaviour {

    Unit caster2;
    boolean initDone = false;     // Da die Einheit erst hinlaufen muss, muss ein Init behandelt werden.
    boolean reachedHarv = false;  // True, sobald die Einheit das Ziel (die harvpos) erreicht hat.
    public Ressource res;             // Die Ressource, die geerntet wird
    Position harvpos;          // Die Position, von der aus geerntet wird.
    double diff;                  // Unterschied zwischen mehreren Aufrufen, damit auch krumme Beträge verarbeitet werden können

    public ClientBehaviourHarvest(ClientCore.InnerClient newinner, Unit caster) {
        super(newinner, caster, 7, 1, false);
        // Units only:
        caster2 = caster;
    }

    @Override
    public void execute() {
        if (!initDone) {
            // Initialtisieren
            // Bewegungziel ist Ernteziel, falls vorhanden
            if (caster2.movingtarget != null) {
                this.harvpos = caster2.movingtarget;
            } else {
                // Bewegung schon fertig Pos ist Ernteziel
                this.harvpos = caster2.position;
            }
            initDone = true;
        }
        // Einheit noch auf dem Weg?

        if (!reachedHarv) {
            // Prüfen, ob sie noch auf dem Weg ist / oder schon da
            if (caster2.position.equals(harvpos)) {
                // Angekommen
                reachedHarv = true;
            } else if (harvpos.equals(caster2.movingtarget)) {
                // Noch auf dem Weg, alles so lassen
            } else {
                // Vom Weg abgekommen, Ernten abbrechen
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 35, this.res.netID, caster.netID, 0, 0));
                /*
                this.deactivate();
                res.removeHarvester(caster);
                 */
            }
        }

        // Ressource noch da?

        if (!res.ready) {
            // Nein abbbrechen, Referenz löschen
            res = null;
            this.deactivate();
        }

        // Erntet die Einheit?
        if (reachedHarv) {
            // Ist sie noch da?
            if (caster2.position.equals(harvpos)) {
                // Erntet sie auch noch?
                if (caster2.order == orders.harvest) {
                    // Jetzt einfach den Betrag der in Player steht von der Ressource anziehen und zum Konto des Spielers tun
                    double exact = (1.0 * rgi.game.getOwnPlayer().harvspeeds[res.getType()] / 60) + diff;
                    int rounded = (int) ((1.0 * rgi.game.getOwnPlayer().harvspeeds[res.getType()] / 60) + diff);
                    // Unterschied zwischen etwa und exakt bestimmen:
                    diff = exact - rounded; // Diff wird bei den nächsten iteration berücksichtigt
                    if (rounded > res.hitpoints) {
                        // Restliche Hitpoints weg, jetzt die Ressource löschen
                        rgi.mapModule.ressourceFullyHarvested(res);
                        rounded = res.hitpoints;
                        this.deactivate();
                    }
                    res.hitpoints -= rounded;
                    rgi.clientstats.trackRes(res.getType(), rounded);
                    switch (res.getType()) {
                        case 1:
                            rgi.game.getOwnPlayer().res1 += rounded;
                            break;
                        case 2:
                            rgi.game.getOwnPlayer().res2 += rounded;
                            break;
                        case 3:
                            rgi.game.getOwnPlayer().res3 += rounded;
                            break;
                        case 4:
                            rgi.game.getOwnPlayer().res4 += rounded;
                            break;
                        case 5:
                            rgi.game.getOwnPlayer().res5 += rounded;
                            break;
                    }
                } else {
                    this.deactivate();
                    res.removeHarvester(caster);
                }
            } else {
                // Ernten abbrechen
                this.deactivate();
                res.removeHarvester(caster);
            }
        }
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

    /**
     * Stoppt das Ernten, z.B. weil die Ressource wegfällt, oder die Einheit abgezogen wird/wurde
     */
    public void stopHarvesting() {
        if (res != null) {
            res.removeHarvester(caster);
        }
        this.res = null;
        this.harvpos = null;
        caster2.order = orders.idle;
        this.deactivate();
    }

    /**
     * Startet den Erntevorgang. Die Einheit muss bereits als Harvester bei der Ressource registriert sein
     * @param harvestingPosition die Position, von der aus geerntet wird
     * @param target Die Ressource, die geerntet wird
     */
    public void startHarvesting(Position harvestingPosition, Ressource target) {
        if (target != null && harvestingPosition != null) {
            // Prüfen, ob registrierter Harvester
            if (target.isRegisteredHarvester(caster)) {
                // Position überprüfen (Fix für Fernernter)
                Position setHarvPos = target.getRegisteredHarvestPosition(caster);
                if (harvestingPosition.equals(setHarvPos)) {
                    // Alles ok, mit dem Ernten beginnen
                    this.res = target;
                    this.harvpos = harvestingPosition;
                    this.initDone = false;
                    this.reachedHarv = false;
                    caster2.order = orders.harvest;
                    this.activate();
                    caster2.action = actions.harvest;
                } else {
                    // Abbrechen
                    this.deactivate();
                    res.removeHarvester(caster);
                }
            }
        }
    }
}
