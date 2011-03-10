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

    public HashMap<Integer, Unit> descUnit;
    public HashMap<Integer, Building> descBuilding;
    CoRMap RandomRogMap;
    ArrayList<Building> buildingList;
    ArrayList<Unit> unitList;
    ArrayList<RandomMapBuilderJob> RMBJob;

    public void newMap(byte PlayerNumber, byte Layout, byte Size, byte Theme) {

	RMBJob = new ArrayList<RandomMapBuilderJob>();
	RMBJob.add(new RandomMapBuilderVillagesPlayer());


	long zstVorher = System.currentTimeMillis(); // Zeit stoppen

	int newMapX = 121 + Size * 20 + PlayerNumber * 20;
	int newMapY = 101 + Size * 20 + PlayerNumber * 20;

	descBuilding = new HashMap<Integer, Building>();
	CoRMapElement[][] newMapArray = new CoRMapElement[newMapX][newMapY];
	String newMapName = "Random Map";
	RandomRogMap = new CoRMap(newMapX, newMapY, newMapName, newMapArray);

	// Map-Array anlegen
	for (int x = 0; x < newMapX; x++) {
	    for (int y = 0; y < newMapY; y++) {
		if (x % 2 == y % 2) {
		    newMapArray[x][y] = new CoRMapElement();
		}
	    }
	}

	// Grenzen der Map mit Kollision und isborder ausstatten
	for (int x = 0; x < newMapX; x++) {
	    for (int y = 0; y < newMapY; y++) {
		if (x % 2 != y % 2) {
		    continue;
		}
		if (x == 0 || x == (newMapX - 1) || y == 0 || y == (newMapY - 1)) {
		    // Feld hat Kollision
		    RandomRogMap.changeElementProperty(x, y, "is_border", "true");
		    RandomRogMap.visMap[x][y].setCollision(collision.blocked);
		} else {
	    	    RandomRogMap.visMap[x][y].setCollision(collision.free);
		}
	    }
	}

	unitList = new ArrayList<Unit>();	// Leere UnitList einfügen
	RandomRogMap.setMapProperty("UNIT_LIST", unitList);
	buildingList = new ArrayList<Building>();	// Leere BuildingList einfügen
	RandomRogMap.setMapProperty("BUILDING_LIST", buildingList);

	zAlles("img/ground/testgrounddesert1.png");
	ArrayList<Position> Land = sLand();
	zRechteck(Land.get(0).getX(), Land.get(0).getY(), Land.get(1).getX(), Land.get(1).getY(), "img/ground/testgrounddesert1.png");

	RMBJob.get(0).performJob(RandomRogMap); //VillagesPlayer

	//Kontinente
	int RndK = (int) Math.random() * 3;
	Position LandMitte = new Position(((int) (Land.get(0).getX() + Land.get(1).getX()) / 4) * 2, ((int) (Land.get(0).getY() + Land.get(1).getY()) / 4) * 2);
	Position RandomMitte = new Position((int) (LandMitte.getX() + (Math.random() - 0.5) * RandomRogMap.getMapSizeX() / 2), (int) (LandMitte.getY() + (Math.random() - 0.5) * RandomRogMap.getMapSizeY() / 2));
	if (RandomMitte.getX() % 2 != RandomMitte.getY()) {
	    RandomMitte.setX(RandomMitte.getX() + 1);
	}
	RndK = 1;
	if (RndK != 0) {
	    zKontinent(RandomMitte, RndK);
	}

	RandomRogMap.setMapProperty("NEXTNETID", RandomRogMap.getNewNetID());
	saveMap(RandomRogMap);

	long zstNachher = System.currentTimeMillis();
	System.out.println("Map in " + (zstNachher - zstVorher) + " ms erstellt.");
    }


    private ArrayList<Position> sLand() {
	ArrayList<Position> Land = new ArrayList<Position>();
	int meer[] = new int[4];
	byte maxtwo = 0;

	for (int i = 0; i < 4; i++) {
	    double Random = Math.random();
	    if (Random < 0.2) {
		if (maxtwo < 2) {
		    maxtwo++;
		    if (i == 0 || i == 2) { // links, rechts
			meer[i] = (int) (RandomRogMap.getMapSizeX() / 6 * (Math.random() * 0.5 + 0.5));
		    } else { // oben, unten
			meer[i] = (int) (RandomRogMap.getMapSizeY() / 6 * (Math.random() * 0.5 + 0.5));
		    }
		}
	    } else {
		meer[i] = 0;
	    }
	}

	Land.add(new Position((((int) (meer[0]) / 2) * 2) * 2, (((int) meer[1]) / 2) * 2));
	Land.add(new Position((((int) RandomRogMap.getMapSizeX() - meer[2]) / 2) * 2, (((int) RandomRogMap.getMapSizeY() - meer[3]) / 2) * 2));

	return Land;
    }


    public void saveMap(CoRMap RandomRogMap) { // Speichert die Map ab
	MapIO.saveMap(RandomRogMap, RandomRogMap.mapName);
    }

    public void zAlles(String tex) { // der ganzen Karte dieselbe Textur geben
	for (int x = 0; x < RandomRogMap.getMapSizeX(); x++) {
	    for (int y = 0; y < RandomRogMap.getMapSizeY(); y++) {
		if (x % 2 != y % 2) {
		    continue;
		}
		zFeld(x, y, tex);
	    }
	}
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

    public void zRechteck(int x1, int y1, int x2, int y2, String tex) {
	for (int a = x1; a <= x2; a++) {
	    for (int b = y1; b <= y2; b++) {
		if (a % 2 != b % 2) {
		    continue;
		}
		zFeld(a, b, tex);
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

    private ArrayList<Position> zKontinent(Position alpha, int RndK) {
	ArrayList<Position> V = new ArrayList<Position>();

	System.out.println("RndK " + RndK);

	double random0 = Math.random();
	double randomV = random0 * 2 * Math.PI;

	for (int j = 0; j <= RndK; j++) {
	    if (j == 1) {
		if (RndK == 1) {
		    randomV = random0 * 2 * Math.PI + Math.PI;
		    System.out.println("randomV" + randomV);
		} else {
		    randomV = (random0 + (Math.random() - 0.5)) / 16 * Math.PI + 120;
		}
	    } else if (j == 2) {
		randomV = (random0 + (Math.random() - 0.5) / 8) * Math.PI / 2 + 240;
	    }

	    double betaX = Math.sin(randomV) + alpha.getX();
	    double betaY = Math.cos(randomV) + alpha.getY();

	    double vX = betaX - alpha.getX();
	    double vY = betaY - alpha.getY();

	    if (Math.abs(vX) >= Math.abs(vY)) {
		if (vX > 0) {
		    for (int i = 0; i < 1000; i++) {
			Position argh = new Position(alpha.getX() + i, (int) (alpha.getY() + (i * vY / vX)));
			if (argh.getX() % 2 != argh.getY() % 2) {
			    argh.setX(argh.getX() - 1);
			}
			if (argh.getX() >= RandomRogMap.getMapSizeX() || argh.getY() >= RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
			    break;
			}
			zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		    }
		} else {
		    for (int i = 0; i > -1000; i--) {
			Position argh = new Position(alpha.getX() + i, (int) (alpha.getY() + (i * vY / vX)));
			if (argh.getX() % 2 != argh.getY() % 2) {
			    argh.setX(argh.getX() - 1);
			}
			if (argh.getX() >= RandomRogMap.getMapSizeX() || argh.getY() >= RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
			    break;
			}
			zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		    }
		}
	    } else {
		if (vY > 0) {
		    for (int i = 0; i < 1000; i++) {
			Position argh = new Position((int) (alpha.getX() + (i * vX / vY)), alpha.getY() + i);
			if (argh.getX() % 2 != argh.getY() % 2) {
			    argh.setY(argh.getY() - 1);
			}
			if (argh.getX() >= RandomRogMap.getMapSizeX() || argh.getY() >= RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
			    break;
			}
			zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		    }
		} else {
		    for (int i = 0; i > -1000; i--) {
			Position argh = new Position((int) (alpha.getX() + (i * vX / vY)), alpha.getY() + i);
			if (argh.getX() % 2 != argh.getY() % 2) {
			    argh.setY(argh.getY() - 1);
			}
			if (argh.getX() >= RandomRogMap.getMapSizeX() || argh.getY() >= RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
			    break;
			}
			zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		    }
		}
	    }
	}

	return V;
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
