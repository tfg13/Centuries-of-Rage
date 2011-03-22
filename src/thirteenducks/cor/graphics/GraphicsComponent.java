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

import thirteenducks.cor.game.Position;
import thirteenducks.cor.tools.mapeditor.MapEditorCursor;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.VolatileImage;
import java.util.*;
import javax.swing.JLabel;
import org.apache.commons.collections.buffer.PriorityBuffer;
import thirteenducks.cor.map.AbstractMapElement;

public class GraphicsComponent extends JLabel {
    // Diese Klasse repräsentiert den Tatsächlichen GrafikINHALT von RogGraphics und RogMapEditor

    CoRImage defaultimage;
    CoRImage colModeImage;
    Color color;
    protected int modi = 0; // Was gerendert werden soll, spezielle Ansichten für den Editor etc...
    AbstractMapElement[][] visMap; // Die angezeigte Map
    boolean renderMesh = false; // Gitter rendern
    boolean renderGround = false; // Boden rendern
    boolean renderObjects = false; // Objecte (Bäuume etc) rendern
    boolean renderCreeps = false; // Einheiten rendern
    boolean renderBuildings = false; // Gebäude rendern
    boolean renderCursor = false;
    public boolean renderPicCursor = false; // Cursor, der ein Bild anzeigt, z.B. ein Haus, das gebaut werden soll
    boolean renderRessources = false;
    boolean renderFogOfWar = false;
    public CoRImage renderPic;
    public int sizeX; // Mapgröße
    public int sizeY;
    public int positionX; // Position, die angezeigt wird
    public int positionY;
    public int viewX; // Die Größe des Angezeigten ausschnitts
    public int viewY;
    public int backupViewX; // Muss manchmal umgestellt werden, wobei das alte behalten werden soll...
    public int backupViewY;
    public int realPixX; // Echte, ganze X-Pixel
    public int realPixY; // Echte, ganze Y-Pixel
    public HashMap<String, CoRImage> imgMap; // Enthält alle Bilder
    public HashMap<String, CoRImage> grayImgMap; // Enthält die Grau-Versionen, für den Editor
    public HashMap<String, CoRImage> coloredImgMap;
    VolatileImage renderBackground = null; // Hintergrundbild für Grafikausgabe
    BufferedImage saverenderBackground = null;
    VolatileImage interactivehud = null;
    BufferedImage saveinteractivehud = null;
    boolean colMode = false; // Modus, der die Kollision anzeigt
    boolean byPass = false; // Speziell, damit einige NULLPOINTER Kontrollen übergangen werden können
    boolean useAntialising = false; // Kantenglättung, normalerweise AUS // NUR IM NORMALEN RENDERMODE
    java.util.List<Unit> unitList;
    java.util.List<Building> buildingList;
    Vector<Sprite> allList;
    ArrayList<GameObject> selectedObjects;
    boolean newMode = true; // Units in Liste statt als Textur
    ArrayList<Position> wayPath; // Ein weg
    boolean enableWaypointHighlighting;
    protected boolean alwaysshowenergybars = false; // Energiebalken immer anzeigen, WC3 lässt grüßen.
    PriorityBuffer wayOpenList;
    ArrayList<Position> wayClosedList;
    boolean displayFrameRate = false;
    Date initRun;
    public int realRuns = 0; // Für echte Framerateanzeige
    public int lastFr = 0; // Auch frameRatezeug
    Dimension framePos = new Dimension(0, 0); // Editor-Rahmen
    MapEditorCursor drawCursor;
    ArrayList selectionShadows;
    ArrayList selectionShadowsB;
    BufferedImage advselshadB;                          // AdvancedSelectionShadow - Ermöglicht Pixelgenaue Einheitenselektion
    Date[] analyze;
    GraphicsConfiguration cgc;
    protected int epoche = 2; // Null ist keine Epoche, also kein Gescheites Grundbild..
    int hudX; // X-Koordinate vom Hud, damits nicht dauernd neu ausgerechnet werden muss...
    int hudSizeX; // Hängt vom oberen ab
    Dimension boxselectionstart;
    protected boolean dragSelectionBox = false;
    BufferedImage wayPointHighlighting[];
    BufferedImage[] huds;                               // Die Huds für verschiedene Epochen
    int lastMenuHash;                                   // Das Hud-Menü wird nur neu gezeichnet, wenn sich der Hash der selektieren Einheiten verändert.
    ArrayList<GameObject[]> interSelFields;          // Die Selektierten Einheiten als Representation auf dem Hud
    BufferedImage[] scaledBuildingLocation;             // Die Größe eines Gebäudes, in skalierter Version
    VolatileImage buildingLayer;                        // Layer für Gebäude auf der Minimap
    BufferedImage savebuildingLayer;
    boolean updateInterHud = false;
    int mouseX;                                         // Die Position der Maus, muss geupdatet werden
    int mouseY;                                         // Die Position der Maus, muss geupdatet werden
    Font[] fonts;                                       // Die Fonts, die häufig benötigt werden
    long pause;                                         // Zeitpunkt der letzen Pause
    boolean saveMode = false;                           // Sicherer Grafikmodus, verhintert Blackscreens, verwendet keine Volatiles...
    boolean coordMode = false;                          // Modus, der die Koordinaten der Felder mit anzeigt.
    boolean idMode = false;                             // Modus, der die NetIds von Einheiten/Gebäuden und Ressourcen mit anzeigt.
    boolean smoothGround = false;                       // Grobe Ecken aus der Bodentextur rausrechnen
    ArrayList<Ability> optList;            // Liste der derzeit anklickbaren Fähigkeiten
    GameObject tempInfoObj;                          // Über dieses Objekt werden Temporär Infos angezeigt.
    boolean updateBuildingsFow = false;                 // Lässt den Gebäude-Fow neu erstellen
    int loadStatus = 0;
    boolean loadWait = false;

    @Override
    public void paintComponent(Graphics g) {
        //Die echte, letzendlich gültige paint-Methode, sollte nicht direkt aufgerufen werden
        // verwendet advanced Grafiktechniken, also Graphics2D Api verwenden
        Graphics2D g2 = (Graphics2D) g;
        if (modi == 2) {
            g2.setColor(Color.BLACK);
            // Editor
            // Bodentexturen rendern

            if (renderGround) {
                // OK, Boden rendern!
                runRenderRoundGround(g2);
            }
            // Mesh rendern?
            if (renderMesh) {
                g2.setColor(Color.BLACK);
                // Mesh rendern
                for (int i = 0; i < sizeX; i++) {
                    //Am oberen Rand entlanggehen, linie nach Rechts unten zeichnen
                    int startX = i;
                    int startY = 0;
                    g2.drawLine((startX * 20) + 5, startY * 15, (sizeX * 20) + 5, ((sizeX - startX)) * 15);

                    //Am oberen Rand entlanggehen, linie nach Links unten zeichnen
                    g2.drawLine((startX * 20) - 10, startY * 15, -10, ((startX)) * 15);
                }

                for (int i = 0; i < sizeY; i++) {
                    //Am linken Rand entlang gehen. Line nach rechts unten ziehen
                    int startX = 0;
                    int startY = i;

                    g2.drawLine(startX * 20, (startY * 15) + 25, (sizeY - i) * 20, (sizeY * 15) + 25);
                }
            }
            if (renderBuildings) {
                // Gebäude
                renderBuildings(g2);
            }

            if (renderCursor) {
                //Cursor rendern
                renderCursor(g2);
            }


            if (renderObjects) {
                // Feste Objekte
                runRenderRoundFix(g2);
            }

            if (renderCreeps) {
                // Einheiten
                renderUnits(g2);
            }
            if (renderPicCursor) {
                // Nochmal, damit es über Einheiten schwebt (naja, leichter Pfusch)
                renderCursor(g2);
            }


        } else if (modi == 4) {
            // Debug-zeichnen
            paint_debug(g2);
        }
    }

    private void renderCursor(Graphics2D g2) {
        if (!renderPicCursor) {
            boolean[][] curArray = drawCursor.getFieldShadow();
            for (int x = 0; x < curArray.length; x++) {
                for (int y = 0; y < curArray[0].length; y++) {
                    if ((x + y) % 2 == 1) {
                        continue;
                    }
                    if (curArray[x][y]) {
                        g2.drawImage(imgMap.get("cur1").getImage(), (framePos.width + x) * 10, (int) ((framePos.height + y) * 7.5), null);
                    }
                }
            }

        } else {
            // Pic-Cursor
            // Einfach Bild an die gerasterte Cursorposition rendern.
            g2.drawImage(renderPic.getImage(), framePos.width * 10, (int) (framePos.height * 7.5), null);
        }
    }

    private void renderBuildings(Graphics2D g2) {
        // Zeichnet die Gebäude
        if (buildingList != null) {
            for (int i = 0; i < buildingList.size(); i++) {
                // Alle Gebäude durchgehen und zeichnen
                Building tempB = buildingList.get(i);
                // Textur holen
                String tex = tempB.getGraphicsData().getTexture();
                // Grafik aus ImgMap laden
                CoRImage tempImage = imgMap.get(tex);
                // Bild da?
                if (tempImage != null) {
                    // Ok, zeichnen
                    g2.drawImage(tempImage.getImage(), (tempB.getMainPosition().getX() - tempB.getGraphicsData().offsetX - positionX) * 10, (int) ((tempB.getMainPosition().getY() - tempB.getGraphicsData().offsetY - positionY) * 7.5), null);
                } else {
                    System.out.println("[RogGraphics][ERROR]: Image \"" + tex + "\" not found!");
                }
            }
        }
    }

    private void paint_debug(Graphics2D g2) {
        System.out.println("Printing Debug-Tree to 0,0");
        g2.drawImage(imgMap.get("img/fix/testtree1.png").getImage(), 0, 0, null);
    }

    public void changePaintCursor(MapEditorCursor rmec) {
        drawCursor = rmec;
    }

    private void renderUnits(Graphics2D g2) {
        selectionShadows = new ArrayList();
        if (unitList != null) {
            for (int i = 0; i < unitList.size(); i++) {
                // Liste durchgehen
                Unit unit = unitList.get(i);
                    // Einheit an aktuelle Position rendern
                    String tex = unit.getGraphicsData().getTexture();
                    CoRImage tempImage= imgMap.get(tex);
                    if (tempImage != null) {
                        // Einheit zeichnen
                        g2.drawImage(tempImage.getImage(), (unit.getMainPosition().getX() - positionX) * 10, (int) ((unit.getMainPosition().getY() - positionY) * 7.5), null);

                    } else {
                        System.out.println("[RogGraphics][ERROR]: Image \"" + tex + "\" not found!");
                    }
                
            }
        }
    }

    private void runRenderRoundGround(Graphics2D g2) {
        //System.out.println("This is runRenderRound, searching for <" + searchprop + "> mapsize(x,y) view [x,y]: (" + sizeX + "," + sizeY + ") [" + viewX + "," + viewY + "]");
        for (int x = 0; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0; y < sizeY && y < viewY; y = y + 2) {
                // X und Y durchlaufen, wenn ein Bild da ist, dann einbauen
                //              System.out.println("Searching for " + x + "," + y);
                String tex = null;
                try {
                    tex = visMap[x + positionX][y + positionY].getGround_tex();
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    // Kann beim Scrollein vorkommen - Einfach nichts zeichnen, denn da ist die Map zu Ende...
                }
                // Was da?
                if (tex != null) {
                    // Bild suchen und einfügen
                    CoRImage tempImage;
                    if (colMode) {
                        // SW-Modus
                        BufferedImage newTemp = grayImgMap.get(tex).getImage();
                        tempImage = new CoRImage(newTemp);
                    } else {
                        // Normalfall
                        tempImage = imgMap.get(tex);
                    }

                    if (tempImage != null) {
                        g2.drawImage(tempImage.getImage(), x * 10, (int) (y * 7.5), null);
                    } else {
                        System.out.println("[RME][ERROR]: Image \"" + tex + "\" not found!");
                    }

                }
                //  System.out.println(x + " " + y);
            }
        }
        for (int x = 0 + 1; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0 + 1; y < sizeY && y < viewY; y = y + 2) {
                // X und Y durchlaufen, wenn ein Bild da ist, dann einbauen
                // System.out.println("Searching for " + x + "," + y);
                String tex = null;
                try {
                    tex = visMap[x + positionX][y + positionY].getGround_tex();
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    // Siehe oben, egal weil hier die Map zu Ende ist...
                }
                // Was da?
                if (tex != null) {
                    // Bild suchen und einfügen
                    CoRImage tempImage;
                    if (colMode) {
                        // SW-Modus
                        BufferedImage newTemp = grayImgMap.get(tex).getImage();
                        tempImage =
                                new CoRImage(newTemp);
                    } else {
                        // Normalfall
                        tempImage = imgMap.get(tex);
                    }

                    if (tempImage != null) {
                        g2.drawImage(tempImage.getImage(), x * 10, (int) (y * 7.5), null);
                    } else {
                        System.out.println("[RME][ERROR]: Image \"" + tex + "\" not found!");
                    }

                }
            }
        }
    }

    private void runRenderRoundFix(Graphics2D g2) {
        //System.out.println("This is runRenderRound, searching for <" + searchprop + "> mapsize(x,y) view [x,y]: (" + sizeX + "," + sizeY + ") [" + viewX + "," + viewY + "]");
        for (int x = 0; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0; y < sizeY && y < viewY; y = y + 2) {
                // X und Y durchlaufen, wenn ein Bild da ist, dann einbauen
                //              System.out.println("Searching for " + x + "," + y);
                String tex = null;
                try {
                    tex = visMap[x + positionX][y + positionY].getFix_tex();
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    // Kann beim Scrollein vorkommen - Einfach nichts zeichnen, denn da ist die Map zu Ende...
                }
                // Was da?
                if (tex != null) {
                    // Bild suchen und einfügen
                    CoRImage tempImage;
                    if (colMode) {
                        // SW-Modus
                        BufferedImage newTemp = grayImgMap.get(tex).getImage();
                        tempImage = new CoRImage(newTemp);
                    } else {
                        // Normalfall
                        tempImage = imgMap.get(tex);
                    }

                    if (tempImage != null) {
                        g2.drawImage(tempImage.getImage(), x * 10, (int) (y * 7.5), null);
                    } else {
                        System.out.println("[RME][ERROR]: Image \"" + tex + "\" not found!");
                    }

                }
                //  System.out.println(x + " " + y);
            }
        }
        for (int x = 0 + 1; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0 + 1; y < sizeY && y < viewY; y = y + 2) {
                // X und Y durchlaufen, wenn ein Bild da ist, dann einbauen
                // System.out.println("Searching for " + x + "," + y);
                String tex = null;
                try {
                    tex = visMap[x + positionX][y + positionY].getFix_tex();
                } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                    // Siehe oben, egal weil hier die Map zu Ende ist...
                }
                // Was da?
                if (tex != null) {
                    // Bild suchen und einfügen
                    CoRImage tempImage;
                    if (colMode) {
                        // SW-Modus
                        BufferedImage newTemp = grayImgMap.get(tex).getImage();
                        tempImage =
                                new CoRImage(newTemp);
                    } else {
                        // Normalfall
                        tempImage = imgMap.get(tex);
                    }

                    if (tempImage != null) {
                        g2.drawImage(tempImage.getImage(), x * 10, (int) (y * 7.5), null);
                    } else {
                        System.out.println("[RME][ERROR]: Image \"" + tex + "\" not found!");
                    }

                }
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (modi == 1) {
            return new Dimension(defaultimage.getImage().getWidth(), defaultimage.getImage().getHeight()); // Größe vom Bild
        } else if (modi == 2) {
            // return new Dimension(12000,12000); // Nur ein TEST
            return new Dimension(sizeX * 10, (int) ((sizeY * 7.5) + 10)); // Echtes rendern
        } else { // Modi == 0;
            return new Dimension(100, 100); // Einfach irgendwas
        }

    }

    public void setFramePosition(Dimension td) {
        // Setzt die Position des Rahmens, inzwischen Editor&Game
        framePos = td;
        if (modi != 3) {
            repaint();
        }
    }

    public static BufferedImage grayScale(BufferedImage im) {
        // Verwandelt ein Bild ein eine Grayscale-Version, behält aber die Transparenz
        BufferedImage grayImage = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < im.getWidth(); x++) {
            for (int y = 0; y < im.getHeight(); y++) {
                int argb = im.getRGB(x, y);
                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = (argb) & 0xff;
                int l = (int) (.299 * r + .587 * g + .114 * b); //luminance
                grayImage.setRGB(x, y, (a << 24) + (l << 16) + (l << 8) + l);
            }

        }
        return grayImage;
    }

    public void setPicture(CoRImage b) {
        // Einfach ein Bild anzeigen - Auflösung wird automatisch justiert
        defaultimage = b;
        modi =
                1;
        this.repaint();
    }

    public void setVisMap(AbstractMapElement[][] newVisMap, int X, int Y) {
        // Einfach einsetzen
        visMap = newVisMap;
        sizeX = X;
        sizeY = Y;
    }

    public void changeVisMap(AbstractMapElement[][] newVisMap) {
        // Einfach einsetzen
        visMap = newVisMap;
        if (modi != 3) { // Im echten Rendern refreshed die Mainloop
            repaint();
        }
    }

    public void setImageMap(HashMap<String, CoRImage> newMap) {
        // Die Bilder, die verfügbar sind
        imgMap = newMap;
    }

    public void setMesh(boolean useMesh) {
        // Gitter anzeigen (Editor) oder nicht?
        renderMesh = useMesh;
        this.repaint();
    }

    public void setGround(boolean useGround) {
        // Boden anzeigen oder nicht?
        renderGround = useGround;
        this.repaint();
    }

    public void setPosition(int posX, int posY) {
        // Setzt die aktuell sichtbare Position
        positionX = posX;
        positionY =
                posY;
        if (modi != 3) { // Im echten Rendern refreshed die Mainloop
            repaint();
        }

    }

    public void setVisibleArea(int vX, int vY) {
        viewY = vY;
        viewX = vX;
        if (modi != 3) { // Im echten Rendern refreshed die Mainloop
            repaint();
        }

    }

    public void setObjects(boolean robj) {
        // Objekte - Häuser Bäume etc rendern oder net
        renderObjects = robj;
    }

    public void setCreeps(boolean rcreeps) {
        // Creeps rendern?
        renderCreeps = rcreeps;
    }

    public void setBuildings(boolean rbuildings) {
        // Gebäude rendern / Nur Editor
        renderBuildings = rbuildings;
    }

    public void setRessources(boolean rRessources) {
        // Ressourcen rendern
        renderRessources = rRessources;
    }

    public void setFogofwar(boolean rFow) {
        // Fog of War rendern
        renderFogOfWar = rFow;
    }

    public void setCursor(boolean rcursor) {
        renderCursor = rcursor;
        if (rcursor) {
            int[] pixels = new int[16 * 16];
            Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
            Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
            this.setCursor(transparentCursor);
        } else {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    public void startEditorRender() {
        // Jetzt keine dummen Sachen, sondern echten EditorContent rendern
        modi = 2;
        this.repaint();
        this.updateUI();
    }

    public void scrollUp() {
        // Ansicht ein Feld nach oben bewegen
        if (positionY > 1) {
            if (positionY == 1) {
                //positionY--;
            } else {
                positionY = positionY - 2;
            }
        }
        if (positionY % 2 == 1) {
            positionY++;
        }
        if (modi != 3) {
            repaint();
        }
    }

    public void scrollRight() {
        if (viewX < sizeX) { // Ans sonsten ist eh alles drin
            if (positionX < (sizeX - viewX)) {
                positionX = positionX + 2;
                if (modi != 3) {
                    repaint();
                }
            }

        }
    }

    public void scrollLeft() {
        if (positionX >= 1) {
            if (positionX == 1) {
                //positionX--;
            } else {
                positionX = positionX - 2;
            }
        }

        if (positionX % 2 == 1) {
            positionX--;
        }
        if (modi != 3) {
            repaint();
        }
    }

    public void scrollDown() {
        if (viewY < sizeY) { // Ansonsten ist eh alles drin, da brauch mer nicht scrollen...
            if (positionY < (sizeY - viewY)) {
                positionY = positionY + 2;
            }
            if (modi != 3) {
                repaint();
            }
        }
    }

    public Dimension getSelectedField(int selX, int selY) {
        // Findet heraus, welches Feld geklickt wurde - Man muss die Felder mittig anklicken, sonst gehts nicht
        // Wir haben die X und Y Koordinate auf dem Display und wollen die X und Y Koordinate auf der Map bekommen
        // Versatz beachten
        selX = selX - 10;
        selY = selY - 15;
        // Grundposition bestimmen
        int coordX = selX / 10;
        int coordY = (int) (selY / 7.5);
        // Scrollposition addieren
        coordX = coordX + positionX;
        coordY = coordY + positionY;
        boolean xg = (coordX % 2 == 0);
        boolean yg = (coordY % 2 == 0);
        if (xg != yg) {
            coordY--;
        }
        return new Dimension(coordX, coordY);
    }

    public Dimension getSpecialSelectedField(int selX, int selY) {
        // Position nicht hineinrechnen
        // Findet heraus, welches Feld geklickt wurde - Man muss die Felder mittig anklicken, sonst gehts nicht
        // Wir haben die X und Y Koordinate auf dem Display und wollen die X und Y Koordinate auf der Map bekommen
        // Versatz beachten
        selX = selX - 10;
        selY = selY - 15;
        // Grundposition bestimmen
        int coordX = selX / 10;
        int coordY = (int) (selY / 7.5);
        // Scrollposition addieren
        boolean xg = (coordX % 2 == 0);
        boolean yg = (coordY % 2 == 0);
        if (xg != yg) {
            coordY--;
        }
        return new Dimension(coordX, coordY);
    }

    public Dimension getGameSelectedField(int x, int y) {
        // Grundposition bestimmen
        // Versatz beachten
        x = x - 10;
        y = y - 15;
        int coordX = x / 10;
        int coordY = (int) (y / 7.5);
        // Scrollposition addieren
        coordX = coordX + positionX;
        coordY = coordY + positionY;
        // Keine ZwischenFelder
        boolean xg = (coordX % 2 == 0);
        boolean yg = (coordY % 2 == 0);
        if (xg != yg) {
            coordX++;
        }
        return new Dimension(coordX, coordY);
    }

    public int getModi() {
        // liefert den aktuellen renderModus zurück
        return modi;
    }

    public void setColMode(boolean cMode) {
        colMode = cMode;
        /* Rausgenommen, da es zu Performanceeinbußen führt, Greymaps werden jetzt beim start gecalct...
        if (cMode) {
        calcGreyMap();
        }
         * */
    }

    public void startDebugMode() {
        modi = 4;
        repaint();

    }

    public void calcGreyMap() {
        grayImgMap.clear();
        // Berechnet aus der imgMap Grayscale Bilder. Die kommmen in die grayMap
        ArrayList<CoRImage> tmpgrayList = new ArrayList<CoRImage>();
        tmpgrayList.addAll(imgMap.values());
        for (int i = 0; i < tmpgrayList.size(); i++) {
            // Jedes Bild umrechnen
            CoRImage trImg = new CoRImage(grayScale(tmpgrayList.get(i).getImage()));
            trImg.setImageName(tmpgrayList.get(i).getImageName());
            grayImgMap.put(trImg.getImageName(), trImg);
        }
    }

    protected void calcColoredMaps(Color[] colors) {
        // Berechnet die eingefärbten Texturen, z.B. für Gebäude
        ArrayList<CoRImage> tList = new ArrayList<CoRImage>();
        tList.addAll(coloredImgMap.values());
        coloredImgMap.clear();
        for (int i = 0; i < tList.size(); i++) {
            BufferedImage im = (BufferedImage) tList.get(i).getImage();
            for (int playerId = 0; playerId < colors.length; playerId++) {
                BufferedImage preImg = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < im.getWidth(); x++) {
                    for (int y = 0; y < im.getHeight(); y++) {
                        // Das ganze Raster durchgehen und Farben ersetzen
                        // Ersetzfarbe da?
                        int rgb = im.getRGB(x, y);

                        float[] col = Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, null);
                        if (col[0] >= 0.8583333f && col[0] <= 0.8666666f) {
                            // Ja, ersetzen
                            Color tc = colors[playerId];
                            int targetC = Color.HSBtoRGB(Color.RGBtoHSB(tc.getRed(), tc.getGreen(), tc.getBlue(), null)[0], 1.0f, col[2]);
                            int a = (rgb >> 24) & 0xff;
                            targetC = (targetC & (~(0xff << 24)) | (a << 24));
                            preImg.setRGB(x, y, targetC);

                        } else {
                            preImg.setRGB(x, y, im.getRGB(x, y));
                        }
                    }
                }
                // Bild berechnet, einfügen
                CoRImage newImg = new CoRImage(preImg);
                newImg.setImageName(tList.get(i).getImageName());
                coloredImgMap.put(newImg.getImageName() + playerId, newImg);
            }

        }
    }

    public void setColModeImage(CoRImage img) {
        colModeImage = img;
        BufferedImage temp = img.getImage();
        // BufferedImage tempNew = new BufferedImage();
    }

    public void enableAntialising() {
        // Schaltet die Kantenglättung an
        useAntialising = true;
    }

    public void updateUnits(java.util.List<Unit> uL) {
        unitList = uL;
        if (modi != 3) {
            repaint();
        }
    }

    public void updateBuildings(java.util.List<Building> bL) {
        buildingList = bL;
        if (modi != 3) {
            repaint();
        }
    }

    protected void resortAllList() {
        // Sortiert die Liste neu, ist super, braucht aber leider viel Leistung, wird deshalb nicht bei jedem Frame aufgerufen
        Collections.sort(allList);
    }

    public GraphicsComponent() {
        cgc = getGraphicsConfiguration();
        interSelFields = new ArrayList<GameObject[]>();
        grayImgMap = new HashMap<String, CoRImage>();
        // Fonts initialisieren
        fonts = new Font[5];
        fonts[0] = Font.decode("Mono-10");
        fonts[1] = Font.decode("Mono-Bold-12");
        fonts[2] = Font.decode("Mono-Bold-10");
        fonts[3] = Font.decode("Mono-Italic-10");
        fonts[4] = Font.decode("Mono-8");

        optList = new ArrayList<Ability>();
    }


}
