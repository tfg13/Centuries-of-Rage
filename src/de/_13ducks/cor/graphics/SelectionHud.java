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

import de._13ducks.cor.game.client.Client;
import de._13ducks.cor.graphics.input.InteractableGameElement;
import de._13ducks.cor.graphics.input.OverlayMouseListener;
import java.util.ArrayList;
import java.util.List;
import org.newdawn.slick.Graphics;

/**
 * Das Hud, das die derzeit selektieren Einheiten anzeigt.
 */
public class SelectionHud implements Overlay, SlideInController {

    /**
     * Der Controller für die slide-Effekte
     */
    private SlideInOverlay slider;
    /**
     * Die aktuelle Tilemap, das "Design"
     */
    private static final String tilemap = "img/hud/hud_tilemap_bronze.png";
    /**
     * Größe des Haupt-Angewählten Elements.
     */
    private static final int singleSize = 70;
    /**
     * Größe der anderen angewählten
     */
    private static final int otherSize = 50;
    /**
     * Die IGE's, die gerade angezeigt werden.
     */
    private List<InteractableGameElement> iges;
    private List<SelectionClass> drawList;
    /**
     * Änderungs-Indikator für iges-abList sync.
     */
    private boolean igesUpdated;
    /**
     * Die letzen Zeichenkoordinaten, für den MouseInput
     */
    private int[] coords;

    public SelectionHud() {
        coords = new int[4];
        drawList = new ArrayList<SelectionClass>();
        Client.getInnerClient().rogGraphics.inputM.addOverlayMouseListener(new OverlayMouseListener() {

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
                System.out.println("AddMe: Gotklick!");
            }

            @Override
            public void mouseRemoved() {
            }
        });
    }

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
        if (igesUpdated) {
            igesUpdated = false;
            drawList = computeDrawList();
        }

        if (!drawList.isEmpty()) {

            String img = drawList.get(0).elems.get(0).getSelectionTexture();
            if (img != null) {
                Renderer.drawImage(tilemap, 0, fullResY - singleSize, singleSize, fullResY, 277, 0, 347, 70);
                Renderer.drawImage(img, 0, fullResY - singleSize, singleSize, singleSize);
                
                if (drawList.get(0).elems.get(0).getAbilityCaster().isMultiSelectable()) {
                    g.drawString("" + drawList.get(0).elems.size(), 0, fullResY - singleSize);
                }
                Renderer.drawImage(tilemap, 70, fullResY - 84, 82, fullResY, 416, 60, 428, 144);
                Renderer.drawImage(tilemap, 0, fullResY - 82, 84, fullResY - 70, 277, 122, 361, 134);
            }

            for (int i = 1; i < drawList.size(); i++) {

                String img2 = drawList.get(i).elems.get(0).getSelectionTexture();
                if (img2 != null) {
                    Renderer.drawImage(tilemap, 0, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 12, otherSize, fullResY - 12 - singleSize - (i * (otherSize + 12)) + otherSize + 12, 277, 0, 327, 50);
                    Renderer.drawImage(img2, 0, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 12, otherSize, otherSize);
                    if (drawList.get(i).elems.get(0).getAbilityCaster().isMultiSelectable()) {
                        g.drawString("" + drawList.get(i).elems.size(), 0, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 12);
                    }
                    if (i != drawList.size() - 1) {
                        Renderer.drawImage(tilemap, otherSize, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 12, otherSize + 12, fullResY - 12 - singleSize - (i * (otherSize + 12)) + otherSize + 12, 277, 70, 289, 120);
                        Renderer.drawImage(tilemap, 0, fullResY - 12 - singleSize - (i * (otherSize + 12)), 65, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 12, 351, 70, 416, 82);
                    } else {
                        Renderer.drawImage(tilemap, otherSize, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 12, otherSize + 12, fullResY - 12 - singleSize - (i * (otherSize + 12)) + otherSize + 12, 289, 70, 301, 120);
                        Renderer.drawImage(tilemap, 0, fullResY - 12 - singleSize - (i * (otherSize + 12)), otherSize, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 12, 301, 70, 351, 82);
                        Renderer.drawImage(tilemap, 29, fullResY - 12 - singleSize - (i * (otherSize + 12)) - 8, 69, fullResY - 12 - singleSize - (i * (otherSize + 12)) + 31, 301, 82, 341, 122);
                    }
                }
            }
        }
        updateCoords(fullResX, fullResY);
    }

    @Override
    public void addSlideIn(SlideInOverlay slider) {
        this.slider = slider;
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

    /**
     * Muss beim Rendern aufgerufen werden, damit der selections-layer immer mit dem sichtbaren layer übereinstimmt.
     * Nicht wirklich schön.
     */
    private synchronized void updateCoords(int resX, int resY) {
        if (!drawList.isEmpty()) {
            coords[0] = 0;
            coords[1] = resY - singleSize + ((drawList.size() - 1) * otherSize);
            coords[2] = singleSize;
            coords[3] = 0;
        } else {
            coords[0] = 0;
            coords[1] = 0;
            coords[2] = 0;
            coords[3] = 0;
        }
    }

    private List<SelectionClass> computeDrawList() {
        ArrayList<SelectionClass> retList = new ArrayList<SelectionClass>();
        
        for (InteractableGameElement elem : iges) {
            // Haben wir das schon?
            SelectionClass selClass = null;
            for (SelectionClass cla : retList) {
                if (cla.elems.get(0).getAbilityCaster().getDescTypeId() == elem.getAbilityCaster().getDescTypeId()) {
                    selClass = cla;
                    break;
                }
            }
            if (selClass == null) {
                selClass = new SelectionClass();
                retList.add(selClass);
            }
            selClass.elems.add(elem);
        }
        
        return retList;
    }

    private class SelectionClass {

        ArrayList<InteractableGameElement> elems;
        
        private SelectionClass() {
            elems = new ArrayList<InteractableGameElement>();
        }
    }
}
