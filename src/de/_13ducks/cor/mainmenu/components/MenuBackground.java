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

import de._13ducks.cor.graphics.FontManager;
import de._13ducks.cor.graphics.GraphicsImage;
import de._13ducks.cor.mainmenu.MainMenu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

/**
 * Zeichnet den Hintergrund für das Hauptmenü
 * @author Johannes
 */

public class MenuBackground extends Component {
    HashMap<String, GraphicsImage> imgMap;
    long starttime;
    long nextspawntime; // Wann wird ein neues BackgroundObj gespawnt?
    int resx; // Auflösung X
    int resy; // Auflösung Y
    int tilex; // Anzahl notwendiger Bodentexturkacheln X
    int tiley; // Anzahl notwendiger Bodentexturkacheln Y
    String groundtex; // Gras / Sand / Wüste bei Bodentexturen
    ArrayList<MenuBackgroundObject> BackgroundObj = new ArrayList<MenuBackgroundObject>();
    final static double speed = 0.04; // Geschwindikeit des Hintergrunds
    final static double sloganspeed = 0.08; // Geschwindikeit des Slogans
    final static int maxspawndelay = 10000; // In welchen Zeitabständen Hintergrundobjekte erzeugt werden
    MenuSlogan currentSlogan; // Der aktuelle Slogan
    long sloganspawntime; // Wann der aktuelle Slogan gestartet ist

    public MenuBackground(MainMenu m, double relX, double relY, double relWidth, double relHeigth, HashMap<String, GraphicsImage> imgMap) {
        super(m, relX, relY, relWidth, relHeigth);
        this.imgMap = imgMap;
        starttime = System.currentTimeMillis();
        nextspawntime = 0;
        sloganspawntime = 100;
        resx = m.getResX();
        resy = m.getResY();
        tilex = (int) Math.ceil(resx / 512) + 2;
        tiley = (int) Math.ceil(resy / 512);
        BackgroundObj.add(new MenuBackgroundObject((int) (resx * 3 / 4), (int) (resy / 2), 200, 160, "img/buildings/human_baracks_e1.png", (long) (-resx / 4 / speed)));

        // Zufällige Bodentextur wählen:
        int random = (int) (Math.random() * 3);
        if (random == 0) {
            groundtex = "img/ground/menuground.png";
        } else if (random == 1) {
            groundtex = "img/ground/menuground2.png";
        } else {
            groundtex = "img/ground/menuground3.png";
        }

        // Slogans einlesen
        File sloganFile = null;
        ArrayList<String> Slogans = new ArrayList<String>(); // Eine ArrayList mit den Slogans
        try {
            sloganFile = new File("randomslogans");
            FileReader sloganReader = new FileReader(sloganFile);
            BufferedReader reader = new BufferedReader(sloganReader);
            String zeile = null;
            while ((zeile = reader.readLine()) != null) {
                // Liest Zeile fuer Zeile, speichert als String
                Slogans.add(zeile);
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            // cfg-Datei nicht gefunden -  egal, wird automatisch neu angelegt
            System.out.println("randomslogans not found, creating new one...");
            try {
                sloganFile.createNewFile();
            } catch (IOException ex) {
                System.out.println("[ERROR] Failed to create randomslogans.");
            }
        } catch (IOException e2) {
            // Inakzeptabel
            e2.printStackTrace();
            System.out.println("[ERROR] Critical I/O ERROR!");
            System.exit(1);
        }

        // 1. Slogan auswählen
        currentSlogan = new MenuSlogan(0, Slogans.get((int) (Math.random() * Slogans.size())));
    }

    @Override
    public void render(Graphics g) {
        long time = System.currentTimeMillis() - starttime; // Berechnet Zeit seit Start in Millisekunden

        // Bodentexturen zeichnen
        for (int i = 0; i <= tilex; i++) {
            for (int j = 0; j <= tiley; j++) {
                imgMap.get(groundtex).getImage().draw((float) (i * 512 - (time % (512 / speed)) * speed), (float) j * 512);
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

        // Slogan-Lkw zeichnen
        Image LkwImg = imgMap.get(currentSlogan.getLkwpic()).getImage();
        Image WagonImg = imgMap.get(currentSlogan.getWagonpic()).getImage();
        Image BarImg = imgMap.get(currentSlogan.getBarpic()).getImage();

        float lkwX = (float) (resx - (sloganspeed * time - currentSlogan.getStarttime()));
        LkwImg.draw(lkwX, (float) (0.86 * resy - LkwImg.getHeight()));
        BarImg.draw(lkwX + 210, (float) (0.86 * resy - LkwImg.getHeight() + 55));

        ArrayList<Float> wheelsX = new ArrayList<Float>(); // Positionen der Lkw-Räder
        wheelsX.add(lkwX);
        wheelsX.add(lkwX + 168);

        // Einzelne Worte zeichnen
        for (int i = 0; i < currentSlogan.getWords().size(); i++) {
            Font bla = FontManager.getFont0();
            float wagonX = lkwX + currentSlogan.getWords().get(i).getWagonX();
            BarImg.draw(wagonX - 33, (float) (0.86 * resy - LkwImg.getHeight() + 55));
            for (int j = 0; j < currentSlogan.getWords().get(i).getNumberofpics(); j++) {
                float xpos = wagonX + j * 59;
                float ypos = (float) 0.86 * resy - LkwImg.getHeight() + 44;
                WagonImg.draw(xpos, ypos);
                wheelsX.add(xpos + 6);
            }
            g.drawString(currentSlogan.getWords().get(i).getWord(), wagonX, (float) 0.86 * resy - LkwImg.getHeight() + 27);
        }

        // Räder zeichnen
        final int wheelheight = 46;
        Image WheelImg = imgMap.get(currentSlogan.getWheelpic()).getImage();
        WheelImg.rotate(-4);
        for (int i = 0; i < wheelsX.size(); i++) {
            WheelImg.draw(wheelsX.get(i), (float) (0.86 * resy - LkwImg.getHeight() + wheelheight));
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

        int height = imgMap.get(picturepath).getImage().getHeight(); // Höhe vom Hintergrund-Objekt
        int width = imgMap.get(picturepath).getImage().getWidth(); // s.o.

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