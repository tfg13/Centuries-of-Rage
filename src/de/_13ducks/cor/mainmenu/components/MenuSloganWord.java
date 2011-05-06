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
import org.newdawn.slick.Font;

/**
 *
 * @author Johannes
 */
public class MenuSloganWord {
    private long starttime;
    private int length;
    private int numberofpics;
    private int lastpicpos;
    private String Word;

    public MenuSloganWord(long starttime, String Word) {
        this.starttime = starttime;
        this.Word = Word;
    }

    /**
     * @return the starttime
     */
    public long getStarttime() {
        return starttime;
    }

    /**
     * @param starttime the starttime to set
     */
    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the numberofpics
     */
    public int getNumberofpics() {
        return numberofpics;
    }

    /**
     * @param numberofpics the numberofpics to set
     */
    public void setNumberofpics(int numberofpics) {
        this.numberofpics = numberofpics;
    }

    /**
     * @return the lastpicpos
     */
    public int getLastpicpos() {
        return lastpicpos;
    }

    /**
     * @param lastpicpos the lastpicpos to set
     */
    public void setLastpicpos(int lastpicpos) {
        this.lastpicpos = lastpicpos;
    }

    /**
     * @return the Word
     */
    public String getWord() {
        return Word;
    }

    /**
     * @param Word the Word to set
     */
    public void setWord(String Word) {
        this.Word = Word;
    }
}
