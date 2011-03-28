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

/**
 * Setzt für jeden Spieler ein Startdorf
 * @author Johannes
 */
public class RandomMapBuilderVillagesFirst extends RandomMapBuilderJob {

    @Override
    public void performJob() {

	//Start-Gebäude am Maprand in gleichen Abständen platzieren!

	ArrayList<Building> StartG = new ArrayList<Building>();//Arraylist mit den endgültigen Startgebäuden
	int mapX = RandomMapBuilder.RandomRogMap.getMapSizeX();
	int mapY = RandomMapBuilder.RandomRogMap.getMapSizeY();
	int umfang = 2 * (mapX + mapY - 60);
	int spielerzahl = RandomMapBuilder.RandomRogMap.getPlayernumber();

	double randompos = Math.random() * umfang;

	for (int i = 1; i <= spielerzahl; i++) { //für jeden Spieler
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
	    Haus.setPlayerId(i);
	    Haus.getGraphicsData().defaultTexture = "img/buildings/human_main_e1.png";

	    int x = 0;
	    int y = 0;
	    final int rand = 30;

	    if (randompos + umfang * i / spielerzahl >= 2 * mapX + 2 * mapY - 8 * rand) {
		randompos -= 2 * mapX + 2 * mapY - 8 * rand;
	    }

	    if (randompos + umfang * i / spielerzahl < mapX - 2 * rand) {
		x = (int) randompos + umfang * i / spielerzahl + rand;
		y = rand;
	    } else if (randompos + umfang * i / spielerzahl < mapX + mapY - 4 * rand) {
		x = mapX - 2 * rand + 8;
		y = (int) randompos + umfang * i / spielerzahl - mapX + 3 * rand;// + 12;
	    } else if (randompos + umfang * i / spielerzahl < 2 * mapX + mapY - 6 * rand) {
		x = (int) (mapX - rand - 30 - (randompos + umfang * i / spielerzahl - mapX - mapY + 4 * rand));
		y = mapY - rand + 3;
	    } else if (randompos + umfang * i / spielerzahl < 2 * mapX + 2 * mapY - 8 * rand) {
		x = 8;
		y = (int) (mapY - rand - (randompos + umfang * i / spielerzahl - 2 * mapX - mapY + 6 * rand));
	    } else {
		System.out.println("Panik");
	    }

	    Haus.setMainPosition(new Position(x, y).valid() ? new Position(x, y) : new Position(x - 1, y));

	    //Kollision nichtmehr nötig.
            for (int z1c = 0; z1c < 12; z1c++) {
		for (int z2c = 0; z2c < 12; z2c++) {
		    RandomMapBuilder.RandomRogMap.getVisMap()[Haus.getMainPosition().getX() + z1c + z2c][Haus.getMainPosition().getY() - z1c + z2c].setGround_tex("img/ground/testground4.png");
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
