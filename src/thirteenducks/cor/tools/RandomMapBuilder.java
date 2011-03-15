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
// Zufallsmapgenerator
// von 2ndCalc
// 90% Try&Error, 10% Copy&Paste
// wenns nich laeuft ist wer anders schuld
package thirteenducks.cor.tools;

import thirteenducks.cor.map.CoRMap;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.util.*;
import thirteenducks.cor.game.DescParamsBuilding;
import thirteenducks.cor.game.DescParamsUnit;
import thirteenducks.cor.game.PlayersBuilding;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit2x2;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.map.MapIO;

public class RandomMapBuilder {

    public static HashMap<Integer, Unit> descUnit;
    public static HashMap<Integer, Building> descBuilding;
    public static CoRMap RandomRogMap;
    public static ArrayList<RandomMapBuilderJob> RMBJob;

    public static void newMap(byte PlayerNumber, byte Layout, byte Size, byte Theme) {

	RMBJob = new ArrayList<RandomMapBuilderJob>();
	RMBJob.add(new RandomMapBuilderTerrainFormer());
	RMBJob.add(new RandomMapBuilderVillagesFirst());
	RMBJob.add(new RandomMapBuilderVillagesNeutral());

	long zstVorher = System.currentTimeMillis(); // Zeit stoppen

	int newMapX = 161 + Size * 20 + PlayerNumber * 20;
	int newMapY = 141 + Size * 20 + PlayerNumber * 20;

	descBuilding = new HashMap<Integer, Building>();
	CoRMapElement[][] newMapArray = new CoRMapElement[newMapX][newMapY];
	String newMapName = "Random Map";
	RandomRogMap = new CoRMap(newMapX, newMapY, newMapName, newMapArray);
	RandomRogMap.setPlayernumber(PlayerNumber);

	// Map-Array anlegen
	for (int x = 0; x < newMapX; x++) {
	    for (int y = 0; y < newMapY; y++) {
		if (x % 2 == y % 2) {
		    newMapArray[x][y] = new CoRMapElement();
		}
	    }
	}

	RMBJob.get(0).performJob(); //TerrainFormer setzt Startdörfer

	RMBJob.get(1).performJob(); //VillagesFirst setzt Startdörfer

	RMBJob.get(2).performJob(); //VillagesFirst setzt neutrale Dörfer

	RandomRogMap.setMapProperty("NEXTNETID", RandomRogMap.getNewNetID());
	saveMap(RandomRogMap);

	long zstNachher = System.currentTimeMillis();
	System.out.println("Map in " + (zstNachher - zstVorher) + " ms erstellt.");
    }

    public static void saveMap(CoRMap RandomRogMap) { // Speichert die Map ab
	MapIO.saveMap(RandomRogMap, RandomRogMap.mapName);
    }

    public void zKreis(int x, int y, double r, String tex) {
	for (int a = 0; a < x; a++) {
	    for (int b = 0; b < y; b++) {
		if (a % 2 != b % 2) {
		    continue;
		}
		double dx = (x / 2) - a;
		double dy = (y / 2) - b;
		if (Math.sqrt((dx * dx) + (dy * dy)) < r) {
		    zFeld(a, b, tex);
		}
	    }
	}
    }

    private void zDreieck(Position a, Position b, Position c) {
    }

    public void zGerade(Position alpha, Position beta) { //eine Gerade aus Wasserfeldern zeichnen
	int vX = beta.getX() - alpha.getX();
	int vY = beta.getY() - alpha.getY();
	if (Math.abs(vX) >= Math.abs(vY)) {
	    if (vX > 0) {
		for (int i = 0; i < vX; i++) {
		    Position argh = new Position(alpha.getX() + i, alpha.getY() + (i * vY / vX));
		    if (argh.getX() % 2 != argh.getY() % 2) {
			argh.setX(argh.getX() - 1);
		    }
		    zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		}
	    } else {
		for (int i = 0; i > vX; i--) {
		    Position argh = new Position(alpha.getX() + i, alpha.getY() + (i * vY / vX));
		    if (argh.getX() % 2 != argh.getY() % 2) {
			argh.setX(argh.getX() - 1);
		    }
		    zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		}
	    }
	} else {
	    if (vY > 0) {
		for (int i = 0; i < vY; i++) {
		    Position argh = new Position(alpha.getX() + (i * vX / vY), alpha.getY() + i);
		    if (argh.getX() % 2 != argh.getY() % 2) {
			argh.setY(argh.getY() - 1);
		    }
		    zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		}
	    } else {
		for (int i = 0; i > vY; i--) {
		    Position argh = new Position(alpha.getX() + (i * vX / vY), alpha.getY() + i);
		    if (argh.getX() % 2 != argh.getY() % 2) {
			argh.setY(argh.getY() - 1);
		    }
		    zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		}
	    }
	}
    }



    public void zFeld(int x, int y, String tex) { //ein einzelnes Feld zeichnen
	if (x % 2 == y % 2 && x >= 0 && x < RandomRogMap.getMapSizeX() && y >= 0 && y < RandomRogMap.getMapSizeY()) {
	    RandomRogMap.changeElementProperty(x, y, "ground_tex", tex); //[x][y].setTex(tex);
	    if (tex.equals("img/ground/testwater1.png")) {
		RandomRogMap.visMap[x][y].setCollision(collision.blocked);
	    } else {
		RandomRogMap.visMap[x][y].setCollision(collision.free);
	    }
	} else {
	    System.out.println("Invalid Field! " + x + "|" + y);
	}

    }

    public Position RandPunkt() {
	double RndPunktX = Math.random() * RandomRogMap.getMapSizeY() / 2;
	int RndPunktXi = (int) RndPunktX;
	double RndPunktY = Math.random() * RandomRogMap.getMapSizeX() / 2;
	int RndPunktYi = (int) RndPunktY;
	Position alpha = new Position(RndPunktXi * 2, RndPunktYi * 2);
	return alpha;
    }

    public void zWasserRand(int i) {
	for (int x = 0; x < RandomRogMap.getMapSizeX(); x++) {
	    for (int y = 0; y < RandomRogMap.getMapSizeY(); y++) {
		if (x % 2 == y % 2) {
		    if (x < i || x > (RandomRogMap.getMapSizeX() - (i + 1)) || y < i || y > (RandomRogMap.getMapSizeY() - (i + 1))) {
			zFeld(x, y, "img/ground/testwater1.png");
		    }
		}
	    }
	}
    }
}
