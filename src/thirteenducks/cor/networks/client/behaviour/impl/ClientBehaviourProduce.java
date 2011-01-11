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

/**
 *
 * @author tfg
 */
public class ClientBehaviourProduce extends ClientBehaviour {

    Building caster2;
    boolean harvesting; // Während des Ernten wirds öfters aufgrufen
    double diff = 0;    // Es können pro Zyklus nur ganzzahlige Werte verarbeitet werden, hier wird die differenz gespeichert.

    public ClientBehaviourProduce(ClientCore.InnerClient newinner, Building newcaster) {
        super(newinner, newcaster, 9, 1, true);
        caster2 = newcaster;
    }

    @Override
    public void execute() {
        // Überhaupt was tun?
        if (caster2.harvRate > 0 && caster2.harvests > 0 && caster2.playerId == rgi.game.getOwnPlayer().playerId) {
            // Ernten oder warten?
            if (caster2.intraUnits.size() > 0) {
                // Ernten
                int number = caster2.intraUnits.size();
                // Mal (Rohstoffe/Sekunde)
                double add = number * caster2.harvRate + diff; //exakt
                number = (int) ((number * caster2.harvRate) + diff); // ungefähr
                // Unterschied zwischen Soll und tatsächlichem Wert berechnen:
                diff = add - number; // Diff wird bei den nächsten iteration berücksichtigt
                // Ernten
		rgi.clientstats.trackRes(caster2.harvests, number);
                switch (caster2.harvests) {
                    case 1:
                        rgi.game.getOwnPlayer().res1 += number;
                        break;
                    case 2:
                        rgi.game.getOwnPlayer().res2 += number;
                        break;
                    case 3:
                        rgi.game.getOwnPlayer().res3 += number;
                        break;
                    case 4:
                        rgi.game.getOwnPlayer().res4 += number;
                        break;
                    case 5:
                        rgi.game.getOwnPlayer().res5 += number;
                        break;
                }
            }

        } else {
            // Abschalten
            this.deactivate();
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
    }

    @Override
    public void pause() {
        // Ganz einfach: Solange es nicht ausgeführt wird, erntet es nicht..
    }

    @Override
    public void unpause() {
    }
}
