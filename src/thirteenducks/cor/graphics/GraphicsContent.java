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
import org.lwjgl.opengl.Display;

import org.newdawn.slick.*;
import org.newdawn.slick.opengl.renderer.Renderer;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;

@SuppressWarnings("CallToThreadDumpStack")
public class GraphicsContent extends BasicGame {
    // Diese Klasse repräsentiert den Tatsächlichen GrafikINHALT von RogGraphics und RogMapEditor

    public GraphicsImage colModeImage;
    public int modi = 0; // Was gerendert werden soll, spezielle Ansichten für den Editor etc...
    CoRMapElement[][] visMap; // Die angezeigte Map
    public boolean renderCursor = false;
    public boolean renderPicCursor = false; // Cursor, der ein Bild anzeigt, z.B. ein Haus, das gebaut werden soll
    public boolean renderFogOfWar = false;
    public GraphicsImage renderPic;
    public int sizeX; // Mapgröße
    public int sizeY;
    public int positionX; // Position, die angezeigt wird
    public int positionY;
    public int viewX; // Die Größe des Angezeigten ausschnitts
    public int viewY;
    public int realPixX; // Echte, ganze X-Pixel
    public int realPixY; // Echte, ganze Y-Pixel
    private HashMap<String, GraphicsImage> imgMap; // Enthält alle Bilder
    public HashMap<String, GraphicsImage> coloredImgMap;
    Image renderBackground = null; // Bodentextur, nur Bild-Auflösung
    int rBvX; // Position während der letzen Bildberechnung
    int rBvY; // Position während der letzen Bildberechnung
    public boolean serverColMode = false;
    boolean useAntialising = false; // Kantenglättung, normalerweise AUS // NUR IM NORMALEN RENDERMODE
    public List<Sprite> allList;
    public boolean alwaysshowenergybars = false; // Energiebalken immer anzeigen, WC3 lässt grüßen.
    boolean displayFrameRate = false;
    Date initRun;
    public int realRuns = 0; // Für echte Framerateanzeige
    public int lastFr = 0; // Auch frameRatezeug
    Dimension framePos = new Dimension(0, 0); // Editor-Rahmen
    public int epoche = 2; // Null ist keine Epoche, also kein Gescheites Grundbild..
    private ClientCore.InnerClient rgi;
    private static final boolean beautyDraw = true;     // Schöner zeichnen durch umsortieren der allList, kostet Leistung
    public int mouseX;                                         // Die Position der Maus, muss geupdatet werden
    public int mouseY;                                         // Die Position der Maus, muss geupdatet werden
    int lastHovMouseX;
    int lastHovMouseY;
    TrueTypeFont[] fonts;                                       // Die Fonts, die häufig benötigt werden
    public boolean pauseMode = false;                          // Pause-Modus
    Color fowGray = new Color(0.0f, 0.0f, 0.0f, 0.4f);
    long pause;                                         // Zeitpunkt der letzen Pause
    public boolean saveMode = false;                    // Sicherer Grafikmodus ohne unsichere, weil zu Große Bilder
    public boolean coordMode = false;                          // Modus, der die Koordinaten der Felder mit anzeigt.
    public boolean idMode = false;                             // Modus, der die NetIds von Einheiten/Gebäuden und Ressourcen mit anzeigt.
    public int unitDestMode = 0;                               // Modus, der die Ziele von Einheiten (Bewegung & Angriff) anzeigt (Aus/Eigene/Fremde/Alle)0123
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
    public ArrayList<Overlay> overlays;
    public GraphicsFireManager fireMan;

    public void paintComponent(Graphics g) {
        //Die echte, letzendlich gültige paint-Methode, sollte nicht direkt aufgerufen werden
        if (modi == -1) {
            // Ladebildschirm (pre-Game)
            renderLoadScreen(g);
        } else if (modi == 3) {
            try {
                if (pauseMode) {
                    // Pause für diesen Frame freischalten:
                    pause = rgi.rogGraphics.getPauseTime();
                }
//                // FoW updaten?
//                if (!fowDisabled && updateBuildingsFow) {
//                    updateBuildingsFow = false;
//                    this.updateBuildingFoW();
//                }
                g.setAntiAlias(useAntialising);

                // Alles löschen
                g.setColor(Color.white);
                g.fillRect(0, 0, realPixX, realPixY);

                // Boden und Fix rendern
                renderBackground();

                // Einheiten nach ihrer Y-Enfernung sortieren, erzeugt räumlichen Eindruck.
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

                // @TODO: buildingwaypoint als groundeffect rendern
                renderBuildingWaypoint();

                // Input-Modul-Hover holen
//                if (mouseX != lastHovMouseX || mouseY != lastHovMouseY) {
//                    // Wenn sichs geändert hat neu suchen
//                  //  hoveredUnit = identifyUnit(mouseX, mouseY);
//                    lastHovMouseX = mouseX;
//                    lastHovMouseY = mouseY;
//                }

                renderGroundEffects(g);

                //@TODO: Einheitenziel als groundeffect rendern
                if (unitDestMode != 0) {
                    renderUnitDest(g);
                }

                // Jetzt alle Sprites rendern
                renderSprites(g);

                //@TODO: fireeffects als skyeffect rendern
                //fireMan.renderFireEffects(buildingList, lastDelta, positionX, positionY);


                // renderHealthBars(g);
                // Fog of War rendern, falls aktiv
//                if (renderFogOfWar) {
//                    renderFogOfWar(g);
//                }

                if (renderPicCursor) {
                    renderCursor();
                }
                /*     if (this.dragSelectionBox) {
                renderSelBox(g);
                }*/
                if (idMode) {
                    renderIds(g);
                }
                // Mouse-Hover rendern
                if (pauseMode) {
                    renderPause(g);
                }
                g.setColor(Color.darkGray);
                g.setFont(fonts[0]);
                //g3.setFont(new UnicodeFont(java.awt.Font.decode("8")));
                g.drawString("13 Ducks Entertainment's: Centuries of Rage HD (pre-alpha)", 10, realPixY - 20);
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
                            g.drawImage(realPixX >= 800 ? imgMap.get("img/game/finish_defeat_spec.png").getImage() : imgMap.get("img/game/finish_defeat_spec.png").getImage().getScaledCopy(realPixX, (int) ((1.0 * realPixX / 800) * 600)), realPixX >= 800 ? (realPixX / 2) - 400 : 0, realPixX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * realPixX / 800) * 600)) / 2);
                        } else {
                            g.setColor(Color.black);
                            g.setFont(fonts[5]);
                            g.drawString("DEFEAT", 10, realPixY - 40);
                        }
                    } else if (gameDone == 2) {
                        // DEFEATED einblenden - bis der Spieler das Spiel beendet
                        g.drawImage(realPixX >= 800 ? imgMap.get("img/game/finish_defeat_gameover.png").getImage() : imgMap.get("img/game/finish_defeat_gameover.png").getImage().getScaledCopy(realPixX, (int) ((1.0 * realPixX / 800) * 600)), realPixX >= 800 ? (realPixX / 2) - 400 : 0, realPixX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * realPixX / 800) * 600)) / 2);
                    } else if (gameDone == 1) {
                        // VICTORY
                        g.drawImage(realPixX >= 800 ? imgMap.get("img/game/finish_victory_gameover.png").getImage() : imgMap.get("img/game/finish_victory_gameover.png").getImage().getScaledCopy(realPixX, (int) ((1.0 * realPixX / 800) * 600)), realPixX >= 800 ? (realPixX / 2) - 400 : 0, realPixX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * realPixX / 800) * 600)) / 2);
                    }
                }

                // Epischer ZoomEffekt?
                if (zoomInGame) {
                    renderLoadScreen(g);
                }

            } catch (Exception ex) {
                System.out.println("Error while rendering frame, dropping.");
                ex.printStackTrace();
            }
            // Pause zurücksetzten
            pause = 0;
        }
    }

    /**
     * Zeichnet alle Sprites auf den Bildschirm, falls diese derzeit im sichtbaren Bereich liegen,
     * gemäß dem FOW gezeichnet werden sollen und sich nicht verstecken.
     * @param g
     */
    private void renderSprites(Graphics g) {
        for (int i = 0; i < allList.size(); i++) {
            Sprite sprite = allList.get(i);
            if (spriteInSight(sprite)) {
                //@TODO: FOW-Behandlung einbauen
                if (sprite.renderInNullFog()) {
                    Position mainPos = sprite.getMainPositionForRenderOrigin();
                    sprite.renderSprite(g, (mainPos.getX() - positionX) * 10, (int) ((mainPos.getY() - positionY) * 7.5), imgMap, rgi.game.getPlayer(sprite.getColorId()).color);
                }
            }
        }
    }

    /**
     * Zeichnet alle Groundeffects aller Sprites auf den Bildschirm, falls diese derzeit im sichtbaren Bereich liegen,
     * gemäß dem FOW gezeichnet werden sollen und sich nicht verstecken.
     * @param g
     */
    private void renderGroundEffects(Graphics g) {
        for (int i = 0; i < allList.size(); i++) {
            Sprite sprite = allList.get(i);
            if (spriteInSight(sprite)) {
                //@TODO: FOW-Behandlung einbauen
                if (sprite.renderInNullFog()) {
                    Position mainPos = sprite.getMainPositionForRenderOrigin();
                    sprite.renderGroundEffect(g, (mainPos.getX() - positionX) * 10, (int) ((mainPos.getY() - positionY) * 7.5), imgMap, rgi.game.getPlayer(sprite.getColorId()).color);
                }
            }
        }
    }

    /**
     * Findet heraus, ob ein Sprite gerade im sichtbaren Bereich liegt.
     * Reine Scroll-Überprüfung, macht nichts mit FOW oder verstecken.
     * @param s
     * @return
     */
    private boolean spriteInSight(Sprite s) {
        Position[] visPos = s.getVisisbilityPositions();
        for (Position pos : visPos) {
            if (positionInSight(pos)) {
                // Wenn irgendwas sichtbar ist, dann true
                return true;
            }
        }
        // Kein einziges Feld im sichtbaren --> false
        return false;
    }

    /**
     * Findet heraus, ob eine Position (ein Feld) derzeit im sichtbaren Bereich liegt.
     * Reine Scroll-Überprüfung, macht nichts mit FOW.
     * @param s
     * @return
     */
    private boolean positionInSight(Position s) {
        if (s.getX() >= positionX && s.getY() < positionX + viewX) {
            if (s.getY() >= positionY && s.getY() < positionY + viewY) {
                return true;
            }
        }
        return false;
    }

    /**
     * Zeichnet alle Health-Bars.
     * @param g2
     */
    /*    private void renderHealthBars(Graphics g2) {
    for (int i = 0; i < buildingList.size(); i++) {
    Building b = buildingList.get(i);
    if (b.isSelected || (this.alwaysshowenergybars && b.wasSeen)) {
    this.renderHealth(b, g2, (b.position.getX() - positionX) * 20, (b.position.getY() - positionY) * 15);
    }
    }
    for (int i = 0; i < unitList.size(); i++) {
    Unit u = unitList.get(i);
    if (u.isSelected || (this.alwaysshowenergybars && u.alive)) {
    // Ist diese Einheit gerade sichtbar? (nur eigene oder sichtbare zeichnen)
    if (u.playerId == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(u, rgi.game.getOwnPlayer()) || fowmap[u.position.getX()][u.position.getY()] > 1) {
    Position tempP = null;
    if (u.isMoving()) {
    tempP = u.getMovingPosition(rgi, positionX, positionY);
    } else {
    tempP = new Position((u.position.getX() - positionX) * 20, (u.position.getY() - positionY) * 15);
    }
    renderHealth(u, g2, tempP.getX(), tempP.getY());
    }
    }
    }
    } */
    private void renderOverlays(Graphics g2) {
        // Zeichnet alle Overlays.
        for (int i = 0; i < overlays.size(); i++) {
            try {
                overlays.get(i).renderOverlay(g2, realPixX, realPixY);
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
//        int mode = unitDestMode;
//        g2.setLineWidth(2);
//        for (int x = 0; x < unitList.size(); x++) {
//            Unit unit = unitList.get(x);
//            // Modus abfragen:
//            if (mode == 3 || (mode == 1 && unit.getPlayerId() == rgi.game.getOwnPlayer().getPlayerId()) || (mode == 2 && unit.getPlayerId() != rgi.game.getOwnPlayer().getPlayerId())) {
//                // Zuerst Bewegung
//                if (unit.movingtarget != null) {
//                    // Markieren, ob Einheit flieht oder nicht
//                    if (unit.getbehaviourC(4).active) {
//                        // Angry, roter Punkt
//                        g2.setColor(Color.red);
//                    } else {
//                        g2.setColor(Color.blue);
//                    }
//                    // Source (unit selber)
//                    Position pos = unit.getMovingPosition(rgi, positionX, positionY);
//                    int sx = pos.getX();
//                    int sy = pos.getY();
//                    g2.fillRect(sx, sy, 5, 5);
//                    // Target (ziel)
//                    // Diese 2. Abfrage ist sinnvoll und notwendig, weil getMovingPosition am Ende der Bewegung das movingtarget löscht
//                    if (unit.movingtarget != null) {
//                        int tx = (unit.movingtarget.getX() - positionX) * 20;
//                        int ty = (unit.movingtarget.getY() - positionY) * 15;
//                        // Zeichnen
//                        g2.setColor(new Color(3, 100, 0)); // Dunkelgrün
//                        g2.drawLine(sx + 19, sy + 25, tx + 19, ty + 25);
//                    }
//                }
//                // Jetzt Angriff
//                if (unit.attacktarget != null) {
//                    g2.setColor(Color.red);
//                    // Source (unit selber)
//                    int sx = (unit.position.getX() - positionX) * 20;
//                    int sy = (unit.position.getY() - positionY) * 15;
//                    if (unit.isMoving()) {
//                        Position pos = unit.getMovingPosition(rgi, positionX, positionY);
//                        sx = pos.getX();
//                        sy = pos.getY();
//                    }
//                    // Target (ziel)
//                    int tx = 0;
//                    int ty = 0;
//                    if (unit.attacktarget.getClass().equals(Unit.class)) {
//                        Unit target = (Unit) unit.attacktarget;
//                        if (target.isMoving()) {
//                            Position pos = target.getMovingPosition(rgi, positionX, positionY);
//                            tx = pos.getX();
//                            ty = pos.getY();
//                        } else {
//                            tx = (target.position.getX() - positionX) * 20;
//                            ty = (target.position.getY() - positionY) * 15;
//                        }
//                    } else if (unit.attacktarget.getClass().equals(Building.class)) {
//                        Building target = (Building) unit.attacktarget;
//                        //Gebäude-Mitte finden:
//                        float bx = 0;
//                        float by = 0;
//                        //Z1
//                        //Einfach die Hälfte als Mitte nehmen
//                        bx = target.position.getX() + ((target.getZ1() - 1) * 1.0f / 2);
//                        by = target.position.getY() - ((target.getZ1() - 1) * 1.0f / 2);
//                        //Z2
//                        // Einfach die Hälfte als Mitte nehmen
//                        bx += ((target.getZ2() - 1) * 1.0f / 2);
//                        by += ((target.getZ2() - 1) * 1.0f / 2);
//                        // Gebäude-Mitte gefunden
//                        tx = (int) ((bx - positionX) * 20) - 19;
//                        ty = (int) ((by - positionY) * 15) - 25;
//                    }
//                    // Zeichnen
//                    g2.drawLine(sx + 19, sy + 25, tx + 19, ty + 25);
//                }
//            }
//        }
//        g2.setLineWidth(1);
    }

    /**
     * Zeichnet NetIds von allen Einheiten / Gebäuden / Ressourcen
     * Zum Debugen. Funktioniert nur, wenn die Debug-Tools (cheats) aktiviert sind
     * @param g2
     */
    private void renderIds(Graphics g2) {
//        g2.setColor(Color.cyan);
//        g2.setFont(fonts[4]);
//        // Einheiten durchgehen
//        for (int i = 0; i < unitList.size(); i++) {
//            Unit unit = unitList.get(i);
//            if (unit.isMoving()) {
//                Position pos = unit.getMovingPosition(rgi, positionX, positionY);
//                g2.drawString(String.valueOf(unit.netID), pos.getX() + 10, pos.getY() + 16);
//            } else {
//                g2.drawString(String.valueOf(unit.netID), (unit.position.getX() - positionX) * 20 + 10, (unit.position.getY() - positionY) * 15 + 16);
//            }
//        }
//        // Gebäude
//        for (int i = 0; i < buildingList.size(); i++) {
//            Building building = buildingList.get(i);
//            g2.drawString(String.valueOf(building.netID), (building.position.getX() - positionX) * 20 + 10, (building.position.getY() - positionY) * 15 + 16);
//        }
//        // Ressourcen
//        for (int i = 0; i < resList.size(); i++) {
//            Ressource res = resList.get(i);
//            g2.drawString(String.valueOf(res.netID), (res.position.getX() - positionX) * 20 + 10, (res.position.getY() - positionY) * 15 + 16);
//        }
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
//        // Gebäude-Sammelpunkt zeichnen
//        if (!rgi.rogGraphics.inputM.selected.isEmpty() && rgi.rogGraphics.inputM.selected.get(0).getClass().equals(Building.class)) {
//            Building b = (Building) rgi.rogGraphics.inputM.selected.get(0);
//            if (b.waypoint != null) {
//                // Dahin rendern
//                GraphicsImage image = coloredImgMap.get("img/game/building_defaulttarget.png" + b.getPlayerId());
//                if (image != null) {
//                    image.getImage().draw(((b.waypoint.getX() - positionX) * 20) - 40, ((b.waypoint.getY() - positionY) * 15) - 40);
//                } else {
//                    System.out.println("ERROR: Textures missing (34124)");
//                }
//            }
//        }
    }

    private void renderFogOfWar(Graphics g) {
//        // Rendert den Fog of War. Liest ihn dazu einfach aus der fowmap aus.
//        // Das Festlegen der fog-Map wird anderswo erledigt...
//        // Murks, aber die position müssen gerade sein...
//        if (positionX % 2 == 1) {
//            positionX--;
//        }
//        if (positionY % 2 == 1) {
//            positionY--;
//        }
//        for (int x = 0; x < (sizeX) && x < (viewX); x = x + 2) {
//            for (int y = 0; y < (sizeY) && y < (viewY + 2); y = y + 2) {
//                // Ist hier Schatten?
//                try {
//                    byte fow = fowmap[x + positionX][y + positionY - 2];
//                    if (fow < 2) {
//                        if (fow == 0) {
//                            g.setColor(Color.black);
//                        } else {
//                            g.setColor(fowGray);
//                        }
//                        //g.fill(fowShape);
//                        g.fillRect(x * 20, (y - 2) * 15 + 10, 40, 30);
//                    }
//                } catch (ArrayIndexOutOfBoundsException ar) {
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//        for (int x = 1; x < (sizeX) && x < (viewX + 4); x = x + 2) {
//            for (int y = 1; y < (sizeY) && y < (viewY + 2); y = y + 2) {
//                // Ist hier Schatten?
//                try {
//                    byte fow = fowmap[x + positionX - 2][y + positionY - 2];
//                    if (fow < 2) {
//                        if (fow == 0) {
//                            g.setColor(Color.black);
//                        } else {
//                            g.setColor(fowGray);
//                        }
//                        g.fillRect((x - 2) * 20, (y - 2) * 15 + 10, 40, 30);
//                    }
//                } catch (ArrayIndexOutOfBoundsException ar) {
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
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

    private void renderSelBox(Graphics g2) {
//        // Rendert die SelektionBox
//        g2.setColor(Color.lightGray);
//        int dirX = mouseX - this.boxselectionstart.width;
//        int dirY = mouseY - this.boxselectionstart.height;
//        // Bpxen können nur von links oben nach rechts unten gezogen werden - eventuell Koordinaten tauschen
//        if (dirX < 0 && dirY > 0) {
//            // Nur x Tauschen
//            g2.drawRect(mouseX, this.boxselectionstart.height, this.boxselectionstart.width - mouseX, mouseY - this.boxselectionstart.height);
//        } else if (dirY < 0 && dirX > 0) {
//            // Nur Y tauschen
//            g2.drawRect(this.boxselectionstart.width, mouseY, mouseX - this.boxselectionstart.width, this.boxselectionstart.height - mouseY);
//        } else if (dirX < 0 && dirY < 0) {
//            // Beide tauschen
//            g2.drawRect(mouseX, mouseY, this.boxselectionstart.width - mouseX, this.boxselectionstart.height - mouseY);
//        }
//        // Nichts tauschen
//        g2.drawRect(this.boxselectionstart.width, this.boxselectionstart.height, dirX, dirY);
    }

    private void renderCursor() {
        if (renderPicCursor) {
            // Pic-Cursor
            // Einfach Bild an die gerasterte Cursorposition rendern.
            renderPic.getImage().draw(framePos.width * 10, (int) (framePos.height * 7.5));
        }
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
                    renderBackground = new Image(realPixX, realPixY);
                }
                Graphics g3 = renderBackground.getGraphics();
                rBvX = positionX;
                rBvY = positionY;
                g3.setColor(Color.white);
                g3.fillRect(0, 0, renderBackground.getWidth(), renderBackground.getHeight());

                for (int x = -2; x < sizeX && x < (viewX + 1); x += 1) {
                    for (int y = -2; y < sizeY && y < (viewY + 1); y += 1) {
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
                                g3.drawImage(tempImage.getImage(), x * 10, (int) (y * 7.5));
                            } else {
                                System.out.println("[RME][ERROR]: Image \"" + ground + "\" not found!");
                            }
                            if (fix != null) {
                                // Bild suchen und einfügen
                                GraphicsImage fixImage = imgMap.get(fix);

                                if (fixImage != null) {
                                    g3.drawImage(fixImage.getImage(), x * 10, (int) (y * 7.5));
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
                        colModeImage.getImage().draw(x * 10, (int) (y * 7.5));
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
                        colModeImage.getImage().draw(x * 10, (int) (y * 7.5));
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
                        imgMap.get("img/game/highlight_blue.png").getImage().draw(x * 10, (int) (y * 7.5));
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
                        imgMap.get("img/game/highlight_blue.png").getImage().draw(x * 10, (int) (y * 7.5));
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
        for (int x = 0; x < sizeX && x < viewX; x = x + 4) {
            for (int y = 0; y < sizeY && y < viewY; y = y + 2) {
                g2.drawString((x + positionX) + "|" + (y + positionY), x * 10 + 5, (int) (y * 7.5) + 10);
            }
        }
        for (int x = 0 + 1; x < sizeX && x < viewX; x = x + 4) {
            for (int y = 0 + 1; y < sizeY && y < viewY; y = y + 2) {
                g2.drawString((x + positionX) + "|" + (y + positionY), x * 10 + 5, (int) (y * 7.5) + 10);
            }
        }
    }

    private void renderHealth(GameObject rO, Graphics g2, int dX, int dY) {
//        try {
//            Unit rU = (Unit) rO;
//            // Billigen Balken rendern
//            g2.setColor(Color.black);
//            if (rU.selectionShadow == 1) {
//                g2.fillRect(dX + 9, dY - 1, 7, 7);
//            }
//            // Farbe bestimmen
//            double percent = 1.0 * rU.getHitpoints() / rU.getMaxhitpoints();
//            if (percent >= 0.3) {
//                g2.setColor(new Color((int) (255 - (((percent - 0.5) * 2) * 255)), 255, 0));
//            } else {
//                g2.setColor(new Color(255, (int) ((percent * 2) * 255), 0));
//            }
//            g2.fillRect(dX + 10, dY, 5, 5);
//        } catch (ClassCastException ex) {
//            // Gebäude
//            try {
//                Building rB = (Building) rO;
//                // Balken soll über der Mitte des Gebäudes schweben
//                // Längenfaktor finden
//                int lf = (rB.getZ1() + rB.getZ2()) * 20;
//                // dX / dY ist der Zeichenursprung
//                // X-Mitte finden (Durchschnitt aus z1 und z2)
//                int cpX = lf / 2 - lf / 4;
//                // Die Anzahl Y-Pixel nur von z1 ab
//                int cpY = rB.getZ1() * 15 + 10;
//                // Billigen Balken rendern
//                g2.setColor(Color.black);
//                g2.fillRect(dX + cpX, dY - cpY, (lf / 2) + 2, 5);
//                // Farbe bestimmen
//                double percent = 1.0 * rB.getHitpoints() / rB.getMaxhitpoints();
//                if (percent >= 0.5) {
//                    g2.setColor(new Color((int) (255 - (((percent - 0.5) * 2) * 255)), 255, 0));
//                }
//                if (percent < 0.5) {
//                    g2.setColor(new Color(255, (int) ((percent * 2) * 255), 0));
//                }
//                percent *= 100;
//                // Entsprechend viel füllen
//                int fillperc = (int) (percent * (lf / 2) / 100);
//                g2.fillRect(dX + cpX + 1, dY - cpY + 1, fillperc, 3);
//            } catch (ClassCastException ex2) {
//            }
//
//        }
    }

    public void setFramePosition(Dimension td) {
        // Setzt die Position des Rahmens, inzwischen Editor&Game
        framePos = td;
        if (modi != 3) {
            repaint();
        }
    }

    public void setVisMap(CoRMapElement[][] newVisMap, int X, int Y) {
        // Einfach einsetzen
        visMap = newVisMap;
        sizeX = X;
        sizeY = Y;

        fowmap = new byte[visMap.length][visMap[0].length];
        // Durchlaufen, alles auf 0 (unerforscht) setzen
        for (int x = 0; x < fowmap.length; x++) {
            for (int y = 0; y < fowmap[0].length; y++) {
                fowmap[x][y] = 0;
            }
        }
    }

    public void setImageMap(HashMap<String, GraphicsImage> newMap) {
        // Die Bilder, die verfügbar sind
        imgMap = newMap;
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
        viewX = vX;
        viewY = vY;
        if (modi != 3) { // Im echten Rendern refreshed die Mainloop
            repaint();
        }

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

    public Position translateCoordinatesToField(int x, int y) {
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
        return new Position(coordX, coordY);
    }

    /*   public void klickedOnMiniMap(final int button, final int x, final int y, final int clickCount) {
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
    } */
    public int getModi() {
        // liefert den aktuellen renderModus zurück
        return modi;
    }

    /*  public Dimension searchMiniMid(int cX, int cY) {
    // Sucht die Koordinaten eines Klicks auf die Minimap, also die Koordinaten des Feldes in der Mitte der Scollbox
    // Input muss auf Minimap gefiltert sein, sonst kommt nur Müll raus
    return new Dimension((int) (((cX - (hudX + hudSizeX * 0.1)) / (hudSizeX * 0.8)) * sizeX), (int) (((cY - realPixY / 7 * 1.4) / (realPixY / 7 * 2 * 0.8)) * sizeY));
    }*/
    public void startDebugMode() {
        modi = 4;
        repaint();

    }

    public void calcColoredMaps(Color[] colors) {
        // Berechnet die eingefärbten Texturen
        ArrayList<GraphicsImage> tList = new ArrayList<GraphicsImage>();
        tList.addAll(coloredImgMap.values());
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
                imgMap.put(newImg.getImageName() + playerId, newImg);
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

    /**
     * Die Updates des FoW dürfen nicht während der Bildberechnung sein, sonst flackert es.
     */
    private void updateBuildingFoW() {
//        clearFogOfWar();
//        for (int i = 0; i < buildingList.size(); i++) {
//            Building b = buildingList.get(i);
//            // FoW berechnen
//            if (b.getPlayerId() == rgi.game.getOwnPlayer().playerId || rgi.game.shareSight(b, rgi.game.getOwnPlayer())) {
//                cutSight(b);
//            }
//        }
    }

    public void buildingsChanged() {
        // FoW updaten
        updateBuildingsFow = true;
    }

    private void cutSight(Building b) {
        // Mitte berechnen
        int x = (int) (b.getMainPosition().getX() + ((b.getZ1() + b.getZ2() - 2) * 1.0 / 2));
        int y = b.getMainPosition().getY();
        // Diese Position als Startfeld überhaupt zulässig?
        if (((int) x + (int) y) % 2 == 1) {
            y++;
        }
        // Dieses Feld selber auch ausschneiden
        fowmap[x][y] = 2;
        cutCircleFast(b.getVisrange() + ((b.getZ1() + b.getZ2()) / 4), new Position(x, y), true);
    }

    /**
     * Setzt den Fow-Sichtlayer von sterbenden Einheitn auf Unit, damit die Gegend beim nächsten Update auf erkundet gesetzt werden kann.
     * @param b
     */
    public void cutDieingBuildingSight(Building b) {
//        // Mitte berechnen
//        int x = (int) (b.getMainPosition().getX() + ((b.getZ1() + b.getZ2() - 2) * 1.0 / 2));
//        int y = b.getMainPosition().getY();
//        // Diese Position als Startfeld überhaupt zulässig?
//        if (((int) x + (int) y) % 2 == 1) {
//            y++;
//        }
//        // Dieses Feld selber auch ausschneiden
//        fowmap[x][y] = 3;
//        // Schablone holen
//        boolean[][] pattern = fowpatmgr.getPattern(b.getVisrange() + ((b.getZ1() + b.getZ2()) / 4));
//        // Schablone anwenden
//        int sx = x - 40;
//        int sy = y - 40;
//        for (int x2 = 0; x2 < 80; x2++) {
//            for (int y2 = 0; y2 < 80; y2++) {
//                if (pattern[x2][y2]) {
//                    try {
//                        fowmap[sx + x2][sy + y2] = 3;
//                    } catch (ArrayIndexOutOfBoundsException ex) {
//                        // Nix tun, ein Kreis kann ja den Maprand schneiden, da gibts natürlich dann kein FoW-Raster...
//                    }
//                }
//            }
//        }
    }

    private void cutSight(Unit unit) {
        // Mitte berechnen
        int x = unit.getMainPosition().getX();
        int y = unit.getMainPosition().getY();
        // Dieses Feld selber auch ausschneiden
        byte val = fowmap[x][y];
        if (val == 0 || val == 1) {
            fowmap[x][y] = 3;
        }
        cutCircleFast(unit.getVisrange(), unit.getMainPosition(), false);
    }

    /**
     * Schneidet schnell Kreise in den FoW.
     * Funktioniert NUR mit Ranges von 1-20!!!, weil schablonenbasiert
     * @param range Sichtweite in ganzen Feldern (1-20)
     * @param origin Mitte
     * @param building Für Gebäude (true) oder Einheit (false)
     */
    public void cutCircleFast(int range, Position origin, boolean building) {
//        // Schablone holen
//        boolean[][] pattern = fowpatmgr.getPattern(range);
//        // Schablone anwenden
//        int sx = origin.getX() - 40;
//        int sy = origin.getY() - 40;
//        for (int x = 0; x < 80; x++) {
//            for (int y = 0; y < 80; y++) {
//                if (pattern[x][y]) {
//                    directCut(sx + x, sy + y, !building);
//                }
//            }
//        }
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
//        // Zuerst alles mal von Einheiten gesehene vergessen
//        for (int x = 0; x < fowmap.length; x++) {
//            for (int y = 0; y < fowmap[0].length; y++) {
//                byte v = fowmap[x][y];
//                if (fowmap[x][y] == 3) {
//                    fowmap[x][y] = 1;
//                }
//
//            }
//        }
//        // Für alle Einheiten auf der Map
//        int ownPlayerId = rgi.game.getOwnPlayer().playerId;
//        for (int i = 0; i < unitList.size(); i++) {
//            Unit unit = unitList.get(i);
//            if (unit.getPlayerId() != ownPlayerId && !rgi.game.shareSight(unit, rgi.game.getOwnPlayer())) {
//                // Nur eigene Einheiten decken den Nebel des Krieges auf
//                continue;
//            }
//            cutSight(unit);
//        }
    }

    public void appendCoreInner(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    public GraphicsContent() {
        super("Centuries of Rage HD (pre-Alpha)");
        coloredImgMap = new HashMap<String, GraphicsImage>();
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
        if (initRun != null) {
            // Update Input-System
            parent.inputM.updateIGEs();
        }
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

        if (buildingsChanged) {
            this.buildingsChanged();
            buildingsChanged = false;
        }
        if (epocheChanged) {
            epocheChanged = false;
        }
        //parent.slickReady = true;
    }

    //@Override
    @Override
    public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
    }

    //@Override
    @Override
    public void inputStarted() {
    }
}
