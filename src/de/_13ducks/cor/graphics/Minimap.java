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

import java.util.List;
import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.graphics.input.OverlayMouseListener;
import de._13ducks.cor.map.AbstractMapElement;

/**
 * Die Minimap.
 */
public class Minimap extends Overlay {

    /**
     * Die Map ist derzeit oben rechts.
     */
    public static final int EDGE_TOP_RIGHT = 0;
    /**
     * Die Map ist derzeit oben links.
     */
    public static final int EDGE_TOP_LEFT = 1;
    /**
     * Die Map ist derzeit unten rechts.
     */
    public static final int EDGE_BOTTOM_RIGHT = 2;
    /**
     * Die Map ist derzeit unten links.
     */
    public static final int EDGE_BOTTOM_LEFT = 3;
    /**
     * Die Default-Größe der Minimap.
     * Der Spieler kann die Größe der Minimap nachträglich ändern.
     * Dieser Wert wird mit der X-Auflösung multipliziert.
     */
    public static final double DEFAULT_SIZEFACTOR_X = 0.25;
    /**
     * Die Default-Größe der Minimap.
     * Der Spieler kann die Größe der Minimap nachträglich ändern.
     * Dieser Wert wird mit der Y-Auflösung multipliziert.
     */
    public static final double DEFAULT_SIZEFACTOR_Y = 0.25;
    /**
     * Das Bild der Minimap
     */
    private Image map;
    /**
     * Die aktuelle Größe der Minimap in X-Richtung
     */
    private int sizeX;
    /**
     * Die aktuelle Größe der Minimap in Y-Richtung
     */
    private int sizeY;
    /**
     * Die derzeitige Ecke
     */
    private int edge = 0;
    /**
     * Der derzeitig sichtbare Bereich.
     */
    private float[] view;
    /**
     * Die aktuelle Position der Minimap, alles absolute Koordinaten
     */
    private int[] pos;
    private boolean leftmouse = false; // MouseDragged nur mit Linksklick
    private List<Sprite> allList;
    private ClientCore.InnerClient rgi;

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
	int dx = pos[0];
	int dy = pos[1];
	// Rahmen
	g.setColor(Color.black);
	g.fillRect(dx - 4, dy - 4, sizeX + 8, sizeY + 8);
	map.draw(dx, dy, sizeX, sizeY);
	for (int i = 0; i < allList.size(); i++) {
	    Sprite sprite = allList.get(i);
	    sprite.renderMinimapMarker(g, dx + (int) (1.0 * sprite.getMainPositionForRenderOrigin().getX() / map.getWidth() * sizeX), dy + (int) (1.0 * sprite.getMainPositionForRenderOrigin().getY() / map.getHeight() * sizeY), rgi.game.getPlayer(sprite.getColorId()).color);
	}
	g.setColor(Color.lightGray);
	g.setLineWidth(1);
	g.drawRect(dx + view[0] * sizeX, dy + view[1] * sizeY, view[2] * sizeX, view[3] * sizeY);
    }

    private Minimap(int resX, int resY, ClientCore.InnerClient newinner) { // Konstruktor private, kann sonst niemand aufrufen
	sizeX = (int) (resX * DEFAULT_SIZEFACTOR_X);
	sizeY = (int) (resY * DEFAULT_SIZEFACTOR_Y);
	view = new float[4];
	pos = new int[4];
	rgi = newinner;
	switch (edge) {
	    case EDGE_TOP_LEFT:
		pos[0] = 0;
		pos[1] = 0;
		pos[2] = sizeX;
		pos[3] = sizeY;
                break;
	    case EDGE_TOP_RIGHT:
		pos[0] = resX - sizeX;
		pos[1] = 0;
		pos[2] = resX;
		pos[3] = sizeY;
		break;
	    case EDGE_BOTTOM_LEFT:
		pos[0] = 0;
		pos[1] = resY - sizeY;
		pos[2] = sizeX;
		pos[3] = resY;
		break;
	    case EDGE_BOTTOM_RIGHT:
		pos[0] = resX - sizeX;
		pos[1] = resY - sizeY;
		pos[2] = resX;
		pos[3] = resY;
		break;
	}
    }

    public void viewChanged(int posX, int posY, int viewX, int viewY, int sizeX, int sizeY) {
	// Koordinaten auf eigene Pixel umrechnen
	view[0] = 1.0f * posX / sizeX;
	view[1] = 1.0f * posY / sizeY;
	view[2] = 1.0f * viewX / sizeX;
	view[3] = 1.0f * viewY / sizeY;
    }

    public static Minimap createMinimap(AbstractMapElement[][] visMap, Map<String, GraphicsImage> imgMap, final int fullResX, final int fullResY, ClientCore.InnerClient rgi) {
	Minimap minimap = new Minimap(fullResX, fullResY, rgi);
	try {
	    // Erstellt einen neue Basis-Minimap
	    minimap.map = new Image(visMap.length, visMap[0].length);
	    Graphics tempGra = minimap.map.getGraphics();
	    // Skalierungsfaktor berechnen
	    for (int x = 0; x < visMap.length; x++) {
		for (int y = 0; y < visMap[0].length; y++) {
		    if ((x + y) % 2 == 1) {
			continue;
		    } else {
			try {
			    GraphicsImage tex = imgMap.get(visMap[x][y].getGround_tex());
			    if (tex != null) {
				Color pcol = tex.getImage().getColor(20, 20);
				tempGra.setColor(pcol);
				tempGra.fillRect(x, y, 2, 2);
			    }
			} catch (Exception ex) {
			    ex.printStackTrace();
			}
		    }
		}
	    }
	    tempGra.flush();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	minimap.initListener(rgi, visMap.length, visMap[0].length);
	return minimap;
    }

    private void initListener(final ClientCore.InnerClient rgi, final int mapX, final int mapY) {
	rgi.rogGraphics.inputM.addOverlayMouseListener(new OverlayMouseListener() {

	    @Override
	    public void mouseWheelMoved(int i) {
	    }

	    @Override
	    public void mousePressed(int i, int i1, int i2) {
		if (i == 0) {
		    // Linke Maustaste
		    leftmouse = true;
		} else if (i == 1) {
		    // Rechte Maustaste
		    leftmouse = false;
		}
	    }

	    @Override
	    public void mouseReleased(int i, int i1, int i2) {
		// Berechnung siehe bei mouseDragged
		if (i == 0) {
		    // Linke Maustaste -> Ansicht verschieben
		    rgi.rogGraphics.jumpTo((int) (((1.0 * i1 / sizeX) - (view[2] / 2)) * mapX), (int) (((1.0 * i2 / sizeY) - (view[3] / 2)) * mapY));
		} else if (i == 1) {
		    // Rechte Maustaste -> Einheiten bewegen
		    int kx = (int) ((1.0 * i1 / sizeX) * mapX);  // Auf gültiges Feld umrechnen
		    int ky = (int) ((1.0 * i2 / sizeY) * mapY);
		    if (kx < 0) {
			kx = 0;
		    } else if (kx >= mapX) {
			kx = mapX - 1;
		    }
		    if (ky < 0) {
			ky = 0;
		    } else if (ky >= mapY) {
			ky = mapY - 1;
		    }
		    if (kx % 2 != ky % 2) {
			kx -= 1;
			if (kx < 0) {
			    kx = 1;
			}
		    }
		    rgi.rogGraphics.inputM.mouseKlickedRight(1, kx, ky, true);
		}
	    }

	    @Override
	    public void mouseMoved(int x, int y) {
	    }

	    @Override
	    public void mouseDragged(int x, int y) {
		// Ausschnitt verschieben.
		// Berechnung: Koordinate / Länge = Positionsfaktor des Mittelpunkts. Minus halber Sichtbereich = Positionsfaktor oben links
		// Mal Map-Größe = Oben-Rechts-Jump-Koordinate
		if (leftmouse) {
		    rgi.rogGraphics.jumpTo((int) (((1.0 * x / sizeX) - (view[2] / 2)) * mapX), (int) (((1.0 * y / sizeY) - (view[3] / 2)) * mapY));
		}
	    }

	    @Override
	    public int getCatch1X() {
		return pos[0];
	    }

	    @Override
	    public int getCatch1Y() {
		return pos[1];
	    }

	    @Override
	    public int getCatch2X() {
		return pos[2];
	    }

	    @Override
	    public int getCatch2Y() {
		return pos[3];
	    }
	});
    }

    /**
     * @param allList the allList to set
     */
    public void setAllList(List<Sprite> allList) {
	this.allList = allList;
    }

    /**
     * @return the allList
     */
    public List<Sprite> getAllList() {
	return allList;
    }
}
