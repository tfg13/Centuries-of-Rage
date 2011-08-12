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
import org.newdawn.slick.Color;
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
     * Die letzen Zeichenkoordinaten, für den MouseInput, oberer Teil
     */
    private int[] coords1;
    /**
     * Die letzen Zeichenkoordinaten, für den MouseInput, unterer Teil
     */
    private int[] coords2;
    /**
     * Über was die Maus schwebt
     */
    private int hoverIndex = -1;
    /**
     * Wenn man die Maus über einer Schaltfläche gedrückt hält
     */
    private int pressedIndex = -1;

    public SelectionHud() {
        coords1 = new int[4];
        coords2 = new int[4];
        drawList = new ArrayList<SelectionClass>();
        Client.getInnerClient().rogGraphics.inputM.addOverlayMouseListener(new OverlayMouseListener() {

            @Override
            public int getCatch1X() {
                return coords1[0];
            }

            @Override
            public int getCatch1Y() {
                return coords1[1];
            }

            @Override
            public int getCatch2X() {
                return coords1[2];
            }

            @Override
            public int getCatch2Y() {
                return coords1[3];
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
                fMousePressed(i, i1, i2, false);
            }

            @Override
            public void mouseReleased(int i, int i1, int i2) {
                fMouseReleased(i, i1, i2, false);
            }

            @Override
            public void mouseRemoved() {
            }
        });

        Client.getInnerClient().rogGraphics.inputM.addOverlayMouseListener(new OverlayMouseListener() {

            @Override
            public int getCatch1X() {
                return coords2[0];
            }

            @Override
            public int getCatch1Y() {
                return coords2[1];
            }

            @Override
            public int getCatch2X() {
                return coords2[2];
            }

            @Override
            public int getCatch2Y() {
                return coords2[3];
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
                fMousePressed(i, i1, i2, true);
            }

            @Override
            public void mouseReleased(int i, int i1, int i2) {
                fMouseReleased(i, i1, i2, true);
            }

            @Override
            public void mouseRemoved() {
            }
        });
    }

    private void fMousePressed(int i, int i1, int i2, boolean singleSizeSelected) {
        int selection = findIndex(i2, singleSizeSelected);
        System.out.println("Mouse Pressed! " + i + " , " + i1 + " , " + i2 + " Sel: " + selection);
    }

    private void fMouseReleased(int i, int i1, int i2, boolean singleSizeSelected) {
        int selection = findIndex(i2, singleSizeSelected);
        System.out.println("Mouse Released! " + i + " , " + i1 + " , " + i2 + " Sel: " + selection);
    }

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
        if (igesUpdated) {
            igesUpdated = false;
            drawList = computeDrawList();
            updateCoords(fullResX, fullResY);
        }

        if (!drawList.isEmpty()) {

            String img = drawList.get(0).elems.get(0).getSelectionTexture();
            g.setColor(Color.black);
            if (img != null) {
                Renderer.drawImage(tilemap, 0, fullResY - singleSize, singleSize, fullResY, 277, 0, 347, 70);
                Renderer.drawImage(img, 0, fullResY - singleSize, singleSize, singleSize);

                int selectioncount = drawList.get(0).elems.size();
                if (selectioncount > 1) {
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
                    int selectioncount = drawList.get(0).elems.size();
                    if (selectioncount > 1) {
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
            coords1[0] = 0;
            coords1[1] = resY - singleSize - ((drawList.size() - 1) * (otherSize + 12));
            coords1[2] = otherSize;
            coords1[3] = resY - singleSize;
            coords2[0] = 0;
            coords2[1] = resY - singleSize;
            coords2[2] = singleSize;
            coords2[3] = resY;
        } else {
            coords1[0] = 0;
            coords1[1] = 0;
            coords1[2] = 0;
            coords1[3] = 0;
            coords2[0] = 0;
            coords2[1] = 0;
            coords2[2] = 0;
            coords2[3] = 0;
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

    private int findIndex(int y, boolean singleSizeSelected) {
        // Ability finden

        if (singleSizeSelected) {
            // Großer Knopf gedrückt
            return 0;
        } else {
            // Berechnen, welcher kleine Knopf gedrückt wurde
            double index = 1.0 * y / (otherSize + 12);
            int intIndex = (int) index;
            index -= intIndex;
            if (index <= .8) {
                int actualbutton = drawList.size() - intIndex - 1;
                return actualbutton;
            } else {
                return -1;
            }
        }
    }
}
