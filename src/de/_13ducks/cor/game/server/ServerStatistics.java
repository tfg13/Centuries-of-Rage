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

package de._13ducks.cor.game.server;

/**
 *
 * @author Johannes
 */
public class ServerStatistics {
    ServerCore.InnerServer rgi;
    int[] unitsrecruited;
    int[] unitskilled;
    int[] unitslost;
    int[] buildingsbuilt;
    int[] buildingskilled;
    int[] buildingslost;

    public ServerStatistics(ServerCore.InnerServer inner) {
        rgi = inner;
    }

    public void createStatArrays(int playernumber) { //Arraylänge = Spielerzahl
	unitsrecruited = new int[playernumber + 1];
	unitskilled = new int[playernumber + 1];
	unitslost = new int[playernumber + 1];
	buildingsbuilt = new int[playernumber + 1];
	buildingskilled = new int[playernumber + 1];
	buildingslost = new int[playernumber + 1];
    }

    public void trackUnitrecruit(int player) {
	unitsrecruited[player]++;
    }

    public void trackUnitkill(int killer, int victim) {
	unitskilled[killer]++;
	unitslost[victim]++;
    }

    public void trackUnitdelete(int victim) {
	unitslost[victim]++;
    }

    public void trackBuildingbuilt(int player) {
	buildingsbuilt[player]++;
    }

    public void trackBuildingkill(int killer, int victim) {
	buildingskilled[killer]++;
	buildingslost[victim]++;
    }

    public void trackBuildingdelete(int victim) {
	buildingslost[victim]++;
    }
}
