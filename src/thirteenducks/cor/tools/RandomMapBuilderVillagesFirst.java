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
package thirteenducks.cor.tools;

import java.util.ArrayList;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.DescParamsBuilding;
import thirteenducks.cor.game.DescParamsUnit;
import thirteenducks.cor.game.PlayersBuilding;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Unit2x2;
import thirteenducks.cor.map.CoRMapElement.collision;

/**
 * Setzt für jeden Spieler ein Startdorf
 * @author Johannes
 */
public class RandomMapBuilderVillagesFirst extends RandomMapBuilderJob {

    @Override
    public void performJob() {

	//Start-Gebäude am Maprand in gleichen Abständen platzieren!

	ArrayList<Position> Frei = new ArrayList<Position>(); //Arraylist mit allen möglichen Startpositionen
	ArrayList<Building> StartG = new ArrayList<Building>();//Arraylist mit den endgültigen Startgebäuden

	for (int i = 1; i <= RandomMapBuilder.RandomRogMap.getPlayernumber(); i++) {	//für jeden Spieler:

	    Frei.clear();
	    for (int u = 2; u < RandomMapBuilder.RandomRogMap.getMapSizeX() - 24; u++) {
		for (int j = 14; j < RandomMapBuilder.RandomRogMap.getMapSizeY() - 14; j++) {
		    if (u % 2 != j % 2) {
			continue;
		    }
		    boolean frei = true;

		    int counter = 0;
		    for (int z1c = 0; z1c < 12; z1c++) {
			for (int z2c = 0; z2c < 12; z2c++) {
			    if (RandomMapBuilder.RandomRogMap.visMap[u + z1c + z2c][j - z1c + z2c].getCollision().equals(collision.blocked)) {
				frei = false;
			    }
			}
		    }

		    if (frei) {
			Frei.add(new Position(u, j)); //mögliche Startpositionen finden
		    }
		}
	    }

	    if (i == 1) {

		double[] dist = new double[Frei.size()]; // Distanz zum nächsten Hauptgebäude

		for (int z = 0; z < Frei.size(); z++) { //für jedes Feld Distanz zum Rand berechnen
		    dist[z] = Math.min(Math.min(Frei.get(z).getX(), Frei.get(z).getY()), Math.min(RandomMapBuilder.RandomRogMap.getMapSizeX() - Frei.get(z).getX(), RandomMapBuilder.RandomRogMap.getMapSizeY() - Frei.get(z).getY()));
		}

		double mittel = 0;
		for (int q = 0; q < dist.length; q++) {
		    mittel += dist[q];
		}
		mittel /= dist.length;

		double mind = 9999;
		for (int q = 0; q < dist.length; q++) {
		    if (mind > dist[q]) {
			mind = dist[q];
		    }
		}

		double xcvbn = 0.4;
		double low = (1 - xcvbn) * mittel + xcvbn * mind;

		for (int q = 0; q < Frei.size(); q++) {
		    if (dist[q] > low) {
			Frei.get(q).setX(Frei.get(q).getX() - 1);
		    }
		}
		int w = 0;
		while (w < Frei.size()) {
		    if (Frei.get(w).getX() == -1) {
			Frei.remove(w); //Felder mit zu geringer Distanz löschen
		    } else {
			w++;
		    }
		}

	    } else {

		double[] dist = new double[Frei.size()]; // Distanz zum nächsten Hauptgebäude
		double[] work = new double[RandomMapBuilder.RandomRogMap.getPlayernumber()];

		for (int z = 0; z < Frei.size(); z++) { //für jedes Feld Distanz zu allen Hauptgebäuden berechnen
		    for (int d = 0; d < i - 1; d++) {
			work[d] = Math.sqrt(Math.pow(Frei.get(z).getX() - StartG.get(d).getMainPosition().getX() + 1, 2) + Math.pow(Frei.get(z).getY() - StartG.get(d).getMainPosition().getY(), 2));
		    }
		    dist[z] = 9999;
		    for (int e = 0; e < i - 1; e++) {
			if (work[e] < dist[z]) {
			    dist[z] = work[e];
			} // kleinste Distanz herausfinden
		    }
		}

		double mittel = 0;
		for (int q = 0; q < dist.length; q++) {
		    mittel += dist[q];
		}
		mittel /= dist.length;

		double maxd = 0;
		for (int q = 0; q < dist.length; q++) {
		    if (maxd < dist[q]) {
			maxd = dist[q];
		    }
		}

		double xcvbn = 0.6 + (0.05 * i) - (0.05 * RandomMapBuilder.RandomRogMap.getPlayernumber());
		double high = (1 - xcvbn) * mittel + xcvbn * maxd;

		for (int q = 0; q < Frei.size(); q++) {
		    if (dist[q] < high) {
			Frei.get(q).setX(Frei.get(q).getX() - 1);
		    }
		}
		int w = 0;
		while (w < Frei.size()) {
		    if (Frei.get(w).getX() == -1) {
			Frei.remove(w); //Felder mit zu geringer Distanz löschen
		    } else {
			w++;
		    }
		}
	    }

	    double RndStartD = Math.random() * Frei.size(); //zufällige Startposition aus der Frei-Arraylist
	    int RndStart = (int) RndStartD;
	    int x = Frei.get(RndStart).getX();
	    int y = Frei.get(RndStart).getY();
	    System.out.println("Pos " + x + " " + y);

	    DescParamsBuilding param = new DescParamsBuilding();

	    //Haus an diese Position setzen

	    param.setDescTypeId(1);
	    param.setDescName("Village Center");
	    param.setHitpoints(2000);
	    param.setMaxhitpoints(2000);

	    param.setZ1(12);
	    param.setZ2(12);


	    PlayersBuilding tmp = new PlayersBuilding(param);
	    PlayersBuilding Haus = new PlayersBuilding(RandomMapBuilder.RandomRogMap.getNewNetID(), tmp);
	    Haus.getGraphicsData().offsetY = 8;
	    Haus.setPlayerId(i);
	    Haus.getGraphicsData().defaultTexture = "img/buildings/human_main_e1.png";
	    Haus.setMainPosition(new Position(x, y).valid() ? new Position(x, y) : new Position(x + 1, y));


	    for (int z1c = 0; z1c < 12; z1c++) {
		for (int z2c = 0; z2c < 12; z2c++) {
		    RandomMapBuilder.RandomRogMap.visMap[Haus.getMainPosition().getX() + z1c + z2c][Haus.getMainPosition().getY() - z1c + z2c].setCollision(collision.blocked);
		}
	    }

	    StartG.add(Haus); //Startgebäude in Arraylist eintragen
	}
	
	ArrayList<Building> buildingList = (ArrayList<Building>) RandomMapBuilder.RandomRogMap.getMapPoperty("BUILDING_LIST");

	for (int i = 0; i < StartG.size(); i++) {
	    buildingList.add(StartG.get(i)); //Startgebäude setzen
	}

    }
}
