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
package de._13ducks.cor.graphics;

import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.GameObject;
import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import org.lwjgl.opengl.Display;

import org.newdawn.slick.*;
import org.newdawn.slick.opengl.renderer.Renderer;
import de._13ducks.cor.map.AbstractMapElement;
import de._13ducks.cor.game.Position;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.Server;
import de._13ducks.cor.game.server.movement.FreePolygon;
import de._13ducks.cor.game.server.movement.Node;
import de._13ducks.cor.graphics.effects.SkyEffect;
import org.newdawn.slick.geom.Polygon;

@SuppressWarnings("CallToThreadDumpStack")
public class GraphicsContent extends BasicGame {

    /**
     * Die Größe eines Feldes Richtung X.
     * Für Zeichenkoordinatenbestimmungen durch multiplizieren wird aber die Halbe benötigt!
     */
    public static final int FIELD_SIZE_X = 20;
    /**
     * Die Größe eines Feldes Richtung Y.
     * Für Zeichenkoordinatenbestimmungen durch multiplizieren wird aber die Halbe benötigt!
     */
    public static final int FIELD_SIZE_Y = 15;
    /**
     * Die halbe Größe eines Feldes in X-Richtung
     */
    public static final int FIELD_HALF_X = FIELD_SIZE_X / 2;
    /**
     * Die halbe Größe eines Feldes in Y-Richtung
     */
    public static final double FIELD_HALF_Y = FIELD_SIZE_Y / 2.0;
    /**
     * Wie viele Pixel das tatsächlich gezeichnete Feld von den Zeichenkoordinaten entfernt ist.
     */
    public static final int BASIC_FIELD_OFFSET_X = 5;
    /**
     * Wie viele Pixel das tatsächlich gezeichnete Feld von den Zeichenkoordinaten entfernt ist.
     */
    public static final double BASIC_FIELD_OFFSET_Y = 17.5;
    /**
     * Wie viele Pixel der Zeichenursprung für ein 1x1 Feld von dem Ursprung des Zuordungsfeldes entfernt ist.
     */
    public static final int OFFSET_1x1_X = 3;
    /**
     * Wie viele Pixel der Zeichenursprung für ein 1x1 Feld von dem Ursprung des Zuordungsfeldes entfernt ist.
     */
    public static final int OFFSET_1x1_Y = 0;
    /**
     * Wie viele Pixel der Zeichenursprung für ein 2x2 Feld von dem Rohkoordinatenfeld entfernt ist.
     */
    public static final int OFFSET_2x2_X = -30;
    /**
     * Wie viele Pixel der Zeichenursprung für ein 2x2 Feld von dem Rohkoordinatenfeld entfernt ist.
     */
    public static final int OFFSET_2x2_Y = -35;
    /**
     * Wie viele Pixel der Zeichenursprung für ein 2x2 Feld von dem Rohkoordinatenfeld entfernt ist.
     */
    public static final int OFFSET_3x3_X = -40;
    /**
     * Wie viele Pixel der Zeichenursprung für ein 2x2 Feld von dem Rohkoordinatenfeld entfernt ist.
     */
    public static final int OFFSET_3x3_Y = -54;
    /**
     * Diesen Abstand muss man bei allen Zeichenoperationen reinrechnen, die mit präzisen Pixeln arbeiten.
     */
    public static final int OFFSET_PRECISE_X = 10;
    /**
     * Diesen Abstand muss man bei allen Zeichenoperationen reinrechnen, die mit präzisen Pixeln arbeiten.
     */
    public static final int OFFSET_PRECISE_Y = 15;
    // Diese Klasse repräsentiert den Tatsächlichen GrafikINHALT von RogGraphics und RogMapEditor
    public GraphicsImage colModeImage;
    public int modi = -2; // Was gerendert werden soll, spezielle Ansichten für den Editor etc...
    AbstractMapElement[][] visMap; // Die angezeigte Map
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
    public static boolean alwaysshowenergybars = false; // Energiebalken immer anzeigen, WC3 lässt grüßen.
    Date initRun;
    Dimension framePos = new Dimension(0, 0); // Editor-Rahmen
    public int epoche = 2; // Null ist keine Epoche, also kein Gescheites Grundbild..
    private ClientCore.InnerClient rgi;
    private static final boolean beautyDraw = true;     // Schöner zeichnen durch umsortieren der allList, kostet Leistung
    public int mouseX;                                         // Die Position der Maus, muss geupdatet werden
    public int mouseY;                                         // Die Position der Maus, muss geupdatet werden
    int lastHovMouseX;
    int lastHovMouseY;
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
    public Image duckslogo;
    public int imgLoadCount = 0;
    public int imgLoadTotal = 0;
    CoreGraphics parent;
    public int initState = -1;                          // Damit alles Slick laden kann, sonst weigert es sich zu arbeiten :(
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
    public ArrayList<SkyEffect> skyEffects;
    public GraphicsFireManager fireMan;
    private Minimap minimap;
    private IngameMenu ingamemenu;

    public void paintComponent(Graphics g) {
        //Die echte, letzendlich gültige paint-Methode, sollte nicht direkt aufgerufen werden
        if (modi == -2) {
            // 13Ducks-Logo
            //g.setColor(Color.green);
            //g.fillRect(10, 10, 10, 10);
            g.setBackground(Color.white);
            g.clear();
            duckslogo.drawCentered(realPixX / 2, realPixY / 2);
            // Sobald es einmal gezeichnet wurde können wir weiter laden
            initState = 12;
            modi = 0;
        } else if (modi == -1) {
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

                renderSpriteGroundEffects(g);

                //@TODO: Einheitenziel als groundeffect rendern
                if (unitDestMode != 0) {
                    renderUnitDest(g);
                }

                // Jetzt alle Sprites rendern
                renderSprites(g);

                //@TODO: fireeffects als skyeffect rendern
                //fireMan.renderFireEffects(buildingList, lastDelta, positionX, positionY);


                renderSpriteSkyEffects(g);
                
                
                // Fog of War rendern, falls aktiv
//                if (renderFogOfWar) {
//                    renderFogOfWar(g);
//                }
                
                renderSkyEffects(g);

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
                g.setFont(FontManager.getFont0());
                //g3.setFont(new UnicodeFont(java.awt.Font.decode("8")));
                g.drawString("13 Ducks Entertainment's: Centuries of Rage HD (pre-alpha)", 10, 2);
                if (serverColMode) {
                    this.renderServerCol(g);
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
                            g.drawImage(realPixX >= 800 ? getImgMap().get("img/game/finish_defeat_spec.png").getImage() : getImgMap().get("img/game/finish_defeat_spec.png").getImage().getScaledCopy(realPixX, (int) ((1.0 * realPixX / 800) * 600)), realPixX >= 800 ? (realPixX / 2) - 400 : 0, realPixX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * realPixX / 800) * 600)) / 2);
                        } else {
                            g.setColor(Color.black);
                            g.setFont(FontManager.getFont0());
                            g.drawString("DEFEAT", 10, realPixY - 40);
                        }
                    } else if (gameDone == 2) {
                        // DEFEATED einblenden - bis der Spieler das Spiel beendet
                        g.drawImage(realPixX >= 800 ? getImgMap().get("img/game/finish_defeat_gameover.png").getImage() : getImgMap().get("img/game/finish_defeat_gameover.png").getImage().getScaledCopy(realPixX, (int) ((1.0 * realPixX / 800) * 600)), realPixX >= 800 ? (realPixX / 2) - 400 : 0, realPixX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * realPixX / 800) * 600)) / 2);
                    } else if (gameDone == 1) {
                        // VICTORY
                        g.drawImage(realPixX >= 800 ? getImgMap().get("img/game/finish_victory_gameover.png").getImage() : getImgMap().get("img/game/finish_victory_gameover.png").getImage().getScaledCopy(realPixX, (int) ((1.0 * realPixX / 800) * 600)), realPixX >= 800 ? (realPixX / 2) - 400 : 0, realPixX >= 800 ? (realPixY / 2) - 300 : (realPixY / 2) - ((int) ((1.0 * realPixX / 800) * 600)) / 2);
                    }
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
     * Zeichnet alle SkyEffects, die derzeit sichtbar sind.
     * Schmeißt SkyEffects raus, die fertig sind.
     * @param g der Grafikkontext
     */
    private void renderSkyEffects(Graphics g) {
        for (int i = 0; i < skyEffects.size(); i++) {
            SkyEffect sky = skyEffects.get(i);
            if (sky.isDone()) {
                // Löschen
                skyEffects.remove(i);
                i--;
                continue;
            }
            if (sky.isVisible(positionX * FIELD_HALF_X, positionY * FIELD_HALF_Y, realPixX, realPixY)) {
                sky.renderSkyEffect(g, positionX * FIELD_HALF_X, positionY * FIELD_HALF_Y, imgMap);
            }
        }
    }

    /**
     * Zeichnet alle Sprites auf den Bildschirm, falls diese derzeit im sichtbaren Bereich liegen,
     * gemäß dem FOW gezeichnet werden sollen und sich nicht verstecken.
     * @param g
     */
    private void renderSprites(Graphics g) {
        // Sprites sortieren
        //@TODO: Hier kann eine CucurrentModificationException auftreten.
        Collections.sort(allList, new Comparator<Sprite>() {

            @Override
            public int compare(Sprite o1, Sprite o2) {
                return o1.getSortPosition().getY() - o2.getSortPosition().getY();
            }
        });
        for (int i = 0; i < allList.size(); i++) {
            Sprite sprite = allList.get(i);
            if (spriteInSight(sprite)) {
                //@TODO: FOW-Behandlung einbauen
                if (sprite.renderInNullFog()) {
                    Position mainPos = sprite.getMainPositionForRenderOrigin();
                    sprite.renderSprite(g, (mainPos.getX() - positionX) * FIELD_HALF_X, (int) ((mainPos.getY() - positionY) * FIELD_HALF_Y), positionX * FIELD_HALF_X, positionY * FIELD_HALF_Y, getImgMap(), rgi.game.getPlayer(sprite.getColorId()).color);
                }
            }
        }
    }

    /**
     * Zeichnet alle Groundeffects aller Sprites auf den Bildschirm, falls diese derzeit im sichtbaren Bereich liegen,
     * gemäß dem FOW gezeichnet werden sollen und sich nicht verstecken.
     * @param g
     */
    private void renderSpriteGroundEffects(Graphics g) {
        for (int i = 0; i < allList.size(); i++) {
            Sprite sprite = allList.get(i);
            if (spriteInSight(sprite)) {
                //@TODO: FOW-Behandlung einbauen
                if (sprite.renderInNullFog()) {
                    Position mainPos = sprite.getMainPositionForRenderOrigin();
                    sprite.renderGroundEffect(g, (mainPos.getX() - positionX) * FIELD_HALF_X, (int) ((mainPos.getY() - positionY) * FIELD_HALF_Y), positionX * FIELD_HALF_X, positionY * FIELD_HALF_Y, getImgMap(), rgi.game.getPlayer(sprite.getColorId()).color);
                }
            }
        }
    }
    
    /**
     * Zeichnet alle SkyEffects aller Sprites auf den Bildschirm, falls diese derzeit im sichtbaren Bereich liegen,
     * gemäß dem FOW gezeichnet werden sollen und sich nicht verstecken.
     * @param g
     */
    private void renderSpriteSkyEffects(Graphics g) {
        for (int i = 0; i < allList.size(); i++) {
            Sprite sprite = allList.get(i);
            if (spriteInSight(sprite)) {
                //@TODO: FOW-Behandlung einbauen
                if (sprite.renderInNullFog()) {
                    Position mainPos = sprite.getMainPositionForRenderOrigin();
                    sprite.renderSkyEffect(g, (mainPos.getX() - positionX) * FIELD_HALF_X, (int) ((mainPos.getY() - positionY) * FIELD_HALF_Y), positionX * FIELD_HALF_X, positionY * FIELD_HALF_Y, getImgMap(), rgi.game.getPlayer(sprite.getColorId()).color);
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
        if (s.getX() >= positionX && s.getX() < positionX + viewX) {
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
                overlays.get(i).renderOverlay(g2, realPixX, realPixY, getImgMap());
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

        // Ladebalken zeigen
        int lx = realPixX / 4;      // Startposition des Balkens
        int ly = realPixY / 50;
        int dx = realPixX / 2;      // Länge des Balkens
        int dy = realPixY / 32;
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
        g2.setFont(FontManager.getFont0());
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
            g2.setFont(FontManager.getFont0());
            g2.setAntiAlias(false);
            g2.drawString("ERROR! - SORRY!", ex + (edx / 2) - (FontManager.getFont0().getWidth("ERROR! - SORRY!") / 2), (float) (ey * 1.05));
            g2.drawLine(ex + 2, (float) (ey + (edy * 0.9)), ex + edx - 2, (float) (ey + (edy * 0.9)));
            g2.drawString("Click here or press ENTER to quit", ex + (edx / 2) - (FontManager.getFont0().getWidth("Click here or press ENTER to quit") / 2), (float) (ey + (edy * 0.95) - 6));
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
        g2.setFont(FontManager.getFont0());
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
            renderPic.getImage().draw(framePos.width * FIELD_HALF_X, (int) (framePos.height * FIELD_HALF_Y));
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

                for (int x = 0; x < sizeX && x < viewX; x += 1) {
                    for (int y = 0; y < sizeY && y < viewY; y += 1) {
                        if ((x + y) % 2 == 1) {
                            continue;
                        }
                        // X und Y durchlaufen, wenn ein Bild da ist, dann einbauen
                        //              System.out.println("Searching for " + x + "," + y);
                        String ground = null;
                        try {
                            ground = visMap[x + positionX][y + positionY].getGround_tex();
                        } catch (Exception ex) {
                            // Kann beim Scrollein vorkommen - Einfach nichts zeichnen, denn da ist die Map zu Ende...
                        }
                        // Was da?
                        if (ground != null) {
                            // Bild suchen und einfügen
                            GraphicsImage tempImage = getImgMap().get(ground);

                            if (tempImage != null) {
                                g3.drawImage(tempImage.getImage(), x * FIELD_HALF_X + OFFSET_1x1_X, (int) (y * FIELD_HALF_Y) + OFFSET_1x1_Y);
                            } else {
                                System.out.println("[RME][ERROR]: Image \"" + ground + "\" not found!");
                            }
                        }
                    }
                }

                for (int x = -2; x < sizeX && x < (viewX + 1); x += 1) {
                    for (int y = -2; y < sizeY && y < (viewY + 1); y += 1) {
                        if ((x + y) % 2 == 1) {
                            continue;
                        }
                        String fix = null;
                        try {
                            fix = visMap[x + positionX][y + positionY].getFix_tex();
                        } catch (Exception ex) {
                            // Kann beim Scrollein vorkommen - Einfach nichts zeichnen, denn da ist die Map zu Ende...
                        }
                        if (fix != null) {
                            // Bild suchen und einfügen
                            GraphicsImage fixImage = getImgMap().get(fix);

                            if (fixImage != null) {
                                g3.drawImage(fixImage.getImage(), x * FIELD_HALF_X + OFFSET_1x1_X, (int) (y * FIELD_HALF_Y) + OFFSET_1x1_Y);
                            } else {
                                System.out.println("[RME][ERROR]: Image \"" + fix + "\" not found!");
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

    private void renderServerCol(Graphics g) {
        // Neues Bewegungssystem
        List<FreePolygon> polys = rgi.mapModule.moveMap.getPolysForDebug();
        for (FreePolygon poly : polys) {
            g.setColor(poly.getColor());
            Polygon gPoly = new Polygon();
            List<Node> nodes = poly.getNodes();
            for (Node node : nodes) {
                gPoly.addPoint((float) (node.getX() - positionX) * FIELD_HALF_X + BASIC_FIELD_OFFSET_X, (float) ((float) ((node.getY() - positionY) * FIELD_HALF_Y) + BASIC_FIELD_OFFSET_Y));
            }
            g.fill(gPoly);
        }

//        // Murks, aber die position müssen gerade sein...
//        if (positionX % 2 == 1) {
//            positionX--;
//        }
//        if (positionY % 2 == 1) {
//            positionY--;
//        }
//        // Rendert die blaueb Kollisionsfarbe
//        for (int x = 0; x < sizeX && x < viewX; x++) {
//            for (int y = 0; y < sizeY && y < viewY; y++) {
//                if (x % 2 != y % 2) {
//                    continue; // Nur echte Felder
//                }
//                // Hat dieses Feld Kollision?
//                try {
//                    int val = rgi.mapModule.serverCollision[x + positionX][y + positionY];
//                    switch (val) {
//                        case 1:
//                            getImgMap().get("img/game/highlight_red.png").getImage().draw(x * FIELD_HALF_X, (int) (y * FIELD_HALF_Y));
//                            break;
//                        case 2:
//                            getImgMap().get("img/game/highlight_yellow_reddot.png").getImage().draw(x * FIELD_HALF_X, (int) (y * FIELD_HALF_Y));
//                            break;
//                        case 3:
//                            getImgMap().get("img/game/highlight_blue.png").getImage().draw(x * FIELD_HALF_X, (int) (y * FIELD_HALF_Y));
//                            break;
//                    }
//                    if (rgi.mapModule.serverRes[x + positionX][y + positionY] > System.currentTimeMillis()) {
//                        imgMap.get("img/game/highlight_bluecross.png").getImage().draw(x * FIELD_HALF_X, (int) (y * FIELD_HALF_Y));
//                    }
//                } catch (Exception ex) {
//                }
//            }
//        }
    }

    private void renderCoords(Graphics g2) {
        // Gitter rendern
        g2.setColor(Color.black);
        g2.setLineWidth(1);
        for (int i = 0; i < viewX * 2; i += 2) { // Doppelt so lang, das Bildverhältniss ist ja in der Regel nicht quadratisch
            // Linie von der Oberen Kante nach rechts unten
            g2.drawLine(i * FIELD_HALF_X + 2, 0, viewX * FIELD_HALF_X + 20, (int) ((viewX - 1 - i) * FIELD_HALF_Y + 21));
            // Linie von der Oberen Kante nach links unten
            g2.drawLine(i * FIELD_HALF_X + 28, 0, 0, (int) (i * FIELD_HALF_Y + 21));
        }

        for (int i = 0; i < viewY; i += 2) {
            // Linie von der Linken Kante nach Rechts Unten
            g2.drawLine(0, (int) (i * FIELD_HALF_Y) - 1, (viewY - 1 - i) * FIELD_HALF_X + 42, (int) ((viewY * FIELD_HALF_Y) + 22.5));
        }
        // Aktuelle Position suchen:
        Position mouse = translateCoordinatesToField(mouseX, mouseY);
        FloatingPointPosition precise = translateCoordinatesToFloatPos(mouseX, mouseY);
        // Position markieren:
        getImgMap().get("img/game/highlight_blue.png").getImage().draw((mouse.getX() - positionX) * FIELD_HALF_X, (int) ((mouse.getY() - positionY) * FIELD_HALF_Y));
        // Position anzeigen
        g2.setColor(Color.white);
        Font font = rgi.chat.getFont();
        String output = "Mouse: " + mouse.getX() + " " + mouse.getY();
        String precout = "Mouse (precise): " + precise.getfX() + " " + precise.getfY();
        g2.fillRect(5, 40, font.getWidth(output), font.getHeight(output));
        g2.fillRect(5, 60, font.getWidth(precout), font.getHeight(precout));
        g2.setFont(font);
        g2.setColor(Color.black);
        g2.drawString(output, 5, 40);
        g2.drawString(precout, 5, 60);

        // DEBUG - Begehbarkeit anzeigen:
        String walk = "free";
        if(Server.getInnerServer().netmap.getMoveMap().isPositionWalkable(precise))
        {
            walk = "blocked";
        }
        g2.drawString(walk, 5, 80);
    }

    public void setFramePosition(Dimension td) {
        // Setzt die Position des Rahmens, inzwischen Editor&Game
        framePos = td;
    }

    public void setVisMap(AbstractMapElement[][] newVisMap, int X, int Y) {
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
        viewPosChanged();
    }

    public void setVisibleArea(int vX, int vY) {
        //Setzt die Größe des sichtbaren Bereichs
        viewX = vX;
        viewY = vY;

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
        }
        viewPosChanged();
    }

    public void scrollRight() {
        if (viewX < sizeX) { // Ans sonsten ist eh alles drin
            if (positionX < (sizeX - viewX)) {
                positionX = positionX + 2;
            }

        }
        viewPosChanged();
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
        viewPosChanged();
    }

    public void scrollDown() {
        if (viewY < sizeY) { // Ansonsten ist eh alles drin, da brauch mer nicht scrollen...
            if (positionY < (sizeY - viewY)) {
                positionY = positionY + 2;
            }
        }
        viewPosChanged();
    }

    /**
     * Informiert die Minimap darüber, dass sich die derzeitige AnzeigePosition geändert hat.
     */
    private void viewPosChanged() {
        if (minimap != null) { // Falls aktiv
            minimap.viewChanged(positionX, positionY, viewX, viewY, sizeX, sizeY);
        }
    }

    public Dimension getSelectedField(int selX, int selY) {
        // Findet heraus, welches Feld geklickt wurde - Man muss die Felder mittig anklicken, sonst gehts nicht
        // Wir haben die X und Y Koordinate auf dem Display und wollen die X und Y Koordinate auf der Map bekommen
        // Versatz beachten
        selX = selX - 10;
        selY = selY - 15;
        // Grundposition bestimmen
        int coordX = selX / FIELD_HALF_X;
        int coordY = (int) (selY / FIELD_HALF_Y);
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
        selX = selX - OFFSET_PRECISE_X;
        selY = selY - OFFSET_PRECISE_Y;
        // Grundposition bestimmen
        int coordX = selX / FIELD_HALF_X;
        int coordY = (int) (selY / FIELD_HALF_Y);
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
        x = x - OFFSET_PRECISE_X;
        y = y - OFFSET_PRECISE_Y;
        int coordX = x / FIELD_HALF_X;
        int coordY = (int) (y / FIELD_HALF_Y);
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

    public FloatingPointPosition translateCoordinatesToFloatPos(int x, int y) {
        // Versatz beachten
        x = x - 10;
        y = y - 15;
        double coordX = (double) x / FIELD_HALF_X;
        double coordY = (double) y / FIELD_HALF_Y;
        // Scrollposition addieren
        coordX = coordX + positionX;
        coordY = coordY + positionY;
        return new FloatingPointPosition(coordX, coordY);
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

    public void calcColoredMaps(Color[] colors) {
        // Berechnet die eingefärbten Texturen
        ArrayList<GraphicsImage> tList = new ArrayList<GraphicsImage>();
        tList.addAll(coloredImgMap.values());
        for (int i = 0; i < tList.size(); i++) {
            Image im = tList.get(i).getImage();
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
                getImgMap().put(newImg.getImageName() + playerId, newImg);
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
        if ((x + y) % 2 == 1) {
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
        skyEffects = new ArrayList<SkyEffect>();
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
        parent = (CoreGraphics) container;
        parent.loadSplash();
        parent.slickReady = true;
        parent.setAlwaysRender(true);
        parent.setVerbose(true);
        parent.setVSync(true);
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
        if (initState == -1) {
            System.out.println("Displaying Splash...");
            this.paintComponent(g);
            return;
        } else if (initState == 12) {
            // Komponenten fürs Hauptmenu laden
            System.out.println("Preloading some components...");
            parent.loadPreMain();
            System.out.println("Initialising menu...");
            parent.initMainMenu();
            System.out.println("Initialising input...");
            parent.initInput();
            System.out.println("Initialisation done.");
            initState = 13;
        } else if (initState == 13) {
            // Hauptmenu
            parent.renderMainMenu(container, g);
        } else if (initState == 1) {
            setVisibleArea((realPixX / FIELD_HALF_X), (int) (realPixY / FIELD_HALF_Y));
            modi = -1;
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
            // LOAD abgeschlossen, dem Server mitteilen
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 7, 0, 0, 0, 0));
        } else if (initState == 3) {
            parent.startRendering();
            initState = 0;
        } else if (initState == 4) {
            // FinalPrepare
            parent.finalPrepare();
            minimap = Minimap.createMinimap(visMap, getImgMap(), realPixX, realPixY, rgi);
            minimap.setAllList(allList);
	    ingamemenu = new IngameMenu();
            overlays.add(minimap);
	    overlays.add(ingamemenu);
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

    /**
     * Stellt die ImageMap anderen Modulen zur Verfügung
     * @return - die ImageMap
     */
    public HashMap<String, GraphicsImage> getImgMap() {
        return imgMap;
    }
}
