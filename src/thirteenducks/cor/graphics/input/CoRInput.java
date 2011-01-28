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
package thirteenducks.cor.graphics.input;

//import elementcorp.rog.RogMapElement.collision;
import thirteenducks.cor.graphics.CoreGraphics;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.newdawn.slick.*;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.game.Pauseable;

/**
 *
 * @author tfg
 *  Das Inputmodul
 */
public class CoRInput implements Pauseable {

    /**
     * Veraltete Referenz auf das Grafikmodul
     * @deprecated
     */
    private CoreGraphics graphics;
    /**
     * Referenz auf den CoR-Kern, wird benötigt um die Funktionen desselben aufzurufen. Eher Pfusch.
     */
    private ClientCore.InnerClient rgi;
    /**
     * Alle derzeit selektierten IGE's.
     */
    private ArrayList<InteractableGameElement> selected;
    /**
     * Alle derzeit anklickbaren IGE's
     */
    private ArrayList<InteractableGameElement> igeList;
    /**
     * Die SelektionsMap, die alle Position speichert, an denen derzeit etwas selektierbar ist.
     */
    private SelectionMap selMap;
    /**
     * Ein anderer Eingabemodus. Kann durch einen Aufruf gesetzt werden, dann kann sich das Inputmodul kurzzeitig
     * komplett anders verhalten, was zusätzliche Eingaben ermöglicht.
     */
    private CoRInputMode specialMode = null;
    /**
     * Das Input-Subsystem
     */
    private org.newdawn.slick.Input input;
    /**
     * True, während die shift-Taste (umschalt) gedrückt ist.
     */
    private boolean shiftDown = false;
    /**
     * True, während die ctrl-Taste (Strg) gedrückt ist.
     */
    private boolean ctrlDown = false;
    /**
     * Veralteter Scroll-Mechanismus, PFUSCH!
     * @deprecated
     */
    public boolean[] scroll;
    /**
     * Chatmodus, der die meisten Tastatureingaben an den Chat weiterreicht.
     * Auch PFUSCH, sollte mit InputModes gemacht werden.
     * @deprecated
     */
    public boolean chatMode = false;
    /**
     * Die auf den Nummerntasten 0-9 speicherbaren Belegungen
     */
    private InteractableGameElement[][] savedSelections; // Gespeicherte Selektion (0-9)
    /**
     * Der Zeitpunkt, zu dem zuletzt eine savedSelection aufgerufen wurde. Zur Doppelklick-Erkennung
     */
    private long lastSavedKlick = 0;
    /**
     * Die zuletzt aufgerufene savedSelection. Für Doppelklick-Erkennung
     */
    private int lastSavedRead = -1;
    /**
     * Wird bei jedem normalen Linksklick auf Einheiten gesetzt.
     * Dient zur Doppelklick-Selektion
     */
    private long klickTime = 0;
    /**
     * Die Zeit, die maximal zwischen 2 Klicks vergehen darf,
     * damit es noch ein Doppelklick ist.
     */
    public static final int doubleKlickDelay = 400;
    /**
     * Die Koordinaten des Startpunktes der Selektionsbox.
     */
    public Dimension boxselectionstart;
    /**
     * Sind wir derzeit im Box-Selektionsmodus?
     */
    public boolean dragSelectionBox = false;

    public void initAsSub(CoreGraphics rg, int mapX, int mapY) {
        graphics = rg;
        selMap = new SelectionMap(mapX, mapY);
        rgi.logger("[RogInput][Init]: Adding Listeners to Gui...");
        initListeners();
        rgi.logger("[RogInput] RogInput is ready to rock! (init completed)");
    }

    public boolean hasSpecialMode() {
        return (specialMode != null);
    }

    public void addAndReplaceSpecialMode(CoRInputMode newmode) {
        // Fügt den neuen Modi hinzu und entfernt den alten (falls vorhanden)
        if (specialMode != null) {
            // Alten entfernen
            specialMode.endMode();
        }
        input.removeAllMouseListeners();

        // Neuen registrieren

        specialMode = newmode;
        specialMode.startMode();
        input.addMouseListener(new MouseListener() {

            @Override
            public void mouseWheelMoved(int change) {
            }

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                if (!graphics.content.pauseMode) {
                    specialMode.mouseKlicked(button, x, y, clickCount);
                }
            }

            @Override
            public void mousePressed(int button, int x, int y) {
                if (!graphics.content.pauseMode) {
                    // Hud, Minimap?
                    if (x > (graphics.content.hudX + (graphics.content.hudSizeX * 0.1)) && x < graphics.content.hudX + graphics.content.hudSizeX - (graphics.content.hudSizeX * 0.1)) {
                        if (y > (graphics.content.viewY * 15 / 7 * 1.2) && y < graphics.content.viewY * 15 / 7 * 3 - graphics.content.viewY * 15 / 7 * 0.2) {
                            // Auf Minimap!
                            graphics.miniMapScrolling = true;
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(int button, int x, int y) {
                if (!graphics.content.pauseMode) {
                    graphics.miniMapScrolling = false;
                }
            }

            @Override
            public void mouseMoved(int oldx, int oldy, int newx, int newy) {
                // Position markieren
                graphics.content.mouseX = newx;
                graphics.content.mouseY = newy;
                if (!graphics.content.pauseMode) {
                    specialMode.mouseMoved(oldx, oldy, newx, newy);
                }
            }

            @Override
            public void setInput(Input input) {
            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputEnded() {
            }

            @Override
            public void mouseDragged(int oldx, int oldy, int newx, int newy) {
                // Position markieren
                graphics.content.mouseX = newx;
                graphics.content.mouseY = newy;
                if (!graphics.content.pauseMode) {
                    specialMode.mouseMoved(oldx, oldy, newx, newy);
                }
            }

            @Override
            public void inputStarted() {
            }
        });


        input.resume();
    }

    public void removeSpecialMode() {
        // Entfernt den SpecialMode und stellt das normale Verhalten wieder her
        if (specialMode != null) {
            specialMode.endMode();
        }
        // Listener löschen, falls welche da sind
        input.removeAllMouseListeners();
        // Ursprungliche Listener wiederherstellen
        initListeners();
    }

    private void initListeners() {
        // Mauszeiger
        input.removeAllMouseListeners();
        input.removeAllKeyListeners();
        input.addMouseListener(new MouseListener() {

            @Override
            public void mouseWheelMoved(int change) {
            }

            @Override
            public void mouseClicked(final int button, final int x, final int y, final int clickCount) {
                if (!graphics.content.pauseMode) {
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            // Im Hud oder im Game-Bereich
                            if (x > graphics.content.hudX) {
                                // Im Hud
                                // Auf MiniMap?
                                if (x > (graphics.content.hudX + (graphics.content.hudSizeX * 0.1)) && x < graphics.content.hudX + graphics.content.hudSizeX - (graphics.content.hudSizeX * 0.1)) {
                                    if (y > (graphics.content.realPixY / 7 + graphics.content.realPixY * 2 / 7 * 0.2) && y < graphics.content.realPixY * 3 / 7) {
                                        // Auf Minimap!
                                        if (button == 0) {
                                            graphics.content.klickedOnMiniMap(button, x, y, clickCount);
                                        }
                                        return;
                                    }
                                }
                                // In den Sel-Unit Bereich
                                if (!selected.isEmpty()) {
                                    if (graphics.clickedInSel(button, x, y, clickCount)) {
                                        // Feld selektiert?
                                        if (button == 0) {
                                            graphics.content.calcSelClicked(button, x, y, clickCount, selected, true);
                                        } else if (button == 1) {
                                            graphics.content.calcSelClicked(button, x, y, clickCount, selected, false);
                                        }
                                    } else if (graphics.clickedInOpt(button, x, y, clickCount)) {
                                        // Fähigkeit angeklickt!
                                        // Auslagern, das muss die Grafik machen, wegen potentiellen Änderungen
                                        graphics.content.lastInputButton = button;
                                        graphics.content.lastInputEvent = 1;
                                        graphics.content.lastInputX = x;
                                        graphics.content.lastInputY = y;
                                        //graphics.content.calcOptClicked(button, x, y, clickCount, selected);
                                    }
                                }
                                //TODO - klicks ins hud behandeln
                            } else {
                                // Rechts, oder Linksklick?
                                if (button == 0) {
                                    // Normaler Linksklick
                                    // Muss an die Grafik ausgelagert werden, sonst ist Slick beleidigt
                                    // Wird bei MouseReleased behandelt.
                                    //mouseKlickedLeft(button, x, y, clickCount);
                                } else if (button == 1) {
                                    // Rechts geklickt
                                    // Wird bei MouseReleased angeklickt
                                }
                            }
                        }
                    });

                    t.setDaemon(true);
                    t.setName("InputEventWorker - MouseClicked");
                    t.start();
                }
            }

            @Override
            public void mousePressed(int button, int x, int y) {
                if (!graphics.content.pauseMode) {
                    // Hud oder Game?
                    if (x > graphics.content.hudX) {
                        // Hud, Minimap?
                        if (x > (graphics.content.hudX + (graphics.content.hudSizeX * 0.1)) && x < graphics.content.hudX + graphics.content.hudSizeX - (graphics.content.hudSizeX * 0.1)) {
                            if (y > (graphics.content.viewY * 15 / 7 * 1.2) && y < graphics.content.viewY * 15 / 7 * 3 - graphics.content.viewY * 15 / 7 * 0.2) {
                                // Auf Minimap!
                                if (button == 0) {
                                    graphics.miniMapScrolling = true;
                                }
                            }
                        }
                    } else {
                        // Im Game.
                        // Selektionskästchen ziehen
                        if (button == 0) {
                            startSelectionBox(x, y);
                            graphics.dSBX = x;
                            graphics.dSBY = y;
                        } else if (button == 1) {
                            if (rgi.rogGraphics.rightScrollingEnabled) {
                                graphics.startRightScrolling();
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(final int button, final int x, final int y) {
                if (!graphics.content.pauseMode) {
                    if (rgi.rogGraphics.rightScrollingEnabled) {
                        graphics.stopRightScrolling();
                    }
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            graphics.miniMapScrolling = false;
                            if (button == 0) {
                                // Wenn die Maus um mindestens 5 Pixel bewegt wurde ist es ein Selektionsrahmen, sonst ein normaler Klick
                                int dx = graphics.dSBX - x;
                                int dy = graphics.dSBY - y;
                                if ((dx > 5 || dx < -5) && (dy > 5 || dy < -5)) {
                                    if (dragSelectionBox) {
                                        // Selektion ist an, jetzt abschalten und eingeschlossene Einheiten selektieren
                                        if (!shiftDown) {
                                            for (int i = 0; i < selected.size(); i++) {
                                                selected.get(i).setSelected(false);
                                            }
                                            selected.clear();
                                        }
                                        List<InteractableGameElement> selectedIGE = getBoxSelected(x, y);
                                        if (selectedIGE != null) {
                                            for (InteractableGameElement ige : selectedIGE) {
                                                ige.setSelected(true);
                                                selected.add(ige);
                                            }
                                        }

                                    }
                                } else {
                                    // Kein Rahmen, normaler klick
                                    if (x < graphics.content.hudX) {
                                        CoRInput.this.mouseKlickedLeft(button, x, y, 1);
                                    }
                                }
                                // Das auf jeden Fall machen:
                                stopSelectionBox();
                            }
                            if (button == 1 && !rgi.rogGraphics.rightScrollingEnabled || (System.currentTimeMillis() - rgi.rogGraphics.rightScrollStart < 200)) {
                                if (x > graphics.content.hudX) {
                                    // Auf Minimap?
                                    if (x > (graphics.content.hudX + (graphics.content.hudSizeX * 0.1)) && x < graphics.content.hudX + graphics.content.hudSizeX - (graphics.content.hudSizeX * 0.1)) {
                                        if (y > (graphics.content.realPixY / 7 * 1.4) && y < graphics.content.realPixY / 7 * 3) {
                                            // Auf Minimap!
                                            mouseKlickedRightMiniMap(button, x, y);
                                        }
                                    }
                                } else {
                                    mouseKlickedRight(button, x, y);
                                }
                            }
                        }
                    });
                    t.setDaemon(true);
                    t.setName("InputEventWorker - MouseReleased");
                    t.start();
                }
            }

            @Override
            public void mouseMoved(int oldx, int oldy, int newx, int newy) {
                // Position markieren
                graphics.content.mouseX = newx;
                graphics.content.mouseY = newy;
            }

            @Override
            public void setInput(Input input) {
            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void mouseDragged(int i, int i1, int newx, int newy) {
                // Position markieren
                graphics.content.mouseX = newx;
                graphics.content.mouseY = newy;
            }

            @Override
            public void inputStarted() {
            }

            @Override
            public void inputEnded() {
            }
        });

        input.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(int key, char c) {
                if (!graphics.content.pauseMode) {
                    if (!chatMode) {
                        switch (key) {
                            case Input.KEY_LSHIFT:
                            case Input.KEY_RSHIFT:
                                shiftDown = true;
                                break;
                            case Input.KEY_LCONTROL:
                            case Input.KEY_RCONTROL:
                                ctrlDown = true;
                                break;
                            case Input.KEY_W:
                            case Input.KEY_UP:
                                scroll[0] = true;
                                break;
                            case Input.KEY_A:
                            case Input.KEY_LEFT:
                                scroll[1] = true;
                                break;
                            case Input.KEY_S:
                            case Input.KEY_DOWN:
                                scroll[2] = true;
                                break;
                            case Input.KEY_D:
                            case Input.KEY_RIGHT:
                                scroll[3] = true;
                                break;
                            case Input.KEY_0:
                                manageSavedSel(0, ctrlDown);
                                break;
                            case Input.KEY_1:
                                manageSavedSel(1, ctrlDown);
                                break;
                            case Input.KEY_2:
                                manageSavedSel(2, ctrlDown);
                                break;
                            case Input.KEY_3:
                                manageSavedSel(3, ctrlDown);
                                break;
                            case Input.KEY_4:
                                manageSavedSel(4, ctrlDown);
                                break;
                            case Input.KEY_5:
                                manageSavedSel(5, ctrlDown);
                                break;
                            case Input.KEY_6:
                                manageSavedSel(6, ctrlDown);
                                break;
                            case Input.KEY_7:
                                manageSavedSel(7, ctrlDown);
                                break;
                            case Input.KEY_8:
                                manageSavedSel(8, ctrlDown);
                                break;
                            case Input.KEY_9:
                                manageSavedSel(9, ctrlDown);
                                break;
                            case Input.KEY_COMMA:
                                System.out.println("AddMe: Reimplement select workless workers!");
                                break;
                        }
                    }
                }
            }

            @Override
            public void keyReleased(int key, char c) {
                if (!graphics.content.pauseMode) {
                    if (!chatMode) {
                        switch (key) {
                            case Input.KEY_LSHIFT:
                            case Input.KEY_RSHIFT:
                                shiftDown = false;
                                break;
                            case Input.KEY_LCONTROL:
                            case Input.KEY_RCONTROL:
                                ctrlDown = false;
                                break;
                            case Input.KEY_P:
                            case Input.KEY_PAUSE:
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 6, 0, 0, 0, 0));
                                break;
                            case Input.KEY_F10:
                                // Ende
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 10, 0, 0, 0, 0));
                                rgi.shutdown(0);
                                break;
                            case Input.KEY_M:
                                // Mute umschalten
                                rgi.rogSound.toggleMute();
                                break;
                            case Input.KEY_E:
                                // Energiebalken umschalten
                                rgi.rogGraphics.setAlwaysShowEnergyBars(!rgi.rogGraphics.getAlwaysShowEnergyBars());
                                break;
                            case Input.KEY_Y:
                            case Input.KEY_X:
                                // Chat umschalten
                                rgi.chat.toggleChat();
                                break;
                            case Input.KEY_ESCAPE:
                                // Speziellen Inputmodus verlassen
                                CoRInput.this.removeSpecialMode();
                                break;
                            case Input.KEY_F2:
                                // Kollisionsmodus
                                rgi.netctrl.broadcastString("F2", (byte) 44);
                                if (rgi.isInDebugMode()) {
                                    rgi.rogGraphics.content.colMode = !rgi.rogGraphics.content.colMode;
                                }
                                break;
                            case Input.KEY_F3:
                                // Koordinatenmodus
                                rgi.netctrl.broadcastString("F3", (byte) 44);
                                if (rgi.isInDebugMode()) {
                                    rgi.rogGraphics.content.coordMode = !rgi.rogGraphics.content.coordMode;
                                }
                                break;
                            case Input.KEY_F4:
                                // ID-Modus
                                rgi.netctrl.broadcastString("F4", (byte) 44);
                                if (rgi.isInDebugMode()) {
                                    rgi.rogGraphics.content.idMode = !rgi.rogGraphics.content.idMode;
                                }
                                break;
                            case Input.KEY_F5:
                                // ServerKollisionsModus
                                rgi.netctrl.broadcastString("F5", (byte) 44);
                                if (rgi.isInDebugMode()) {
                                    rgi.rogGraphics.content.serverColMode = !rgi.rogGraphics.content.serverColMode;
                                }
                                break;
                            case Input.KEY_F6:
                                // FoW abschalten
                                rgi.netctrl.broadcastString("F6", (byte) 44);
                                if (rgi.isInDebugMode()) {
                                    rgi.rogGraphics.disableFoW();
                                }
                                break;
                            case Input.KEY_F7:
                                // Ressourcen herbeicheaten
                                rgi.netctrl.broadcastString("F7", (byte) 44);
                                if (rgi.isInDebugMode()) {
                                    NetPlayer player = rgi.game.getOwnPlayer();
                                    player.res1 += 1000;
                                    player.res2 += 1000;
                                    player.res3 += 1000;
                                    player.res4 += 1000;
                                    player.res5 += 1000;
                                }
                                break;
                            case Input.KEY_F8:
                                // Einheitenverhalten-Debugmodus
                                if (rgi.isInDebugMode()) {
                                    rgi.rogGraphics.content.unitDestMode += 1;
                                    if (rgi.rogGraphics.content.unitDestMode == 4) {
                                        rgi.rogGraphics.content.unitDestMode = 0;
                                    }
                                }
                                break;
                            case Input.KEY_W:
                            case Input.KEY_UP:
                                scroll[0] = false;
                                break;
                            case Input.KEY_A:
                            case Input.KEY_LEFT:
                                scroll[1] = false;
                                break;
                            case Input.KEY_S:
                            case Input.KEY_DOWN:
                                scroll[2] = false;
                                break;
                            case Input.KEY_D:
                            case Input.KEY_RIGHT:
                                scroll[3] = false;
                                break;
                            case Input.KEY_DELETE:
                                // Alle derzeit selektierten Einheiten löschen
                                rgi.mapModule.deleteSelected(selected);
                                break;
                            case Input.KEY_T:
                                rgi.teamSel.toggle();
                                break;
                        }
                    } else {
                        // Tasten, die auch im Chatmode verarbeitet werden
                        if (key == Input.KEY_F10) {
                            // Ende
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 10, 0, 0, 0, 0));
                            rgi.shutdown(0);
                        } else if (key == Input.KEY_ESCAPE) {
                            // Chatmode verlassen
                            // Chat umschalten
                            rgi.chat.toggleChat();
                        } else {
                            // An den Chat weiterleiten
                            rgi.chat.input(key, c);
                        }
                    }
                } else {
                    if (!chatMode) {
                        // Befehle, die auch im Pause-Modus funktionieren:
                        switch (key) {
                            case Input.KEY_P:
                            case Input.KEY_PAUSE:
                                // Pause umschalten
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 6, 0, 0, 0, 0));
                                break;
                            case Input.KEY_X:
                            case Input.KEY_Y:
                                // Chat umschalten
                                rgi.chat.toggleChat();
                                break;
                        }
                    } else {
                        // Tasten, die auch im Chatmode verarbeitet werden
                        if (key == Input.KEY_ESCAPE) {
                            // Chatmode verlassen
                            // Chat umschalten
                            rgi.chat.toggleChat();
                        } else {
                            // An den Chat weiterleiten
                            rgi.chat.input(key, c);
                        }
                    }
                }
            }

            @Override
            public void setInput(Input input) {
            }

            @Override
            public boolean isAcceptingInput() {
                return true;
            }

            @Override
            public void inputStarted() {
            }

            @Override
            public void inputEnded() {
            }
        });

        input.resume();
    }

    /**
     * Verwaltet die gespeicherten Gruppen
     */
    private void manageSavedSel(int number, boolean set) {
        // Setzen oder auslesen?
        if (set) {
            // Setzen
            savedSelections[number] = selected.toArray(new GameObject[selected.size()]);
        } else {
            // Doppelklick?
            if (lastSavedRead == number && System.currentTimeMillis() - lastSavedKlick < 250) {
                // Ansicht zum ersten Go scollen
                if (!selected.isEmpty()) {
                    if (selected.get(0) instanceof Unit) {
                        Unit u = (Unit) selected.get(0);
                        rgi.rogGraphics.jumpTo(u.getMainPosition().getX() - (rgi.rogGraphics.content.viewX / 2), u.getMainPosition().getY() - (rgi.rogGraphics.content.viewY / 2));
                    } else {
                        Building b = (Building) selected.get(0);
                        rgi.rogGraphics.jumpTo((b.getMainPosition().getX() + 6) - (rgi.rogGraphics.content.viewX / 2), b.getMainPosition().getY() - (rgi.rogGraphics.content.viewY / 2));
                    }
                }
            } else {
                // Alle momentan selektierten Abwählen (falls nicht shift gedrückt wurde)
                if (!shiftDown) {
                    for (InteractableGameElement obj : selected) {
                        obj.setSelected(false);
                    }
                    selected.clear();
                }
                // Wenn was gespeichert ist, dann das setzen
                InteractableGameElement[] list = savedSelections[number];
                if (list != null) {
                    for (InteractableGameElement obj : list) {
                        if (!selected.contains(obj)) {
                            obj.setSelected(true);
                            selected.add(obj);
                        }
                    }
                }
            }
            lastSavedRead = number;
            lastSavedKlick = System.currentTimeMillis();
        }
    }

    /**
     * Wird beim Rechtsklick auf die Minimap aufgerufen
     * @param e
     */
    private void mouseKlickedRightMiniMap(final int button, final int x, final int y) {
        // Nur bei selektieren Einheiten
        if (!selected.isEmpty() && selected.get(0).getClass().equals(Unit.class)) {
            // Alle da hin schicken, immer mit der Zielsuch-Logik
            // Erstmal grundlegendes Ziel berechnen
            Dimension selField = rgi.rogGraphics.content.searchMiniMid(x, y);
            // Auf 2er-Raster anpassen
            if ((selField.width + selField.height) % 2 == 1) {
                selField.height--;
            }

            for (int i = 0; i < selected.size(); i++) {
                Unit tmpUnit = (Unit) selected.get(i);
                Position target = new Position(selField.width, selField.height).aroundMe(i, rgi, 10000);
                tmpUnit.sendToPosition(target, rgi, true);
            }

        }
    }

    /**
     * Wird bei Linksklick aufgerufen. Behandelt an/abwählen von Einheiten, Gebäuden.
     * @param e     MouseEvent, enthält Position, welcher Button, wie oft, welche Maus, etc..
     */
    public void mouseKlickedLeft(final int button, final int x, final int y, int clickCount) {
        System.out.println("AddMe: Rework mKL-Input");
        rgi.rogGraphics.triggerTempStatus(null);
        final Dimension selField = graphics.content.getGameSelectedField(x, y);
        Unit rUnit = identifyUnit(x, y);

        // Wenn keine Einheit angeklickt wurde alle Einheiten abwählen:
        if (rUnit == null) {
            for (int i = 0; i < selected.size(); i++) {
                selected.get(i).setSelected(false);
            }
            selected.clear();


            Building rBuilding = identifyBuilding(selField.width, selField.height);
            if (rBuilding != null) {
                if (rBuilding.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
                    // Gebäude auswählen
                    selected.add(rBuilding);
                    rBuilding.setSelected(true);
                } else {
                    // Im FogOfWar?
                    if (rgi.rogGraphics.content.fowmap[selField.width][selField.height] > 1) { // Es muss sichtbar sein, erkundet reicht nicht
                        // Temporär Energie im Hud anzeigen:
                        rgi.rogGraphics.triggerTempStatus(rBuilding);
                    }
                }
            }
        } else {
            //Wenn shift nicht gedrücekt ist Alle Einheiten abwählen :
            if (!shiftDown) {
                for (int i = 0; i < selected.size(); i++) {
                    selected.get(i).setSelected(false);
                }
                selected.clear();
                if (rUnit.getPlayerId() != rgi.game.getOwnPlayer().playerId) {
                    rgi.rogGraphics.triggerTempStatus(rUnit);
                }
            }

            if (System.currentTimeMillis() - klickTime <= CoRInput.doubleKlickDelay) {
                // Es ist ein doppelklick (der 2. Klick)
                // Alle sichtbaren Einheiten gleichen Typs selektieren
                if (rUnit.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
                    for (int i = 0; i < rgi.mapModule.unitList.size(); i++) {
                        Unit unit = rgi.mapModule.unitList.get(i);
                        if (rgi.rogGraphics.isInSight(unit.getMainPosition().getX(), unit.getMainPosition().getY()) && unit.getPlayerId() == rUnit.getPlayerId() && unit.getDescTypeId() == rUnit.getDescTypeId() && unit.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
                            unit.setSelected(true);
                            selected.add(unit);
                        }
                    }
                }
            } else {
                // Normaler - "erster" Klick
                // Die angeklickte Einheit zur Auswahl hinzufügen:
                if (rUnit.getPlayerId() == rgi.game.getOwnPlayer().playerId) {
                    selected.add(rUnit);
                    rUnit.setSelected(shiftDown);
                }
            }
            klickTime = System.currentTimeMillis();
        }
    }

    /**
     * Wird bei Rechtsklick aufgerufen. Behandelt Befehle an die Einheiten (Angreifen, bewegen, Gebäude weiterbauen, ...)
     * @param e: MouseEvent, enthält Position, welcher Button, wie oft, etc..
     */
    public void mouseKlickedRight(final int button, final int x, final int y) {
        System.out.println("AddMe: Improve/Acknowledge basic check for valid Koordinates!");
        System.out.println("AddMe: Check for double-klicks");
        Position selField = new Position(x, y);
        if (!selField.valid()) {
            selField.setX(selField.getX() + 1);
        }
        // Überhaupt was selektiert?
        if (!selected.isEmpty()) {
            // Ziele finden:
            List<InteractableGameElement> targets = selMap.getIGEsAt(x, y);
            if (!targets.isEmpty()) {
                for (InteractableGameElement elem : selected) {
                    elem.command(button, targets, false);
                }
            } else {
                for (InteractableGameElement elem : selected) {
                    elem.command(button, selField, false);
                }
            }
            }}

            // Überhaupt was selektiert?

      /*      if (!selected.isEmpty()) {

                // Was wurde angeklickt?

                final Dimension selField = graphics.content.getGameSelectedField(x, y);
                final Unit selUnit = graphics.content.identifyUnit(x, y);
                final Building selBuilding = graphics.content.identifyBuilding(selField.width, selField.height);

                final int playerId = selected.get(0).getPlayerId();

                // Ganz unbekannt:
                final boolean clickedInBlack = (rgi.rogGraphics.content.fowmap[(int) selField.getWidth()][(int) selField.getHeight()] < 1);
                // Grob bekannt:
                final boolean clickedInKnown = (rgi.rogGraphics.content.fowmap[(int) selField.getWidth()][(int) selField.getHeight()] < 2);

                // Ab jetzt multithreaden, sonst wird die Grafik überlastet

                Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (!clickedInBlack) { //Weiß der Spieler, was er anklickt?

                            // Was ist zurzeit selektiert

                            if (selected.get(0).getClass().equals(Unit.class)) {

                                // Unit selektiert

                                if (selUnit != null && selUnit.getPlayerId() != playerId && !rgi.game.areAllies(selUnit, rgi.game.getPlayer(playerId)) && !clickedInKnown) { // Man kann Feinde im erkundeten nicht angereifen

                                    // Angreifen - Einzeln?
                                    if (selected.size() == 1) {
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 29, selected.get(0).netID, selUnit.netID, 2, 0));
                                    } else {
                                        // Bei mehr als 2 sind mehrere Packete nötig:
                                        if (selected.size() == 2) {
                                            // Nur ein Packet:
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selUnit.netID, selected.get(0).netID, selected.get(1).netID, 0));
                                        } else {
                                            // Das erste gleich mal raushauen
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selUnit.netID, selected.get(0).netID, selected.get(1).netID, selected.get(2).netID));
                                            // Jetzt den Rest abhandeln
                                            int[] ids = new int[4];
                                            for (int i = 0; i < 4; i++) {
                                                ids[i] = 0;
                                            }
                                            int nextselindex = 3;
                                            int nextidindex = 0;
                                            // Solange noch was da ist:
                                            while (nextselindex < selected.size()) {
                                                // Auffüllen
                                                ids[nextidindex] = selected.get(nextselindex).netID;
                                                nextidindex++;
                                                nextselindex++;
                                                // Zu weit?
                                                if (nextidindex == 4) {
                                                    // Einmal rausschicken & löschen
                                                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
                                                    for (int i = 0; i < 4; i++) {
                                                        ids[i] = 0;
                                                    }
                                                    nextidindex = 0;
                                                }
                                            }
                                            // Fertig, den Rest noch senden
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
                                        }
                                    }
                                } else if (selBuilding != null && selBuilding.getPlayerId() == playerId && selBuilding.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
                                    // Gebäude - gleicher Spieler : Bauen
                                    Unit unit = (Unit) selected.get(0);
                                    // Wenn noch keiner da dran rumbaut:
                                    System.out.println("AddMe: Check for other builders!");
                                    // Kann die Einheit das?
                                    AbilityBuild ro = unit.getBuildAbility(selBuilding.getDescTypeId());
                                    if (ro != null) {
                                        // Ja, geht.
                                        //unit.moveToBuilding(rBuilding, rgi);
                                        // Unit hinlaufen lassen
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 26, unit.netID, selBuilding.netID, 0, 0));
                                        // Signal senden
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 17, unit.netID, selBuilding.netID, ro.duration, 0));
                                    }
                                } else if (selBuilding != null && selBuilding.getPlayerId() == playerId && selBuilding.ready) {
                                    // Gebäude - gleicher Spieler : Betreten
                                    // So viele Einheiten reinschicken, wie noch Platz frei ist.
                                    int searchCount = 0;
                                    int free = selBuilding.intraFree();
                                    while (free > 0 && selected.size() > searchCount) {
                                        if (selBuilding.accepts == Building.ACCEPTS_ALL) {
                                            selBuilding.goIntra((Unit) selected.get(searchCount), rgi);
                                            searchCount++;
                                            free--;
                                        } else {
                                            boolean found = true;
                                            while (!((Unit) selected.get(searchCount)).canHarvest) {
                                                searchCount++;
                                                if (selected.size() <= searchCount) {
                                                    found = false;
                                                    break;
                                                }
                                            }
                                            if (found) {
                                                selBuilding.goIntra((Unit) selected.get(searchCount), rgi);
                                                searchCount++;
                                                free--;
                                            }
                                        }
                                    }

                                } else if (selBuilding != null && selBuilding.playerId != playerId && !rgi.game.areAllies(selBuilding, rgi.game.getPlayer(playerId)) && selBuilding.wasSeen) { // Man kann Gebäude im erkundeten angreiffen, aber nur wenn sie bereits entdeckt wurden
                                    // Gebäude - fremder Spieler : Gebäudeangriff
                                    // Gruppenangriff?
                                    if (selected.size() == 1) {
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 29, selected.get(0).netID, selBuilding.netID, 2, 0));
                                    } else {
                                        // Hier sind unter umständen mehrere Packete nötig:
                                        if (selected.size() == 2) {
                                            // Nur ein Packet:
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selBuilding.netID, selected.get(0).netID, selected.get(1).netID, 0));
                                        } else {
                                            // Das erste gleich mal raushauen
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, selBuilding.netID, selected.get(0).netID, selected.get(1).netID, selected.get(2).netID));
                                            // Jetzt den Rest abhandeln
                                            int[] ids = new int[4];
                                            for (int i = 0; i < 4; i++) {
                                                ids[i] = 0;
                                            }
                                            int nextselindex = 3;
                                            int nextidindex = 0;
                                            // Solange noch was da ist:
                                            while (nextselindex < selected.size()) {
                                                // Auffüllen
                                                ids[nextidindex] = selected.get(nextselindex).netID;
                                                nextidindex++;
                                                nextselindex++;
                                                // Zu weit?
                                                if (nextidindex == 4) {
                                                    // Einmal rausschicken & löschen
                                                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
                                                    for (int i = 0; i < 4; i++) {
                                                        ids[i] = 0;
                                                    }
                                                    nextidindex = 0;
                                                }
                                            }
                                            // Fertig, den Rest noch senden
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 32, ids[0], ids[1], ids[2], ids[3]));
                                        }
                                    }

                                } else if (selRessource != null) { // Man kann Ressourcen im grauen anklicken
                                    // Ernten
                                    if (selRessource.getType() < 5) {
                                        for (GameObject obj : selected) {
                                            try {
                                                Unit unit = (Unit) obj;
                                                unit.goHarvest(selRessource, rgi);
                                            } catch (ClassCastException ex1) {
                                            }
                                        }
                                    }
                                } else {
                                    // Bewegen - man kann sich ins graue bewegen
                                    // Mehrere?
                                    if (selected.size() == 1) {
                                        Unit tmpUnit = (Unit) selected.get(0);
                                        Position target = new Position(selField.width, selField.height);
                                        tmpUnit.sendToPosition(target, rgi, true);
                                    } else {
                                        // Alle vorbereiten:
                                        for (int i = 0; i < selected.size(); i++) {
                                            Unit mover = (Unit) selected.get(i);
                                            mover.prepareMove();
                                        }
                                        // Befehl abschicken:
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, selField.width, selField.height, selected.get(0).netID, selected.get(1).netID));
                                        // Hier sind unter umständen mehrere Packete nötig:
                                        if (selected.size() == 2) {
                                            // Nein, abbrechen
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, 0, 0, 0, 0));
                                        } else {
                                            // Jetzt den Rest abhandeln
                                            int[] ids = new int[4];
                                            for (int i = 0; i < 4; i++) {
                                                ids[i] = 0;
                                            }
                                            int nextselindex = 2;
                                            int nextidindex = 0;
                                            // Solange noch was da ist:
                                            while (nextselindex < selected.size()) {
                                                // Auffüllen
                                                ids[nextidindex] = selected.get(nextselindex).netID;
                                                nextidindex++;
                                                nextselindex++;
                                                // Zu weit?
                                                if (nextidindex == 4) {
                                                    // Einmal rausschicken & löschen
                                                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
                                                    for (int i = 0; i < 4; i++) {
                                                        ids[i] = 0;
                                                    }
                                                    nextidindex = 0;
                                                }
                                            }
                                            // Fertig, den Rest noch senden
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
                                        }
                                    }
                                }

                            } else if (selected.get(0).getClass().equals(Building.class)) {
                                // Building selektiert
                                // Nur Gebäude die auch was rekrutieren, dürfen Sammelpunkte setzen
                                boolean recruits = false;
                                for (Ability ab : selected.get(0).abilitys) {
                                    if (ab.type == Ability.ABILITY_RECRUIT) {
                                        recruits = true;
                                        break;
                                    }
                                }
                                // Kann das Gebäude angreifen?
                                if (selected.get(0).getDamage() != 0) {
                                    // Eigene Position berechnen
                                    Building bui = (Building) selected.get(0);
                                    float bx = 0;
                                    float by = 0;
                                    bx = bui.position.X + ((bui.z1 - 1) * 1.0f / 2);
                                    by = bui.position.Y - ((bui.z1 - 1) * 1.0f / 2);
                                    bx += ((bui.z2 - 1) * 1.0f / 2);
                                    by += ((bui.z2 - 1) * 1.0f / 2);
                                    Position Omg = new Position((int) bx, (int) by);
                                    // Feindliche Einheit angeklickt?
                                    if (selUnit != null && selUnit.playerId != playerId && !rgi.game.areAllies(selUnit, rgi.game.getPlayer(playerId))) {
                                        // In Reichweite?
                                        if (selUnit.position.getDistance(Omg) <= bui.getRange()) {
                                            // Idle deaktivieren
                                            bui.getbehaviourC(10).deactivate();
                                            // Angriff an Server senden
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 47, bui.netID, selUnit.netID, 0, 0));
                                        }
                                    } else if (selBuilding != null && selBuilding.playerId != playerId && !rgi.game.areAllies(selBuilding, rgi.game.getPlayer(playerId))) {
                                        //Eines der Gebäudefelder in Reichweite?
                                        //z1 nuff, z2 nab
                                        boolean inreichweite = false;
                                        int randfelder = (selBuilding.z1 + selBuilding.z2 - 2) * 2;
                                        Position Test = new Position(selBuilding.position.X, selBuilding.position.Y);
                                        for (int i = 0; i < randfelder; i++) {
                                            if (i == 0) {
                                            } else if (i < selBuilding.z2) {
                                                Test.X++;
                                                Test.Y++;
                                            } else if (i < selBuilding.z1 + selBuilding.z2) {
                                                Test.X++;
                                                Test.Y--;
                                            } else if (i < selBuilding.z1 + 2 * selBuilding.z2) {
                                                Test.X--;
                                                Test.Y--;
                                            } else if (i < 2 * (selBuilding.z1 + selBuilding.z2)) {
                                                Test.X--;
                                                Test.Y++;
                                            } else {
                                                break;
                                            }
                                            if (Omg.getDistance(Test) <= bui.getRange()) {
                                                inreichweite = true;
                                                break;
                                            }
                                        }
                                        if (inreichweite) {
                                            // Idle deaktivieren
                                            bui.getbehaviourC(10).deactivate();
                                            // Angriff an Server senden
                                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 47, bui.netID, selBuilding.netID, 0, 0));
                                        }
                                    }
                                } else if (recruits) {
                                    // Wegpunkt setzen - man darf das ins Graue tun...
                                    // ...kann es aber auch auf ein Ressourcengebäude oder auf Ressourcen tun, um die neuen Einheiten sofort ernten zu lassen
                                    if (selRessource != null) {
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 27, selected.get(0).netID, selRessource.netID, selField.width, selField.height));
                                    } else if (selBuilding != null && selBuilding.playerId == rgi.game.getOwnPlayer().playerId && selBuilding.maxIntra > 0) {
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 27, selected.get(0).netID, selBuilding.netID, selField.width, selField.height));
                                    } else {
                                        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 27, selected.get(0).netID, 0, selField.width, selField.height));
                                    }
                                }
                            }
                        } else {
                            // Ins schwarze geklickt, das ist nur für Einheiten zulässig:
                            if (selected.get(0).getClass().equals(Unit.class)) {
                                // Dahin laufen lassen
                                // Die Positionen der Einheiten freimachen, für Wegfindung, und wenn sie sich bewegen sind die felder eh frei
                                for (int a = 0; a < selected.size(); a++) {
                                    rgi.mapModule.setCollision(selected.get(a).position, collision.free);
                                }

                                for (int i = 0; i < selected.size(); i++) {
                                    Unit tmpUnit = (Unit) selected.get(i);
                                    Position target = new Position(selField.width, selField.height).aroundMe(i, rgi, 10000);
                                    tmpUnit.sendToPosition(target, rgi, true);
                                }
                            }
                        }

                    }
                });

                t.setDaemon(true);
                t.setName("InputHandler: RightClick");
                t.start();
            }

            return;
        } */

    private Unit identifyUnit(int x, int y) {
        // Identifizier eine Einheit anhand ihrer Koordinaten
        return rgi.rogGraphics.content.identifyUnit(x, y);
    }

    private Building identifyBuilding(int x, int y) {
        // Findet ein Gebäude anhand seiner Koordinaten
        return rgi.rogGraphics.content.identifyBuilding(x, y);
    }

    public CoRInput(ClientCore.InnerClient inner, org.newdawn.slick.Input inp) {
        rgi = inner;
        selected = new ArrayList<InteractableGameElement>();        //Die Liste der angewählten Einheiten initialisieren
        input = inp;
        scroll = new boolean[4];
        savedSelections = new GameObject[10][];
        igeList = new ArrayList<InteractableGameElement>();
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
        this.removeSpecialMode();
    }

    private List<InteractableGameElement> getBoxSelected(final int x, final int y) {
        // Wir wollen alle Einheiten im SelectionBox Feld.
        // Wir müssen die Ecken der SelectionShadows mit den Ecken der SelektionBox vergleichen
        LinkedList<InteractableGameElement> finalList = new LinkedList<InteractableGameElement>();
        Dimension finalDimension = new Dimension(x, y);
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
        // Felder im Rahmen berechnen & Dort anwählbare Elemente zur Liste hinzufügen
        Position eckeLO = new Position(boxselectionstart.width, boxselectionstart.height);
        Position eckeRU = new Position(finalDimension.width, finalDimension.height);
        for (int cx = eckeLO.getX(); cx <= eckeRU.getX(); cx++) {
            for (int cy = eckeLO.getY(); cy <= eckeRU.getY(); cy++) {
                List<InteractableGameElement> elems = selMap.getIGEsWithTeamAt(cx, cy, rgi.game.getOwnPlayer().playerId);
                for (InteractableGameElement elem : elems) {
                    if (!finalList.contains(elem)) {
                    }
                }
            }
        }

        // Nachbearbeiten.
        // Manche Objekte sind nicht zusammen mit anderen auswählbar. (Builindgs mit Units)
        // Sobald ein multi-fähiges auftaucht, alle nicht-multis rausschmeißen
        // Außerdem alle nicht-multis nach dem ersten nichtmulti entfernen
        int singleIndex = -1;
        boolean foundMulti = false;
        boolean killSingle = false;
        for (int i = 0; i < finalList.size(); i++) {
            InteractableGameElement elem = finalList.get(i);
            if (elem.isMultiSelectable()) {
                foundMulti = true;
                if (singleIndex != -1) {
                    killSingle = true;
                    // Single entfernen
                    finalList.remove(singleIndex);
                    i--;
                }
            } else {
                if (killSingle || singleIndex != -1) {
                    // Schon eins da? Dann das hier löschen
                    finalList.remove(i--);
                    continue;
                } else {
                    singleIndex = i;
                }
            }
        }
        // Fertig, zurückgeben
        return finalList;

    }

    public void startSelectionBox(int x, int y) {
        dragSelectionBox = true;
        boxselectionstart = new Dimension(x, y);
    }

    public void stopSelectionBox() {
        dragSelectionBox = false;
    }
}
