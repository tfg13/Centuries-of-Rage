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
package thirteenducks.cor.graphics;

import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author  tfg
 */
public class GraphicsPanel extends javax.swing.JLabel {
/*
//    public boolean gotlist = false; // Ob BilderListe erhalten UND angezeigt wird
//    ArrayList<CoRImage> images;
//    ArrayList<String> selectedImages; // Für die neue mehrfachselektion
//    ArrayList<Unit> descUnits;
//    ArrayList<Building> descBuildings;
//    CoRImage selectedImage; // Derzeit gewähltes Bild
//    int selectedIndex = 0;
//    private boolean moreMode = false; // Mehrfachselektion aktiv?
//    private boolean allowMoreMode = true;
//    private boolean descMode = false; // In diesem Modus werden keine Texturen, sondern eingelesene RogUnit/RogBuildings verwendet.
//    private byte descModi;
//    int hoverx = -1;
//    int hovery = -1;
//
//    /** Creates new form RogGraphicsPanel */
//    public GraphicsPanel() {
//        selectedImages = new ArrayList<String>();
//        initComponents();
//    }
//
//    @Override
//    public void paintComponent(Graphics g) {
//        //Liste einblenden, falls wir die bekommen haben...
//        if (gotlist) {
//            // Liste einblenden
//            // Immer 3 Nebeneinanden, beliebig viele untereinander
//            int xcounter = 0;
//            int ycounter = 0;
//            for (int i = 0; i < images.size(); i++) {
//                // Für jedes in dem Array einmal durchlaufen und einfügen
//                // Position bestimmen:
//                int xtarget = xcounter * 40;
//                int ytarget = ycounter * 40;
//                CoRImage tempimage = images.get(i);
//                if (tempimage.getImage().getWidth() != 40 || tempimage.getImage().getWidth() != 40) {
//                    g.drawImage(tempimage.getImage(), xtarget, ytarget, xtarget + 40, ytarget + 40, 0, 0, tempimage.getImage().getWidth(), tempimage.getImage().getWidth(), null);
//                } else {
//                    g.drawImage(tempimage.getImage(), xtarget, ytarget, null);
//                }
//                if (xtarget != 0) {
//                    g.drawLine(xtarget, ytarget, xtarget, ytarget + 40); // Trennlinie zeichnen
//                }
//                xcounter++;
//                if (xcounter == 3) {
//                    ycounter++;
//                    xcounter = 0;
//                    g.drawLine(0, ycounter * 40, 120, ytarget + 40);
//                }
//                // Derzeit selectiert?
//                if (!moreMode) {
//                    if (i == selectedIndex) {
//                        // Markieren
//                        g.drawRect(xtarget + 1, ytarget + 1, 38, 38);
//                    }
//                } else {
//                    // Alle markierten rot anmalen
//                    if (selectedImages.contains(images.get(i).getImageName())) {
//                        // Markieren
//                        g.setColor(Color.RED);
//                        g.drawRect(xtarget + 1, ytarget + 1, 38, 38);
//                        g.setColor(Color.BLACK);
//                    }
//                }
//            }
//
//            // Jetzt das Hover rendern, falls Koordinaten da sind...
//            if (hoverx != -1) {
//                // Position des Feldes finden
//                // Zuerst den X Wert untersuchen:
//                int countX = 0; // Default auf erstes
//                if (hoverx > 40 && hoverx <= 80) {
//                    // Zweites
//                    countX = 1;
//                } else if (hoverx > 80) {
//                    // Drittes
//                    countX = 2;
//                }
//                // Jetzt Y
//                int countY = Math.round(hovery / 40);
//                // Nummer
//                int countImage = countX + (3 * countY);
//                if (countImage >= 0 && countImage < images.size()) {
//                    // Ok, gefunden.
//                    // Hover oben oder unten rendern?
//                    g.setColor(Color.ORANGE);
//                    int renderY = 0;
//                    if (hovery < 40) {
//                        renderY = 40;
//                    }
//                    g.fillRect(0, renderY, getWidth(), 40);
//                    g.setColor(Color.BLACK);
//                    g.drawRect(0, renderY, getWidth() - 1, 40);
//                    // Jetzt DESC-Infos reinrendern
//                    if (descModi == 'b') {
//                        // Namen
//                        g.setFont(Font.decode("Arial-10"));
//                        String tempStr = null;
//                        tempStr = descBuildings.get(countImage).name;
//                        if (tempStr != null) {
//                            g.drawString(tempStr, 2, renderY + 10);
//                        }
//                        tempStr = descBuildings.get(countImage).z1 + " x " + descBuildings.get(countImage).z2;
//                        // Größe
//                        if (tempStr != null) {
//                            g.drawString(tempStr, getWidth() - 25, renderY + 10);
//                        }
//                        tempStr = descBuildings.get(countImage).Gdesc;
//                        if (tempStr != null) {
//                            // Beschreibung
//                            g.drawString(tempStr, 2, renderY + 19);
//                        }
//                        tempStr = "HP: " + descBuildings.get(countImage).getHitpoints() + "/" + descBuildings.get(countImage).getMaxhitpoints();
//                        if (tempStr != null) {
//                            // Hitpoints
//                            g.drawString(tempStr, 2, renderY + 28);
//                        }
//                    } else if (descModi == 'u') {
//                        // Namen
//                        g.setFont(Font.decode("Arial-10"));
//                        String tempStr = null;
//                        tempStr = descUnits.get(countImage).name;
//                        if (tempStr != null) {
//                            g.drawString(tempStr, 2, renderY + 10);
//                        }
//                        // Beschreibung
//                        tempStr = descUnits.get(countImage).Gdesc;
//                        if (tempStr != null) {
//                            // Beschreibung
//                            g.drawString(tempStr, 2, renderY + 19);
//                        }
//                        // Hitpoints
//                        if (countImage == 0) { // Die Löschen-Funktion braucht das net
//                            tempStr = null;
//                        } else {
//                            tempStr = "HP: " + descUnits.get(countImage).getHitpoints() + "/" + descUnits.get(countImage).getMaxhitpoints();
//                        }
//                        if (tempStr != null) {
//                            g.drawString(tempStr, 2, renderY + 28);
//                        }
//                        // Stark / Schwach gegen
//                        if (countImage == 0) {
//                            tempStr = null;
//                        } else {
//                            tempStr = "+ " + descUnits.get(countImage).Gpro + " | - " + descUnits.get(countImage).Gcon;
//                        }
//                        if (tempStr != null) {
//                            g.drawString(tempStr, 2, renderY + 37);
//                        }
//                        // Reichweite
//                        if (countImage != 0) {
//                            if (descUnits.get(countImage).getRange() == 2.0) {
//                                tempStr = "NAH";
//                            } else {
//                                tempStr = "FERN";
//                            }
//                            g.drawString(tempStr, getWidth() - 25, renderY + 10);
//                        }
//                    }
//                }
//            }
//
//        }
//    }
//
//    public void setList(ArrayList<CoRImage> imglist) {
//        // Neue Bildliste einfügen
//        images = imglist;
//        gotlist = true;
//        // Per default ist das erste Selektiert
//        if (images.isEmpty()) {
//            gotlist = false;
//        } else {
//            selectedImage = images.get(0);
//            // Größe einstellen
//            int shownElements = images.size() / 3 + 1;
//            this.setSize(this.getWidth(), shownElements * 40);
//            this.setPreferredSize(new Dimension(this.getWidth(), shownElements * 40));
//        }
//
//    }
//
//    public void enableDescMode(byte mode, HashMap descList) {
//        // Schaltet auf den DESC-Modus um, nicht umkehrbar
//        // Nicht robust gegen Aufrufe mit fehlerhaften Parametern
//        descMode = true;
//        descModi = mode;
//        if (mode == 'u') {
//            // Units
//            ArrayList tArrayList = new ArrayList();
//            tArrayList.addAll(descList.values());
//            descUnits = (ArrayList<Unit>) tArrayList;
//        } else if (mode == 'b') {
//            // Gebäude
//            ArrayList tArrayList = new ArrayList();
//            tArrayList.addAll(descList.values());
//            descBuildings = (ArrayList<Building>) tArrayList;
//        }
//        allowMoreMode = false;
//        moreMode = false;
//    }
//
//    public void mouseHover(int x, int y) {
//        hoverx = x;
//        hovery = y;
//        repaint();
//    }
//
//    public Dimension getPreferedSize() {
//        // Größe der Liste bestimmen
//        if (gotlist) {
//            // Große der Auswahl anhand der Anzahl ihrer Elemente bestimmen:
//            // Anzahl x: immer 3 Stück, also 3x40=120Pixel
//            // Anzahl y: Gesamtanzahl / 3 * 40 Pixel
//            return new Dimension(120, (images.size() + 1) / 3 * 40);
//        } else {
//            // Einfach irgendein Standartwert
//            return new Dimension(120, 120);
//        }
//    }
//
//    public void selectImage(int selX, int selY, boolean moreSel) {
//        // Bild an diesen Koordinaten wurde angeklickt: Herausfinden, welches das ist
//        // Zuerst den X Wert untersuchen:
//        int countX = 0; // Default auf erstes
//        if (selX > 40 && selX <= 80) {
//            // Zweites
//            countX = 1;
//        } else if (selX > 80) {
//            // Drittes
//            countX = 2;
//        }
//        // Jetzt Y
//        int countY = Math.round(selY / 40);
//        // Nummer
//        int countImage = countX + (3 * countY);
//        try {
//            if (moreSel) {
//                if (allowMoreMode) {
//                    moreMode = true;
//                    // Dazu oder weg
//                    if (!selectedImages.contains(images.get(countImage).getImageName())) {
//                        selectedImages.add(images.get(countImage).getImageName());
//                    } else {
//                        // weg, wenn dann noch was übrig bleibt
//                        if (selectedImages.size() > 1) {
//                            selectedImages.remove(images.get(countImage).getImageName());
//                        }
//                    }
//                }
//            } else {
//                moreMode = false;
//                selectedImages.clear();
//                // Nur eines
//                selectedImage = images.get(countImage);
//                selectedIndex = countImage;
//            }
//        } catch (java.lang.IndexOutOfBoundsException ex) {
//            // User hat ausserhalb der Palette geklickt, nix unternehmen
//        }
//    }
//
//    protected void setMoreModeAllowed(boolean b) {
//        if (!descMode) {
//            // Im Desc-Mode kann immer nur eine Einheit ausgewählt werden
//            allowMoreMode = b;
//        }
//    }
//
//    public ArrayList<String> getSelectedImages() {
//        return selectedImages;
//    }
//
//    public boolean isInMoreMode() {
//        return moreMode;
//    }
//
//    public CoRImage getSelectedImage() {
//        // Liefert das aktuelle Image zurück
//        return selectedImage;
//    }
//
//    public int getSelectedDesc() {
//        if (descModi == 'b') {
//            return descBuildings.get(selectedIndex).descTypeId;
//        } else {
//            return descUnits.get(selectedIndex).descTypeId;
//        }
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
