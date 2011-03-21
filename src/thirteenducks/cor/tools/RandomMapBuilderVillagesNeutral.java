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
import thirteenducks.cor.game.PlayersBuilding;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.map.CoRMapElement.collision;

/**
 * Platziert neutrale Dörfer
 *
 * @author Johannes
 */
public class RandomMapBuilderVillagesNeutral extends RandomMapBuilderJob {

    @Override
    public void performJob() {
	final int rand = 40;
	final int minX = 40;
	final int minY = 40;
	final int maxX = RandomMapBuilder.RandomRogMap.getMapSizeX() - 40;
	final int maxY = RandomMapBuilder.RandomRogMap.getMapSizeY() - 40;

	ArrayList<Position> wippos = new ArrayList<Position>();
	ArrayList<Building> buildingList = (ArrayList<Building>) RandomMapBuilder.RandomRogMap.getMapPoperty("BUILDING_LIST");

	int dorfzahl = (int) ((Math.random() * 2.5 + 1.5) * RandomMapBuilder.RandomRogMap.getPlayernumber()); //für jeden Spieler 1.5 bis 3 neutrale Dörfer

	//zufällige Dorfpositionen
	for (int i = 0; i < dorfzahl; i++) {
	    double RndPunktX = ((Math.random() * (maxX - minX)) + minX) / 2;
	    int RndPunktXi = (int) RndPunktX;
	    double RndPunktY = ((Math.random() * (maxY - minY)) + minY)/ 2;
	    int RndPunktYi = (int) RndPunktY;
	    Position alpha = new Position(RndPunktXi * 2, RndPunktYi * 2);
	    wippos.add(alpha);
	}

	//neutrale Dörfer voneinander abstoßen
	for (int i = 0; i < 40; i++) {
	    for (int j = 0; j < wippos.size(); j++) {
		double mindist = 999999; // kleinste gefundene Distanz
		Position nextdorf = new Position(-1, -1); // nächstes Dorf
		for (int k = 0; k < wippos.size(); k++) {
		    //distanz j und k!=j
		    if (j == k) {
			continue;
		    }
		    double dist = wippos.get(j).getDistance(wippos.get(k)); //Distanz zwischen den 2 Dörfern
		    if (dist < mindist) {
			mindist = dist;
			nextdorf = wippos.get(k);
		    }
		}
		// Distanz zu Startdörfern
		for (int l = 0; l < RandomMapBuilder.RandomRogMap.getPlayernumber(); l++) {
		    double dist = wippos.get(j).getDistance(buildingList.get(l).getMainPosition()); //Distanz zwischen den 2 Dörfern
		    if (dist < mindist) {
			mindist = dist;
			nextdorf = buildingList.get(l).getMainPosition();
		    }
		}
		// entfernen von nächstem Dorf
		Position vector = new Position(wippos.get(j).getX() - nextdorf.getX(), wippos.get(j).getY() - nextdorf.getY());
		Position newvec = new Position(0, 0);

		if (vector.getX() > 0) {
		    newvec.setX(1);
		} else if (vector.getX() < 0) {
		    newvec.setX(-1);
		} else {
		    newvec.setX(0);
		}
		if (vector.getY() > 0) {
		    newvec.setY(1);
		} else if (vector.getY() < 0) {
		    newvec.setY(-1);
		} else {
		    newvec.setY(0);
		}

		if (newvec.getX() == 0 || newvec.getY() == 0) {
		    newvec.setX(newvec.getX() * 2);
		    newvec.setY(newvec.getY() * 2);
		}
		
		wippos.get(j).setX(wippos.get(j).getX() + newvec.getX());
		wippos.get(j).setY(wippos.get(j).getY() + newvec.getY());

		// überprüfen ob gültig
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

	    DescParamsBuilding param = new DescParamsBuilding();
	    param.setDescTypeId(1);
	    param.setDescName("Village Center");
	    param.setHitpoints(2000);
	    param.setMaxhitpoints(2000);

	    param.setZ1(12);
	    param.setZ2(12);

	    PlayersBuilding tmp = new PlayersBuilding(param);
	    PlayersBuilding Haus = new PlayersBuilding(RandomMapBuilder.RandomRogMap.getNewNetID(), tmp);
	    Haus.getGraphicsData().offsetY = 8;
	    Haus.setPlayerId(1);
	    Haus.getGraphicsData().defaultTexture = "img/buildings/human_main_e1.png";

	    Haus.setMainPosition(new Position(x, y).valid() ? new Position(x, y) : new Position(x - 1, y));

	    buildingList.add(Haus);

	    for (int z1c = 0; z1c < 12; z1c++) {
		for (int z2c = 0; z2c < 12; z2c++) {
		    RandomMapBuilder.RandomRogMap.visMap[Haus.getMainPosition().getX() + z1c + z2c][Haus.getMainPosition().getY() - z1c + z2c].setCollision(collision.blocked);
		}
	    }
	}
    }
}
