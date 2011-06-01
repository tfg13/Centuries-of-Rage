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
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.Unit;
import java.util.ArrayList;

/**
 * TopLevel-Movemanagement
 * 
 * Oberste Ebene des 3-stufigen Server-Bewegungssystems.
 * Verwaltet Einheitengruppen.
 * Teilt Gruppen dynamisch ein, erstellt als neue, wenn nötig bzw. merged bestehende zusammen.
 * Nach der Gruppeneinteilung wird der Befehl an den GruppenMoveManager (das MidLevel-Management weitergegeben)
 */
public class ServerMoveManager {

    /**
     * Die aktuelle MovementMap
     */
    private MovementMap moveMap;

    /**
     * Initialisiert den Server-Bewegungsmanager.
     * @param moveMap 
     */
    public void initMoveManager(MovementMap moveMap) {
        this.moveMap = moveMap;
    }

    /**
     * Ein Client-Moverequest geht ein.
     * @param target
     * @param movers 
     */
    public void moveRequest(FloatingPointPosition target, ArrayList<Unit> movers) {
        // TODO: Vernünftige (nicht-triviale) Gruppen-Verwaltung
        // trivial: Alle Einheiten aus ihrer alten Gruppe löschen und in eine neue einteilen
        for (Unit unit : movers) {
            synchronized (unit) {
                unit.removeFromCurrentGroup();
            }
        }
        // Neue Gruppe aufmachen und alle hinzufügen
        GroupManager man = new GroupManager(moveMap);
        for (Unit unit : movers) {
            synchronized (unit) {
                unit.setCurrentGroup(man);
            }
        }
        // Gruppe ans Ziel senden
        man.goTo(target);
    }
    // Add_some_content

    /**
     * Ein Client-Stoprequest geht ein.
     * @param unit54 Die anzuhaltende Einheit
     */
    public void stopRequest(Unit unit54) {
        synchronized (unit54) { // Nicht im laufenden Behaviour rumpfuschen
            // TODO: Bessere Gruppenverwaltung
            // Trivial: Einheit aus ihrer Gruppe werfen, dann anhalten.
            unit54.removeFromCurrentGroup();
            unit54.getLowLevelManager().stopImmediately();
        }
    }

    /**
     * Einheit soll anhalten, der Befehl kommt nicht vom Client, sondern intern vom Kampfsystem.
     */
    void stopForFight(Unit unit) {
        // Erst mal trivial implementiert (wie ein Client-Befehl)
        stopRequest(unit);
    }
}
