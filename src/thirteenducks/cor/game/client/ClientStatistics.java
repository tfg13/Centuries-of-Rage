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

package thirteenducks.cor.game.client;

/**
 *
 * @author Johannes
 */
public class ClientStatistics {
    ClientCore.InnerClient rgi;
    int[] rescollected = new int[6]; // Wieviel von jeder Ressource gesammelt worden ist
    public int[][] collectedstats; //Sammelt am Ende vom Spiel alle Statistiken

    public ClientStatistics(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    //Am Anfang des Spiels mit Spielerzahl initialisieren
    public void createStatArrays(int playernumber) {
	collectedstats = new int[playernumber + 1][12];
    }

    //Am Ende des Spiels Statistiken vom Server empfangen
    public void collectStatistics(int type, int value, int player) {
	collectedstats[player][type]=value;
    }

    //Während dem Spiel Statistiken audzeichnen
    public void trackRes(int ResType, int ResAmount) {
	rescollected[ResType] += ResAmount;
    }
}
