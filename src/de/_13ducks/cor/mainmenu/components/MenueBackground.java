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
import java.util.ArrayList;
import java.util.HashMap;
import org.newdawn.slick.Graphics;

/**
 * Zeichnet den Hintergrund für das Hauptmenü
 * @author Johannes
 */
public class MenueBackground extends Component {

    HashMap<String, GraphicsImage> imgMap;
    long starttime;
    long nextspawntime; // Wann wird ein neues BackgroundObj gespawnt?
    int resx; // Auflösung X
    int resy; // Auflösung Y
    int tilex; // Anzahl notwendiger Bodentexturkacheln X
    int tiley; // Anzahl notwendiger Bodentexturkacheln Y
    final static double speed = 0.04; // Geschwindikeit des Hintergrunds
    ArrayList<MenueBackgroundObject> BackgroundObj = new ArrayList<MenueBackgroundObject>();

    public MenueBackground(MainMenu m, double relX, double relY, double relWidth, double relHeigth, HashMap<String, GraphicsImage> imgMap) {
	super(m, relX, relY, relWidth, relHeigth);
	this.imgMap = imgMap;
	starttime = System.currentTimeMillis();
	nextspawntime = 0;
	resx = m.getResX();
	resy = m.getResY();
	tilex = (int) Math.ceil(resx / 100) + 2;
	tiley = (int) Math.ceil(resy / 100);
	//BackgroundObj.add(new MenueBackgroundObject(resx, 100, "img/buildings/human_baracks_e1.png", (long) 0));
    }

    @Override
    public void render(Graphics g) {

	long time = System.currentTimeMillis() - starttime; // Berechnet Zeit seit Start in Millisekunden

	// Bodentexturen zeichnen
	for (int i = 0; i <= tilex; i++) {
	    for (int j = 0; j <= tiley; j++) {
		imgMap.get("img/ground/menueground.png").getImage().draw((float) (i * 100 - (time % (100 / speed)) * speed), (float) j * 100);
	    }
	}

	// Background-Objekte zeichnen / entfernen
	for (int i = 0; i < BackgroundObj.size(); i++) {
	    if ((time - BackgroundObj.get(i).getStarttime()) * speed > resx + 300) {
		// Entfernen, wenn es am linken Bildschirmrand ist
		BackgroundObj.remove(i);
		i--;
	    } else {
		// Zeichnen
		imgMap.get(BackgroundObj.get(i).getPic()).getImage().draw((float) (resx - (speed * (time - BackgroundObj.get(i).getStarttime()))), BackgroundObj.get(i).getY());
	    }
	}

	// Neue Background-Objekte zufällig erstellen
	if (time > nextspawntime) {
	    nextspawntime = (long) (time + Math.random() * 15000);
	    double bla = Math.random();
	    BackgroundObj.add(new MenueBackgroundObject(resx, (int) (Math.random() * resy), "img/buildings/human_baracks_e1.png", time));
	}
    }
}
