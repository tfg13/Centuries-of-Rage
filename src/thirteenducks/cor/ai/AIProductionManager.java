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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import thirteenducks.cor.game.client.ClientCore;

/**
 * Verwaltet Einheiten- und Gebäudeproduktion
 */
public class AIProductionManager {

    // Core-Referenz:
    ClientCore.InnerClient rgi;
    ArrayList<AIProductionManagerJob> jobList;          // Liste der Produktionsaufträge
    // verbleibende ressourcen:
    int remainingFood;
    int remainingWood;
    int remainingIron;
    int remainingGold;

    /**
     * Konstruktor
     */
    public AIProductionManager(ClientCore.InnerClient inner) {
        rgi = inner;
        jobList = new ArrayList<AIProductionManagerJob>();

    }

    /**
     * nimmt produktionsaufträge in die produktionsschleife auf
     */
    void requestProduction(int descID, int priority, int count, boolean building) {
        for (int i = 0; i < count; i++) {
            if (building) {
                jobList.add(new AIProductionManagerJobConstructBuilding(descID, priority, rgi));
            } else {
                jobList.add(new AIProductionManagerJobRecruitUnit(descID, priority, rgi));
            }
        }
    }

    /**
     * Plant die Produktion der Aufträge in der jobList
     * wird in einem eigenen thread ausgeführt
     */
    void planProduction() {

        // jobList nach Priorität sortieren:
        //System.out.println("planning production... Jobs: " + jobList.size());
        Collections.sort(jobList);

        unlockRessources();
        for (int i = 0; i < jobList.size(); i++) {
            AIProductionManagerJob j = jobList.get(i);

            //System.out.println("checking job " + j.descID);
            if (j.isDoable()) {
                j.beginProduction();
                jobList.remove(j);
                i--; // durch das entfernen eines jobs rutschen alle folgenden um einen index zurück, i wird dekrementiert dass nicht einer übersprungen wird
            } else {
                //System.out.println("job not doable");
                lockRessources(j);
            }
        }
    }

    /**
     * sperrt die ressourcen für einen auftrag
     */
    private void lockRessources(AIProductionManagerJob j) {
        remainingFood -= j.foodCost;
        remainingWood -= j.woodCost;
        remainingIron -= j.ironCost;
        remainingGold -= j.goldCost;
    }

    /**
     * Entsperrt die Resourcen wieder
     */
    void unlockRessources() {
        remainingFood = rgi.game.getOwnPlayer().res1;
        remainingWood = rgi.game.getOwnPlayer().res2;
        remainingIron = rgi.game.getOwnPlayer().res3;
        remainingGold = rgi.game.getOwnPlayer().res4;
    }
}// Klassenende

