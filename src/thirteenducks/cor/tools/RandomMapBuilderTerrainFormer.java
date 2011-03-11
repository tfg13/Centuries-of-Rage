/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.tools;

import java.util.ArrayList;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;

/**
 *
 * @author Johannes
 */
public class RandomMapBuilderTerrainFormer extends RandomMapBuilderJob {

    ArrayList<Building> buildingList;
    ArrayList<Unit> unitList;

    @Override
    public void performJob() {

	int newMapX = RandomMapBuilder.RandomRogMap.getMapSizeX();
	int newMapY = RandomMapBuilder.RandomRogMap.getMapSizeY();

	// Grenzen der Map mit Kollision und isborder ausstatten
	for (int x = 0; x < newMapX; x++) {
	    for (int y = 0; y < newMapY; y++) {
		if (x % 2 != y % 2) {
		    continue;
		}
		if (x == 0 || x == (newMapX - 1) || y == 0 || y == (newMapY - 1)) {
		    // Feld hat Kollision
		    RandomMapBuilder.RandomRogMap.changeElementProperty(x, y, "is_border", "true");
		    RandomMapBuilder.RandomRogMap.visMap[x][y].setCollision(collision.blocked);
		} else {
		    RandomMapBuilder.RandomRogMap.visMap[x][y].setCollision(collision.free);
		}
	    }
	}

	unitList = new ArrayList<Unit>();	// Leere UnitList einfügen
	RandomMapBuilder.RandomRogMap.setMapProperty("UNIT_LIST", unitList);
	buildingList = new ArrayList<Building>();	// Leere BuildingList einfügen
	RandomMapBuilder.RandomRogMap.setMapProperty("BUILDING_LIST", buildingList);

	zAlles("img/ground/testwater1.png");
	ArrayList<Position> Land = sLand();
	zRechteck(Land.get(0).getX(), Land.get(0).getY(), Land.get(1).getX(), Land.get(1).getY(), "img/ground/testgrounddesert1.png");

	//Kontinente
	int RndK = (int) (Math.random() * 3);
	Position LandMitte = new Position(((int) (Land.get(0).getX() + Land.get(1).getX()) / 4) * 2, ((int) (Land.get(0).getY() + Land.get(1).getY()) / 4) * 2);
	Position RandomMitte = new Position((int) (LandMitte.getX() + (Math.random() - 0.5) * RandomMapBuilder.RandomRogMap.getMapSizeX() / 2), (int) (LandMitte.getY() + (Math.random() - 0.5) * RandomMapBuilder.RandomRogMap.getMapSizeY() / 2));
	if (RandomMitte.getX() % 2 != RandomMitte.getY()) {
	    RandomMitte.setX(RandomMitte.getX() + 1);
	}

	if (RndK != 0) {
	    zKontinent(RandomMitte, RndK);
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

    public void zAlles(String tex) { // der ganzen Karte dieselbe Textur geben
	for (int x = 0; x < RandomMapBuilder.RandomRogMap.getMapSizeX(); x++) {
	    for (int y = 0; y < RandomMapBuilder.RandomRogMap.getMapSizeY(); y++) {
		if (x % 2 != y % 2) {
		    continue;
		}
		zFeld(x, y, tex);
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
			if (argh.getX() >= RandomMapBuilder.RandomRogMap.getMapSizeX() || argh.getY() >= RandomMapBuilder.RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
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
			if (argh.getX() >= RandomMapBuilder.RandomRogMap.getMapSizeX() || argh.getY() >= RandomMapBuilder.RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
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
			if (argh.getX() >= RandomMapBuilder.RandomRogMap.getMapSizeX() || argh.getY() >= RandomMapBuilder.RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
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
			if (argh.getX() >= RandomMapBuilder.RandomRogMap.getMapSizeX() || argh.getY() >= RandomMapBuilder.RandomRogMap.getMapSizeY() || argh.getX() < 0 || argh.getY() < 0) {
			    break;
			}
			zFeld(argh.getX(), argh.getY(), "img/ground/testwater1.png");
		    }
		}
	    }
	}

	return V;
    }

    private ArrayList<Position> sLand() {
	ArrayList<Position> Land = new ArrayList<Position>();
	int meer[] = new int[4];
	byte maxtwo = 0;

	for (int i = 0; i < 4; i++) {
	    double Random = Math.random();
	    if (Random < 0.2) {
		System.out.println("Meer");
		if (maxtwo < 2) {
		    maxtwo++;
		    if (i == 0 || i == 2) { // links, rechts
			meer[i] = (int) (RandomMapBuilder.RandomRogMap.getMapSizeX() / 6 * (Math.random() * 0.5 + 0.5));
		    } else { // oben, unten
			meer[i] = (int) (RandomMapBuilder.RandomRogMap.getMapSizeY() / 6 * (Math.random() * 0.5 + 0.5));
		    }
		}
	    } else {
		meer[i] = 0;
	    }
	}

	Land.add(new Position((((int) (meer[0]) / 2) * 2) * 2, (((int) meer[1]) / 2) * 2));
	Land.add(new Position((((int) RandomMapBuilder.RandomRogMap.getMapSizeX() - meer[2]) / 2) * 2, (((int) RandomMapBuilder.RandomRogMap.getMapSizeY() - meer[3]) / 2) * 2));

	return Land;
    }

    public void zFeld(int x, int y, String tex) { //ein einzelnes Feld zeichnen
	if (x % 2 == y % 2 && x >= 0 && x < RandomMapBuilder.RandomRogMap.getMapSizeX() && y >= 0 && y < RandomMapBuilder.RandomRogMap.getMapSizeY()) {
	    RandomMapBuilder.RandomRogMap.changeElementProperty(x, y, "ground_tex", tex); //[x][y].setTex(tex);
	    if (tex.equals("img/ground/testwater1.png")) {
		RandomMapBuilder.RandomRogMap.visMap[x][y].setCollision(collision.blocked);
	    } else {
		RandomMapBuilder.RandomRogMap.visMap[x][y].setCollision(collision.free);
	    }
	} else {
	    System.out.println("Invalid Field! " + x + "|" + y);
	}

    }
}
