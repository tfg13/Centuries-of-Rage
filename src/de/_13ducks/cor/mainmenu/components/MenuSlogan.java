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
import java.util.ArrayList;
import org.newdawn.slick.Font;

/**
 * Hat was mit den Sprüchen im Nauptmenü zu tun
 * @author Johannes
 */
public class MenuSlogan {
    private long starttime;
    private String Slogan;
    private ArrayList<MenuSloganWord> Words = new ArrayList<MenuSloganWord>();
    private String Lkwpic;
    private String Wagonpic;
    private String Wheelpic;
    private String Barpic; // Deichsel-Bild
    private int endofslogan; // Letzter Pixel in x-Richtung

    // Konstruktor
    public MenuSlogan(long starttime, String Slogan) {
        Lkwpic = "img/mainmenu/lkw.png";
        Wagonpic = "img/mainmenu/kachelbar.png";
        Wheelpic = "img/mainmenu/rad.png";
        Barpic ="img/mainmenu/deichsel.png";

        this.starttime = starttime;
        this.Slogan = Slogan;
        System.out.println("SLOGAN: " + Slogan);

        // Slogan in Worte aufteilen
        String[] WordWIP; // Array aus den einzelnen Wörtern, wird nur hier kurz benutzt
        WordWIP = Slogan.split("\\s+");
        Font bla = FontManager.getFont0();
        int currentwagonpos = 243 + 33;
        for (int i = 0; i < WordWIP.length; i++) {
            int wordlength = bla.getWidth(WordWIP[i]);
            int wagonlength = (int) Math.ceil((double) wordlength / 59);
            Words.add(new MenuSloganWord(starttime, WordWIP[i], wordlength, wagonlength, currentwagonpos));
            currentwagonpos += wagonlength * 59 + 33;
        }
        endofslogan = currentwagonpos; // Hier ist der Slogan zu Ende
    }

    /**
     * @return the Slogan
     */
    public String getSlogan() {
        return Slogan;
    }

    /**
     * @return the starttime
     */
    public long getStarttime() {
        return starttime;
    }

    /**
     * @return the Words
     */
    public ArrayList<MenuSloganWord> getWords() {
        return Words;
    }

     /**
     * @return the Lkwpic
     */
    public String getLkwpic() {
        return Lkwpic;
    }

     /**
     * @return the Wheelpic
     */
    public String getWheelpic() {
        return Wheelpic;
    }

    /**
     * @param starttime the starttime to set
     */
    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    /**
     * @param Slogan the Slogan to set
     */
    public void setSlogan(String Slogan) {
        this.Slogan = Slogan;
    }

    /**
     * @param Lkwpic the Lkwpic to set
     */
    public void setLkwpic(String Lkwpic) {
        this.Lkwpic = Lkwpic;
    }

    /**
     * @return the Wagonpic
     */
    public String getWagonpic() {
        return Wagonpic;
    }

    /**
     * @param Wagonpic the Wagonpic to set
     */
    public void setWagonpic(String Wagonpic) {
        this.Wagonpic = Wagonpic;
    }

    /**
     * @param Wheelpic the Wheelpic to set
     */
    public void setWheelpic(String Wheelpic) {
        this.Wheelpic = Wheelpic;
    }

    /**
     * @return the Barpic
     */
    public String getBarpic() {
        return Barpic;
    }

    /**
     * @param Barpic the Barpic to set
     */
    public void setBarpic(String Barpic) {
        this.Barpic = Barpic;
    }

    /**
     * @param Words the Words to set
     */
    public void setWords(ArrayList<MenuSloganWord> Words) {
        this.Words = Words;
    }

    /**
     * @return the endofslogan
     */
    public int getEndofslogan() {
        return endofslogan;
    }

    /**
     * @param endofslogan the endofslogan to set
     */
    public void setEndofslogan(int endofslogan) {
        this.endofslogan = endofslogan;
    }
}
