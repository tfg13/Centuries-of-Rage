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

import java.util.ArrayList;

/**
 * Hat was mit den Sprüchen im Nauptmenü zu tun
 * @author Johannes
 */
public class MenuSlogan {
    private long starttime;
    private String Slogan;
    private ArrayList<MenuSloganWord> Words = new ArrayList<MenuSloganWord>();
    private String Lkwpic;
    private String Wheelpic;

    // Konstruktor
    public MenuSlogan(long starttime, String Slogan) {
        Lkwpic = "img/mainmenu/lkw.png";
        Wheelpic = "img/mainmenu/rad.png";

        this.starttime = starttime;
        this.Slogan = Slogan;

        // Slogan in Worte aufteilen
        String[] WordWIP; // Array aus den einzelnen Wörtern, wird nur hier kurz benutzt
        WordWIP = Slogan.split("\\s+");
        for (int i = 0; i < WordWIP.length; i++) {
            // todo: vernünftige Werte übergeben
            Words.add(new MenuSloganWord(starttime, WordWIP[i]));
        }
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
}
