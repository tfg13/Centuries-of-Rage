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
import java.io.BufferedInputStream;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;

/**
 * Stellt Methoden zum Lesen und Schreiben von CoRMaps zur Verfügung
 */
public class MapIO {

    private MapIO() {
    }

    /**
     * Öffnet die angegebene Mapdatei und ließt die Map vollständig aus.
     * Dabei wird zunächst versucht, die Map mithilfe der Daten im MAP-Bereich zu deserialisieren.
     * Sollte dies Fehlschlagen, wird die Map aus den Daten im RMAP-Bereich rekonstruiert.
     * @return Die geladene CoRMap - Einsatzbereit
     */
    public static CoRMap readMap(String path) {
        // Jetzt Map öffnen
        CoRMap theMap = null;
        ObjectInputStream objIn = null;
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(path);
            ZipEntry map = zipfile.getEntry("MAP");
            objIn = new ObjectInputStream(new BufferedInputStream(zipfile.getInputStream(map)));
            theMap = (CoRMap) objIn.readObject();
        } catch (Exception ex) {
            // MAP-Bereich konnte (warum auch immer) nicht gelesen werden. Versuche Map aus RMAP-Datenbereich zu rekonstruieren
            BufferedReader reader = null;
            ZipEntry rmap = zipfile.getEntry("RMAP");
            try {
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

                File OrdnerSuchen = new File("game/"); //Unterordner in "game" suchen
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
                    for (File file : files) {
                        flist.add(file);
                    }
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
                        Building rB = null;
                        Unit rU = null;
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
                                                rB.name = v2;
                                            } else if (v1.equals("defaultTexture")) {
                                                rB.defaultTexture = v2;
                                            } else if (v1.equals("hitpoints")) {
                                                rB.setHitpoints(saveStrtoInt(v2, zeile, line));
                                            } else if (v1.equals("maxhitpoints")) {
                                                rB.maxhitpoints = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("Gdesc")) {
                                                rB.Gdesc = v2;
                                            } else if (v1.equals("Gimg")) {
                                                rB.Gimg = v2;
                                            } else if (v1.equals("offsetX")) {
                                                rB.offsetX = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("offsetY")) {
                                                rB.offsetY = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("z1")) {
                                                rB.z1 = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("z2")) {
                                                rB.z2 = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("maxIntra")) {
                                                rB.maxIntra = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("harvests")) {
                                                rB.harvests = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("harvRate")) {
                                                rB.harvRate = saveStrtoDouble(v2, zeile, line);
                                            } else if (v1.equals("damage")) {
                                                rB.damage = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("cooldownmax")) {
                                                rB.cooldownmax = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("range")) {
                                                rB.range = saveStrtoDouble(v2, zeile, line);
                                            } else if (v1.equals("bulletspeed")) {
                                                rB.bulletspeed = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("atkdelay")) {
                                                rB.atkdelay = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antiair")) {
                                                rB.antiair = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antibuilding")) {
                                                rB.antibuilding = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antiheavyinf")) {
                                                rB.antiheavyinf = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antikav")) {
                                                rB.antikav = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antilightinf")) {
                                                rB.antilightinf = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antitank")) {
                                                rB.antitank = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antivehicle")) {
                                                rB.antivehicle = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("accepts")) {
                                                if ("all".equals(v2)) {
                                                    rB.accepts = Building.ACCEPTS_ALL;
                                                }
                                            }/* else if (v1.equals("ability")) {
                                            RogGameObjectAbility ra = clientDescAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
                                            if (ra != null) {
                                            rB.abilitys.add(ra);
                                            }
                                            }*/
                                        } else if (mode.equals("U")) {
                                            // Einheiten
                                            if (v1.equals("name")) {
                                                rU.name = v2;
                                            } else if (v1.equals("defaultTexture")) {
                                                rU.graphicsdata.defaultTexture = v2;
                                            } else if (v1.equals("hitpoints")) {
                                                rU.setHitpoints(saveStrtoInt(v2, zeile, line));
                                            } else if (v1.equals("maxhitpoints")) {
                                                rU.maxhitpoints = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("Gdesc")) {
                                                rU.Gdesc = v2;
                                            } else if (v1.equals("Gimg")) {
                                                rU.Gimg = v2;
                                            } else if (v1.equals("Gpro")) {
                                                rU.Gpro = v2;
                                            } else if (v1.equals("Gcon")) {
                                                rU.Gcon = v2;
                                            } else if (v1.equals("speed")) {
                                                rU.speed = saveStrtoDouble(v2, zeile, line);
                                            } else if (v1.equals("range")) {
                                                rU.range = saveStrtoDouble(v2, zeile, line);
                                            } else if (v1.equals("damage")) {
                                                rU.damage = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("cooldownmax")) {
                                                rU.cooldownmax = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("bulletspeed")) {
                                                rU.bulletspeed = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("atkdelay")) {
                                                rU.atkdelay = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("armortype")) {
                                                rU.armortype = v2;
                                            } else if (v1.equals("antiheavyinf")) {
                                                rU.antiheavyinf = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antilightinf")) {
                                                rU.antilightinf = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antikav")) {
                                                rU.antikav = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antivehicle")) {
                                                rU.antivehicle = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antitank")) {
                                                rU.antitank = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antiair")) {
                                                rU.antiair = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("antibuilding")) {
                                                rU.antibuilding = saveStrtoInt(v2, zeile, line);
                                            } else if (v1.equals("idlerange")) {
                                                rU.idlerange = saveStrtoDouble(v2, zeile, line);
                                            } /*else if (v1.equals("ability")) {
                                            RogGameObjectAbility ra = clientDescAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
                                            if (ra != null) {
                                            rU.abilitys.add(ra);
                                            }
                                            } */ else if ("harvester".equals(v1)) {
                                                // Einheit kann ernten:
                                                rU.canHarvest = true;
                                            }

                                        }
                                    }
                                }

                                if (first == 'U') {
                                    // Neue Einheit
                                    rU = new Unit(0, 0, -1);
                                    inDesc = true;
                                    int indexL1 = zeile.indexOf(" ");
                                    int indexL2 = zeile.lastIndexOf(" ");
                                    String v3 = zeile.substring(indexL1 + 1, indexL2);
                                    id = Integer.parseInt(v3);
                                    mode = "U";
                                } else if (first == 'B') {
                                    // Neues Gebäude
                                    rB = new Building(0, 0, -1);
                                    inDesc = true;
                                    int indexL1 = zeile.indexOf(" ");
                                    int indexL2 = zeile.lastIndexOf(" ");
                                    String v3 = zeile.substring(indexL1 + 1, indexL2);
                                    id = Integer.parseInt(v3);
                                    mode = "B";
                                } else if (first == '}') {
                                    // Fertig, in HashMap speichern
                                    if (mode.equals("U")) {
                                        rU.descTypeId = id;
                                        descTypeUnit.put(id, rU);
                                        inDesc = false;
                                    } else if (mode.equals("B")) {
                                        rB.descTypeId = id;
                                        descTypeBuilding.put(id, rB);
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

                CoRMapElement[][] newMapArray = new CoRMapElement[newMapX][newMapY];
                for (int x = 0; x < newMapX; x++) {
                    for (int y = 0; y < newMapY; y++) {
                        if (x % 2 == y % 2) {
                            newMapArray[x][y] = new CoRMapElement();
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
                        if (!"".equals(val)) {
                            theMap.changeElementProperty(x, y, "ground_tex", val);
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
                        if (!"".equals(val)) {
                            theMap.changeElementProperty(x, y, "fix_tex", val);
                        }
                    }
                }
                // Kollision
                for (int x = 0; x < newMapX; x++) {
                    for (int y = 0; y < newMapY; y++) {
                        if (x % 2 != y % 2) {
                            continue;
                        }
                        String read = reader.readLine();
                        if ("free".equals(read)) {
                            theMap.visMap[x][y].collision = collision.free;
                        } else if ("blocked".equals(read)) {
                            theMap.visMap[x][y].collision = collision.blocked;
                        } else if ("occupied".equals(read)) {
                            theMap.visMap[x][y].collision = collision.occupied;
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
                    Building b = null;
                        b = (Building) descTypeBuilding.get(desc).getCopy(netId);
                    b.setPlayerId(playerId);
                    b.setMainPosition(new Position(x, y));
                    buildingList.add(b);
                }

                // Gebäude

                for (int i = 1; i <= rS; i++) {
                    // Res-List ignorieren, aus Kompatibiltätsgründen
                }

                // Grenzen der Map mit Kollision und isborder ausstatten
                for (int x = 0; x < newMapX; x++) {
                    for (int y = 0; y < newMapY; y++) {
                        if (x % 2 != y % 2) {
                            continue;
                        }
                        if (x == 0 || x == (newMapX - 1) || y == 0 || y == (newMapY - 1)) {
                            // Feld hat Kollision
                            theMap.changeElementProperty(x, y, "is_border", "true");
                            theMap.visMap[x][y].setCollision(collision.blocked);
                        }
                    }
                }

                theMap.setMapProperty("NEXTNETID", nnid);
            } catch (Exception ex2) {
                // Auch das hat versagt, Exception werfen
                System.out.println("Cannot load map!");
                throw new RuntimeException("Unable to load map!");
            }
        }
        return theMap;
    }

    /**
     * Speichert die Map unter dem Angegebenen Dateinahmen ab. Überschreibt ohne Nachfrage.
     * Die übergebene CoRMap müss vollständig sein, es müssen also alle notwendigen Parameter gesetzt sein,
     * die Listen müssen da sein, und es muss ein gültiger Header (CoRMapMetaInf) zur Verfügung gestellt werden.
     * Vorschaubilder werden automatisch erstellt
     * @param map
     */
    public static void saveMap(CoRMap map, String name) {
        MapIO.saveMap(map, name, calcPreview(map));
    }

    /**
     * Speichert die Map unter dem Angegebenen Dateinahmen ab. Überschreibt ohne Nachfrage.
     * Die übergebene CoR-Map muss vollständig sein.
     * Das übergebene Preview-Bild wird verwendet.
     * @param map
     */
    public static void saveMap(CoRMap map, String name, java.awt.Image preview) {
        // Datei anlegen
        File newMapSaver = new File("map/" + name + ".map");
        try {
            // Map serialisieren und speichern
            ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(newMapSaver)));
            // MAP selber
            ZipEntry entry = new ZipEntry("MAP");
            zipOut.putNextEntry(entry);
            ObjectOutputStream objOut = new ObjectOutputStream(zipOut);
            objOut.writeObject(map);
            zipOut.closeEntry();
            // META - Header
            ZipEntry meta = new ZipEntry("META");
            zipOut.putNextEntry(meta);
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
                        String val = map.getElementProperty(x, y, "ground_tex");
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
                        String val = map.getElementProperty(x, y, "fix_tex");
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
                        thirteenducks.cor.map.CoRMapElement.collision col = map.visMap[x][y].getCollision();
                        if (col != null) {
                            writer.write(col.toString());
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
                    writer.write(building.getDescTypeId() + " " + building.getMainPosition() + " " + building.getPlayerId() + " " + building.netID);
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
                            BufferedImage field = imgMap.get(map.getElementProperty(x, y, "ground_tex"));
                            int col = field.getRGB(30, 30);
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
