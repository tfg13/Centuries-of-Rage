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
package de._13ducks.cor.tools;

import de._13ducks.cor.map.CoRMap;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.map.AbstractMapElement.collision;
import java.util.*;
import de._13ducks.cor.game.DescParamsBuilding;
import de._13ducks.cor.game.DescParamsUnit;
import de._13ducks.cor.game.Position;
import de._13ducks.cor.game.Unit2x2;
import de._13ducks.cor.map.AbstractMapElement;
import de._13ducks.cor.map.MapIO;

/**
 * Der alte RandomMapBuilder
 *
 *   Zufallsmapgenerator
 *   von 2ndCalc
 *   90% Try&Error, 10% Copy&Paste
 *   wenns nich laeuft ist wer anders schuld
 *
 * @deprecated
 * @author 2ndCalc
 */
public class RandomMapBuilder_Old {
//
//    public HashMap<Integer, Unit> descUnit;
//    public HashMap<Integer, Building> descBuilding;
//    CoRMap RandomRogMap;
//    ArrayList<Building> buildingList;
//    ArrayList<Unit> unitList;
//    int nextNetID = 1;
//    ArrayList<Position> Reserviert = new ArrayList<Position>();
//    ArrayList<Position> Bruecke = new ArrayList<Position>();
//    RandomMapBuilderMapElement[][] workArray;
//    int sizeX;
//    int sizeY;
//    // Muss global sein, wird für die Oasen benötigt
//    ArrayList<Position> Wald;
//
//    /*
//     * Ich hab den MapBuilder mal deutlich beschleunigt, damit man coole neue Features hinzufügen kann:
//     *
//     * Der MapBuilder benutzt keine "richtigen" MapElements mehr, da diese beim
//     * Millionenfachen Auslesen und Ändern einfach zu lahm wären.
//     *
//     * Stattdessen wird ein MapBuilderMapElement verwendet, das nur mit Zahlen arbeitet.
//     * Zahlen lassen sich viel schneller verarbeiten - egal ob beim auslesen, vergleichen oder setzen
//     *
//     * Bei in prepareMap wird dann alles in eine richtige Map umgewandelt.
//     * Dann können auch Gebäude/Einheiten gesetzt werden etc...
//     *
//     * Teilweise ist es danach notwendig, die Texturen noch einmal zu verarbeiten. Daher werden die Texturen direkt vor dem Speichern nochmal kopiert.
//     *
//     */
//    // Wasserfelder sind negativ
//    private final int WATERTEX2 = -2;
//    private final int WATERTEX = -1;
//    private final int LANDTEX = 1;
//    private final int LANDTEX2 = 2;
//    private final int BRIDGETEX = 3;
//    private final int LANDTEX3 = 4;
//    private final int LANDTEX4 = 5;
//    private final int OASIS1 = 10;
//    private final int OASIS2 = 11;
//    private final int OASIS3 = 12;
//    // Hier stehen die Entsprechenden, echten Texturen
//    private String real_LandTex;
//    private String real_LandTex2;
//    private String real_WaterTex;
//    private String real_BridgeTex;
//    private String real_WaterTex2;
//    private String real_LandTex3;
//    private String real_LandTex4;
//    private String real_Oasis1;
//    private String real_Oasis2;
//    private String real_Oasis3;
//
//    public void newMap(byte PlayerNumber, byte Layout, byte Size, byte Theme) {
//        long zstVorher = System.currentTimeMillis(); // Zeit stoppen
//
//        int newMapX = 121 + Size * 20 + PlayerNumber * 20;
//        int newMapY = 101 + Size * 20 + PlayerNumber * 20;
//        sizeX = newMapX;
//        sizeY = newMapY;
//
//        workArray = new RandomMapBuilderMapElement[newMapX][newMapY];
//        for (int x = 0; x < newMapX; x++) {
//            for (int y = 0; y < newMapY; y++) {
//                if (x % 2 == y % 2) {
//                    workArray[x][y] = new RandomMapBuilderMapElement();
//                }
//            }
//        }
//
//        real_LandTex = "img/ground/testground4.png";
//        real_LandTex2 = "img/ground/testground3.png";
//        real_LandTex3 = "img/ground/testground5.png";
//        real_LandTex4 = "img/ground/testground6.png";
//        real_WaterTex = "img/ground/testwater1.png";
//        real_WaterTex2 = "img/ground/testwater2.png";
//        real_BridgeTex = "img/ground/testground3.png";
//        if (Theme == 0) {
//            double RndTheme = Math.random() * 3 + 1;
//            Theme = (byte) RndTheme;
//        }
//        if (Theme == 2) {
//            real_LandTex = "img/ground/testgroundsnow1.png";
//            real_LandTex2 = "img/ground/testgroundsnow2.png";
//        } else if (Theme == 3) {
//            real_LandTex = "img/ground/testgrounddesert1.png";
//            real_LandTex3 = "img/ground/testgrounddesert2.png";
//            real_LandTex4 = "img/ground/testgrounddesert3.png";
//            real_Oasis1 = "img/ground/desert_oasis1.png";
//            real_Oasis2 = "img/ground/desert_oasis2.png";
//            real_Oasis3 = "img/ground/desert_oasis3.png";
//        }
//
//        if (Layout == 0) {
//            double RndLayout = Math.random() * 4 + 1;
//            Layout = (byte) RndLayout;
//        }
//
//        if (Layout == 1) { //Insel
//            zAlles(WATERTEX);
//            for (int i = 0; i < (15 + (PlayerNumber + Size) * 1.7); i++) {
//                Position alpha = RandPunkt();
//                zFeld(alpha.getX(), alpha.getY(), LANDTEX);
//                zBreit(LANDTEX, true);
//            }
//            zWasserRand(5);
//            for (int i = 0; i < 10; i++) {
//                zStrand(true, WATERTEX);
//                zStrand(false, LANDTEX);
//            }
//            zWasserRand(2);
//            zClean(LANDTEX);
//            sInseln();
//            if (Theme != 2) { // Nicht für Winter ;)
//                zFarbStufen();
//            }
//
//        } else if (Layout == 2) { //See
//            zAlles(LANDTEX);
//            zKreis(sizeX, sizeY, Math.min(newMapX / 4, newMapY / 4), WATERTEX);
//            zKreis(sizeX, sizeY, Math.min(newMapX / 10, newMapY / 10), LANDTEX2);
//            for (int i = 0; i < 10; i++) {
//                zStrand(true, WATERTEX);
//                zStrand(false, LANDTEX);
//            }
//            zStrand(false, LANDTEX);
//            zBreit(WATERTEX, false);
//            zBreit(WATERTEX, false);
//
//        } else if (Layout == 3) { //Ebene
//            zAlles(LANDTEX);
//
//            //Flüsse:
//            Position alpha = RandPunkt();
//            alpha.setX(0);
//
//            Position beta = RandPunkt();
//            double RndCoD = Math.random() * 2;
//            byte RndCo = (byte) RndCoD;
//            if (RndCo == 0) {
//                beta.setX(sizeX);
//            } else {
//                beta.setY(sizeY - 1);
//                if (beta.getX() < sizeX * 0.4) {
//                    beta.setX(beta.getX() + ((sizeX - 1) / 2));
//                    if (sizeX / 2 % 2 != 0) {
//                        beta.setX(beta.getX() + 1);
//                    }
//                }
//            }
//
//            Position delta = RandPunkt();
//            delta.setY(0);
//
//            Position gamma = new Position((alpha.getX() + beta.getX()) / 2, (alpha.getY() + beta.getY()) / 2);
//            if (gamma.getX() % 2 != gamma.getY() % 2) {
//                gamma.setX(gamma.getX() + 1);
//            }
//
//            zGerade(alpha, gamma);
//            zGerade(alpha, beta);
//            zGerade(gamma, delta);
//            zStrand(WATERTEX, 1.0);
//            zStrand(WATERTEX, 1.0);
//            sInseln();
//
//        } else if (Layout == 4) { //Fluss
//            zAlles(LANDTEX);
//            zRechteck(newMapX / 2 - 10, 0, newMapX / 2 + 10, newMapY / 2 - 40, WATERTEX);
//            zRechteck(newMapX / 2 - 12, newMapY / 2 - 39, newMapX / 2 + 12, newMapY / 2 - 21, LANDTEX2);
//            zRechteck(newMapX / 2 - 10, newMapY / 2 - 20, newMapX / 2 + 10, newMapY / 2 - 10, WATERTEX);
//            zRechteck(newMapX / 2 - 12, newMapY / 2 - 11, newMapX / 2 + 12, newMapY / 2 + 9, LANDTEX2);
//            zRechteck(newMapX / 2 - 10, newMapY / 2 + 10, newMapX / 2 + 10, newMapY / 2 + 20, WATERTEX);
//            zRechteck(newMapX / 2 - 12, newMapY / 2 + 21, newMapX / 2 + 12, newMapY / 2 + 39, LANDTEX2);
//            zRechteck(newMapX / 2 - 10, newMapY / 2 + 40, newMapX / 2 + 10, newMapY - 2, WATERTEX);
//        }
//
//        zBreit(BRIDGETEX, false);
//        zBreit(BRIDGETEX, false);
//
//        // Jetzt ist die rechenintensive Bearbeitung des Bodens abgeschlossen, jetzt die echte Map erzeugen
//        // Ab jetzt dürfen Kollisionsänderungen nurnoch auf der echten Map ausgeführt werden
//        // Texturänderungen müssen auf dem workArray ausgeführt werden.
//
//        prepareMap(newMapX, newMapY);
//
//        ArrayList<Building> StartG = sStartpunkt(PlayerNumber); //Startgebäude suchen
//        for (int i = 0; i < StartG.size(); i++) {
//            buildingList.add(StartG.get(i)); //Startgebäude setzen
//        }
//
//        for (int v = 0; v < Reserviert.size(); v++) { //Reservierte Felder freigeben
//            // Nur ändern, wenn vorher kein land da war:
//            if (workArray[Reserviert.get(v).getX()][Reserviert.get(v).getY()].getTex() < 0) {
//                workArray[Reserviert.get(v).getX()][Reserviert.get(v).getY()].setTex(LANDTEX);
//            }
//            RandomRogMap.visMap[Reserviert.get(v).getX()][Reserviert.get(v).getY()].setCollision(collision.free);
//        }
//
//        for (int v = 0; v < Bruecke.size(); v++) { //Brückenfelder freigeben
//            RandomRogMap.visMap[Bruecke.get(v).getX()][Bruecke.get(v).getY()].setCollision(collision.free);
//        }
//
//        ArrayList<Unit> UnitL = sStartEinheiten(StartG);
//        for (int i = 0; i < UnitL.size(); i++) {
//            unitList.add(UnitL.get(i)); //Starteinheiten setzen
//        }
//
//        // Jetzt "Oasen" suchen und einfärben
//        //zOasen(Theme);
//
//        // Abschließend nocheinmal die Bodentexturen kopieren.
//        // Nachfolgende Änderungen an dem workArray werden nichtmehr berücksichtigt
//        copyTextures();
//
//        RandomRogMap.setMapProperty("NEXTNETID", nextNetID);
//        saveMap(RandomRogMap);
//
//        long zstNachher = System.currentTimeMillis();
//        System.out.println("Map in " + (zstNachher - zstVorher) + " ms erstellt.");
//    }
//
//    /**
//     * Sucht "Oasen" (Wälder) und färbt den Boden in deren Gegend je nach Theme ein:
//     * Wüste: Grüner (Oasen eben)
//     * Gemäßigt: Dunkler/Brauner (Waldboden)
//     * Schnee: Nix. Ist eh hässlich ;)
//     */
//    private void zOasen(int theme) {
//        /*
//         * Verfahren:
//         * Großes int-Array anlegen (für jedes Feld ein Datenpunkt) Startwert 0
//         * Jeder Baum erhöht in einem Bereich um sich herum die Werte der Felder (echte Kreise + abstandsverstärktes Zufallselement)
//         * Dann werden die Felder umgefärbt, wenn sie einen ausreichenden Wert erreichen
//         */
//
//        // Unnötig bei Temperate, nicht unterstützt für Schnee:
//        if (theme == 3) {
//            // Anlegen:
//            int[][] oasen = new int[sizeX][sizeY];
//            // Jetzt für jeden Baum den Bereich färben
//            for (Position baum : Wald) {
//                calcOasis(baum.getX(), baum.getY(), oasen);
//            }
//            // Jetzt Felder mit ausreichend Großen Werten umfärben
//            for (int x = 0; x < sizeX; x++) {
//                for (int y = 0; y < sizeY; y++) {
//                    if (x % 2 != y % 2) {
//                        continue;
//                    }
//                    int val = oasen[x][y];
//                    if (val > 50) {
//                        workArray[x][y].setTex(OASIS3);
//                    } else if (val > 35) {
//                        workArray[x][y].setTex(OASIS2);
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Erhöht die Umgebung der Korrdinaten x y im arr-Array
//     * @param x
//     * @param y
//     * @param arr
//     */
//    private void calcOasis(int x, int y, int[][] arr) {
//        /**
//         * Dieser Algorithmus scheint noch nen Fehler zu haben.
//         * Es werden Bereiche gefärbt, die mehr als 3 Felder von allem Weg liegen.
//         */
//        for (int a = x - 2; a < x + 2; a++) {
//            for (int b = y - 2; b < y + 2; b++) {
//                if (a % 2 != b % 2) {
//                    continue;
//                }
//                double dx = x - a;
//                double dy = y - b;
//                double dist = Math.sqrt((dx * dx) + (dy * dy));
//                if (dist <= 2) {
//                    try {
//                        arr[a][b] += 20 - (1.0 * dist * 8);
//                    } catch (IndexOutOfBoundsException ex) {
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Färbt weiter innen liegende Landbereiche ein, damit die Maps abwechslungsreicher aussehen.
//     * Darf erst ganz am Ende aufgerufen werden, da jetzt viele verschiedene Texturen verwendet werden, also funktionieren vielleicht manche Mechanismen nichtmehr
//     * Streut ein Zufallselement ein
//     */
//    private void zFarbStufen() {
//        for (int x = 0; x < sizeX; x++) {
//            for (int y = 0; y < sizeY; y++) {
//                // Nur echte Felder
//                if (x % 2 != y % 2) {
//                    continue;
//                }
//                boolean land = workArray[x][y].getTex() > 0;
//                // Brücken nicht
//                if (workArray[x][y].getTex() != BRIDGETEX) {
//                    int distance = distanceTo(x, y, land, 11);
//                    // Jetzt färben
//                    if (distance > 10) {
//                        workArray[x][y].setTex(land ? LANDTEX4 : WATERTEX2);
//                    } else if (distance > 5) {
//                        workArray[x][y].setTex(land ? LANDTEX3 : WATERTEX);
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Berechnet kleinsten Abstand zum nächsten Wasser/Landfeld.
//     * Vorsicht: Such nicht in alle Richtungen (wegen der Performance) Derzeit suche in 8 Richtungen
//     * Vorsicht Abstand ist in "Kreisen" (1 = 1 Feld diagonal)
//     * Ende der Map zählt nicht als Treffer.
//     * Sucht nur bis maxInterest. Laufzeitkritischer Wert.
//     * Wenn man z.B. nacher nur bis 8 vergleicht, muss man auch nur bis 8 suchen.
//     * Brücken zählen weder als Land, noch als Wasser.
//     */
//    private int distanceTo(int x, int y, boolean searchForWater, int maxInterest) {
//        int kreis = 1;
//        try {
//            while (true) {
//                // In 8 Richtungen suchen, sofort abbrechen, wenn wir auf Land/Wasser stoßen
//                // Fehler hoch zählen. Sobald 8 Fehler kommen, haben wir in allen Richtungen das Ende erreicht, dann spätestens Abbrechen
//                int errorCount = 0;
//                // N
//                try {
//                    int testN = workArray[x][y - 2 * kreis].getTex();
//                    if (testN != BRIDGETEX && ((testN < 0 && searchForWater) || (testN > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                // NO
//                try {
//                    int testNO = workArray[x + kreis][y - kreis].getTex();
//                    if (testNO != BRIDGETEX && ((testNO < 0 && searchForWater) || (testNO > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                // O
//                try {
//                    int testO = workArray[x + 2 * kreis][y].getTex();
//                    if (testO != BRIDGETEX && ((testO < 0 && searchForWater) || (testO > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                // SO
//                try {
//                    int testSO = workArray[x + kreis][y + kreis].getTex();
//                    if (testSO != BRIDGETEX && ((testSO < 0 && searchForWater) || (testSO > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                // S
//                try {
//                    int testS = workArray[x][y + 2 * kreis].getTex();
//                    if (testS != BRIDGETEX && ((testS < 0 && searchForWater) || (testS > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                // SW
//                try {
//                    int testSW = workArray[x - kreis][y + kreis].getTex();
//                    if (testSW != BRIDGETEX && ((testSW < 0 && searchForWater) || (testSW > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                // W
//                try {
//                    int testW = workArray[x - 2 * kreis][y].getTex();
//                    if (testW != BRIDGETEX && ((testW < 0 && searchForWater) || (testW > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                // NW
//                try {
//                    int testNW = workArray[x - kreis][y - kreis].getTex();
//                    if (testNW != BRIDGETEX && ((testNW < 0 && searchForWater) || (testNW > 0 && !searchForWater))) {
//                        return kreis;
//                    }
//                } catch (IndexOutOfBoundsException ex) {
//                    errorCount++;
//                }
//                kreis++;
//                // Abbrechenen mit Maximaldistanz
//                if (errorCount >= 8 || kreis > maxInterest) {
//                    return maxInterest;
//                }
//            }
//        } catch (IndexOutOfBoundsException ex) {
//            // Hier abbrechen
//            return kreis;
//        }
//    }
//
//    /**
//     * Erstellt die richtige Map
//     */
//    private void prepareMap(int newMapX, int newMapY) {
//        descBuilding = new HashMap<Integer, Building>();
//        AbstractMapElement[][] newMapArray = new AbstractMapElement[newMapX][newMapY];
//        String newMapName = "Random Map";
//        RandomRogMap = new CoRMap(newMapX, newMapY, newMapName, newMapArray);
//
//        // Map-Array anlegen
//        for (int x = 0; x < newMapX; x++) {
//            for (int y = 0; y < newMapY; y++) {
//                if (x % 2 == y % 2) {
//                    newMapArray[x][y] = new AbstractMapElement();
//                }
//            }
//        }
//        // Texturen drauf schreibe
//        copyTextures();
//
//        // Grenzen der Map mit Kollision und isborder ausstatten
//        for (int x = 0; x < newMapX; x++) {
//            for (int y = 0; y < newMapY; y++) {
//                if (x % 2 != y % 2) {
//                    continue;
//                }
//                if (x == 0 || x == (newMapX - 1) || y == 0 || y == (newMapY - 1)) {
//                    // Feld hat Kollision
//                    RandomRogMap.changeElementProperty(x, y, "is_border", "true");
//                    RandomRogMap.visMap[x][y].setCollision(collision.blocked);
//                } else {
//                    if (workArray[x][y].isBlocked()) {
//                        RandomRogMap.visMap[x][y].setCollision(collision.blocked);
//                    } else {
//                        RandomRogMap.visMap[x][y].setCollision(collision.free);
//                    }
//                }
//            }
//        }
//
//        unitList = new ArrayList<Unit>();	// Leere UnitList einfügen
//        RandomRogMap.setMapProperty("UNIT_LIST", unitList);
//        buildingList = new ArrayList<Building>();	// Leere BuildingList einfügen
//        RandomRogMap.setMapProperty("BUILDING_LIST", buildingList);
//
//    }
//
//    /**
//     * Kopiert die Texturen von dem BearbeitungsArray auf die echte Map
//     */
//    private void copyTextures() {
//        for (int x = 0; x < sizeX; x++) {
//            for (int y = 0; y < sizeY; y++) {
//                if (x % 2 == y % 2) {
//                    // Neue Textur einfügen
//                    switch (workArray[x][y].getTex()) {
//                        case LANDTEX:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex);
//                            break;
//                        case LANDTEX2:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex2);
//                            break;
//                        case WATERTEX:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_WaterTex);
//                            break;
//                        case WATERTEX2:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_WaterTex2);
//                            break;
//                        case BRIDGETEX:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_BridgeTex);
//                            break;
//                        case LANDTEX3:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex3);
//                            break;
//                        case LANDTEX4:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex4);
//                            break;
//                        case OASIS1:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_Oasis1);
//                            break;
//                        case OASIS2:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_Oasis2);
//                            break;
//                        case OASIS3:
//                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_Oasis3);
//                            break;
//
//                    }
//                }
//            }
//        }
//    }
//
//    public void saveMap(CoRMap RandomRogMap) { // Speichert die Map ab
//        MapIO.saveMap(RandomRogMap, RandomRogMap.mapName);
//    }
//
//    public void zAlles(int tex) { // der ganzen Karte dieselbe Textur geben
//        for (int x = 0; x < sizeX; x++) {
//            for (int y = 0; y < sizeY; y++) {
//                if (x % 2 != y % 2) {
//                    continue;
//                }
//                zFeld(x, y, tex);
//            }
//        }
//    }
//
//    public void zKreis(int x, int y, double r, int tex) {
//        for (int a = 0; a < x; a++) {
//            for (int b = 0; b < y; b++) {
//                if (a % 2 != b % 2) {
//                    continue;
//                }
//                double dx = (x / 2) - a;
//                double dy = (y / 2) - b;
//                if (Math.sqrt((dx * dx) + (dy * dy)) < r) {
//                    zFeld(a, b, tex);
//                }
//            }
//        }
//    }
//
//    public void zRechteck(int x1, int y1, int x2, int y2, int tex) {
//        for (int a = x1; a <= x2; a++) {
//            for (int b = y1; b <= y2; b++) {
//                if (a % 2 != b % 2) {
//                    continue;
//                }
//                zFeld(a, b, tex);
//            }
//        }
//    }
//
//    public void zStrand(int tex, Double percent) {
//        ArrayList<Position> change = new ArrayList<Position>();
//        for (int i = 0; i < sizeX; i++) {
//            for (int j = 0; j < sizeY; j++) {
//                if (i % 2 != j % 2) {
//                    continue;
//                }
//                for (int x = -1; x <= 1; x += 2) {	//Die 4 Nachbarfelder suchen
//                    for (int y = -1; y <= 1; y += 2) {
//                        if ((i + x) % 2 != (j + y) % 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
//                            continue;
//                        }
//                        if (workArray[i + x][j + y].getTex() == tex) {
//                            Position wzw = new Position(i, j);
//                            change.add(wzw);
//                        }
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < change.size(); i++) {
//            double Random = Math.random();
//            if (Random < percent) {
//                zFeld(change.get(i).getX(), change.get(i).getY(), tex);
//            }
//        }
//    }
//
//    public void sInseln() { // erkennt Inseln, um kleine zu löschen / Brücken zu bauen
//        ArrayList<Position> allef = new ArrayList<Position>();
//        ArrayList<Position> open = new ArrayList<Position>();
//        ArrayList<Position> closed = new ArrayList<Position>();
//        ArrayList<ArrayList<Position>> hae = new ArrayList<ArrayList<Position>>();
//
//        for (int i = 0; i < sizeX; i++) { // Alle freien Felder in AL packen
//            for (int j = 0; j < sizeY; j++) {
//                if (i % 2 != j % 2) {
//                    continue;
//                }
//                if (!workArray[i][j].isBlocked()) {
//                    allef.add(new Position(i, j));
//                }
//            }
//        }
//
//        while (!allef.isEmpty()) { // Wenn es Landfelder gibt, die noch nicht zu einer Insel gehören
//            open.add(allef.get(0)); // erstbestes Landfeld nehmen
//            allef.remove(0);
//            while (!open.isEmpty()) { // freies Feld aussuchen und alle angrenzenden finden
//                closed.add(open.get(0));
//                int x = open.get(0).getX();
//                int y = open.get(0).getY();
//                open.remove(0);
//
//                ArrayList<Position> verschieben = new ArrayList<Position>(); // 4 Nachbarfelder
//                for (int i = 0; i < allef.size(); i++) {
//                    if (allef.get(i).getX() == x + 1 && allef.get(i).getY() == y + 1) {
//                        verschieben.add(allef.get(i));
//                    } else if (allef.get(i).getX() == x - 1 && allef.get(i).getY() == y + 1) {
//                        verschieben.add(allef.get(i));
//                    } else if (allef.get(i).getX() == x + 1 && allef.get(i).getY() == y - 1) {
//                        verschieben.add(allef.get(i));
//                    } else if (allef.get(i).getX() == x - 1 && allef.get(i).getY() == y - 1) {
//                        verschieben.add(allef.get(i));
//                    }
//                }
//
//                for (int i = 0; i < verschieben.size(); i++) {
//                    open.add(verschieben.get(i));
//                    allef.remove(verschieben.get(i));
//                }
//                verschieben.clear();
//            } // Insel fertig
//
//            ArrayList<Position> lassdas = new ArrayList<Position>();
//            for (int v = 0; v < closed.size(); v++) {
//                lassdas.add(closed.get(v));
//            }
//            hae.add(lassdas); // Insel in hae tun
//            open.clear();
//            closed.clear();
//        }
//
//        System.out.print("Inseln: " + hae.size() + " -> "); //zu kleine Inseln entfernen
//        for (int i = 0; i < hae.size(); i++) {
//            if (hae.get(i).size() < 200) {
//                for (int j = 0; j < hae.get(i).size(); j++) {
//                    zFeld(hae.get(i).get(j).getX(), hae.get(i).get(j).getY(), WATERTEX);
//                }
//                hae.remove(i);
//                i--;
//            }
//        }
//        System.out.println(hae.size());
//
//        int n = hae.get(0).size(); // größte Insel
//        int m = 0;
//        for (int l = 1; l < hae.size(); l++) {
//            if (hae.get(l).size() > n) {
//                n = hae.get(l).size();
//                m = l;
//            }
//        }
//
//        for (int i = 0; i < hae.size(); i++) {
//            if (i == m) {
//                continue;
//            }
//
//            int ax = 0;
//            int ay = 0;
//            for (int j = 0; j < hae.get(i).size(); j++) {
//                ax += hae.get(i).get(j).getX();
//                ay += hae.get(i).get(j).getY();
//            }
//            ax /= hae.get(i).size(); // durchschnitt
//            ay /= hae.get(i).size();
//
//            int nah = 0;
//            double dist = 99999;
//            for (int z = 0; z < hae.get(m).size(); z++) {
//                double rechnen = Math.sqrt(Math.abs(hae.get(m).get(z).getX() - ax) + Math.abs(hae.get(m).get(z).getY() - ay));
//                if (rechnen < dist) {
//                    dist = rechnen;
//                    nah = z;
//                }
//            }
//
//            int nah2 = 0;
//            double dist2 = 99999;
//            for (int z = 0; z < hae.get(i).size(); z++) {
//                double rechnen = Math.sqrt(Math.abs(hae.get(i).get(z).getX() - hae.get(m).get(nah).getX()) + Math.abs(hae.get(i).get(z).getY() - hae.get(m).get(nah).getY()));
//                if (rechnen < dist2) {
//                    dist2 = rechnen;
//                    nah2 = z;
//                }
//            }
//
//            zBruecke(new Position(hae.get(i).get(nah2).getX(), hae.get(i).get(nah2).getY()), new Position(hae.get(m).get(nah).getX(), hae.get(m).get(nah).getY()));
//        }
//    }
//
//    public void zStrand(boolean col, int tex) {
//        ArrayList<Position> change = new ArrayList<Position>();
//        for (int i = 0; i < sizeX; i++) {
//            for (int j = 0; j < sizeY; j++) {
//                if (i % 2 != j % 2) {
//                    continue;
//                }
//                for (int x = -1; x <= 1; x += 2) {	//Die 4 Nachbarfelder suchen
//                    for (int y = -1; y <= 1; y += 2) {
//                        if ((i + x) % 2 != (j + y) % 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
//                            continue;
//                        }
//                        if (workArray[i + x][j + y].isBlocked() == col) {
//                            Position wzw = new Position(i, j);
//                            change.add(wzw);
//                        }
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < change.size(); i++) {
//            double Random = Math.random();
//            if (Random < 0.4) {
//                zFeld(change.get(i).getX(), change.get(i).getY(), tex);
//            }
//        }
//    }
//
//    public void zGerade(Position alpha, Position beta) { //eine Gerade aus Wasserfeldern zeichnen
//        int vX = beta.getX() - alpha.getX();
//        int vY = beta.getY() - alpha.getY();
//        if (Math.abs(vX) >= Math.abs(vY)) {
//            if (vX > 0) {
//                for (int i = 0; i < vX; i++) {
//                    Position argh = new Position(alpha.getX() + i, alpha.getY() + (i * vY / vX));
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setX(argh.getX() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), WATERTEX);
//                }
//            } else {
//                for (int i = 0; i > vX; i--) {
//                    Position argh = new Position(alpha.getX() + i, alpha.getY() + (i * vY / vX));
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setX(argh.getX() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), WATERTEX);
//                }
//            }
//        } else {
//            if (vY > 0) {
//                for (int i = 0; i < vY; i++) {
//                    Position argh = new Position(alpha.getX() + (i * vX / vY), alpha.getY() + i);
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setY(argh.getY() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), WATERTEX);
//                }
//            } else {
//                for (int i = 0; i > vY; i--) {
//                    Position argh = new Position(alpha.getX() + (i * vX / vY), alpha.getY() + i);
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setY(argh.getY() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), WATERTEX);
//                }
//            }
//        }
//    }
//
//    public void zBruecke(Position alpha, Position beta) { //Bruecke zeichnen
//        int vX = beta.getX() - alpha.getX();
//        int vY = beta.getY() - alpha.getY();
//        if (Math.abs(vX) >= Math.abs(vY)) {
//            if (vX > 0) {
//                for (int i = 0; i < vX; i++) {
//                    Position argh = new Position(alpha.getX() + i, alpha.getY() + (i * vY / vX));
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setX(argh.getX() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), BRIDGETEX);
//                    workArray[argh.getX()][argh.getY()].setBlocked(true);
//                    Bruecke.add(argh);
//                }
//            } else {
//                for (int i = 0; i > vX; i--) {
//                    Position argh = new Position(alpha.getX() + i, alpha.getY() + (i * vY / vX));
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setX(argh.getX() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), BRIDGETEX);
//                    workArray[argh.getX()][argh.getY()].setBlocked(true);
//                    Bruecke.add(argh);
//                }
//            }
//        } else {
//            if (vY > 0) {
//                for (int i = 0; i < vY; i++) {
//                    Position argh = new Position(alpha.getX() + (i * vX / vY), alpha.getY() + i);
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setY(argh.getY() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), BRIDGETEX);
//                    workArray[argh.getX()][argh.getY()].setBlocked(true);
//                    Bruecke.add(argh);
//                }
//            } else {
//                for (int i = 0; i > vY; i--) {
//                    Position argh = new Position(alpha.getX() + (i * vX / vY), alpha.getY() + i);
//                    if (argh.getX() % 2 != argh.getY() % 2) {
//                        argh.setY(argh.getY() - 1);
//                    }
//                    zFeld(argh.getX(), argh.getY(), BRIDGETEX);
//                    workArray[argh.getX()][argh.getY()].setBlocked(true);
//                    Bruecke.add(argh);
//                }
//            }
//        }
//    }
//
//    public void zBreit(int tex, Boolean Test) { //Wasser verbreitern
//        if (Test == true) {
//            double Rnd = Math.random();
//            if (Rnd < 0.5) {
//                Test = false;
//            }
//        }
//
//        ArrayList<Position> Wasser = new ArrayList<Position>();
//        if (Test == false) {
//            for (int i = 0; i < sizeX; i++) {
//                for (int j = 0; j < sizeY; j++) {
//                    if (i % 2 != j % 2 || workArray[i][j].getTex() == tex) {
//                        continue;
//                    }
//                    for (int x = -1; x <= 1; x += 2) {	//Die 4 Nachbarfelder suchen
//                        for (int y = -1; y <= 1; y += 2) {
//                            if ((i + x) % 2 != (j + y) % 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
//                                continue;
//                            }
//                            if (workArray[i + x][j + y].getTex() == tex) {
//                                Position wzw = new Position(i, j);
//                                Wasser.add(wzw);
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            for (int i = 0; i < sizeX; i++) {
//                for (int j = 0; j < sizeY; j++) {
//                    if (i % 2 != j % 2 || workArray[i][j].getTex() == tex) {
//
//                        continue;
//                    }
//                    for (int x = -2; x <= 2; x++) {	//Die 8 Nachbarfelder suchen
//                        for (int y = -2; y <= 2; y++) {
//                            if (Math.abs(x) + Math.abs(y) != 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
//                                continue;
//                            }
//                            if (workArray[i + x][j + y].getTex() == tex) {
//                                Position wzw = new Position(i, j);
//                                Wasser.add(wzw);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        for (int i = 0; i < Wasser.size(); i++) {
//            zFeld(Wasser.get(i).getX(), Wasser.get(i).getY(), tex);
//        }
//    }
//
//    public void zFeld(int x, int y, int tex) { //ein einzelnes Feld zeichnen
//        if (x % 2 == y % 2 && x >= 0 && x <= sizeX && y >= 0 && y < sizeY) {
//            workArray[x][y].setTex(tex);
//            if (tex == WATERTEX) {
//                workArray[x][y].setBlocked(true);
//            } else {
//                workArray[x][y].setBlocked(false);
//            }
//        } else {
//            System.out.println("Invalid Field!");
//        }
//
//    }
//
//    public Position RandPunkt() {
//        double RndPunktX = Math.random() * sizeY / 2;
//        int RndPunktXi = (int) RndPunktX;
//        double RndPunktY = Math.random() * sizeX / 2;
//        int RndPunktYi = (int) RndPunktY;
//        Position alpha = new Position(RndPunktXi * 2, RndPunktYi * 2);
//        return alpha;
//    }
//
//    public void zWasserRand(int i) {
//        for (int x = 0; x < sizeX; x++) {
//            for (int y = 0; y < sizeY; y++) {
//                if (x % 2 == y % 2) {
//                    if (x < i || x > (sizeX - (i + 1)) || y < i || y > (sizeY - (i + 1))) {
//                        zFeld(x, y, WATERTEX);
//                    }
//                }
//            }
//        }
//    }
//
//    public void zClean(int tex) {
//        for (int u = 1; u < sizeX - 1; u++) {
//            for (int j = 1; j < sizeY - 1; j++) {
//                if (u % 2 == j % 2 && workArray[u][j].getTex() == WATERTEX) {
//                    boolean vier = true;
//                    for (int i = -1; i <= 1; i += 2) {
//                        for (int o = -1; o <= 1; o += 2) {
//                            if (u + i < 0 || j + o < 0 || u + i >= sizeX || j + o >= sizeY) {
//                                continue;
//                            }
//                            if (workArray[u + i][j + o].getTex() == WATERTEX) {
//                                vier = false;
//                            }
//                        }
//                    }
//                    if (vier) {
//                        zFeld(u, j, tex);
//                    }
//                }
//            }
//        }
//    }
//
//    //setzt für jeden Spieler ein Startgebäude
//    public ArrayList<Building> sStartpunkt(int player) {
//        ArrayList<Position> Frei = new ArrayList<Position>(); //Arraylist mit allen möglichen Startpositionen
//        ArrayList<Building> StartG = new ArrayList<Building>();//Arraylist mit den endgültigen Startgebäuden
//
//        for (int i = 1; i <= player; i++) {	//für jeden Spieler:
//
//            Frei.clear();
//            for (int u = 2; u < RandomRogMap.getMapSizeX() - 12; u++) {
//                for (int j = 7; j < RandomRogMap.getMapSizeY() - 7; j++) {
//                    if (u % 2 != j % 2) {
//                        continue;
//                    }
//                    boolean frei = true;
//                    if (RandomRogMap.visMap[u + 5][j - 5].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 4][j - 4].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 6][j - 4].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 3][j - 3].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 5][j - 3].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 7][j - 3].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 2][j - 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 4][j - 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 6][j - 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 8][j - 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 1][j - 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 3][j - 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 5][j - 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 7][j - 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 9][j - 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u][j].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 2][j].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 4][j].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 6][j].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 8][j].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 10][j].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 1][j + 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 3][j + 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 5][j + 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 7][j + 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 9][j + 1].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 2][j + 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 4][j + 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 6][j + 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 8][j + 2].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 3][j + 3].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 5][j + 3].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 7][j + 3].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 4][j + 4].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 6][j + 4].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    } else if (RandomRogMap.visMap[u + 5][j + 5].getCollision().equals(collision.blocked)) {
//                        frei = false;
//                    }
//
//
//                    if (frei) {
//                        Frei.add(new Position(u, j)); //mögliche Startpositionen finden
//                    }
//                }
//            }
//
//            if (i == 1) {
//
//                double[] dist = new double[Frei.size()]; // Distanz zum nächsten Hauptgebäude
//
//                for (int z = 0; z < Frei.size(); z++) { //für jedes Feld Distanz zum Rand berechnen
//                    dist[z] = Math.min(Math.min(Frei.get(z).getX(), Frei.get(z).getY()), Math.min(RandomRogMap.getMapSizeX() - Frei.get(z).getX(), RandomRogMap.getMapSizeY() - Frei.get(z).getY()));
//                }
//
//                double mittel = 0;
//                for (int q = 0; q < dist.length; q++) {
//                    mittel += dist[q];
//                }
//                mittel /= dist.length;
//
//                double mind = 9999;
//                for (int q = 0; q < dist.length; q++) {
//                    if (mind > dist[q]) {
//                        mind = dist[q];
//                    }
//                }
//
//                double xcvbn = 0.4;
//                double low = (1 - xcvbn) * mittel + xcvbn * mind;
//
//                for (int q = 0; q < Frei.size(); q++) {
//                    if (dist[q] > low) {
//                        Frei.get(q).setX(Frei.get(q).getX() - 1);
//                    }
//                }
//                int w = 0;
//                while (w < Frei.size()) {
//                    if (Frei.get(w).getX() == -1) {
//                        Frei.remove(w); //Felder mit zu geringer Distanz löschen
//                    } else {
//                        w++;
//                    }
//                }
//
//            } else {
//
//                double[] dist = new double[Frei.size()]; // Distanz zum nächsten Hauptgebäude
//                double[] work = new double[player];
//
//                for (int z = 0; z < Frei.size(); z++) { //für jedes Feld Distanz zu allen Hauptgebäuden berechnen
//                    for (int d = 0; d < i - 1; d++) {
//                        work[d] = Math.sqrt(Math.pow(Frei.get(z).getX() - StartG.get(d).getMainPosition().getX() + 1, 2) + Math.pow(Frei.get(z).getY() - StartG.get(d).getMainPosition().getY(), 2));
//                    }
//                    dist[z] = 9999;
//                    for (int e = 0; e < i - 1; e++) {
//                        if (work[e] < dist[z]) {
//                            dist[z] = work[e];
//                        } // kleinste Distanz herausfinden
//                    }
//                }
//
//                double mittel = 0;
//                for (int q = 0; q < dist.length; q++) {
//                    mittel += dist[q];
//                }
//                mittel /= dist.length;
//
//                double maxd = 0;
//                for (int q = 0; q < dist.length; q++) {
//                    if (maxd < dist[q]) {
//                        maxd = dist[q];
//                    }
//                }
//
//                double xcvbn = 0.6 + (0.05 * i) - (0.05 * player);
//                double high = (1 - xcvbn) * mittel + xcvbn * maxd;
//
//                for (int q = 0; q < Frei.size(); q++) {
//                    if (dist[q] < high) {
//                        Frei.get(q).setX(Frei.get(q).getX() - 1);
//                    }
//                }
//                int w = 0;
//                while (w < Frei.size()) {
//                    if (Frei.get(w).getX() == -1) {
//                        Frei.remove(w); //Felder mit zu geringer Distanz löschen
//                    } else {
//                        w++;
//                    }
//                }
//            }
//
//            double RndStartD = Math.random() * Frei.size(); //zufällige Startposition aus der Frei-Arraylist
//            int RndStart = (int) RndStartD;
//            int x = Frei.get(RndStart).getX();
//            int y = Frei.get(RndStart).getY();
//
//            DescParamsBuilding param = new DescParamsBuilding();
//
//            //Haus an diese Position setzen
//
//            param.setDescTypeId(1);
//            param.setDescName("Village Center");
//            param.setHitpoints(2000);
//            param.setMaxhitpoints(2000);
//
//            param.setZ1(12);
//            param.setZ2(12);
//
//
//            PlayersBuilding tmp = new PlayersBuilding(param);
//            PlayersBuilding Haus = new PlayersBuilding(getNewNetID(), tmp);
//            Haus.getGraphicsData().offsetY = 8;
//            Haus.setPlayerId(i);
//            Haus.getGraphicsData().defaultTexture = "img/buildings/human_main_e1.png";
//            Haus.setMainPosition(new Position(x, y).valid() ? new Position(x, y) : new Position(x + 1, y));
////            RandomRogMap.visMap[x + 5][y - 5].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 4][y - 4].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 6][y - 4].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 3][y - 3].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 5][y - 3].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 7][y - 3].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 2][y - 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 4][y - 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 6][y - 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 8][y - 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 1][y - 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 3][y - 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 5][y - 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 7][y - 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 9][y - 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x][y].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 2][y].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 4][y].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 6][y].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 8][y].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 10][y].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 1][y + 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 3][y + 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 5][y + 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 7][y + 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 9][y + 1].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 2][y + 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 4][y + 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 6][y + 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 8][y + 2].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 3][y + 3].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 5][y + 3].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 7][y + 3].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 4][y + 4].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 6][y + 4].setCollision(collision.blocked);
////            RandomRogMap.visMap[x + 5][y + 5].setCollision(collision.blocked);
////
////            RandomRogMap.visMap[x - 2][y].setCollision(collision.blocked);
////            Reserviert.add(new Position(x - 2, y));
//////            RandomRogMap.visMap[x + 12][y].setCollision(collision.blocked);
////            Reserviert.add(new Position(x + 12, y));
////            for (int k = -1; k <= 5; k++) {
//////                RandomRogMap.visMap[x + k][y + k + 2].setCollision(collision.blocked);
////                Reserviert.add(new Position(x + k, y + k + 2));
//////                RandomRogMap.visMap[x + k][y - 2 - k].setCollision(collision.blocked);
////                Reserviert.add(new Position(x + k, y - 2 - k));
////            }
////            for (int k = 6; k <= 11; k++) {
//////                RandomRogMap.visMap[x + k][y - k + 12].setCollision(collision.blocked);
////                Reserviert.add(new Position(x + k, y - k + 12));
//////                RandomRogMap.visMap[x + k][k + y - 12].setCollision(collision.blocked);
////                Reserviert.add(new Position(x + k, k + y - 12));
////            }
//
//
//            StartG.add(Haus); //Startgebäude in Arraylist eintragen
//        }
//        return StartG; //Arraylist zurückgeben
//    }
//
//    public ArrayList<Unit> sStartEinheiten(ArrayList<Building> StartG) {
//        ArrayList<Unit> StartU = new ArrayList<Unit>(); //Arraylist mit den Starteinheiten
//
//        DescParamsUnit workerP = new DescParamsUnit();
//        workerP.setDescTypeId(401);
//        Unit2x2 worker = new Unit2x2(workerP);
//
//        DescParamsUnit kundschafterP = new DescParamsUnit();
//        kundschafterP.setDescTypeId(402);
//        Unit2x2 kundschafter = new Unit2x2(kundschafterP);
//
//        //    for (int i = 0; i < StartG.size(); i++) {	//für jeden Spieler 4 Starteinheiten setzen
//
//        Unit2x2 Einheit = new Unit2x2(getNewNetID(), worker);
//        Position unitPos = new Position(StartG.get(0).getMainPosition().getX() + 8, StartG.get(0).getMainPosition().getY() + 12);
//        if (!unitPos.valid()) {
//            unitPos.setX(unitPos.getX() + 1);
//        }
//        Einheit.setMainPosition(unitPos);
//        Einheit.setPlayerId(1);
//        StartU.add(Einheit);
////            RandomRogMap.visMap[Einheit.getMainPosition().getX()][Einheit.getMainPosition().getY()].setCollision(collision.occupied);
///*
//        Unit2x2 Einheit2 = new Unit2x2(getNewNetID(), worker);
//        Einheit2.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 5, StartG.get(i).getMainPosition().getY() + 7));
//        Einheit2.setPlayerId(i + 1);
//        StartU.add(Einheit2);
//        //            RandomRogMap.visMap[Einheit2.getMainPosition().getX()][Einheit2.getMainPosition().getY()].setCollision(collision.occupied);
//
//        Unit2x2 Einheit3 = new Unit2x2(getNewNetID(), worker);
//        Einheit3.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 6, StartG.get(i).getMainPosition().getY() + 6));
//        Einheit3.setPlayerId(i + 1);
//        StartU.add(Einheit3);
//        //            RandomRogMap.visMap[Einheit3.getMainPosition().getX()][Einheit3.getMainPosition().getY()].setCollision(collision.occupied);
//
//        Unit2x2 Einheit4 = new Unit2x2(getNewNetID(), worker);
//        Einheit4.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 3, StartG.get(i).getMainPosition().getY() + 5));
//        Einheit4.setPlayerId(i + 1);
//        StartU.add(Einheit4);
//        //            RandomRogMap.visMap[Einheit4.getMainPosition().getX()][Einheit4.getMainPosition().getY()].setCollision(collision.occupied);
//
//        Unit2x2 Einheit5 = new Unit2x2(getNewNetID(), kundschafter);
//        Einheit5.setMainPosition(new Position(StartG.get(i).getMainPosition().getX() + 8, StartG.get(i).getMainPosition().getY() + 4));
//        Einheit5.setPlayerId(i + 1);
//        StartU.add(Einheit5);
//        //            RandomRogMap.visMap[Einheit5.getMainPosition().getX()][Einheit5.getMainPosition().getY()].setCollision(collision.occupied); */
//        // }
//
//        return StartU;
//    }
//
//    private int getNewNetID() {
//        nextNetID++;
//        return (nextNetID - 1);
//    }
}
