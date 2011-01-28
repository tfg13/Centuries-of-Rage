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

import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.collections.buffer.PriorityBuffer;
import org.lwjgl.opengl.Display;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;
import org.newdawn.slick.opengl.renderer.Renderer;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.AbilityBuild;
import thirteenducks.cor.game.ability.AbilityIntraManager;
import thirteenducks.cor.game.ability.AbilityRecruit;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.tools.mapeditor.MapEditorCursor;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.graphics.input.InteractableGameElement;

@SuppressWarnings("CallToThreadDumpStack")
public class GraphicsContent extends BasicGame {
    // Diese Klasse repräsentiert den Tatsächlichen GrafikINHALT von RogGraphics und RogMapEditor

    GraphicsImage defaultimage;
    public GraphicsImage colModeImage;
    Color color;
    public int modi = 0; // Was gerendert werden soll, spezielle Ansichten für den Editor etc...
    CoRMapElement[][] visMap; // Die angezeigte Map
    public boolean renderMesh = false; // Gitter rendern
    public boolean renderGround = false; // Boden rendern
    public boolean renderObjects = false; // Objecte (Bäuume etc) rendern
    public boolean renderCreeps = false; // Einheiten rendern
    public boolean renderBuildings = false; // Gebäude rendern
    public boolean renderCursor = false;
    public boolean renderPicCursor = false; // Cursor, der ein Bild anzeigt, z.B. ein Haus, das gebaut werden soll
    public boolean renderRessources = false;
    public boolean renderFogOfWar = false;
    public GraphicsImage renderPic;
    public int sizeX; // Mapgröße
    public int sizeY;
    public int positionX; // Position, die angezeigt wird
    public int positionY;
    public int viewX; // Die Größe des Angezeigten ausschnitts
    public int viewY;
    int backupViewX; // Muss manchmal umgestellt werden, wobei das alte behalten werden soll...
    int backupViewY;
    public int realPixX; // Echte, ganze X-Pixel
    public int realPixY; // Echte, ganze Y-Pixel
    public HashMap<String, GraphicsImage> imgMap; // Enthält alle Bilder
    public HashMap<String, GraphicsImage> coloredImgMap;
    Image renderBackground = null; // Bodentextur, nur Bild-Auflösung
    int rBvX; // Position während der letzen Bildberechnung
    int rBvY; // Position während der letzen Bildberechnung
    Image interactivehud = null;
    public boolean colMode = false; // Modus, der die Kollision anzeigt
    public boolean serverColMode = false;
    public boolean byPass = false; // Speziell, damit einige NULLPOINTER Kontrollen übergangen werden können
    boolean useAntialising = false; // Kantenglättung, normalerweise AUS // NUR IM NORMALEN RENDERMODE
    java.util.List<Unit> unitList;
    java.util.List<Building> buildingList;
    public List<Sprite> allList;
    public ArrayList<GameObject> selectedObjects;
    boolean newMode = true; // Units in Liste statt als Textur
    ArrayList<Position> wayPath; // Ein weg
    boolean enableWaypointHighlighting;
    public boolean alwaysshowenergybars = false; // Energiebalken immer anzeigen, WC3 lässt grüßen.
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
    int[][] brSel;
    Date[] analyze;
    private Image hudGround; // Hud-Hauptbild, hängt von epoche ab
    private Image fowMiniLayer;  // FoW der Minimap
    private Image fowMiniLayer2;  // FoW der Minimap
    public int epoche = 2; // Null ist keine Epoche, also kein Gescheites Grundbild..
    public int hudX; // X-Koordinate vom Hud, damits nicht dauernd neu ausgerechnet werden muss...
    public int hudSizeX; // Hängt vom oberen ab
    private Image miniMap; // Hud-Minimap
    private ClientCore.InnerClient rgi;
    private int miniMapViewSizeX = 10;
    private int miniMapViewSizeY = 10;
    
    
    Image wayPointHighlighting[];
    private static final boolean beautyDraw = true;     // Schöner zeichnen durch umsortieren der allList, kostet Leistung
    public Image[] huds;                               // Die Huds für verschiedene Epochen
    int lastMenuHash;                                   // Das Hud-Menü wird nur neu gezeichnet, wenn sich der Hash der selektieren Einheiten verändert.
    ArrayList<GameObject[]> interSelFields;          // Die Selektierten Einheiten als Representation auf dem Hud
    Image[] scaledBuildingLocation;             // Die Größe eines Gebäudes, in skalierter Version
    Image buildingLayer;                        // Layer für Gebäude auf der Minimap
    double maxminscaleX;
    double maxminscaleY;
    public boolean updateInterHud = false;
    public int mouseX;                                         // Die Position der Maus, muss geupdatet werden
    public int mouseY;                                         // Die Position der Maus, muss geupdatet werden
    int lastHovMouseX;
    int lastHovMouseY;
    TrueTypeFont[] fonts;                                       // Die Fonts, die häufig benötigt werden
    public boolean pauseMode = false;                          // Pause-Modus
    public boolean statisticsMode = false;
    Color fowGray = new Color(0.0f, 0.0f, 0.0f, 0.4f);
    long pause;                                         // Zeitpunkt der letzen Pause
    public boolean saveMode = false;                    // Sicherer Grafikmodus ohne unsichere, weil zu Große Bilder
    public boolean coordMode = false;                          // Modus, der die Koordinaten der Felder mit anzeigt.
    public boolean idMode = false;                             // Modus, der die NetIds von Einheiten/Gebäuden und Ressourcen mit anzeigt.
    public int unitDestMode = 0;                               // Modus, der die Ziele von Einheiten (Bewegung & Angriff) anzeigt (Aus/Eigene/Fremde/Alle)0123
    boolean smoothGround = false;                       // Grobe Ecken aus der Bodentextur rausrechnen
    ArrayList<Ability> optList;            // Liste der derzeit anklickbaren Fähigkeiten
    public GameObject tempInfoObj;                          // Über dieses Objekt werden Temporär Infos angezeigt.
    public byte[][] fowmap;                                    // Fog of War - Map
    GraphicsFogOfWarPattern fowpatmgr;                  // Managed und erstellt Fow-Pattern
    boolean updateBuildingsFow = false;                 // Lässt den Gebäude-Fow neu erstellen
    public boolean buildingsChanged = false;
    public boolean epocheChanged = false;
    public boolean fowDisabled = false;
    public int loadStatus = 0;
    public boolean loadWait = false;
    public Image loading_backblur;                          // Ladebild
    public Image loading_backnoblur;
    public Image loading_frontnoblur;
    public Image loading_frontblur;
    public Image loading_sun_blur;
    public Image loading_backblur_s;                          // Ladebild
    public Image loading_backnoblur_s;
    public Image loading_frontnoblur_s;
    public Image loading_frontblur_s;
    long loadZoom1StartTime = 0;
    long loadZoom2StartTime = 0;
    boolean zoomInGame = false;
    boolean loadScaled = false;
    public int imgLoadCount = 0;
    public int imgLoadTotal = 0;
    CoreGraphics parent;
    public int initState = 0;                          // Damit alles Slick laden kann, sonst weigert es sich zu arbeiten :(
    // Slick ist monoTreaded...
    // Die Grafik muss also auch manche Input-Events berechnen:
    public int lastInputEvent = 0;
    public int lastInputX = 0;
    public int lastInputY = 0;
    public int lastInputButton = 0;
    // Startbildschirm - Error:
    public boolean launchError = false;
    public boolean launchScreenGrayed = false;
    public int lEtype = 0; // Fehler, siehe
    public int gameDone = 0; // Spiel zu Ende? 1=ja, mit Sieg; 2 = ja mit Niederlage; 3 = nein, aber trotzdem verloren
    public long endTime;
    public ReentrantLock allListLock;
    Unit hoveredUnit;
    public ArrayList<Overlay> overlays;
    long lastFowMiniRender;
    public GraphicsFireManager fireMan;
    int lastDelta;

    public void paintComponent(Graphics g) {
        //Die echte, letzendlich gültige paint-Methode, sollte nicht direkt aufgerufen werden
        if (modi == -1) {
            // Ladebildschirm (pre-Game)
            renderLoadScreen(g);
        } else if (modi == 0) {
            // Gar nix rendern
        } else if (modi == 1) {
            // Bestimmted Bild (defaultimage) rendern
            g.drawImage(defaultimage.getImage(), 0, 0);
        } else if (modi == 2) {
            // Editor Mode. Done by GraphicsComponent&Java2D
        } else if (modi == 3) {
            try {
                if (pauseMode) {
                    // Pause für diesen Frame freischalten:
                    pause = rgi.rogGraphics.getPauseTime();
                }
                // FoW updaten?
                if (!fowDisabled && updateBuildingsFow) {
                    updateBuildingsFow = false;
                    this.updateBuildingFoW();
                }
                g.setAntiAlias(false);
                // Für die Haupt-grafikausgabe
                if (useAntialising) {
                    g.setAntiAlias(true);
                }
                //g3 = tempImg.getGraphics();
                // Alles löschen
                g.setColor(Color.black);
                g.fillRect(0, 0, hudX, realPixY);

                renderBackground();
                if (beautyDraw) {
                    try {
                        allListLock.lock();
                        Collections.sort(allList);
                    } catch (Exception ex) {
                        System.out.println("FixMe: SortEX-reg");
                    } finally {
                        allListLock.unlock();
                    }
                }
                //renderUnits(g3);
                //renderBuildings(g3);
                renderBuildingWaypoint();

                if (mouseX != lastHovMouseX || mouseY != lastHovMouseY) {
                    // Wenn sichs geändert hat neu suchen
                    hoveredUnit = identifyUnit(mouseX, mouseY);
                    lastHovMouseX = mouseX;
                    lastHovMouseY = mouseY;
                }

                synchronized (this) {
                    renderBuildingMarkers(g);
                    if (unitDestMode != 0) {
                        renderUnitDest(g);
                    }
                    g.setWorldClip(0, 0, hudX, realPixY);
                    renderGraphicElements(g);
                    g.clearWorldClip();

                }

                fireMan.renderFireEffects(buildingList, lastDelta, positionX, positionY);


                renderHealthBars(g);
                // Fog of War rendern, falls aktiv
                if (renderFogOfWar) {
                    renderFogOfWar(g);
                }
                if (renderPicCursor) {
                    renderCursor();
                }
                if (this.dragSelectionBox) {
                    renderSelBox(g);
                }
                if (idMode) {
                    renderIds(g);
                }
                renderHud(g);
                // Mouse-Hover rendern
                if (pauseMode) {
                    renderPause(g);
                } else {
                    renderMouseHover(g);
                }
                if (statisticsMode) {
                    renderStatistics(g);
                }
                g.setColor(Color.darkGray);
                g.setFont(fonts[0]);
                //g3.setFont(new UnicodeFont(java.awt.Font.decode("8")));
                g.drawString("13 Ducks Entertainment's: Centuries of Rage BETA", 10, realPixY - 20);
                if (colMode) {
                    this.renderCol();
                }
                if (serverColMode) {
                    this.renderServerCol();
                }
                if (coordMode) {
                    renderCoords(g);
                }

                // Overlays rendern:
                renderOverlays(g);

                if (gameDone != 0) {
                    if (gameDone == 3) {
                        // DEFEATED einblenden - ne Weile groß in der Mitte, dann kleiner - man kann nämlich noch spec sein
                        if (System.currentTimeMillis() - endTime < 5000) {
                            g.drawImage(hudX >= 800 ? imgMap.get("img/game/finish_defeat_spec.png").getImage() : imgMap.get("img/game/finish_defeat_spec.png").getImage().getScaledCopy(hudX, (int) ((1.0 * hudX / 800) * 600)), hudX >= 800 ? (hudX / 2) - 400 : 0, hudX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * hudX / 800) * 600)) / 2);
                        } else {
                            g.setColor(Color.black);
                            g.setFont(fonts[5]);
                            g.drawString("DEFEAT", 10, realPixY - 40);
                        }
                    } else if (gameDone == 2) {
                        // DEFEATED einblenden - bis der Spieler das Spiel beendet
                        g.drawImage(hudX >= 800 ? imgMap.get("img/game/finish_defeat_gameover.png").getImage() : imgMap.get("img/game/finish_defeat_gameover.png").getImage().getScaledCopy(hudX, (int) ((1.0 * hudX / 800) * 600)), hudX >= 800 ? (hudX / 2) - 400 : 0, hudX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * hudX / 800) * 600)) / 2);
                    } else if (gameDone == 1) {
                        // VICTORY
                        g.drawImage(hudX >= 800 ? imgMap.get("img/game/finish_victory_gameover.png").getImage() : imgMap.get("img/game/finish_victory_gameover.png").getImage().getScaledCopy(hudX, (int) ((1.0 * hudX / 800) * 600)), hudX >= 800 ? (hudX / 2) - 400 : 0, hudX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * hudX / 800) * 600)) / 2);
                    }
                }

                // Epischer ZoomEffekt?
                if (zoomInGame) {
                    renderLoadScreen(g);
                }


                //g.drawImage(tempImg, 0, 0);
                // Fertig, Volatiles prüfen
                /*if (!saveMode) {
                checkVolatile();
                } */

            } catch (Exception ex) {
                System.out.println("Error while rendering frame, dropping.");
                ex.printStackTrace();
            }
            // Pause zurücksetzten
            pause = 0;
        } else if (modi == 4) {
            // Debug-zeichnen
            paint_debug(g);
        }
    }

    /**
     * Zeichnet alle Health-Bars.
     * @param g2
     */
    private void renderHealthBars(Graphics g2) {
        for (int i = 0; i < buildingList.size(); i++) {
            Building b = buildingList.get(i);
            if (b.isSelected || (this.alwaysshowenergybars && b.wasSeen)) {
                this.renderHealth(b, g2, (b.position.X - positionX) * 20, (b.position.Y - positionY) * 15);
            }
        }
        for (int i = 0; i < unitList.size(); i++) {
            Unit u = unitList.get(i);
            if (u.isSelected || (this.alwaysshowenergybars && u.alive)) {
                // Ist diese Einheit gerade sichtbar? (nur eigene oder sichtbare zeichnen)
                if (u.playerId == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(u, rgi.game.getOwnPlayer()) || fowmap[u.position.X][u.position.Y] > 1) {
                    Position tempP = null;
                    if (u.isMoving()) {
                        tempP = u.getMovingPosition(rgi, positionX, positionY);
                    } else {
                        tempP = new Position((u.position.X - positionX) * 20, (u.position.Y - positionY) * 15);
                    }
                    renderHealth(u, g2, tempP.X, tempP.Y);
                }
            }
        }
    }

    private void renderOverlays(Graphics g2) {
        // Zeichnet alle Overlays.
        for (int i = 0; i < overlays.size(); i++) {
            try {
                overlays.get(i).renderOverlay(g2, realPixX, realPixY, hudX);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Zeichnet die Bewegungs und Angriffsziele aller Einheiten an, zum Debuggen
     * @param g2
     */
    private void renderUnitDest(Graphics g2) {
        int mode = unitDestMode;
        g2.setLineWidth(2);
        for (int x = 0; x < unitList.size(); x++) {
            Unit unit = unitList.get(x);
            // Modus abfragen:
            if (mode == 3 || (mode == 1 && unit.playerId == rgi.game.getOwnPlayer().playerId) || (mode == 2 && unit.playerId != rgi.game.getOwnPlayer().playerId)) {
                // Zuerst Bewegung
                if (unit.movingtarget != null) {
                    // Markieren, ob Einheit flieht oder nicht
                    if (unit.getbehaviourC(4).active) {
                        // Angry, roter Punkt
                        g2.setColor(Color.red);
                    } else {
                        g2.setColor(Color.blue);
                    }
                    // Source (unit selber)
                    Position pos = unit.getMovingPosition(rgi, positionX, positionY);
                    int sx = pos.X;
                    int sy = pos.Y;
                    g2.fillRect(sx, sy, 5, 5);
                    // Target (ziel)
                    // Diese 2. Abfrage ist sinnvoll und notwendig, weil getMovingPosition am Ende der Bewegung das movingtarget löscht
                    if (unit.movingtarget != null) {
                        int tx = (unit.movingtarget.X - positionX) * 20;
                        int ty = (unit.movingtarget.Y - positionY) * 15;
                        // Zeichnen
                        g2.setColor(new Color(3, 100, 0)); // Dunkelgrün
                        g2.drawLine(sx + 19, sy + 25, tx + 19, ty + 25);
                    }
                }
                // Jetzt Angriff
                if (unit.attacktarget != null) {
                    g2.setColor(Color.red);
                    // Source (unit selber)
                    int sx = (unit.position.X - positionX) * 20;
                    int sy = (unit.position.Y - positionY) * 15;
                    if (unit.isMoving()) {
                        Position pos = unit.getMovingPosition(rgi, positionX, positionY);
                        sx = pos.X;
                        sy = pos.Y;
                    }
                    // Target (ziel)
                    int tx = 0;
                    int ty = 0;
                    if (unit.attacktarget.getClass().equals(Unit.class)) {
                        Unit target = (Unit) unit.attacktarget;
                        if (target.isMoving()) {
                            Position pos = target.getMovingPosition(rgi, positionX, positionY);
                            tx = pos.X;
                            ty = pos.Y;
                        } else {
                            tx = (target.position.X - positionX) * 20;
                            ty = (target.position.Y - positionY) * 15;
                        }
                    } else if (unit.attacktarget.getClass().equals(Building.class)) {
                        Building target = (Building) unit.attacktarget;
                        //Gebäude-Mitte finden:
                        float bx = 0;
                        float by = 0;
                        //Z1
                        //Einfach die Hälfte als Mitte nehmen
                        bx = target.position.X + ((target.z1 - 1) * 1.0f / 2);
                        by = target.position.Y - ((target.z1 - 1) * 1.0f / 2);
                        //Z2
                        // Einfach die Hälfte als Mitte nehmen
                        bx += ((target.z2 - 1) * 1.0f / 2);
                        by += ((target.z2 - 1) * 1.0f / 2);
                        // Gebäude-Mitte gefunden
                        tx = (int) ((bx - positionX) * 20) - 19;
                        ty = (int) ((by - positionY) * 15) - 25;
                    }
                    // Zeichnen
                    g2.drawLine(sx + 19, sy + 25, tx + 19, ty + 25);
                }
            }
        }
        g2.setLineWidth(1);
    }

    /**
     * Zeichnet NetIds von allen Einheiten / Gebäuden / Ressourcen
     * Zum Debugen. Funktioniert nur, wenn die Debug-Tools (cheats) aktiviert sind
     * @param g2
     */
    private void renderIds(Graphics g2) {
        g2.setColor(Color.cyan);
        g2.setFont(fonts[4]);
        // Einheiten durchgehen
        for (int i = 0; i < unitList.size(); i++) {
            Unit unit = unitList.get(i);
            if (unit.isMoving()) {
                Position pos = unit.getMovingPosition(rgi, positionX, positionY);
                g2.drawString(String.valueOf(unit.netID), pos.X + 10, pos.Y + 16);
            } else {
                g2.drawString(String.valueOf(unit.netID), (unit.position.X - positionX) * 20 + 10, (unit.position.Y - positionY) * 15 + 16);
            }
        }
        // Gebäude
        for (int i = 0; i < buildingList.size(); i++) {
            Building building = buildingList.get(i);
            g2.drawString(String.valueOf(building.netID), (building.position.X - positionX) * 20 + 10, (building.position.Y - positionY) * 15 + 16);
        }
        // Ressourcen
        for (int i = 0; i < resList.size(); i++) {
            Ressource res = resList.get(i);
            g2.drawString(String.valueOf(res.netID), (res.position.X - positionX) * 20 + 10, (res.position.Y - positionY) * 15 + 16);
        }
    }

    private void renderLoadScreen(Graphics g2) {
        // Löschen
        if (loadStatus < 10) {
            g2.setBackground(Color.white);
            g2.clear();
        }
        //g2.clearRect(0, 0, realPixX, realPixY);

        if (launchError && !launchScreenGrayed) {
            parent.setTargetFrameRate(25);
            launchScreenGrayed = true;
        }
        // Ladebild
        if (loading_backblur != null) {
            // Auf Höhe skalieren
            float scale = 1.0f * realPixY / loading_backblur.getHeight();
            // Seitenverhältnis beibehalten
            int xSize = (int) (1.0 * loading_backblur.getWidth() * scale);
            int ySize = (int) (1.0 * loading_backblur.getHeight() * scale);
            // Mittig anzeigen
            int xOffset = (xSize - realPixX) / 2;
            // Bereits skaliert?
            if (!loadScaled) {
                loadScaled = true;
                loading_backblur_s = loading_backblur.getScaledCopy(scale);
                loading_backnoblur_s = loading_backnoblur.getScaledCopy(scale);
                loading_frontblur_s = loading_frontblur.getScaledCopy(scale);
                loading_frontnoblur_s = loading_frontnoblur.getScaledCopy(scale);
                loading_sun_blur = loading_sun_blur.getScaledCopy(scale);
            }
            // Sonne immer:
            if (loadStatus < 10) {
                g2.drawImage(loading_sun_blur, 0 - xOffset, 0);
                if (loadStatus < 5) { // Voll vorne
                    g2.drawImage(loading_backblur_s, 0 - xOffset, 0);
                    g2.drawImage(loading_frontnoblur_s, 0 - xOffset, 0);
                } else if (loadZoom1StartTime == 0 || (System.currentTimeMillis() - loadZoom1StartTime) > 1000) { // Ganz hinten
                    loading_backnoblur.getSubImage(180, 216, 1700, 893).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0);
                    loading_frontblur.getSubImage(180, 216, 1700, 893).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0);
                } else {
                    // Jetzt wird spannend! Hier ist der epische Zoom-Effekt Nr1
                    int time = (int) (System.currentTimeMillis() - loadZoom1StartTime);
                    // Logistisches Wachstum über 1 Sekunde (0-1000), Werte von 0 bis 1. k = 0.0049
                    float perc = (float) (1.0f * (2 / (1 + Math.pow(Math.E, -0.0049 * 2 * (time - 500)))) / 2);

                    // Blende-Farben
                    Color fadeOut = new Color(1f, 1f, 1f, 1 - perc);
                    Color fadeIn = new Color(1f, 1f, 1f, perc);
                    // Zoom-Koordinaten:
                    int px = (int) (180 * perc);
                    int py = (int) (216 * perc);
                    int sx = (int) (loading_backnoblur.getWidth() - ((loading_backnoblur.getWidth() - 1700) * perc));
                    int sy = (int) (loading_backnoblur.getHeight() - ((loading_backnoblur.getHeight() - 893) * perc));
                    // Reienfolge: HS-HB-VS-VB

                    loading_backnoblur.getSubImage(px, py, sx, sy).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0);
                    loading_backblur.getSubImage(px, py, sx, sy).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0, fadeOut);
                    loading_frontnoblur.getSubImage(px, py, sx, sy).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0, fadeOut);
                    loading_frontblur.getSubImage(px, py, sx, sy).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0, fadeIn);
                }
            } else {
                // Episches - Ins-Game zoomen
                // Geschwindigkeit fürs Ausblenden
                float perc = (float) (Math.pow(100, 1.0f * (System.currentTimeMillis() - loadZoom2StartTime) / 500) / 100);
                if (perc > 1) {
                    // Fertig!
                    zoomInGame = false;
                }
                Color fadeOut = new Color(1f, 1f, 1f, 1 - perc);

                // Schneller zoomen
                perc = (float) (Math.pow(100, 1.0f * (System.currentTimeMillis() - loadZoom2StartTime) / 500) / 100);
                if (perc > 1) {
                    perc = 1;
                }
                int px = (int) (180 + 870 * perc);
                int py = (int) (216 + 388 * perc);
                int sx = (int) (1700 - ((1700 - 1) * perc));
                int sy = (int) (893 - ((893 - 1) * perc));
                // Blende-Farben

                g2.drawImage(loading_sun_blur, 0 - xOffset, 0, fadeOut);
                loading_backnoblur.getSubImage(px, py, sx, sy).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0, fadeOut);
                loading_frontblur.getSubImage(px, py, sx, sy).getScaledCopy(xSize, ySize).draw(0 - xOffset, 0, fadeOut);
            }
        }

        // Ladebalken zeigen
        int lx = realPixX / 4;      // Startposition des Balkens
        int ly = realPixY / 50;
        int dx = realPixX / 2;      // Länge des Balkens
        int dy = realPixY / 32;
        if (loadStatus == 10) {
            // Rausziehen
            float perc = (float) (Math.pow(100, 1.0f * (System.currentTimeMillis() - loadZoom2StartTime) / 500) / 100);
            ly = (int) ((realPixY / 50) - (ly * 60 * perc));
        }
        g2.setColor(new Color(0, 0, 0, 0.8f));
        // Rahmen ziehen
        g2.drawRect(lx - 2, ly - 2, dx + 3, dy + 3);
        // Balken zeichnen
        if (loadStatus != 4 || imgLoadTotal == 0) {
            g2.fillRect(lx, ly, dx * loadStatus / 10, dy);
        } else {
            int dxf = dx * loadStatus / 10 + (int) ((1.0 * this.imgLoadCount / imgLoadTotal) * dx / 10);
            g2.fillRect(lx, ly, dxf, dy);
        }
        // Status drunter anzeigen
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
                    status = status + "downloading map...";
                    break;
                case 8:
                    status = status + "loading map...";
                    break;
                case 9:
                    status = status + "preparing start...";
                    break;
                case 10:
                    status = status + "Centuries of Rage: Loading done! Have a lot of fun...";
                    break;
            }
        } else {
            switch (loadStatus) {
                case 5:
                    status = status + "waiting for other players...";
                    break;
                case 8:
                    status = status + "waiting for other players...";
                    break;
                case 9:
                    status = status + "waiting for other players to finish preparations...";
                    break;
            }
        }
        g2.setFont(fonts[0]);
        g2.drawString(status, lx, (int) (ly + (dy * 1.5)));
        // Error - Status

        if (launchError) {
            // Allgemeinen Fehlerkasten zeichnen:
            int ex = realPixX / 5;      // Startposition des Balkens
            int ey = realPixY / 10 * 2;
            int edx = realPixX / 5 * 3;      // Länge des Balkens
            int edy = realPixY / 2;
            // Kasten zeichnen
            g2.setColor(Color.red);
            g2.fillRect(ex, ey, edx, edy);
            g2.setColor(Color.black);
            g2.drawRect(ex, ey, edx, edy);
            g2.drawRect(ex + 2, ey + 2, edx - 4, edy - 4);
            // Allgemeine Überschrift:
            g2.setFont(fonts[5]);
            g2.setAntiAlias(false);
            g2.drawString("ERROR! - SORRY!", ex + (edx / 2) - (fonts[5].getWidth("ERROR! - SORRY!") / 2), (float) (ey * 1.05));
            // Trennlinie drunter:
            g2.setColor(Color.black);
            g2.drawLine(ex + 2, (float) (ey * 1.3), ex + edx - 2, (float) (ey * 1.3));
            g2.setFont(fonts[2]);
            g2.drawString("An error occured:", ex + 5, (float) (ey * 1.3) + 2);
            g2.setFont(fonts[1]);
            switch (lEtype) {
                case 1:
                    g2.drawString("Can't load the map!", ex + (edx / 2) - (fonts[1].getWidth("Can't load the map!") / 2), (float) (ey * 1.3) + 14);
                    g2.setFont(fonts[3]);
                    g2.drawString("Possible reason:", ex + 5, (float) (ey * 1.5));
                    g2.setFont(fonts[0]);
                    g2.drawString("Server tries to load a file that does not exist", ex + 20, (float) (ey * 1.5) + 15);
                    g2.drawString("Server tries to load a file that is not a valid CoR-Map and/or corrupted", ex + 20, (float) (ey * 1.5) + 27);
                    g2.drawString("Server tries to load a map that was created with an older version of CoR", ex + 20, (float) (ey * 1.5) + 39);
                    g2.drawString("Server can't transfer the map to this client (very unlikely)", ex + 20, (float) (ey * 1.5) + 51);
                    g2.drawString("Version mismatch - Server and Client aren't at the same version", ex + 20, (float) (ey * 1.5) + 63);
                    g2.setFont(fonts[3]);
                    g2.drawString("What you could do:", ex + 5, (float) (ey * 2.4));
                    g2.setFont(fonts[0]);
                    g2.drawString("Create a new Map on the Server-PC (use the RandomMapBuilder)", ex + 20, (float) (ey * 2.4) + 15);
                    g2.drawString("Make sure everyone uses the same version - redownload the newest", ex + 20, (float) (ey * 2.4) + 27);
                    g2.drawString("Enter the correct path when starting the Server (or use last random-map)", ex + 20, (float) (ey * 2.4) + 39);
                    break;
            }
            g2.drawLine(ex + 2, (float) (ey + (edy * 0.9)), ex + edx - 2, (float) (ey + (edy * 0.9)));
            g2.setFont(fonts[2]);
            g2.drawString("Click here or press ENTER to quit", ex + (edx / 2) - (fonts[2].getWidth("Click here or press ENTER to quit") / 2), (float) (ey + (edy * 0.95) - 6));
        }
    }

    private void renderBuildingWaypoint() {
        // Gebäude-Sammelpunkt zeichnen
        if (!rgi.rogGraphics.inputM.selected.isEmpty() && rgi.rogGraphics.inputM.selected.get(0).getClass().equals(Building.class)) {
            Building b = (Building) rgi.rogGraphics.inputM.selected.get(0);
            if (b.waypoint != null) {
                // Dahin rendern
                GraphicsImage image = coloredImgMap.get("img/game/building_defaulttarget.png" + b.playerId);
                if (image != null) {
                    image.getImage().draw(((b.waypoint.X - positionX) * 20) - 40, ((b.waypoint.Y - positionY) * 15) - 40);
                } else {
                    System.out.println("ERROR: Textures missing (34124)");
                }
            }
        }
    }

    private void renderFogOfWar(Graphics g) {
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
                // Ist hier Schatten?
                try {
                    byte fow = fowmap[x + positionX][y + positionY - 2];
                    if (fow < 2) {
                        if (fow == 0) {
                            g.setColor(Color.black);
                        } else {
                            g.setColor(fowGray);
                        }
                        //g.fill(fowShape);
                        g.fillRect(x * 20, (y - 2) * 15 + 10, 40, 30);
                    }
                } catch (ArrayIndexOutOfBoundsException ar) {
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        for (int x = 1; x < (sizeX) && x < (viewX + 4); x = x + 2) {
            for (int y = 1; y < (sizeY) && y < (viewY + 2); y = y + 2) {
                // Ist hier Schatten?
                try {
                    byte fow = fowmap[x + positionX - 2][y + positionY - 2];
                    if (fow < 2) {
                        if (fow == 0) {
                            g.setColor(Color.black);
                        } else {
                            g.setColor(fowGray);
                        }
                        g.fillRect((x - 2) * 20, (y - 2) * 15 + 10, 40, 30);
                    }
                } catch (ArrayIndexOutOfBoundsException ar) {
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void renderPause(Graphics g2) {
        // PAUSE - Overlay rendern
        g2.setColor(Color.lightGray);
        g2.fillRect(realPixX / 3, realPixY / 3, realPixX / 3, realPixY / 5);
        g2.setColor(Color.black);
        g2.setFont(fonts[5]);
        g2.drawString("P A U S E", realPixX / 2 - 80, realPixY / 2 - 60);
        g2.drawRect(realPixX / 3, realPixY / 3, realPixX / 3, realPixY / 5);
    }

    private void renderStatistics(Graphics g2) {
        // Statistiken am Ende des Spiels einblenden

        int spielerzahl = 0;
        int movedown = 0;
        for (int i = 1; i < rgi.game.playerList.size(); i++) {
            if (rgi.game.playerList.get(i).nickName.equals("")) {
                break;
            } else {
                spielerzahl++;
            }
        }
        movedown = (int) ((9 - spielerzahl) * realPixY * 0.05);

        g2.setColor(Color.lightGray);
        g2.fillRect((int) (realPixX * 0.005), (int) (realPixY * 0.4 + movedown), (int) (realPixX * 0.692), (int) (realPixY * 0.595 - movedown));
        g2.setColor(Color.gray);
        g2.drawLine((int) (realPixX * 0.165), (int) (realPixY * 0.415 + movedown), (int) (realPixX * 0.165), (int) (realPixY * 0.98));
        g2.drawLine((int) (realPixX * 0.415), (int) (realPixY * 0.415 + movedown), (int) (realPixX * 0.415), (int) (realPixY * 0.98));
        g2.drawLine((int) (realPixX * 0.565), (int) (realPixY * 0.415 + movedown), (int) (realPixX * 0.565), (int) (realPixY * 0.98));
        g2.drawLine((int) (realPixX * 0.015), (int) (realPixY * 0.51 + movedown), (int) (realPixX * 0.687), (int) (realPixY * 0.51 + movedown));
        g2.setColor(Color.black);
        g2.drawRect((int) (realPixX * 0.005), (int) (realPixY * 0.4 + movedown), (int) (realPixX * 0.692), (int) (realPixY * 0.595 - movedown));

        g2.setFont(fonts[1]);
        g2.drawString("Statistics", (int) (realPixX * 0.011), (int) (realPixY * 0.41 + movedown));

        g2.drawString("Ressources", (int) (realPixX * 0.17), (int) (realPixY * 0.44 + movedown));
        g2.drawImage(imgMap.get("img/sym/res1.png").getImage(), (int) (realPixX * 0.175), (int) (realPixY * 0.48 + movedown));
        g2.drawImage(imgMap.get("img/sym/res2.png").getImage(), (int) (realPixX * 0.225), (int) (realPixY * 0.48 + movedown));
        g2.drawImage(imgMap.get("img/sym/res3.png").getImage(), (int) (realPixX * 0.275), (int) (realPixY * 0.48 + movedown));
        g2.drawImage(imgMap.get("img/sym/res4.png").getImage(), (int) (realPixX * 0.325), (int) (realPixY * 0.48 + movedown));
        g2.drawImage(imgMap.get("img/sym/res5.png").getImage(), (int) (realPixX * 0.375), (int) (realPixY * 0.48 + movedown));

        g2.drawString("Units", (int) (realPixX * 0.42), (int) (realPixY * 0.44 + movedown));
        g2.drawString("Buildings", (int) (realPixX * 0.57), (int) (realPixY * 0.44 + movedown));

        g2.setFont(fonts[0]);
        g2.drawString("Built", (int) (realPixX * 0.42), (int) (realPixY * 0.48 + movedown));
        g2.drawString("Killed", (int) (realPixX * 0.47), (int) (realPixY * 0.48 + movedown));
        g2.drawString("Lost", (int) (realPixX * 0.52), (int) (realPixY * 0.48 + movedown));

        g2.drawString("Built", (int) (realPixX * 0.57), (int) (realPixY * 0.48 + movedown));
        g2.drawString("Killed", (int) (realPixX * 0.62), (int) (realPixY * 0.48 + movedown));
        g2.drawString("Lost", (int) (realPixX * 0.67), (int) (realPixY * 0.48 + movedown));

        for (int i = 1; i <= spielerzahl; i++) {
            g2.setFont(fonts[1]);
            g2.drawString(rgi.game.playerList.get(i).nickName, (int) (realPixX * 0.045), (int) (realPixY * (0.48 + i * 0.05) + movedown));
            if (rgi.game.playerList.get(i).lobbyRace != 1) {
                g2.drawImage(imgMap.get("img/creeps/testhuman2.png").getImage(), (int) (realPixX * 0.025 - 16), (int) (realPixY * (0.48 + i * 0.05) - 10) + movedown);
            } else {
                g2.drawImage(imgMap.get("img/creeps/testundead1.png").getImage(), (int) (realPixX * 0.025 - 16), (int) (realPixY * (0.48 + i * 0.05) - 11) + movedown);
            }
            g2.setFont(fonts[2]);
            //Ressourcen
            for (int r = 1; r <= 5; r++) {
                g2.drawString(Integer.toString(rgi.clientstats.collectedstats[i][r]), (int) (realPixX * (0.12 + r * 0.05)), (int) (realPixY * (0.48 + i * 0.05) + movedown));
            }
            //Units
            g2.drawString(Integer.toString(rgi.clientstats.collectedstats[i][6]), (int) (realPixX * 0.42), (int) (realPixY * (0.48 + i * 0.05) + movedown));
            g2.drawString(Integer.toString(rgi.clientstats.collectedstats[i][7]), (int) (realPixX * 0.47), (int) (realPixY * (0.48 + i * 0.05) + movedown));
            g2.drawString(Integer.toString(rgi.clientstats.collectedstats[i][8]), (int) (realPixX * 0.52), (int) (realPixY * (0.48 + i * 0.05) + movedown));
            //Buildings
            g2.drawString(Integer.toString(rgi.clientstats.collectedstats[i][9]), (int) (realPixX * 0.57), (int) (realPixY * (0.48 + i * 0.05) + movedown));
            g2.drawString(Integer.toString(rgi.clientstats.collectedstats[i][10]), (int) (realPixX * 0.62), (int) (realPixY * (0.48 + i * 0.05) + movedown));
            g2.drawString(Integer.toString(rgi.clientstats.collectedstats[i][11]), (int) (realPixX * 0.67), (int) (realPixY * (0.48 + i * 0.05) + movedown));
        }
    }

    private void renderBuildingMarkers(Graphics g2) {
        // Spielermarkierungen für Gebäude - neueres System
        for (int i = 0; i < buildingList.size(); i++) {
            Building b = buildingList.get(i);
            if (b.playerId != rgi.game.getOwnPlayer().playerId && !b.wasSeen) {
                continue;
            }
            // Startkoordinaten
            int x = (b.position.X - positionX) * 20;
            int y = ((b.position.Y - positionY) * 15) + 25;
            // Linien ziehen
            g2.setLineWidth(4);
            //g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
            g2.setColor(rgi.game.getPlayer(b.playerId).color);
            g2.drawLine(x, y, x + (b.z1 * 20), y - (b.z1 * 15));
            g2.drawLine(x, y, x + (b.z2 * 20), y + (b.z2 * 15));
            g2.drawLine(x + (b.z1 * 20), y - (b.z1 * 15), x + (b.z1 * 20) + (b.z2 * 20), y - (b.z1 * 15) + (b.z2 * 15));
            g2.drawLine(x + (b.z2 * 20), y + (b.z2 * 15), x + (b.z1 * 20) + (b.z2 * 20), y - (b.z1 * 15) + (b.z2 * 15));
        }
        g2.setLineWidth(1);
        //g2.setStroke(new BasicStroke(1));
    }

    private void renderMouseHover(Graphics g2) {
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
                g2.setColor(Color.lightGray);
                g2.fillRect(hovX, hovY, mouseX - hovX, mouseY - hovY);
                g2.setColor(Color.black);
                g2.drawRect(hovX, hovY, mouseX - hovX, mouseY - hovY);
                // Erst Name der Ability
                g2.setFont(fonts[1]);
                g2.setColor(Color.black);
                g2.drawString(ability.name, hovX + 2, hovY + 1);
                g2.setFont(fonts[0]);
                // Jetzt je nach Typ weiter:
                if (ability.type == Ability.ABILITY_BUILD) {
                    AbilityBuild abb = (AbilityBuild) ability;
                    // Dauer:
                    g2.drawString("Duration: " + GraphicsContent.transformTime(abb.duration), hovX + 2, hovY + 15);
                    Building building = rgi.mapModule.getDescBuilding(abb.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
                    // Truppenlimit
                    boolean limitOk = true;
                    if (building.limit > 0) {
                        if (rgi.game.getOwnPlayer().freeLimit() < building.limit || rgi.game.getOwnPlayer().currentlimit + building.limit > 100) {
                            g2.setColor(Color.red);
                            limitOk = false;
                        }
                        g2.drawString("Requires: " + building.limit, hovX + 82, hovY + 15);
                        g2.drawImage(imgMap.get("img/sym/limit.png").getImage(), hovX + 135, hovY + 16);
                    } else if (building.limit < 0) {
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Increases limit by: " + String.valueOf(building.limit).substring(1), hovX + 82, hovY + 15);
                    }
                    g2.setColor(Color.black);
                    if (ability.showCosts) {
                        // Kosten:
                        Color green = new Color(3, 100, 0);
                        int xposition = 3;
                        //FOOD
                        if (ability.costs[0] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res1.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res1 >= ability.costs[0]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[0]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //WOOD
                        if (ability.costs[1] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res2.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res2 >= ability.costs[1]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[1]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //METAL
                        if (ability.costs[2] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res3.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res3 >= ability.costs[2]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[2]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //GOLD
                        if (ability.costs[3] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res4.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res4 >= ability.costs[3]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[3]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //OIL
                        if (ability.costs[4] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res5.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res5 >= ability.costs[4]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[4]), hovX + xposition + 12, hovY + 30);
                        }
                        hovY += 15;
                    }
                    g2.setColor(Color.black);
                    g2.drawLine(hovX, hovY + 28, mouseX, hovY + 28);
                    // Infos über das Gebäude
                    // Name:
                    g2.setFont(fonts[2]);
                    g2.drawString(building.name, hovX + 2, hovY + 32);
                    // Beschreibung
                    g2.setFont(fonts[0]);
                    g2.drawString(building.Gdesc, hovX + 2, hovY + 48);
                    // HP
                    g2.drawString("HP: " + building.getMaxhitpoints(), hovX + 2, hovY + 60);
                    // Absatz
                    g2.drawLine(hovX, hovY + 73, mouseX, hovY + 73);
                    // Verfügbar?
                    g2.setFont(fonts[2]);
                    if (ability.isAvailable()) {
                        // Dunkelgrün
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Available, click to build.", hovX + 2, hovY + 74);
                    } else {
                        g2.setColor(Color.red);
                        g2.drawString("Not available, requires:", hovX + 2, hovY + 74);
                        if (missing != null) {
                            int i = 0;
                            for (; i < missing.length; i++) {
                                String obj = missing[i];
                                g2.drawString("- " + obj, hovX + 2, hovY + 74 + (12 * (i + 1)));
                            }
                            int d1 = ability.costs[0] - rgi.game.getOwnPlayer().res1;
                            int d2 = ability.costs[1] - rgi.game.getOwnPlayer().res2;
                            int d3 = ability.costs[2] - rgi.game.getOwnPlayer().res3;
                            int d4 = ability.costs[3] - rgi.game.getOwnPlayer().res4;
                            int d5 = ability.costs[4] - rgi.game.getOwnPlayer().res5;
                            if (d1 > 0) {
                                g2.drawString("- another " + d1 + " Food", hovX + 2, hovY + 74 + (12 * (i + 1)));
                                i++;
                            }
                            if (d2 > 0) {
                                g2.drawString("- another " + d2 + " Wood", hovX + 2, hovY + 74 + (12 * (i + 1)));
                                i++;
                            }
                            if (d3 > 0) {
                                g2.drawString("- another " + d3 + " Metal", hovX + 2, hovY + 74 + (12 * (i + 1)));
                                i++;
                            }
                            if (d4 > 0) {
                                g2.drawString("- another " + d4 + " Gold", hovX + 2, hovY + 74 + (12 * (i + 1)));
                                i++;
                            }
                            if (d5 > 0) {
                                g2.drawString("- another " + d5 + " Oil", hovX + 2, hovY + 74 + (12 * (i + 1)));
                                i++;
                            }
                            if (!limitOk) {
                                g2.drawString("- more Houses", hovX + 2, hovY + 74 + (12 * (i + 1)));
                            }
                        }
                    }
                } else if (ability.type == Ability.ABILITY_RECRUIT) {
                    AbilityRecruit abr = (AbilityRecruit) ability;
                    // Dauer:
                    g2.drawString("Duration: " + GraphicsContent.transformTime(abr.duration), hovX + 2, hovY + 15);
                    Unit unit = rgi.mapModule.getDescUnit(abr.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
                    // Truppenlimit
                    boolean limitOk = true;
                    if (unit.limit > 0) {
                        if (rgi.game.getOwnPlayer().freeLimit() < unit.limit || rgi.game.getOwnPlayer().currentlimit + unit.limit > 100) {
                            g2.setColor(Color.red);
                            limitOk = false;
                        }
                        g2.drawString("Requires: " + unit.limit, hovX + 82, hovY + 15);
                        g2.drawImage(imgMap.get("img/sym/limit.png").getImage(), hovX + 135, hovY + 16);
                    } else if (unit.limit < 0) {
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Increases limit by: " + String.valueOf(unit.limit).substring(1), hovX + 82, hovY + 15);
                    }
                    if (ability.showCosts) {
                        // Kosten:
                        Color green = new Color(3, 100, 0);
                        int xposition = 3;
                        //FOOD
                        if (ability.costs[0] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res1.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res1 >= ability.costs[0]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[0]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //WOOD
                        if (ability.costs[1] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res2.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res2 >= ability.costs[1]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[1]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //METAL
                        if (ability.costs[2] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res3.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res3 >= ability.costs[2]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[2]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //GOLD
                        if (ability.costs[3] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res4.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res4 >= ability.costs[3]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[3]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //OIL
                        if (ability.costs[4] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res5.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res5 >= ability.costs[4]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[4]), hovX + xposition + 12, hovY + 30);
                        }
                        hovY += 15;
                    }
                    g2.setColor(Color.black);
                    g2.drawLine(hovX, hovY + 28, mouseX, hovY + 28);
                    // Infos über die Einheit:

                    // Name:
                    g2.setFont(fonts[2]);
                    g2.drawString(unit.name, hovX + 2, hovY + 32);
                    // Beschreibung
                    g2.setFont(fonts[0]);
                    g2.drawString(unit.Gdesc, hovX + 2, hovY + 48);
                    // HP
                    g2.drawString("HP: " + unit.getMaxhitpoints(), hovX + 2, hovY + 60);
                    // Stark / Schwach gegen...
                    g2.drawString("Strong vs: " + unit.Gpro, hovX + 2, hovY + 72);
                    g2.drawString("Weak vs: " + unit.Gcon, hovX + 2, hovY + 84);
                    // Trennlinie
                    g2.drawLine(hovX, hovY + 97, mouseX, hovY + 97);
                    // Verfügbar?
                    g2.setFont(fonts[2]);
                    if (ability.isAvailable()) {
                        // Dunkelgrün
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Available, click to build", hovX + 2, hovY + 98);
                    } else {
                        g2.setColor(Color.red);
                        g2.drawString("Not available, requires:", hovX + 2, hovY + 98);
                        if (missing != null) {
                            int i = 0;
                            for (; i < missing.length; i++) {
                                String obj = missing[i];
                                g2.drawString("- " + obj, hovX + 2, hovY + 98 + (12 * (i + 1)));
                            }
                            int d1 = ability.costs[0] - rgi.game.getOwnPlayer().res1;
                            int d2 = ability.costs[1] - rgi.game.getOwnPlayer().res2;
                            int d3 = ability.costs[2] - rgi.game.getOwnPlayer().res3;
                            int d4 = ability.costs[3] - rgi.game.getOwnPlayer().res4;
                            int d5 = ability.costs[4] - rgi.game.getOwnPlayer().res5;
                            if (d1 > 0) {
                                g2.drawString("- another " + d1 + " Food", hovX + 2, hovY + 98 + (12 * (i + 1)));
                                i++;
                            }
                            if (d2 > 0) {
                                g2.drawString("- another " + d2 + " Wood", hovX + 2, hovY + 98 + (12 * (i + 1)));
                                i++;
                            }
                            if (d3 > 0) {
                                g2.drawString("- another " + d3 + " Metal", hovX + 2, hovY + 98 + (12 * (i + 1)));
                                i++;
                            }
                            if (d4 > 0) {
                                g2.drawString("- another " + d4 + " Gold", hovX + 2, hovY + 98 + (12 * (i + 1)));
                                i++;
                            }
                            if (d5 > 0) {
                                g2.drawString("- another " + d5 + " Oil", hovX + 2, hovY + 98 + (12 * (i + 1)));
                                i++;
                            }
                            if (!limitOk) {
                                g2.drawString("- more Houses", hovX + 2, hovY + 98 + (12 * (i + 1)));
                            }
                        }
                    }
                } else if (ability.type == Ability.ABILITY_MOVE) {
                } else if (ability.type == Ability.ABILITY_UPGRADE) {
                    AbilityUpgrade abu = (AbilityUpgrade) ability;
                    // Dauer:
                    g2.drawString("Duration: " + GraphicsContent.transformTime(abu.duration), hovX + 2, hovY + 15);
                    if (ability.showCosts) {
                        // Kosten:
                        Color green = new Color(3, 100, 0);
                        int xposition = 3;
                        //FOOD
                        if (ability.costs[0] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res1.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res1 >= ability.costs[0]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[0]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //WOOD
                        if (ability.costs[1] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res2.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res2 >= ability.costs[1]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[1]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //METAL
                        if (ability.costs[2] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res3.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res3 >= ability.costs[2]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[2]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //GOLD
                        if (ability.costs[3] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res4.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res4 >= ability.costs[3]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[3]), hovX + xposition + 12, hovY + 30);
                            xposition += 39;
                        }
                        //OIL
                        if (ability.costs[4] > 0) {
                            g2.drawImage(imgMap.get("img/sym/res5.png").getImage(), hovX + xposition, hovY + 31);
                            if (rgi.game.getOwnPlayer().res5 >= ability.costs[4]) {
                                g2.setColor(green);
                            } else {
                                g2.setColor(Color.red);
                            }
                            g2.drawString(String.valueOf(ability.costs[4]), hovX + xposition + 12, hovY + 30);
                        }
                        hovY += 15;
                    }
                    // Beschreibung:
                    g2.setColor(Color.black);
                    g2.setFont(fonts[2]);
                    if (abu.gdesc != null) {
                        g2.drawString(abu.gdesc, hovX + 2, hovY + 32);
                    } else {
                        g2.drawString("No describtion available!", hovX + 2, hovY + 32);
                    }
                    // Trennlinie
                    g2.drawLine(hovX, hovY + 45, mouseX, hovY + 45);
                    // Verfügbar?
                    g2.setFont(fonts[2]);
                    if (ability.isAvailable()) {
                        // Dunkelgrün
                        g2.setColor(new Color(3, 100, 0));
                        g2.drawString("Available, click to upgrade.", hovX + 2, hovY + 46);
                    } else {
                        // Warum net, vllt weil schon verwendet?
                        if (ability.alreadyUsed) {
                            g2.setColor(Color.red);
                            g2.drawString("Not available, already used.", hovX + 2, hovY + 46);
                        } else {
                            g2.setColor(Color.red);
                            g2.drawString("Not available, requires:", hovX + 2, hovY + 46);
                            if (missing != null) {
                                int i = 0;
                                for (; i < missing.length; i++) {
                                    String obj = missing[i];
                                    g2.drawString("- " + obj, hovX + 2, hovY + 46 + (12 * (i + 1)));
                                }
                                int d1 = ability.costs[0] - rgi.game.getOwnPlayer().res1;
                                int d2 = ability.costs[1] - rgi.game.getOwnPlayer().res2;
                                int d3 = ability.costs[2] - rgi.game.getOwnPlayer().res3;
                                int d4 = ability.costs[3] - rgi.game.getOwnPlayer().res4;
                                int d5 = ability.costs[4] - rgi.game.getOwnPlayer().res5;
                                if (d1 > 0) {
                                    g2.drawString("- annother " + d1 + " Food", hovX + 2, hovY + 46 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d2 > 0) {
                                    g2.drawString("- another " + d2 + " Wood", hovX + 2, hovY + 46 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d3 > 0) {
                                    g2.drawString("- antoher " + d3 + " Metal", hovX + 2, hovY + 46 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d4 > 0) {
                                    g2.drawString("- another " + d4 + " Gold", hovX + 2, hovY + 46 + (12 * (i + 1)));
                                    i++;
                                }
                                if (d5 > 0) {
                                    g2.drawString("- another " + d5 + " Oil", hovX + 2, hovY + 46 + (12 * (i + 1)));
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
                    g2.drawString(intr.caster2.intraUnits.size() + " / " + intr.caster2.maxIntra + " Units indoors", hovX + 2, hovY + 18);
                    g2.setColor(new Color(3, 100, 0));
                    g2.drawString("Left-click to evacuate ONE Unit", hovX + 2, hovY + 35);
                    g2.drawString("Right-click to evacuate ALL Units", hovX + 2, hovY + 47);

                }
            }
        }

    }

    private void renderGraphicElements(Graphics g) {
        // Zeichnet alles, was es an GameObjects gibt.
        selectionShadows = new ArrayList();
        for (int i = 0; i < allList.size(); i++) {
            Unit unit;
            Building building;
            // Einheit?
            try {
                unit = (Unit) allList.get(i);
                // Ok, es ist eine Einheit:
                // In Bewegung?
                GraphicsImage tempImage = null;
                Position tempP = null;
                if (unit.alive) {
                    if (unit.isMoving()) {
                        boolean animatk = false;
                        if (unit.anim != null && unit.atkAnim && unit.anim.isAttackingAnimated()) {
                            Image img = unit.anim.getNextAttackingFrame();
                            if (img != null) {
                                animatk = true;
                                tempImage = new GraphicsImage(img);
                            } else {
                                unit.atkAnim = false;
                            }
                        }
                        if (!animatk) {
                            if (unit.anim != null && unit.anim.isMovingAnimated()) {
                                tempImage = new GraphicsImage(unit.anim.getNextMovingFrame());
                            } else {
                                tempImage = imgMap.get(unit.graphicsdata.getTexture());
                            }
                        }
                        tempP = unit.getMovingPosition(rgi, positionX, positionY);

                    } else if (unit.order == orders.harvest) {
                        if (unit.anim != null && unit.anim.isHarvestingAnimated()) {
                            tempImage = new GraphicsImage(unit.anim.getNextHarvestingFrame());
                        } else {
                            tempImage = imgMap.get(unit.graphicsdata.getTexture());
                        }
                        tempP = new Position((unit.position.X - positionX) * 20, (unit.position.Y - positionY) * 15);
                    } else {
                        boolean animatk = false;
                        if (unit.anim != null && unit.atkAnim && unit.anim.isAttackingAnimated()) {
                            Image img = unit.anim.getNextAttackingFrame();
                            if (img != null) {
                                animatk = true;
                                tempImage = new GraphicsImage(img);
                            } else {
                                unit.atkAnim = false;
                            }
                        }
                        if (!animatk) {
                            if (unit.anim != null && unit.anim.isIdleAnimated()) {
                                tempImage = new GraphicsImage(unit.anim.getNextIdleFrame());
                            } else {
                                tempImage = imgMap.get(unit.graphicsdata.getTexture());
                            }
                        }
                        tempP = new Position((unit.position.X - positionX) * 20, (unit.position.Y - positionY) * 15);
                    }
                } else {
                    if (unit.anim != null && unit.anim.isDieingAnimated()) {
                        tempImage = new GraphicsImage(unit.anim.getNextDieingFrame());
                    } else {
                        tempImage = null;
                    }
                    tempP = new Position((unit.position.X - positionX) * 20, (unit.position.Y - positionY) * 15);
                }
                if (tempImage != null) {
                    // Ist diese Einheit gerade sichtbar? (nur eigene oder sichtbare zeichnen)
                    if (unit.playerId == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(unit, rgi.game.getOwnPlayer()) || fowmap[unit.position.X][unit.position.Y] > 1) {
                        if (tempP.X != -1) {
                            //Einheit gehört zu / Selektiert
                            if (unit.playerId != 0) {
                                String sub = "img/game/sel_s1.png" + unit.playerId;
                                coloredImgMap.get(sub).getImage().draw(tempP.X, tempP.Y);
                            }
                            if (unit.isSelected) {
                                // Weiß markieren
                                //TODO größe der Einheit an selektion anpassen
                                imgMap.get("img/game/sel_t0_s1.png").getImage().draw(tempP.X, tempP.Y);
                            }
                            // Beim Angriff die Position der Einheit ein bissle verschieben: (Nur Nahkampf)
                            int maxX = 0;
                            int maxY = 0;
                            if (unit.anim != null && unit.atkStart != 0 && unit.getRange() == 2) {
                                int del = unit.getAtkdelay() != 0 ? unit.getAtkdelay() : 250;
                                // Solange wie del sagt vorschieben, danach gleich lang zurück
                                switch (unit.anim.dir) {
                                    case 1:
                                        // Oben
                                        maxY = -10;
                                        break;
                                    case 2:
                                        // Oben rechts
                                        maxX = 5;
                                        maxY = -5;
                                        break;
                                    case 3:
                                        // Rechts
                                        maxX = 10;
                                        break;
                                    case 4:
                                        // RU
                                        maxX = 5;
                                        maxY = 5;
                                        break;
                                    case 5:
                                        // U
                                        maxY = 10;
                                        break;
                                    case 6:
                                        // LU
                                        maxX = -5;
                                        maxY = 5;
                                        break;
                                    case 7:
                                        // L
                                        maxX = -10;
                                        break;
                                    case 8:
                                        // LO
                                        maxX = -5;
                                        maxY = -5;
                                        break;
                                }
                                // Zeit (Fortschritt der Bewegung reinrechnen)
                                // Fortschritt berechnen:
                                double pro = 1.0 * ((System.currentTimeMillis() - unit.atkStart) * 1.0 / del);
                                if (pro > 2) {
                                    // Fertig
                                    pro = 0;
                                    unit.atkStart = 0;
                                } else if (pro > 1) {
                                    // Zurück
                                    pro -= ((pro - 1) * 2);
                                }
                                maxX = (int) (pro * maxX);
                                maxY = (int) (pro * maxY);
                            }
                            if (unit.equals(hoveredUnit)) {
                                Image ui = tempImage.getImage();
                                ui.drawFlash(tempP.X + maxX, tempP.Y + maxY);
                                ui.draw(tempP.X + maxX, tempP.Y + maxY, new Color(1.0f, 1.0f, 1.0f, 0.5f));
                                //tempImage.getImage().draw(tempP.X + maxX, tempP.Y + maxY);
                            } else {
                                tempImage.getImage().draw(tempP.X + maxX, tempP.Y + maxY);
                            }

                            if (unit.alive) { // Tote Einheiten kann man nicht selektieren
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

            } catch (ClassCastException ex) {
                try {
                    building = (Building) allList.get(i);
                    // Textur holen
                    String tex = building.defaultTexture;
                    // Grafik aus ImgMap laden
                    GraphicsImage tempImage = null;
                    boolean inSight = building.wasSeen;
                    // Wenn noch nicht, dann schauen, ob aktuell
                    if (!inSight) {
                        for (int z1 = 0; z1 < building.z1; z1++) {
                            for (int z2 = 0; z2 < building.z2; z2++) {
                                if (fowmap[(int) building.position.X + z1 + z2][(int) building.position.Y - z1 + z2] > 1) {
                                    // Jetzt sehen wirs
                                    inSight = true;
                                    building.wasSeen = true;
                                    // Auf Minimap anzeigen
                                    calcMiniMap();
                                    break;
                                }
                            }
                            if (inSight) {
                                break;
                            }
                        }
                    }
                    if (inSight) {
                        if (building.ready) {
                            if (building.alive) {
                                if (!building.isWorking) {
                                    if (building.isSelected) {
                                        if (building.anim != null && building.anim.isIdleAnimated()) {
                                            tempImage = new GraphicsImage(building.anim.getNextIdleFrame(9));
                                        } else {
                                            tempImage = imgMap.get(tex);
                                        }
                                    } else {
                                        // Zum Spieler passend setzen
                                        if (building.anim != null && building.anim.isIdleAnimated()) {
                                            tempImage = new GraphicsImage(building.anim.getNextIdleFrame(building.playerId));
                                        } else {
                                            tempImage = imgMap.get(tex);
                                        }
                                    }
                                } else {
                                    if (building.isSelected) {
                                        if (building.anim != null && building.anim.isWorkingAnimated()) {
                                            tempImage = new GraphicsImage(building.anim.getNextWorkingFrame(9));
                                        } else {
                                            tempImage = imgMap.get(tex);
                                        }
                                    } else {
                                        // Zum Spieler passend setzen
                                        if (building.anim != null && building.anim.isWorkingAnimated()) {
                                            tempImage = new GraphicsImage(building.anim.getNextWorkingFrame(building.playerId));
                                        } else {
                                            tempImage = imgMap.get(tex);
                                        }
                                    }
                                }
                            } else {
                                // Dieing
                                if (building.anim != null && building.anim.isDieingAnimated()) {
                                    tempImage = new GraphicsImage(building.anim.getNextDieingFrame(building.playerId));
                                } else {
                                    tempImage = null;
                                }
                            }

                        } else {
                            tempImage = imgMap.get(tex);
                            if (tempImage != null) {

                                Image img = tempImage.getImage();
                                // Baustelle
                                // Wieviele Pixel rendern?
                                int rpix = (int) (img.getHeight() * building.buildprogress);
                                // Boden der Baustelle markieren
                                GraphicsImage bimg = null;
                                for (int z1 = 0; z1 < building.z1; z1++) {
                                    for (int z2 = 0; z2 < building.z2; z2++) {
                                        if (z1 == building.z1 - 1 && z2 == 0) {
                                            bimg = imgMap.get("img/game/build_hm.png");
                                        } else if (z2 == 0) {
                                            bimg = imgMap.get("img/game/build_hl.png");
                                        } else if (z1 == building.z1 - 1) {
                                            bimg = imgMap.get("img/game/build_hr.png");
                                        } else {
                                            bimg = imgMap.get("img/game/build.png");
                                        }
                                        if (bimg != null) {
                                            bimg.getImage().draw(((building.position.X + z1 + z2) - positionX) * 20, ((building.position.Y - z1 + z2) - positionY) * 15);
                                        }
                                        bimg = null;
                                    }
                                }
                                // Gebäude zeichnen
                                int rX = (building.position.X - building.offsetX - positionX) * 20;
                                int rY = (building.position.Y - building.offsetY - positionY) * 15;
                                rY = rY + (img.getHeight() - rpix);
                                img.draw(rX, rY, rX + img.getWidth(), rY + rpix, 0, img.getHeight() - rpix, img.getWidth(), img.getHeight());
                                tempImage = null;

                                // Vordergrund der Baustelle zeichnen:
                                GraphicsImage bimg2 = null;
                                for (int z1 = 0; z1 < building.z1; z1++) {
                                    for (int z2 = 0; z2 < building.z2; z2++) {
                                        if (z2 == building.z2 - 1 && z1 == 0) {
                                            bimg2 = imgMap.get("img/game/build_vm.png");
                                        } else if (z1 == 0) {
                                            bimg2 = imgMap.get("img/game/build_vl.png");
                                        } else if (z2 == building.z2 - 1) {
                                            bimg2 = imgMap.get("img/game/build_vr.png");
                                        }
                                        if (bimg2 != null) {
                                            bimg2.getImage().draw(((building.position.X + z1 + z2) - positionX) * 20, ((building.position.Y - z1 + z2) - positionY) * 15);
                                        }
                                        bimg2 = null;
                                    }
                                }
                            }

                        }
                        // Bild da?
                        if (tempImage != null) {
                            // Ok, zeichnen
                            tempImage.getImage().draw((building.position.X - building.offsetX - positionX) * 20, (building.position.Y - building.offsetY - positionY) * 15);
                        } else {
                            if (building.ready) {
                                System.out.println("[Graphics][ERROR]: Image \"" + tex + "\" not found!");
                            }
                        }
                    }
                } catch (ClassCastException ex2) {
                    try {
                        // Ressource?
                        Ressource res = (Ressource) allList.get(i);
                        GraphicsImage img = imgMap.get(res.getTex());
                        if (img != null) {
                            if (res.position != null) {
                                if (res.getType() < 3) {
                                    // Einfach hinzeichnen:
                                    img.getImage().draw((res.position.X - positionX) * 20, (res.position.Y - positionY) * 15);
                                } else {
                                    // Größere Bilder, eins weiter oben zeichnen
                                    img.getImage().draw((res.position.X - positionX) * 20, (res.position.Y - positionY - 2) * 15);
                                }
                            }
                        }
                    } catch (ClassCastException ex3) {
                        // Bullet!
                        GraphicsBullet bullet = (GraphicsBullet) allList.get(i);
                        // Bullet sichtbar? (eigene immer anzeigen, ansonsten was sichtbar ist)
                        int[] renderloc = bullet.getRenderLocation(positionX, positionY);
                        if (renderloc == null) {
                            // Bullet ist schon angekommen!
                            // Das Mapmodul soll die Damage dealen
                            rgi.mapModule.dealDamage(bullet.target, bullet.damage);
                            // Bullet löschen
                            allList.remove(bullet);
                            i--;
                            continue;
                        }
                        if (bullet.attacker != null) {
                            if (bullet.attacker.playerId == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(rgi.game.getPlayer(bullet.attacker.playerId), rgi.game.getOwnPlayer()) || positionInSight(bullet.getRoundedPosition(positionX, positionY), true)) {
                                GraphicsImage img = imgMap.get(bullet.texture + bullet.getDirection());
                                if (img != null) {
                                    img.getImage().draw(renderloc[0], renderloc[1]);
                                }
                            }
                        } else {
                            if (bullet.attackerB.playerId == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(rgi.game.getPlayer(bullet.attackerB.playerId), rgi.game.getOwnPlayer()) || positionInSight(bullet.getRoundedPosition(positionX, positionY), true)) {
                                GraphicsImage img = imgMap.get(bullet.texture + bullet.getDirection());
                                if (img != null) {
                                    img.getImage().draw(renderloc[0], renderloc[1]);
                                }
                            }
                        }
                    }
                }
            }
        }
        selectionShadowsB = selectionShadows;
    }

    /**
     * Checkt, ob diese Position sichtbar ist.
     * Sichtbar ist FoW > 1, also Einheiten und Gebäude-Sicht.
     * Wenn Fehler auftreten (z.B. weil die Koordinaten falsch sind) wird onFail zurückgegeben
     * Auf diese Weise kann entschieden werden, ob unklare Situationen lieber gezeigt werden, oder lieber nicht
     * @param pos
     * @param onFail
     * @return
     */
    private boolean positionInSight(Position pos, boolean onFail) {
        try {
            return (fowmap[pos.X][pos.Y] > 1);
        } catch (Exception ex) {
            return onFail;
        }
    }

    private void renderSelBox(Graphics g2) {
        // Rendert die SelektionBox
        g2.setColor(Color.lightGray);
        int dirX = mouseX - this.boxselectionstart.width;
        int dirY = mouseY - this.boxselectionstart.height;
        // Bpxen können nur von links oben nach rechts unten gezogen werden - eventuell Koordinaten tauschen
        if (dirX < 0 && dirY > 0) {
            // Nur x Tauschen
            g2.drawRect(mouseX, this.boxselectionstart.height, this.boxselectionstart.width - mouseX, mouseY - this.boxselectionstart.height);
        } else if (dirY < 0 && dirX > 0) {
            // Nur Y tauschen
            g2.drawRect(this.boxselectionstart.width, mouseY, mouseX - this.boxselectionstart.width, this.boxselectionstart.height - mouseY);
        } else if (dirX < 0 && dirY < 0) {
            // Beide tauschen
            g2.drawRect(mouseX, mouseY, this.boxselectionstart.width - mouseX, this.boxselectionstart.height - mouseY);
        }
        // Nichts tauschen
        g2.drawRect(this.boxselectionstart.width, this.boxselectionstart.height, dirX, dirY);
    }

    private void renderHud(Graphics g) {
        try {
            // Allgemeines Hud-backgroundbild
            hudGround.draw(hudX, 0);
            // Gebäude-Layer
            buildingLayer.draw((int) (hudX + hudSizeX * 0.1), (int) (realPixY / 7 * 1.4));

            boolean reCalcFow = false;
            if (System.currentTimeMillis() - lastFowMiniRender > 1000) {
                reCalcFow = true;
                lastFowMiniRender = System.currentTimeMillis();
            }
            // Minimap - Grund-Minimap schon im Hud drin, Rest kommt jetzt dazu
            // Fow-Layer kopieren (von 1 auf 2)
            Graphics fowg1 = null;
            Graphics fowg2 = null;
            if (!fowDisabled && reCalcFow) {
                if (fowMiniLayer2 == null) {
                    fowMiniLayer2 = new Image(fowMiniLayer.getWidth(), fowMiniLayer.getHeight());
                }
                fowg2 = fowMiniLayer2.getGraphics();
                fowg2.clear();
                fowg2.setColor(new Color(0, 0, 0, 0.7f));
                fowg2.fillRect(0, 0, fowMiniLayer2.getWidth(), fowMiniLayer2.getHeight());
                fowg1 = fowMiniLayer.getGraphics();
            }
            int myPlayerId = rgi.game.getOwnPlayer().playerId;

            /*
             *  Hinweis zur Fow-Berechnung.
             *
             * Leider frisst das herausschneiden der Einheitensichweiten massiv Leistung
             * - Teilweise bis zu 80% der Prozessorlaufzeit
             * Das Herausstanzen mit Schablonen auf Image-Basis hat leider auch nicht funktioniert
             *
             * Daher jetzt folgendes:
             *
             * Schattenberechnung nurnoch 1-2mal pro Sekunde
             * Mehrfache durchläufe der unit/und buildingList, jedes Mal nur einen Layer ausschneiden.
             * Das ist schneller, als zwischen jedem Aufruf die Farbe und den DrawMode (ALPHA_MAP) zu ändern
             * Ja, es ist Pfusch, aber ich hab leider keine bessere Idee.
             *
             */

            if (unitList != null) {
                if (!fowDisabled && reCalcFow) {

                    fowg1.setColor(new org.newdawn.slick.Color(0, 0, 0, 0));
                    fowg2.setColor(new org.newdawn.slick.Color(0, 0, 0, 0));

                    fowg1.setDrawMode(Graphics.MODE_ALPHA_MAP);
                    // Jetzt Fow-Layer schneiden
                    for (int i = 0; i < unitList.size(); i++) {
                        Unit unit = unitList.get(i);
                        // Nur eigene und sichtbare rendern
                        try {
                            if (unit.playerId == myPlayerId || rgi.game.shareSight(unit, rgi.game.getOwnPlayer())) {
                                if (miniMap != null) {
                                    if (!fowDisabled) {
                                        // Wenn eigene, dann noch sichtbarer Bereich rausschneiden (voll auf 1 und halb auf 2)
                                        int bx = (int) (unit.position.X * 20 * maxminscaleX);
                                        int by = (int) (unit.position.Y * 15 * maxminscaleY);
                                        int vrangeX = (int) (unit.getVisrange() * 20 * maxminscaleX * 2);
                                        int vrangeY = (int) (unit.getVisrange() * 15 * maxminscaleY * 2);
//                                        fowPainter.drawImage(miniMapFoWShadow[unit.visrange], bx - vrangeX, by - vrangeY);
//                                        fowPainter2.drawImage(miniMapFoWShadow[unit.visrange], bx - vrangeX, by - vrangeY);
                                        fowg1.fillOval(bx - vrangeX, by - vrangeY, vrangeX * 2, vrangeY * 2);
                                    }
                                }
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.out.println("FixMe: Illegal Position: Unit " + unit + " pos: " + unit.position);
                        }
                    }
                    fowg2.setDrawMode(Graphics.MODE_ALPHA_MAP);
                    for (int i = 0; i < unitList.size(); i++) {
                        Unit unit = unitList.get(i);
                        // Nur eigene und sichtbare rendern
                        try {
                            if (unit.playerId == myPlayerId || rgi.game.shareSight(unit, rgi.game.getOwnPlayer())) {
                                if (miniMap != null) {
                                    if (!fowDisabled) {
                                        // Wenn eigene, dann noch sichtbarer Bereich rausschneiden (voll auf 1 und halb auf 2)
                                        int bx = (int) (unit.position.X * 20 * maxminscaleX);
                                        int by = (int) (unit.position.Y * 15 * maxminscaleY);
                                        int vrangeX = (int) (unit.getVisrange() * 20 * maxminscaleX * 2);
                                        int vrangeY = (int) (unit.getVisrange() * 15 * maxminscaleY * 2);
//                                        fowPainter.drawImage(miniMapFoWShadow[unit.visrange], bx - vrangeX, by - vrangeY);
//                                        fowPainter2.drawImage(miniMapFoWShadow[unit.visrange], bx - vrangeX, by - vrangeY);
                                        fowg2.fillOval(bx - vrangeX, by - vrangeY, vrangeX * 2, vrangeY * 2);
                                    }
                                }
                            }
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.out.println("FixMe: Illegal Position: Unit " + unit + " pos: " + unit.position);
                        }
                    }
                }
            }
            if (!fowDisabled && reCalcFow) {
                // Gebäude schneiden (fow2)
                if (buildingList != null) {

                    for (int i = 0; i < buildingList.size(); i++) {
                        Building building = buildingList.get(i);
                        // Nur eigene & team
                        if (building.playerId == myPlayerId || rgi.game.shareSight(building, rgi.game.getOwnPlayer())) {
                            // "Loch" in den fow-layer schneiden
                            int bx = (int) ((building.position.X + ((building.z1 + building.z2 - 2) * 1.0 / 2)) * 20 * maxminscaleX);
                            int by = (int) (building.position.Y * 15 * maxminscaleY);
                            // Mir sind diese Werte ehrlich gesagt net ganz klar, besonders der letzte faktor
                            int vrangeX = (int) ((building.getVisrange() + ((building.z1 + building.z2) / 4)) * 20 * maxminscaleX * 2);
                            int vrangeY = (int) ((building.getVisrange() + ((building.z1 + building.z2) / 4)) * 15 * maxminscaleY * 2);
                            fowg2.fillOval(bx - vrangeX, by - vrangeY, vrangeX * 2, vrangeY * 2);
                        }
                    }

                    fowg1.setDrawMode(Graphics.MODE_ALPHA_MAP);

                    for (int i = 0; i < buildingList.size(); i++) {
                        Building building = buildingList.get(i);
                        // Nur eigene & team
                        if (building.playerId == myPlayerId || rgi.game.shareSight(building, rgi.game.getOwnPlayer())) {
                            // "Loch" in den fow-layer schneiden
                            int bx = (int) ((building.position.X + ((building.z1 + building.z2 - 2) * 1.0 / 2)) * 20 * maxminscaleX);
                            int by = (int) (building.position.Y * 15 * maxminscaleY);
                            // Mir sind diese Werte ehrlich gesagt net ganz klar, besonders der letzte faktor
                            int vrangeX = (int) ((building.getVisrange() + ((building.z1 + building.z2) / 4)) * 20 * maxminscaleX * 2);
                            int vrangeY = (int) ((building.getVisrange() + ((building.z1 + building.z2) / 4)) * 15 * maxminscaleY * 2);
                            fowg1.fillOval(bx - vrangeX, by - vrangeY, vrangeX * 2, vrangeY * 2);
                        }
                    }
                }
                fowg1.flush();
                fowg2.flush();
            }
            if (!fowDisabled) {
                // FoW-Layer auf die Minimap
                fowMiniLayer2.draw((int) (hudX + hudSizeX * 0.1) + 1, (int) (realPixY / 7 * 1.4) + 1);
                fowMiniLayer.draw((int) (hudX + hudSizeX * 0.1) + 1, (int) (realPixY / 7 * 1.4) + 1);
            }
            for (int i = 0; i < unitList.size(); i++) {
                Unit unit = unitList.get(i);
                // Nur eigene und sichtbare rendern
                try {
                    if (unit.playerId == myPlayerId || rgi.game.shareSight(unit, rgi.game.getOwnPlayer()) || fowmap[unit.position.X][unit.position.Y] > 1) {
                        if (miniMap != null) {
                            setColorToPlayer(unit.playerId, g);
                            g.fillRect((int) ((unit.position.X * 1.0 / sizeX * hudSizeX * 0.8) + hudX + hudSizeX * 0.1) - 1, (int) ((unit.position.Y * 1.0 / sizeY * realPixY * 2 / 7 * 0.8) + realPixY / 7 * 1.371428) + 2, 3, 3);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.println("FixMe: Illegal Position: Unit " + unit + " pos: " + unit.position);
                }
            }
            // Sichtbaren Bereich anzeigen
            g.setColor(Color.lightGray);
            g.drawRect((int) ((positionX * 1.0 / sizeX * hudSizeX * 0.8) + hudX + hudSizeX * 0.1), (int) ((positionY * 1.0 / sizeY * realPixY * 2 / 7 * 0.8) + realPixY / 7 * 1.4), miniMapViewSizeX, miniMapViewSizeY);
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
            g.drawImage(interactivehud, hudX, realPixY * 3 / 7);

            // Ressourcen & Truppenlimit reinschreiben
            g.setColor(Color.black);
            g.setFont(fonts[1]);
            try {
                g.drawString(String.valueOf(rgi.game.getOwnPlayer().res1), (int) (hudX + hudSizeX * 0.1), (int) (realPixY * 0.015));
                g.drawString(String.valueOf(rgi.game.getOwnPlayer().res2), (int) (hudX + hudSizeX * 0.43), (int) (realPixY * 0.015));
                g.setColor(epoche > 1 ? Color.black : Color.lightGray);
                g.drawString(String.valueOf(rgi.game.getOwnPlayer().res3), (int) (hudX + hudSizeX * 0.1), (int) (realPixY * 0.063));
                g.setColor(epoche > 2 ? Color.black : Color.lightGray);
                g.drawString(String.valueOf(rgi.game.getOwnPlayer().res4), (int) (hudX + hudSizeX * 0.43), (int) (realPixY * 0.063));
                g.setColor(epoche > 5 ? Color.black : Color.lightGray);
                g.drawString(String.valueOf(rgi.game.getOwnPlayer().res5), (int) (hudX + hudSizeX * 0.1), (int) (realPixY * 0.11));
                g.setColor(Color.black);
                g.drawString(rgi.game.getOwnPlayer().currentlimit + "/" + Math.min(rgi.game.getOwnPlayer().maxlimit, 100), (int) (hudX + hudSizeX * 0.43), (int) (realPixY * 0.11));
            } catch (NullPointerException ex) {
            }

        } catch (org.newdawn.slick.SlickException ex) {
            ex.printStackTrace();
        }
    }

    private void buildInteractiveHud() {
        // Baut den interaktiven Teil des Huds neu auf - z.B. da wo Einheiten etc angezeigt werden
        try {
            int px = -1;
            int py = 0;
            Graphics g2 = null;
            ArrayList<GameObject> selected = rgi.rogGraphics.inputM.selected;
            if (selected.isEmpty() && this.tempInfoObj == null) {
                // Hud leeren
                g2 = interactivehud.getGraphics();
                g2.setBackground(new org.newdawn.slick.Color(0.0f, 0.0f, 0.0f, 0.0f));
                //g2.clearRect(0, 0, interactivehud.getWidth(), interactivehud.getHeight());
                g2.clear();
            }
            if ((selected.isEmpty() && this.tempInfoObj != null) || selected.size() == 1) {
                // Hud-Info mode
                if (selected.size() == 1) {
                    tempInfoObj = selected.get(0);
                }
                g2 = interactivehud.getGraphics();
                g2.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
                //g2.clearRect(0, 0, interactivehud.getWidth(), interactivehud.getHeight());
                g2.clear();

                // Spezifische Infos anzeigen:
                if (tempInfoObj.getClass().equals(Unit.class)) {
                    // Unit - Infos rendern
                    Unit unit = (Unit) tempInfoObj;
                    // Bildchen
                    GraphicsImage img = imgMap.get(unit.graphicsdata.defaultTexture);
                    int dx1 = (int) (hudSizeX * 0.15);
                    int dy1 = (int) (realPixY * 2 / 7 * 0.2); // realPixY * 2/35
                    int dx2 = (int) (hudSizeX * 0.15 + hudSizeX * 0.7 / 5); // hudSizeX * 0.29
                    int dy2 = (int) (realPixY * 2 / 7 * 0.2 + realPixY * 2 / 7 * 0.7 / 4); // realPixY * 3/28
                    if (img != null) {
                        g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight());
                    }
                    // Spielername:
                    g2.setColor(Color.gray);
                    g2.setFont(fonts[2]);
                    g2.drawString(rgi.game.getPlayer(unit.playerId).nickName, (int) (hudSizeX * 0.4), (int) (dy2 * 0.5) - 10);
                    // Einheitename
                    g2.setColor(Color.black);
                    g2.setFont(fonts[1]);
                    g2.drawString(unit.name, (int) (hudSizeX * 0.4), (int) (dy2 * 0.7) - 10);
                    // HP
                    g2.setFont(fonts[2]);
                    g2.drawString("HP:  " + unit.getHitpoints() + " / " + unit.getMaxhitpoints(), (int) (hudSizeX * 0.41), (int) (dy2 * 0.9) - 10);
                    // Rüstung
                    g2.setFont(fonts[0]);
                    g2.drawString("Armortype: ", dx1, (int) (dy2 * 1.1) - 10);
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
                    g2.drawString(atype, (int) (dx1 * 4.1), (int) (dy2 * 1.1) - 10);
                    //Geschwindigkeit
                    g2.drawString("Speed: ", dx1, (int) (dy2 * 1.25) - 10);
                    g2.drawString(String.valueOf(unit.speed), (int) (dx1 * 4.1), (int) (dy2 * 1.25) - 10);
                    //Reichweite
                    g2.drawString("Range: ", dx1, (int) (dy2 * 1.4) - 10);
                    if (unit.getRange() == 2) {
                        g2.drawString("Melee", (int) (dx1 * 4.1), (int) (dy2 * 1.4) - 10);
                    } else {
                        g2.drawString(String.valueOf(unit.getRange()), (int) (dx1 * 4.1), (int) (dy2 * 1.4) - 10);
                    }
                    // Schaden:
                    g2.drawString("Damage: ", dx1, (int) (dy2 * 1.60) - 10);
                    g2.drawString(String.valueOf(unit.getDamage()), (int) (dx1 * 4.1), (int) (dy2 * 1.60) - 10);
                    // Special-Schaden gegen Rüstungsklassen:
                    float yposition = 1.75f;
                    if (unit.antilightinf != 100) {
                        g2.drawString(" vs. Light Infantry: ", dx1, (int) (dy2 * yposition) - 10);
                        g2.drawString(unit.antilightinf + "%", (int) (dx1 * 4.1), (int) (dy2 * yposition) - 10);
                        yposition += 0.15;
                    }
                    if (unit.antiheavyinf != 100) {
                        g2.drawString(" vs. Heavy Infantry: ", dx1, (int) (dy2 * yposition) - 10);
                        g2.drawString(unit.antiheavyinf + "%", (int) (dx1 * 4.1), (int) (dy2 * yposition) - 10);
                        yposition += 0.15;
                    }
                    if (unit.antikav != 100) {
                        g2.drawString(" vs. Cavalry: ", dx1, (int) (dy2 * yposition) - 10);
                        g2.drawString(unit.antikav + "%", (int) (dx1 * 4.1), (int) (dy2 * yposition) - 10);
                        yposition += 0.15;
                    }
                    if (unit.antivehicle != 100) {
                        g2.drawString(" vs. Vehicle: ", dx1, (int) (dy2 * yposition) - 10);
                        g2.drawString(unit.antivehicle + "%", (int) (dx1 * 4.1), (int) (dy2 * yposition) - 10);
                        yposition += 0.15;
                    }
                    if (unit.antitank != 100) {
                        g2.drawString(" vs. Tanks: ", dx1, (int) (dy2 * yposition) - 10);
                        g2.drawString(unit.antitank + "%", (int) (dx1 * 4.1), (int) (dy2 * yposition) - 10);
                        yposition += 0.15;
                    }
                    if (unit.antiair != 0) {
                        g2.drawString(" vs. Air: ", dx1, (int) (dy2 * yposition) - 10);
                        g2.drawString(unit.antiair + "%", (int) (dx1 * 4.1), (int) (dy2 * yposition) - 10);
                        yposition += 0.15;
                    }
                    if (unit.antibuilding != 100) {
                        g2.drawString(" vs. Buildings: ", dx1, (int) (dy2 * yposition) - 10);
                        g2.drawString(unit.antibuilding + "%", (int) (dx1 * 4.1), (int) (dy2 * yposition) - 10);
                    }

                } else if (tempInfoObj.getClass().equals(Building.class)) {
                    // Building - Infos rendern
                    Building building = (Building) tempInfoObj;
                    // Bildchen
                    GraphicsImage img = imgMap.get(building.defaultTexture);
                    int dx1 = (int) (hudSizeX * 0.15);
                    int dy1 = (int) (realPixY * 2 / 7 * 0.2);
                    int dx2 = (int) (hudSizeX * 0.15 + hudSizeX * 0.7 / 5);
                    int dy2 = (int) (realPixY * 2 / 7 * 0.2 + realPixY * 2 / 7 * 0.7 / 4);
                    if (img != null) {
                        g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight());
                    }
                    // Spielername:
                    g2.setColor(Color.gray);
                    g2.setFont(fonts[2]);
                    g2.drawString(rgi.game.getPlayer(building.playerId).nickName, (int) (hudSizeX * 0.4), (int) (dy2 * 0.5) - 10);
                    // Gebäudename
                    g2.setColor(Color.black);
                    g2.setFont(fonts[1]);
                    g2.drawString(building.name, (int) (hudSizeX * 0.4), (int) (dy2 * 0.7) - 10);
                    // HP
                    g2.setFont(fonts[2]);
                    g2.drawString("HP:  " + building.getHitpoints() + " / " + building.getMaxhitpoints(), (int) (hudSizeX * 0.41), (int) (dy2 * 0.9) - 10);
                    // Rüstung
                    g2.setFont(fonts[0]);
                    g2.drawString("Armortype: ", dx1, (int) (dy2 * 1.1) - 10);
                    g2.drawString("Building", (int) (dx1 * 4.1), (int) (dy2 * 1.1) - 10);
                    if (building.isbuilt) {
                        // Fortschritt anzeigen
                        g2.drawString("Constructing:  ", dx1, (int) (dy2 * 1.3) - 10);
                        g2.drawString(Math.round(building.buildprogress * 100) + "%", (int) (dx1 * 4.1), (int) (dy2 * 1.3) - 10);
                    } else {
                        double movedown = 1.3;
                        if (building.limit < 0) {
                            g2.drawString("Limit: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString("+" + (building.limit * -1), (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.2;
                        }
                        if (building.maxIntra > 0) {
                            // Anzahl der Arbeiter im Gebäude
                            g2.drawString("Harvesters: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.intraUnits.size() + "/" + building.maxIntra, (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.2;
                        }
                        if (building.heal > 0) {
                            // Anzahl der Arbeiter im Gebäude
                            g2.drawString("Heals: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(String.valueOf(building.heal), (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.2;
                        }
                        if (building.getDamage() != 0) {
                            g2.drawString("Range: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(String.valueOf(building.getRange()), (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                            g2.drawString("Damage: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(String.valueOf(building.getDamage()), (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                        }
                        if (building.antilightinf != 100) {
                            g2.drawString(" vs. Light Infantry: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.antilightinf + "%", (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                        }
                        if (building.antiheavyinf != 100) {
                            g2.drawString(" vs. Heavy Infantry: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.antiheavyinf + "%", (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                        }
                        if (building.antikav != 100) {
                            g2.drawString(" vs. Cavalry: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.antikav + "%", (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                        }
                        if (building.antivehicle != 100) {
                            g2.drawString(" vs. Vehicle: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.antivehicle + "%", (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                        }
                        if (building.antitank != 100) {
                            g2.drawString(" vs. Tanks: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.antitank + "%", (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                        }
                        if (building.antiair != 0) {
                            g2.drawString(" vs. Air: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.antiair + "%", (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                            movedown += 0.15;
                        }
                        if (building.antibuilding != 100) {
                            g2.drawString(" vs. Buildings: ", dx1, (int) (dy2 * movedown) - 10);
                            g2.drawString(building.antibuilding + "%", (int) (dx1 * 4.1), (int) (dy2 * movedown) - 10);
                        }
                    }

                } else if (tempInfoObj.getClass().equals(Ressource.class)) {
                    // Ressource - Infos zeigen, falls weit genug erforscht
                    Ressource res = (Ressource) tempInfoObj;
                    boolean showinfo = (res.getType() < 3 || (res.getType() == Ressource.RES_METAL && epoche >= 2) || (res.getType() == Ressource.RES_COINS) && epoche >= 3);
                    // Bildchen
                    GraphicsImage img = imgMap.get(res.getTex());
                    int dx1 = (int) (hudSizeX * 0.15);
                    int dy1 = (int) (realPixY * 2 / 7 * 0.2);
                    int dx2 = (int) (hudSizeX * 0.15 + hudSizeX * 0.7 / 5);
                    int dy2 = (int) (realPixY * 2 / 7 * 0.2 + realPixY * 2 / 7 * 0.7 / 4);
                    if (img != null) {
                        g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight());
                    }
                    // Name
                    g2.setColor(Color.black);
                    g2.setFont(fonts[1]);
                    String name = "";
                    if (showinfo) {
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
                    } else {
                        name = "???";
                    }
                    g2.drawString(name, (int) (hudSizeX * 0.4), (int) (dy2 * 0.7) - 10);
                    // Energie:
                    g2.setFont(fonts[2]);
                    if (showinfo) {
                        g2.drawString("Resources left:  " + res.hitpoints, (int) (hudSizeX * 0.41), (int) (dy2 * 0.9) - 10);
                    } else {
                        g2.drawString("Resources left:  ???", (int) (hudSizeX * 0.41), (int) (dy2 * 0.9) - 10);
                    }
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
                // Fertig, Struktur ist aufgebaut, jetzt zeichnen
                if (selected.size() > 1) {
                    g2 = interactivehud.getGraphics();
                    g2.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));
                    //g2.clearRect(0, 0, interactivehud.getWidth(), interactivehud.getHeight());
                    g2.clear();
                    for (int m = 0; m < interSelFields.size(); m++) {
                        // Position bestimmen:
                        px++;
                        if (px > 3) {
                            py++;
                            px = 0;
                        }
                        // Dahin zeichnen:
                        g2.setColor(Color.black);
                        GameObject rgo = interSelFields.get(m)[0];
                        String sel;
                        Image img = null;
                        // Versuche Gebäude
                        try {
                            Unit u = (Unit) rgo;
                            // Ok, Unit
                            if ((sel = u.graphicsdata.hudTexture) == null) {
                                sel = u.graphicsdata.defaultTexture;
                            }
                            if (sel != null) {
                                GraphicsImage rimg = imgMap.get(sel);
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
                                GraphicsImage rimg = imgMap.get(sel);
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
                            g2.drawImage(img, dx1, dy1, dx2, dy2, 0, 0, img.getWidth(), img.getHeight());
                            g2.setColor(Color.black);
                            g2.drawRect(dx1, dy1, dx2 - dx1, dy2 - dy1);
                            g2.drawRect(dx1 + 1, dy1 + 1, (dx2 - dx1) - 2, (dy2 - dy1) - 2);
                            g2.drawString(String.valueOf(interSelFields.get(m).length), dx1 + 2, dy1);
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
                            GraphicsImage img = null;
                            try {
                                tex = ability.symbols[epoche];
                                if (tex == null) {
                                    tex = ability.symbols[0]; // Wenn für die Spezielle Epoche keins da ist, dann das allgemeine versuchen
                                }
                                img = imgMap.get(tex);
                            } catch (Exception ex) {
                                rgi.logger("[Graphics][ERROR]: Symbol for ability \"" + ability.name + "\" (epoche " + epoche + ") not found!");
                            }
                            if (img != null) {
                                // Jetzt Zeichnen
                                int dx1 = (int) (hudSizeX * 0.15 + px * (hudSizeX * 0.7 * 4 / 15));
                                int dy1 = (int) ((realPixY * 2 / 7 * 0.2 + py * (realPixY * 48 / 560)) + realPixY * 0.257);
                                int dx2 = (int) (hudSizeX * 0.15 + px * (hudSizeX * 0.7 * 4 / 15) + hudSizeX * 0.7 / 5);
                                int dy2 = (int) ((realPixY * 2 / 7 * 0.2 + py * (realPixY * 48 / 560) + realPixY * 2 / 7 * 0.7 / 4) + realPixY * 0.257);
                                g2.drawImage(img.getImage(), dx1, dy1, dx2, dy2, 0, 0, img.getImage().getWidth(), img.getImage().getHeight());
                                if (!ability.isAvailable()) {
                                    g2.setColor(fowGray);
                                    g2.fillRect(dx1, dy1, dx2 - dx1, dy2 - dy1);
                                }
                                g2.setColor(ability.frameColor);
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

                                    if (progress > -0.001 && progress < 0.001) {
                                        g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.6f));
                                        g2.fillRect(dx1, dy1, dx2 -dx1, dy2 - dy1);
                                    } else {
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
                                        g2.setColor(new Color(0.0f, 0.0f, 0.0f, 0.6f));
                                        g2.fill(poly);
                                    }
                                    g2.setColor(Color.black);
                                    if (number != -1) {
                                        g2.drawString(Integer.toString(number), dx1 + 2, dy1);
                                    }
                                }
                            }

                        }

                    }
                }
            }
            // Komplett fertig, Struckturiert und alles - prima. Ende

        } catch (org.newdawn.slick.SlickException ex) {
            ex.printStackTrace();
        }
    }

    private void setColorToPlayer(int playerId, Graphics g2) {
        // Stellt die Farbe für die Minimap-Punkte richtig ein
        g2.setColor(rgi.game.getPlayer(playerId).color);
    }

    private void renderCursor() {
        if (renderPicCursor) {
            // Pic-Cursor
            // Einfach Bild an die gerasterte Cursorposition rendern.
            renderPic.getImage().draw(framePos.width * 20, framePos.height * 15);
        }
    }

    private void paint_debug(Graphics g2) {
        System.out.println("Printing Debug-Tree to 0,0");
        g2.drawImage(imgMap.get("img/fix/testtree1.png").getImage(), 0, 0);
    }

    private void renderBackground() {
        /*if (modi != 3) {
        g2.drawImage(renderBackground, 0, 0);
        } else {
        if (g2 == null) {
        }
        g2.drawImage(renderBackground, 0, 0, realPixX, realPixY, positionX * 20, positionY * 15, (positionX * 20) + realPixX, (positionY * 15) + realPixY);
        } */
        //System.out.println("This is runRenderRound, searching for <" + searchprop + "> mapsize(x,y) view [x,y]: (" + sizeX + "," + sizeY + ") [" + viewX + "," + viewY + "]");


        // Alte Methode - Große Background-Bilder im Speicher verträgt nicht jedes System
        //runRenderRound("ground_tex", g2);
        //runRenderRound("fix_tex", g2);

        // Neu berechnen oder altes nehmen?
        if (rBvX == positionX && rBvY == positionY && renderBackground != null) {
            // Gleich altes nehmen
            renderBackground.draw(0, 0);
        } else {
            if (renderBackground == null) {
                System.out.println("ACHTUNG: RE-RENDERING BACKGROUND!!!!!");
            }
            // Neu berechnen:
            try {
                if (renderBackground == null) {
                    renderBackground = new Image(hudX, realPixY);
                }
                Graphics g3 = renderBackground.getGraphics();
                rBvX = positionX;
                rBvY = positionY;
                g3.setColor(Color.black);
                g3.fillRect(0, 0, renderBackground.getWidth(), renderBackground.getHeight());

                for (int x = -1; x < sizeX && x < (viewX + 1); x++) {
                    for (int y = -2; y < sizeY && y < (viewY + 1); y++) {
                        if ((x + y) % 2 == 1) {
                            continue;
                        }
                        // X und Y durchlaufen, wenn ein Bild da ist, dann einbauen
                        //              System.out.println("Searching for " + x + "," + y);
                        String ground = null;
                        String fix = null;
                        try {
                            ground = visMap[x + positionX][y + positionY].getProperty("ground_tex");
                            fix = visMap[x + positionX][y + positionY].getProperty("fix_tex");
                        } catch (Exception ex) {
                            // Kann beim Scrollein vorkommen - Einfach nichts zeichnen, denn da ist die Map zu Ende...
                        }
                        // Was da?
                        if (ground != null) {
                            // Bild suchen und einfügen
                            GraphicsImage tempImage = imgMap.get(ground);

                            if (tempImage != null) {
                                g3.drawImage(tempImage.getImage(), x * 20, y * 15);
                            } else {
                                System.out.println("[RME][ERROR]: Image \"" + ground + "\" not found!");
                            }
                            if (fix != null) {
                                // Bild suchen und einfügen
                                GraphicsImage fixImage = imgMap.get(fix);

                                if (fixImage != null) {
                                    g3.drawImage(fixImage.getImage(), x * 20, y * 15);
                                } else {
                                    System.out.println("[RME][ERROR]: Image \"" + fix + "\" not found!");
                                }

                            }

                        }
                        //  System.out.println(x + " " + y);
                    }
                }

                g3.flush();
                renderBackground.draw(0, 0);
            } catch (org.newdawn.slick.SlickException ex) {
            }
        }

    }

    protected void changePaintCursor(MapEditorCursor rmec) {
        drawCursor = rmec;
    }

    private void renderCol() {
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
                        colModeImage.getImage().draw(x * 20, y * 15);
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
                        colModeImage.getImage().draw(x * 20, y * 15);
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    private void renderServerCol() {
        // Murks, aber die position müssen gerade sein...
        if (positionX % 2 == 1) {
            positionX--;
        }
        if (positionY % 2 == 1) {
            positionY--;
        }
        // Rendert die blaueb Kollisionsfarbe
        for (int x = 0; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0; y < sizeY && y < viewY; y = y + 2) {
                // Hat dieses Feld Kollision?
                try {
                    if (visMap[x + positionX][y + positionY].getCollision() != collision.free) {
                        // Bild einfügen
                        imgMap.get("img/game/highlight_blue.png").getImage().draw(x * 20, y * 15);
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
                        imgMap.get("img/game/highlight_blue.png").getImage().draw(x * 20, y * 15);
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    private void renderCoords(Graphics g2) {
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
                g2.drawString((x + positionX) + "|" + (y + positionY), x * 20 + 10, y * 15 + 20);
            }
        }
        for (int x = 0 + 1; x < sizeX && x < viewX; x = x + 2) {
            for (int y = 0 + 1; y < sizeY && y < viewY; y = y + 2) {
                g2.drawString((x + positionX) + "|" + (y + positionY), x * 20 + 10, y * 15 + 20);
            }
        }
    }

    private void renderHealth(GameObject rO, Graphics g2, int dX, int dY) {
        try {
            Unit rU = (Unit) rO;
            // Billigen Balken rendern
            g2.setColor(Color.black);
            if (rU.selectionShadow == 1) {
                g2.fillRect(dX + 9, dY - 1, 7, 7);
            }
            // Farbe bestimmen
            double percent = 1.0 * rU.getHitpoints() / rU.getMaxhitpoints();
            if (percent >= 0.3) {
                g2.setColor(new Color((int) (255 - (((percent - 0.5) * 2) * 255)), 255, 0));
            } else {
                g2.setColor(new Color(255, (int) ((percent * 2) * 255), 0));
            }
            g2.fillRect(dX + 10, dY, 5, 5);
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
                g2.setColor(Color.black);
                g2.fillRect(dX + cpX, dY - cpY, (lf / 2) + 2, 5);
                // Farbe bestimmen
                double percent = 1.0 * rB.getHitpoints() / rB.getMaxhitpoints();
                if (percent >= 0.5) {
                    g2.setColor(new Color((int) (255 - (((percent - 0.5) * 2) * 255)), 255, 0));
                }
                if (percent < 0.5) {
                    g2.setColor(new Color(255, (int) ((percent * 2) * 255), 0));
                }
                percent *= 100;
                // Entsprechend viel füllen
                int fillperc = (int) (percent * (lf / 2) / 100);
                g2.fillRect(dX + cpX + 1, dY - cpY + 1, fillperc, 3);
            } catch (ClassCastException ex2) {
            }

        }
    }

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

    public static Image grayScale(Image im) {

        // Verwandelt ein Bild ein eine Grayscale-Version, behält aber die Transparenz
        ImageBuffer grayImage = new ImageBuffer(im.getWidth(), im.getHeight());
        //Graphics g = grayImage.getGraphics();

        for (int x = 0; x < im.getWidth(); x++) {
            for (int y = 0; y < im.getHeight(); y++) {
                Color argb = im.getColor(x, y);
                int a = argb.getAlpha();
                int r = argb.getRed();
                int g = argb.getGreen();
                int b = argb.getBlue();
                int l = (int) (.299 * r + .587 * g + .114 * b); //luminance
                grayImage.setRGBA(x, y, l, l, l, a);
            }
        }
        return grayImage.getImage();
    }

    public void setPicture(GraphicsImage b) {
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

    public void setImageMap(HashMap<String, GraphicsImage> newMap) {
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
        positionY = posY;
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
            // Cursor unsichtbar machen
            parent.setMouseGrabbed(true);
        } else {
            parent.setMouseGrabbed(false);
        }
    }

    public void startEditorRender() {
        // Jetzt keine dummen Sachen, sondern echten EditorContent rendern
        modi = 2;
        this.repaint();
    }

    // Löscht die gesamte Fow-Map (überschreibt sie mit 0 (unerkundet))
    // Bereits erkundete Bereiche bleiben erkundet
    private void clearFogOfWar() {
        for (int x = 0; x < fowmap.length; x++) {
            for (int y = 0; y < fowmap[0].length; y++) {
                if (fowmap[x][y] == 2) {
                    fowmap[x][y] = 0;
                }
            }
        }
    }

    /**
     * Setzt alles auf Sichtbar. Kann nicht rückgängig gemacht werden. Ist cheaten
     */
    public void freeFogOfWar() {
        for (int x = 0; x < fowmap.length; x++) {
            for (int y = 0; y < fowmap[0].length; y++) {
                fowmap[x][y] = 3;
            }
        }
    }

    /**
     * Startet das Rendern
     * @param rImg ein VolatileImage, im Savemode ein BufferedImage
     */
    public void startRender() {
        // Echter Game-Rendering Mode
        // rImg ist Hintergrundbild
        modi = 3;
        initRun = new Date();// Das mach ich jetzt genau ein mal, das muss in den Gebäudebau-Handling-Code...
//        try {Thread.sleep(100000);} catch (Exception ex) {}
    }

    public void initBRSel() {
        // Das neue, rasend schnelle Gebäude-Selektionssystem.
        // Großes int-Array Initialisieren
        brSel = new int[sizeX][];
        for (int i = 0; i < sizeX; i++) {
            brSel[i] = new int[sizeY];
        }
    }

    /**
     * Erneuert das komplette BRSel-System.
     * Normalerweise nur einmal beim Spielstart nötig,
     * Änderungen werden dann direkt angewendet.
     */
    public void refreshBRSel() {
        // Alles löschen
        for (int x = 0; x < brSel.length; x++) {
            for (int y = 0; y < brSel[x].length; y++) {
                brSel[x][y] = 0;
            }
        }
        // Alles reinschreiben
        // Hier ist diese Schleife erlaubt, da vor dem Spielstart
        for (Building b : buildingList) {
            placeGO(b);
        }
        for (Ressource r : resList) {
            placeGO(r);
        }
    }

    /**
     * Platziert ein GameObject auf dem BRSel
     * @param obj
     */
    public void placeGO(GameObject obj) {
        if (obj.getClass().equals(Ressource.class)) {
            Ressource r = (Ressource) obj;
            brSel[obj.position.X][obj.position.Y] = obj.netID;
            if (r.getType() > 2) {
                // Diese Ressourcen sind größer
                brSel[obj.position.X + 2][obj.position.Y] = obj.netID;
                brSel[obj.position.X + 1][obj.position.Y + 1] = obj.netID;
                brSel[obj.position.X + 1][obj.position.Y - 1] = obj.netID;
            }
        } else if (obj.getClass().equals(Building.class)) {
            // Das müssen wir casten, um z1 und z2 zu bekommen
            Building b = (Building) obj;
            for (int z1 = 0; z1 < b.z1; z1++) {
                for (int z2 = 0; z2 < b.z2; z2++) {
                    brSel[b.position.X + z1 + z2][b.position.Y - z1 + z2] = b.netID;
                }
            }
        }
    }

    /**
     * Löscht ein GO vom BRSel
     * @param obj
     */
    public void deleteGO(GameObject obj) {
        if (obj.getClass().equals(Ressource.class)) {
            brSel[obj.position.X][obj.position.Y] = 0;
        } else if (obj.getClass().equals(Building.class)) {
            // Das müssen wir casten, um z1 und z2 zu bekommen
            Building b = (Building) obj;
            for (int z1 = 0; z1 < b.z1; z1++) {
                for (int z2 = 0; z2 < b.z2; z2++) {
                    brSel[b.position.X + z1 + z2][b.position.Y - z1 + z2] = 0;
                }
            }
        }
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
            if (modi != 3) {
                repaint();
            }
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
        // calcImage(false, true);
        // MiniMap
        if (parent.isShowingFPS()) {
            parent.setShowFPS(false);
            transformMiniMap();
            mergeHud();
            parent.setShowFPS(true);
        } else {
            transformMiniMap();
            mergeHud();
        }
    }

    public void initMiniMap() {
        if (parent.isShowingFPS()) {
            parent.setShowFPS(false);
            transformMiniMap();
            mergeHud();
            parent.setShowFPS(true);
        } else {
            transformMiniMap();
            mergeHud();
        }
    }

    public void updateMiniMap() {
        calcMiniMap();
    }

    private void transformMiniMap() {
        try {
            try {
                // Erstellt einen neue Basis-Minimap
                Image tempImg2 = new Image(visMap.length * 2, visMap[0].length * 2);
                Graphics tempGra = tempImg2.getGraphics();
                // Skalierungsfaktor berechnen
                for (int x = 0; x < visMap.length; x++) {
                    for (int y = 0; y < visMap[0].length; y++) {
                        if ((x + y) % 2 == 1) {
                            continue;
                        } else {
                            try {
                                GraphicsImage tex = imgMap.get(visMap[x][y].getProperty("ground_tex"));
                                if (tex != null) {
                                    Color pcol = tex.getImage().getColor(20, 20);
                                    tempGra.setColor(pcol);
                                    tempGra.fillRect(x * 2, y * 2, 4, 4);
                                }
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
                tempGra.flush();
                miniMap = tempImg2.getScaledCopy((int) (hudSizeX * 0.8), (int) (realPixY * 2 / 7 * 0.8));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Graphics g3 = null;
            g3 = fowMiniLayer.getGraphics();
            g3.setBackground(Color.black);
            //g3.clearRect(0, 0, fowMiniLayer.getWidth(), fowMiniLayer.getHeight());
            g3.clear();
        } catch (SlickException ex) {
            ex.printStackTrace();
        }

    }

    protected void mergeHud() {
        try {
            // Liefert das Hud zurück, mit der Minimap reingearbeitet
            Graphics g4 = null;
            if (hudGround == null) {
                hudGround = new Image(hudSizeX, realPixY);
            }
            g4 = hudGround.getGraphics();
            // Löschen
            g4.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.5f));
            //g4.clearRect(0, 0, hudGround.getWidth(), hudGround.getHeight());
            g4.clear();
            // Bilder passend rendern
            if (epoche != 0) {
                // Hud skaliert reinrendern
                Image hud = huds[epoche];
                if (hud == null) {
                    hud = huds[1];
                }
                g4.drawImage(hud, 0, 0, hudGround.getWidth(), hudGround.getHeight(), 0, 0, hud.getWidth(), hud.getHeight());
            }
            g4.drawImage(miniMap, (int) (hudSizeX * 0.1) + 1, (int) (realPixY / 7 * 1.4));
            g4.flush();
        } catch (SlickException ex) {
            ex.printStackTrace();
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

    

    public void klickedOnMiniMap(final int button, final int x, final int y, final int clickCount) {
        // Koordinaten finden
        Dimension tempD = searchMiniMid(x, y);
        // Sicht auf Mitte davon setzen
        rgi.rogGraphics.jumpTo(tempD.width - (viewX / 2), tempD.height - (viewY / 2));
    }

    public void klickedOnMiniMap(int x, int y) {
        // Koordinaten finden
        Dimension tempD = searchMiniMid(x, y);
        // Sicht auf Mitte davon setzen
        rgi.rogGraphics.jumpTo(tempD.width - (viewX / 2), tempD.height - (viewY / 2));
    }

    public int getModi() {
        // liefert den aktuellen renderModus zurück
        return modi;
    }

    public void renderWayPoint(Graphics g2) {
        for (int i = 0; i < wayPath.size(); i++) {
            // Liste durchgehen
            // rendern
            g2.drawImage(wayPointHighlighting[0], (wayPath.get(i).X - positionX) * 20, (wayPath.get(i).Y - positionY) * 15);

        }
        if (wayOpenList != null) {
            for (int o = 0; o < wayOpenList.size(); o++) {
                Position pos = (Position) wayOpenList.remove();
                g2.drawImage(wayPointHighlighting[3], (pos.X - positionX) * 20, (pos.Y - positionY) * 15);
            }
        }
        if (wayClosedList != null) {
            for (int u = 0; u < wayClosedList.size(); u++) {
                g2.drawImage(wayPointHighlighting[2], (wayClosedList.get(u).X - positionX) * 20, (wayClosedList.get(u).Y - positionY) * 15);
            }
        }
    }

    public void appendOpenList(PriorityBuffer oL) {
        wayOpenList = oL;
    }

    public void appendClosedList(ArrayList<Position> cL) {
        wayClosedList = cL;
    }

    /**
     * Rendert eine neue Minimap - Rasend schnell, weil nichts skaliert werden muss.
     * Benutzt die GFM-Minimap als Basis
     *
     */
    public void calcMiniMap() {
        try {
            if (buildingLayer == null) {
                buildingLayer = new Image((int) (hudSizeX * 0.8), (int) (realPixY / 7 * 2 * 0.8));
            }
            if (fowMiniLayer == null) {
                fowMiniLayer = new Image((int) (hudSizeX * 0.8), (int) (realPixY / 7 * 2 * 0.8));
            }

            // Fps-Anzeige temporär abschalten, sonst kann die Zahl auch im BuildingLayer erscheinen
            boolean fpson = parent.isShowingFPS();
            parent.setShowFPS(false);

            // Bildchen ist schon da:
            Graphics g2 = null;

            g2 = buildingLayer.getGraphics();

            g2.setBackground(new Color(0.0f, 0.0f, 0.0f, 0.0f));

            //g2.clearRect(0, 0, buildingLayer.getWidth(), buildingLayer.getHeight());
            g2.clear();

            // Einfach noch alle Gebäude mit dem vorgescaleten Bildchen reinrendern
            for (Building building : buildingList) {
                if (building.playerId == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(building, rgi.game.getOwnPlayer()) || building.wasSeen) {
                    for (int z1 = 0; z1 < building.z1; z1++) {
                        for (int z2 = 0; z2 < building.z2; z2++) {
                            // Hierhin das Bildchen zeichnen
                            g2.drawImage(coloredImgMap.get("img/game/ground.png" + building.playerId).getImage(), (int) ((building.position.X + z1 + z2) * 20 * maxminscaleX), (int) ((building.position.Y - z1 + z2) * 15 * maxminscaleY));
                        }
                    }
                }

            }
            g2.flush();
            if (fpson) {
                parent.setShowFPS(true);
            }
        } catch (org.newdawn.slick.SlickException ex) {
            ex.printStackTrace();
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

    public void calcColoredMaps(Color[] colors) {
        // Berechnet die eingefärbten Texturen
        ArrayList<GraphicsImage> tList = new ArrayList<GraphicsImage>();
        tList.addAll(coloredImgMap.values());
        coloredImgMap.clear();
        for (int i = 0; i < tList.size(); i++) {
            Image im = (Image) tList.get(i).getImage();
            for (int playerId = 0; playerId < colors.length; playerId++) {
                ImageBuffer preImg = new ImageBuffer(im.getWidth(), im.getHeight());
                for (int x = 0; x < im.getWidth(); x++) {
                    for (int y = 0; y < im.getHeight(); y++) {
                        // Das ganze Raster durchgehen und Farben ersetzen
                        // Ersetzfarbe da?
                        Color col = im.getColor(x, y);
                        //int rgb = im.getRGB(x, y);

                        //float[] col = Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, null);
                        float[] hsb = java.awt.Color.RGBtoHSB(col.getRed(), col.getGreen(), col.getBlue(), null);
                        if (hsb[0] >= 0.8583333f && hsb[0] <= 0.8666666f) {
                            // Ja, ersetzen
                            Color tc = colors[playerId];
                            preImg.setRGBA(x, y, tc.getRed(), tc.getGreen(), tc.getBlue(), col.getAlpha());

                        } else {
                            preImg.setRGBA(x, y, col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());
                        }
                    }
                }
                // Bild berechnet, einfügen
                GraphicsImage newImg = new GraphicsImage(preImg.getImage());
                newImg.setImageName(tList.get(i).getImageName());
                coloredImgMap.put(newImg.getImageName() + playerId, newImg);
            }

        }
    }

    public void setColModeImage(GraphicsImage img) {
        colModeImage = img;
        Image temp = img.getImage();
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
        // Sucht Gebäude via BRSel
        try {
            return rgi.mapModule.getBuildingviaID(brSel[sX][sY]);
        } catch (Exception ex) {
            return null;
        }
    }

    public void enableWayPointHighlighting(ArrayList<Position> path) {
        // Zeigt den Path an
        wayPath = path;
        enableWaypointHighlighting = true;
//        calcImage(false, true);
        System.out.println("AddMe: Enable Waypoint Highlighting");

    }

    /**
     * Die Updates des FoW dürfen nicht während der Bildberechnung sein, sonst flackert es.
     */
    private void updateBuildingFoW() {
        clearFogOfWar();
        for (int i = 0; i < buildingList.size(); i++) {
            Building b = buildingList.get(i);
            // FoW berechnen
            if (b.playerId == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(b, rgi.game.getOwnPlayer())) {
                cutSight(b);
            }
        }
    }

    public void buildingsChanged() {
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

    /**
     * Setzt den Fow-Sichtlayer von sterbenden Einheitn auf Unit, damit die Gegend beim nächsten Update auf erkundet gesetzt werden kann.
     * @param b
     */
    public void cutDieingBuildingSight(Building b) {
        // Mitte berechnen
        int x = (int) (b.position.X + ((b.z1 + b.z2 - 2) * 1.0 / 2));
        int y = b.position.Y;
        // Diese Position als Startfeld überhaupt zulässig?
        if (((int) x + (int) y) % 2 == 1) {
            y++;
        }
        // Dieses Feld selber auch ausschneiden
        fowmap[x][y] = 3;
        // Schablone holen
        boolean[][] pattern = fowpatmgr.getPattern(b.getVisrange() + ((b.z1 + b.z2) / 4));
        // Schablone anwenden
        int sx = x - 40;
        int sy = y - 40;
        for (int x2 = 0; x2 < 80; x2++) {
            for (int y2 = 0; y2 < 80; y2++) {
                if (pattern[x2][y2]) {
                    try {
                        fowmap[sx + x2][sy + y2] = 3;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        // Nix tun, ein Kreis kann ja den Maprand schneiden, da gibts natürlich dann kein FoW-Raster...
                    }
                }
            }
        }
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
    public void calcFogOfWar() {
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
            if (unit.playerId != ownPlayerId && !rgi.game.shareSight(unit, rgi.game.getOwnPlayer())) {
                // Nur eigene Einheiten decken den Nebel des Krieges auf
                continue;
            }
            cutSight(unit);
        }
    }

    public void calcSelClicked(final int button, final int x, final int y, int clickCount, List<InteractableGameElement> list, boolean select) {
        // Es gibt 12 Möglichkeiten, aber wir müssen nur auf Reihen uns Spalten Testen, also 7 Test
        int kX = x;
        int kY = y;
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

    public void preCalcMiniMapElements(double scalefactorX, double scalefactorY) {
        // Alle Bilder gemäß des Scale-Faktors skalieren und einfügen
        // Scalefacor = MiniMap-Auflösung / Volle Hudgroundauflösung
        // Größe der Bilder errechnen:
        try {
            int tarX = (int) (40 * scalefactorX);
            int tarY = (int) (40 * scalefactorY);
            // Bilder anlegen & gleich reinrendern
            Image nimg = new Image(tarX + 1, tarY + 1);
            Graphics g = nimg.getGraphics();
            g.drawImage(coloredImgMap.get("img/game/ground.png").getImage(), 0, 0, nimg.getWidth(), nimg.getHeight(), 0, 0, 40, 40);
            GraphicsImage timg = new GraphicsImage(nimg);
            timg.setImageName("img/game/ground.png");
            coloredImgMap.put("img/game/ground.png", timg);
            maxminscaleX = scalefactorX;
            maxminscaleY = scalefactorY;
            // Fertig
        } catch (org.newdawn.slick.SlickException ex) {
            ex.printStackTrace();
        }
    }

    public void calcOptClicked(final int button, final int x, final int y, final int clickCount, ArrayList<GameObject> list) {
        mouseX = x;
        mouseY = y;
        Ability ab = searchOptFast();
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
            if (button == 0) {
                if (rgi.rogGraphics.inputM.shiftDown) {
                    for (int i = 0; i < 5; i++) {
                        if (ab.isAvailable()) {
                            ab.perform(list.get(0));
                        }
                    }
                } else {
                    if (ab.isAvailable()) {
                        ab.perform(list.get(0));
                    }
                }
            } else if (button == 1) {
                if (rgi.rogGraphics.inputM.shiftDown) {
                    for (int i = 0; i < 5; i++) {
                        ab.antiperform(list.get(0));
                    }
                } else {
                    ab.antiperform(list.get(0));
                }
            }
            // Für noch mehr ausführen?
            if (ab.useForAll) {
                for (int i = 1; i < list.size(); i++) {
                    Ability abr = list.get(i).abilitys.get(optList.indexOf(ab));
                    if (abr != null) {
                        if (button == 0) {
                            if (abr.isAvailable()) {
                                abr.perform(list.get(i));
                            }
                        } else if (button == 1) {
                            abr.antiperform(list.get(i));
                        }
                    }
                }
            }
            // IA-Hud triggern, wegen eventuellem Progress-Rendern
            this.updateInterHud = true;

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

    public void appendCoreInner(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    public GraphicsContent() {
        super("Centuries of Rage BETA");
        selectionShadows = new ArrayList();
        interSelFields = new ArrayList<GameObject[]>();
        coloredImgMap = new HashMap<String, GraphicsImage>();
        wayPointHighlighting = new Image[4];
        optList = new ArrayList<Ability>();
        overlays = new ArrayList<Overlay>();
        fowpatmgr = new GraphicsFogOfWarPattern();
        allListLock = new ReentrantLock();
    }

    public void repaint() {
        /*  if (parent != null && parent.slickReady && initState == 0) {
        try {
        // this.render(parent, parent.getGraphics());
        } catch (SlickException ex) {
        ex.printStackTrace();
        }
        } */
    }

    @Override
    public void init(GameContainer container) throws SlickException {
        // Egal, wird automatisch gemacht
        try {
            wayPointHighlighting[0] = new Image("img/notinlist/editor/colmode.png");
            wayPointHighlighting[1] = new Image("img/notinlist/editor/highlight_yellow.png");
            wayPointHighlighting[2] = new Image("img/notinlist/editor/highlight_orange.png");
            wayPointHighlighting[2] = new Image("img/notinlist/editor/highlight_blue.png");
        } catch (SlickException ex) {
            System.out.println("Can't init waypoint-highlighting!");
        }

        // Fonts initialisieren
        fonts = new org.newdawn.slick.TrueTypeFont[6];
        fonts[0] = new TrueTypeFont(java.awt.Font.decode("Mono-10"), false);
        fonts[1] = new TrueTypeFont(java.awt.Font.decode("Mono-Bold-12"), false);
        fonts[2] = new TrueTypeFont(java.awt.Font.decode("Mono-Bold-10"), false);
        fonts[3] = new TrueTypeFont(java.awt.Font.decode("Mono-Italic-10"), false);
        fonts[4] = new TrueTypeFont(java.awt.Font.decode("Mono-8"), false);
        fonts[5] = new TrueTypeFont(java.awt.Font.decode("Mono-Bold-30"), false);

        parent = (CoreGraphics) container;
        parent.preStart2();
        parent.slickReady = true;
        parent.setClearEachFrame(false);
        parent.setAlwaysRender(true);
        parent.setVerbose(false);

        // Fenster ungefähr in die Mitte des Bildschirms
        int mx = Display.getDisplayMode().getWidth();
        int my = Display.getDisplayMode().getHeight();
        Display.setLocation(parent.getScreenWidth() / 2 - mx / 2, parent.getScreenHeight() / 2 - my / 2);
    }

    @Override
    public void update(GameContainer container, int delta) throws SlickException {
        lastDelta = delta;
    }

    @Override
    public void render(GameContainer container, Graphics g) throws SlickException {
        if (initState == 1) {
            // Logo setzen
            String[] ab = {"img/game/logo_128x128.png", "img/game/logo_32x32.png", "img/game/logo_16x16.png"};
            try {
                parent.setIcons(ab);
            } catch (SlickException ex) {
                System.out.println("ERROR: Failed to load game Icons!");
                rgi.logger("ERROR: Failed to load game Icons!");
            }
            // Einmal vorrendern
            this.paintComponent(g);
            Renderer.get().flush();
            Display.update();
            parent.initModule();
            rgi.chat.init((int) (0.5 * realPixX), (int) (0.3 * realPixY));
            rgi.teamSel.setFont(rgi.chat.getFont());
            overlays.add(rgi.teamSel);
            try {
                fireMan = new GraphicsFireManager(new Image("img/particles/simple.tga", true), rgi);
            } catch (Exception ex) {
                rgi.logger("Can't load particle img/particles/simple.tga");
            }
            if (Boolean.TRUE.equals(rgi.configs.get("benchmark")) || "true".equals(rgi.configs.get("benchmark"))) {
                // Keine Limits
                parent.setTargetFrameRate(-1);
            } else {
                int framerate;
                try {
                    framerate = Integer.parseInt((String) rgi.configs.get("framerate"));
                } catch (NumberFormatException ex) {
                    // Nicht da, default von 35 nehmen
                    framerate = 35;
                }
                parent.setTargetFrameRate(framerate);
            }
            initState = 0;
        } else if (initState == 3) {
            parent.startRendering();
            initState = 0;
        } else if (initState == 4) {
            // FinalPrepare
            parent.finalPrepare();
            // Fertig - dem Server schicken
            rgi.rogGraphics.triggerStatusWaiting();
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 3, 0, 0, 0, 0));
            initState = 0;
        }
        if (initRun != null) {
            parent.renderAndCalc(container, g);
        } else {
            this.paintComponent(g);
        }
        //this.paintComponent(g);
        checkInputEvents();

        if (buildingsChanged) {
            this.buildingsChanged();
            buildingsChanged = false;
        }
        if (epocheChanged) {
            if (parent.isShowingFPS()) {
                parent.setShowFPS(false);
                this.mergeHud();
                parent.setShowFPS(true);
            } else {
                this.mergeHud();
            }
            epocheChanged = false;
        }
        //parent.slickReady = true;
    }

    private void checkInputEvents() {
        try {
            if (lastInputEvent == 1) {
                // CalcOptClicked
                this.calcOptClicked(lastInputButton, lastInputX, lastInputY, 1, rgi.rogGraphics.inputM.selected);
            }
        } finally {
            lastInputEvent = 0;
            lastInputX = 0;
            lastInputY = 0;
            lastInputButton = 0;
        }
    }

    //@Override
    public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
    }

    //@Override
    public void inputStarted() {
    }
}
