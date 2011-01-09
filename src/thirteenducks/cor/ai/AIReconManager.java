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
package thirteenducks.cor.ai;

import java.util.ArrayList;
import java.util.HashMap;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.client.ClientCore;

/**
 * Analytikmodul der KI
 * Analysiert Truppenverhältnisse, Gegnerverhalten, etc...
 */
public class AIReconManager {

    // Core-Referenz:
    ClientCore.InnerClient rgi;
    Position townCenter;                        // Zentrum der Basis
    ArrayList<Unit> startWorkers;               // Startarbeiter

    // Konstruktor:
    public AIReconManager(ClientCore.InnerClient inner) {
        rgi = inner;

        initData();
    }

    /**
     * lädt/berechnet grundlegende informationen für die ki
     */
    private void initData() {
        // Zentrum der Basis bestimmen:
        for (Building b : rgi.mapModule.buildingList) {
            if (b.playerId == rgi.game.getOwnPlayer().playerId) {
                townCenter = b.position;
            }
        }

        // die startarbeiter erfassen:
        startWorkers = new ArrayList<Unit>();
        for (Unit u : rgi.mapModule.unitList) {
            if (u.playerId == rgi.game.getOwnPlayer().playerId && u.canHarvest) {
                startWorkers.add(u);
            }
        }
    }

    /**
     * Gibt das Zentrum der eigenen Basis zurück
     */
    Position getTownCenter() {
        return townCenter;
    }

    /**
     * Gibt die Liste der Startarbeiter zurück.
     */
    ArrayList<Unit> getStartWorkers() {
        return startWorkers;
    }

    /**
     * Versucht einen Busch zu finden, an dem noch ein Ernteplatz frei ist.
     * Gibt einen Busch zurück oder null, wenn keiner gefunden wurde.
     */
    Ressource getBushToHarvest() {

        for (Ressource r : rgi.mapModule.getRessourceList()) {
            //System.out.println("sadsadsad: " + r.getNextFreeHarvestingPosition(townCenter, townCenter, rgi));
            if (r.getType() == 1 && r.position.getDistance(townCenter) < 30 /*&& r.getAllHarvesters().size() < 4*/) {
                return r;
            }
        }
        System.out.println("ouch, no more bushes!");
        return null;
    }

    /**
     * Gibt die Gebäude der KI zurück
     * @return
     */
    ArrayList<Building> getBuildings() {
        ArrayList<Building> buildings = new ArrayList<Building>();
        for (Building b : rgi.mapModule.getBuildingList()) {
            if (b.playerId == rgi.game.getOwnPlayer().playerId) {
                buildings.add(b);
            }
        }
        return buildings;
    }
}// Klasssenende

