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
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.AbilityBuild;
import thirteenducks.cor.game.client.ClientCore;

/**
 *
 * @author michael
 */
public class AIProductionManagerJobConstructBuilding extends AIProductionManagerJob {

    /**
     * Konstruktor
     */
    public AIProductionManagerJobConstructBuilding(int descID, int priority, ClientCore.InnerClient inner) {
        super(descID, priority, inner);

        AbilityBuild b = rgi.aiModule.economyManager.myworkers.get(0).getBuildAbility(descID);

        foodCost = b.costs[0];
        woodCost = b.costs[1];
        ironCost = b.costs[2];
        goldCost = b.costs[3];

    }

    @Override
    boolean isDoableExtraCondition() {
        return true;
    }

    @Override
    void beginProduction() {

        rgi.game.getOwnPlayer().res1 -= foodCost;
        rgi.game.getOwnPlayer().res2 -= woodCost;
        rgi.game.getOwnPlayer().res3 -= ironCost;
        rgi.game.getOwnPlayer().res4 -= goldCost;

        rgi.aiModule.productionManager.remainingFood -= foodCost;
        rgi.aiModule.productionManager.remainingWood -= woodCost;
        rgi.aiModule.productionManager.remainingIron -= ironCost;
        rgi.aiModule.productionManager.remainingGold -= goldCost;
        //System.out.println("constructing Building...");

        /**
         * @TODO: Maße des Gebäudes beachten!
         */
        Building b = rgi.game.getOwnPlayer().descBuilding.get(descID);


        Position buildingSite = getBuildingSite(b.z1, b.z2);

        //System.out.println("Buildingsite is: (" + buildingSite.X + "|" + buildingSite.Y + ")");

        rgi.netctrl.broadcastDATA(rgi.aiModule.rgi.packetFactory((byte) 18, 0, descID, buildingSite.X, buildingSite.Y)); // Baustelle erstellen

        // Warten, bis der Server das Geböude registriert hat:
        Building myBuilding = null;
        int loopCount = 0;
        while (myBuilding == null) {
            try {
                Thread.sleep(50);

                // nach 200 durchgängen aufgeben:
                loopCount++;
                if (loopCount > 200) {
                    System.out.println("AI: gebäude wurde nicht registriert");
                    return;
                }

                myBuilding = rgi.mapModule.findBuilingViaPosition(buildingSite);

            } catch (Exception ex) {
            }
        }

        Unit builder = rgi.aiModule.economyManager.releaseWorker();


        // wenn es keinen entsprechenden arbeiter gibt abbrechen:
        if (builder == null) {
            System.out.println("AI: kein arbeiter für gebäudebau");
            return;
        }

        // Arbeiter hinschicken:
        builder.sendToPosition(myBuilding.getNextFreeField(builder.position, rgi), rgi, false);

        // Baubefehl geben:
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 17, builder.netID, myBuilding.netID, builder.getBuildAbility(myBuilding.descTypeId).duration, 0));                // Einheit zum bauen schicken

    }

    /**
     * gibt die nächste baupositionfür ein gebäude mit den angegebenen maßen zurück, ausgehend vom hauptgebäude
     */
    Position getBuildingSite(int width, int height) {

        // ein feld um das gebäude frei lassen:
        width += 2;
        height += 2;

        Position pos;
        int i = 1;
        while (true) {
            // nächstes Feld checken:
            pos = rgi.aiModule.reconManager.getTownCenter().aroundMe(i, rgi);
            //System.out.println("Checking Position (" +pos.X+ "|" +pos.Y+ ")");
            boolean isOk = true;
            for (int x = -1; x < width - 1; x++) {
                for (int y = -1; y < height - 1; y++) {
                    if (rgi.mapModule.isGroundColliding(pos.X + x + y, pos.Y + x - y)) {
                        isOk = false; // wenn eins der felder blockiert istkann auf dieses gebiet nicht gebaut werden
                    }
                }
            }
            // wenn gebaut werden kann position zurückgeben
            if (isOk) {
                return new Position(pos.X + 1, pos.Y + 1);
            } else {
                // ansonsten NEXT:
                i++;
            }

        }
    }
}// Klassenende

