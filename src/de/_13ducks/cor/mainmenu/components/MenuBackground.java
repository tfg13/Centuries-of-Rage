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
import de._13ducks.cor.graphics.Renderer;
import de._13ducks.cor.mainmenu.MainMenu;
import java.util.ArrayList;
import java.util.HashMap;
import org.newdawn.slick.Graphics;

/**
 * Zeichnet den Hintergrund für das Hauptmenü
 * @author Johannes
 */

public class MenuBackground extends Component {
    long starttime;
    long nextspawntime; // Wann wird ein neues BackgroundObj gespawnt?
    int resx; // Auflösung X
    int resy; // Auflösung Y
    int tilex; // Anzahl notwendiger Bodentexturkacheln X
    int tiley; // Anzahl notwendiger Bodentexturkacheln Y
    String groundtex; // Gras / Sand / Wüste bei Bodentexturen
    ArrayList<MenuBackgroundObject> BackgroundObj = new ArrayList<MenuBackgroundObject>();
    final static double speed = 0.04; // Geschwindikeit des Hintergrunds
    final static int maxspawndelay = 10000; // In welchen Zeitabständen Hintergrundobjekte erzeugt werden

    public MenuBackground(MainMenu m, double relX, double relY, double relWidth, double relHeigth) {
        super(m, relX, relY, relWidth, relHeigth);
        starttime = System.currentTimeMillis();
        nextspawntime = 0;
        resx = m.getResX();
        resy = m.getResY();
        tilex = (int) Math.ceil(resx / 512) + 2;
        tiley = (int) Math.ceil(resy / 512);
        BackgroundObj.add(new MenuBackgroundObject((resx * 3 / 4), (resy / 2), 200, 160, "img/buildings/human_baracks_e1.png", (long) (-resx / 4 / speed)));

        // Zufällige Bodentextur wählen:
        int random = (int) (Math.random() * 3);
        if (random == 0) {
            groundtex = "img/ground/menuground.png";
        } else if (random == 1) {
            groundtex = "img/ground/menuground2.png";
        } else {
            groundtex = "img/ground/menuground3.png";
        }
    }

    @Override
    public void render(Graphics g) {
        long time = System.currentTimeMillis() - starttime; // Berechnet Zeit seit Start in Millisekunden

        // Bodentexturen zeichnen
        for (int i = 0; i <= tilex; i++) {
            for (int j = 0; j <= tiley; j++) {
                Renderer.drawImage(groundtex, (float) (i * 512 - (time % (512 / speed)) * speed), (float) j * 512);
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
                Renderer.drawImage(BackgroundObj.get(i).getPic(), (float) (resx - (speed * (time - BackgroundObj.get(i).getStarttime()))), BackgroundObj.get(i).getY());
            }
        }

        // Neue Background-Objekte zufällig erstellen
        if (time > nextspawntime) {
            nextspawntime = (long) (time + Math.random() * maxspawndelay);
            createNewBackgroundObject(time);
        }
    }

    private void createNewBackgroundObject(long time) {
        String picturepath;
        double randomnumber = Math.random();

        // zufällig Bild auswählen
        if (randomnumber < 0.04 && time > 30000) {
            picturepath = "img/creeps/testhuman2.png";
        } else if (randomnumber < 0.36) {
            picturepath = "img/buildings/human_house_e1.png";
        } else if (randomnumber < 0.68) {
            picturepath = "img/buildings/human_storage_e1.png";
        } else {
            picturepath = "img/buildings/human_baracks_e1.png";
        }

        // zufällig y-Position auswählen
        int y = (int) (Math.random() * resy);

        int height = Renderer.getImageInfo(picturepath).getHeight(); // Höhe vom Hintergrund-Objekt
        int width = Renderer.getImageInfo(picturepath).getWidth(); // s.o.

        // Überschneidet es sich mit anderen Bildern? -> Wird nicht erzeugt
        boolean everythingfine = true;
        for (int i = 0; i < BackgroundObj.size(); i++) {
            // Bild noch am rechten Rand?
            if ((time - BackgroundObj.get(i).getStarttime()) * speed <= BackgroundObj.get(i).getWidth()) {
                // Auf gleicher Höhe mit anderem Bild?
                if (y <= BackgroundObj.get(i).getY() + BackgroundObj.get(i).getHeight()) {
                    if (y + height >= BackgroundObj.get(i).getY()) {
                        everythingfine = false;
                    }
                }
            }
        }

        // Wenn es sich nicht überschneidet, wird es jetzt eingetragen
        if (everythingfine) {
            BackgroundObj.add(new MenuBackgroundObject(resx, y, width, height, picturepath, time));
        }
    }
}