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

import java.util.ArrayList;
import java.util.List;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.game.ability.Ability;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.graphics.input.InteractableGameElement;
import de._13ducks.cor.graphics.input.OverlayMouseListener;
import java.util.Iterator;

/**
 * Die Fähigkeiten-Anzeige des Huds
 */
public class AbilityHud implements Overlay, SlideInController {

    private SlideInOverlay slider;
    /**
     * Die größe der (quadratischen) Fähigkeiten-Icons
     */
    public static final int ICON_SIZE_XY = 60;
    private static final String tilemap = "img/hud/hud_tilemap_bronze.png";
    /**
     * Die IGE'S deren Fähigkeiten derzeit angezeigt werden.
     */
    private List<InteractableGameElement> iges;
    /**
     * Immer, wenn sich die IGE's ändern, wird diese Liste geupdated.
     * Hier sind alle Fähigkeiten enthalten, die angezeigt werden.
     * Es werden nur Fähigkeiten angezeigt, die alle IGE's haben (und die multi-fähig sind)
     */
    private List<Ability> abList;
    /**
     * Die Liste mit Elementen, die gerade auf den Bildschrim gezeichnet sind.
     */
    private List<Ability> drawList;
    /**
     * Änderungs-Indikator für iges-abList sync.
     */
    private boolean igesUpdated;
    /**
     * Die letzen Zeichenkoordinaten, für den MouseInput
     */
    private int[] coords;
    /**
     * Abstand vom linken Bildrand
     */
    private int leftSpace = 30;

    @Override
    public synchronized void renderOverlay(Graphics g, int fullResX, int fullResY) {
        if (iges != null && !iges.isEmpty()) {
            // Liste updaten?
            if (igesUpdated) {
                igesUpdated = false;
                // Nur Abilitys in die Liste aufnehmen, die alle IGEs haben
                abList = new ArrayList<Ability>(iges.get(0).getAbilitys());
                for (int i = 1; i < iges.size(); i++) {
                    List<Ability> abs = iges.get(i).getAbilitys();
                    for (int o = 0; o < abList.size(); o++) {
                        Ability ability = abList.get(o);
                        if (!(ability.useForAll && abs.contains(ability))) {
                            // Raus damit
                            abList.remove(o);
                            o--;
                        }
                    }
                }
            }
            drawList = computeDraw(abList);
            if (!drawList.isEmpty()) {
                // Balken links
                Renderer.drawImage(tilemap, leftSpace - 18, fullResY - 57, leftSpace + 1, fullResY, 363, 0, 377, 45);
                int i = 0;
                for (; i < drawList.size(); i++) {
                    Ability ab = drawList.get(i);
                    String tex = ab.symbols[0];
                    if (tex == null) {
                        tex = ab.symbols[1];
                    }
                    if (tex != null) {
                        boolean available = false;
                        // Fähigkeit ist anklickbar, wenns nur bei einem einzigen Verfügbar ist.
                        for (InteractableGameElement elem : iges) {
                            List<Ability> avabilitys = elem.getAbilitys();
                            if (avabilitys.get(avabilitys.indexOf(ab)).isAvailable()) {
                                available = true;
                                break;
                            }
                        }
                        if (i != 0) {
                            // Vor sich selbst, also beim ersten nicht
                            Renderer.drawImage(tilemap, leftSpace + i * (ICON_SIZE_XY + 14) - 14, fullResY - 80, leftSpace + i * (ICON_SIZE_XY + 14) + 2, fullResY, 285, 0, 297, 63);
                        }
                        Renderer.drawImage(tilemap, leftSpace + i * (ICON_SIZE_XY + 14), fullResY - ICON_SIZE_XY, leftSpace + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY, fullResY - ICON_SIZE_XY + ICON_SIZE_XY, 385, 15, 432, 62);
                        // Kachelbarer Oben eventuell mit Endstücken ersetzen
                        if (i == 0) {
                            Renderer.drawImage(tilemap, leftSpace + i * (ICON_SIZE_XY + 14), fullResY - ICON_SIZE_XY - 16, leftSpace + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY, fullResY - ICON_SIZE_XY + 1, 383, 0, 431, 13);
                        } else if (i == drawList.size() - 1) {
                            Renderer.drawImage(tilemap, leftSpace + i * (ICON_SIZE_XY + 14), fullResY - ICON_SIZE_XY - 16, leftSpace + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY, fullResY - ICON_SIZE_XY + 1, 383, 62, 431, 75);
                        } else {
                            Renderer.drawImage(tilemap, leftSpace + i * (ICON_SIZE_XY + 14), fullResY - ICON_SIZE_XY - 16, leftSpace + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY, fullResY - ICON_SIZE_XY + 1, 300, 0, 347, 13);
                        }
                        Renderer.drawImage(tex, leftSpace + i * (ICON_SIZE_XY + 14), fullResY - ICON_SIZE_XY, ICON_SIZE_XY, ICON_SIZE_XY, available ? Color.white : new Color(1f, 1f, 1f, 0.3f));
                    }
                }
                i--;
                // Balken rechts
                Renderer.drawImage(tilemap, leftSpace - 1 + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY, fullResY - 57, leftSpace + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY + 18, fullResY, 363, 46, 377, 90);
                // Die beiden Schrägen
                Renderer.drawImage(tilemap, leftSpace - 30, fullResY - 90, leftSpace + 27, fullResY - 32, 300, 18, 345, 64);
                Renderer.drawImage(tilemap, leftSpace + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY - 28, fullResY - 90, leftSpace + i * (ICON_SIZE_XY + 14) + ICON_SIZE_XY + 29, fullResY - 32, 300, 65, 345, 111);
            }
            updateCoords(fullResX, fullResY, drawList);
        }
    }

    /**
     * Muss beim Rendern aufgerufen werden, damit der selections-layer immer mit dem sichtbaren layer übereinstimmt.
     * Nicht wirklich schön.
     */
    private synchronized void updateCoords(int resX, int resY, List<Ability> abList) {
        if (!abList.isEmpty()) {
            coords[0] = leftSpace;
            coords[1] = resY - ICON_SIZE_XY;
            coords[2] = leftSpace + abList.size() * (ICON_SIZE_XY + 14) - 14;
            coords[3] = resY;
        } else {
            coords[0] = 0;
            coords[1] = 0;
            coords[2] = 0;
            coords[3] = 0;
        }
    }

    public synchronized void setActiveObjects(List<InteractableGameElement> elems) {
        if (elems == null || elems.isEmpty()) {
            if (slider.isOut()) {
                slider.slideIn();
            }
        } else {
            this.iges = new ArrayList<InteractableGameElement>(elems);
            igesUpdated = true;
            if (!slider.isOut()) {
                slider.slideOut();
            }
        }
    }

    private AbilityHud(ClientCore.InnerClient rgi) {
        coords = new int[4];
        rgi.rogGraphics.inputM.addOverlayMouseListener(new OverlayMouseListener() {

            @Override
            public int getCatch1X() {
                return coords[0];
            }

            @Override
            public int getCatch1Y() {
                return coords[1];
            }

            @Override
            public int getCatch2X() {
                return coords[2];
            }

            @Override
            public int getCatch2Y() {
                return coords[3];
            }

            @Override
            public void mouseMoved(int x, int y) {
                System.out.println(Math.random());
            }

            @Override
            public void mouseDragged(int x, int y) {
            }

            @Override
            public void mouseWheelMoved(int i) {
            }

            @Override
            public void mousePressed(int i, int i1, int i2) {
            }

            @Override
            public void mouseReleased(int i, int i1, int i2) {
                // Ability finden
                i1 -= leftSpace;
                int index = i1 / (ICON_SIZE_XY + 14);
                Ability ab = drawList.get(index);
                for (InteractableGameElement ige : iges) {
                    // Die zu dem Behaviour gehörende
                    ab = ige.getAbilitys().get(ige.getAbilitys().indexOf(ab));
                    // Diese hier!
                    if (i == 0) {
                        ab.perform(ige.getAbilityCaster());
                    } else {
                        ab.antiperform(ige.getAbilityCaster());
                    }
                }
            }
        });
    }

    public static AbilityHud createAbilityHud(ClientCore.InnerClient rgi) {
        return new AbilityHud(rgi);
    }

    private ArrayList<Ability> computeDraw(List<Ability> abList) {
        ArrayList<Ability> retList = new ArrayList<Ability>(abList);
        Iterator<Ability> iter = retList.iterator();
        while (iter.hasNext()) {
            Ability ab = iter.next();
            if (!ab.isVisible()) {
                iter.remove();
            }
        }
        return retList;
    }

    @Override
    public void addSlideIn(SlideInOverlay slider) {
        this.slider = slider;
    }
}
