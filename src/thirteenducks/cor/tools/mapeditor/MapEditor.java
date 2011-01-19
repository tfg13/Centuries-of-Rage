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
package thirteenducks.cor.tools.mapeditor;

import thirteenducks.cor.map.CoRMap;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import thirteenducks.cor.graphics.BuildingAnimator;
import thirteenducks.cor.graphics.GraphicsComponent;
import thirteenducks.cor.graphics.UnitAnimator;
import thirteenducks.cor.graphics.CoRImage;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.map.MapIO;

public class MapEditor {

    String versionNumber = "0.2.2";
    // Eigene Vars
    HashMap imgInput;
    int imgInputNumber = 0;
    ArrayList<CoRImage> groundImages;
    ArrayList<CoRImage> fixImages;
    ArrayList<CoRImage> creepImages;
    ArrayList<CoRImage> otherImages;
    ArrayList<CoRImage> irregularimages;
    ArrayList<CoRImage> colImages;
    ArrayList<CoRImage> buildingImages;
    HashMap<String, CoRImage> imgMap;
    // Liste für irregularimages:
    /*
     * 0 - defaultbackground
     * 1 - colmode
     * 2 - texnotfound
     *
     * */
    MapEditorGui rmegui;
    MapEditorNewMapDialog rmenmd;
    MapEditorAbout rmeab;
    CoRMap theMap;
    boolean colisionMode = false; // Modus für Kolisionsänderung
    ArrayList<Unit> unitList;
    ArrayList<Building> buildingList;
    ArrayList<Ressource> resList;
    boolean firstMapRun = false;
    boolean newMode = true; // Units in unitList und nicht als Textur eines Feldes
    MapEditorCursor paintCursor;
    int editorMode = 0;
    Unit currentUnit = null;
    Building currentBuilding = null;
    public HashMap<Integer, Unit> descUnit;
    public HashMap<Integer, Building> descBuilding;
    // Netzwerk - Patches
    int nextNetID = 1;

    public MapEditor() {
        // Los gehts!
        // Bilder einlesen:
        System.out.println("[RME]: Starting up...");
        System.out.println("[RME]: Loading images...");
        // Bilder aus dem /img ordner laden
        // Haupt-Zuordnungsdatei öffnen (types.list)
        File imgtypes = new File("img/types.list");
        imgInput = new HashMap();
        File f3 = null;
        try {
            FileReader imgtypereader = new FileReader(imgtypes);
            BufferedReader itr = new BufferedReader(imgtypereader);
            String input;
            while ((input = itr.readLine()) != null) {
                // Zeile für Zeile lesen
                // Verifizieren, dass Datei da ist:
                input = "img/" + input;
                // System.out.println("IN: " + input);
                File f = new File(input);
                if (f.isDirectory()) {
                    // Ok, ist da hinzufügen
                    imgInput.put(imgInputNumber, input);
                    imgInputNumber++;
                    // System.out.println("Einlesen1: " + input);
                } else {
                    // Datei nicht da, ignorieren, könnte ein Kommentar sein
                }
            }
            imgtypereader.close();
            // Grundliste ist da, jetzt Typlisten einlesen
            groundImages = new ArrayList();
            fixImages = new ArrayList();
            creepImages = new ArrayList();
            otherImages = new ArrayList();
            colImages = new ArrayList();
            buildingImages = new ArrayList();
            descUnit = new HashMap<Integer, Unit>();
            descBuilding = new HashMap<Integer, Building>();
            imgMap = new HashMap();
            // Einheitentypen einlesen
            initDescTypes();
            final ArrayList<String> blackList = new ArrayList<String>();
            // Blacklist einlesen
            itr = new BufferedReader(new FileReader(new File("img/blacklist")));
            String inputv = null;
            while ((inputv = itr.readLine()) != null) {
                if (!inputv.startsWith("#")) {
                    blackList.add(inputv);
                }
            }
            // Anzahl der Bilder berechnen:
            int total = 0;
            ArrayList<File[]> folders = new ArrayList<File[]>();
            for (int i = 0; i < imgInputNumber; i++) {
                String temp = imgInput.get(i).toString();
                File folder = new File(temp);
                if (folder.isDirectory()) {
                    File[] images = folder.listFiles(new FileFilter() {

                        public boolean accept(File pathname) {
                            if (pathname.getName().endsWith(".png")) {
                                return !blackList.contains(pathname.getPath());
                            }
                            return false;
                        }
                    });
                    folders.add(images);
                    total += images.length;
                }
            }
            // Einige Bilder vorzeitig einlesen
            CoRImage tempImage2;
            tempImage2 = new CoRImage(ImageIO.read(new File("img/notinlist/notex.png")));
            tempImage2.setImageName("noTex");
            fixImages.add(tempImage2);
            otherImages.add(tempImage2);
            imgMap.put(tempImage2.getImageName(), tempImage2);
            tempImage2 = new CoRImage(ImageIO.read(new File("img/notinlist/del.png")));
            tempImage2.setImageName("del");
            imgMap.put(tempImage2.getImageName(), tempImage2);
            creepImages.add(tempImage2);
            // Tatsächlich einlesen
            for (File[] list : folders) {
                for (File image : list) {
                    if (image.exists() && image.isFile()) { // Datei vorhanden?
                        // Ok, hinzufügen
                        // Als BufferedImage reinladen, dann wird es direkt in den Ram geladen
                        CoRImage tempImage = new CoRImage(ImageIO.read(image));
                        tempImage.setImageName(image.getPath());
                        String temp = image.getPath();
                        // Einordnen:
                        if (temp.contains("ground")) {
                            groundImages.add(tempImage);
                        } else if (temp.contains("fix")) {
                            fixImages.add(tempImage);
                        } else if (temp.contains("creeps")) {
                            // Ausgeschaltet, weil auf DESC umgestellt
                            //creepImages.add(tempImage);
                        } else if (temp.contains("res")) {
                            // Ausgeschaltet, hier sind jetzt die Ressourcen
                            otherImages.add(tempImage);
                        }
                        // Auf jeden Fall für das Grafikmodul behalten
                        // Slashs tauschen für Win-Linux-Kompatibilität
                        imgMap.put(image.getPath(), tempImage); // Dazu machen
                        if (tempImage.getImageName().contains("/")) {
                            tempImage = new CoRImage(ImageIO.read(image));
                            tempImage.setImageName(image.getPath().replace('/', '\\'));
                            imgMap.put(tempImage.getImageName(), tempImage);
                        } else {
                            tempImage = new CoRImage(ImageIO.read(image));
                            tempImage.setImageName(image.getPath().replace('\\', '/'));
                            imgMap.put(tempImage.getImageName(), tempImage);
                        }
                        //   imgMap.put(f3.getPath().replace('/', '\\'), tempImage);
                        // imgMap.put(f3.getPath().replace('\\', '/'), tempImage);
                        System.out.println("[LoadImage]: " + tempImage.getImageName());

                    }
                }
            }

        } catch (FileNotFoundException ex) {
            // Datei nicht gefunden!
            System.out.println("File not found, Reason:");
            ex.printStackTrace();
        } catch (IOException ex) {
            // Sollte nicht auftreten...
            System.out.println("I/O Error, Reason:");
            ex.printStackTrace();
            System.out.println(f3);
        }
        // Jetzt noch einige Bilder nicht-automatisch einlesen
        irregularimages = new ArrayList<CoRImage>();
        try {
            irregularimages.add(new CoRImage(ImageIO.read(new File("img/notinlist/editor/defaultstart.png"))));
            irregularimages.add(new CoRImage(ImageIO.read(new File("img/notinlist/editor/colmode.png"))));
            irregularimages.add(new CoRImage(ImageIO.read(new File("img/notinlist/editor/tex_not_found.png"))));
            CoRImage tempImage;
            tempImage = new CoRImage(ImageIO.read(new File("img/notinlist/editor/select_frame.png")));
            tempImage.setImageName("cur1");
            imgMap.put("cur1", tempImage);
            tempImage = new CoRImage(ImageIO.read(new File("img/notinlist/misc/col.png")));
            tempImage.setImageName("col");
            colImages.add(tempImage);
            tempImage = new CoRImage(ImageIO.read(new File("img/notinlist/misc/nocol.png")));
            tempImage.setImageName("nocol");
            colImages.add(tempImage);
        } catch (IOException ex) {
            // Bild nicht gefunden....
            System.out.println("[RME][ERROR]: Default-Splash (img/notinlist/editor/defaultstart.png) not found!");
            ex.printStackTrace();
        }
        // Grafiken laden abgeschlossen
        System.out.println("[RME]: Done loading images.");

        // Gui starten
        System.out.println("[RME]: Building gui...");

        rmegui = new MapEditorGui();
        // Modi-Einstellungen auf Bilder umstellen
        EditorModeSelector modeRenderer = new EditorModeSelector();
        modeRenderer.setPreferredSize(new Dimension(25, 50));
        rmegui.jComboBox2.setRenderer(modeRenderer);

        // Listener für den Input aktivieren
        initListeners();
        // Grafikpaletten anzeigen
        rmegui.groundPanel.setList(groundImages);
        rmegui.fixPanel.setList(fixImages);
        //rmegui.creepPanel.setList(creepImages); // Aus wegen DESC
        rmegui.miscPanel.setList(otherImages);
        // Startsplash anzeigen
        rmegui.content.setPicture(irregularimages.get(0));
        rmegui.content.setColModeImage(irregularimages.get(1));
        // BilderListe übernehmen
        rmegui.content.setImageMap(imgMap);
        //rmegui.content.calcColoredMaps();
        // Andere Grafikpaletten updaten
        rmegui.buildPanel.enableDescMode((byte) 'b', (HashMap) descBuilding);
        updateImageList((byte) 'b', (HashMap) descBuilding);
        rmegui.buildPanel.setList(buildingImages);
        rmegui.creepPanel.enableDescMode((byte) 'u', (HashMap) descUnit);
        updateImageList((byte) 'u', (HashMap) descUnit);
        rmegui.creepPanel.setList(creepImages);
        // Colmode vorbereiten
        rmegui.content.calcGreyMap();
        // Version übernehmen
        displayStatus("RogMapEditor " + versionNumber);
        // Cursor initieren
        paintCursor = new MapEditorCursor(MapEditorCursor.TYPE_STRAIGHT, MapEditorCursor.FILL_NORMAL, 1, 1);
        rmegui.content.changePaintCursor(paintCursor);
        // Gui sichtbar machen
        rmegui.setLocationRelativeTo(null);
        rmegui.setVisible(true);
        // Gui fertig aufgebaut
        System.out.println("[RME]: Gui-init done");
        //Fertig, User machen lassen
        System.out.println("[RME]: Init done.");

    }

    private void readAnimations() {
        /*   // Animationsdaten einlesen
        System.out.println("[RogGraphics][LoadAnim]: Start reading animations...");
        File f = new File("img/anim");
        if (f.exists() && f.isDirectory()) {
        // Ok, der Ordner ist schonmal da...
        // Hier trennt es sich nach Units U und Buildings B
        // TODO: Implement animations for buildings
        File u = new File("img/anim/U");
        if (u.exists() && u.isDirectory()) {
        // Gut, der Ordner ist da, jetzt die Inhalte (Ordner) einlesen
        File[] units = u.listFiles();
        for (File unit : units) {
        if (!unit.isDirectory() || unit.getName().equals(".svn")) { // Gehört nicht dazu
        continue;
        }
        // Ist es eine Nummer?
        int desc;
        try {
        desc = Integer.parseInt(unit.getName());
        // Ist das eine gültige DESC?
        if (isValidUnitDesc(desc)) {
        // Ja, einlesen und eintragen
        RogGraphicsUnitAnimator amanager = new RogGraphicsUnitAnimator();
        // Inhalt genauer bestimmen:
        ArrayList<File> contents = new ArrayList<File>(Arrays.asList(unit.listFiles()));
        // Texturen für Idle?
        File idleFolder = new File(unit.getPath() + "/idle");
        if (contents.contains(idleFolder)) {
        // Ja! - Einlesen
        ArrayList<BufferedImage> imglist = new ArrayList<BufferedImage>();
        int count = 0;
        while (true) {
        try {
        imglist.add(ImageIO.read(new File(idleFolder.getPath() + "/" + count + ".png")));
        count++;
        } catch (java.io.IOException ex) {
        // Bild gibts nicht, dann wars das, abbrechen
        break;
        }
        }
        int animframerate = readFrameRate(new File(idleFolder.getPath() + "/anim.properties"));
        amanager.addIdle(animframerate, imglist.toArray(new BufferedImage[imglist.size()]));
        System.out.println("[RogGraphics][LoadAnim]: U-DESC:" + desc + "-IDLE Animation mit " + count + " Bildern geladen.");
        }
        // Texturen für Bewegung?
        File movingFolder = new File(unit.getPath() + "/moving");
        if (contents.contains(movingFolder)) {
        // Ja! - Einlesen
        ArrayList<BufferedImage> imglist = new ArrayList<BufferedImage>();
        int count = 0;
        while (true) {
        try {
        imglist.add(ImageIO.read(new File(movingFolder.getPath() + "/" + count + ".png")));
        count++;
        } catch (java.io.IOException ex) {
        // Bild gibts nicht, dann wars das, abbrechen
        break;
        }
        }
        int animframerate = readFrameRate(new File(movingFolder.getPath() + "/anim.properties"));
        amanager.addMoving(animframerate, imglist.toArray(new BufferedImage[imglist.size()]));
        System.out.println("[RogGraphics][LoadAnim]: U-DESC:" + desc + "-MOVING Animation mit " + count + " Bildern geladen.");
        }
        // Texturen für Angriff?
        File attackingFolder = new File(unit.getPath() + "/attacking");
        if (contents.contains(attackingFolder)) {
        // Ja! - Einlesen
        ArrayList<BufferedImage> imglist = new ArrayList<BufferedImage>();
        int count = 0;
        while (true) {
        try {
        imglist.add(ImageIO.read(new File(attackingFolder.getPath() + "/" + count + ".png")));
        count++;
        } catch (java.io.IOException ex) {
        // Bild gibts nicht, dann wars das, abbrechen
        break;
        }
        }
        int animframerate = readFrameRate(new File(attackingFolder.getPath() + "/anim.properties"));
        amanager.addAttacking(animframerate, imglist.toArray(new BufferedImage[imglist.size()]));
        System.out.println("[RogGraphics][LoadAnim]: U-DESC:" + desc + "-ATTACKING Animation mit " + count + " Bildern geladen.");
        }
        // Texturen fürs Sterben?
        File dieingFolder = new File(unit.getPath() + "/dieing");
        if (contents.contains(dieingFolder)) {
        // Ja! - Einlesen
        ArrayList<BufferedImage> imglist = new ArrayList<BufferedImage>();
        int count = 0;
        while (true) {
        try {
        imglist.add(ImageIO.read(new File(dieingFolder.getPath() + "/" + count + ".png")));
        count++;
        } catch (java.io.IOException ex) {
        // Bild gibts nicht, dann wars das, abbrechen
        break;
        }
        }
        int animframerate = readFrameRate(new File(dieingFolder.getPath() + "/anim.properties"));
        amanager.addDieing(animframerate, imglist.toArray(new BufferedImage[imglist.size()]));
        System.out.println("[RogGraphics][LoadAnim]: U-DESC:" + desc + "-DIEING Animation mit " + count + " Bildern geladen.");
        }
        // Fertig mit einlesen - speichern
        insertUnitAnimator(desc, amanager);
        }
        // Nein
        continue;
        } catch (NumberFormatException ex) {
        // Irgendwie keine Zahl
        continue;
        }
        }
        }
        }
        System.out.println("[RogGraphics][LoadAnim]: Finished reading animations."); */
    }

    public void initListeners() {
        // Fügt die Listener der Gui hinzu
        rmegui.jComboBox1.addActionListener(new ActionListener() { // Der Layer-Umschalter

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Anderer Layer wurde selektiert
                // Auslagern
                outsourceJob(new Runnable() {

                    @Override
                    public void run() {
                        rmegui.jTabbedPane1.setSelectedIndex(rmegui.jComboBox1.getSelectedIndex());
                        if (rmegui.jComboBox1.getSelectedIndex() == 0) {
                            // Bodentextur
                            rmegui.jSpinner1.setEnabled(true);
                            rmegui.jSpinner2.setEnabled(true);
                            rmegui.jComboBox3.setEnabled(true);
                            rmegui.content.renderPicCursor = false;
                        } else if (rmegui.jComboBox1.getSelectedIndex() == 1) {
                            // Fixe Objekte
                            rmegui.jSpinner1.setEnabled(true);
                            rmegui.jSpinner2.setEnabled(true);
                            rmegui.jComboBox3.setEnabled(true);
                            rmegui.content.renderPicCursor = false;
                        } else if (rmegui.jComboBox1.getSelectedIndex() == 2) {
                            // Einheiten
                            rmegui.jSpinner1.setValue(1);
                            rmegui.jSpinner2.setValue(1);
                            rmegui.jSpinner1.setEnabled(false);
                            rmegui.jSpinner2.setEnabled(false);
                            rmegui.jComboBox3.setSelectedIndex(0);
                            rmegui.jComboBox3.setEnabled(false);
                            // Cursor-Bild rendern
                            rmegui.content.renderPic = new CoRImage(GraphicsComponent.grayScale(rmegui.creepPanel.getSelectedImage().getImage()));
                            rmegui.content.renderPicCursor = true;
                        } else if (rmegui.jComboBox1.getSelectedIndex() == 3) {
                            // Misc
                            rmegui.jSpinner1.setValue(1);
                            rmegui.jSpinner2.setValue(1);
                            rmegui.jSpinner1.setEnabled(false);
                            rmegui.jSpinner2.setEnabled(false);
                            rmegui.jComboBox3.setSelectedIndex(0);
                            rmegui.jComboBox3.setEnabled(false);
                            rmegui.content.renderPicCursor = false;
                        } else if (rmegui.jComboBox1.getSelectedIndex() == 4) {
                            // Gebäude
                            rmegui.jSpinner1.setValue(1);
                            rmegui.jSpinner2.setValue(1);
                            rmegui.jSpinner1.setEnabled(false);
                            rmegui.jSpinner2.setEnabled(false);
                            rmegui.jComboBox3.setSelectedIndex(0);
                            rmegui.jComboBox3.setEnabled(false);
                            // Cursor-Bild rendern
                            rmegui.content.renderPic = new CoRImage(GraphicsComponent.grayScale(rmegui.buildPanel.getSelectedImage().getImage()));
                            rmegui.content.renderPicCursor = true;
                        }
                    }
                });
            }
        });

        rmegui.jComboBox3.addActionListener(new ActionListener() { // Cursor ändern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                paintCursor = new MapEditorCursor(rmegui.jComboBox4.getSelectedIndex(), rmegui.jComboBox3.getSelectedIndex(), Integer.parseInt(rmegui.jSpinner1.getValue().toString()), Integer.parseInt(rmegui.jSpinner2.getValue().toString()));
                rmegui.content.changePaintCursor(paintCursor);
            }
        });

        rmegui.jComboBox4.addActionListener(new ActionListener() { // Cursor ändern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.jSpinner2.setEnabled(rmegui.jComboBox4.getSelectedIndex() != 2);
                paintCursor = new MapEditorCursor(rmegui.jComboBox4.getSelectedIndex(), rmegui.jComboBox3.getSelectedIndex(), Integer.parseInt(rmegui.jSpinner1.getValue().toString()), Integer.parseInt(rmegui.jSpinner2.getValue().toString()));
                rmegui.content.changePaintCursor(paintCursor);
            }
        });

        rmegui.jSpinner1.addChangeListener(new ChangeListener() { // X-Wert für Cursor hat sich geändert

            @Override
            public void stateChanged(ChangeEvent e) {
                int newVal = Integer.valueOf(rmegui.jSpinner1.getValue().toString());
                if (newVal > 30 || newVal < 1) {
                    displayStatus(" ERROR: Size of cursor must be > 0, < 31!");
                } else {
                    // Wert ok
                    paintCursor = new MapEditorCursor(rmegui.jComboBox4.getSelectedIndex(), rmegui.jComboBox3.getSelectedIndex(), Integer.parseInt(rmegui.jSpinner1.getValue().toString()), Integer.parseInt(rmegui.jSpinner2.getValue().toString()));
                    rmegui.content.changePaintCursor(paintCursor);
                }
            }
        });

        rmegui.jSpinner2.addChangeListener(new ChangeListener() { // Y-Wert für Cursor hat sich geändert

            @Override
            public void stateChanged(ChangeEvent e) {
                int newVal = Integer.valueOf(rmegui.jSpinner2.getValue().toString());
                if (newVal > 30 || newVal < 1) {
                    displayStatus(" ERROR: Size of cursor must be > 0, < 31!");
                } else {
                    // Wert ok
                    paintCursor = new MapEditorCursor(rmegui.jComboBox4.getSelectedIndex(), rmegui.jComboBox3.getSelectedIndex(), Integer.parseInt(rmegui.jSpinner1.getValue().toString()), Integer.parseInt(rmegui.jSpinner2.getValue().toString()));
                    rmegui.content.changePaintCursor(paintCursor);
                }
            }
        });

        rmegui.jMenuItem4.addActionListener(new ActionListener() { //Der Beenden-Button

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Editor beenden - nachfragen
                Object[] options = {"Close", "Back"};
                int n = JOptionPane.showOptionDialog(rmegui,
                        "Really quit? \n Did you save?",
                        "Quit",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);
                if (n == 0) {
                    // Beenden
                    System.exit(0);
                }
            }
        });

        rmegui.jMenuItem1.addActionListener(new ActionListener() { //Der Neue-Map Button

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Auslagern
                outsourceJob(new Runnable() {

                    @Override
                    public void run() {
                        // Neue Map anlegen
                        firstMapRun = true;
                        // Wir müssen wissen: Name, Dateiname, Größe
                        // Dialog einblenden

                        rmenmd = new MapEditorNewMapDialog();
                        rmenmd.pack();
                        rmenmd.setLocationRelativeTo(null);
                        rmenmd.setVisible(true);
                        // Listener für Buttons OK und Abbrechen
                        rmenmd.jButton1.addActionListener(new ActionListener() {

                            @Override
                            public void actionPerformed(ActionEvent arg0) {
                                // OK
                                String newMapName = rmenmd.getMapName();
                                int newMapX = rmenmd.getMapX();
                                int newMapY = rmenmd.getMapY();
                                File newMapFile;

                                // Werte sind da, überprüfen ob die auch sinnvoll sind
                                newMapFile = new File("map/main/" + newMapName + ".map");
                                if (newMapFile.exists()) {
                                    // Ist schon da, geht nicht!
                                    JOptionPane.showMessageDialog(rmenmd,
                                            "Map exists!",
                                            "Syntaxfehler",
                                            JOptionPane.ERROR_MESSAGE);
                                } else if (newMapName.equals("")) {
                                    // Es wurde nix bei Name eingetragen
                                    // Warnung anzeigen
                                    JOptionPane.showMessageDialog(rmenmd,
                                            "No Mapname!",
                                            "Syntaxfehler",
                                            JOptionPane.ERROR_MESSAGE);

                                } else if (newMapX <= 0 || newMapX > 60000) {
                                    // Werte für X-Länge ausserhalb des gültigen Bereichs
                                    JOptionPane.showMessageDialog(rmenmd, "X - Length must be > 0, < 10000!", "X irregular", JOptionPane.ERROR_MESSAGE);
                                } else if (newMapY <= 0 || newMapY > 60000) {
                                    // Werte für Y-Länge ausserhalb der gültigen Bereichs
                                    JOptionPane.showMessageDialog(rmenmd, "Y - Length must be > 0, < 10000!", "Y irregular", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    // Wenn wir hier hinkommen, dann waren die Werte gut!
                                    // Fenster wieder schließen
                                    rmenmd.setVisible(false);
                                    // Löschen, spart speicher
                                    rmenmd = null;
                                    // Neue Map anlegen
                                    newMap(newMapName, newMapX, newMapY);

                                }
                            }
                        });

                        rmenmd.jButton2.addActionListener(
                                new ActionListener() {

                                    @Override
                                    public void actionPerformed(ActionEvent arg0) { // Der Abbrechen Button
                                        // Einfach das Fenster schließen
                                        rmenmd.setVisible(false);
                                        rmenmd = null;
                                    }
                                });


                    }
                });
            }
        });

        rmegui.jMenuItem2.addActionListener(new ActionListener() { //Map öffnen

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Auslagern
                outsourceJob(new Runnable() {

                    @Override
                    public void run() {
                        // Map öffnen, einen Pfadauswahldialog anzeigen
                        JFileChooser openMapChooser = new JFileChooser();
                        openMapChooser.setCurrentDirectory(new File("./map/"));
                        openMapChooser.setDialogTitle("Choose Mapfile!");
                        openMapChooser.setFileFilter(new FileNameExtensionFilter("CoR-Map", "map", "MAP"));
                        int returnVal = openMapChooser.showOpenDialog(rmegui);

                        if (returnVal == JFileChooser.APPROVE_OPTION) { // Es wurde was ausgewählt
                            File openMapFile = openMapChooser.getSelectedFile();
                            openMap(openMapFile.getPath());
                        } else { // Es wurde nix ausgewählt, abbrechen
                        }

                    }
                });
            }
        });

        rmegui.jMenuItem6.addActionListener(new ActionListener() { // Neu rendern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Einfach repaint
                rmegui.getContentPane().repaint();
                rmegui.jMenuBar1.repaint(); // Das macht der obrige Aufruf warum auch immer nicht mit...
            }
        });

        rmegui.jCheckBoxMenuItem1.addActionListener(new ActionListener() { // Gitternetz rendern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.setMesh(rmegui.jCheckBoxMenuItem1.isSelected());
                rmegui.content.repaint();
            }
        });
        rmegui.jCheckBoxMenuItem2.addActionListener(new ActionListener() { // Boden rendern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.setGround(rmegui.jCheckBoxMenuItem2.isSelected());
                rmegui.content.repaint();
            }
        });
        rmegui.jCheckBoxMenuItem3.addActionListener(new ActionListener() { // Feste Objekte rendern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.setObjects(rmegui.jCheckBoxMenuItem3.isSelected());
                rmegui.content.repaint();
            }
        });
        rmegui.jCheckBoxMenuItem4.addActionListener(new ActionListener() { // Einheiten rendern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.setCreeps(rmegui.jCheckBoxMenuItem4.isSelected());
                rmegui.content.repaint();
            }
        });
        rmegui.jCheckBoxMenuItem6.addActionListener(new ActionListener() { // Cursor rendern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.setCursor(rmegui.jCheckBoxMenuItem6.isSelected());
                rmegui.content.repaint();
            }
        });

        rmegui.jCheckBoxMenuItem7.addActionListener(new ActionListener() { // Gebäude rendern

            @Override
            public void actionPerformed(ActionEvent e) {
                rmegui.content.setBuildings(rmegui.jCheckBoxMenuItem7.isSelected());
                rmegui.content.repaint();
            }
        });

        rmegui.jCheckBoxMenuItem8.addActionListener(new ActionListener() { // Ressourcen rendern

            @Override
            public void actionPerformed(ActionEvent e) {
                rmegui.content.setRessources(rmegui.jCheckBoxMenuItem8.isSelected());
                rmegui.content.repaint();
            }
        });

        rmegui.jMenuItem3.addActionListener(new ActionListener() { // Map abspeichern

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Auslagern
                outsourceJob(new Runnable() {

                    @Override
                    public void run() {
                        // Map soll gespeichert werden
                        saveMap(theMap);
                    }
                });
            }
        });

        rmegui.groundPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // Maus wurde geklickt, zugeordnetes Feld suchen (lassen)
                boolean shiftUsed = false;
                if (arg0.isShiftDown()) {
                    shiftUsed = true;
                }
                rmegui.groundPanel.selectImage(arg0.getX(), arg0.getY(), shiftUsed);
                rmegui.groundPanel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseReleased(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseEntered(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseExited(MouseEvent arg0) { // Wurscht
            }
        });

        rmegui.fixPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // Maus wurde geklickt, zugeordnetes Feld suchen (lassen)
                boolean shiftUsed = false;
                if (arg0.isShiftDown()) {
                    shiftUsed = true;
                }
                rmegui.fixPanel.selectImage(arg0.getX(), arg0.getY(), shiftUsed);
                rmegui.fixPanel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseReleased(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseEntered(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseExited(MouseEvent arg0) { // Wurscht
            }
        });

        rmegui.creepPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // Maus wurde geklickt, zugeordnetes Feld suchen (lassen)
                if (arg0.isShiftDown()) {
                    // Für Einheiten nicht erlaubt --> Melden
                    displayStatus("Fehler: Multiselection not allowed for Units!");
                }
                rmegui.creepPanel.selectImage(arg0.getX(), arg0.getY(), false);
                rmegui.content.renderPic = new CoRImage(GraphicsComponent.grayScale(rmegui.creepPanel.getSelectedImage().getImage()));
                rmegui.creepPanel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseReleased(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseEntered(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseExited(MouseEvent arg0) { // Wurscht
            }
        });

        rmegui.miscPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                // Maus wurde geklickt, zugeordnetes Feld suchen (lassen)
                boolean shiftUsed = false;
                if (arg0.isShiftDown()) {
                    shiftUsed = true;
                }
                rmegui.miscPanel.selectImage(arg0.getX(), arg0.getY(), shiftUsed);
                rmegui.miscPanel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseReleased(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseEntered(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseExited(MouseEvent arg0) { // Wurscht
            }
        });

        rmegui.buildPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                rmegui.buildPanel.selectImage(e.getX(), e.getY(), false);
                rmegui.content.renderPic = new CoRImage(GraphicsComponent.grayScale(rmegui.buildPanel.getSelectedImage().getImage()));
                rmegui.buildPanel.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        rmegui.content.addMouseListener(new MouseListener() { // Hauptfenster

            @Override
            public void mouseClicked(final MouseEvent arg0) {
                if (arg0.getButton() == 1) {
                    // Auslagern
                    outsourceJob(new Runnable() {

                        @Override
                        public void run() {
                            // Im Zeichenmmodus?
                            if (rmegui.content.getModi() == 2) {
                                // Maus wurde geklickt, zugeordnetes (Master-)Feld suchen (lassen)
                                Dimension selField = rmegui.content.getSelectedField(arg0.getX(), arg0.getY());
                                if (!(selField.width >= theMap.getMapSizeX() || selField.height >= theMap.getMapSizeY())) { // Ob die Felder auch im Rahmen sind
                                    // Normalerweise will der User jetzt hier die aktuelle Textur hinhaben
                                    // Aber nicht immer, prüfen
                                    if (rmegui.content.getModi() == 2) {
                                        if (!colisionMode) {
                                            if (editorMode == 0) {
                                                //Ok, falls aktuell was angewählt ist, dann einfügen
                                                //Aktuelle selektierte Textur finden
                                                // Zuerst die Layereinstellung ansehen
                                                if (rmegui.jComboBox1.getSelectedIndex() == 0) {
                                                    // Layer = Boden
                                                    // Alle Felder haben eine Groundtextur, keine Begrenzung.
                                                    // Mehrfachselektion erlaubt
                                                    if (rmegui.groundPanel.gotlist) { // Sicherheitsabfrage wegen NullPointerException
                                                        // Selektierte Textur finden
                                                        // Mehrfach selektiert?
                                                        ArrayList<String> texes;
                                                        if (!rmegui.groundPanel.isInMoreMode()) {
                                                            // Cursorgröße beachten
                                                            String selTex = rmegui.groundPanel.getSelectedImage().getImageName();
                                                            texes = new ArrayList<String>();
                                                            texes.add(selTex);

                                                        } else {
                                                            texes = rmegui.groundPanel.getSelectedImages();
                                                        }
                                                        // Cursorfelder holen
                                                        ArrayList<MapEditorCursorField> fList = paintCursor.getFields(texes);
                                                        // In Map schreiben
                                                        for (int z = 0; z < fList.size(); z++) {
                                                            try {
                                                                // Bei Wasser Kollision setzten
                                                                if (fList.get(z).tex.contains("water")) {
                                                                    // Kollision setzen
                                                                    theMap.visMap[fList.get(z).posX + (int) selField.getWidth()][fList.get(z).posY + (int) selField.getHeight()].setCollision(collision.blocked);
                                                                } else if (theMap.getElementProperty(fList.get(z).posX + (int) selField.getWidth(), fList.get(z).posY + (int) selField.getHeight(), "ground_tex").contains("water")) {
                                                                    // Wenn das alte Wasser war wieder Kollision wegmachen
                                                                    theMap.visMap[fList.get(z).posX + (int) selField.getWidth()][fList.get(z).posY + (int) selField.getHeight()].setCollision(collision.free);
                                                                }
                                                                theMap.changeElementProperty(fList.get(z).posX + (int) selField.getWidth(), fList.get(z).posY + (int) selField.getHeight(), "ground_tex", fList.get(z).tex);

                                                            } catch (ArrayIndexOutOfBoundsException ex) {
                                                            }
                                                        }
                                                        // Dem Grafiksystem mitteilen
                                                        rmegui.content.changeVisMap(theMap.getVisMap());
                                                        rmegui.mapEditorMiniMap1.repaint();
                                                    }
                                                } else if (rmegui.jComboBox1.getSelectedIndex() == 1) {
                                                    // Layer = Objekte
                                                    // Grundsätzlich erlaubt, aber Konflikt mit Creeps, nur eines von beidem geht
                                                    // Löschen möglich!
                                                    if (rmegui.fixPanel.gotlist) { // Sicherheitsabfrage wegen NullPointerException
                                                        ArrayList<String> texes;
                                                        if (!rmegui.fixPanel.isInMoreMode()) {
                                                            String selTex = rmegui.fixPanel.getSelectedImage().getImageName();
                                                            texes = new ArrayList<String>();
                                                            texes.add(selTex);
                                                        } else {
                                                            texes = rmegui.fixPanel.getSelectedImages();
                                                        }
                                                        // Cursorfelder holen
                                                        ArrayList<MapEditorCursorField> fList = paintCursor.getFields(texes);
                                                        // In Map schreiben
                                                        for (int z = 0; z < fList.size(); z++) {
                                                            if (fList.get(z).tex.equals("noTex")) {
                                                                // Textur löschen - das ist möglich
                                                                theMap.deleteElementProperty(fList.get(z).posX + (int) selField.getWidth(), fList.get(z).posY + (int) selField.getHeight(), "fix_tex");
                                                                // Kollision weg! AutoCol?
                                                                if (rmegui.jCheckBoxMenuItem5.isSelected()) {
                                                                    // Col löschen
                                                                    theMap.visMap[fList.get(z).posX + (int) selField.getWidth()][fList.get(z).posY + (int) selField.getHeight()].setCollision(collision.free);
                                                                }
                                                            } else {
                                                                // In Map schreiben
                                                                theMap.deleteElementProperty(fList.get(z).posX + (int) selField.getWidth(), fList.get(z).posY + (int) selField.getHeight(), "unit_tex");
                                                                theMap.changeElementProperty(fList.get(z).posX + (int) selField.getWidth(), fList.get(z).posY + (int) selField.getHeight(), "fix_tex", fList.get(z).tex);
                                                                // Produziert Kollision! - Falls AutoCol
                                                                if (rmegui.jCheckBoxMenuItem5.isSelected()) {
                                                                    theMap.visMap[fList.get(z).posX + (int) selField.getWidth()][fList.get(z).posY + (int) selField.getHeight()].setCollision(collision.blocked);
                                                                }
                                                            }
                                                        }
                                                        rmegui.content.changeVisMap(theMap.getVisMap());

                                                    }
                                                } else if (rmegui.jComboBox1.getSelectedIndex() == 2) {
                                                    // Units
                                                    if (rmegui.creepPanel.gotlist) { // Sicherheitsabfrage wegen NullPointerException
                                                        String selTex = rmegui.creepPanel.getSelectedImage().getImageName();
                                                        int selUnitId = rmegui.creepPanel.getSelectedDesc();
                                                        // Löschen?
                                                        if (selUnitId == 0) {
                                                            // Unit löschen
                                                            if (!newMode) {
                                                                theMap.deleteElementProperty((int) selField.getWidth(), (int) selField.getHeight(), "unit_tex");
                                                            } else {
                                                                // DeleteUnit
                                                                // Suchen
                                                                Unit tUnit = identifyUnit((int) selField.getWidth(), (int) selField.getHeight());
                                                                if (tUnit != null) {
                                                                    unitList.remove(tUnit);
                                                                }
                                                            }
                                                            // Kollision weg! AutoCol?
                                                            if (rmegui.jCheckBoxMenuItem5.isSelected()) {
                                                                // Col löschen
                                                                theMap.visMap[(int) selField.getWidth()][(int) selField.getHeight()].setCollision(collision.free);
                                                            }
                                                        } else {
                                                            // Einheit einfügen
                                                            // Keine Bäume oder sowas da erlaubt
                                                            theMap.deleteElementProperty((int) selField.getWidth(), (int) selField.getHeight(), "fix_tex");
                                                            if (!newMode) {
                                                                theMap.changeElementProperty((int) selField.getWidth(), (int) selField.getHeight(), "unit_tex", selTex);
                                                            } else {
                                                                //Einfach neu erstellen und hinzufügen
                                                                try {
                                                                    Unit kUnit = descUnit.get(selUnitId).clone(getNextNetID());
                                                                    // Spieler festgelegt?
                                                                    if (rmegui.jCheckBox1.isSelected()) {
                                                                        try {
                                                                            int trans = Integer.parseInt(rmegui.jSpinner5.getValue().toString());
                                                                            if (trans >= 0 && trans <= 8) {
                                                                                kUnit.setPlayerId(trans);
                                                                            }
                                                                        } catch (java.lang.NumberFormatException exx) {
                                                                        }
                                                                    }
                                                                    kUnit.position = new Position((int) selField.getWidth(), (int) selField.getHeight());
                                                                    unitList.add(kUnit);
                                                                } catch (java.lang.CloneNotSupportedException ex) {
                                                                }
                                                            }
                                                            // Produziert Kollision!
                                                            if (rmegui.jCheckBoxMenuItem5.isSelected()) {
                                                                theMap.visMap[(int) selField.getWidth()][(int) selField.getHeight()].setCollision(collision.blocked);
                                                            }
                                                        }
                                                        // Dem Grafiksystem mitteilen - nur einmal nötig, bin aber zu faul für if's...
                                                        rmegui.content.changeVisMap(theMap.getVisMap());
                                                        rmegui.content.updateUnits(unitList);
                                                    }
                                                } else if (rmegui.jComboBox1.getSelectedIndex() == 3) {
                                                    // Layer = Ressourcen
                                                    if (rmegui.miscPanel.gotlist) { // Nullpointer - Sicherheitsabfrage
                                                        String tex = rmegui.miscPanel.getSelectedImage().getImageName();
                                                        if (tex != null && tex.equals("noTex")) {
                                                            // Löschen, suchen ob da was ist:
                                                            Ressource dres = identifyRessource(selField.width, selField.height);
                                                            if (dres != null) {
                                                                // Löschen
                                                                resList.remove(dres);
                                                                // Kollision entfernen
                                                                theMap.visMap[selField.width][selField.height].setCollision(collision.free);
                                                                // Mehrere Felder?
                                                                if (dres.getType() >= 3 && dres.getType() <= 5) {
                                                                    theMap.visMap[selField.width + 1][selField.height - 1].setCollision(collision.free);
                                                                    theMap.visMap[selField.width + 2][selField.height].setCollision(collision.free);
                                                                    theMap.visMap[selField.width + 1][selField.height + 1].setCollision(collision.free);
                                                                }
                                                                rmegui.content.repaint();
                                                            }
                                                        } else {
                                                            if (tex != null) {
                                                                // Ressource adden
                                                                // Type herausfinden
                                                                int type = 0;
                                                                if (tex.contains("FOOD")) {
                                                                    type = 1;
                                                                } else if (tex.contains("WOOD")) {
                                                                    type = 2;
                                                                } else if (tex.contains("METAL")) {
                                                                    type = 3;
                                                                } else if (tex.contains("COINS")) {
                                                                    type = 4;
                                                                } else if (tex.contains("OIL")) {
                                                                    type = 5;
                                                                }
                                                                // Was da?
                                                                if (type != 0) {
                                                                    // Position frei?
                                                                    if (theMap.visMap[selField.width][selField.height].getCollision().equals(collision.free)) {
                                                                        boolean free = true;
                                                                        // Große Ressource?
                                                                        if (type >= 3) {
                                                                            // Andere Felder checken
                                                                            if (!theMap.visMap[selField.width + 1][selField.height - 1].getCollision().equals(collision.free)) {
                                                                                free = false;
                                                                            }
                                                                            if (!theMap.visMap[selField.width + 2][selField.height].getCollision().equals(collision.free)) {
                                                                                free = false;
                                                                            }
                                                                            if (!theMap.visMap[selField.width + 1][selField.height + 1].getCollision().equals(collision.free)) {
                                                                                free = false;
                                                                            }
                                                                        }
                                                                        if (free) {
                                                                            // OK, frei, Einfügen
                                                                            Ressource newres = new Ressource(type, tex, getNextNetID());
                                                                            newres.position = new Position(selField.width, selField.height);
                                                                            resList.add(newres);
                                                                            // Kollision setzen
                                                                            theMap.visMap[selField.width][selField.height].setCollision(collision.blocked);
                                                                            if (type >= 3) {
                                                                                theMap.visMap[selField.width + 1][selField.height - 1].setCollision(collision.blocked);
                                                                                theMap.visMap[selField.width + 2][selField.height].setCollision(collision.blocked);
                                                                                theMap.visMap[selField.width + 1][selField.height + 1].setCollision(collision.blocked);
                                                                            }
                                                                            rmegui.content.repaint();
                                                                        }
                                                                    } else {
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                } else if (rmegui.jComboBox1.getSelectedIndex() == 4) {
                                                    // Layer = Gebäude
                                                    if (rmegui.buildPanel.gotlist) { // NullPointer-Sicherheitsabfrage
                                                        int selBuildingId = rmegui.buildPanel.getSelectedDesc();
                                                        if (selBuildingId == 0) {
                                                            // Löschen
                                                            // Gebäude suchen:
                                                            Building tempBu = identifyBuilding(selField.width, selField.height);
                                                            if (tempBu != null) {
                                                                // Ist da, löschen!
                                                                buildingList.remove(tempBu);
                                                                // Neu sortieren ist nich nötig!
                                                                // Kollision entfernen:
                                                                for (int z1 = 0; z1 < tempBu.z1; z1++) {
                                                                    for (int z2 = 0; z2 < tempBu.z2; z2++) {
                                                                        CoRMapElement tEle = theMap.visMap[tempBu.position.X + z1 + z2][tempBu.position.Y - z1 + z2];
                                                                        tEle.setCollision(collision.free);
                                                                    }
                                                                }
                                                                rmegui.content.updateBuildings(buildingList);
                                                            }
                                                        } else {
                                                            // Gebäude einfügen
                                                            // Alle Felder frei?
                                                            boolean free = true;
                                                            for (int z1 = 0; z1 < descBuilding.get(selBuildingId).z1; z1++) {
                                                                for (int z2 = 0; z2 < descBuilding.get(selBuildingId).z2; z2++) {
                                                                    CoRMapElement tEle = theMap.visMap[(int) selField.getWidth() + z1 + z2 + descBuilding.get(selBuildingId).offsetX][(int) selField.getHeight() - z1 + z2 + descBuilding.get(selBuildingId).offsetY];
                                                                    if (tEle != null) {
                                                                        if (tEle.getCollision() == collision.blocked) {
                                                                            // Da ist schon was, also blockiert
                                                                            free = false;
                                                                        }
                                                                    } else {
                                                                        // Das Feld gibts gar nicht / Ausserhalb der Map, also auch false
                                                                        free = false;
                                                                    }
                                                                }
                                                            }
                                                            if (free) {
                                                                try {
                                                                    // Ok, wir können bauen
                                                                    // Gebäude zur Liste hinzufügen
                                                                    Building tempB = descBuilding.get(selBuildingId).clone(getNextNetID());
                                                                    // Spieler festgelegt?
                                                                    if (rmegui.jCheckBox2.isSelected()) {
                                                                        try {
                                                                            int trans = Integer.parseInt(rmegui.jSpinner6.getValue().toString());
                                                                            if (trans >= 0 && trans <= 8) {
                                                                                tempB.setPlayerId(trans);
                                                                            }
                                                                        } catch (java.lang.NumberFormatException exx) {
                                                                        }
                                                                    }
                                                                    tempB.position = new Position((int) selField.getWidth() + descBuilding.get(selBuildingId).offsetX, (int) selField.getHeight() + descBuilding.get(selBuildingId).offsetY);
                                                                    tempB.anim = new BuildingAnimator();
                                                                    buildingList.add(tempB);
                                                                    // Liste sortieren, wegen der Perspektive
                                                                    Collections.sort(buildingList);
                                                                    // Kollision setzen
                                                                    for (int z1 = 0; z1 < descBuilding.get(selBuildingId).z1; z1++) {
                                                                        for (int z2 = 0; z2 < descBuilding.get(selBuildingId).z2; z2++) {
                                                                            CoRMapElement tEle = theMap.visMap[(int) selField.getWidth() + z1 + z2 + descBuilding.get(selBuildingId).offsetX][(int) selField.getHeight() - z1 + z2 + descBuilding.get(selBuildingId).offsetY];
                                                                            tEle.setCollision(collision.blocked);
                                                                        }
                                                                    }
                                                                    rmegui.content.updateBuildings(buildingList);

                                                                } catch (CloneNotSupportedException ex) {
                                                                    // Is abgesichert, passiert net!
                                                                }
                                                                // Ferig

                                                            }
                                                        }
                                                    }
                                                }

                                            } else if (editorMode == 2) {
                                                // Sachen verändern. - Nach Einheit suchen.
                                                Unit tempU = identifyUnit(selField.width, selField.height);
                                                Building tempB = identifyBuilding(selField.width, selField.height);
                                                // Alte einheit abwählen
                                                if (currentUnit != null) {
                                                    currentUnit.isSelected = false;
                                                    currentUnit = null;
                                                    rmegui.jButton1.setEnabled(false);
                                                }
                                                if (currentBuilding != null) {
                                                    currentBuilding.isSelected = false;
                                                    currentBuilding = null;
                                                }
                                                // Als aktuelle Einheit setzen, wenn eine gefunden wurde
                                                if (tempU != null) {
                                                    // Einheit gefunden!
                                                    currentUnit = tempU;
                                                    currentUnit.isSelected = true;
                                                    updateCurrentUnit();
                                                    rmegui.jTabbedPane3.setSelectedIndex(0);
                                                } else if (tempB != null) {
                                                    // Gebäude gefunden!
                                                    currentBuilding = tempB;
                                                    currentBuilding.isSelected = true;
                                                    updateCurrentBuilding();
                                                    rmegui.jTabbedPane3.setSelectedIndex(1);
                                                }
                                            }
                                        } else {
                                            // Kollisionsmodus
                                            // Herausfinden ob COL oder NOCOL selektiert ist
                                            if (rmegui.miscPanel.getSelectedImage().getImageName().equals("col")) {
                                                // User will COL haben
                                                // TODO: Prüfen, ob Feld Kollision NICHT bekommen darf
                                                // Col setzten
                                                ArrayList<String> texes;
                                                // Cursorgröße beachten
                                                String selTex = rmegui.groundPanel.getSelectedImage().getImageName();
                                                texes = new ArrayList<String>();
                                                texes.add(selTex);
                                                // Cursorfelder holen
                                                ArrayList<MapEditorCursorField> fList = paintCursor.getFields(texes);
                                                // In Map schreiben
                                                for (int z = 0; z < fList.size(); z++) {
                                                    theMap.visMap[fList.get(z).posX + (int) selField.getWidth()][fList.get(z).posY + (int) selField.getHeight()].setCollision(collision.blocked);
                                                }
                                                // Dem Grafikmodul schicken
                                                rmegui.content.changeVisMap(theMap.getVisMap());
                                            } else {
                                                // Nocol
                                                // Prüfen ob Feld keine Kollision haben darf
                                                // Aussenfelder müssen Kollision haben:
                                                ArrayList<String> texes;
                                                // Cursorgröße beachten
                                                String selTex = rmegui.groundPanel.getSelectedImage().getImageName();
                                                texes = new ArrayList<String>();
                                                texes.add(selTex);
                                                // Cursorfelder holen
                                                ArrayList<MapEditorCursorField> fList = paintCursor.getFields(texes);
                                                // In Map schreiben
                                                for (int z = 0; z < fList.size(); z++) {
                                                    if (theMap.visMap[fList.get(z).posX + (int) selField.getWidth()][fList.get(z).posY + (int) selField.getHeight()].getProperty("is_border") != null) {
                                                        // Nicht erlaubt
                                                    } else {
                                                        // Ok, Kollision löschen
                                                        theMap.visMap[fList.get(z).posX + (int) selField.getWidth()][fList.get(z).posY + (int) selField.getHeight()].setCollision(collision.free);
                                                    }
                                                }
                                                rmegui.content.changeVisMap(theMap.getVisMap());
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    });
                } else if (arg0.getButton() == 3) {
                    // Nur für Platzierungsmodi
                    if (MapEditor.this.editorMode == 0) {
                        // Bei rechtsklick den NoTex/Bombe (löschen) auswählen.
                        int ins = rmegui.jComboBox1.getSelectedIndex();
                        switch (ins) {
                            case 0:
                                // Boden
                                rmegui.groundPanel.selectImage(5, 5, false);
                                rmegui.groundPanel.repaint();
                                break;
                            case 1:
                                // Objekte
                                rmegui.fixPanel.selectImage(5, 5, false);
                                rmegui.fixPanel.repaint();
                                break;
                            case 2:
                                // Units
                                rmegui.creepPanel.selectImage(5, 5, false);
                                rmegui.content.renderPic = new CoRImage(GraphicsComponent.grayScale(rmegui.creepPanel.getSelectedImage().getImage()));
                                rmegui.repaint();
                                break;
                            case 3:
                                // Misc
                                rmegui.miscPanel.selectImage(5, 5, false);
                                rmegui.miscPanel.repaint();
                                break;
                            case 4:
                                // Gebäude
                                rmegui.buildPanel.selectImage(5, 5, false);
                                rmegui.content.renderPic = new CoRImage(GraphicsComponent.grayScale(rmegui.buildPanel.getSelectedImage().getImage()));
                                rmegui.repaint();
                                break;
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseReleased(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseEntered(MouseEvent arg0) { // Wurscht
            }

            @Override
            public void mouseExited(MouseEvent arg0) { // Wurscht
            }
        });

        rmegui.content.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Position bestimmen
                Dimension tempD = rmegui.content.getSpecialSelectedField(e.getX(), e.getY());
                // Frame dahin rendern
                rmegui.content.setFramePosition(tempD);
            }
        });

        rmegui.mapEditorMiniMap1.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // Ansicht da hin springen lassen
                MapEditor.this.jumpTo((int) (1.0 * e.getX() / rmegui.mapEditorMiniMap1.getWidth() * rmegui.content.sizeX) - (rmegui.content.viewX / 2), (int) (1.0 * e.getY() / rmegui.mapEditorMiniMap1.getHeight() * rmegui.content.sizeY) - (rmegui.content.viewY / 2));
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        rmegui.mapEditorMiniMap1.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                MapEditor.this.jumpTo((int) (1.0 * e.getX() / rmegui.mapEditorMiniMap1.getWidth() * rmegui.content.sizeX) - (rmegui.content.viewX / 2), (int) (1.0 * e.getY() / rmegui.mapEditorMiniMap1.getHeight() * rmegui.content.sizeY) - (rmegui.content.viewY / 2));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        rmegui.jComboBox2.addItemListener(new ItemListener() {
            // Modi-Umschalter

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                // Auslagern
                outsourceJob(new Runnable() {

                    @Override
                    public void run() {
                        // Herausfinden, welcher Modus eingestellt werden soll
                        if (rmegui.jComboBox2.getSelectedItem().toString().equals("COL")) {
                            // Col-Mode
                            rmegui.content.setColMode(true);
                            // Palette ändern
                            rmegui.content.renderPicCursor = false;
                            rmegui.jTabbedPane2.setSelectedIndex(0);
                            rmegui.jComboBox1.setSelectedIndex(3);
                            rmegui.jComboBox1.setEnabled(false);
                            rmegui.jTabbedPane1.setSelectedIndex(3);
                            rmegui.miscPanel.setList(colImages);
                            rmegui.jComboBox3.setSelectedIndex(0);
                            rmegui.jComboBox3.setEnabled(false);
                            rmegui.jSpinner1.setEnabled(true);
                            rmegui.jSpinner2.setEnabled(true);
                            rmegui.repaint();
                            colisionMode = true;
                            editorMode = 1;

                            displayStatus("ColMode active");

                        } else if (rmegui.jComboBox2.getSelectedItem().toString().equals("MAP")) {

                            // Nein, umstellen
                            rmegui.content.setColMode(false);
                            rmegui.jTabbedPane2.setSelectedIndex(0);
                            // Palette ändern
                            rmegui.jComboBox1.setSelectedIndex(0);
                            rmegui.jComboBox1.setEnabled(true);
                            rmegui.jTabbedPane1.setSelectedIndex(0);
                            rmegui.miscPanel.setList(otherImages);
                            rmegui.content.setCursor(true);
                            rmegui.jComboBox3.setEnabled(true);
                            rmegui.jSpinner1.setEnabled(true);
                            rmegui.jSpinner2.setEnabled(true);
                            if (editorMode == 4) {
                                rmegui.content.renderPicCursor = true;
                            }
                            rmegui.content.renderPicCursor = false;
                            rmegui.jTabbedPane2.setSelectedIndex(0);
                            rmegui.jTabbedPane2.setSelectedIndex(0);
                            rmegui.jTabbedPane2.setSelectedIndex(0);
                            System.out.println(rmegui.jTabbedPane2.getSelectedIndex());
                            if (currentUnit != null) {
                                currentUnit.isSelected = false;
                            }
                            currentUnit = null;
                            if (currentBuilding != null) {
                                currentBuilding.isSelected = false;
                            }
                            currentBuilding = null;

                            rmegui.content.repaint();
                            colisionMode = false;
                            editorMode = 0;

                            displayStatus("MapMode active");


                        } else if (rmegui.jComboBox2.getSelectedItem().toString().equals("UNIT")) {
                            // Unitlist-Mode
                            rmegui.content.setColMode(false);
                            rmegui.content.setCursor(true);
                            rmegui.content.renderPicCursor = false;
                            colisionMode = false;
                            editorMode = 2;
                            rmegui.jTabbedPane2.setSelectedIndex(1);
                            paintCursor = new MapEditorCursor(MapEditorCursor.TYPE_STRAIGHT, MapEditorCursor.FILL_NORMAL, 1, 1);
                            rmegui.content.changePaintCursor(paintCursor);
                            rmegui.content.repaint();

                            displayStatus("Unit/Buildingmode - active.");

                        }
                    }
                });
            }
        });

        rmegui.jButton1.addActionListener(new ActionListener() {   // OK, im Einheitendialog

            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentUnit();
            }
        });

        rmegui.jButton2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentBuilding();
            }
        });

        rmegui.jMenuItem7.addActionListener(new ActionListener() { // Boden füllen

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // Boden mit aktueller Textur füllen
                // Auslagern
                outsourceJob(new Runnable() {

                    @Override
                    public void run() {
                        // Nachfragen
                        Object[] options = {"Fill", "Back"};
                        int n = JOptionPane.showOptionDialog(rmegui,
                                "Fill complete Map with selected texture? \n Please note: This will overwrite everything!",
                                "Fill ground",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[1]);
                        if (n == 0) {
                            // Füllen
                            // Textur abrufen
                            String selTex = rmegui.groundPanel.getSelectedImage().getImageName();
                            // In Schleife füllen
                            for (int x = 0; x < theMap.getMapSizeX(); x++) {
                                for (int y = 0; y < theMap.getMapSizeY(); y++) {
                                    if (x % 2 == y % 2) {
                                        // Einfach schreiben
                                        theMap.changeElementProperty(x, y, "ground_tex", selTex);
                                    }
                                }
                            }
                            // Geschrieben
                            rmegui.content.changeVisMap(theMap.getVisMap());
                            displayStatus("Ground filled");
                            rmegui.content.repaint();
                        }
                        // User sagt nein: Dann ist auch egal
                    }
                });
            }
        });

        rmegui.jMenuItem5.addActionListener(new ActionListener() { // Maps äufräumen

            @Override
            public void actionPerformed(ActionEvent e) {
                // Nochmal fragen
                Object[] options = {"Tidy up", "Back"};
                int n = JOptionPane.showOptionDialog(rmegui,
                        "This will tidy up your map-folder by deleting outdated maps, continue?",
                        "Tidy up maps",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]);
                if (n == JOptionPane.YES_OPTION) {
                    checkMaps();
                }
            }
        });

        rmegui.jMenuItem8.addActionListener(new ActionListener() { //about

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // About Fenster anzeigen
                rmeab = new MapEditorAbout();
                rmeab.jButton1.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        // Schließen
                        rmeab.setVisible(false);
                        rmeab = null;
                    }
                });
                rmeab.setLocationRelativeTo(null);
                rmeab.setVisible(true);
            }
        });

        rmegui.buildPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Maus ist draußen, hovern aus
                rmegui.buildPanel.mouseHover(-1, -1);
            }
        });

        rmegui.buildPanel.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Position übertragen
                rmegui.buildPanel.mouseHover(e.getX(), e.getY());
            }
        });

        rmegui.creepPanel.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Maus ist draußen, hovern aus
                rmegui.creepPanel.mouseHover(-1, -1);
            }
        });

        rmegui.creepPanel.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Posititon übertragen
                rmegui.creepPanel.mouseHover(e.getX(), e.getY());
            }
        });

        rmegui.jTextField1.addActionListener(new saveUnitListener());
        rmegui.jTextField2.addActionListener(new saveUnitListener());
        rmegui.jTextField3.addActionListener(new saveUnitListener());
        rmegui.jTextField4.addActionListener(new saveUnitListener());
        rmegui.jTextField12.addActionListener(new saveUnitListener());
        rmegui.jTextField13.addActionListener(new saveUnitListener());
        rmegui.jTextField14.addActionListener(new saveUnitListener());

        rmegui.jTextField5.addActionListener(new saveBuildingListener());
        rmegui.jTextField6.addActionListener(new saveBuildingListener());
        rmegui.jTextField7.addActionListener(new saveBuildingListener());
        rmegui.jTextField8.addActionListener(new saveBuildingListener());
        rmegui.jTextField9.addActionListener(new saveBuildingListener());
        rmegui.jTextField10.addActionListener(new saveBuildingListener());
        rmegui.jTextField11.addActionListener(new saveBuildingListener());

        Action scrollRight = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.scrollRight();
                rmegui.mapEditorMiniMap1.refreshView();
            }
        };
        Action scrollLeft = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.scrollLeft();
                rmegui.mapEditorMiniMap1.refreshView();
            }
        };
        Action scrollUp = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.scrollUp();
                rmegui.mapEditorMiniMap1.refreshView();
            }
        };
        Action scrollDown = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                rmegui.content.scrollDown();
                rmegui.mapEditorMiniMap1.refreshView();
            }
        };

        rmegui.content.getInputMap(GraphicsComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("D"), "scrollRight");
        rmegui.content.getInputMap(GraphicsComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("A"), "scrollLeft");
        rmegui.content.getInputMap(GraphicsComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "scrollUp");
        rmegui.content.getInputMap(GraphicsComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("S"), "scrollDown");
        rmegui.content.getActionMap().put("scrollRight", scrollRight);
        rmegui.content.getActionMap().put("scrollLeft", scrollLeft);
        rmegui.content.getActionMap().put("scrollUp", scrollUp);
        rmegui.content.getActionMap().put("scrollDown", scrollDown);


    }

    private void initDescTypes() {
        // Läd die Einheiten & Gebäudespezifikationen
        System.out.println("[RLE]: Loading Unit-/Building-Types...");
        descUnit = new HashMap<Integer, Unit>();
        descBuilding = new HashMap<Integer, Building>();
        // Löschen-Felder einfügen
        Unit tU = new Unit(0, 0, -2);
        tU.descTypeId = 0;
        tU.Gdesc = "Deletes Units";
        tU.graphicsdata.setTexture("del");
        descUnit.put(0, tU);
        Building tB = new Building(0, 0, -2);
        tB.descTypeId = 0;
        tB.Gdesc = "Deleted Buildings";
        tB.defaultTexture = "del";
        descBuilding.put(0, tB);
        // Dateiliste erstellen

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
                                        rB.hitpoints = saveStrtoInt(v2, zeile, line);
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
                                    }/* else if (v1.equals("ability")) {
                                    RogGameObjectAbility ra = descAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
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
                                        rU.hitpoints = saveStrtoInt(v2, zeile, line);
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
                                        rU.speed = saveStrtoInt(v2, zeile, line);
                                    } else if (v1.equals("range")) {
                                        rU.range = saveStrtoDouble(v2, zeile, line);
                                    } else if (v1.equals("damage")) {
                                        rU.damage = saveStrtoInt(v2, zeile, line);
                                    } else if (v1.equals("cooldownmax")) {
                                        rU.cooldownmax = saveStrtoInt(v2, zeile, line);
                                    } else if (v1.equals("idlerange")) {
                                        rU.idlerange = saveStrtoDouble(v2, zeile, line);
                                    } /*else if (v1.equals("ability")) {
                                    RogGameObjectAbility ra = descAbilities.get(new Integer(saveStrtoInt(v2, zeile, line)));
                                    if (ra != null) {
                                    rU.abilitys.add(ra);
                                    }
                                    } */ else if ("harvester".equals(v1)) {
                                        // Einheit kann ernten:
                                        rU.canHarvest = true;
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
                                descUnit.put(id, rU);
                                inDesc = false;
                            } else if (mode.equals("B")) {
                                rB.descTypeId = id;
                                descBuilding.put(id, rB);
                                inDesc = false;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException ex) {
                System.out.println("[RogMapModule][ERROR]: Can't open descTypes");
            } catch (IOException ex) {
                System.out.println("[RogMapModule][ERROR]: Can't read descTypes!");
            } finally {
                try {
                    descReader.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private int saveStrtoInt(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        int i;
        try {
            i = Integer.parseInt(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            // Ging nicht, Syntax-Fehler ins Logfile schreiben
            System.out.println("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number! - Defaulting to 1");
            return 1;
        }
    }

    private double saveStrtoDouble(String transform, String ganzeZeile, int line) {
        // Übersetzt den String in ein int und fängt exceptions ab.
        double i;
        try {
            i = Double.parseDouble(transform);
            return i;
        } catch (java.lang.NumberFormatException ex) {
            // Ging nicht, Syntax-Fehler ins Logfile schreiben
            System.out.println("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number! - Defaulting to 1.0");
            return 1;
        }
    }

    private void updateImageList(byte modi, HashMap map) {
        // Ordnet den descListen die passenden Bilder in die ...images (ArrayList) zu.
        ArrayList tempArrayList = new ArrayList();
        tempArrayList.addAll(map.values());
        if (modi == 'b') {
            ArrayList<Building> tList = (ArrayList<Building>) tempArrayList;
            buildingImages.clear();
            for (int i = 0; i < tList.size(); i++) {
                // Für jedes einfach einfügen
                String tex = tList.get(i).defaultTexture;
                CoRImage tImg = rmegui.content.imgMap.get(tex);
                if (tImg != null) {
                    buildingImages.add(tImg);
                } else {
                    System.out.println(tex);
                    buildingImages.add(irregularimages.get(2));
                }
            }
        } else if (modi == 'u') {
            ArrayList<Unit> tList = (ArrayList<Unit>) tempArrayList;
            creepImages.clear();
            for (int i = 0; i < tList.size(); i++) {
                // Für jedes einfach einfügen
                String tex = tList.get(i).graphicsdata.getTexture();
                CoRImage tImg = rmegui.content.imgMap.get(tex);
                if (tImg != null) {
                    creepImages.add(tImg);
                } else {
                    creepImages.add(irregularimages.get(2));
                }
            }
        }
    }

    private void updateCurrentUnit() {
        // Läd die Einstellungen neu in den Einheiteneditor rein.
        rmegui.jTextField1.setText(currentUnit.graphicsdata.getTexture());
        rmegui.jSpinner3.setValue(currentUnit.playerId);
        rmegui.jTextField2.setText(String.valueOf(currentUnit.speed));
        rmegui.jTextField3.setText(String.valueOf(currentUnit.getMaxhitpoints()));
        rmegui.jTextField4.setText(String.valueOf(currentUnit.getHitpoints()));
        rmegui.jTextField12.setText(String.valueOf(currentUnit.getDamage()));
        rmegui.jTextField13.setText(String.valueOf(currentUnit.cooldownmax));
        rmegui.jTextField14.setText(String.valueOf(currentUnit.getRange()));
        rmegui.jTextField15.setText(String.valueOf(currentUnit.idlerange));
        rmegui.jButton1.setEnabled(true);
        rmegui.content.repaint();
    }

    private void updateCurrentBuilding() {
        // Läd die Einstellungen neu in den Gebäudeeditor rein.
        rmegui.jTextField5.setText(currentBuilding.defaultTexture);
        rmegui.jSpinner4.setValue(currentBuilding.playerId);
        rmegui.jTextField6.setText(String.valueOf(currentBuilding.getMaxhitpoints()));
        rmegui.jTextField7.setText(String.valueOf(currentBuilding.getHitpoints()));
        rmegui.jTextField8.setText(String.valueOf(currentBuilding.z1));
        rmegui.jTextField9.setText(String.valueOf(currentBuilding.z2));
        rmegui.jTextField10.setText(String.valueOf(currentBuilding.offsetX));
        rmegui.jTextField11.setText(String.valueOf(currentBuilding.offsetY));
        rmegui.jButton2.setEnabled(true);
        rmegui.content.repaint();
    }

    private void saveCurrentUnit() {
        // Schreibt die Einstellungen, sofern möglich, zurück aus dem EinheitenEditor in die Einheit.
        // Werte überprüfen
        boolean valuesOK = true;
        if (!imgMap.containsKey(rmegui.jTextField1.getText())) {
            valuesOK = false;
            rmegui.jTextField1.setForeground(Color.red);
        } else {
            rmegui.jTextField1.setForeground(Color.darkGray);
        }
        int tempint = Integer.parseInt(rmegui.jSpinner3.getValue().toString());
        if (tempint > 8 || tempint < 0) {
            valuesOK = false;
            rmegui.jLabel9.setForeground(Color.red);
        } else {
            rmegui.jLabel9.setForeground(Color.darkGray);
        }
        try {
            double tempD = Double.parseDouble(rmegui.jTextField2.getText());
            if (tempD < 0) {
                valuesOK = false;
                rmegui.jLabel10.setForeground(Color.red);
            } else {
                rmegui.jLabel10.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel10.setForeground(Color.red);
        }
        try {
            int tint2 = Integer.parseInt(rmegui.jTextField3.getText());
            if (tint2 < 1) {
                valuesOK = false;
                rmegui.jLabel11.setForeground(Color.red);
            } else {
                rmegui.jLabel11.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel11.setForeground(Color.red);
        }
        try {
            int tint3 = Integer.parseInt(rmegui.jTextField4.getText());
            if (tint3 < 1) {
                valuesOK = false;
                rmegui.jLabel12.setForeground(Color.red);
            } else {
                rmegui.jLabel12.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel12.setForeground(Color.red);
        }
        try {
            int tint4 = Integer.parseInt(rmegui.jTextField12.getText());
            if (tint4 < 0) {
                valuesOK = false;
                rmegui.jLabel22.setForeground(Color.red);
            } else {
                rmegui.jLabel22.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel22.setForeground(Color.red);
        }
        try {
            int tempintint = Integer.parseInt(rmegui.jTextField13.getText());
            if (tempintint < 0) {
                valuesOK = false;
                rmegui.jLabel23.setForeground(Color.red);
            } else {
                rmegui.jLabel23.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel23.setForeground(Color.red);
        }
        try {
            double tempD3 = Double.parseDouble(rmegui.jTextField14.getText());
            if (tempD3 < 0) {
                valuesOK = false;
                rmegui.jLabel24.setForeground(Color.red);
            } else {
                rmegui.jLabel24.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel24.setForeground(Color.red);
        }
        try {
            double tempD4 = Double.parseDouble(rmegui.jTextField15.getText());
            if (tempD4 < 0) {
                valuesOK = false;
                rmegui.jLabel25.setForeground(Color.red);
            } else {
                rmegui.jLabel25.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel25.setForeground(Color.red);
        }

        // Ferig, values ok?
        if (valuesOK) {
            // Einheiten reinschreiben
            currentUnit.setPlayerId(Integer.parseInt(rmegui.jSpinner3.getValue().toString()));
            currentUnit.graphicsdata.defaultTexture = rmegui.jTextField1.getText();
            currentUnit.speed = Double.parseDouble(rmegui.jTextField2.getText());
            currentUnit.maxhitpoints = Integer.parseInt(rmegui.jTextField3.getText());
            currentUnit.hitpoints = Integer.parseInt(rmegui.jTextField4.getText());
            currentUnit.damage = Integer.parseInt(rmegui.jTextField12.getText());
            currentUnit.cooldownmax = Integer.parseInt(rmegui.jTextField13.getText());
            currentUnit.range = Double.parseDouble(rmegui.jTextField14.getText());
            currentUnit.idlerange = Double.parseDouble(rmegui.jTextField15.getText());
            rmegui.content.repaint();
        }
    }

    public void saveCurrentBuilding() {
        // Schreibt die Werte aus der Gui in das Gebäude
        // Erst Werte checken
        boolean valuesOK = true;
        if (!imgMap.containsKey(rmegui.jTextField5.getText())) {
            valuesOK = false;
            rmegui.jTextField5.setForeground(Color.red);
        } else {
            rmegui.jTextField5.setForeground(Color.darkGray);
        }
        int tempint = Integer.parseInt(rmegui.jSpinner4.getValue().toString());
        if (tempint > 8 || tempint < 0) {
            valuesOK = false;
            rmegui.jLabel15.setForeground(Color.red);
        } else {
            rmegui.jLabel15.setForeground(Color.darkGray);
        }
        try {
            int tint2 = Integer.parseInt(rmegui.jTextField6.getText());
            if (tint2 < 1) {
                valuesOK = false;
                rmegui.jLabel16.setForeground(Color.red);
            } else {
                rmegui.jLabel16.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel16.setForeground(Color.red);
        }
        try {
            int tint2 = Integer.parseInt(rmegui.jTextField7.getText());
            if (tint2 < 1) {
                valuesOK = false;
                rmegui.jLabel17.setForeground(Color.red);
            } else {
                rmegui.jLabel17.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel17.setForeground(Color.red);
        }
        try {
            int tint2 = Integer.parseInt(rmegui.jTextField8.getText());
            if (tint2 < 1) {
                valuesOK = false;
                rmegui.jLabel18.setForeground(Color.red);
            } else {
                rmegui.jLabel18.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel18.setForeground(Color.red);
        }
        try {
            int tint2 = Integer.parseInt(rmegui.jTextField9.getText());
            if (tint2 < 1) {
                valuesOK = false;
                rmegui.jLabel19.setForeground(Color.red);
            } else {
                rmegui.jLabel19.setForeground(Color.darkGray);
            }
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel19.setForeground(Color.red);
        }
        try {
            int tint2 = Integer.parseInt(rmegui.jTextField10.getText());
            rmegui.jLabel20.setForeground(Color.darkGray);
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel20.setForeground(Color.red);
        }
        try {
            int tint2 = Integer.parseInt(rmegui.jTextField11.getText());
            rmegui.jLabel21.setForeground(Color.darkGray);
        } catch (java.lang.NumberFormatException ex) {
            valuesOK = false;
            rmegui.jLabel21.setForeground(Color.red);
        }

        // War alles ok?
        if (valuesOK) {
            // Werte schreiben
            currentBuilding.setPlayerId(Integer.parseInt(rmegui.jSpinner4.getValue().toString()));
            currentBuilding.defaultTexture = rmegui.jTextField5.getText();
            currentBuilding.maxhitpoints = Integer.parseInt(rmegui.jTextField6.getText());
            currentBuilding.hitpoints = Integer.parseInt(rmegui.jTextField7.getText());
            currentBuilding.z1 = Integer.parseInt(rmegui.jTextField8.getText());
            currentBuilding.z2 = Integer.parseInt(rmegui.jTextField9.getText());
            currentBuilding.offsetX = Integer.parseInt(rmegui.jTextField10.getText());
            currentBuilding.offsetY = Integer.parseInt(rmegui.jTextField11.getText());
            rmegui.content.repaint();
        }
    }

    public void newMap(String newMapName, int newMapX, int newMapY) {
        // Erstellt neue Maps, Input gefiltert!!!!
        System.out.println("[RME]: Creating new Map \"" + newMapName + "\" " + newMapX + "x" + newMapY);
        // Die Werte wurden bereits erstellt, sie sind in Ordnung!
        // Mapobject anlegen
        // Erstmap das MapArray anlegen:
        CoRMapElement[][] newMapArray = new CoRMapElement[newMapX][newMapY];
        for (int x = 0; x < newMapX; x++) {
            for (int y = 0; y < newMapY; y++) {
                // Jedes 2. Feld überspringen, da es die nicht gibt:
                if (x % 2 == y % 2) {
                    newMapArray[x][y] = new CoRMapElement();
                }
            }
        }
        // Jetzt die Map
        CoRMap newRogMap = new CoRMap(newMapX, newMapY, newMapName, newMapArray);
        // Grenzen der Map mit Kollision und isborder ausstatten
        for (int x = 0; x < newMapX; x++) {
            for (int y = 0; y < newMapY; y++) {
                if (x == 0 || x == (newMapX - 1) || y == 0 || y == (newMapY - 1)) {
                    if (x % 2 == y % 2) {
                        // Feld hat Kollision
                        newRogMap.changeElementProperty(x, y, "is_border", "true");
                        newRogMap.visMap[x][y].setCollision(collision.blocked);
                    }
                }
            }
        }
        // Leere UnitList einfügen
        ArrayList<Unit> tempList = new ArrayList<Unit>();
        newRogMap.setMapProperty("UNIT_LIST", tempList);
        // Leere BuildingList einfügen
        ArrayList<Building> tempList2 = new ArrayList<Building>();
        newRogMap.setMapProperty("BUILDING_LIST", tempList2);
        // Leere ResList erstellen
        ArrayList<Ressource> tempList3 = new ArrayList<Ressource>();
        newRogMap.setMapProperty("RES_LIST", tempList3);
        // Map im Editor öffnen
        System.out.println("[RME]: Map created.");
        // TEST: Map mit Testtextur füllen:
      /*
        newRogMap.changeElementProperty(x, y, "ground_tex", "img/ground/testground1.png");
        if (x == 6 && y == 8) {
        newRogMap.changeElementProperty(x, y, "unit_tex", "img/creeps/orc1.png");
        }
        }
        } */
        System.out.println("[RME]: Saving map...");
        // Das Erste mal abspeichern
        saveMap(newRogMap);
        // Das Erste mal öffnen
        System.out.println("[RME]: Reading map...");
        openMap(newRogMap.getPath());
    }

    public void saveMap(CoRMap newRogMap) {
        // Speichert die Map ab.
        // UnitList zurückschreiben, wenn nicht das erste Mal
        if (currentUnit != null) {
            currentUnit.isSelected = false;
        }
        if (currentBuilding != null) {
            currentBuilding.isSelected = false;
        }
        if (!firstMapRun) {
            newRogMap.setMapProperty("BUILDING_LIST", buildingList);
            newRogMap.setMapProperty("UNIT_LIST", unitList);
            newRogMap.setMapProperty("RES_LIST", resList);
            newRogMap.setMapProperty("NEXTNETID", new Integer(this.nextNetID));
        } else {
            newRogMap.setMapProperty("NEXTNETID", new Integer(1));
            firstMapRun = false;
        }
        // Datei speichern anlegen
        MapIO.saveMap(newRogMap, newRogMap.mapName);
    }

    public void openMap(String openMapPath) {
        // Öffnet eine Map anhand des Dateinamens
        theMap = MapIO.readMap(openMapPath);
        unitList = (ArrayList<Unit>) theMap.getMapPoperty("UNIT_LIST");
        buildingList = (ArrayList<Building>) theMap.getMapPoperty("BUILDING_LIST");
        resList = (ArrayList<Ressource>) theMap.getMapPoperty("RES_LIST");
        nextNetID = (Integer) theMap.getMapPoperty("NEXTNETID");
        rmegui.content.updateUnits(unitList);
        rmegui.content.updateBuildings(buildingList);
        rmegui.content.updateRessources(resList);
        System.out.println("[RLE] Loading map done. Displaying...");
        rmegui.content.setPosition(0, 0);
        rmegui.content.setVisibleArea(34, 42);
        rmegui.mapEditorMiniMap1.imgMap = rmegui.content.imgMap;
        rmegui.mapEditorMiniMap1.content = rmegui.content;
        rmegui.mapEditorMiniMap1.map = theMap;
        rmegui.mapEditorMiniMap1.repaint();
        refreshMap();
        displayStatus("Loading Map... done.");
        rmegui.content.repaint();
    }

    public void jumpTo(int scrollX, int scrollY) {
        // Lässt die Ansicht zur Position springen, alles was größere Koordinaten hat ist drin, die angegebene Position ist links oben
        // Achtung: Garantiert nicht, dass die Position links oben auch eingestellt wird, die Map wird nicht über den Rand hinaus gescrollt....
        if ((scrollX + rmegui.content.viewX) > rmegui.content.sizeX) {
            scrollX = rmegui.content.sizeX - rmegui.content.viewX;
        }
        if ((scrollY + rmegui.content.viewY) > rmegui.content.sizeY) {
            scrollY = rmegui.content.sizeY - rmegui.content.viewY;
        }
        if (scrollX < 0) {
            scrollX = 0;
        }
        if (scrollY < 0) {
            scrollY = 0;
        }
        // Nur in 2er-Schritten scollen:
        if (scrollX % 2 == 1) {
            scrollX--;
            if (scrollX < 0) {
                scrollX = 0;
            }
        }
        if (scrollY % 2 == 1) {
            scrollY--;
            if (scrollY < 0) {
                scrollY = 0;
            }
        }
        rmegui.content.setPosition(scrollX, scrollY);
        rmegui.mapEditorMiniMap1.refreshView();
    }

    private int getNextNetID() {
        nextNetID++;
        System.out.println(nextNetID);
        return (nextNetID - 1);
    }

    private void checkMaps() {
        // Überprüft alle Maps, ob sie sich öffnen lassen , wenn nicht, ist die Map veraltet und wird gelöscht

        int deletedMaps = 0;

        // Files holen
        File mapFolder = new File("map/main");
        File maps[] = mapFolder.listFiles(new FilenameFilter() {    // Nur .map und .MAP einlesen

            @Override
            public boolean accept(File dir, String name) {
                File testFile = new File(dir.getPath() + "/" + name);
                return testFile.isFile() && (testFile.getName().endsWith(".map") || testFile.getName().endsWith(".MAP"));
            }
        });

        // Durchgehen, und versuchen einzulesen

        for (File map : maps) {
            // Lässt sie sich öffnen?
            ObjectInputStream objIn = null;
            try {
                ZipFile zipfile = new ZipFile(map);
                ZipEntry entry = zipfile.getEntry("MAP");
                objIn = new ObjectInputStream(new BufferedInputStream(zipfile.getInputStream(entry)));
                try {
                    theMap = (CoRMap) objIn.readObject();
                } catch (ClassNotFoundException ex) {
                }
                objIn.close();
            } catch (IOException ex) {
                // Diese geht nicht auf!
                try {
                    objIn.close();
                } catch (Exception exx) {
                }
                // Also löschen
                map.delete();
                deletedMaps++;
            } finally {
                try {
                    objIn.close();
                } catch (Exception ex) {
                }
            }
        }

        // Fertig, ausgeben
        JOptionPane.showMessageDialog(rmegui, deletedMaps + " old maps deleted", "Tidy up Maps.", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshMap() {
        // Zeigt die derzeitige Map (theMap) auch entgültig an
        // Mesh oder nicht?
        rmegui.content.setVisMap(theMap.getVisMap(), theMap.getMapSize("x"), theMap.getMapSize("y"));
        rmegui.content.setMesh(rmegui.jCheckBoxMenuItem1.isSelected());
        rmegui.content.setGround(rmegui.jCheckBoxMenuItem2.isSelected());
        rmegui.content.setObjects(rmegui.jCheckBoxMenuItem3.isSelected());
        rmegui.content.setCreeps(rmegui.jCheckBoxMenuItem4.isSelected());
        rmegui.content.setCursor(rmegui.jCheckBoxMenuItem6.isSelected());
        rmegui.content.setBuildings(rmegui.jCheckBoxMenuItem7.isSelected());
        rmegui.content.setRessources(rmegui.jCheckBoxMenuItem8.isSelected());
        // Und los gehts!
        rmegui.content.startEditorRender();
    }

    public void displayStatus(String s) {
        // Zeit den Statustext in der Leiste unten an
        rmegui.setStatus(s);
    }

    public Unit identifyUnit(int x, int y) {
        // Identifizier eine Einheit anhand ihrer Koordinaten
        // EDITOR-VERSION, nur FELDER
        for (int i = 0; i < unitList.size(); i++) {
            Unit tempUnit = unitList.get(i);
            if (tempUnit.position.X == x && tempUnit.position.Y == y) {
                // Gefunden zurückliefern
                return tempUnit;
            }
        }
        // Nix, gefunden, ok
        return null;
    }

    public Building identifyBuilding(int x, int y) {
        // Indentifiziert ein Gebäude anhand der Koordinaten
        for (int i = 0; i < buildingList.size(); i++) {
            Building tempBuilding = buildingList.get(i);
            for (int z1 = 0; z1 < tempBuilding.z1; z1++) {
                for (int z2 = 0; z2 < tempBuilding.z2; z2++) {
                    if (x == (tempBuilding.position.X + z1 + z2) && y == (tempBuilding.position.Y - z1 + z2)) {
                        // Da!
                        return tempBuilding;
                    }
                }
            }
        }
        // Nix gfunden, schade
        return null;
    }

    public Ressource identifyRessource(int x, int y) {
        // Findet eine Ressource durch ihre Koordinaten - EDITOR ONLY
        for (Ressource res : resList) {
            if (res.position.equals(new Position(x, y))) {
                return res;
            } else {
                // z-Felder checken, aber nur bei Res 3-5
                if (res.getType() >= 3 && res.getType() <= 5) {
                    // 3 Weitere Felder zum checken
                    if (res.position.equals(new Position(x + 1, y - 1))) {
                        return res;
                    } else if (res.position.equals(new Position(x + 2, y))) {
                        return res;
                    } else if (res.position.equals(new Position(x + 1, y + 1))) {
                        return res;
                    }
                }
            }
        }
        // Nix da.
        return null;
    }

    private void errorMessage(String title, String message) {
        JOptionPane.showMessageDialog(rmegui, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void infoMessage(String title, String message) {
        JOptionPane.showMessageDialog(rmegui, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void compressMap(String path) {
        try {
            // Komprimiert eine alte, noch unkompprimierte Map - nur aus Kompatibilitätsgründen vorhanden
            ObjectInputStream objIn = null;
            CoRMap map = null;
            objIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));
            map = (CoRMap) objIn.readObject();
            objIn.close();
            File newMapSaver = new File(map.getPath());
            ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(newMapSaver)));
            ZipEntry entry = new ZipEntry("MAP");
            zipOut.putNextEntry(entry);
            ObjectOutputStream objOut = new ObjectOutputStream(zipOut);
            objOut.writeObject(map);
            zipOut.closeEntry();
            objOut.close();
        } catch (ClassNotFoundException ex) {
            errorMessage("Import map", "Error importing Map. Not a CoR-Map, too old or currupted.");
        } catch (FileNotFoundException ex) {
            // Sollte intern abgefangen werden....
            ex.printStackTrace();
        } catch (IOException ex) {
            errorMessage("Import map", "Error importing Map. Not a CoR-Map, too old or currupted.");
        }

    }

    private void outsourceJob(Runnable r) {
        // Auslagern und starten
        new Thread(r).start();
    }

    public boolean isValidUnitDesc(int desc) {
        if (descUnit.get(desc) != null) {
            return true;
        }
        return false;
    }

    public boolean isValidBuildingDesc(int desc) {
        if (descBuilding.get(desc) != null) {
            return true;
        }
        return false;
    }

    public void insertUnitAnimator(int desc, UnitAnimator rgua) {
        // Speichert einen vorkonfigurierten Animator in die DESC-Datenbank ein
        descUnit.get(desc).anim = rgua;
    }

    private int readFrameRate(File properties) {
        try {
            // Liest die Framerate aus einem gegebenen Properties-File ein.
            FileReader reader = new FileReader(properties);
            BufferedReader breader = new BufferedReader(reader);
            String zeile = null;
            while ((zeile = breader.readLine()) != null) {
                if (zeile.contains("framerate")) {
                    return Integer.parseInt(zeile.substring(zeile.indexOf("=")));
                }
            }
            // Nicht gefunden, defaulting
            return 25;
        } catch (IOException ex) {
            return 25;
        } catch (NumberFormatException ex) {
            return 25;
        }
    }

    public static void main(String args[]) {
        // Einfach durchstarten
        MapEditor RME = new MapEditor();
    }

    class saveUnitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            saveCurrentUnit();
        }
    }

    class saveBuildingListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            saveCurrentBuilding();
        }
    }
}
