/*
 *  Copyright 2008, 2009, 2010:
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
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import thirteenducks.cor.graphics.BuildingAnimator;
import thirteenducks.cor.graphics.UnitAnimator;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.map.MapIO;

public class RandomMapBuilder {

    public HashMap<Integer, Unit> descUnit;
    public HashMap<Integer, Building> descBuilding;
    CoRMap RandomRogMap;
    ArrayList<Ressource> resList;
    ArrayList<Building> buildingList;
    ArrayList<Unit> unitList;
    int nextNetID = 1;
    ArrayList<Position> Reserviert = new ArrayList<Position>();
    ArrayList<Position> Bruecke = new ArrayList<Position>();
    RandomMapBuilderMapElement[][] workArray;
    int sizeX;
    int sizeY;
    // Muss global sein, wird für die Oasen benötigt
    ArrayList<Position> Wald;

    /*
     * Ich hab den MapBuilder mal deutlich beschleunigt, damit man coole neue Features hinzufügen kann:
     *
     * Der MapBuilder benutzt keine "richtigen" MapElements mehr, da diese beim
     * Millionenfachen Auslesen und Ändern einfach zu lahm wären.
     *
     * Stattdessen wird ein MapBuilderMapElement verwendet, das nur mit Zahlen arbeitet.
     * Zahlen lassen sich viel schneller verarbeiten - egal ob beim auslesen, vergleichen oder setzen
     *
     * Bei in prepareMap wird dann alles in eine richtige Map umgewandelt.
     * Dann können auch Gebäude/Einheiten gesetzt werden etc...
     *
     * Teilweise ist es danach notwendig, die Texturen noch einmal zu verarbeiten. Daher werden die Texturen direkt vor dem Speichern nochmal kopiert.
     * 
     */
    // Wasserfelder sind negativ
    private final int WATERTEX2 = -2;
    private final int WATERTEX = -1;
    private final int LANDTEX = 1;
    private final int LANDTEX2 = 2;
    private final int BRIDGETEX = 3;
    private final int LANDTEX3 = 4;
    private final int LANDTEX4 = 5;
    private final int OASIS1 = 10;
    private final int OASIS2 = 11;
    private final int OASIS3 = 12;
    // Hier stehen die Entsprechenden, echten Texturen
    private String real_LandTex;
    private String real_LandTex2;
    private String real_WaterTex;
    private String real_BridgeTex;
    private String real_WaterTex2;
    private String real_LandTex3;
    private String real_LandTex4;
    private String real_Oasis1;
    private String real_Oasis2;
    private String real_Oasis3;

    public void newMap(byte PlayerNumber, byte Layout, byte Size, byte Theme) {
        long zstVorher = System.currentTimeMillis(); // Zeit stoppen

        int newMapX = 121 + Size * 20 + PlayerNumber * 20;
        int newMapY = 101 + Size * 20 + PlayerNumber * 20;
        sizeX = newMapX;
        sizeY = newMapY;

        workArray = new RandomMapBuilderMapElement[newMapX][newMapY];
        for (int x = 0; x < newMapX; x++) {
            for (int y = 0; y < newMapY; y++) {
                if (x % 2 == y % 2) {
                    workArray[x][y] = new RandomMapBuilderMapElement();
                }
            }
        }

        real_LandTex = "img/ground/testground4.png";
        real_LandTex2 = "img/ground/testground3.png";
        real_LandTex3 = "img/ground/testground5.png";
        real_LandTex4 = "img/ground/testground6.png";
        real_WaterTex = "img/ground/testwater1.png";
        real_WaterTex2 = "img/ground/testwater2.png";
        real_BridgeTex = "img/ground/testground3.png";
        if (Theme == 0) {
            double RndTheme = Math.random() * 3 + 1;
            Theme = (byte) RndTheme;
        }
        if (Theme == 2) {
            real_LandTex = "img/ground/testgroundsnow1.png";
            real_LandTex2 = "img/ground/testgroundsnow2.png";
        } else if (Theme == 3) {
            real_LandTex = "img/ground/testgrounddesert1.png";
            real_LandTex3 = "img/ground/testgrounddesert2.png";
            real_LandTex4 = "img/ground/testgrounddesert3.png";
            real_Oasis1 = "img/ground/desert_oasis1.png";
            real_Oasis2 = "img/ground/desert_oasis2.png";
            real_Oasis3 = "img/ground/desert_oasis3.png";
        }

        if (Layout == 0) {
            double RndLayout = Math.random() * 4 + 1;
            Layout = (byte) RndLayout;
        }

        if (Layout == 1) { //Insel
            zAlles(WATERTEX);
            for (int i = 0; i < (15 + (PlayerNumber + Size) * 1.7); i++) {
                Position alpha = RandPunkt();
                zFeld(alpha.X, alpha.Y, LANDTEX);
                zBreit(LANDTEX, true);
            }
            zWasserRand(5);
            for (int i = 0; i < 10; i++) {
                zStrand(true, WATERTEX);
                zStrand(false, LANDTEX);
            }
            zWasserRand(2);
            zClean(LANDTEX);
            sInseln();
            if (Theme != 2) { // Nicht für Winter ;)
                zFarbStufen();
            }

        } else if (Layout == 2) { //See
            zAlles(LANDTEX);
            zKreis(sizeX, sizeY, Math.min(newMapX / 4, newMapY / 4), WATERTEX);
            zKreis(sizeX, sizeY, Math.min(newMapX / 10, newMapY / 10), LANDTEX2);
            for (int i = 0; i < 10; i++) {
                zStrand(true, WATERTEX);
                zStrand(false, LANDTEX);
            }
            zStrand(false, LANDTEX);
            zBreit(WATERTEX, false);
            zBreit(WATERTEX, false);

        } else if (Layout == 3) { //Ebene
            zAlles(LANDTEX);

            //Flüsse:
            Position alpha = RandPunkt();
            alpha.X = 0;

            Position beta = RandPunkt();
            double RndCoD = Math.random() * 2;
            byte RndCo = (byte) RndCoD;
            if (RndCo == 0) {
                beta.X = sizeX;
            } else {
                beta.Y = sizeY - 1;
                if (beta.X < sizeX * 0.4) {
                    beta.X += ((sizeX - 1) / 2);
                    if (sizeX / 2 % 2 != 0) {
                        beta.X += 1;
                    }
                }
            }

            Position delta = RandPunkt();
            delta.Y = 0;

            Position gamma = new Position((alpha.X + beta.X) / 2, (alpha.Y + beta.Y) / 2);
            if (gamma.X % 2 != gamma.Y % 2) {
                gamma.X++;
            }

            zGerade(alpha, gamma);
            zGerade(alpha, beta);
            zGerade(gamma, delta);
            zStrand(WATERTEX, 1.0);
            zStrand(WATERTEX, 1.0);
            sInseln();

        } else if (Layout == 4) { //Fluss
            zAlles(LANDTEX);
            zRechteck(newMapX / 2 - 10, 0, newMapX / 2 + 10, newMapY / 2 - 40, WATERTEX);
            zRechteck(newMapX / 2 - 12, newMapY / 2 - 39, newMapX / 2 + 12, newMapY / 2 - 21, LANDTEX2);
            zRechteck(newMapX / 2 - 10, newMapY / 2 - 20, newMapX / 2 + 10, newMapY / 2 - 10, WATERTEX);
            zRechteck(newMapX / 2 - 12, newMapY / 2 - 11, newMapX / 2 + 12, newMapY / 2 + 9, LANDTEX2);
            zRechteck(newMapX / 2 - 10, newMapY / 2 + 10, newMapX / 2 + 10, newMapY / 2 + 20, WATERTEX);
            zRechteck(newMapX / 2 - 12, newMapY / 2 + 21, newMapX / 2 + 12, newMapY / 2 + 39, LANDTEX2);
            zRechteck(newMapX / 2 - 10, newMapY / 2 + 40, newMapX / 2 + 10, newMapY - 2, WATERTEX);
        }

        zBreit(BRIDGETEX, false);
        zBreit(BRIDGETEX, false);

        // Jetzt ist die rechenintensive Bearbeitung des Bodens abgeschlossen, jetzt die echte Map erzeugen
        // Ab jetzt dürfen Kollisionsänderungen nurnoch auf der echten Map ausgeführt werden
        // Texturänderungen müssen auf dem workArray ausgeführt werden.

        prepareMap(newMapX, newMapY);

        ArrayList<Building> StartG = sStartpunkt(PlayerNumber); //Startgebäude suchen
        for (int i = 0; i < StartG.size(); i++) {
            buildingList.add(StartG.get(i)); //Startgebäude setzen
        }

        ArrayList<Ressource> RessourcenL = sRessourcen(StartG, PlayerNumber, Theme); //Ressourcen suchen
        for (int i = 0; i < RessourcenL.size(); i++) {
            resList.add(RessourcenL.get(i)); //Ressourcen setzen
        }

        for (int v = 0; v < Reserviert.size(); v++) { //Reservierte Felder freigeben
            // Nur ändern, wenn vorher kein land da war:
            if (workArray[Reserviert.get(v).X][Reserviert.get(v).Y].getTex() < 0) {
                workArray[Reserviert.get(v).X][Reserviert.get(v).Y].setTex(LANDTEX);
            }
            RandomRogMap.visMap[Reserviert.get(v).X][Reserviert.get(v).Y].setCollision(collision.free);
        }

        for (int v = 0; v < Bruecke.size(); v++) { //Brückenfelder freigeben
            RandomRogMap.visMap[Bruecke.get(v).X][Bruecke.get(v).Y].setCollision(collision.free);
        }

        ArrayList<Unit> UnitL = sStartEinheiten(StartG);
        for (int i = 0; i < UnitL.size(); i++) {
            unitList.add(UnitL.get(i)); //Starteinheiten setzen
        }

        // Jetzt "Oasen" suchen und einfärben
        zOasen(Theme);

        // Abschließend nocheinmal die Bodentexturen kopieren.
        // Nachfolgende Änderungen an dem workArray werden nichtmehr berücksichtigt
        copyTextures();

        RandomRogMap.setMapProperty("NEXTNETID", nextNetID);
        saveMap(RandomRogMap);

        long zstNachher = System.currentTimeMillis();
        System.out.println("Map in " + (zstNachher - zstVorher) + " ms erstellt.");
    }

    /**
     * Sucht "Oasen" (Wälder) und färbt den Boden in deren Gegend je nach Theme ein:
     * Wüste: Grüner (Oasen eben)
     * Gemäßigt: Dunkler/Brauner (Waldboden)
     * Schnee: Nix. Ist eh hässlich ;)
     */
    private void zOasen(int theme) {
        /*
         * Verfahren:
         * Großes int-Array anlegen (für jedes Feld ein Datenpunkt) Startwert 0
         * Jeder Baum erhöht in einem Bereich um sich herum die Werte der Felder (echte Kreise + abstandsverstärktes Zufallselement)
         * Dann werden die Felder umgefärbt, wenn sie einen ausreichenden Wert erreichen
         */

        // Unnötig bei Temperate, nicht unterstützt für Schnee:
        if (theme == 3) {
            // Anlegen:
            int[][] oasen = new int[sizeX][sizeY];
            // Jetzt für jeden Baum den Bereich färben
            for (Position baum : Wald) {
                calcOasis(baum.X, baum.Y, oasen);
            }
            // Jetzt Felder mit ausreichend Großen Werten umfärben
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    if (x % 2 != y % 2) {
                        continue;
                    }
                    int val = oasen[x][y];
                    if (val > 50) {
                        workArray[x][y].setTex(OASIS3);
                    } else if (val > 35) {
                        workArray[x][y].setTex(OASIS2);
                    }
                }
            }
        }
    }

    /**
     * Erhöht die Umgebung der Korrdinaten x y im arr-Array
     * @param x
     * @param y
     * @param arr
     */
    private void calcOasis(int x, int y, int[][] arr) {
        /**
         * Dieser Algorithmus scheint noch nen Fehler zu haben.
         * Es werden Bereiche gefärbt, die mehr als 3 Felder von allem Weg liegen.
         */
        for (int a = x - 2; a < x + 2; a++) {
            for (int b = y - 2; b < y + 2; b++) {
                if (a % 2 != b % 2) {
                    continue;
                }
                double dx = x - a;
                double dy = y - b;
                double dist = Math.sqrt((dx * dx) + (dy * dy));
                if (dist <= 2) {
                    try {
                        arr[a][b] += 20 - (1.0 * dist * 8);
                    } catch (IndexOutOfBoundsException ex) {
                    }
                }
            }
        }
    }

    /**
     * Färbt weiter innen liegende Landbereiche ein, damit die Maps abwechslungsreicher aussehen.
     * Darf erst ganz am Ende aufgerufen werden, da jetzt viele verschiedene Texturen verwendet werden, also funktionieren vielleicht manche Mechanismen nichtmehr
     * Streut ein Zufallselement ein
     */
    private void zFarbStufen() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                // Nur echte Felder
                if (x % 2 != y % 2) {
                    continue;
                }
                boolean land = workArray[x][y].getTex() > 0;
                // Brücken nicht
                if (workArray[x][y].getTex() != BRIDGETEX) {
                    int distance = distanceTo(x, y, land, 11);
                    // Jetzt färben
                    if (distance > 10) {
                        workArray[x][y].setTex(land ? LANDTEX4 : WATERTEX2);
                    } else if (distance > 5) {
                        workArray[x][y].setTex(land ? LANDTEX3 : WATERTEX);
                    }
                }
            }
        }
    }

    /**
     * Berechnet kleinsten Abstand zum nächsten Wasser/Landfeld.
     * Vorsicht: Such nicht in alle Richtungen (wegen der Performance) Derzeit suche in 8 Richtungen
     * Vorsicht Abstand ist in "Kreisen" (1 = 1 Feld diagonal)
     * Ende der Map zählt nicht als Treffer.
     * Sucht nur bis maxInterest. Laufzeitkritischer Wert.
     * Wenn man z.B. nacher nur bis 8 vergleicht, muss man auch nur bis 8 suchen.
     * Brücken zählen weder als Land, noch als Wasser.
     */
    private int distanceTo(int x, int y, boolean searchForWater, int maxInterest) {
        int kreis = 1;
        try {
            while (true) {
                // In 8 Richtungen suchen, sofort abbrechen, wenn wir auf Land/Wasser stoßen
                // Fehler hoch zählen. Sobald 8 Fehler kommen, haben wir in allen Richtungen das Ende erreicht, dann spätestens Abbrechen
                int errorCount = 0;
                // N
                try {
                    int testN = workArray[x][y - 2 * kreis].getTex();
                    if (testN != BRIDGETEX && ((testN < 0 && searchForWater) || (testN > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                // NO
                try {
                    int testNO = workArray[x + kreis][y - kreis].getTex();
                    if (testNO != BRIDGETEX && ((testNO < 0 && searchForWater) || (testNO > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                // O
                try {
                    int testO = workArray[x + 2 * kreis][y].getTex();
                    if (testO != BRIDGETEX && ((testO < 0 && searchForWater) || (testO > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                // SO
                try {
                    int testSO = workArray[x + kreis][y + kreis].getTex();
                    if (testSO != BRIDGETEX && ((testSO < 0 && searchForWater) || (testSO > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                // S
                try {
                    int testS = workArray[x][y + 2 * kreis].getTex();
                    if (testS != BRIDGETEX && ((testS < 0 && searchForWater) || (testS > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                // SW
                try {
                    int testSW = workArray[x - kreis][y + kreis].getTex();
                    if (testSW != BRIDGETEX && ((testSW < 0 && searchForWater) || (testSW > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                // W
                try {
                    int testW = workArray[x - 2 * kreis][y].getTex();
                    if (testW != BRIDGETEX && ((testW < 0 && searchForWater) || (testW > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                // NW
                try {
                    int testNW = workArray[x - kreis][y - kreis].getTex();
                    if (testNW != BRIDGETEX && ((testNW < 0 && searchForWater) || (testNW > 0 && !searchForWater))) {
                        return kreis;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    errorCount++;
                }
                kreis++;
                // Abbrechenen mit Maximaldistanz
                if (errorCount >= 8 || kreis > maxInterest) {
                    return maxInterest;
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            // Hier abbrechen
            return kreis;
        }
    }

    /**
     * Erstellt die richtige Map
     */
    private void prepareMap(int newMapX, int newMapY) {
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
        // Texturen drauf schreibe
        copyTextures();

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
                    if (workArray[x][y].isBlocked()) {
                        RandomRogMap.visMap[x][y].setCollision(collision.blocked);
                    } else {
                        RandomRogMap.visMap[x][y].setCollision(collision.free);
                    }
                }
            }
        }

        unitList = new ArrayList<Unit>();	// Leere UnitList einfügen
        RandomRogMap.setMapProperty("UNIT_LIST", unitList);
        buildingList = new ArrayList<Building>();	// Leere BuildingList einfügen
        RandomRogMap.setMapProperty("BUILDING_LIST", buildingList);
        resList = new ArrayList<Ressource>();
        RandomRogMap.setMapProperty("RES_LIST", resList);

    }

    /**
     * Kopiert die Texturen von dem BearbeitungsArray auf die echte Map
     */
    private void copyTextures() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (x % 2 == y % 2) {
                    // Neue Textur einfügen
                    switch (workArray[x][y].getTex()) {
                        case LANDTEX:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex);
                            break;
                        case LANDTEX2:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex2);
                            break;
                        case WATERTEX:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_WaterTex);
                            break;
                        case WATERTEX2:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_WaterTex2);
                            break;
                        case BRIDGETEX:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_BridgeTex);
                            break;
                        case LANDTEX3:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex3);
                            break;
                        case LANDTEX4:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_LandTex4);
                            break;
                        case OASIS1:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_Oasis1);
                            break;
                        case OASIS2:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_Oasis2);
                            break;
                        case OASIS3:
                            RandomRogMap.changeElementProperty(x, y, "ground_tex", real_Oasis3);
                            break;

                    }
                }
            }
        }
    }

    public void saveMap(CoRMap RandomRogMap) { // Speichert die Map ab
        MapIO.saveMap(RandomRogMap, RandomRogMap.mapName);
    }

    public void zAlles(int tex) { // der ganzen Karte dieselbe Textur geben
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (x % 2 != y % 2) {
                    continue;
                }
                zFeld(x, y, tex);
            }
        }
    }

    public void zKreis(int x, int y, double r, int tex) {
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

    public void zRechteck(int x1, int y1, int x2, int y2, int tex) {
        for (int a = x1; a <= x2; a++) {
            for (int b = y1; b <= y2; b++) {
                if (a % 2 != b % 2) {
                    continue;
                }
                zFeld(a, b, tex);
            }
        }
    }

    public void zStrand(int tex, Double percent) {
        ArrayList<Position> change = new ArrayList<Position>();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (i % 2 != j % 2) {
                    continue;
                }
                for (int x = -1; x <= 1; x += 2) {	//Die 4 Nachbarfelder suchen
                    for (int y = -1; y <= 1; y += 2) {
                        if ((i + x) % 2 != (j + y) % 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
                            continue;
                        }
                        if (workArray[i + x][j + y].getTex() == tex) {
                            Position wzw = new Position(i, j);
                            change.add(wzw);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < change.size(); i++) {
            double Random = Math.random();
            if (Random < percent) {
                zFeld(change.get(i).X, change.get(i).Y, tex);
            }
        }
    }

    public void sInseln() { // erkennt Inseln, um kleine zu löschen / Brücken zu bauen
        ArrayList<Position> allef = new ArrayList<Position>();
        ArrayList<Position> open = new ArrayList<Position>();
        ArrayList<Position> closed = new ArrayList<Position>();
        ArrayList<ArrayList<Position>> hae = new ArrayList<ArrayList<Position>>();

        for (int i = 0; i < sizeX; i++) { // Alle freien Felder in AL packen
            for (int j = 0; j < sizeY; j++) {
                if (i % 2 != j % 2) {
                    continue;
                }
                if (!workArray[i][j].isBlocked()) {
                    allef.add(new Position(i, j));
                }
            }
        }

        while (!allef.isEmpty()) { // Wenn es Landfelder gibt, die noch nicht zu einer Insel gehören
            open.add(allef.get(0)); // erstbestes Landfeld nehmen
            allef.remove(0);
            while (!open.isEmpty()) { // freies Feld aussuchen und alle angrenzenden finden
                closed.add(open.get(0));
                int x = open.get(0).X;
                int y = open.get(0).Y;
                open.remove(0);

                ArrayList<Position> verschieben = new ArrayList<Position>(); // 4 Nachbarfelder
                for (int i = 0; i < allef.size(); i++) {
                    if (allef.get(i).X == x + 1 && allef.get(i).Y == y + 1) {
                        verschieben.add(allef.get(i));
                    } else if (allef.get(i).X == x - 1 && allef.get(i).Y == y + 1) {
                        verschieben.add(allef.get(i));
                    } else if (allef.get(i).X == x + 1 && allef.get(i).Y == y - 1) {
                        verschieben.add(allef.get(i));
                    } else if (allef.get(i).X == x - 1 && allef.get(i).Y == y - 1) {
                        verschieben.add(allef.get(i));
                    }
                }

                for (int i = 0; i < verschieben.size(); i++) {
                    open.add(verschieben.get(i));
                    allef.remove(verschieben.get(i));
                }
                verschieben.clear();
            } // Insel fertig

            ArrayList<Position> lassdas = new ArrayList<Position>();
            for (int v = 0; v < closed.size(); v++) {
                lassdas.add(closed.get(v));
            }
            hae.add(lassdas); // Insel in hae tun
            open.clear();
            closed.clear();
        }

        System.out.print("Inseln: " + hae.size() + " -> "); //zu kleine Inseln entfernen
        for (int i = 0; i < hae.size(); i++) {
            if (hae.get(i).size() < 200) {
                for (int j = 0; j < hae.get(i).size(); j++) {
                    zFeld(hae.get(i).get(j).X, hae.get(i).get(j).Y, WATERTEX);
                }
                hae.remove(i);
                i--;
            }
        }
        System.out.println(hae.size());

        int n = hae.get(0).size(); // größte Insel
        int m = 0;
        for (int l = 1; l < hae.size(); l++) {
            if (hae.get(l).size() > n) {
                n = hae.get(l).size();
                m = l;
            }
        }

        for (int i = 0; i < hae.size(); i++) {
            if (i == m) {
                continue;
            }

            int ax = 0;
            int ay = 0;
            for (int j = 0; j < hae.get(i).size(); j++) {
                ax += hae.get(i).get(j).X;
                ay += hae.get(i).get(j).Y;
            }
            ax /= hae.get(i).size(); // durchschnitt
            ay /= hae.get(i).size();

            int nah = 0;
            double dist = 99999;
            for (int z = 0; z < hae.get(m).size(); z++) {
                double rechnen = Math.sqrt(Math.abs(hae.get(m).get(z).X - ax) + Math.abs(hae.get(m).get(z).Y - ay));
                if (rechnen < dist) {
                    dist = rechnen;
                    nah = z;
                }
            }

            int nah2 = 0;
            double dist2 = 99999;
            for (int z = 0; z < hae.get(i).size(); z++) {
                double rechnen = Math.sqrt(Math.abs(hae.get(i).get(z).X - hae.get(m).get(nah).X) + Math.abs(hae.get(i).get(z).Y - hae.get(m).get(nah).Y));
                if (rechnen < dist2) {
                    dist2 = rechnen;
                    nah2 = z;
                }
            }

            zBruecke(new Position(hae.get(i).get(nah2).X, hae.get(i).get(nah2).Y), new Position(hae.get(m).get(nah).X, hae.get(m).get(nah).Y));
        }
    }

    public void zStrand(boolean col, int tex) {
        ArrayList<Position> change = new ArrayList<Position>();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (i % 2 != j % 2) {
                    continue;
                }
                for (int x = -1; x <= 1; x += 2) {	//Die 4 Nachbarfelder suchen
                    for (int y = -1; y <= 1; y += 2) {
                        if ((i + x) % 2 != (j + y) % 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
                            continue;
                        }
                        if (workArray[i + x][j + y].isBlocked() == col) {
                            Position wzw = new Position(i, j);
                            change.add(wzw);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < change.size(); i++) {
            double Random = Math.random();
            if (Random < 0.4) {
                zFeld(change.get(i).X, change.get(i).Y, tex);
            }
        }
    }

    public void zGerade(Position alpha, Position beta) { //eine Gerade aus Wasserfeldern zeichnen
        int vX = beta.X - alpha.X;
        int vY = beta.Y - alpha.Y;
        if (Math.abs(vX) >= Math.abs(vY)) {
            if (vX > 0) {
                for (int i = 0; i < vX; i++) {
                    Position argh = new Position(alpha.X + i, alpha.Y + (i * vY / vX));
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.X -= 1;
                    }
                    zFeld(argh.X, argh.Y, WATERTEX);
                }
            } else {
                for (int i = 0; i > vX; i--) {
                    Position argh = new Position(alpha.X + i, alpha.Y + (i * vY / vX));
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.X -= 1;
                    }
                    zFeld(argh.X, argh.Y, WATERTEX);
                }
            }
        } else {
            if (vY > 0) {
                for (int i = 0; i < vY; i++) {
                    Position argh = new Position(alpha.X + (i * vX / vY), alpha.Y + i);
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.Y -= 1;
                    }
                    zFeld(argh.X, argh.Y, WATERTEX);
                }
            } else {
                for (int i = 0; i > vY; i--) {
                    Position argh = new Position(alpha.X + (i * vX / vY), alpha.Y + i);
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.Y -= 1;
                    }
                    zFeld(argh.X, argh.Y, WATERTEX);
                }
            }
        }
    }

    public void zBruecke(Position alpha, Position beta) { //Bruecke zeichnen
        int vX = beta.X - alpha.X;
        int vY = beta.Y - alpha.Y;
        if (Math.abs(vX) >= Math.abs(vY)) {
            if (vX > 0) {
                for (int i = 0; i < vX; i++) {
                    Position argh = new Position(alpha.X + i, alpha.Y + (i * vY / vX));
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.X -= 1;
                    }
                    zFeld(argh.X, argh.Y, BRIDGETEX);
                    workArray[argh.X][argh.Y].setBlocked(true);
                    Bruecke.add(argh);
                }
            } else {
                for (int i = 0; i > vX; i--) {
                    Position argh = new Position(alpha.X + i, alpha.Y + (i * vY / vX));
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.X -= 1;
                    }
                    zFeld(argh.X, argh.Y, BRIDGETEX);
                    workArray[argh.X][argh.Y].setBlocked(true);
                    Bruecke.add(argh);
                }
            }
        } else {
            if (vY > 0) {
                for (int i = 0; i < vY; i++) {
                    Position argh = new Position(alpha.X + (i * vX / vY), alpha.Y + i);
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.Y -= 1;
                    }
                    zFeld(argh.X, argh.Y, BRIDGETEX);
                    workArray[argh.X][argh.Y].setBlocked(true);
                    Bruecke.add(argh);
                }
            } else {
                for (int i = 0; i > vY; i--) {
                    Position argh = new Position(alpha.X + (i * vX / vY), alpha.Y + i);
                    if (argh.X % 2 != argh.Y % 2) {
                        argh.Y -= 1;
                    }
                    zFeld(argh.X, argh.Y, BRIDGETEX);
                    workArray[argh.X][argh.Y].setBlocked(true);
                    Bruecke.add(argh);
                }
            }
        }
    }

    public void zBreit(int tex, Boolean Test) { //Wasser verbreitern
        if (Test == true) {
            double Rnd = Math.random();
            if (Rnd < 0.5) {
                Test = false;
            }
        }

        ArrayList<Position> Wasser = new ArrayList<Position>();
        if (Test == false) {
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeY; j++) {
                    if (i % 2 != j % 2 || workArray[i][j].getTex() == tex) {
                        continue;
                    }
                    for (int x = -1; x <= 1; x += 2) {	//Die 4 Nachbarfelder suchen
                        for (int y = -1; y <= 1; y += 2) {
                            if ((i + x) % 2 != (j + y) % 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
                                continue;
                            }
                            if (workArray[i + x][j + y].getTex() == tex) {
                                Position wzw = new Position(i, j);
                                Wasser.add(wzw);
                            }
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < sizeX; i++) {
                for (int j = 0; j < sizeY; j++) {
                    if (i % 2 != j % 2 || workArray[i][j].getTex() == tex) {

                        continue;
                    }
                    for (int x = -2; x <= 2; x++) {	//Die 8 Nachbarfelder suchen
                        for (int y = -2; y <= 2; y++) {
                            if (Math.abs(x) + Math.abs(y) != 2 || (i + x) <= 0 || (i + x) >= sizeX || (j + y) <= 0 || (j + y) >= sizeY) {
                                continue;
                            }
                            if (workArray[i + x][j + y].getTex() == tex) {
                                Position wzw = new Position(i, j);
                                Wasser.add(wzw);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < Wasser.size(); i++) {
            zFeld(Wasser.get(i).X, Wasser.get(i).Y, tex);
        }
    }

    public void zFeld(int x, int y, int tex) { //ein einzelnes Feld zeichnen
        if (x % 2 == y % 2 && x >= 0 && x <= sizeX && y >= 0 && y < sizeY) {
            workArray[x][y].setTex(tex);
            if (tex == WATERTEX) {
                workArray[x][y].setBlocked(true);
            } else {
                workArray[x][y].setBlocked(false);
            }
        } else {
            System.out.println("Invalid Field!");
        }

    }

    public Position RandPunkt() {
        double RndPunktX = Math.random() * sizeY / 2;
        int RndPunktXi = (int) RndPunktX;
        double RndPunktY = Math.random() * sizeX / 2;
        int RndPunktYi = (int) RndPunktY;
        Position alpha = new Position(RndPunktXi * 2, RndPunktYi * 2);
        return alpha;
    }

    public void zWasserRand(int i) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (x % 2 == y % 2) {
                    if (x < i || x > (sizeX - (i + 1)) || y < i || y > (sizeY - (i + 1))) {
                        zFeld(x, y, WATERTEX);
                    }
                }
            }
        }
    }

    public void zClean(int tex) {
        for (int u = 1; u < sizeX - 1; u++) {
            for (int j = 1; j < sizeY - 1; j++) {
                if (u % 2 == j % 2 && workArray[u][j].getTex() == WATERTEX) {
                    boolean vier = true;
                    for (int i = -1; i <= 1; i += 2) {
                        for (int o = -1; o <= 1; o += 2) {
                            if (u + i < 0 || j + o < 0 || u + i >= sizeX || j + o >= sizeY) {
                                continue;
                            }
                            if (workArray[u + i][j + o].getTex() == WATERTEX) {
                                vier = false;
                            }
                        }
                    }
                    if (vier) {
                        zFeld(u, j, tex);
                    }
                }
            }
        }
    }

    //setzt für jeden Spieler ein Startgebäude
    public ArrayList<Building> sStartpunkt(int player) {
        ArrayList<Position> Frei = new ArrayList<Position>(); //Arraylist mit allen möglichen Startpositionen
        ArrayList<Building> StartG = new ArrayList<Building>();//Arraylist mit den endgültigen Startgebäuden

        for (int i = 1; i <= player; i++) {	//für jeden Spieler:

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
                    dist[z] = Math.min(Math.min(Frei.get(z).X, Frei.get(z).Y), Math.min(RandomRogMap.getMapSizeX() - Frei.get(z).X, RandomRogMap.getMapSizeY() - Frei.get(z).Y));
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
                        Frei.get(q).X = -1;
                    }
                }
                int w = 0;
                while (w < Frei.size()) {
                    if (Frei.get(w).X == -1) {
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
                        work[d] = Math.sqrt(Math.pow(Frei.get(z).X - StartG.get(d).position.X + 1, 2) + Math.pow(Frei.get(z).Y - StartG.get(d).position.Y, 2));
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
                        Frei.get(q).X = -1;
                    }
                }
                int w = 0;
                while (w < Frei.size()) {
                    if (Frei.get(w).X == -1) {
                        Frei.remove(w); //Felder mit zu geringer Distanz löschen
                    } else {
                        w++;
                    }
                }
            }

            double RndStartD = Math.random() * Frei.size(); //zufällige Startposition aus der Frei-Arraylist
            int RndStart = (int) RndStartD;
            int x = Frei.get(RndStart).X;
            int y = Frei.get(RndStart).Y;

            Building Haus = new Building(x, y, getNewNetID());   //Haus an diese Position setzen

            Haus.descTypeId = 1;
            Haus.defaultTexture = "img/buildings/human_main_e1.png";

            Haus.playerId = i;
            Haus.anim = new BuildingAnimator();
            Haus.name = "Village Center";
            Haus.hitpoints = 2000;
            Haus.maxhitpoints = 2000;
            Haus.offsetY = 4;
            Haus.z1 = 6;
            Haus.z2 = 6;
            RandomRogMap.visMap[x + 5][y - 5].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 4][y - 4].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 6][y - 4].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 3][y - 3].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 5][y - 3].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 7][y - 3].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 2][y - 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 4][y - 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 6][y - 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 8][y - 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 1][y - 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 3][y - 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 5][y - 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 7][y - 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 9][y - 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x][y].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 2][y].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 4][y].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 6][y].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 8][y].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 10][y].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 1][y + 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 3][y + 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 5][y + 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 7][y + 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 9][y + 1].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 2][y + 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 4][y + 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 6][y + 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 8][y + 2].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 3][y + 3].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 5][y + 3].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 7][y + 3].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 4][y + 4].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 6][y + 4].setCollision(collision.blocked);
            RandomRogMap.visMap[x + 5][y + 5].setCollision(collision.blocked);

            RandomRogMap.visMap[x - 2][y].setCollision(collision.blocked);
            Reserviert.add(new Position(x - 2, y));
            RandomRogMap.visMap[x + 12][y].setCollision(collision.blocked);
            Reserviert.add(new Position(x + 12, y));
            for (int k = -1; k <= 5; k++) {
                RandomRogMap.visMap[x + k][y + k + 2].setCollision(collision.blocked);
                Reserviert.add(new Position(x + k, y + k + 2));
                RandomRogMap.visMap[x + k][y - 2 - k].setCollision(collision.blocked);
                Reserviert.add(new Position(x + k, y - 2 - k));
            }
            for (int k = 6; k <= 11; k++) {
                RandomRogMap.visMap[x + k][y - k + 12].setCollision(collision.blocked);
                Reserviert.add(new Position(x + k, y - k + 12));
                RandomRogMap.visMap[x + k][k + y - 12].setCollision(collision.blocked);
                Reserviert.add(new Position(x + k, k + y - 12));
            }


            StartG.add(Haus); //Startgebäude in Arraylist eintragen
        }
        return StartG; //Arraylist zurückgeben
    }

    // Setzt Ressourcen auf die Map
    public ArrayList<Ressource> sRessourcen(ArrayList<Building> StartG, int player, int Theme) {
        ArrayList<Position> Frei = new ArrayList<Position>(); //Arraylist mit allen freien Rogpositions
        ArrayList<Ressource> ResPos = new ArrayList<Ressource>(); //Liste mit RogRessourcen, die zurückgegeben wird
        ArrayList<ArrayList<Position>> Hae = new ArrayList<ArrayList<Position>>();

        //Nahrung neben Hauptgebäude setzen
        for (int i = 2; i < RandomRogMap.getMapSizeX() - 3; i++) { //alle freien Felder suchen
            for (int j = 2; j < RandomRogMap.getMapSizeY() - 3; j++) {
                if (i % 2 != j % 2) {
                    continue;
                }
                if (RandomRogMap.visMap[i][j].getCollision().equals(collision.free)) {
                    Frei.add(new Position(i, j));
                }
            }
        }

        for (int i = 0; i < player; i++) { //für jeden Spieler einmal ausführen
            ArrayList<Position> Resposis = new ArrayList<Position>();
            for (int j = 1; j < Frei.size(); j++) {
                double distance = Math.sqrt(Math.pow(Frei.get(j).X - StartG.get(i).position.X + 1, 2) + Math.pow(Frei.get(j).Y - StartG.get(i).position.Y, 2));
                if (distance < 15 && distance > 2) {
                    Resposis.add(Frei.get(j)); //alle freien Felder in der Nähe der Startgebäude
                }
                if (Resposis.isEmpty()) {
                    if (distance < 30) {
                        Resposis.add(Frei.get(j));
                    }
                }
            }
            Hae.add(Resposis);
        }

        for (int i = 0; i < Hae.size(); i++) {
            double RndResPd = Math.random() * Hae.get(i).size(); //eine zufällige RogPosition
            int RndResP = (int) RndResPd;

            Ressource newres = new Ressource(1, "img/res/FOOD0.png", getNewNetID()); //eine Ressource neben das Startgebäude setzen
            Position Resi = new Position(Hae.get(i).get(RndResP).X, Hae.get(i).get(RndResP).Y);
            newres.position = Resi;
            RandomRogMap.visMap[Resi.X][Resi.Y].setCollision(collision.blocked);
            ResPos.add(newres);

            Reserviert.add(new Position(Resi.X - 1, Resi.Y - 1)); // 4 Nachbarfelder reservieren
            RandomRogMap.visMap[Resi.X - 1][Resi.Y - 1].setCollision(collision.blocked);
            Reserviert.add(new Position(Resi.X - 1, Resi.Y + 1));
            RandomRogMap.visMap[Resi.X - 1][Resi.Y + 1].setCollision(collision.blocked);
            Reserviert.add(new Position(Resi.X + 1, Resi.Y - 1));
            RandomRogMap.visMap[Resi.X + 1][Resi.Y - 1].setCollision(collision.blocked);
            Reserviert.add(new Position(Resi.X + 1, Resi.Y + 1));
            RandomRogMap.visMap[Resi.X + 1][Resi.Y + 1].setCollision(collision.blocked);

            Hae.get(i).clear();
            for (int t = 0; t < Frei.size(); t++) {
                if (Frei.get(t).X == newres.position.X) {
                    if (Frei.get(t).Y == newres.position.Y) {
                        Frei.remove(t);
                        t--;
                    }
                } else if (Frei.get(t).X == newres.position.X - 1 || Frei.get(t).X == newres.position.X + 1) {
                    if (Frei.get(t).Y == newres.position.Y - 1 || Frei.get(t).Y == newres.position.Y + 1) {
                        Frei.remove(t);
                        t--;
                    }
                }
            }

            for (int w = 0; w < Frei.size(); w++) { //freie Felder neben der Rogressource suchen
                double distance = Math.sqrt(Math.pow(Frei.get(w).X - newres.position.X, 2) + Math.pow(Frei.get(w).Y - newres.position.Y, 2));
                if (distance < 6) {
                    Hae.get(i).add(Frei.get(w));
                }
            }

            for (int m = 1; m < 4; m++) { //mehr Rogressourcen neben die erste setzen
                double mRndResPd = Math.random() * Hae.get(i).size();
                int mRndResP = (int) mRndResPd;

                Ressource newres2 = new Ressource(1, "img/res/FOOD0.png", getNewNetID());
                Position Resi2 = new Position(Hae.get(i).get(mRndResP).X, Hae.get(i).get(mRndResP).Y);
                newres2.position = Resi2;
                RandomRogMap.visMap[Resi2.X][Resi2.Y].setCollision(collision.blocked);

                Reserviert.add(new Position(Resi2.X - 1, Resi2.Y - 1)); // 4 Nachbarfelder reservieren
                RandomRogMap.visMap[Resi2.X - 1][Resi2.Y - 1].setCollision(collision.blocked);
                Reserviert.add(new Position(Resi2.X - 1, Resi2.Y + 1));
                RandomRogMap.visMap[Resi2.X - 1][Resi2.Y + 1].setCollision(collision.blocked);
                Reserviert.add(new Position(Resi2.X + 1, Resi2.Y - 1));
                RandomRogMap.visMap[Resi2.X + 1][Resi2.Y - 1].setCollision(collision.blocked);
                Reserviert.add(new Position(Resi2.X + 1, Resi2.Y + 1));
                RandomRogMap.visMap[Resi2.X + 1][Resi2.Y + 1].setCollision(collision.blocked);

                ResPos.add(newres2);
                Hae.get(i).remove(mRndResP);

                for (int t = 0; t < Hae.get(i).size(); t++) {
                    if (Hae.get(i).get(t).X == newres2.position.X) {
                        if (Hae.get(i).get(t).Y == newres2.position.Y) {
                            Hae.get(i).remove(t);
                            t--;
                        }
                    } else if (Hae.get(i).get(t).X == newres2.position.X - 1 || Hae.get(i).get(t).X == newres2.position.X + 1) {
                        if (Hae.get(i).get(t).Y == newres2.position.Y - 1 || Hae.get(i).get(t).Y == newres2.position.Y + 1) {
                            Hae.get(i).remove(t);
                            t--;
                        }
                    }
                }
            }
        }

        // WÄLDER SETZEN
        Frei.clear();
        for (int i = 2; i < RandomRogMap.getMapSizeX() - 3; i++) { //alle freien Felder suchen
            for (int j = 2; j < RandomRogMap.getMapSizeY() - 3; j++) {
                if (i % 2 != j % 2) {
                    continue;
                }
                if (RandomRogMap.visMap[i][j].getCollision().equals(collision.free)) {
                    Frei.add(new Position(i, j));
                }
            }
        }

        Wald = new ArrayList<Position>();
        ArrayList<Position> Waldpos = Frei;
        ArrayList<Integer> Waldminus = new ArrayList<Integer>();
        ArrayList<Integer> Waldtrf = new ArrayList<Integer>();
        int platz;

        for (int z = 0; z < 2; z++) { //Für jedes Feld überprüfen, ob Nachbarfelder frei sind, sonst aus Waldpos entfernen
            for (int i = 0; i < Waldpos.size(); i++) {
                boolean allesfrei = true;
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        if (Math.abs(x) + Math.abs(y) == 2) {
                            if (RandomRogMap.visMap[Waldpos.get(i).X + x][Waldpos.get(i).Y + y].getCollision().equals(collision.blocked)) {
                                allesfrei = false;
                            }
                        }
                    }
                }
                if (allesfrei == false) {
                    Waldminus.add(i);
                }
            }
            for (int i = Waldminus.size() - 1; i >= 0; i--) {
                Waldpos.remove((int) Waldminus.get(i));
            }
            Waldminus.clear();
        }
        platz = Waldpos.size();

        boolean genugw = false;
        while (!genugw) { //Wälder setzen, bis es genug sind
            int einFeld = (int) (Waldpos.size() * Math.random());
            int x = Waldpos.get(einFeld).X;
            int y = Waldpos.get(einFeld).Y;
            Wald.add(Waldpos.get(einFeld));
            Waldpos.remove(einFeld);

            for (int i = 0; i < Waldpos.size(); i++) {
                double zufall = 5 + Math.random() * 11;
                if (Math.sqrt((Waldpos.get(i).X - x) * (Waldpos.get(i).X - x) + (Waldpos.get(i).Y - y) * (Waldpos.get(i).Y - y)) < zufall) {
                    Wald.add(Waldpos.get(i));
                    Waldtrf.add(i);
                }
            }
            for (int i = Waldtrf.size() - 1; i >= 0; i--) {
                Waldpos.remove((int) Waldtrf.get(i));
            }
            if (Wald.size() * 7 > platz) {
                genugw = true;
                System.out.println(Wald.size() + " / " + platz);
            }
            Waldtrf.clear();
        }

        double freq = 0.75 + 0.15 * Math.random();
        for (int i = 0; i < Wald.size(); i++) {
            double randomt = Math.random();
            if (randomt >= freq) {
                String Holz = "img/res/WOOD1.png";
                if (Theme == 1) {
                    double Rnd2 = Math.random();
                    if (Rnd2 < 0.5) {
                        Holz = "img/res/WOOD0.png";
                    }
                } else if (Theme == 3) {
                    double Rnd2 = Math.random();
                    if (Rnd2 < 0.5) {
                        Holz = "img/res/WOOD2.png";
                    } else {
                        Holz = "img/res/WOOD3.png";
                    }
                }
                Ressource newres = new Ressource(2, Holz, getNewNetID()); //eine Ressource neben das Startgebäude setzen
                Position Resi = new Position(Wald.get(i).X, Wald.get(i).Y);
                newres.position = Resi;
                RandomRogMap.visMap[Resi.X][Resi.Y].setCollision(collision.blocked);
                ResPos.add(newres);
            }
        }

        //Metall
        Frei.clear();
        Hae.clear();
        for (int u = 4; u < RandomRogMap.getMapSizeX() - 6; u++) { //alle freien Felder suchen
            for (int j = 5; j < RandomRogMap.getMapSizeY() - 5; j++) {
                if (u % 2 != j % 2) {
                    continue;
                }
                boolean frei = true;
                if (RandomRogMap.visMap[u][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 2][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 2][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u][j - 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u][j + 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 1][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 1][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j - 3].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 2][j - 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 3][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 4][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 3][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 2][j + 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j + 3].getCollision().equals(collision.blocked)) {
                    frei = false;
                }
                if (frei) {
                    Frei.add(new Position(u, j));
                }
            }
        }

        for (int i = 0; i < player; i++) { //für jeden Spieler einmal ausführen
            ArrayList<Position> Resposis = new ArrayList<Position>();
            for (int j = 1; j < Frei.size(); j++) {
                double distance = Math.sqrt(Math.pow(Frei.get(j).X - StartG.get(i).position.X + 1, 2) + Math.pow(Frei.get(j).Y - StartG.get(i).position.Y, 2));
                if (distance < 20 && distance > 5) {
                    Resposis.add(Frei.get(j)); //alle freien Felder in der Nähe der Startgebäude
                }
                if (Resposis.isEmpty()) {
                    if (distance < 40) {
                        Resposis.add(Frei.get(j));
                    }
                }
            }
            Hae.add(Resposis);
        }

        for (int i = 0; i < Hae.size(); i++) {
            double RndResPd = Math.random() * Hae.get(i).size(); //eine zufällige RogPosition
            int RndResP = (int) RndResPd;

            Ressource newres = new Ressource(3, "img/res/METAL0.png", getNewNetID());
            Position Resi = new Position(Hae.get(i).get(RndResP).X, Hae.get(i).get(RndResP).Y);
            newres.position = Resi;
            RandomRogMap.visMap[Resi.X][Resi.Y].setCollision(collision.blocked);
            RandomRogMap.visMap[Resi.X + 2][Resi.Y].setCollision(collision.blocked);
            RandomRogMap.visMap[Resi.X + 1][Resi.Y + 1].setCollision(collision.blocked);
            RandomRogMap.visMap[Resi.X + 1][Resi.Y - 1].setCollision(collision.blocked);
            ResPos.add(newres);

            for (int t = 0; t < Frei.size(); t++) {
                if (Frei.get(t).X == newres.position.X + 1 && Frei.get(t).Y == newres.position.Y + 1) {
                    Frei.remove(t);
                } else if (Frei.get(t).X == newres.position.X && Frei.get(t).Y == newres.position.Y) {
                    Frei.remove(t);
                } else if (Frei.get(t).X == newres.position.X + 2 && Frei.get(t).Y == newres.position.Y) {
                    Frei.remove(t);
                } else if (Frei.get(t).X == newres.position.X + 1 && Frei.get(t).Y == newres.position.Y - 1) {
                    Frei.remove(t);
                }
            }

            Hae.get(i).clear();
        }

        //Gold
        Frei.clear();
        Hae.clear();
        for (int u = 4; u < RandomRogMap.getMapSizeX() - 6; u++) { //alle freien Felder suchen
            for (int j = 5; j < RandomRogMap.getMapSizeY() - 5; j++) {
                if (u % 2 != j % 2) {
                    continue;
                }
                boolean frei = true;
                if (RandomRogMap.visMap[u][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 2][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 2][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u][j - 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u][j + 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 1][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 1][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j - 3].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 2][j - 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 3][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 4][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 3][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 2][j + 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j + 3].getCollision().equals(collision.blocked)) {
                    frei = false;
                }
                if (frei) {
                    Frei.add(new Position(u, j));
                }
            }
        }

        for (int i = 0; i < player; i++) { //für jeden Spieler einmal ausführen
            ArrayList<Position> Resposis = new ArrayList<Position>();
            for (int j = 1; j < Frei.size(); j++) {
                double distance = Math.sqrt(Math.pow(Frei.get(j).X - StartG.get(i).position.X + 1, 2) + Math.pow(Frei.get(j).Y - StartG.get(i).position.Y, 2));
                if (distance < 20 && distance > 5) {
                    Resposis.add(Frei.get(j)); //alle freien Felder in der Nähe der Startgebäude
                }
                if (Resposis.isEmpty()) {
                    if (distance < 40) {
                        Resposis.add(Frei.get(j));
                    }
                }
            }
            Hae.add(Resposis);
        }

        for (int i = 0; i < Hae.size(); i++) {
            double RndResPd = Math.random() * Hae.get(i).size(); //eine zufällige RogPosition
            int RndResP = (int) RndResPd;

            Ressource newres = new Ressource(4, "img/res/COINS0.png", getNewNetID());
            Position Resi = new Position(Hae.get(i).get(RndResP).X, Hae.get(i).get(RndResP).Y);
            newres.position = Resi;
            RandomRogMap.visMap[Resi.X][Resi.Y].setCollision(collision.blocked);
            RandomRogMap.visMap[Resi.X + 2][Resi.Y].setCollision(collision.blocked);
            RandomRogMap.visMap[Resi.X + 1][Resi.Y + 1].setCollision(collision.blocked);
            RandomRogMap.visMap[Resi.X + 1][Resi.Y - 1].setCollision(collision.blocked);
            ResPos.add(newres);

            for (int t = 0; t < Frei.size(); t++) {
                if (Frei.get(t).X == newres.position.X + 1 && Frei.get(t).Y == newres.position.Y + 1) {
                    Frei.remove(t);
                } else if (Frei.get(t).X == newres.position.X && Frei.get(t).Y == newres.position.Y) {
                    Frei.remove(t);
                } else if (Frei.get(t).X == newres.position.X + 2 && Frei.get(t).Y == newres.position.Y) {
                    Frei.remove(t);
                } else if (Frei.get(t).X == newres.position.X + 1 && Frei.get(t).Y == newres.position.Y - 1) {
                    Frei.remove(t);
                }
            }

            Hae.get(i).clear();
        }

        //Bäume neben Startgebäude
        Frei.clear();
        Hae.clear();
        for (int i = 2; i < RandomRogMap.getMapSizeX() - 3; i++) { //alle freien Felder suchen
            for (int j = 2; j < RandomRogMap.getMapSizeY() - 3; j++) {
                if (i % 2 != j % 2) {
                    continue;
                }
                if (RandomRogMap.visMap[i][j].getCollision().equals(collision.free)) {
                    Frei.add(new Position(i, j));
                }
            }
        }

        for (int i = 0; i < player; i++) { //für jeden Spieler einmal ausführen
            ArrayList<Position> Resposis = new ArrayList<Position>();
            for (int j = 1; j < Frei.size(); j++) {
                double distance = Math.sqrt(Math.pow(Frei.get(j).X - StartG.get(i).position.X + 1, 2) + Math.pow(Frei.get(j).Y - StartG.get(i).position.Y, 2));
                if (distance < 15 && distance > 2) {
                    Resposis.add(Frei.get(j)); //alle freien Felder in der Nähe der Startgebäude
                }
                if (Resposis.isEmpty()) {
                    if (distance < 30) {
                        Resposis.add(Frei.get(j));
                    }
                }
            }
            Hae.add(Resposis);
        }

        for (int i = 0; i < Hae.size(); i++) {
            double RndResPd = Math.random() * Hae.get(i).size(); //eine zufällige RogPosition
            int RndResP = (int) RndResPd;

            String Baumtyp = "img/res/WOOD1.png"; // Welcher Baumtyp?
            if (Theme == 1) {
                double Rnd2 = Math.random();
                if (Rnd2 < 0.5) {
                    Baumtyp = "img/res/WOOD0.png";
                }
            } else if (Theme == 3) {
                double Rnd2 = Math.random();
                if (Rnd2 < 0.5) {
                    Baumtyp = "img/res/WOOD2.png";
                } else {
                    Baumtyp = "img/res/WOOD3.png";
                }
            }

            Ressource newres = new Ressource(2, Baumtyp, getNewNetID()); //eine Ressource neben das Startgebäude setzen
            Position Resi = new Position(Hae.get(i).get(RndResP).X, Hae.get(i).get(RndResP).Y);
            newres.position = Resi;
            RandomRogMap.visMap[Resi.X][Resi.Y].setCollision(collision.blocked);
            ResPos.add(newres);
            Hae.get(i).clear();
            for (int t = 0; t < Frei.size(); t++) {
                if (Frei.get(t).X == newres.position.X) {
                    if (Frei.get(t).Y == newres.position.Y) {
                        Frei.remove(t);
                    }
                }
            }

            for (int w = 0; w < Frei.size(); w++) { //freie Felder neben der Rogressource suchen
                double distance = Math.sqrt(Math.pow(Frei.get(w).X - newres.position.X, 2) + Math.pow(Frei.get(w).Y - newres.position.Y, 2));
                if (distance < 5) {
                    Hae.get(i).add(Frei.get(w));
                }
            }

            int Zufall = (int) (Math.random() * 3 + 1); // zufällig 2,3 oder 4 Bäume
            for (int m = 1; m < Zufall; m++) { //mehr Rogressourcen neben die erste setzen
                double mRndResPd = Math.random() * Hae.get(i).size();
                int mRndResP = (int) mRndResPd;

                Baumtyp = "img/res/WOOD1.png"; // Welcher Baumtyp?
                if (Theme == 1) {
                    double Rnd2 = Math.random();
                    if (Rnd2 < 0.5) {
                        Baumtyp = "img/res/WOOD0.png";
                    }
                } else if (Theme == 3) {
                    double Rnd2 = Math.random();
                    if (Rnd2 < 0.5) {
                        Baumtyp = "img/res/WOOD2.png";
                    } else {
                        Baumtyp = "img/res/WOOD3.png";
                    }
                }

                Ressource newres2 = new Ressource(2, Baumtyp, getNewNetID());
                Position Resi2 = new Position(Hae.get(i).get(mRndResP).X, Hae.get(i).get(mRndResP).Y);
                newres2.position = Resi2;
                RandomRogMap.visMap[Resi2.X][Resi2.Y].setCollision(collision.blocked);
                ResPos.add(newres2);
                Hae.get(i).remove(mRndResP);
                Zufall = (int) (Math.random() * 3 + 2);
            }
        }

        // Einzelne Bäume
        for (int u = 3; u < RandomRogMap.getMapSizeX() - 3; u++) { // einzelne Bäume
            for (int j = 3; j < RandomRogMap.getMapSizeY() - 3; j++) {
                if (u % 2 != j % 2) {
                    continue;
                }
                boolean frei = true;
                if (RandomRogMap.visMap[u][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 2][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 2][j].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u][j - 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u][j + 2].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 1][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u + 1][j - 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if (RandomRogMap.visMap[u - 1][j + 1].getCollision().equals(collision.blocked)) {
                    frei = false;
                } else if ("img/ground/testground3.png".equals(RandomRogMap.getElementProperty(u, j, "ground_tex"))) {
                    frei = false;
                }

                if (frei) {
                    double Rnd = Math.random();
                    if (Rnd < 0.001) {
                        RandomRogMap.changeElementProperty(u, j, "fix_tex", "img/fix/teststone.png");
                        RandomRogMap.visMap[u][j].setCollision(collision.blocked);
                    } else if (Rnd < 0.002) {
                        Ressource newres = new Ressource(1, "img/res/FOOD0.png", getNewNetID()); //eine Ressource neben das Startgebäude setzen
                        Position Resi = new Position(u, j);
                        newres.position = Resi;
                        RandomRogMap.visMap[Resi.X][Resi.Y].setCollision(collision.blocked);
                        ResPos.add(newres);
                    } else if (Rnd < 0.009) {
                        String Holz = "img/res/WOOD1.png";
                        if (Theme == 1) {
                            double Rnd2 = Math.random();
                            if (Rnd2 < 0.5) {
                                Holz = "img/res/WOOD0.png";
                            }
                        } else if (Theme == 3) {
                            double Rnd2 = Math.random();
                            if (Rnd2 < 0.5) {
                                Holz = "img/res/WOOD2.png";
                            } else {
                                Holz = "img/res/WOOD3.png";
                            }
                        }
                        Ressource newres = new Ressource(2, Holz, getNewNetID()); //eine Ressource neben das Startgebäude setzen
                        Position Resi = new Position(u, j);
                        newres.position = Resi;
                        RandomRogMap.visMap[Resi.X][Resi.Y].setCollision(collision.blocked);
                        ResPos.add(newres);
                    }
                }
            }
        }

        return ResPos;
    }

    public ArrayList<Unit> sStartEinheiten(ArrayList<Building> StartG) {
        ArrayList<Unit> StartU = new ArrayList<Unit>(); //Arraylist mit den Starteinheiten

        for (int i = 0; i < StartG.size(); i++) {	//für jeden Spieler 4 Starteinheiten setzen

            Unit Einheit = new Unit(StartG.get(i).getX() + 4, StartG.get(i).getY() + 6, getNewNetID());
            Einheit.descTypeId = 401;
            Einheit.playerId = i + 1;
            Einheit.anim = new UnitAnimator();
            StartU.add(Einheit);
            RandomRogMap.visMap[Einheit.position.X][Einheit.position.Y].setCollision(collision.occupied);

            Unit Einheit2 = new Unit(StartG.get(i).getX() + 5, StartG.get(i).getY() + 7, getNewNetID());
            Einheit2.descTypeId = 401;
            Einheit2.playerId = i + 1;
            Einheit2.anim = new UnitAnimator();
            StartU.add(Einheit2);
            RandomRogMap.visMap[Einheit2.position.X][Einheit2.position.Y].setCollision(collision.occupied);

            Unit Einheit3 = new Unit(StartG.get(i).getX() + 6, StartG.get(i).getY() + 6, getNewNetID());
            Einheit3.descTypeId = 401;
            Einheit3.playerId = i + 1;
            Einheit3.anim = new UnitAnimator();
            StartU.add(Einheit3);
            RandomRogMap.visMap[Einheit3.position.X][Einheit3.position.Y].setCollision(collision.occupied);

            Unit Einheit4 = new Unit(StartG.get(i).getX() + 3, StartG.get(i).getY() + 5, getNewNetID());
            Einheit4.descTypeId = 401;
            Einheit4.playerId = i + 1;
            Einheit4.anim = new UnitAnimator();
            StartU.add(Einheit4);
            RandomRogMap.visMap[Einheit4.position.X][Einheit4.position.Y].setCollision(collision.occupied);

            Unit Einheit5 = new Unit(StartG.get(i).getX() + 8, StartG.get(i).getY() + 4, getNewNetID());
            Einheit5.descTypeId = 402;
            Einheit5.playerId = i + 1;
            Einheit5.anim = new UnitAnimator();
            StartU.add(Einheit5);
            RandomRogMap.visMap[Einheit5.position.X][Einheit5.position.Y].setCollision(collision.occupied);
        }

        return StartU;
    }

    private int getNewNetID() {
        nextNetID++;
        return (nextNetID - 1);
    }
}
