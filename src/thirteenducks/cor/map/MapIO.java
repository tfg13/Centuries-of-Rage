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
package thirteenducks.cor.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.DescParamsBuilding;
import thirteenducks.cor.game.DescParamsUnit;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.NeutralBuilding;
import thirteenducks.cor.game.PlayersBuilding;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.Unit2x2;
import thirteenducks.cor.game.Unit3x3;

/**
 * Stellt Methoden zum Lesen und Schreiben von CoRMaps zur Verfügung
 */
public class MapIO {

    /**
     * Zum Laden einer Client-/Editortauglichen Version der Map.
     * Clientmaps enthalten Texturen, aber keine Verwaltungssysteme für Einheiten (Kollision etc.)
     */
    public static final int MODE_CLIENT = 1;
    /**
     * Zum Laden einer Servertauglichen Version der Map.
     * Servermaps enthalten Verwaltungssysteme für Einheiten, aber keine Texturen.
     */
    public static final int MODE_SERVER = 2;

    private MapIO() {
    }

    /**
     * Öffnet die angegebene Mapdatei und ließt die Map vollständig aus.
     * Liest die RMAP-Daten ein.
     * @return Die geladene CoRMap - Einsatzbereit
     */
    public static CoRMap readMap(String path, int mapMode) {
        // Jetzt Map öffnen
        CoRMap theMap = null;
        ZipFile zipfile = null;
        BufferedReader reader = null;
        try {
            zipfile = new ZipFile(path);
            ZipEntry rmap = zipfile.getEntry("RMAP");
            reader = new BufferedReader(new InputStreamReader(zipfile.getInputStream(rmap)));
            String infoline = reader.readLine();
            // Die Infozeile enthält (in dieser Reihenfolge): X Y unitList.size() buildingList.size() resList.size() nextNetID
            // Rauslesen
            String[] data = infoline.split(" ");
            int newMapX = Integer.parseInt(data[0]);
            int newMapY = Integer.parseInt(data[1]);
            int uS = Integer.parseInt(data[2]);
            int bS = Integer.parseInt(data[3]);
            int rS = Integer.parseInt(data[4]);
            int nnid = Integer.parseInt(data[5]);
            // DESC-Settings lesen:
            // Einglesen, normal weitermachen.

            ArrayList<File> olist = new ArrayList<File>();
            ArrayList<File> flist = new ArrayList<File>();
            ArrayList<File> dlist = new ArrayList<File>();

            File OrdnerSuchen = new File("game/epoch"); //Unterordner in "game" suchen
            File[] Ordner = OrdnerSuchen.listFiles();
            for (File ord : Ordner) {
                if (ord.isDirectory() && !ord.getName().startsWith(".") && !ord.getName().startsWith(".")) {
                    olist.add(ord);
                }
            }

            for (int i = 0; i < olist.size(); i++) { //Dateien in den Unterordnern suchen
                String Unterordner = olist.get(i).toString();
                File gameOrdner = new File(Unterordner);
                File[] files = gameOrdner.listFiles();
                flist.addAll(Arrays.asList(files));
            }

            for (int i = 0; i < flist.size(); i++) {
                File file = flist.get(i);
                if (file.getName().endsWith("~") || file.getName().startsWith(".")) { // Sicherungsdateien und unsichtbares Aussortieren
                    flist.remove(i);
                    i--;
                } else {
                    if (file.getName().startsWith("d") || file.getName().startsWith("D")) {
                        dlist.add(file);
                    }
                }
            }

            HashMap<Integer, Unit> descTypeUnit = new HashMap<Integer, Unit>();
            HashMap<Integer, Building> descTypeBuilding = new HashMap<Integer, Building>();

            for (File descFile : dlist) {
                // Read game/descTypes
                FileReader descReader = null;
                try {
                    descReader = new FileReader(descFile);
                    BufferedReader bdescReader = new BufferedReader(descReader);
                    String zeile = null;
                    // Einlesen
                    boolean inDesc = false;
                    String mode = null;
                    DescParamsBuilding rB = null;
                    DescParamsUnit rU = null;
                    int id = 0;
                    int line = 0;
                    while ((zeile = bdescReader.readLine()) != null) {
                        line++;
                        // Zeile interpretieren
                        if (!zeile.isEmpty()) {
                            char first = zeile.charAt(0);
                            if (first == '#') {
                                // Kommentar, ignorieren
                                continue;
                            }

                            // Sind wir grad in der Klammer ?
                            if (inDesc) {
                                // Beim = trennen
                                int indexgleich = zeile.indexOf('='); // Istgleich suchen
                                if (indexgleich != -1) {
                                    String v1 = zeile.substring(0, indexgleich);
                                    String v2 = zeile.substring(indexgleich + 1);
                                    int indexraute = v2.indexOf('#'); //Kommentar am Ende der Zeile?
                                    if (indexraute != -1) {
                                        v2 = v2.substring(0, indexraute - 1);
                                    }
                                    if (mode.equals("B")) {
                                        // Gebäude
                                        if (v1.equals("name")) {
                                            rB.setDescName(v2);
                                        } else if (v1.equals("defaultTexture")) {
                                            rB.getGraphicsData().defaultTexture = v2;
                                        } else if (v1.equals("hitpoints")) {
                                            rB.setHitpoints(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("maxhitpoints")) {
                                            rB.setMaxhitpoints(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("Gdesc")) {
                                            rB.setDescDescription(v2);
                                        } else if (v1.equals("offsetX")) {
                                            rB.getGraphicsData().offsetX = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("offsetY")) {
                                            rB.getGraphicsData().offsetY = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("z1")) {
                                            rB.setZ1(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("z2")) {
                                            rB.setZ2(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("maxIntra")) {
                                            rB.setMaxIntra(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("harvests")) {
                                            rB.setHarvests(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("harvRate")) {
                                            rB.setHarvRate(saveStrtoDouble(v2, zeile, line));
                                        } else if (v1.equals("range")) {
                                            rB.setRange(saveStrtoDouble(v2, zeile, line));
                                        } else if (v1.equals("damage")) {
                                            rB.setDamage(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("cooldownmax")) {
                                            rB.setFireDelay(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("bulletspeed")) {
                                            rB.setBulletspeed(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("bullettexture")) {
                                            rB.setBullettexture(v2);
                                        } else if (v1.equals("atkdelay")) {
                                            rB.setAtkdelay(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("antiair")) {
                                            int[] damageFactors = rB.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_AIR] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antibuilding")) {
                                            int[] damageFactors = rB.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_BUILDING] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antiheavyinf")) {
                                            int[] damageFactors = rB.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_HEAVYINF] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antikav")) {
                                            int[] damageFactors = rB.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_KAV] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antilightinf")) {
                                            int[] damageFactors = rB.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_LIGHTINF] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antitank")) {
                                            int[] damageFactors = rB.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_TANK] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antivehicle")) {
                                            int[] damageFactors = rB.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_VEHICLE] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("accepts")) {
                                            if ("all".equals(v2)) {
                                                rB.setAccepts(Building.ACCEPTS_ALL);
                                            }
                                        } else if (v1.equals("visrange")) {
                                            rB.setVisrange(saveStrtoInt(v2, zeile, line));
                                        }
                                    } else if (mode.equals("U")) {
                                        // Einheiten
                                        if (v1.equals("name")) {
                                            rU.setDescName(v2);
                                        } else if (v1.equals("defaultTexture")) {
                                            rU.getGraphicsData().defaultTexture = v2;
                                        } else if (v1.equals("hitpoints")) {
                                            rU.setHitpoints(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("maxhitpoints")) {
                                            rU.setMaxhitpoints(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("Gdesc")) {
                                            rU.setDescDescription(v2);
                                        } else if (v1.equals("Gpro")) {
                                            rU.setDescPro(v2);
                                        } else if (v1.equals("size")) {
                                            if ("3x3".equals(v2)) {
                                                rU.setSize(3);
                                            }
                                        } else if (v1.equals("Gcon")) {
                                            rU.setDescCon(v2);
                                        } else if (v1.equals("speed")) {
                                            rU.setSpeed(saveStrtoDouble(v2, zeile, line));
                                        } else if (v1.equals("range")) {
                                            rU.setRange(saveStrtoDouble(v2, zeile, line));
                                        } else if (v1.equals("damage")) {
                                            rU.setDamage(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("cooldownmax")) {
                                            rU.setFireDelay(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("bulletspeed")) {
                                            rU.setBulletspeed(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("bullettexture")) {
                                            rU.setBullettexture(v2);
                                        } else if (v1.equals("atkdelay")) {
                                            rU.setAtkdelay(saveStrtoInt(v2, zeile, line));
                                        } else if (v1.equals("armortype")) {
                                            if ("lightinf".equals(v2)) {
                                                rU.setArmorType(GameObject.ARMORTYPE_LIGHTINF);
                                            } else if ("heavyinf".equals(v2)) {
                                                rU.setArmorType(GameObject.ARMORTYPE_HEAVYINF);
                                            } else if ("air".equals(v2)) {
                                                rU.setArmorType(GameObject.ARMORTYPE_AIR);
                                            } else if ("kav".equals(v2)) {
                                                rU.setArmorType(GameObject.ARMORTYPE_KAV);
                                            } else if ("tank".equals(v2)) {
                                                rU.setArmorType(GameObject.ARMORTYPE_TANK);
                                            } else if ("vehicle".equals(v2)) {
                                                rU.setArmorType(GameObject.ARMORTYPE_VEHICLE);
                                            }
                                        } else if (v1.equals("antiair")) {
                                            int[] damageFactors = rU.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_AIR] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antibuilding")) {
                                            int[] damageFactors = rU.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_BUILDING] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antiheavyinf")) {
                                            int[] damageFactors = rU.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_HEAVYINF] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antikav")) {
                                            int[] damageFactors = rU.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_KAV] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antilightinf")) {
                                            int[] damageFactors = rU.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_LIGHTINF] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antitank")) {
                                            int[] damageFactors = rU.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_TANK] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("antivehicle")) {
                                            int[] damageFactors = rU.getDamageFactors();
                                            damageFactors[GameObject.ARMORTYPE_VEHICLE] = saveStrtoInt(v2, zeile, line);
                                        } else if (v1.equals("visrange")) {
                                            rU.setVisrange(saveStrtoInt(v2, zeile, line));
                                        }
                                    }
                                }
                            }

                            if (first == 'U') {
                                // Neue Einheit
                                rU = new DescParamsUnit();
                                inDesc = true;
                                int indexL1 = zeile.indexOf(" ");
                                int indexL2 = zeile.lastIndexOf(" ");
                                String v3 = zeile.substring(indexL1 + 1, indexL2);
                                id = Integer.parseInt(v3);
                                mode = "U";
                            } else if (first == 'B') {
                                // Neues Gebäude
                                rB = new DescParamsBuilding();
                                inDesc = true;
                                int indexL1 = zeile.indexOf(" ");
                                int indexL2 = zeile.lastIndexOf(" ");
                                String v3 = zeile.substring(indexL1 + 1, indexL2);
                                id = Integer.parseInt(v3);
                                mode = "B";
                            } else if (first == '}') {
                                // Fertig, in HashMap speichern
                                if (mode.equals("U")) {
                                    rU.setDescTypeId(id);
                                    descTypeUnit.put(id, rU.getSize() == 3 ? new Unit3x3(rU) : new Unit2x2(rU));
                                    inDesc = false;
                                } else if (mode.equals("B")) {
                                    rB.setDescTypeId(id);
                                    descTypeBuilding.put(id, new PlayersBuilding(rB));
                                    inDesc = false;
                                }
                            }
                        }
                    }
                } catch (FileNotFoundException fnfe) {
                } catch (IOException ioe) {
                } finally {
                    try {
                        descReader.close();
                    } catch (IOException ioe2) {
                    }
                }
            }
            // Leere Map im Speicher anlegen

            AbstractMapElement[][] newMapArray = new AbstractMapElement[newMapX][newMapY];
            for (int x = 0; x < newMapX; x++) {
                for (int y = 0; y < newMapY; y++) {
                    if (x % 2 == y % 2) {
                        if (mapMode == MODE_CLIENT) {
                            newMapArray[x][y] = new ClientMapElement();
                        } else {
                            newMapArray[x][y] = new ServerMapElement();
                        }
                    }
                }
            }

            String newMapName = zipfile.getName();
            // Die Endung des Mapnamens entfernen
            newMapName = newMapName.substring(newMapName.lastIndexOf(File.separator) + 1, newMapName.lastIndexOf("."));
            theMap = new CoRMap(newMapX, newMapY, newMapName, newMapArray);

            ArrayList<Unit> unitList = new ArrayList<Unit>();
            theMap.setMapProperty("UNIT_LIST", unitList);
            ArrayList<Building> buildingList = new ArrayList<Building>();
            theMap.setMapProperty("BUILDING_LIST", buildingList);


            // Jetzt alles einlesen. Als erstes die ground_tex
            for (int x = 0; x < newMapX; x++) {
                for (int y = 0; y < newMapY; y++) {
                    if (x % 2 != y % 2) {
                        continue;
                    }
                    String val = reader.readLine();
                    if (mapMode == MODE_CLIENT && !"".equals(val)) {
                        theMap.getVisMap()[x][y].setGround_tex(val);
                    }
                }
            }
            // fix_tex
            for (int x = 0; x < newMapX; x++) {
                for (int y = 0; y < newMapY; y++) {
                    if (x % 2 != y % 2) {
                        continue;
                    }
                    String val = reader.readLine();
                    if (mapMode == MODE_CLIENT && !"".equals(val)) {
                        theMap.getVisMap()[x][y].setFix_tex(val);
                    }
                }
            }
            // Kollision - nur unreachable
            for (int x = 0; x < newMapX; x++) {
                for (int y = 0; y < newMapY; y++) {
                    if (x % 2 != y % 2) {
                        continue;
                    }
                    String read = reader.readLine();
                    if ("unreachable".equals(read)) {
                        theMap.getVisMap()[x][y].setUnreachable(true);
                    }
                }
            }

            // Units

            for (int i = 1; i <= uS; i++) {
                String[] unitData = reader.readLine().split(" ");
                int desc = Integer.parseInt(unitData[0]);
                String pos = unitData[1];
                int x = Integer.parseInt(pos.substring(0, pos.indexOf("|")));
                int y = Integer.parseInt(pos.substring(pos.indexOf("|") + 1, pos.length()));
                int playerId = Integer.parseInt(unitData[2]);
                int netId = Integer.parseInt(unitData[3]);
                Unit unit = null;
                unit = (Unit) descTypeUnit.get(desc).getCopy(netId);
                unit.setPlayerId(playerId);
                unit.setMainPosition(new Position(x, y));
                unitList.add(unit);
            }

            // Gebäude

            for (int i = 1; i <= bS; i++) {
                String[] unitData = reader.readLine().split(" ");
                int desc = Integer.parseInt(unitData[0]);
                String pos = unitData[1];
                int x = Integer.parseInt(pos.substring(0, pos.indexOf("|")));
                int y = Integer.parseInt(pos.substring(pos.indexOf("|") + 1, pos.length()));
                int playerId = Integer.parseInt(unitData[2]);
                int netId = Integer.parseInt(unitData[3]);
                String s = null;
                try {
                    s = unitData[4];
                } catch (ArrayIndexOutOfBoundsException ex) {
                    // Ok, das ist nur aus Kompatibilitätsgründen da
                }
                if (s == null || s.equals("p")) {
                    // Players Building
                    Building b = null;
                    b = (Building) descTypeBuilding.get(desc).getCopy(netId);
                    b.setPlayerId(playerId);
                    b.setMainPosition(new Position(x, y));
                    buildingList.add(b);
                } else if (s.equals("n")) {
                    // Neutral Building!
                    NeutralBuilding nb = new NeutralBuilding(netId, new Position(x, y));
                    nb.setMainPosition(new Position(x, y));
                    buildingList.add(nb);
                    
                }
            }

            // Gebäude

            for (int i = 1; i <= rS; i++) {
                // Res-List ignorieren, nur aus Kompatibiltätsgründen da
            }

            // Grenzen der Map mit Kollision ausstatten
            for (int x = 0; x < newMapX; x++) {
                for (int y = 0; y < newMapY; y++) {
                    if (x % 2 != y % 2) {
                        continue;
                    }
                    if (x == 0 || x == (newMapX - 1) || y == 0 || y == (newMapY - 1)) {
                        // Feld hat Kollision
                        theMap.getVisMap()[x][y].setUnreachable(true);
                    }
                }
            }

            theMap.setMapProperty("NEXTNETID", nnid);
        } catch (Exception ex2) {
            // Auch das hat versagt, Exception werfen
            System.out.println("Cannot load map!");
            throw new RuntimeException("Unable to load map!", ex2);
        }

        return theMap;
    }

    /**
     * Speichert die Map unter dem Angegebenen Dateinahmen ab. Überschreibt ohne Nachfrage.
     * Die übergebene CoRMap müss vollständig sein, es müssen also alle notwendigen Parameter gesetzt sein,
     * die Listen müssen da sein, und es muss ein gültiger Header (CoRMapMetaInf) zur Verfügung gestellt werden.
     * Vorschaubilder werden automatisch erstellt
     * Die Map MUSS im Client/Editormode vorliegen!!
     * @param map
     */
    public static void saveMap(CoRMap map, String name) {
        MapIO.saveMap(map, name, calcPreview(map));
    }

    /**
     * Speichert die Map unter dem Angegebenen Dateinahmen ab. Überschreibt ohne Nachfrage.
     * Die übergebene CoR-Map muss vollständig sein.
     * Das übergebene Preview-Bild wird verwendet.
     * Die Map MUSS im Client/Editormode vorliegen.
     * @param map
     */
    public static void saveMap(CoRMap map, String name, java.awt.Image preview) {
        // Datei anlegen
        File newMapSaver = new File("map/" + name + ".map");
        try {
            // Map serialisieren und speichern
            ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(newMapSaver)));
            // META - Header
            ZipEntry meta = new ZipEntry("META");
            zipOut.putNextEntry(meta);
            ObjectOutputStream objOut = new ObjectOutputStream(zipOut);
            objOut.writeObject((CoRMapMetaInf) map.getMapPoperty("META"));
            zipOut.closeEntry();
            // PREV - Vorschaubild
            ZipEntry prev = new ZipEntry("PREV");
            zipOut.putNextEntry(prev);
            ImageIO.write((RenderedImage) preview, "jpg", zipOut);
            zipOut.closeEntry();
            // RMAP
            ZipEntry rmap = new ZipEntry("RMAP");
            zipOut.putNextEntry(rmap);
            try {
                BufferedWriter writer = new BufferedWriter(new java.io.OutputStreamWriter(zipOut));
                ArrayList<Unit> unitList = (ArrayList<Unit>) map.getMapPoperty("UNIT_LIST");
                ArrayList<Building> buildingList = (ArrayList<Building>) map.getMapPoperty("BUILDING_LIST");
                int nextNetID = Integer.parseInt(map.getMapPoperty("NEXTNETID").toString());
                // Zuerst ints nach folgender Systax: X Y unitList.size() buildingList.size() resList.size() nextNetID
                writer.write(map.getMapSizeX() + " " + map.getMapSizeY() + " " + unitList.size() + " " + buildingList.size() + " 0 " + nextNetID);
                writer.newLine();
                // Jetzt die Bodentexturen
                for (int x = 0; x < map.getMapSizeX(); x++) {
                    for (int y = 0; y < map.getMapSizeY(); y++) {
                        if (x % 2 != y % 2) {
                            continue;
                        }
                        String val = map.getVisMap()[x][y].getGround_tex();
                        if (val != null) {
                            writer.write(val);
                        }
                        writer.newLine();
                    }
                }
                // Fix
                // Jetzt die Bodentexturen
                for (int x = 0; x < map.getMapSizeX(); x++) {
                    for (int y = 0; y < map.getMapSizeY(); y++) {
                        if (x % 2 != y % 2) {
                            continue;
                        }
                        String val = map.getVisMap()[x][y].getFix_tex();
                        if (val != null) {
                            writer.write(val);
                        }
                        writer.newLine();
                    }
                }
                // Kollision
                for (int x = 0; x < map.getMapSizeX(); x++) {
                    for (int y = 0; y < map.getMapSizeY(); y++) {
                        if (x % 2 != y % 2) {
                            continue;
                        }
                        if (map.getVisMap()[x][y].isUnreachable()) {
                            writer.write("unreachable");
                        }
                        writer.newLine();
                    }
                }
                // Einheiten
                for (Unit unit : unitList) {
                    writer.write(unit.getDescTypeId() + " " + unit.getMainPosition() + " " + unit.getPlayerId() + " " + unit.netID);
                    writer.newLine();
                }
                // Gebäude
                for (Building building : buildingList) {
                    if (!(building instanceof NeutralBuilding)) {
                        writer.write(building.getDescTypeId() + " " + building.getMainPosition() + " " + building.getPlayerId() + " " + building.netID + " p");
                    } else {
                        writer.write("1 " + building.getMainPosition() + " 0 " + building.netID + " n");
                    }
                    writer.newLine();
                }
                // Fertig
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Saving failed!");
                return;
            }
            zipOut.closeEntry();
            zipOut.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Saving failed!");
            return;
        }
    }

    /**
     * Berechnet ein Vorschaubild.
     * Die Map muss gültig sein, sonst gibts ne Exception
     * @param map
     * @return
     */
    public static java.awt.Image calcPreview(CoRMap map) {
        // Alle verfügbaren Bilder suchen:
        HashMap<String, BufferedImage> imgMap = new HashMap<String, BufferedImage>();
        File[] list = new File("img/ground").listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".png") || name.endsWith(".PNG");
            }
        });
        try {
            for (File image : list) {
                if (image.exists() && image.isFile()) {
                    BufferedImage img = ImageIO.read(image);
                    imgMap.put(image.getPath().replace('/', '\\'), img);
                    imgMap.put(image.getPath().replace('\\', '/'), img);
                }
            }
            Image img = new BufferedImage(map.getMapSizeX() * 2, map.getMapSizeY() * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g3 = (Graphics2D) img.getGraphics();
            for (int x = 0; x < map.getMapSizeX(); x++) {
                for (int y = 0; y < map.getMapSizeY(); y++) {
                    if ((x + y) % 2 == 1) {
                        continue;
                    } else {
                        try {
                            BufferedImage field = imgMap.get(map.getVisMap()[x][y].getGround_tex());
                            int col = field.getRGB(15, 15);
                            g3.setColor(new Color(col));
                            g3.fillRect(x * 2, y * 2, 4, 4);
                        } catch (Exception ex) {
                        }
                    }
                }
            }
            return img;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Läd ein Vorschaubild aus einer Datei
     * @param path der Pfad zur Map
     * @return Das Vorschaubild, eventuell null, wenns keins gibt
     */
    public static Image loadPreview(String path) {
        try {
            ZipFile zipfile = new ZipFile(path);
            ZipEntry prev = zipfile.getEntry("PREV");
            Image image = ImageIO.read(zipfile.getInputStream(prev));
            zipfile.close();
            return image;
        } catch (IOException ex) {
        }
        return null;
    }

    private static int saveStrtoInt(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        int i;
        try {
            i = Integer.parseInt(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            return 1;
        }
    }

    private static double saveStrtoDouble(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        double i;
        try {
            i = Double.parseDouble(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            return 1;
        }
    }
}
