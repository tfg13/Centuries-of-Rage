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

/**
 * Ein Objekt, dass im Hauptmenü-Hintergrund gerendert wird
 * @author Johannes
 */
public class MenuBackgroundObject {

    private int x;
    private int y;
    private int width; // Breite des Objekts
    private int height; // Höhe des Objekts
    private String pic;
    private long starttime;

    //Konstruktor
    public MenuBackgroundObject(int x1, int y1, int width1, int height1, String pic1, long starttime1) {
        x = x1;
        y = y1;
        width = width1;
        height = height1;
        pic = pic1;
        starttime = starttime1;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the pic
     */
    public String getPic() {
        return pic;
    }

    /**
     * @param pic the pic to set
     */
    public void setPic(String pic) {
        this.pic = pic;
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
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }
}
