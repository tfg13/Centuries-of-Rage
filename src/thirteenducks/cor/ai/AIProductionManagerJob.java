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

import java.util.logging.Level;
import java.util.logging.Logger;
import thirteenducks.cor.game.client.ClientCore;

/**
 * Basisklasse für Produktionsaufgaben
 */
public class AIProductionManagerJob implements Comparable<AIProductionManagerJob> {

    ClientCore.InnerClient rgi; // Core-Referenz
    int priority;               // Priorität
    int descID;                 // descID des zu produzierenden Objekts
    int foodCost;
    int woodCost;
    int ironCost;
    int goldCost;

    /**
     * Konstruktor
     */
    public AIProductionManagerJob(int id, int thePriority, ClientCore.InnerClient inner) {
        rgi = inner;

        descID = id;
        priority = thePriority;
    }

    /**
     * gibt true zurück, wenn das Objekt produziert werden kann (d.h. genug ressourcen, arbeiter, bevölkerungslimit etc da ist.)
     * @return
     */
    boolean isDoable() {
        //System.out.println("goldcost: " + goldCost + " - available gold: " + rgi.game.getOwnPlayer().res1 + " - available gold in prodman: " + rgi.aiModule.productionManager.remainingGold);
        if (isDoableExtraCondition() && foodCost <= rgi.aiModule.productionManager.remainingFood && woodCost <= rgi.aiModule.productionManager.remainingWood && ironCost <= rgi.aiModule.productionManager.remainingIron && goldCost <= rgi.aiModule.productionManager.remainingGold) {
            return true;
        }
        return false;
    }

    /**
     * Zusatzbedingung
     */
    boolean isDoableExtraCondition() {
        return true;
    }

    /**
     * wird von erweiternden klassen überschrieben
     * produziert das angegebene objekt
     */
    void beginProduction() {
        
    }

   

    public int compareTo(AIProductionManagerJob t) {
        if (t.priority < this.priority) {
            return 1;
        } else if (t.priority == this.priority) {
            return 0;
        } else if (t.priority > this.priority) {
            return -1;
        }

        // soweit sollte man nie kommen:
        try {
            throw new Exception("Oh Shit! Error while comparing!");
        } catch (Exception ex) {
            Logger.getLogger(AIProductionManagerJob.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}// Klassenende

