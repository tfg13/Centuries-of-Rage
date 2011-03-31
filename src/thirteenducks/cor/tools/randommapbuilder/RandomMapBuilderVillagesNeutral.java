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
package thirteenducks.cor.tools.randommapbuilder;

import java.util.ArrayList;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.NeutralBuilding;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.FloatingPointPosition;

/**
 * Platziert neutrale Dörfer
 *
 * @author Johannes
 */
public class RandomMapBuilderVillagesNeutral extends RandomMapBuilderJob {

    @Override
    public void performJob() {
	final int minX = 30;
	final int minY = 30;
	final int maxX = RandomMapBuilder.RandomRogMap.getMapSizeX() - 30;
	final int maxY = RandomMapBuilder.RandomRogMap.getMapSizeY() - 30;

	ArrayList<Position> wippos = new ArrayList<Position>(); // Die WIP-Positionen der neutralen Dörfer
	ArrayList<Building> buildingList = (ArrayList<Building>) RandomMapBuilder.RandomRogMap.getMapPoperty("BUILDING_LIST");

	int dorfzahl = (int) ((Math.random() * 1.5 + 2) * RandomMapBuilder.RandomRogMap.getPlayernumber()); //für jeden Spieler 1.5 bis 3 neutrale Dörfer

	//zufällige Dorfpositionen
	for (int i = 0; i < dorfzahl; i++) {
	    double RndPunktX = ((Math.random() * (maxX - minX)) + minX) / 2;
	    int RndPunktXi = (int) RndPunktX;
	    double RndPunktY = ((Math.random() * (maxY - minY)) + minY) / 2;
	    int RndPunktYi = (int) RndPunktY;
	    Position alpha = new Position(RndPunktXi * 2, RndPunktYi * 2);
	    wippos.add(alpha);
	}

	//neutrale Dörfer voneinander und von Startdörfen abstoßen
	for (int i = 0; i < 2000; i++) {
	    for (int j = 0; j < wippos.size(); j++) {
		//Vektoren von gerade überprüftem Dorf zum Dorf, das verschoben wird
		ArrayList<FloatingPointPosition> VektorenNeutral = new ArrayList<FloatingPointPosition>();
		ArrayList<FloatingPointPosition> VektorenStart = new ArrayList<FloatingPointPosition>();

		//Distanz zu anderen neutralen Dörfern
		for (int k = 0; k < wippos.size(); k++) {
		    if (j == k) {
			continue;
		    }
		    double dist = wippos.get(j).getDistance(wippos.get(k)); //Distanz zwischen den 2 Dörfern
		    VektorenNeutral.add(new FloatingPointPosition(((wippos.get(j).getX() - wippos.get(k).getX()) / Math.pow(dist, 5)), ((wippos.get(j).getY() - wippos.get(k).getY()) / Math.pow(dist, 5))));
		}

		// Distanz zu Startdörfern
		for (int k = 0; k < RandomMapBuilder.RandomRogMap.getPlayernumber(); k++) {
		    double dist = wippos.get(j).getDistance(buildingList.get(k).getMainPosition()); //Distanz zwischen den 2 Dörfern		    
		    VektorenStart.add(new FloatingPointPosition(((wippos.get(j).getX() - buildingList.get(k).getMainPosition().getX()) / Math.pow(dist, 5)), (wippos.get(j).getY() - buildingList.get(k).getMainPosition().getY()) / Math.pow(dist, 5)));
		}

		// Endvektor aus Vektoren zu allen Dörfern berechen
		double vecX = 0.0;
		double vecY = 0.0;

		for (int k = 0; k < VektorenNeutral.size(); k++) {
		    vecX += VektorenNeutral.get(k).getfX();
		    vecY += VektorenNeutral.get(k).getfY();
		}
		for (int k = 0; k < VektorenStart.size(); k++) {
		    vecX += VektorenStart.get(k).getfX();
		    vecY += VektorenStart.get(k).getfY();
		}

		Position newvec = new Position(0, 0);
		if (vecX == 0) {
		    if (vecY > 0) {
			newvec.setY(2); //unten
		    } else {
			newvec.setY(-2); //oben
		    }
		} else {
		    // Winkel berechnen:
		    double deg = (double) Math.atan(-(vecY) / (vecX)); // Gk durch Ak
		    // In 360Grad System umrechnen (falls negativ)
		    if (deg < 0) {
			deg += 2 * Math.PI;
		    }
		    // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
		    if (vecX < 0 && vecY < 0) { //links oben
			deg -= Math.PI;
		    } else if (vecX < 0 && vecY > 0) { //links unten
			deg += Math.PI;
		    }
		    if (deg == 0 || deg == -0) {
			if (vecX < 0) {
			    deg = Math.PI;
			}
		    }
		    if (deg < 0) {
			deg += 2 * Math.PI;
		    }

		    if (deg < Math.PI / 8) {
			// rechts
			newvec.setX(2);
		    } else if (deg < Math.PI * 3 / 8) {
			// rechts oben
			newvec.setX(1);
			newvec.setY(-1);
		    } else if (deg < Math.PI * 5 / 8) {
			// oben
			newvec.setY(-2);
		    } else if (deg < Math.PI * 7 / 8) {
			// oben links
			newvec.setX(-1);
			newvec.setY(-1);
		    } else if (deg < Math.PI * 9 / 8) {
			// links
			newvec.setX(-2);
		    } else if (deg < Math.PI * 11 / 8) {
			// links unten
			newvec.setX(-1);
			newvec.setY(1);
		    } else if (deg < Math.PI * 13 / 8) {
			// unten
			newvec.setY(2);
		    } else if (deg < Math.PI * 15 / 8) {
			// unten rechts
			newvec.setX(1);
			newvec.setY(1);
		    } else {
			// rechts
			newvec.setX(2);
		    }
		}

		wippos.get(j).setX(wippos.get(j).getX() + newvec.getX());
		wippos.get(j).setY(wippos.get(j).getY() + newvec.getY());

		// Rückgängig machen wenn ungültig
		if (wippos.get(j).getX() < minX || wippos.get(j).getX() > maxX || wippos.get(j).getY() < minY || wippos.get(j).getY() > maxY) {
		    wippos.get(j).setX(wippos.get(j).getX() - newvec.getX());
		    wippos.get(j).setY(wippos.get(j).getY() - newvec.getY());
		}
	    }
	}

	//neutrale Dörfer setzen
	for (int i = 0; i < wippos.size(); i++) {
	    int x = wippos.get(i).getX();
	    int y = wippos.get(i).getY();

	    NeutralBuilding Haus = new NeutralBuilding(RandomMapBuilder.RandomRogMap.getNewNetID(), new Position(x - 12, y).valid() ? new Position(x - 12, y) : new Position(x - 13, y));

	    buildingList.add(Haus);

            
	    for (int z1c = 0; z1c < 12; z1c++) {
		for (int z2c = 0; z2c < 12; z2c++) {
		    RandomMapBuilder.RandomRogMap.getVisMap()[Haus.getMainPosition().getX() + z1c + z2c][Haus.getMainPosition().getY() - z1c + z2c].setGround_tex("img/ground/testground1.png");
		}
	    }
            
	}
    }
}
