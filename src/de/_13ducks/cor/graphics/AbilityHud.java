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

/**
 * Die Fähigkeiten-Anzeige des Huds
 */
public class AbilityHud extends Overlay {

    /**
     * Die größe der (quadratischen) Fähigkeiten-Icons
     */
    public static final int ICON_SIZE_XY = 40;
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
     * Änderungs-Indikator für iges-abList sync.
     */
    private boolean igesUpdated;
    /**
     * Die letzen Zeichenkoordinaten, für den MouseInput
     */
    private int[] coords;

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
            if (abList != null) {
                int visCounter = 0;
                for (int i = 0; i < abList.size(); i++) {
                    Ability ab = abList.get(i);
                    if (ab.isVisible()) { // Ist global, muss nicht für jeden erfasst werden
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
                            Renderer.drawImage(tex, visCounter++ * ICON_SIZE_XY, fullResY - ICON_SIZE_XY, ICON_SIZE_XY, ICON_SIZE_XY, available ? Color.white : new Color(1f, 1f, 1f, 0.3f));
                        }
                    }
                }
                updateCoords(fullResX, fullResY, abList);
            } else {
                coords[0] = 0;
                coords[1] = 0;
                coords[2] = 0;
                coords[3] = 0;
            }
        }
    }

    /**
     * Muss beim Rendern aufgerufen werden, damit der selections-layer immer mit dem sichtbaren layer übereinstimmt.
     * Nicht wirklich schön.
     */
    private synchronized void updateCoords(int resX, int resY, List<Ability> abList) {
        coords[0] = 0;
        coords[1] = resY - ICON_SIZE_XY;
        coords[2] = abList.size() * ICON_SIZE_XY;
        coords[3] = resY;
    }

    public synchronized void setActiveObjects(List<InteractableGameElement> elems) {
        this.iges = elems;
        igesUpdated = true;
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
                int index = i1 / ICON_SIZE_XY;
                int counter = 0;
                for (int o = 0; o < abList.size(); o++) {
                    Ability ab = abList.get(o);
                    if (ab.isVisible()) {
                        if (counter++ == index) {
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

                    }
                }

            }
        });
    }

    public static AbilityHud createAbilityHud(ClientCore.InnerClient rgi) {
        return new AbilityHud(rgi);
    }
}
