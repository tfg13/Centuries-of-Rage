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
import de._13ducks.cor.graphics.Renderer;
import de._13ducks.cor.mainmenu.MainMenu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;

/**
 * Hat was mit den Sprüchen im Nauptmenü zu tun: rendert
 * @author Johannes
 */
public class MenuSlogans extends Component {

    long starttime;
    long nextspawntime; // Wann wird ein neues BackgroundObj gespawnt?
    int resx; // Auflösung X
    int resy; // Auflösung Y
    int tilex; // Anzahl notwendiger Bodentexturkacheln X
    int tiley; // Anzahl notwendiger Bodentexturkacheln Y
    String groundtex; // Gras / Sand / Wüste bei Bodentexturen
    ArrayList<MenuBackgroundObject> BackgroundObj = new ArrayList<MenuBackgroundObject>();
    final static double speed = 0.04; // Geschwindikeit des Hintergrunds
    final static double sloganspeed = 0.3; // Geschwindikeit des Slogans
    final static int maxspawndelay = 10000; // In welchen Zeitabständen Hintergrundobjekte erzeugt werden
    Slogan currentSlogan; // Der aktuelle Slogan
    long sloganspawntime; // Wann der aktuelle Slogan gestartet ist
    ArrayList<String> Slogans = new ArrayList<String>(); // Eine ArrayList mit den Slogans

    public MenuSlogans(MainMenu m, double relX, double relY, double relWidth, double relHeigth) {
        super(m, relX, relY, relWidth, relHeigth);
        starttime = System.currentTimeMillis();
        nextspawntime = 0;
        sloganspawntime = 1000;
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

        // Slogans einlesen
        File sloganFile = null;
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
        currentSlogan = new Slogan(0, Slogans.get((int) (Math.random() * Slogans.size())));
    }

    @Override
    public void render(Graphics g) {
        long time = System.currentTimeMillis() - starttime; // Berechnet Zeit seit Start in Millisekunden

        // Slogan-Lkw zeichnen

        int lkwheight = Renderer.getImageInfo(currentSlogan.getLkwpic()).getHeight();

        float lkwX = (float) (resx - (sloganspeed * (time - currentSlogan.getStarttime())));

        Renderer.drawImage(currentSlogan.getLkwpic(), lkwX, 0.86 * resy - lkwheight);
        Renderer.drawImage(currentSlogan.getBarpic(), lkwX + 210, (float) (0.86 * resy - lkwheight + 55));

        ArrayList<Float> wheelsX = new ArrayList<Float>(); // Positionen der Lkw-Räder
        wheelsX.add(lkwX);
        wheelsX.add(lkwX + 168);

        // Einzelne Worte zeichnen
        for (int i = 0; i < currentSlogan.getWords().size(); i++) {
            float wagonX = lkwX + currentSlogan.getWords().get(i).getWagonX();
            Renderer.drawImage(currentSlogan.getBarpic(), wagonX - 33, (float) (0.86 * resy - lkwheight + 55));
            for (int j = 0; j < currentSlogan.getWords().get(i).getNumberofpics(); j++) {
                float xpos = wagonX + j * 59;
                float ypos = (float) 0.86 * resy - lkwheight + 44;
                Renderer.drawImage(currentSlogan.getWagonpic(), xpos, ypos);
                if (j == 0 || j == currentSlogan.getWords().get(i).getNumberofpics() - 1) {
                    wheelsX.add(xpos + 6);
                }
            }

            Renderer.stopCaching();
            g.setFont(FontManager.getSloganFont());
            g.setColor(Color.black);

            // Text zentrieren
            float lastwagonend = wagonX + 59 * currentSlogan.getWords().get(i).getNumberofpics();
            float middleofwagons = (wagonX + lastwagonend) / 2;
            Font bla = FontManager.getSloganFont();
            int wordlength = bla.getWidth(currentSlogan.getWords().get(i).getWord());

            g.drawString(currentSlogan.getWords().get(i).getWord(), middleofwagons - (wordlength / 2), (float) 0.86 * resy + 44 - lkwheight - FontManager.getSloganFont().getAscent());
        }

        // Räder zeichnen
        final int wheelheight = 46;
        //Renderer.setImageRotation(currentSlogan.getWheelpic(), (-50 * sloganspeed));
        for (int i = 0; i < wheelsX.size(); i++) {
            Renderer.drawImage(currentSlogan.getWheelpic(), wheelsX.get(i), (float) (0.86 * resy - lkwheight + wheelheight));
        }

        // Slogan außerhalb des Bildes? -> Neuer Slogan
        if (lkwX + currentSlogan.getEndofslogan() < 0) {
            currentSlogan = new Slogan(time + sloganspawntime, Slogans.get((int) (Math.random() * Slogans.size())));
        }
    }
}
