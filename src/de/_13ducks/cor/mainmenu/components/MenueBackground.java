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
package de._13ducks.cor.mainmenu.components;

import de._13ducks.cor.graphics.GraphicsImage;
import de._13ducks.cor.mainmenu.MainMenu;
import java.util.HashMap;
import org.newdawn.slick.Graphics;

/**
 * Zeichnet den Hintergrund für das Hauptmenü
 * @author Johannes
 */
public class MenueBackground extends Component {

    HashMap<String, GraphicsImage> imgMap;
    long starttime;
    int resx; // Auflösung X
    int resy; // Auflösung Y
    int tilex; // Anzahl notwendiger Bodentexturkacheln X
    int tiley;// Anzahl notwendiger Bodentexturkacheln Y

    public MenueBackground(MainMenu m, double relX, double relY, double relWidth, double relHeigth, HashMap<String, GraphicsImage> imgMap) {
	super(m, relX, relY, relWidth, relHeigth);
	this.imgMap = imgMap;
	starttime = System.currentTimeMillis();
	resx = m.getResX();
	resy = m.getResY();
	tilex = (int) Math.ceil(resx / 100) + 2;
	tiley = (int) Math.ceil(resy / 100);
    }

    @Override
    public void render(Graphics g) {
	double speed = 0.04;
	long time = System.currentTimeMillis() - starttime; // Berechnet Zeit seit Start in Millisekunden

	for (int i = 0; i <= tilex; i++) {
	    for (int j = 0; j <= tiley; j++) {
		imgMap.get("img/ground/menueground.png").getImage().draw((float) (i * 100 - (time % (100 / speed)) * speed),(float) j * 100);
	    }
	}
	imgMap.get("img/buildings/human_baracks_e1.png").getImage().draw((float) (resx - (speed * (time % (resx * 2 / speed)))), 100.0f);

    }
}
