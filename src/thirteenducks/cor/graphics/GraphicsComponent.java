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
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.tools.mapeditor.MapEditorCursor;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.game.ability.AbilityIntraManager;
import thirteenducks.cor.game.ability.AbilityRecruit;
import thirteenducks.cor.game.ability.AbilityBuild;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.graphics.GraphicsFogOfWarPattern;
import thirteenducks.cor.graphics.GraphicsRenderable;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.MemoryImageSource;
import java.awt.image.RescaleOp;
import java.awt.image.VolatileImage;
import java.util.*;
import javax.swing.JLabel;
import javax.imageio.*;
import java.io.*;
import javax.swing.SwingUtilities;
import org.apache.commons.collections.buffer.PriorityBuffer;
import thirteenducks.cor.map.CoRMapElement;

public class GraphicsComponent extends JLabel {
    // Diese Klasse repräsentiert den Tatsächlichen GrafikINHALT von RogGraphics und RogMapEditor

    CoRImage defaultimage;
    CoRImage colModeImage;
    Color color;
    protected int modi = 0; // Was gerendert werden soll, spezielle Ansichten für den Editor etc...
    CoRMapElement[][] visMap; // Die angezeigte Map
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
    java.util.List<Ressource> resList; // Ressourcenliste
    Vector<GraphicsRenderable> allList;
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
    private VolatileImage tempImg;
    private BufferedImage savetempImg;
    private VolatileImage hudGround; // Hud-Hauptbild, hängt von epoche ab
    private BufferedImage savehudGround;
    private VolatileImage fowMiniLayer;  // FoW der Minimap
    private BufferedImage savefowMiniLayer;
    private VolatileImage fowMiniLayer2;  // FoW der Minimap
    private BufferedImage savefowMiniLayer2;
    protected int epoche = 2; // Null ist keine Epoche, also kein Gescheites Grundbild..
    int hudX; // X-Koordinate vom Hud, damits nicht dauernd neu ausgerechnet werden muss...
    int hudSizeX; // Hängt vom oberen ab
    private VolatileImage miniMap; // Hud-Minimap
    private BufferedImage saveminiMap;
    private ClientCore.InnerClient rgi;
    private int miniMapViewSizeX = 10;
    private int miniMapViewSizeY = 10;
    Dimension boxselectionstart;
    protected boolean dragSelectionBox = false;
    BufferedImage wayPointHighlighting[];
    private static final boolean beautyDraw = true;     // Schöner zeichnen durch umsortieren der allList, kostet Leistung
    BufferedImage[] huds;                               // Die Huds für verschiedene Epochen
    int lastMenuHash;                                   // Das Hud-Menü wird nur neu gezeichnet, wenn sich der Hash der selektieren Einheiten verändert.
    ArrayList<GameObject[]> interSelFields;          // Die Selektierten Einheiten als Representation auf dem Hud
    BufferedImage[] scaledBuildingLocation;             // Die Größe eines Gebäudes, in skalierter Version
    VolatileImage buildingLayer;                        // Layer für Gebäude auf der Minimap
    BufferedImage savebuildingLayer;
    double maxminscaleX;
    double maxminscaleY;
    boolean updateInterHud = false;
    int mouseX;                                         // Die Position der Maus, muss geupdatet werden
    int mouseY;                                         // Die Position der Maus, muss geupdatet werden
    Font[] fonts;                                       // Die Fonts, die häufig benötigt werden
    boolean pauseMode = false;                          // Pause-Modus
    public static final float[] BLUR3x3 = { // Parameter für den Blur-Filter der Minimap
        0.1f, 0.1f, 0.1f, // low-pass filter kernel
        0.1f, 0.3f, 0.1f,
        0.1f, 0.1f, 0.1f
    };
    long pause;                                         // Zeitpunkt der letzen Pause
    boolean saveMode = false;                           // Sicherer Grafikmodus, verhintert Blackscreens, verwendet keine Volatiles...
    boolean coordMode = false;                          // Modus, der die Koordinaten der Felder mit anzeigt.
    boolean idMode = false;                             // Modus, der die NetIds von Einheiten/Gebäuden und Ressourcen mit anzeigt.
    boolean smoothGround = false;                       // Grobe Ecken aus der Bodentextur rausrechnen
    ArrayList<Ability> optList;            // Liste der derzeit anklickbaren Fähigkeiten
    GameObject tempInfoObj;                          // Über dieses Objekt werden Temporär Infos angezeigt.
    byte[][] fowmap;                                    // Fog of War - Map
    BufferedImage[] fowImgs;                            // 0 = Ganz schwarz; 1 = leicht schwarz
    GraphicsFogOfWarPattern fowpatmgr;                  // Managed und erstellt Fow-Pattern
    boolean updateBuildingsFow = false;                 // Lässt den Gebäude-Fow neu erstellen
    int loadStatus = 0;
    boolean loadWait = false;
    BufferedImage loadScreen;                           // Ladebild

    @Override
    public void paintComponent(Graphics g) {
        //Die echte, letzendlich gültige paint-Methode, sollte nicht direkt aufgerufen werden
        // verwendet advanced Grafiktechniken, also Graphics2D Api verwenden
        Graphics2D g2 = (Graphics2D) g;
        if (modi == -1) {
            // Ladebildschirm (pre-Game)
            renderLoadScreen(g2);
        } else if (modi == 0) {
            // Gar nix rendern
        } else if (modi == 1) {
            // Bestimmted Bild (defaultimage) rendern
            g2.drawImage(defaultimage.getImage(), 0, 0, null);
        } else if (modi == 2) {
            g2.setColor(Color.BLACK);
            // Editor
            // Bodentexturen rendern

            if (renderGround) {
                // OK, Boden rendern!
                runRenderRound("ground_tex", g2);
            }
            // Mesh rendern?
            if (renderMesh) {
                g2.setColor(Color.BLACK);
                // Mesh rendern
                for (int i = 0; i < sizeX; i++) {
                    //Am oberen Rand entlanggehen, linie nach Rechts unten zeichnen
                    int startX = i;
                    int startY = 0;
                    g2.drawLine((startX * 40) + 5, startY * 30, (sizeX * 40) + 5, ((sizeX - startX)) * 30);

                    //Am oberen Rand entlanggehen, linie nach Links unten zeichnen
                    g2.drawLine((startX * 40) - 10, startY * 30, -10, ((startX)) * 30);
                }

                for (int i = 0; i < sizeY; i++) {
                    //Am linken Rand entlang gehen. Line nach rechts unten ziehen
                    int startX = 0;
                    int startY = i;

                    g2.drawLine(startX * 40, (startY * 30) + 25, (sizeY - i) * 40, (sizeY * 30) + 25);
                }
            }

            if (renderRessources) {
                // Ressourcen
                renderRessources(g2);
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
                runRenderRound("fix_tex", g2);
            }

            if (renderCreeps) {
                // Einheiten
                if (!newMode) {
                    runRenderRound("unit_tex", g2);
                } else {
                    renderUnits(g2);
                }
            }
            if (renderPicCursor) {
                // Nochmal, damit es über Einheiten schwebt (naja, leichter Pfusch)
                renderCursor(g2);
            }

            // Mesh rendern fertig, oder gar nicht gemacht
            if (colMode) {
                // Kollision drauf rendern
                renderCol(g2);
            }


        } else if (modi == 3) {
            try {
                if (pauseMode) {
                    // Pause für diesen Frame freischalten:
                    pause = rgi.rogGraphics.getPauseTime();
                }
                // FoW updaten?
                if (updateBuildingsFow) {
                    updateBuildingsFow = false;
                    this.updateBuildingFoW();
                }
                // Für die Haupt-grafikausgabe
                if (useAntialising) {
                    RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.addRenderingHints(rh);
                }
                Graphics2D g3 = null;
                if (saveMode) {
                    g3 = savetempImg.createGraphics();
                } else {
                    g3 = tempImg.createGraphics();
                }
                renderBackground(g3);
                if (beautyDraw) {
                    try {
                        Collections.sort(allList);
                    } catch (Exception ex) {
                        System.out.println("FixMe: SortEX");
                    }
                }
                //renderUnits(g3);
                //renderBuildings(g3);
                renderBuildingWaypoint(g3);

                synchronized (this) {
                    renderBuildingMarkers(g3);
                    renderGraphicElements(g3);
                }
                // Fog of War rendern, falls aktiv
                if (renderFogOfWar) {
                    renderFogOfWar(g3);
                }
                if (renderPicCursor) {
                    renderCursor(g3);
                }
                if (this.dragSelectionBox) {
                    renderSelBox(g3);
                }
                if (displayFrameRate) {
                    renderFrameRate(g3);
                }
                if (idMode) {
                    renderIds(g3);
                }
                renderHud(g3);
                // Mouse-Hover rendern
                if (pauseMode) {
                    renderPause(g3);
                } else {
                    renderMouseHover(g3);
                }
                g3.setColor(Color.DARK_GRAY);
                g3.setFont(Font.decode("Sans-BOLD"));
                g3.drawString("Centuries of Rage - pre-Alpha 2", 10, 10);
                if (colMode) {
                    this.renderCol(g3);
                }
                if (coordMode) {
                    renderCoords(g3);
                }
                if (saveMode) {
                    g2.drawImage(savetempImg, 0, 0, null);
                } else {
                    g2.drawImage(tempImg, 0, 0, null);
                }
                // Fertig, Volatiles prüfen
                if (!saveMode) {
                    checkVolatile();
                }

            } catch (Exception ex) {
                System.out.println("Error while rendering frame, dropping.");
                ex.printStackTrace();
            }
            // Pause zurücksetzten
            pause = 0;
        } else if (modi == 4) {
            // Debug-zeichnen
            paint_debug(g2);
        }
    }

    /**
     * Zeichnet NetIds von allen Einheiten / Gebäuden / Ressourcen
     * Zum Debugen. Funktioniert nur, wenn die Debug-Tools (cheats) aktiviert sind
     * @param g2
     */
    private void renderIds(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        g2.setFont(fonts[4]);
        // Einheiten durchgehen
        for (int i = 0; i < unitList.size(); i++) {
            Unit unit = unitList.get(i);
            if (unit.isMoving()) {
                Position pos = unit.getMovingPosition(rgi, positionX, positionY);
                g2.drawString(String.valueOf(unit.netID), pos.X + 10, pos.Y + 28);
            } else {
                g2.drawString(String.valueOf(unit.netID), (unit.position.X - positionX) * 20 + 10, (unit.position.Y - positionY) * 15 + 28);
            }
        }
        // Gebäude
        for (int i = 0; i < buildingList.size(); i++) {
            Building building = buildingList.get(i);
            g2.drawString(String.valueOf(building.netID), (building.position.X - positionX) * 20 + 10, (building.position.Y - positionY) * 15 + 28);
        }
        // Ressourcen
        for (int i = 0; i < resList.size(); i++) {
            Ressource res = resList.get(i);
            g2.drawString(String.valueOf(res.netID), (res.position.X - positionX) * 20 + 10, (res.position.Y - positionY) * 15 + 28);
        }
    }

    private void renderLoadScreen(Graphics2D g2) {
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2.addRenderingHints(rh);
        // Löschen
        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, realPixX, realPixY);

        // Ladebild
        if (loadScreen != null) {
            // Auf Breite skalieren
            double scale = 1.0 * realPixY / loadScreen.getHeight();
            // Seitenverhältnis beibehalten
            int xSize = (int) (loadScreen.getWidth() * scale);
            int ySize = (int) (loadScreen.getHeight() * scale);
            // Mittig anzeigen
            int xOffset = (xSize - realPixX) / 2;
            g2.drawImage(loadScreen, 0 - xOffset, 0, xSize - xOffset, ySize, 0, 0, loadScreen.getWidth(), loadScreen.getHeight(), null);
        }

        g2.setColor(Color.BLACK);
        // Ladebalken zeigen
        int lx = realPixX / 4;      // Startposition des Balkens
        int ly = realPixY / 10 * 9;
        int dx = realPixX / 2;      // Länge des Balkens
        int dy = realPixY / 32;
        // Rahmen ziehen
        g2.drawRect(lx - 2, ly - 2, dx + 3, dy + 3);
        // Balken zeichnen
        g2.setColor(new java.awt.Color(186, 0, 0));
        g2.fillRect(lx, ly, dx * loadStatus / 10, dy);
        // Status drunter anzeigen
        g2.setColor(Color.BLACK);
        String status = "Status: ";
        if (!loadWait) {
            switch (loadStatus) {
                case 0:
                    status = status + "waiting for server...";
                    break;
                case 1:
                    status = status + "receiving gamesettings (phase 1 of 2)...";
                    break;
                case 2:
                    status = status + "receiving gamesettings (phase 2 of 2)...";
                    break;
                case 3:
                    status = status + "computing received gamesettings...";
                    break;
                case 4:
                    status = status + "loading graphics (phase 1 of 2): importing images & textures";
                    break;
                case 5:
                    status = status + "loading graphics (phase 2 of 2): importing animations";
                    break;
                case 6:
                    status = status + "searching map...";
                    break;
                case 7:
                    status = status + "downloading map from server...";
                    break;
                case 8:
                    status = status + "loading map...";
                    break;
                case 9:
                    status = status + "preparing start...";
                    break;
                case 10:
                    status = status + "Centuries of Rage completely loaded! Have a lot of fun...";
                    break;
            }
        } else {
            switch (loadStatus) {
                case 5:
                    status = status + "waiting for other players to complete loading settings and graphics...";
                    break;
                case 8:
                    status = status + "waiting for other players to complete loading map...";
                    break;
                case 9:
                    status = status + "waiting for other players to finish preparations...";
                    break;
            }
        }
        g2.drawString(status, lx, (int) (ly + (dy * 1.7)));
    }

    private void renderBuildingWaypoint(Graphics2D g2) {
        // Gebäude-Sammelpunkt zeichnen
        if (!rgi.rogGraphics.inputM.selected.isEmpty() && rgi.rogGraphics.inputM.selected.get(0).getClass().equals(Building.class)) {
            Building b = (Building) rgi.rogGraphics.inputM.selected.get(0);
            if (b.waypoint != null) {
                // Dahin rendern
                CoRImage image = coloredImgMap.get("img/game/building_defaulttarget.png" + b.playerId);
                if (image != null) {
                    g2.drawImage(image.getImage(), ((b.waypoint.X - positionX) * 20) - 40, ((b.waypoint.Y - positionY) * 15) - 40, null);
                } else {
                    System.out.println("ERROR: Textures missing (34124)");
                }
            }
        }
    }

    private void renderFogOfWar(Graphics2D g2) {
        // Rendert den Fog of War. Liest ihn dazu einfach aus der fowmap aus.
        // Das Festlegen der fog-Map wird anderswo erledigt...
        // Murks, aber die position müssen gerade sein...
        if (positionX % 2 == 1) {
            positionX--;
        }
        if (positionY % 2 == 1) {
            positionY--;
        }
        for (int x = 0; x < (sizeX) && x < (viewX); x = x + 2) {
            for (int y = 0; y < (sizeY) && y < (viewY + 2); y = y + 2) {
                // Hat dieses Feld Kollision?
                try {
                    byte fow = fowmap[x + positionX][y + positionY - 2];
                    if (fow < 2) {
                        g2.drawImage(fowImgs[fow], x * 20, (y - 2) * 15, null);
                    }
                } catch (ArrayIndexOutOfBoundsException ar) {
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        for (int x = 1; x < (sizeX) && x < (viewX + 4); x = x + 2) {
            for (int y = 1; y < (sizeY) && y < (viewY + 2); y = y + 2) {
                // Hat dieses Feld Kollision?
                try {
                    byte fow = fowmap[x + positionX - 2][y + positionY - 2];
                    if (fow < 2) {
                        g2.drawImage(fowImgs[fow], ((x - 2) * 20), ((y - 2) * 15), null);
                    }
                } catch (ArrayIndexOutOfBoundsException ar) {
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void renderPause(Graphics2D g2) {
        // PAUSE - Overlay rendern
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(realPixX / 3, realPixY / 3, realPixX / 3, realPixY / 5);
        g2.setColor(Color.black);
        g2.setFont(Font.decode("Sans-Bold-30"));
        g2.drawString("P A U S E", realPixX / 2 - 80, realPixY / 2 - 30);
        g2.drawRect(realPixX / 3, realPixY / 3, realPixX / 3, realPixY / 5);
    }

    private void renderRessources(Graphics2D g2) {
        // Zeichnet die Ressourcen - EDITOR ONLY
        // Liste durchgehen und einfach zeichnen, recht easy
        for (Ressource res : resList) {
            CoRImage img = null;
            if (colMode) {
                img = grayImgMap.get(res.getTex());
            } else {
                img = imgMap.get(res.getTex());
            }
            if (img != null) {
                if (res.position != null) {
                    if (res.getType() < 3) {
                        // Einfach hinzeichnen:
                        g2.drawImage(img.getImage(), (res.position.X - positionX) * 20, (res.position.Y - positionY) * 15, null);
                    } else {
                        // Größere Bilder, eins weiter oben zeichnen
                        g2.drawImage(img.getImage(), (res.position.X - positionX) * 20, (res.position.Y - positionY - 2) * 15, null);
                    }
                }
            }
        }
    }

    private void renderBuildingMarkers(Graphics2D g2) {
        // Spielermarkierungen für Gebäude - neueres System
        for (int i = 0; i < buildingList.size(); i++) {
            Building b = buildingList.get(i);
            // Startkoordinaten
            int x = (b.position.X - positionX) * 20;
            int y = ((b.position.Y - positionY) * 15) + 25;
            // Linien ziehen
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.drawLine(x, y, x + (b.z1 * 20), y - (b.z1 * 15));
            g2.drawLine(x, y, x + (b.z2 * 20), y + (b.z2 * 15));
            g2.drawLine(x + (b.z1 * 20), y - (b.z1 * 15), x + (b.z1 * 20) + (b.z2 * 20), y - (b.z1 * 15) + (b.z2 * 15));
            g2.drawLine(x + (b.z2 * 20), y + (b.z2 * 15), x + (b.z1 * 20) + (b.z2 * 20), y - (b.z1 * 15) + (b.z2 * 15));
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void renderMouseHover(Graphics2D g2) {
        // Muss überhaupt was gezeichnet werden?
        if (mouseX > hudX && rgi.rogGraphics.clickedInOpt(mouseX, mouseY)) {
            // Zugehörige Ability finden:
            Ability ability = this.searchOptFast();
            String[] missing = null;
            if (ability != null) {
                int hovX = mouseX - 200;
                int hovY = mouseY - 80;
                // Y-Größe festlegen
                if (ability.type == Ability.ABILITY_BUILD) {
                    if (ability.isAvailable()) {
                        if (ability.showCosts) {
                            hovY = mouseY - 101;
                        } else {
                            hovY = mouseY - 86;
                        }
                    } else {
                        missing = ability.getMissingDependencies();
                        if (ability.showCosts) {
                            hovY = mouseY - 101 - ((missing.length + ability.getNumberOfMissingResources()) * 12);
                        } else {
                            hovY = mouseY - 86 - (missing.length * 12);
                        }
                    }
                } else if (ability.type == Ability.ABILITY_RECRUIT) {
                    if (ability.isAvailable()) {
                        if (ability.showCosts) {
                            hovY = mouseY - 125;
                        } else {
                            hovY = mouseY - 110;
                        }
                    } else {
                        missing = ability.getMissingDependencies();
                        if (ability.showCosts) {
                            hovY = mouseY - 125 - ((missing.length + ability.getNumberOfMissingResources()) * 12);
                        } else {
                            hovY = mouseY - 110 - (missing.length * 12);
                        }
                    }
                } else if (ability.type == Ability.ABILITY_UPGRADE) {
                    if (ability.isAvailable()) {
                        if (ability.showCosts || ability.alreadyUsed) {
                            hovY = mouseY - 73;
                        } else {
                            hovY = mouseY - 58;
                        }
                    } else {
                        missing = ability.getMissingDependencies();
                        if (ability.showCosts) {
                            hovY = mouseY - 73 - ((missing.length + ability.getNumberOfMissingResources()) * 12);
                        } else {
                            hovY = mouseY - 58 - (missing.length * 12);
                        }
                    }
                    hovX = mouseX - 250;
                } else if (ability.type == Ability.ABILITY_INTRA) {
                    hovY = mouseY - 60;
                    hovX = mouseX - 200;
                } else {
                    hovY = mouseY - 97;
                    hovX = mouseX - 235;
                }
                // Hover-Fenster rendern:
                g2.setColor(Color.LIGHT_GRAY);
                g2.fillRect(hovX, hovY, mouseX - hovX, mouseY - hovY);
                g2.setColor(Color.BLACK);
                g2.drawRect(hovX, hovY, mouseX - hovX, mouseY - hovY);
                // Erst Name der Ability
                g2.setFont(fonts[1]);
                g2.setColor(Color.BLACK);
                g2.drawString(ability.name, hovX + 2, hovY + 13);
                g2.setFont(fonts[0]);
                // Jetzt je nach Typ weiter:
                if (ability.type == Ability.ABILITY_BUILD) {
                    AbilityBuild abb = (AbilityBuild) ability;
                    // Dauer:
                    g2.drawString("Duration: " + GraphicsComponent.transformTime(abb.duration), hovX + 2, hovY + 25);
                    Building building = rgi.mapModule.getDescBuilding(abb.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
                    // Truppenlimit
                    boolean limitOk = true;
                    if (building.limit > 0) {
                        if (rgi.game.getOwnPlayer().freeLimit() < building.limit) {
                            g2.setColor(Color.RED);
                            limitOk = false;
                        }
                        g2.drawString("Requires: " + building.limit, hovX + 82, hovY + 25);
                    } else if (building.limit < 0) {
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Increases limit by: " + String.valueOf(building.limit).substring(1), hovX + 82, hovY + 25);
                    }
                    g2.setColor(Color.BLACK);
                    if (ability.showCosts) {
                        // Kosten:
                        Color green = new Color(3, 100, 0);
                        int xposition = 3;
                        //FOOD
                        if (ability.costs[0] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res1.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res1 >= ability.costs[0]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[0]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //WOOD
                        if (ability.costs[1] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res2.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res2 >= ability.costs[1]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[1]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //METAL
                        if (ability.costs[2] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res3.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res3 >= ability.costs[2]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[2]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //GOLD
                        if (ability.costs[3] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res4.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res4 >= ability.costs[3]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[3]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //OIL
                        if (ability.costs[4] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res5.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res5 >= ability.costs[4]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[4]), hovX + xposition + 12, hovY + 40);
                        }
                        hovY += 15;
                    }
                    g2.setColor(Color.BLACK);
                    g2.drawLine(hovX, hovY + 28, mouseX, hovY + 28);
                    // Infos über das Gebäude
                    // Name:
                    g2.setFont(fonts[2]);
                    g2.drawString(building.name, hovX + 2, hovY + 42);
                    // Beschreibung
                    g2.setFont(fonts[0]);
                    g2.drawString(building.Gdesc, hovX + 2, hovY + 58);
                    // HP
                    g2.drawString("HP: " + building.getMaxhitpoints(), hovX + 2, hovY + 70);
                    // Absatz
                    g2.drawLine(hovX, hovY + 73, mouseX, hovY + 73);
                    // Verfügbar?
                    g2.setFont(fonts[2]);
                    if (ability.isAvailable()) {
                        // Dunkelgrün
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Available, click to build.", hovX + 2, hovY + 84);
                    } else {
                        g2.setColor(Color.RED);
                        g2.drawString("Not available, requires:", hovX + 2, hovY + 84);
                        if (missing != null) {
                            int i = 0;
                            for (; i < missing.length; i++) {
                                String obj = missing[i];
                                g2.drawString("- " + obj, hovX + 2, hovY + 84 + (12 * (i + 1)));
                            }
                            int d1 = ability.costs[0] - rgi.game.getOwnPlayer().res1;
                            int d2 = ability.costs[1] - rgi.game.getOwnPlayer().res2;
                            int d3 = ability.costs[2] - rgi.game.getOwnPlayer().res3;
                            int d4 = ability.costs[3] - rgi.game.getOwnPlayer().res4;
                            int d5 = ability.costs[4] - rgi.game.getOwnPlayer().res5;
                            if (d1 > 0) {
                                g2.drawString("- another " + d1 + " Food", hovX + 2, hovY + 84 + (12 * (i + 1)));
                                i++;
                            }
                            if (d2 > 0) {
                                g2.drawString("- another " + d2 + " Wood", hovX + 2, hovY + 84 + (12 * (i + 1)));
                                i++;
                            }
                            if (d3 > 0) {
                                g2.drawString("- another " + d3 + " Metal", hovX + 2, hovY + 84 + (12 * (i + 1)));
                                i++;
                            }
                            if (d4 > 0) {
                                g2.drawString("- another " + d4 + " Gold", hovX + 2, hovY + 84 + (12 * (i + 1)));
                                i++;
                            }
                            if (d5 > 0) {
                                g2.drawString("- another " + d5 + " Oil", hovX + 2, hovY + 84 + (12 * (i + 1)));
                                i++;
                            }
                            if (!limitOk) {
                                g2.drawString("- more Houses", hovX + 2, hovY + 84 + (12 * (i + 1)));
                            }
                        }
                    }
                } else if (ability.type == Ability.ABILITY_RECRUIT) {
                    AbilityRecruit abr = (AbilityRecruit) ability;
                    // Dauer:
                    g2.drawString("Duration: " + GraphicsComponent.transformTime(abr.duration), hovX + 2, hovY + 25);
                    Unit unit = rgi.mapModule.getDescUnit(abr.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
                    // Truppenlimit
                    boolean limitOk = true;
                    if (unit.limit > 0) {
                        if (rgi.game.getOwnPlayer().freeLimit() < unit.limit) {
                            g2.setColor(Color.RED);
                            limitOk = false;
                        }
                        g2.drawString("Requires: " + unit.limit, hovX + 82, hovY + 25);
                    } else if (unit.limit < 0) {
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Increases limit by: " + String.valueOf(unit.limit).substring(1), hovX + 82, hovY + 25);
                    }
                    if (ability.showCosts) {
                        // Kosten:
                        Color green = new Color(3, 100, 0);
                        int xposition = 3;
                        //FOOD
                        if (ability.costs[0] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res1.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res1 >= ability.costs[0]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[0]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //WOOD
                        if (ability.costs[1] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res2.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res2 >= ability.costs[1]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[1]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //METAL
                        if (ability.costs[2] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res3.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res3 >= ability.costs[2]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[2]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //GOLD
                        if (ability.costs[3] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res4.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res4 >= ability.costs[3]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[3]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //OIL
                        if (ability.costs[4] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res5.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res5 >= ability.costs[4]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[4]), hovX + xposition + 12, hovY + 40);
                        }
                        hovY += 15;
                    }
                    g2.setColor(Color.BLACK);
                    g2.drawLine(hovX, hovY + 28, mouseX, hovY + 28);
                    // Infos über die Einheit:

                    // Name:
                    g2.setFont(fonts[2]);
                    g2.drawString(unit.name, hovX + 2, hovY + 42);
                    // Beschreibung
                    g2.setFont(fonts[0]);
                    g2.drawString(unit.Gdesc, hovX + 2, hovY + 58);
                    // HP
                    g2.drawString("HP: " + unit.getMaxhitpoints(), hovX + 2, hovY + 70);
                    // Stark / Schwach gegen...
                    g2.drawString("Strong vs: " + unit.Gpro, hovX + 2, hovY + 82);
                    g2.drawString("Weak vs: " + unit.Gcon, hovX + 2, hovY + 94);
                    // Trennlinie
                    g2.drawLine(hovX, hovY + 97, mouseX, hovY + 97);
                    // Verfügbar?
                    g2.setFont(fonts[2]);
                    if (ability.isAvailable()) {
                        // Dunkelgrün
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Available, click to build", hovX + 2, hovY + 108);
                    } else {
                        g2.setColor(Color.RED);
                        g2.drawString("Not available, requires:", hovX + 2, hovY + 108);
                        if (missing != null) {
                            int i = 0;
                            for (; i < missing.length; i++) {
                                String obj = missing[i];
                                g2.drawString("- " + obj, hovX + 2, hovY + 108 + (12 * (i + 1)));
                            }
                            int d1 = ability.costs[0] - rgi.game.getOwnPlayer().res1;
                            int d2 = ability.costs[1] - rgi.game.getOwnPlayer().res2;
                            int d3 = ability.costs[2] - rgi.game.getOwnPlayer().res3;
                            int d4 = ability.costs[3] - rgi.game.getOwnPlayer().res4;
                            int d5 = ability.costs[4] - rgi.game.getOwnPlayer().res5;
                            if (d1 > 0) {
                                g2.drawString("- another " + d1 + " Food", hovX + 2, hovY + 108 + (12 * (i + 1)));
                                i++;
                            }
                            if (d2 > 0) {
                                g2.drawString("- another " + d2 + " Wood", hovX + 2, hovY + 108 + (12 * (i + 1)));
                                i++;
                            }
                            if (d3 > 0) {
                                g2.drawString("- another " + d3 + " Metal", hovX + 2, hovY + 108 + (12 * (i + 1)));
                                i++;
                            }
                            if (d4 > 0) {
                                g2.drawString("- another " + d4 + " Gold", hovX + 2, hovY + 108 + (12 * (i + 1)));
                                i++;
                            }
                            if (d5 > 0) {
                                g2.drawString("- another " + d5 + " Oil", hovX + 2, hovY + 108 + (12 * (i + 1)));
                                i++;
                            }
                            if (!limitOk) {
                                g2.drawString("- more Houses", hovX + 2, hovY + 108 + (12 * (i + 1)));
                            }
                        }
                    }
                } else if (ability.type == Ability.ABILITY_MOVE) {
                } else if (ability.type == Ability.ABILITY_UPGRADE) {
                    AbilityUpgrade abu = (AbilityUpgrade) ability;
                    // Dauer:
                    g2.drawString("Duration: " + GraphicsComponent.transformTime(abu.duration), hovX + 2, hovY + 25);
                    if (ability.showCosts) {
                        // Kosten:
                        Color green = new Color(3, 100, 0);
                        int xposition = 3;
                        //FOOD
                        if (ability.costs[0] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res1.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res1 >= ability.costs[0]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[0]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //WOOD
                        if (ability.costs[1] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res2.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res2 >= ability.costs[1]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[1]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //METAL
                        if (ability.costs[2] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res3.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res3 >= ability.costs[2]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[2]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //GOLD
                        if (ability.costs[3] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res4.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res4 >= ability.costs[3]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[3]), hovX + xposition + 12, hovY + 40);
                            xposition += 39;
                        }
                        //OIL
                        if (ability.costs[4] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res5.png").getImage(), hovX + xposition, hovY + 31, this);
                            if (rgi.game.getOwnPlayer().res5 >= ability.costs[4]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.RED);
                            }
                            g2.drawString(String.valueOf(ability.costs[4]), hovX + xposition + 12, hovY + 40);
                        }
                        hovY += 15;
                    }
                    // Beschreibung:
                    g2.setColor(Color.BLACK);
                    g2.setFont(fonts[2]);
                    if (abu.gdesc != null) {
                        g2.drawString(abu.gdesc, hovX + 2, hovY + 42);
                    } else {
                        g2.drawString("No describtion available!", hovX + 2, hovY + 42);
                    }
                    // Trennlinie
                    g2.drawLine(hovX, hovY + 45, mouseX, hovY + 45);
                    // Verfügbar?
                    g2.setFont(fonts[2]);
                    if (ability.isAvailable()) {
                        // Dunkelgrün
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Available, click to upgrade.", hovX + 2, hovY + 56);
                    } else {
                        // Warum net, vllt weil schon verwendet?
                        if (ability.alreadyUsed) {
                            g2.setColor(Color.RED);
                            g2.drawString("Not available, already used.", hovX + 2, hovY + 56);
                        } else {
                            g2.setColor(Color.RED);
                            g2.drawString("Not available, requires:", hovX + 2, hovY + 56);
                            if (missing != null) {
                                int i = 0;
                                for (; i < missing.length; i++) {
                                    String obj = missing[i];
                                    g2.drawString("- " + obj, hovX + 2, hovY + 56 + (12 * (i + 1)));
                                }
                                int d1 = ability.costs[0] - rgi.game.getOwnPlayer().res1;
                                int d2 = ability.costs[1] - rgi.game.getOwnPlayer().res2;
                                int d3 = ability.costs[2] - rgi.game.getOwnPlayer().res3;
                                int d4 = ability.costs[3] - rgi.game.getOwnPlayer().res4;
                                int d5 = ability.costs[4] - rgi.game.getOwnPlayer().res5;
                                if (d1 > 0) {
                                    g2.drawString("- annother " + d1 + " Food", hovX + 2, hovY + 56 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d2 > 0) {
                                    g2.drawString("- another " + d2 + " Wood", hovX + 2, hovY + 56 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d3 > 0) {
                                    g2.drawString("- antoher " + d3 + " Metal", hovX + 2, hovY + 56 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d4 > 0) {
                                    g2.drawString("- another " + d4 + " Gold", hovX + 2, hovY + 56 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d5 > 0) {
                                    g2.drawString("- another " + d5 + " Oil", hovX + 2, hovY + 56 + (12 * (i + 1)));
                                    i++;
                                }
                            }
                        }
                    }
                } else if (ability.type == Ability.ABILITY_INTRA) {
                    AbilityIntraManager intr = (AbilityIntraManager) ability;
                    // Trennlinie
                    g2.drawLine(hovX, hovY + 15, mouseX, hovY + 15);
                    g2.setFont(fonts[2]);
                    g2.drawString(intr.caster2.intraUnits.size() + " / " + intr.caster2.maxIntra + " Units indoors", hovX + 2, hovY + 28);
                    g2.setColor(new Color(3, 100, 0));
                    g2.drawString("Left-click to evacuate ONE Unit", hovX + 2, hovY + 45);
                    g2.drawString("Right-click to evacuate ALL Units", hovX + 2, hovY + 57);

                }
            }
        }

    }

    private void renderGraphicElements(Graphics2D g2) {
    }

    private void renderSelBox(Graphics2D g2) {
        // Rendert die SelektionBox
        g2.setColor(Color.WHITE);
        Point tP = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(tP, this);
        int dirX = tP.x - this.boxselectionstart.width;
        int dirY = tP.y - this.boxselectionstart.height;
        // Bpxen können nur von links oben nach rechts unten gezogen werden - eventuell Koordinaten tauschen
        if (dirX < 0 && dirY > 0) {
            // Nur x Tauschen
            g2.drawRect(tP.x, this.boxselectionstart.height, this.boxselectionstart.width - tP.x, tP.y - this.boxselectionstart.height);
        } else if (dirY < 0 && dirX > 0) {
            // Nur Y tauschen
            g2.drawRect(this.boxselectionstart.width, tP.y, tP.x - this.boxselectionstart.width, this.boxselectionstart.height - tP.y);
        } else if (dirX < 0 && dirY < 0) {
            // Beide tauschen
            g2.drawRect(tP.x, tP.y, this.boxselectionstart.width - tP.x, this.boxselectionstart.height - tP.y);
        }
        // Nichts tauschen
        g2.drawRect(this.boxselectionstart.width, this.boxselectionstart.height, dirX, dirY);
    }

    private void renderHud(Graphics2D g2) {
        // Allgemeines Hud-backgroundbild
        if (saveMode) {
            g2.drawImage(savehudGround, hudX, 0, null);
        } else {
            g2.drawImage(hudGround, hudX, 0, null);
        }
        // Gebäude-Layer
        if (saveMode) {
            g2.drawImage(savebuildingLayer, (int) (hudX + hudSizeX * 0.1), (int) (realPixY / 7 * 1.4), null);
        } else {
            g2.drawImage(buildingLayer, (int) (hudX + hudSizeX * 0.1), (int) (realPixY / 7 * 1.4), null);
        }
        // Minimap - Grund-Minimap schon im Hud drin, Rest kommt jetzt dazu
        // Fow-Layer kopieren (von 1 auf 2)
        Graphics2D fowg1;
        Graphics2D fowg2;
        if (saveMode) {
            if (savefowMiniLayer2 == null) {
                savefowMiniLayer2 = cgc.createCompatibleImage(savefowMiniLayer.getWidth(), savefowMiniLayer.getHeight(), Transparency.TRANSLUCENT);
            }
            fowg2 = savefowMiniLayer2.createGraphics();
            fowg2.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
            fowg2.clearRect(0, 0, savefowMiniLayer2.getWidth(), savefowMiniLayer2.getHeight());
            fowg2.drawImage(savefowMiniLayer, 0, 0, null);
            fowg1 = savefowMiniLayer.createGraphics();
        } else {
            if (fowMiniLayer2 == null) {
                fowMiniLayer2 = cgc.createCompatibleVolatileImage(fowMiniLayer.getWidth(), fowMiniLayer.getHeight(), Transparency.TRANSLUCENT);
            }
            fowg2 = fowMiniLayer2.createGraphics();
            fowg2.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
            fowg2.clearRect(0, 0, fowMiniLayer2.getWidth(), fowMiniLayer2.getHeight());
            fowg2.drawImage(fowMiniLayer, 0, 0, null);
            fowg1 = fowMiniLayer.createGraphics();
        }
        // Fow vorbereiten
        AlphaComposite full = AlphaComposite.getInstance(AlphaComposite.CLEAR);
        AlphaComposite half = AlphaComposite.getInstance(AlphaComposite.DST_ATOP);
        fowg1.setComposite(full);
        fowg2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
        fowg2.setComposite(half);
        int playerId = rgi.game.getOwnPlayer().playerId;
        // Units auf die MiniMap (+ Fow auf 1)
        if (unitList != null) {
            for (int i = 0; i < unitList.size(); i++) {
                Unit unit = unitList.get(i);
                // Nur eigene und sichtbare rendern
                try {
                    if (unit.playerId == playerId || fowmap[unit.position.X][unit.position.Y] > 1) {
                        if ((saveMode && saveminiMap != null) || (!saveMode && miniMap != null)) {
                            setColorToPlayer(unit.playerId, g2);
                            g2.fillRect((int) ((unit.position.X * 1.0 / sizeX * hudSizeX * 0.8) + hudX + hudSizeX * 0.1) - 1, (int) ((unit.position.Y * 1.0 / sizeY * realPixY * 2 / 7 * 0.8) + realPixY / 7 * 1.371428) + 2, 3, 3);
                            if (unit.playerId == playerId) {
                                // Wenn eigene, dann noch sichtbarer Bereich rausschneiden (voll auf 1 und halb auf 2)
                                // Der letze Faktor ist PI*Daumen (try&error)
                                int bx = (int) (unit.position.X * 20 * maxminscaleX);
                                int by = (int) (unit.position.Y * 15 * maxminscaleY);
                                // Mir sind diese Werte ehrlich gesagt net ganz klar, besonders der letzte faktor
                                int vrangeX = (int) (unit.getVisrange() * 20 * maxminscaleX * 2);
                                int vrangeY = (int) (unit.getVisrange() * 15 * maxminscaleY * 2);
                                fowg1.fillOval(bx - vrangeX, by - vrangeY, vrangeX * 2, vrangeY * 2);
                                fowg2.fillOval(bx - vrangeX, by - vrangeY, vrangeX * 2, vrangeY * 2);
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.println("FixMe: Illegal Position: Unit " + unit + " pos: " + unit.position);
                }
            }
        }
        // Gebäude schneiden (fow)
        if (buildingList != null) {
            for (int i = 0; i < buildingList.size(); i++) {
                Building building = buildingList.get(i);
                // Nur eigene
                if (building.playerId == playerId) {
                    // "Loch" in den fow-grundlayer schneiden
                    int bx = (int) ((building.position.X + ((building.z1 + building.z2 - 2) * 1.0 / 2)) * 20 * maxminscaleX);
                    int by = (int) (building.position.Y * 15 * maxminscaleY);
                    // Mir sind diese Werte ehrlich gesagt net ganz klar, besonders der letzte faktor
                    int vrangeX = (int) ((building.getVisrange() + ((building.z1 + building.z2) / 4)) * 20 * maxminscaleX * 2);
                    int vrangeY = (int) ((building.getVisrange() + ((building.z1 + building.z2) / 4)) * 15 * maxminscaleY * 2);
                    fowg1.fillOval(bx - vrangeX, by - vrangeY, vrangeX * 2, vrangeY * 2);
                }
            }
        }
        // FoW-Layer (1) auf die Minimap
        if (saveMode) {
            g2.drawImage(savefowMiniLayer, (int) (hudX + hudSizeX * 0.1), (int) (realPixY / 7 * 1.4), null);
        } else {
            g2.drawImage(fowMiniLayer, (int) (hudX + hudSizeX * 0.1), (int) (realPixY / 7 * 1.4), null);
        }
        // 1 löschen, 2 wieder zurückkopieren
        fowg1.setComposite(g2.getComposite());
        fowg1.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        if (saveMode) {
            fowg1.clearRect(0, 0, savefowMiniLayer2.getWidth(), savefowMiniLayer2.getHeight());
            fowg1.drawImage(savefowMiniLayer2, 0, 0, null);
        } else {
            fowg1.clearRect(0, 0, fowMiniLayer2.getWidth(), fowMiniLayer2.getHeight());
            fowg1.drawImage(fowMiniLayer2, 0, 0, null);
        }
        // Sichtbaren Bereich anzeigen
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect((int) ((positionX * 1.0 / sizeX * hudSizeX * 0.8) + hudX + hudSizeX * 0.1), (int) ((positionY * 1.0 / sizeY * realPixY * 2 / 7 * 0.8) + realPixY / 7 * 1.4), miniMapViewSizeX, miniMapViewSizeY);
        int newHash = 0;
        try {
            newHash = selectedObjects.hashCode();
        } catch (ConcurrentModificationException ex) {
            System.out.println("Catched: CME in Hud-Hashcode-Check - ignore if rare");
        }
        if (newHash != lastMenuHash || updateInterHud) {
            lastMenuHash = newHash;
            // Menü neu berechnen:
            updateInterHud = false;
            buildInteractiveHud();
        }
        // Interaktives Hud zeichnen
        if (saveMode) {
            g2.drawImage(saveinteractivehud, hudX, realPixY * 3 / 7, null);
        } else {
            g2.drawImage(interactivehud, hudX, realPixY * 3 / 7, null);
        }

        // Ressourcen & Truppenlimit reinschreiben
        g2.setColor(Color.BLACK);
        g2.setFont(fonts[1]);
        try {
            g2.drawString(String.valueOf(rgi.game.getOwnPlayer().res1), (int) (hudX + hudSizeX * 0.1), (int) (realPixY * 0.03));
            g2.drawString(String.valueOf(rgi.game.getOwnPlayer().res2), (int) (hudX + hudSizeX * 0.43), (int) (realPixY * 0.03));
            g2.drawString(String.valueOf(rgi.game.getOwnPlayer().res3), (int) (hudX + hudSizeX * 0.1), (int) (realPixY * 0.078));
            g2.drawString(String.valueOf(rgi.game.getOwnPlayer().res4), (int) (hudX + hudSizeX * 0.43), (int) (realPixY * 0.078));
            g2.drawString(String.valueOf(rgi.game.getOwnPlayer().res5), (int) (hudX + hudSizeX * 0.1), (int) (realPixY * 0.125));
            g2.drawString(rgi.game.getOwnPlayer().currentlimit + "/" + rgi.game.getOwnPlayer().maxlimit, (int) (hudX + hudSizeX * 0.43), (int) (realPixY * 0.125));
        } catch (NullPointerException ex) {
        }
    }

    private void buildInteractiveHud() {
        // Baut den interaktiven Teil des Huds neu auf - z.B. da wo Einheiten etc angezeigt werden
        int px = -1;
        int py = 0;
        Graphics2D g2 = null;
        ArrayList<GameObject> selected = rgi.rogGraphics.inputM.selected;
        if (selected.isEmpty() && this.tempInfoObj == null) {
            // Hud leeren
            if (saveMode) {
                g2 = saveinteractivehud.createGraphics();
            } else {
                g2 = interactivehud.createGraphics();
            }
            g2.setBackground(new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f));
            if (saveMode) {
                g2.clearRect(0, 0, saveinteractivehud.getWidth(), saveinteractivehud.getHeight());
            } else {
                g2.clearRect(0, 0, interactivehud.getWidth(), interactivehud.getHeight());
            }
        }
        if ((selected.isEmpty() && this.tempInfoObj != null) || selected.size() == 1) {
            // Hud-Info mode
            if (selected.size() == 1) {
                tempInfoObj = selected.get(0);
            }
            if (saveMode) {
                g2 = saveinteractivehud.createGraphics();
            } else {
                g2 = interactivehud.createGraphics();
            }
            g2.setBackground(new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f));
            if (saveMode) {
                g2.clearRect(0, 0, saveinteractivehud.getWidth(), saveinteractivehud.getHeight());
            } else {
                g2.clearRect(0, 0, interactivehud.getWidth(), interactivehud.getHeight());
            }

            // Spezifische Infos anzeigen:
            if (tempInfoObj.getClass().equals(Unit.class)) {
                // Unit - Infos rendern
                Unit unit = (Unit) tempInfoObj;
                // Bildchen
                CoRImage img = imgMap.get(unit.graphicsdata.defaultTexture);
                int dx1 = (int) (hudSizeX * 0.15);
                int dy1 = (int) (realPixY * 2 / 7 * 0.2);
                int dx2 = (int) (hudSizeX * 0.15 + hudSizeX * 0.7 / 5);
                int dy2 = (int) (realPixY * 2 / 7 * 0.2 + realPixY * 2 / 7 * 0.7 / 4);
                if (img != null) {
                    g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight(), null);
                }
                // Name
                g2.setColor(Color.BLACK);
                g2.setFont(fonts[1]);
                g2.drawString(unit.name, (int) (hudSizeX * 0.4), dy1 + 12);
                // Energie:
                g2.setFont(fonts[2]);
                g2.drawString("HP:  " + unit.getHitpoints() + " / " + unit.getMaxhitpoints(), (int) (hudSizeX * 0.41), (int) ((dy2 - dy1) * 0.7) + dy1);
                // Rüstung
                g2.setFont(fonts[0]);
                g2.drawString("Armortype: ", dx1, (int) (dy2 * 1.1));
                String atype = "";
                if (unit.armortype.equals("lightinf")) {
                    atype = "Light Infantry";
                } else if (unit.armortype.equals("heavyinf")) {
                    atype = "Heavy Infantry";
                } else if (unit.armortype.equals("kav")) {
                    atype = "Cavalry";
                } else if (unit.armortype.equals("vehicle")) {
                    atype = "Vehicle";
                } else if (unit.armortype.equals("tank")) {
                    atype = "Tank";
                } else if (unit.armortype.equals("air")) {
                    atype = "Air";
                }
                g2.drawString(atype, (int) ((dx2 - dx1) * 4.7), (int) (dy2 * 1.1));
                //Geschwindigkeit
                g2.drawString("Speed: ", dx1, (int) (dy2 * 1.25));
                g2.drawString(String.valueOf(unit.speed), (int) ((dx2 - dx1) * 4.7), (int) (dy2 * 1.25));
                //Reichweite
                g2.drawString("Range: ", dx1, (int) (dy2 * 1.4));
                if (unit.getRange() == 2) {
                    g2.drawString("Melee", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * 1.4));
                } else {
                    g2.drawString(String.valueOf(unit.getRange()), (int) ((dx2 - dx1) * 4.7), (int) (dy2 * 1.4));
                }
                // Schaden:
                g2.drawString("Damage: ", dx1, (int) (dy2 * 1.60));
                g2.drawString(String.valueOf(unit.getDamage()), (int) ((dx2 - dx1) * 4.7), (int) (dy2 * 1.60));
                // Special-Schaden gegen Rüstungsklassen:
                float yposition = 1.75f;
                if (unit.antilightinf != 100) {
                    g2.drawString("Damage vs. Light Infantry: ", dx1, (int) (dy2 * yposition));
                    g2.drawString(unit.antilightinf + "%", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * yposition));
                    yposition += 0.15;
                }
                if (unit.antiheavyinf != 100) {
                    g2.drawString("Damage vs. Heavy Infantry: ", dx1, (int) (dy2 * yposition));
                    g2.drawString(unit.antiheavyinf + "%", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * yposition));
                    yposition += 0.15;
                }
                if (unit.antikav != 100) {
                    g2.drawString("Damage vs. Cavalry: ", dx1, (int) (dy2 * yposition));
                    g2.drawString(unit.antikav + "%", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * yposition));
                    yposition += 0.15;
                }
                if (unit.antivehicle != 100) {
                    g2.drawString("Damage vs. Vehicle: ", dx1, (int) (dy2 * yposition));
                    g2.drawString(unit.antivehicle + "%", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * yposition));
                    yposition += 0.15;
                }
                if (unit.antitank != 100) {
                    g2.drawString("Damage vs. Tanks: ", dx1, (int) (dy2 * yposition));
                    g2.drawString(unit.antitank + "%", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * yposition));
                    yposition += 0.15;
                }
                if (unit.antiair != 0) {
                    g2.drawString("Damage vs. Air: ", dx1, (int) (dy2 * yposition));
                    g2.drawString(unit.antiair + "%", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * yposition));
                    yposition += 0.15;
                }
                if (unit.antibuilding != 100) {
                    g2.drawString("Damage vs. Buildings: ", dx1, (int) (dy2 * yposition));
                    g2.drawString(unit.antibuilding + "%", (int) ((dx2 - dx1) * 4.7), (int) (dy2 * yposition));
                    yposition += 0.15;
                }

            } else if (tempInfoObj.getClass().equals(Building.class)) {
                // Building - Infos rendern
                Building building = (Building) tempInfoObj;
                // Bildchen
                CoRImage img = imgMap.get(building.defaultTexture);
                int dx1 = (int) (hudSizeX * 0.15);
                int dy1 = (int) (realPixY * 2 / 7 * 0.2);
                int dx2 = (int) (hudSizeX * 0.15 + hudSizeX * 0.7 / 5);
                int dy2 = (int) (realPixY * 2 / 7 * 0.2 + realPixY * 2 / 7 * 0.7 / 4);
                if (img != null) {
                    g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight(), null);
                }
                // Name
                g2.setColor(Color.BLACK);
                g2.setFont(fonts[1]);
                g2.drawString(building.name, (int) (hudSizeX * 0.4), dy1 + 12);
                // Energie:
                g2.setFont(fonts[2]);
                g2.drawString("HP:  " + building.getHitpoints() + " / " + building.getMaxhitpoints(), (int) (hudSizeX * 0.41), (int) ((dy2 - dy1) * 0.7) + dy1);
                // Rüstung
                g2.setFont(fonts[0]);
                g2.drawString("Armortype: ", dx1, (int) (dy2 * 1.1));
                g2.drawString("Building", (int) ((dx2 - dx1) * 5), (int) (dy2 * 1.1));
                if (building.isbuilt) {
                    // Fortschritt anzeigen
                    g2.drawString("Constructing:  ", dx1, (int) (dy2 * 1.3));
                    g2.drawString(Math.round(building.buildprogress * 100) + "%", (int) ((dx2 - dx1) * 5), (int) (dy2 * 1.3));
                } else if (building.maxIntra > 0) {
                    // Anzahl der Arbeiter im Gebäude
                    g2.drawString("Harvesters: ", dx1, (int) (dy2 * 1.3));
                    g2.drawString(building.intraUnits.size() + "/" + building.maxIntra, (int) ((dx2 - dx1) * 5), (int) (dy2 * 1.3));
                }
            } else if (tempInfoObj.getClass().equals(Ressource.class)) {
                // Unit - Infos rendern
                Ressource res = (Ressource) tempInfoObj;
                // Bildchen
                CoRImage img = imgMap.get(res.getTex());
                int dx1 = (int) (hudSizeX * 0.15);
                int dy1 = (int) (realPixY * 2 / 7 * 0.2);
                int dx2 = (int) (hudSizeX * 0.15 + hudSizeX * 0.7 / 5);
                int dy2 = (int) (realPixY * 2 / 7 * 0.2 + realPixY * 2 / 7 * 0.7 / 4);
                if (img != null) {
                    g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight(), null);
                }
                // Name
                g2.setColor(Color.BLACK);
                g2.setFont(fonts[1]);
                String name = "";
                switch (res.getType()) {
                    case 2:
                        name = "Tree";
                        break;
                    case 1:
                        name = "Berry bush";
                        break;
                    case 3:
                        name = "Metal mine";
                        break;
                    case 4:
                        name = "Gold mine";
                        break;
                }
                g2.drawString(name, (int) (hudSizeX * 0.4), dy1 + 12);
                // Energie:
                g2.setFont(fonts[2]);
                g2.drawString("Resources left:  " + res.hitpoints, (int) (hudSizeX * 0.41), (int) ((dy2 - dy1) * 0.7) + dy1);
            }
            // Permanent updaten, damit Hp-Infos etc da bleiben
            updateInterHud = true;
        }
        interSelFields.clear();
        if (!selected.isEmpty()) {
            // Altes löschen
            tempInfoObj = null;
            // Erstmal die Grundlegende Struktur mit mehreren gleichen zusammenfassen und so
            ArrayList<ArrayList<GameObject>> prelist = new ArrayList<ArrayList<GameObject>>();
            for (int i = 0; i < selected.size(); i++) {
                GameObject obj = selected.get(i);
                // Nachsehen, ob es schon Kategorien mit dieser Einheit gibt:
                int res = -1;
                for (int k = 0; k < prelist.size(); k++) {
                    ArrayList<GameObject> list = prelist.get(k);
                    if (list.get(0).descTypeId == obj.descTypeId) {
                        res = k;
                        break;
                    }
                }
                if (res != -1) { // Ja das gibts schon, einfach adden
                    prelist.get(res).add(obj);
                } else {
                    // Neue Kategorie aufmachen
                    prelist.add(new ArrayList<GameObject>());
                    prelist.get(prelist.size() - 1).add(obj);
                }
            }
            // Strucktur für ein späteres finden speichern

            for (ArrayList<GameObject> oblist : prelist) {
                GameObject[] obarr = new GameObject[oblist.size()];
                oblist.toArray(obarr);
                interSelFields.add(obarr);
            }
            // Fertig, Strucktur ist aufgebaut, jetzt zeichnen
            if (selected.size() > 1) {
                if (saveMode) {
                    g2 = saveinteractivehud.createGraphics();
                } else {
                    g2 = interactivehud.createGraphics();
                }
                g2.setBackground(new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f));
                if (saveMode) {
                    g2.clearRect(0, 0, saveinteractivehud.getWidth(), saveinteractivehud.getHeight());
                } else {
                    g2.clearRect(0, 0, interactivehud.getWidth(), interactivehud.getHeight());
                }
                for (int m = 0; m < interSelFields.size(); m++) {
                    // Position bestimmen:
                    px++;
                    if (px > 3) {
                        py++;
                        px = 0;
                    }
                    // Dahin zeichnen:
                    g2.setColor(Color.BLACK);
                    GameObject rgo = interSelFields.get(m)[0];
                    String sel;
                    BufferedImage img = null;
                    // Versuche Gebäude
                    try {
                        Unit u = (Unit) rgo;
                        // Ok, Unit
                        if ((sel = u.graphicsdata.hudTexture) == null) {
                            sel = u.graphicsdata.defaultTexture;
                        }
                        if (sel != null) {
                            CoRImage rimg = imgMap.get(sel);
                            if (rimg != null) {
                                img = rimg.getImage();
                            }
                        }
                    } catch (java.lang.ClassCastException ex) {
                        Building b = (Building) rgo;
                        // Ok, Gebäude
                        if ((sel = b.hudTexture) == null) {
                            sel = b.defaultTexture;
                        }
                        if (sel != null) {
                            CoRImage rimg = imgMap.get(sel);
                            if (rimg != null) {
                                img = rimg.getImage();
                            }
                        }
                    }
                    if (img != null) {
                        // Jetzt Zeichnen
                        int dx1 = (int) (hudSizeX * 0.15 + px * (hudSizeX * 0.7 * 4 / 15));
                        int dy1 = (int) (realPixY * 2 / 7 * 0.2 + py * (realPixY * 48 / 560));
                        int dx2 = (int) (hudSizeX * 0.15 + px * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5);
                        int dy2 = (int) (realPixY * 2 / 7 * 0.2 + py * (realPixY * 48 / 560) + realPixY * 2 / 7 * 0.7 / 4);
                        g2.drawImage(img, dx1, dy1, dx2, dy2, 0, 0, img.getWidth(), img.getHeight(), null);
                        g2.setColor(Color.BLACK);
                        g2.drawRect(dx1, dy1, dx2 - dx1, dy2 - dy1);
                        g2.drawRect(dx1 + 1, dy1 + 1, (dx2 - dx1) - 2, (dy2 - dy1) - 2);
                        g2.drawString(String.valueOf(interSelFields.get(m).length), dx1 + 2, dy1 + 12);
                    }
                }
            }
        }
        optList.clear();
        if (selected.size() == 1 || interSelFields.size() == 1) {
            // Wenn alles von der gleichen Sorte ist
            // Alle Fähigkeiten dieser "Sorte" einblenden
            GameObject obj = null;
            if (interSelFields.size() == 1) {
                obj = interSelFields.get(0)[0];
            } else {
                obj = selected.get(0);
            }
            if (obj.ready) {
                java.util.List<Ability> alist = obj.abilitys;
                px = -1;
                py = 0;
                for (int i = 0; i < alist.size(); i++) {
                    Ability ability = alist.get(i);
                    // Entspricht Epoche? - Überhaupt zeichnen?
                    if ((ability.epoche == this.epoche || ability.epoche == 0) && ability.isVisible()) {
                        optList.add(ability);
                        // Position bestimmen:
                        px++;
                        if (px > 3) {
                            py++;
                            px = 0;
                        }
                        // Entweder wir haben gerade die angegebene Epoche, oder es gilt für alle 0
                        // Bildchen holen
                        String tex = null;
                        CoRImage img = null;
                        try {
                            tex = ability.symbols[epoche];
                            if (tex == null) {
                                tex = ability.symbols[0]; // Wenn für die Spezielle Epoche keins da ist, dann das allgemeine versuchen
                            }
                            // Aktivierbar?
                            if (ability.isAvailable()) {
                                img = imgMap.get(tex);
                            } else {
                                img = grayImgMap.get(tex);
                            }
                        } catch (Exception ex) {
                            rgi.logger("[Graphics][ERROR]: Symbol for ability \"" + ability.name + "\" (epoche " + epoche + ") not found!");
                        }
                        if (img != null) {
                            // Jetzt Zeichnen
                            int dx1 = (int) (hudSizeX * 0.15 + px * (hudSizeX * 0.7 * 4 / 15));
                            int dy1 = (int) ((realPixY * 2 / 7 * 0.2 + py * (realPixY * 48 / 560)) + realPixY * 0.257);
                            int dx2 = (int) (hudSizeX * 0.15 + px * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5);
                            int dy2 = (int) ((realPixY * 2 / 7 * 0.2 + py * (realPixY * 48 / 560) + realPixY * 2 / 7 * 0.7 / 4) + realPixY * 0.257);
                            g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight(), null);
                            //g2.setColor(ability.frameColor);
                            g2.drawRect(dx1, dy1, dx2 - dx1, dy2 - dy1);
                            g2.drawRect(dx1 + 1, dy1 + 1, (dx2 - dx1) - 2, (dy2 - dy1) - 2);
                            // Fortschritt zeichnen?
                            if (ability.behaviour != null) {
                                float progress = 1.0f;
                                int number = -1;
                                if (ability.getClass().equals(AbilityRecruit.class)) {
                                    AbilityRecruit abr = (AbilityRecruit) ability;
                                    if (abr.behaviour.showProgess(abr.descTypeId)) {
                                        // Live-Rendern des IA-Huds einschalten
                                        updateInterHud = true;
                                        progress = abr.behaviour.getProgress(abr.descTypeId);
                                        if (abr.behaviour.showNumber(abr.descTypeId)) {
                                            number = abr.behaviour.getNumber(abr.descTypeId);
                                        }
                                    }
                                } else if (ability.getClass().equals(AbilityBuild.class)) {
                                    AbilityBuild abb = (AbilityBuild) ability;
                                    if (abb.behaviour.showProgess(abb.descTypeId)) {
                                        // Live-Rendern des IA-Huds einschalten
                                        updateInterHud = true;
                                        progress = abb.behaviour.getProgress(0);
                                    }
                                } else {
                                    if (ability.behaviour.showProgess(0)) {
                                        updateInterHud = true;
                                        progress = ability.behaviour.getProgress(0);
                                        if (ability.behaviour.showNumber(0)) {
                                            number = ability.behaviour.getNumber(0);
                                        }
                                    }
                                }

                                // Punkt-Darstellung ermitteln:
                                Polygon poly = new Polygon();
                                // Mittelpunkt adden:
                                poly.addPoint((dx1 + dx2) / 2, (dy1 + dy2) / 2);
                                if (progress < 0.125) {
                                    // Den Variablen Punkt:
                                    // Mitte X + progress * 8 * restX, obenY
                                    poly.addPoint((int) (((dx1 + dx2) / 2) + ((((dx2 + dx1) / 2) - dx1) * progress * 8)), dy1);
                                    // Oben rechts
                                    poly.addPoint(dx2, dy1);
                                    // Unten Rechts
                                    poly.addPoint(dx2, dy2);
                                    // Unten links
                                    poly.addPoint(dx1, dy2);
                                    // Oben Links
                                    poly.addPoint(dx1, dy1);
                                    // Oben Mitte
                                    poly.addPoint((dx1 + dx2) / 2, dy1);
                                } else if (progress < 0.375) {
                                    // Den Variablen Punkt
                                    // X2, Y1 + (progess - 0.125) * 4 * (Y2 - Y1)
                                    poly.addPoint(dx2, (int) (dy1 + (progress - 0.125) * 4 * (dy2 - dy1)));
                                    // Unten Rechts
                                    poly.addPoint(dx2, dy2);
                                    // Unten links
                                    poly.addPoint(dx1, dy2);
                                    // Oben Links
                                    poly.addPoint(dx1, dy1);
                                    // Oben Mitte
                                    poly.addPoint((dx1 + dx2) / 2, dy1);
                                } else if (progress < 0.625) {
                                    // Variabler Punkt
                                    // (X2 - X1) * (progress - 0.375) * 4 , Y2
                                    poly.addPoint((int) (dx2 - ((dx2 - dx1) * (progress - 0.375) * 4)), dy2);
                                    // Unten links
                                    poly.addPoint(dx1, dy2);
                                    // Oben Links
                                    poly.addPoint(dx1, dy1);
                                    // Oben Mitte
                                    poly.addPoint((dx1 + dx2) / 2, dy1);
                                } else if (progress < 0.875) {
                                    // Variabler Punkt
                                    // X1, (dy2 -dy1) * (progress - 0.625) * 4
                                    poly.addPoint(dx1, (int) (dy2 - (dy2 - dy1) * (progress - 0.625) * 4));
                                    // Oben Links
                                    poly.addPoint(dx1, dy1);
                                    // Oben Mitte
                                    poly.addPoint((dx1 + dx2) / 2, dy1);
                                } else {
                                    // Variabler Punkt
                                    // X1 + (X2 - X1) / 2 * (progress - 0.875) * 8, Y1
                                    poly.addPoint((int) (dx1 + (dx2 - dx1) / 2 * (progress - 0.875) * 8), dy1);
                                    // Oben Mitte
                                    poly.addPoint((dx1 + dx2) / 2, dy1);
                                }
                                // Mittelpunkt wieder adden
                                poly.addPoint((dx1 + dx2) / 2, (dy1 + dy2) / 2);
                                // Zeichnen
                                g2.setColor(new java.awt.Color(0.0f, 0.0f, 0.0f, 0.6f));
                                g2.fillPolygon(poly);
                                g2.setColor(Color.black);
                                if (number != -1) {
                                    g2.drawString(Integer.toString(number), dx1 + 2, dy1 + 12);
                                }
                            }
                        }

                    }

                }
            }
        }
        // Komplett fertig, Struckturiert und alles - prima. Ende
    }

    private void setColorToPlayer(int playerId, Graphics2D g2) {
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
                        g2.drawImage(imgMap.get("cur1").getImage(), (framePos.width + x) * 20, (framePos.height + y) * 15, null);
                    }
                }
            }

        } else {
            // Pic-Cursor
            // Einfach Bild an die gerasterte Cursorposition rendern.
            g2.drawImage(renderPic.getImage(), framePos.width * 20, framePos.height * 15, null);
        }
    }

    private void renderBuildings(Graphics2D g2) {
        // Zeichnet die Gebäude
        if (buildingList != null) {
            for (int i = 0; i < buildingList.size(); i++) {
                // Alle Gebäude durchgehen und zeichnen
                Building tempB = buildingList.get(i);
                // Textur holen
                String tex = tempB.defaultTexture;
                // Grafik aus ImgMap laden
                CoRImage tempImage;
                // Schwarz-Weiß oder normal
                if (colMode) {
                    tempImage = grayImgMap.get(tex);
                } else {
                    if (tempB.isSelected) {
                        tempImage = imgMap.get(tex);
                    } else {
                        // Zum Spieler passend setzen
                        tempImage = imgMap.get(tex);
                    }

                }
                // Bild da?
                if (tempImage != null) {
                    // Ok, zeichnen
                    g2.drawImage(tempImage.getImage(), (tempB.position.X - tempB.offsetX - positionX) * 20, (tempB.position.Y - tempB.offsetY - positionY) * 15, null);
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

    private void renderBackground(Graphics2D g2) {
        if (modi != 3) {
            if (saveMode) {
                g2.drawImage(saverenderBackground, 0, 0, null);
            } else {
                g2.drawImage(renderBackground, 0, 0, null);
            }
        } else {
            if (saveMode) {
                g2.drawImage(saverenderBackground, 0, 0, getWidth(), getHeight(), positionX * 20, positionY * 15, (positionX * 20) + getWidth(), (positionY * 15) + getHeight(), null);
            } else {
                g2.drawImage(renderBackground, 0, 0, getWidth(), getHeight(), positionX * 20, positionY * 15, (positionX * 20) + getWidth(), (positionY * 15) + getHeight(), null);
            }
        }
    }

    private void renderFrameRate(Graphics2D g2) {
        // Zeigt die Framerate an
        g2.setColor(Color.DARK_GRAY);
        g2.drawString(Integer.toString(lastFr) + " fps", 10, 30);
    }

    public void changePaintCursor(MapEditorCursor rmec) {
        drawCursor = rmec;
    }

    private void renderCol(Graphics2D g2) {
        // Murks, aber die position müssen gerade sein...
        if (positionX % 2 == 1) {
            positionX--;
        }
        if (positionY % 2 == 1) {
            positionY--;
        }
        // Rendert die rote Kollisionsfarbe
        for (int x = 0; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0; y < sizeY && y < viewY; y = y + 2) {
                // Hat dieses Feld Kollision?
                try {
                    if (visMap[x + positionX][y + positionY].getCollision() != collision.free) {
                        // Bild einfügen
                        g2.drawImage(colModeImage.getImage(), x * 20, y * 15, null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        for (int x = 0 + 1; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0 + 1; y < sizeY && y < viewY; y = y + 2) {
                // Hat dieses Feld Kollision?
                try {
                    if (visMap[x + positionX][y + positionY].getCollision() != collision.free) {
                        // Bild einfügen
                        g2.drawImage(colModeImage.getImage(), x * 20, y * 15, null);
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    private void renderCoords(Graphics2D g2) {
        // Murks, aber die position müssen gerade sein...
        if (positionX % 2 == 1) {
            positionX--;
        }
        if (positionY % 2 == 1) {
            positionY--;
        }
        g2.setFont(fonts[4]);
        // Rendert die rote Kollisionsfarbe
        for (int x = 0; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0; y < sizeY && y < viewY; y = y + 2) {
                g2.drawString((x + positionX) + "|" + (y + positionY), x * 20 + 10, y * 15 + 28);
            }
        }
        for (int x = 0 + 1; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0 + 1; y < sizeY && y < viewY; y = y + 2) {
                g2.drawString((x + positionX) + "|" + (y + positionY), x * 20 + 10, y * 15 + 28);
            }
        }
    }

    private void renderUnits(Graphics2D g2) {
        selectionShadows = new ArrayList();
        if (unitList != null) {
            for (int i = 0; i < unitList.size(); i++) {
                // Liste durchgehen
                // Wenn Einheit stillsteht:
                Unit unit = unitList.get(i);
                if (!unit.isMoving()) {
                    // Einheit an aktuelle Position rendern
                    String tex = unit.graphicsdata.getTexture();
                    CoRImage tempImage;
                    if (colMode) {
                        // SW-Modus
                        BufferedImage newTemp = grayImgMap.get(tex).getImage();
                        tempImage = new CoRImage(newTemp);
                    } else {
                        // Normalfall
                        // Animated?
                        if (unit.anim != null && unit.anim.isIdleAnimated() && modi == 3) {
                            // tempImage = new RogImage(unit.anim.getNextIdleFrame());
                            tempImage = null;
                        } else {
                            tempImage = imgMap.get(tex);
                        }
                    }
                    if (tempImage != null) {
                        //Einheit gehört zu / Selektiert
                        if (!colMode) {
                            if (unit.playerId != 0) {
                                String sub = "img/game/sel_t" + unit.playerId + "_s1.png";
                                g2.drawImage(imgMap.get(sub).getImage(), (unit.position.X - positionX) * 20, (unit.position.Y - positionY) * 15, null);
                            }
                            if (unit.isSelected) {
                                // Weiß markieren
                                //TODO größe der Einheit an selektion anpassen
                                g2.drawImage(imgMap.get("img/game/sel_t0_s1.png").getImage(), (unit.position.X - positionX) * 20, (unit.position.Y - positionY) * 15, null);
                            }
                            if (unit.isSelected || alwaysshowenergybars) {
                                renderHealth(unit, g2, ((unit.position.X - positionX) * 20), ((unit.position.Y - positionY) * 15));
                            }
                        }
                        // Einheit zeichnen
                        g2.drawImage(tempImage.getImage(), (unit.position.X - positionX) * 20, (unit.position.Y - positionY) * 15, null);
                        // Einheit zu selektionsshadow dazutun
                        if (unit.selectionShadow == 1) {
                            // Kleine Units, 27x33 + 8/3
                            selectionShadows.add(new Dimension(((unit.position.X - positionX) * 20) + 8, ((unit.position.Y - positionY) * 15) + 3));
                            selectionShadows.add(new Dimension(((unit.position.X - positionX) * 20) + 35, ((unit.position.Y - positionY) * 15) + 36));
                            selectionShadows.add(unit);
                        }

                    } else {
                        System.out.println("[RogGraphics][ERROR]: Image \"" + tex + "\" not found!");
                    }
                } else {
                    // Einheit in Bewegung
                    String tex = unit.graphicsdata.getTexture();
                    CoRImage tempImage;
                    if (unit.anim != null && unit.anim.isMovingAnimated()) {
                        //tempImage = new RogImage(unit.anim.getNextMovingFrame());
                        tempImage = null;
                    } else {
                        tempImage = imgMap.get(tex);
                    }
                    if (tempImage != null) {
                        Position tempP = unit.getMovingPosition(rgi, positionX, positionY);
                        if (tempP.X != -1) {
                            //Einheit gehört zu / Selektiert

                            if (unit.playerId != 0) {
                                String sub = "img/game/sel_t" + unit.playerId + "_s1.png";
                                g2.drawImage(imgMap.get(sub).getImage(), tempP.X, tempP.Y, null);
                            }
                            if (unit.isSelected) {
                                // Weiß markieren
                                //TODO größe der Einheit an selektion anpassen
                                g2.drawImage(imgMap.get("img/game/sel_t0_s1.png").getImage(), tempP.X, tempP.Y, null);
                                renderHealth(unit, g2, tempP.X, tempP.Y);
                            }
                            g2.drawImage(tempImage.getImage(), tempP.X, tempP.Y, null);
                            if (unit.selectionShadow == 1) {
                                // Kleine Units, 27x33 + 8/3
                                selectionShadows.add(new Dimension(tempP.X + 8, tempP.Y + 3));
                                selectionShadows.add(new Dimension(tempP.X + 35, tempP.Y + 36));
                                selectionShadows.add(unit);
                            }
                        }
                    }
                }
            }
        }
        selectionShadowsB = selectionShadows;
    }

    private void renderHealth(GameObject rO, Graphics2D g2, int dX, int dY) {
        try {
            Unit rU = (Unit) rO;
            // Billigen Balken rendern
            g2.setColor(Color.BLACK);
            if (rU.selectionShadow == 1) {
                g2.fillRect(dX + 9, dY - 5, 22, 5);
            }
            // Farbe bestimmen
            int percent = rU.getHitpoints() * 100 / rU.getMaxhitpoints();
            if (percent > 65) {
                g2.setColor(Color.GREEN);
            } else if (percent > 32) {
                g2.setColor(Color.ORANGE);
            } else {
                g2.setColor(Color.RED);
            }
            // Entsprechend viel füllen
            int fillperc = percent * 20 / 100;
            g2.fillRect(dX + 10, dY - 4, fillperc, 3);
        } catch (ClassCastException ex) {
            // Gebäude
            try {
                Building rB = (Building) rO;
                // Balken soll über der Mitte des Gebäudes schweben
                // Längenfaktor finden
                int lf = (rB.z1 + rB.z2) * 20;
                // dX / dY ist der Zeichenursprung
                // X-Mitte finden (Durchschnitt aus z1 und z2)
                int cpX = lf / 2 - lf / 4;
                // Die Anzahl Y-Pixel nur von z1 ab
                int cpY = rB.z1 * 15 + 10;
                // Billigen Balken rendern
                g2.setColor(Color.BLACK);
                g2.fillRect(dX + cpX, dY - cpY, (lf / 2) + 2, 5);
                // Farbe bestimmen
                int percent = rB.getHitpoints() * 100 / rB.getMaxhitpoints();
                if (percent > 65) {
                    g2.setColor(Color.GREEN);
                } else if (percent > 32) {
                    g2.setColor(Color.ORANGE);
                } else {
                    g2.setColor(Color.RED);
                }
                // Entsprechend viel füllen
                int fillperc = percent * (lf / 2) / 100;
                g2.fillRect(dX + cpX + 1, dY - cpY + 1, fillperc, 3);
            } catch (ClassCastException ex2) {
            }

        }
    }

    private void runRenderRound(String searchprop, Graphics2D g2) {
        //System.out.println("This is runRenderRound, searching for <" + searchprop + "> mapsize(x,y) view [x,y]: (" + sizeX + "," + sizeY + ") [" + viewX + "," + viewY + "]");
        for (int x = 0; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0; y < sizeY && y < viewY; y = y + 2) {
                // X und Y durchlaufen, wenn ein Bild da ist, dann einbauen
                //              System.out.println("Searching for " + x + "," + y);
                String tex = null;
                try {
                    tex = visMap[x + positionX][y + positionY].getProperty(searchprop);
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
                        g2.drawImage(tempImage.getImage(), x * 20, y * 15, null);
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
                    tex = visMap[x + positionX][y + positionY].getProperty(searchprop);
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
                        g2.drawImage(tempImage.getImage(), x * 20, y * 15, null);
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
            return new Dimension(sizeX * 20, (sizeY * 15) + 10); // Echtes rendern
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

    public void setVisMap(CoRMapElement[][] newVisMap, int X, int Y) {
        // Einfach einsetzen
        visMap = newVisMap;
        sizeX = X;
        sizeY = Y;

        miniMapViewSizeX = (int) (viewX * 1.0 / sizeX * hudSizeX * 0.8);
        miniMapViewSizeY = (int) (viewY * 1.0 / sizeY * realPixY / 7 * 2 * 0.8);
        // Fog of War initialisieren


        fowmap = new byte[visMap.length][visMap[0].length];
        // Durchlaufen, alles auf 0 (unerforscht) setzen
        for (int x = 0; x < fowmap.length; x++) {
            for (int y = 0; y < fowmap[0].length; y++) {
                fowmap[x][y] = 0;
            }
        }
    }

    public void changeVisMap(CoRMapElement[][] newVisMap) {
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
        //Setzt die Größe des sichtbaren Bereichs
        double hudxxrelation = (realPixX - (realPixY / 2.5)) / realPixX;
        if (byPass) {
            viewX = (int) (vX * hudxxrelation); // Hud hat feste Größe
        } else {
            viewX = vX;
        }
        viewY = vY;
        hudX = (int) (realPixX * hudxxrelation) + 1; // Erster Pixel vom Hud.
        hudSizeX = realPixX - hudX + 1;
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

    // Löscht die gesamte Fow-Map (überschreibt sie mit 0 (unerkundet))
    // Bereits erkundete Bereiche bleiben erkundet
    private void clearFogOfWar() {
        for (int x = 0; x < fowmap.length; x++) {
            for (int y = 0; y < fowmap[0].length; y++) {
                if (fowmap[x][y] != 1) {
                    fowmap[x][y] = 0;
                }
            }
        }
    }

    /**
     * Startet das Rendern
     * @param rImg ein VolatileImage, im Savemode ein BufferedImage
     */
    public void startRender(Image rImg) {
        // Echter Game-Rendering Mode
        // rImg ist Hintergrundbild
        if (saveMode) {
            saverenderBackground = (BufferedImage) rImg;
        } else {
            renderBackground = (VolatileImage) rImg;
        }
        modi = 3;
        initRun = new Date();
        if (saveMode) {
            saveinteractivehud = cgc.createCompatibleImage(hudSizeX, realPixY / 7 * 4, Transparency.TRANSLUCENT);
            savetempImg = cgc.createCompatibleImage(realPixX, realPixY);
        } else {
            interactivehud = cgc.createCompatibleVolatileImage(hudSizeX, realPixY / 7 * 4, Transparency.TRANSLUCENT);
            tempImg = cgc.createCompatibleVolatileImage(realPixX, realPixY);
        }
        advselshadB = cgc.createCompatibleImage(sizeX * 20, sizeY * 15);
        buildingsChanged(); // Das mach ich jetzt genau ein mal, das muss in den Gebäudebau-Handling-Code...
//        try {Thread.sleep(100000);} catch (Exception ex) {}
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

    public void renderBackgroundChanged() {
        // Re-rendert den Background
        calcImage(false, true);
        // MiniMap
        transformMiniMap();
        mergeHud();
    }

    public void initMiniMap() {
        transformMiniMap();
        mergeHud();
    }

    public void updateMiniMap() {
        calcMiniMap();
    }

    private void transformMiniMap() {
        try {
            if (saveMode) {
                // Erstellt einen neue Basis-Minimap
                BufferedImage tempImg2 = cgc.createCompatibleImage((int) (hudSizeX * 0.8), (int) (realPixY * 2 / 7 * 0.8));
                Graphics2D tempGra = tempImg2.createGraphics();
                BufferedImage vimg = (BufferedImage) calcImage(false, false);
                BufferedImage img = cgc.createCompatibleImage(tempImg2.getWidth(), tempImg2.getHeight());
                BufferedImage img2 = cgc.createCompatibleImage(tempImg2.getWidth(), tempImg2.getHeight());
                Graphics2D tempGra2 = img.createGraphics();
                Graphics2D tempGra3 = img2.createGraphics();
                tempGra2.drawImage(vimg, 0, 0, (int) (hudSizeX * 0.8), (int) (realPixY * 2 / 7 * 0.8), 20, 25, sizeX * 20, sizeY * 15, null);
                ConvolveOp cop = new ConvolveOp(new Kernel(3, 3, BLUR3x3), ConvolveOp.EDGE_NO_OP, null);
                tempGra3.drawImage(img, cop, 0, 0);
                RescaleOp rop = new RescaleOp(2.0f, 10.0f, null);
                tempGra.drawImage(img2, rop, 0, 0);
                saveminiMap = tempImg2;
            } else {
                // Erstellt einen neue Basis-Minimap
                VolatileImage tempImg2 = cgc.createCompatibleVolatileImage((int) (hudSizeX * 0.8), (int) (realPixY * 2 / 7 * 0.8));
                Graphics2D tempGra = tempImg2.createGraphics();
                VolatileImage vimg = (VolatileImage) calcImage(false, false);
                BufferedImage img = cgc.createCompatibleImage(tempImg2.getWidth(), tempImg2.getHeight());
                BufferedImage img2 = cgc.createCompatibleImage(tempImg2.getWidth(), tempImg2.getHeight());
                Graphics2D tempGra2 = img.createGraphics();
                Graphics2D tempGra3 = img2.createGraphics();
                tempGra2.drawImage(vimg, 0, 0, (int) (hudSizeX * 0.8), (int) (realPixY * 2 / 7 * 0.8), 20, 25, sizeX * 20, sizeY * 15, null);
                ConvolveOp cop = new ConvolveOp(new Kernel(3, 3, BLUR3x3), ConvolveOp.EDGE_NO_OP, null);
                tempGra3.drawImage(img, cop, 0, 0);
                RescaleOp rop = new RescaleOp(2.0f, 10.0f, null);
                tempGra.drawImage(img2, rop, 0, 0);
                miniMap = tempImg2;
            }
        } catch (Exception ex) {
            System.out.println("DINF!");
            ex.printStackTrace();
        }
        // Schwarzen Fow einmalig erstellen
        Graphics2D g3 = null;
        if (saveMode) {
            g3 = savefowMiniLayer.createGraphics();
        } else {
            g3 = fowMiniLayer.createGraphics();
        }
        g3.setBackground(Color.BLACK);
        if (saveMode) {
            g3.clearRect(0, 0, savefowMiniLayer.getWidth(), savefowMiniLayer.getHeight());
        } else {
            g3.clearRect(0, 0, fowMiniLayer.getWidth(), fowMiniLayer.getHeight());
        }
    }

    protected void mergeHud() {
        // Liefert das Hud zurück, mit der Minimap reingearbeitet
        Graphics2D g4 = null;
        if (saveMode) {
            if (savehudGround == null) {
                savehudGround = cgc.createCompatibleImage(hudSizeX, realPixY);
            }
            g4 = savehudGround.createGraphics();
        } else {
            if (hudGround == null) {
                hudGround = cgc.createCompatibleVolatileImage(hudSizeX, realPixY);
            }
            g4 = hudGround.createGraphics();
        }
        // Löschen
        g4.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.5f));
        g4.clearRect(0, 0, saveMode ? savehudGround.getWidth() : hudGround.getWidth(), saveMode ? savehudGround.getHeight() : hudGround.getHeight());
        // Bilder passend rendern
        if (epoche != 0) {
            // Hud skaliert reinrendern
            BufferedImage hud = huds[epoche];
            if (hud == null) {
                hud = huds[1];
            }
            if (saveMode) {
                g4.drawImage(hud, 0, 0, savehudGround.getWidth(), savehudGround.getHeight(), 0, 0, hud.getWidth(), hud.getHeight(), null);
            } else {
                g4.drawImage(hud, 0, 0, hudGround.getWidth(), hudGround.getHeight(), 0, 0, hud.getWidth(), hud.getHeight(), null);
            }
        }
        if (saveMode) {
            g4.drawImage(saveminiMap, (int) (hudSizeX * 0.1) + 1, (int) (realPixY / 7 * 1.4), null);
        } else {
            g4.drawImage(miniMap, (int) (hudSizeX * 0.1) + 1, (int) (realPixY / 7 * 1.4), null);
        }
    }

    public Dimension getSelectedField(int selX, int selY) {
        // Findet heraus, welches Feld geklickt wurde - Man muss die Felder mittig anklicken, sonst gehts nicht
        // Wir haben die X und Y Koordinate auf dem Display und wollen die X und Y Koordinate auf der Map bekommen
        // Versatz beachten
        selX = selX - 10;
        selY = selY - 15;
        // Grundposition bestimmen
        int coordX = selX / 20;
        int coordY = selY / 15;
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
        int coordX = selX / 20;
        int coordY = selY / 15;
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
        int coordX = x / 20;
        int coordY = y / 15;
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

    protected ArrayList<Unit> getBoxSelected(MouseEvent e) {
        // Wir wollen alle Einheiten im SelectionBox Feld.
        // Wir müssen die Ecken der SelectionShadows mit den Ecken der SelektionBox vergleichen
        ArrayList<Unit> finalList = new ArrayList<Unit>();
        Dimension finalDimension = new Dimension(e.getX(), e.getY());
        if (finalDimension.equals(boxselectionstart)) {
            // Wir haben gar keine Box gezogen...
            return null;
        }
        // Gleiches Tauschsystem, wie bei der drawSelBox Methode
        int dirX = finalDimension.width - this.boxselectionstart.width;
        int dirY = finalDimension.height - this.boxselectionstart.height;
        // Bpxen können nur von links oben nach rechts unten berechnet werden - eventuell Koordinaten tauschen
        if (dirX < 0 && dirY > 0) {
            // Nur x Tauschen
            int backupX = finalDimension.width;
            finalDimension.width = this.boxselectionstart.width;
            this.boxselectionstart.width = backupX;
        } else if (dirY < 0 && dirX > 0) {
            // Nur Y tauschen
            int backupY = finalDimension.height;
            finalDimension.height = this.boxselectionstart.height;
            this.boxselectionstart.height = backupY;
        } else if (dirX < 0 && dirY < 0) {
            // Beide tauschen
            int backupX = finalDimension.width;
            finalDimension.width = this.boxselectionstart.width;
            this.boxselectionstart.width = backupX;
            int backupY = finalDimension.height;
            finalDimension.height = this.boxselectionstart.height;
            this.boxselectionstart.height = backupY;
        }

        for (int i = 0; i < selectionShadows.size(); i += 3) {
            Dimension ecke1 = (Dimension) selectionShadows.get(i);
            Dimension ecke2 = (Dimension) selectionShadows.get(i + 1);
            // Wenn im Rahmen dann gut
            if (((boxselectionstart.width < ecke1.width && finalDimension.width > ecke1.width) && (boxselectionstart.height < ecke1.height && finalDimension.height > ecke1.height)) || ((boxselectionstart.width < ecke2.width && finalDimension.width > ecke2.width) && (boxselectionstart.height < ecke2.height && finalDimension.height > ecke2.height))) {
                // Einheit drin, addieren
                finalList.add((Unit) selectionShadows.get(i + 2));
            }
        }

        // Fertig, zurückgeben
        return finalList;

    }

    protected void klickedOnMiniMap(MouseEvent e) {
        // Koordinaten finden
        Dimension tempD = searchMiniMid(e.getX(), e.getY());
        // Sicht auf Mitte davon setzen
        rgi.rogGraphics.jumpTo(tempD.width - (viewX / 2), tempD.height - (viewY / 2));
    }

    protected void klickedOnMiniMap(Point p) {
        // Koordinaten finden
        Dimension tempD = searchMiniMid(p.x, p.y);
        // Sicht auf Mitte davon setzen
        rgi.rogGraphics.jumpTo(tempD.width - (viewX / 2), tempD.height - (viewY / 2));
    }

    public int getModi() {
        // liefert den aktuellen renderModus zurück
        return modi;
    }

    public void renderWayPoint(Graphics2D g2) {
        for (int i = 0; i < wayPath.size(); i++) {
            // Liste durchgehen
            // rendern
            g2.drawImage(wayPointHighlighting[0], (wayPath.get(i).X - positionX) * 20, (wayPath.get(i).Y - positionY) * 15, null);

        }
        if (wayOpenList != null) {
            for (int o = 0; o < wayOpenList.size(); o++) {
                Position pos = (Position) wayOpenList.remove();
                g2.drawImage(wayPointHighlighting[3], (pos.X - positionX) * 20, (pos.Y - positionY) * 15, null);
            }
        }
        if (wayClosedList != null) {
            for (int u = 0; u < wayClosedList.size(); u++) {
                g2.drawImage(wayPointHighlighting[2], (wayClosedList.get(u).X - positionX) * 20, (wayClosedList.get(u).Y - positionY) * 15, null);
            }
        }
    }

    public void appendOpenList(PriorityBuffer oL) {
        wayOpenList = oL;
    }

    public void appendClosedList(ArrayList<Position> cL) {
        wayClosedList = cL;
    }

    public Image calcImage(boolean forMiniMap, boolean justRefresh) {
        // Liefert normalerweise ein VolatileImage, im Savemode ein BufferedImage
        // Berechnet das Hauptbild, das aus Performancegründen als Hintergrund dient
        if (modi == 2 || byPass) {
            // Bild erstellen
            Graphics2D g2;
            Graphics2D smoothG = null;
            BufferedImage presmooth = null;
            BufferedImage tempImg010 = null;
            VolatileImage tempImg10 = null;
            if (!justRefresh) {
                cgc = getGraphicsConfiguration();
                if (saveMode) {
                    tempImg010 = cgc.createCompatibleImage((sizeX * 20) + 20, (sizeY * 15) + 25);
                    g2 = tempImg010.createGraphics();
                } else {
                    tempImg10 = cgc.createCompatibleVolatileImage((sizeX * 20) + 20, (sizeY * 15) + 25);
                    g2 = tempImg10.createGraphics();
                }
            } else {
                if (saveMode) {
                    g2 = saverenderBackground.createGraphics();
                } else {
                    g2 = renderBackground.createGraphics();
                }
            }

            if (smoothGround) {
                if (presmooth == null) {
                    presmooth = new BufferedImage((sizeX * 20) + 20, (sizeY * 15) + 25, BufferedImage.TYPE_INT_ARGB);
                }
                smoothG = presmooth.createGraphics();
            }
            // Bild komplett rendern
            // Dazu viewXY aufs ganze stellen udn werte sichern
            backupViewX = viewX;
            backupViewY = viewY;
            viewX = sizeX;
            viewY = sizeY;
            int backupPX = positionX;
            int backupPY = positionY;
            positionX = 0;
            positionY = 0;
            // Für Boden und Fix, alles andere ist dynamisch
            if (smoothGround && !forMiniMap) {
                runRenderRound("ground_tex", smoothG);
                runRenderRound("fix_tex", smoothG);
            }

            runRenderRound("ground_tex", g2);
            runRenderRound("fix_tex", g2);

            if (forMiniMap) {
                // Bei der Minimap noch die Bereiche, in denen Gebäude stehen einfärben.
                for (int i = 0; i < buildingList.size(); i++) {
                    Building tB = buildingList.get(i);
                    for (int z1 = 0; z1 < tB.z1; z1++) {
                        for (int z2 = 0; z2 < tB.z2; z2++) {
                            g2.drawImage(imgMap.get("img/game/ground_t" + tB.playerId + ".png").getImage(), (tB.position.X + z1 + z2 + tB.offsetX) * 20, (tB.position.Y - z1 + z2 + tB.offsetY) * 15, null);
                        }
                    }
                }
            }
            // Smoothing, falls erwünscht
            if (smoothGround && !forMiniMap && !justRefresh && !saveMode) {
                System.out.println("Smooting ground...");
                long stime = System.currentTimeMillis();

                VolatileImage pre2 = cgc.createCompatibleVolatileImage((sizeX * 20) + 20, (sizeY * 15) + 25);
                Graphics2D layer = pre2.createGraphics();
                layer.setBackground(Color.WHITE);
                layer.clearRect(0, 0, pre2.getWidth(), pre2.getHeight());
                VolatileImage pre3 = cgc.createCompatibleVolatileImage((sizeX * 20) + 20, (sizeY * 15) + 25);
                Graphics2D layer2 = pre3.createGraphics();
                layer2.setBackground(Color.WHITE);
                layer2.clearRect(0, 0, pre3.getWidth(), pre3.getHeight());

                // Aufwand abschätzen:
                long toGo = sizeX * sizeY;
                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        if ((x + y) % 2 == 1) {
                            continue;
                        }
                        // Alle Nachbarn dieses Feldes untersuchen - soferns die gibt
                        // Rechts unten:
                        try {
                            CoRMapElement ele = visMap[x][y];
                            if (!ele.getProperty("ground_tex").equals(visMap[x + 1][y + 1].getProperty("ground_tex"))) {
                                // Hier einen Übergang einfügen
                                Polygon poly = new Polygon();
                                poly.addPoint(x * 20 + 12, y * 15 + 38);
                                poly.addPoint(x * 20 + 22, y * 15 + 48);
                                poly.addPoint(x * 20 + 48, y * 15 + 27);
                                poly.addPoint(x * 20 + 38, y * 15 + 17);
                                layer.setColor(Color.BLACK);
                                layer.fillPolygon(poly);
                            }
                            if (!ele.getProperty("ground_tex").equals(visMap[x - 1][y + 1].getProperty("ground_tex"))) {
                                // Hier einen Übergang einfügen
                                Polygon poly = new Polygon();
                                poly.addPoint(x * 20 - 2, y * 15 + 27);
                                poly.addPoint(x * 20 + 2, y * 15 + 17);
                                poly.addPoint(x * 20 + 28, y * 15 + 38);
                                poly.addPoint(x * 20 + 18, y * 15 + 48);
                                layer.setColor(Color.BLACK);
                                layer.fillPolygon(poly);
                            }
                            if (!ele.getProperty("ground_tex").equals(visMap[x - 1][y - 1].getProperty("ground_tex"))) {
                                // Hier einen Übergang einfügen
                                Polygon poly = new Polygon();
                                poly.addPoint(x * 20 + 2, y * 15 + 33);
                                poly.addPoint(x * 20 - 2, y * 15 + 23);
                                poly.addPoint(x * 20 + 18, y * 15 + 2);
                                poly.addPoint(x * 20 + 28, y * 15 + 12);
                                layer.setColor(Color.BLACK);
                                layer.fillPolygon(poly);
                            }
                            if (!ele.getProperty("ground_tex").equals(visMap[x + 1][y - 1].getProperty("ground_tex"))) {
                                // Hier einen Übergang einfügen
                                Polygon poly = new Polygon();
                                poly.addPoint(x * 20 + 12, y * 15 + 12);
                                poly.addPoint(x * 20 + 22, y * 15 + 2);
                                poly.addPoint(x * 20 + 48, y * 15 + 23);
                                poly.addPoint(x * 20 + 38, y * 15 + 33);
                                layer.setColor(Color.BLACK);
                                layer.fillPolygon(poly);
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                        }
                    }
                }


                // Übergangsbild erzeugen
                float[] scales = {0.05f, 0.05f, 0.05f, 0.05f};
                float[] offsets = {0f, 0f, 0f, 0f};
                RescaleOp rop = new RescaleOp(scales, offsets, null);

                // Grundbild zeichnen
                layer2.drawImage(presmooth, 0, 0, null);

                int smoothX = -5;
                int smoothY = -5;

                // Jetzt versetzt drüber gehen
                while (smoothX != 5 || smoothY != 5) {
                    layer2.drawImage(presmooth, rop, smoothX, smoothY);
                    smoothX++;
                    if (smoothX > 5) {
                        smoothX = -5;
                        smoothY++;
                    }
                }

                //

                //layer2.drawImage(pre2, 0, 0, null);

                // Jetzt die Schablone, die nur die Übergänge enthält auf das normale Bild rendern
                //g2.drawImage(pre3, 0, 0, null);

                // Jetzt die beiden Bilder mergen.

                BufferedImage schl = pre2.getSnapshot();
                BufferedImage verw = pre3.getSnapshot();
                BufferedImage result = tempImg10.getSnapshot();
                int black = Color.BLACK.getRGB();
                for (int x = 0; x < tempImg10.getWidth(); x++) {
                    for (int y = 0; y < tempImg10.getHeight(); y++) {
                        if (schl.getRGB(x, y) == black) {
                            // Den Pixel aus dem Verwischten nehmen
                            result.setRGB(x, y, verw.getRGB(x, y));
                        }
                    }
                }

                g2.drawImage(result, 0, 0, null);

                System.out.println("Smoothing done. It took " + (System.currentTimeMillis() - stime) + " ms");
            }


            if (enableWaypointHighlighting) {
                renderWayPoint(g2);
            }
            viewX = backupViewX;
            viewY = backupViewY;
            positionX = backupPX;
            positionY = backupPY;
            // Bild fertig gerendert
            if (saveMode) {
                return tempImg010;
            } else {
                return tempImg10;
            }

        } else {
            return null;
        }

    }

    /**
     * Rendert eine neue Minimap - Rasend schnell, weil nichts skaliert werden muss.
     * Benutzt die GFM-Minimap als Basis
     *
     */
    public void calcMiniMap() {
        if (saveMode) {
            if (savebuildingLayer == null) {
                savebuildingLayer = cgc.createCompatibleImage((int) (hudSizeX * 0.8), (int) (realPixY / 7 * 2 * 0.8), Transparency.TRANSLUCENT);
            }
            if (savefowMiniLayer == null) {
                savefowMiniLayer = cgc.createCompatibleImage((int) (hudSizeX * 0.8), (int) (realPixY / 7 * 2 * 0.8), Transparency.TRANSLUCENT);
            }
        } else {
            if (buildingLayer == null) {
                buildingLayer = cgc.createCompatibleVolatileImage((int) (hudSizeX * 0.8), (int) (realPixY / 7 * 2 * 0.8), Transparency.TRANSLUCENT);
            }
            if (fowMiniLayer == null) {
                fowMiniLayer = cgc.createCompatibleVolatileImage((int) (hudSizeX * 0.8), (int) (realPixY / 7 * 2 * 0.8), Transparency.TRANSLUCENT);
            }
        }
        // Bildchen ist schon da:
        Graphics2D g2 = null;
        if (saveMode) {
            g2 = savebuildingLayer.createGraphics();
        } else {
            g2 = buildingLayer.createGraphics();
        }
        g2.setBackground(new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f));
        if (saveMode) {
            g2.clearRect(0, 0, savebuildingLayer.getWidth(), savebuildingLayer.getHeight());
        } else {
            g2.clearRect(0, 0, buildingLayer.getWidth(), buildingLayer.getHeight());
        }
        // Einfach noch alle Gebäude mit dem vorgescaleten Bildchen reinrendern
        for (Building building : buildingList) {
            if (building.playerId == rgi.game.getOwnPlayer().playerId || building.wasSeen) {
                for (int z1 = 0; z1 < building.z1; z1++) {
                    for (int z2 = 0; z2 < building.z2; z2++) {
                        // Hierhin das Bildchen zeichnen
                        g2.drawImage(coloredImgMap.get("img/game/ground.png" + building.playerId).getImage(), (int) ((building.position.X + z1 + z2) * 20 * maxminscaleX), (int) ((building.position.Y - z1 + z2) * 15 * maxminscaleY), null);
                    }
                }
            }

        }
    }

    public void setColMode(boolean cMode) {
        colMode = cMode;
        /* Rausgenommen, da es zu Performanceeinbußen führt, Greymaps werden jetzt beim start gecalct...
        if (cMode) {
        calcGreyMap();
        }
         * */
    }

    public Dimension searchMiniMid(int cX, int cY) {
        // Sucht die Koordinaten eines Klicks auf die Minimap, also die Koordinaten des Feldes in der Mitte der Scollbox
        // Input muss auf Minimap gefiltert sein, sonst kommt nur Müll raus
        return new Dimension((int) (((cX - (hudX + hudSizeX * 0.1)) / (hudSizeX * 0.8)) * sizeX), (int) (((cY - realPixY / 7 * 1.4) / (realPixY / 7 * 2 * 0.8)) * sizeY));
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

    public void updateRessources(java.util.List<Ressource> rL) {
        resList = rL;
        if (modi != 3) {
            repaint();
        }
    }

    public synchronized Unit identifyUnit(int sX, int sY) {
        // Sucht Einheiten richtig, arbeitet mit shadows
        for (int i = 0; i < selectionShadows.size(); i += 3) {
            Dimension t1 = (Dimension) selectionShadows.get(i);
            Dimension t2 = (Dimension) selectionShadows.get(i + 1);
            // Wenn im Rahmen dann gut
            if (sX > t1.width && sY > t1.height && sX < t2.width && sY < t2.height) {
                return (Unit) selectionShadows.get(i + 2);
            }
        }
        // Nicht gefunden
        return null;
    }

    public Building identifyBuilding(int sX, int sY) {
        // Sucht Gebäude Pixelgenau durch AdvancedSelectionShadows
        int number = advselshadB.getRGB(sX + positionX * 20, sY + positionY * 15);
        number *= -1;
        number -= 1;
        try {
            Building retb = buildingList.get(number);
            return retb;
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            return null;
        } catch (java.lang.IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public Ressource identifyRessource(int sX, int sY) {
        // Sucht Ressoucen Pixelgenau durch AdvancedSelectionShadows
        int number = advselshadB.getRGB(sX + positionX * 20, sY + positionY * 15);
        number *= -1;
        number -= (buildingList.size() + 1);
        try {
            Ressource retr = resList.get(number);
            return retr;
        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            return null;
        } catch (java.lang.IndexOutOfBoundsException ex) {
            return null;
        }
    }

    public void enableWayPointHighlighting(ArrayList<Position> path) {
        // Zeigt den Path an
        wayPath = path;
        enableWaypointHighlighting = true;
        calcImage(false, true);

    }

    /**
     * Die Updates des FoW dürfen nicht während der Bildberechnung sein, sonst flackert es.
     */
    private void updateBuildingFoW() {
        clearFogOfWar();
        for (int i = 0; i < buildingList.size(); i++) {
            Building b = buildingList.get(i);
            // FoW berechnen
            if (b.playerId == rgi.game.getOwnPlayer().playerId) {
                cutSight(b);
            }
        }
    }

    public void buildingsChanged() {
        // Muss aufgerufen werden, wenn sich die Gebäude geändert haben (neues Gebäude, altes Weg etc..)
        // AUCH WICHTIG FÜR RESSOUCENÄNDERUNGEN
        //Collections.sort(buildingList); RAUSGENOMMEN - WIRD EH DURCH BEAUTYDRAW GEMACHT
        // AdvancedSelectionShadows aktualisieren
        Graphics2D g2 = advselshadB.createGraphics();
        // Alte Sachen löschen
        g2.clearRect(0, 0, advselshadB.getWidth(), advselshadB.getHeight());
        int ib = 0;
        for (int i = 0; i < buildingList.size(); i++) {
            ib = i;
            Building b = buildingList.get(i);
            // Textur holen
            CoRImage tex = imgMap.get(b.defaultTexture);
            if (tex != null) {
                // Speziell einfärben
                BufferedImage bu = advShadCalc(i, tex);
                // Auf ShadowBuffer Zeichnen
                g2.drawImage(bu, (b.position.X - b.offsetX) * 20, (b.position.Y - b.offsetY) * 15, null);
            } else {
            }

        }
        if (buildingList.size() == 0) {
            ib--;
        }
        for (int i = 0; i < resList.size(); i++) {
            ib++;
            Ressource res = resList.get(i);
            // Textur holen
            CoRImage tex = imgMap.get(res.getTex());
            if (tex != null) {
                // Speziell einfärben
                BufferedImage bu = advShadCalc(ib, tex);
                // Auf ShadowBuffer Zeichnen
                if (res.getType() < 3) {
                    g2.drawImage(bu, (res.position.X) * 20, (res.position.Y) * 15, null);
                } else {
                    g2.drawImage(bu, (res.position.X) * 20, (res.position.Y - 2) * 15, null);
                }
            }
        }
        // Minimap updaten, damit Gebäude korrekt angezeigt werden
        updateMiniMap();
        // FoW updaten
        updateBuildingsFow = true;
    }

    private void cutSight(Building b) {
        // Mitte berechnen
        int x = (int) (b.position.X + ((b.z1 + b.z2 - 2) * 1.0 / 2));
        int y = b.position.Y;
        // Diese Position als Startfeld überhaupt zulässig?
        if (((int) x + (int) y) % 2 == 1) {
            y++;
        }
        // Dieses Feld selber auch ausschneiden
        fowmap[x][y] = 2;
        cutCircleFast(b.getVisrange() + ((b.z1 + b.z2) / 4), new Position(x, y), true);
    }

    private void cutSight(Unit unit) {
        // Mitte berechnen
        int x = unit.position.X;
        int y = unit.position.Y;
        // Dieses Feld selber auch ausschneiden
        byte val = fowmap[x][y];
        if (val == 0 || val == 1) {
            fowmap[x][y] = 3;
        }
        cutCircleFast(unit.getVisrange(),unit.position, false);
    }

    /**
     * Schneidet schnell Kreise in den FoW.
     * Funktioniert NUR mit Ranges von 1-20!!!, weil schablonenbasiert
     * @param range Sichtweite in ganzen Feldern (1-20)
     * @param origin Mitte
     * @param building Für Gebäude (true) oder Einheit (false)
     */
    public void cutCircleFast(int range, Position origin, boolean building) {
        // Schablone holen
        boolean[][] pattern = fowpatmgr.getPattern(range);
        // Schablone anwenden
        int sx = origin.X - 40;
        int sy = origin.Y - 40;
        for (int x = 0; x < 80; x++) {
            for (int y = 0; y < 80; y++) {
                if (pattern[x][y]) {
                    directCut(sx + x, sy + y, !building);
                }
            }
        }
    }

    private void directCut(int X, int Y, boolean checkUp) {
        if (!checkUp) {
            try {
                fowmap[X][Y] = 2;
            } catch (ArrayIndexOutOfBoundsException ex) {
            }
        } else {
            try {
                byte val2 = fowmap[X][Y];
                if (val2 == 0 || val2 == 1) { // Wenn unerforscht oder vergessen
                    fowmap[X][Y] = 3;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
            }
        }
    }

    /**
     * Muss regelmäßig vom Grafikmodul aufgerufen werden (4 mal pro Sekunde oder so)
     * Aktualisiert die gesamte FoW-Map auf Einheiten-Sichtweiten
     */
    protected void calcFogOfWar() {
        // Zuerst alles mal von Einheiten gesehene vergessen
        for (int x = 0; x < fowmap.length; x++) {
            for (int y = 0; y < fowmap[0].length; y++) {
                byte v = fowmap[x][y];
                if (fowmap[x][y] == 3) {
                    fowmap[x][y] = 1;
                }

            }
        }
        // Für alle Einheiten auf der Map
        int ownPlayerId = rgi.game.getOwnPlayer().playerId;
        for (int i = 0; i < unitList.size(); i++) {
            Unit unit = unitList.get(i);
            if (unit.playerId != ownPlayerId) {
                // Nur eigene Einheiten decken den Nebel des Krieges auf
                continue;
            }
            cutSight(unit);
        }
    }

    public void calcSelClicked(MouseEvent e, ArrayList<GameObject> list, boolean select) {
        // Es gibt 12 Möglichkeiten, aber wir müssen nur auf Reihen uns Spalten Testen, also 7 Test
        int kX = e.getX();
        int kY = e.getY();
        int calc = -500; // Sehr niedrigen Wert ansetzten, damit ein plus in Y nicht ein fehlendes X erstzen kann
        // Spalten - Xwerte
        if (kX >= hudX + (int) (hudSizeX * 0.15) && kX <= hudX + (hudSizeX * 0.15 + hudSizeX * 0.7 / 5)) {
            calc = 0;
        } else if (kX >= hudX + (int) (hudSizeX * 0.15 + 1 * (hudSizeX * 0.7 * 4 / 15)) && kX <= hudX + (hudSizeX * 0.15 + 1 * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5)) {
            calc = 1;
        } else if (kX >= hudX + (int) (hudSizeX * 0.15 + 2 * (hudSizeX * 0.7 * 4 / 15)) && kX <= hudX + (hudSizeX * 0.15 + 2 * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5)) {
            calc = 2;
        } else if (kX >= hudX + (int) (hudSizeX * 0.15 + 3 * (hudSizeX * 0.7 * 4 / 15)) && kX <= hudX + (hudSizeX * 0.15 + 3 * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5)) {
            calc = 3;
        }
        // Reihen
        if (kY >= hudX + (int) (realPixY * 2 / 7 * 0.2 + 1 * (realPixY * 48 / 560)) && kY <= hudX + (int) (realPixY * 2 / 7 * 0.2 + 1 * (realPixY * 48 / 560) + realPixY * 2 / 7 * 0.7 / 4)) {
            calc += 4;
        } else if (kY >= hudX + (int) (realPixY * 2 / 7 * 0.2 + 2 * (realPixY * 48 / 560)) && kY <= hudX + (int) (realPixY * 2 / 7 * 0.2 + 2 * (realPixY * 48 / 560) + realPixY * 2 / 7 * 0.7 / 4)) {
            calc += 8;
        }
        if (calc >= 0) {
            // Gut wir haben da was!
            if (calc < interSelFields.size()) {
                GameObject[] klicked = interSelFields.get(calc);
                if (klicked != null) {
                    if (select) {
                        // Gut, diese Kategorie gibts auch, nur diese Anwählen
                        // Erstmal alle löschen
                        for (GameObject obj : list) {
                            obj.isSelected = false;
                        }
                        list.clear();
                        for (GameObject newobj : klicked) {
                            list.add(newobj);
                            newobj.isSelected = true;
                        }
                        // Fertig
                    } else {
                        for (GameObject newobj : klicked) {
                            list.remove(newobj);
                            newobj.isSelected = false;
                        }
                    }
                }
            }
        }
    }

    public void calcScaledBuildingLocations(double scalefactorX, double scalefactorY) {
        // Alle Bilder gemäß des Scale-Faktors skalieren und einfügen
        // Scalefacor = MiniMap-Auflösung / Volle Hudgroundauflösung
        // Größe der Bilder errechnen:
        int tarX = (int) (40 * scalefactorX);
        int tarY = (int) (40 * scalefactorY);
        // Bilder anlegen & gleich reinrendern
        BufferedImage nimg = cgc.createCompatibleImage(tarX + 1, tarY + 1, Transparency.TRANSLUCENT);
        Graphics2D g = nimg.createGraphics();
        g.drawImage(coloredImgMap.get("img/game/ground.png").getImage(), 0, 0, nimg.getWidth(), nimg.getHeight(), 0, 0, 40, 40, null);
        CoRImage timg = new CoRImage(nimg);
        timg.setImageName("img/game/ground.png");
        coloredImgMap.put("img/game/ground.png", timg);
        maxminscaleX = scalefactorX;
        maxminscaleY = scalefactorY;
        // Fertig
    }

    public void calcOptClicked(MouseEvent e, ArrayList<GameObject> list) {
        // Es gibt 12 Möglichkeiten, aber wir müssen nur auf Reihen uns Spalten Testen, also 7 Test
        int kX = e.getX();
        int kY = e.getY();
        int calc = -500; // Sehr niedrigen Wert ansetzten, damit ein plus in Y nicht ein fehlendes X erstzen kann
        // Spalten - Xwerte
        if (kX >= hudX + (int) (hudSizeX * 0.15) && kX <= hudX + (hudSizeX * 0.15 + hudSizeX * 0.7 / 5)) {
            calc = 0;
        } else if (kX >= hudX + (int) (hudSizeX * 0.15 + 1 * (hudSizeX * 0.7 * 4 / 15)) && kX <= hudX + (hudSizeX * 0.15 + 1 * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5)) {
            calc = 1;
        } else if (kX >= hudX + (int) (hudSizeX * 0.15 + 2 * (hudSizeX * 0.7 * 4 / 15)) && kX <= hudX + (hudSizeX * 0.15 + 2 * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5)) {
            calc = 2;
        } else if (kX >= hudX + (int) (hudSizeX * 0.15 + 3 * (hudSizeX * 0.7 * 4 / 15)) && kX <= hudX + (hudSizeX * 0.15 + 3 * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5)) {
            calc = 3;
        }
        // Reihen
        if (kY >= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8) / 4 * 1.5)) && kY <= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8) / 4 * 2.5))) {
            calc += 4;
        } else if (kY >= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8) / 4 * 3)) && kY <= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8)))) {
            calc += 8;
        }
        if (calc >= 0 && calc < optList.size()) {
            // Gut wir haben da was!
            // Fähigkeit holen
            Ability ab = null;
            try {
                ab = optList.get(calc);
            } catch (java.lang.IndexOutOfBoundsException ex) {
            }
            if (ab != null && list.get(0).ready) {
                // Mouse weg, damit Hover verschwindet:
                if (ab.removeHoverAfterUse) {
                    mouseX = 50;
                    mouseY = 50;
                }
                // Für die Erste GO ausführen
                // Invoker setzen
                ab.setInvoker(list.get(0));
                // Fähigkeit starten
                if (e.getButton() == 1) {
                    if (ab.isAvailable()) {
                        ab.perform(list.get(0));
                    }
                } else if (e.getButton() == 3) {
                    ab.antiperform(list.get(0));
                }
                // Für noch mehr ausführen?
                if (ab.useForAll) {
                    for (int i = 1; i < list.size(); i++) {
                        Ability abr = list.get(i).abilitys.get(calc);
                        if (abr != null) {
                            if (e.getButton() == 1) {
                                if (abr.isAvailable()) {
                                    abr.perform(list.get(i));
                                }
                            } else if (e.getButton() == 3) {
                                abr.antiperform(list.get(i));
                            }
                        }
                    }
                }
                // IA-Hud triggern, wegen eventuellem Progress-Rendern
                this.updateInterHud = true;

            }
        }
    }

    /**
     * Sucht die Ability heraus, über der derzeit die Maus steht.
     * Diese Methode ist auf Geschwindigkeit optimiert.
     * @return
     */
    private Ability searchOptFast() {
        // Es gibt 12 Möglichkeiten, aber wir müssen nur auf Reihen uns Spalten Testen, also 7 Test
        int calc = -500; // Sehr niedrigen Wert ansetzten, damit ein plus in Y nicht ein fehlendes X ersetzen kann
        // Rechenzeit sparen durch vorrechnen
        int hsx1 = (int) (hudSizeX * 0.15);
        int hsx2 = (int) (hudSizeX * 14 / 75);
        // Spalten - Xwerte
        if (mouseX >= hudX + (int) (hsx1) && mouseX <= hudX + (hsx1 + hudSizeX * 0.7 / 5)) {
            calc = 0;
        } else if (mouseX >= hudX + (int) (hsx1 + 1 * hsx2) && mouseX <= hudX + (hsx1 + 1 * hsx2 + hudSizeX * 0.7 / 5)) {
            calc = 1;
        } else if (mouseX >= hudX + (int) (hsx1 + 2 * hsx2) && mouseX <= hudX + (hsx1 + 2 * hsx2 + hudSizeX * 0.7 / 5)) {
            calc = 2;
        } else if (mouseX >= hudX + (int) (hsx1 + 3 * hsx2) && mouseX <= hudX + (hsx1 + 3 * hsx2 + hudSizeX * 0.7 / 5)) {
            calc = 3;
        }
        // Reihen
        if (mouseY >= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1) && mouseY <= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8) / 4))) {
            // Nix
        } else if (mouseY >= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8) / 4 * 1.5)) && mouseY <= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8) / 4 * 2.5))) {
            calc += 4;
        } else if (mouseY >= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8) / 4 * 3)) && mouseY <= (int) (realPixY * 5 / 7 + realPixY * 2 / 7 * 0.1 + ((realPixY * 2 / 7 * 0.8)))) {
            calc += 8;
        } else {
            calc = -500;
        }
        if (calc >= 0 && calc < optList.size()) {
            // Gut wir haben da was!
            // Fähigkeit holen
            Ability ab = null;
            try {
                ab = optList.get(calc);
                if (rgi.rogGraphics.inputM.selected.get(0).ready) {
                    return ab;
                }
            } catch (java.lang.IndexOutOfBoundsException ex) {
            }
        }
        return null;
    }

    private BufferedImage advShadCalc(int number, CoRImage shadow) {
        // Berechnet die Farbe eines Elements der AdvancedSelectionShadows
        // Diese ermöglichen pixelgenaues Selektieren von Gebäuden
        // Hierzu wird ein Bild mit der vollen Auflösung der Map erstellt -
        // Alle Gebäude werden exakt eingezeichnet, aber mit einer speziellen, Computergenerierten Farbe.
        // Wir nun irgendwo hingelickt, muss nur die Farbinformation auf diesem Pixel bestimmt werden,
        // Anhand dieser kann das Gebäude gefunden werden...
        // Aus mysteriösen Gründen gehen nur negative Zahlen..
        number += 1;
        number *= -1;
        // Ganzes Bild Einfärben:
        BufferedImage old = shadow.getImage();
        BufferedImage edit = new BufferedImage(old.getWidth(), old.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < edit.getWidth(); x++) {
            for (int y = 0; y < edit.getHeight(); y++) {
                // Ist da die Transparenz 100%?
                if (((old.getRGB(x, y) >> 24) & 0xff) == 0) {
                    // Dann gehört das nicht zum Selshad
                    edit.setRGB(x, y, 0);
                } else {
                    // Sonst schon...
                    edit.setRGB(x, y, number);
                }
            }
        }
        return edit;

    }

    private void checkVolatile() {
        // Überprüft die VolatileImages auf Konsistenz und läd sie bei Bedarf neu.
        if (renderBackground != null && renderBackground.contentsLost()) {
            System.out.print(System.currentTimeMillis() + " ");
            System.out.print("DING-V B1:");
            System.out.println(renderBackground.validate(cgc));
            this.renderBackgroundChanged();
        }
        if (interactivehud != null && interactivehud.contentsLost()) {
            System.out.print(System.currentTimeMillis() + " ");
            System.out.print("DING-V B2:");
            System.out.println(interactivehud.validate(cgc));
            updateInterHud = true;
        }
        if (hudGround != null && hudGround.contentsLost()) {
            System.out.print(System.currentTimeMillis() + " ");
            System.out.print("DING-V B3:");
            System.out.println(hudGround.validate(cgc));
            mergeHud();
        }
        if (miniMap != null && miniMap.contentsLost()) {
            System.out.print(System.currentTimeMillis() + " ");
            System.out.print("DING-V B4:");
            System.out.println(miniMap.validate(cgc));
            transformMiniMap();

        }
        if (buildingLayer != null && buildingLayer.contentsLost()) {
            System.out.print(System.currentTimeMillis() + " ");
            System.out.print("DING-V B5:");
            System.out.println(buildingLayer.validate(cgc));
            calcMiniMap();
        }
        if (fowMiniLayer != null && fowMiniLayer.contentsLost()) {
            System.out.print(System.currentTimeMillis() + " ");
            System.out.print("DING-V B6:");
            System.out.println(fowMiniLayer.validate(cgc));
            System.out.println("FixMe: Rebuild FoW-Layer");
        }
    }

    private static String transformTime(int time) {
        // Stellt die Zeit als schönen String da
        int secs = (int) (time / 1000);
        if (secs < 60) {
            return secs + " s";
        } else if (secs < 3600) {
            int a;
            return (secs / 60) + " min " + secs % 60 + " s";
        } else {
            int a = secs / 3600;
            int b = secs % 3600;
            return a + " h " + b / 60 + " min " + b % 60 + " s";
        }
    }

    protected void appendCoreInner(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    protected void resortAllList() {
        // Sortiert die Liste neu, ist super, braucht aber leider viel Leistung, wird deshalb nicht bei jedem Frame aufgerufen
        Collections.sort(allList);
    }

    public GraphicsComponent() {
        cgc = getGraphicsConfiguration();
        selectionShadows = new ArrayList();
        interSelFields = new ArrayList<GameObject[]>();
        grayImgMap = new HashMap<String, CoRImage>();
        coloredImgMap = new HashMap<String, CoRImage>();
        wayPointHighlighting = new BufferedImage[4];
        try {
            wayPointHighlighting[0] = ImageIO.read(new File("img/notinlist/editor/colmode.png"));
            wayPointHighlighting[1] = ImageIO.read(new File("img/notinlist/editor/highlight_yellow.png"));
            wayPointHighlighting[2] = ImageIO.read(new File("img/notinlist/editor/highlight_orange.png"));
            wayPointHighlighting[2] = ImageIO.read(new File("img/notinlist/editor/highlight_blue.png"));

        } catch (IOException ex) {
            System.out.println("Can't init waypoint-highlighting!");
        }
        // Fonts initialisieren
        fonts = new Font[5];
        fonts[0] = Font.decode("Mono-10");
        fonts[1] = Font.decode("Mono-Bold-12");
        fonts[2] = Font.decode("Mono-Bold-10");
        fonts[3] = Font.decode("Mono-Italic-10");
        fonts[4] = Font.decode("Mono-8");

        optList = new ArrayList<Ability>();

        fowpatmgr = new GraphicsFogOfWarPattern();
    }
}
