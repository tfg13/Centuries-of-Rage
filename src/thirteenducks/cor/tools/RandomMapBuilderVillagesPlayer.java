/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.tools;

import java.util.ArrayList;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.map.CoRMap;

/**
 * Setzt für jeden Spieler ein Startdorf
 * @author Johannes
 */
public class RandomMapBuilderVillagesPlayer extends RandomMapBuilderJob {

    @Override
    public void performJob(CoRMap RandomRogMap) {

	        ArrayList<Position> Frei = new ArrayList<Position>(); //Arraylist mit allen möglichen Startpositionen
        ArrayList<Building> StartG = new ArrayList<Building>();//Arraylist mit den endgültigen Startgebäuden

        for (int i = 1; i <= RandomRogMap.; i++) {	//für jeden Spieler:

            Frei.clear();
            for (int u = 2; u < RandomRogMap.getMapSizeX() - 12; u++) {
                for (int j = 7; j < RandomRogMap.getMapSizeY() - 7; j++) {
                    if (u % 2 != j % 2) {
                        continue;
                    }
                    boolean frei = true;
                    if (RandomRogMap.visMap[u + 5][j - 5].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 4][j - 4].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 6][j - 4].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 3][j - 3].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 5][j - 3].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 7][j - 3].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 2][j - 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 4][j - 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 6][j - 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 8][j - 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 1][j - 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 3][j - 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 5][j - 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 7][j - 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 9][j - 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u][j].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 2][j].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 4][j].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 6][j].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 8][j].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 10][j].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 1][j + 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 3][j + 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 5][j + 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 7][j + 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 9][j + 1].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 2][j + 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 4][j + 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 6][j + 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 8][j + 2].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 3][j + 3].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 5][j + 3].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 7][j + 3].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 4][j + 4].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 6][j + 4].getCollision().equals(collision.blocked)) {
                        frei = false;
                    } else if (RandomRogMap.visMap[u + 5][j + 5].getCollision().equals(collision.blocked)) {
                        frei = false;
                    }


                    if (frei) {
                        Frei.add(new Position(u, j)); //mögliche Startpositionen finden
                    }
                }
            }

            if (i == 1) {

                double[] dist = new double[Frei.size()]; // Distanz zum nächsten Hauptgebäude

                for (int z = 0; z < Frei.size(); z++) { //für jedes Feld Distanz zum Rand berechnen
                    dist[z] = Math.min(Math.min(Frei.get(z).getX(), Frei.get(z).getY()), Math.min(RandomRogMap.getMapSizeX() - Frei.get(z).getX(), RandomRogMap.getMapSizeY() - Frei.get(z).getY()));
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
                double[] work = new double[player];

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

                double xcvbn = 0.6 + (0.05 * i) - (0.05 * player);
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

            DescParamsBuilding param = new DescParamsBuilding();

               //Haus an diese Position setzen

            param.setDescTypeId(1);
            param.setDescName("Village Center");
            param.setHitpoints(2000);
            param.setMaxhitpoints(2000);

            param.setZ1(12);
            param.setZ2(12);


            PlayersBuilding tmp = new PlayersBuilding(param);
            PlayersBuilding Haus = new PlayersBuilding(getNewNetID(), tmp);
            Haus.getGraphicsData().offsetY = 8;
            Haus.setPlayerId(i);
            Haus.getGraphicsData().defaultTexture = "img/buildings/human_main_e1.png";
            Haus.setMainPosition(new Position(x, y).valid() ? new Position(x, y) : new Position(x + 1, y));
//            RandomRogMap.visMap[x + 5][y - 5].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 4][y - 4].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 6][y - 4].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 3][y - 3].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 5][y - 3].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 7][y - 3].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 2][y - 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 4][y - 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 6][y - 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 8][y - 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 1][y - 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 3][y - 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 5][y - 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 7][y - 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 9][y - 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x][y].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 2][y].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 4][y].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 6][y].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 8][y].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 10][y].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 1][y + 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 3][y + 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 5][y + 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 7][y + 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 9][y + 1].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 2][y + 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 4][y + 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 6][y + 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 8][y + 2].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 3][y + 3].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 5][y + 3].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 7][y + 3].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 4][y + 4].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 6][y + 4].setCollision(collision.blocked);
//            RandomRogMap.visMap[x + 5][y + 5].setCollision(collision.blocked);
//
//            RandomRogMap.visMap[x - 2][y].setCollision(collision.blocked);
//            Reserviert.add(new Position(x - 2, y));
////            RandomRogMap.visMap[x + 12][y].setCollision(collision.blocked);
//            Reserviert.add(new Position(x + 12, y));
//            for (int k = -1; k <= 5; k++) {
////                RandomRogMap.visMap[x + k][y + k + 2].setCollision(collision.blocked);
//                Reserviert.add(new Position(x + k, y + k + 2));
////                RandomRogMap.visMap[x + k][y - 2 - k].setCollision(collision.blocked);
//                Reserviert.add(new Position(x + k, y - 2 - k));
//            }
//            for (int k = 6; k <= 11; k++) {
////                RandomRogMap.visMap[x + k][y - k + 12].setCollision(collision.blocked);
//                Reserviert.add(new Position(x + k, y - k + 12));
////                RandomRogMap.visMap[x + k][k + y - 12].setCollision(collision.blocked);
//                Reserviert.add(new Position(x + k, k + y - 12));
//            }


            StartG.add(Haus); //Startgebäude in Arraylist eintragen
        }
        return StartG; //Arraylist zurückgeben
    }

    public ArrayList<Unit> sStartEinheiten(ArrayList<Building> StartG) {
        ArrayList<Unit> StartU = new ArrayList<Unit>(); //Arraylist mit den Starteinheiten

        DescParamsUnit workerP = new DescParamsUnit();
        workerP.setDescTypeId(401);
        Unit2x2 worker = new Unit2x2(workerP);

        DescParamsUnit kundschafterP = new DescParamsUnit();
        kundschafterP.setDescTypeId(402);
        Unit2x2 kundschafter = new Unit2x2(kundschafterP);

    //    for (int i = 0; i < StartG.size(); i++) {	//für jeden Spieler 4 Starteinheiten setzen

            Unit2x2 Einheit = new Unit2x2(getNewNetID(), worker);
            Position unitPos = new Position(StartG.get(0).getMainPosition().getX() + 8, StartG.get(0).getMainPosition().getY() + 12);
            if (!unitPos.valid()) {
                unitPos.setX(unitPos.getX() + 1);
            }
            Einheit.setMainPosition(unitPos);
            Einheit.setPlayerId(1);
            StartU.add(Einheit);
//            RandomRogMap.visMap[Einheit.getMainPosition().getX()][Einheit.getMainPosition().getY()].setCollision(collision.occupied);
/*
            Unit2x2 Einheit2 = new Unit2x2(getNewNetID(), worker);
            Einheit2.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 5, StartG.get(i).getMainPosition().getY() + 7));
            Einheit2.setPlayerId(i + 1);
            StartU.add(Einheit2);
//            RandomRogMap.visMap[Einheit2.getMainPosition().getX()][Einheit2.getMainPosition().getY()].setCollision(collision.occupied);

            Unit2x2 Einheit3 = new Unit2x2(getNewNetID(), worker);
            Einheit3.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 6, StartG.get(i).getMainPosition().getY() + 6));
            Einheit3.setPlayerId(i + 1);
            StartU.add(Einheit3);
//            RandomRogMap.visMap[Einheit3.getMainPosition().getX()][Einheit3.getMainPosition().getY()].setCollision(collision.occupied);

            Unit2x2 Einheit4 = new Unit2x2(getNewNetID(), worker);
            Einheit4.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 3, StartG.get(i).getMainPosition().getY() + 5));
            Einheit4.setPlayerId(i + 1);
            StartU.add(Einheit4);
//            RandomRogMap.visMap[Einheit4.getMainPosition().getX()][Einheit4.getMainPosition().getY()].setCollision(collision.occupied);

            Unit2x2 Einheit5 = new Unit2x2(getNewNetID(), kundschafter);
            Einheit5.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 8, StartG.get(i).getMainPosition().getY() + 4));
            Einheit5.setPlayerId(i + 1);
            StartU.add(Einheit5);
//            RandomRogMap.visMap[Einheit5.getMainPosition().getX()][Einheit5.getMainPosition().getY()].setCollision(collision.occupied); */
       // }

        return StartU;
    }
    

}
