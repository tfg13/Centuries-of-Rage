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
package thirteenducks.cor.ai;

import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.client.ClientCore;

/**
 *
 * @author michael
 */
public class AIProductionManagerJobRecruitUnit extends AIProductionManagerJob {

    /**
     * Konstruktor
     */
    public AIProductionManagerJobRecruitUnit(int descID, int thePriority, ClientCore.InnerClient inner) {
        super(descID, thePriority, inner);

        try {
            for (Building b : rgi.aiModule.reconManager.getBuildings()) {
                Ability a = b.getRecruitAbility(descID);
                if (a != null) {
                    foodCost = a.costs[0];
                    woodCost = a.costs[1];
                    ironCost = a.costs[2];
                    goldCost = a.costs[3];
                }
            }
        } catch (Exception ex) {
            // tritt auf wenn kein gebäude da ist, das die einheit produzieren kann
            // @TODO: isDoableEtraCondoition gibt false zurük wenn kein entsprechendes gebäude da ist. wenn dasentsprechende gebäude existiert, sollte isDoableExtraCondition die kosten berehnen lassen
        }





    }

    @Override
    boolean isDoableExtraCondition() {
        return true;
    }

    @Override
    void beginProduction() {
        //System.out.println("recruit unit");

        rgi.aiModule.productionManager.remainingFood -= foodCost;
        rgi.aiModule.productionManager.remainingWood -= woodCost;
        rgi.aiModule.productionManager.remainingIron -= ironCost;
        rgi.aiModule.productionManager.remainingGold -= goldCost;

        for (Building b : rgi.aiModule.reconManager.getBuildings()) {
            Ability a = b.getRecruitAbility(descID);
            if (a != null) {
                a.perform(b);
            }
        }
    }
}// Klassenende

