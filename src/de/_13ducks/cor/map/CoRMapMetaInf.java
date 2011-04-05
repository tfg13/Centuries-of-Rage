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
package de._13ducks.cor.map;

import java.io.Serializable;

/**
 * Enthält alle Metadaten einer Map
 */
public class CoRMapMetaInf implements Serializable {

    public static final int LAYOUT_ISLES = 1;
    public static final int LAYOUT_LAKE = 2;
    public static final int LAYOUT_FLAT = 3;
    public static final int LAYOUT_RIVER = 4;
    public static final int LAYOUT_OTHER = 5;
    public static final int THEME_DESERT = 10;
    public static final int THEME_TEMPERATE = 11;
    public static final int THEME_SNOW = 12;
    public static final int THEME_OTHER = 13;
    /**
     * Die maximale Spieleranzahl auf dieser Map
     */
    private int maxPlayers;
    /**
     * Die Größe dieser Map in Feldern in X-Richtung
     */
    private int sizeX;
    /**
     * Die Größe dieser Map in Feldern in Y-Richtung
     */
    private int sizeY;
    /**
     * Die Art der Spielwelt z.B. Inseln
     * Einer der Werte CoRMapMetaInf.LAYOUT_XXX
     */
    private int layout;
    /**
     * Das Theme der Spielwelt (typtischerweise die Vegetation)
     * Einer der Werte CoRMapMetaInf.THEME_XXX
     */
    private int theme;
    /**
     * Der Name der Map
     */
    private String name;
    /**
     * Eine Kurzbeschreibung in einer Zeile
     * Einziger Parameter, der leer sein darf
     */
    private String description;
    /**
     * Bestimmt, ob das Vorschaubild per default versteckt bleiben soll, z.B. für RandomMaps
     */
    private boolean hidePreview = false;

    /**
     * Erstellt eine Neue Metainformation
     * Alle Parameter außer description müssen angegeben werden und sinnvoll (siehe setter) sein.
     * Alle Parameter sind später veränderbar, müssen aber auch dann auf sinnvolle Werte gesetzt werden.
     * @param maxPlayers Anzahl freier Slots
     * @param sizeX Größe in X-Richtung (in Feldern)
     * @param sizeY Größe in Y-Richtung (in Feldern)
     * @param layout "Art" der Spielwelt (Inseln etc.)
     * @param theme Vegetationstyp der Inselwelt
     * @param name Name der Map
     * @param description Kurzbeschreibung in einer Zeile. Darf leer sein
     */
    public CoRMapMetaInf(int maxPlayers, int sizeX, int sizeY, int layout, int theme, String name, String description) {
        this(maxPlayers, sizeX, sizeY, layout, theme, name, description, false);
    }

    /**
     * Erstellt eine Neue Metainformation
     * Alle Parameter außer description müssen angegeben werden und sinnvoll (siehe setter) sein.
     * Alle Parameter sind später veränderbar, müssen aber auch dann auf sinnvolle Werte gesetzt werden.
     * @param maxPlayers Anzahl freier Slots
     * @param sizeX Größe in X-Richtung (in Feldern)
     * @param sizeY Größe in Y-Richtung (in Feldern)
     * @param layout "Art" der Spielwelt (Inseln etc.)
     * @param theme Vegetationstyp der Inselwelt
     * @param name Name der Map
     * @param description Kurzbeschreibung in einer Zeile. Darf leer sein
     * @param hidePreview Soll das Vorschaubild erstmal versteckt bleiben?
     */
    public CoRMapMetaInf(int maxPlayers, int sizeX, int sizeY, int layout, int theme, String name, String description, boolean hidePreview) {
        if (maxPlayers > 1) {
            this.maxPlayers = maxPlayers;
        }
        if (sizeX > 1) {
            this.sizeX = sizeX;
        }
        if (sizeY > 0) {
            this.sizeY = sizeY;
        }
        if (theme >= 10 && theme <= 13) {
            this.theme = theme;
        }
        if (layout >= 1 && layout <= 5) {
            this.layout = layout;
        }
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
        this.description = description;
        this.hidePreview = hidePreview;
    }

    /**
     * Gets the number of player slots
     * @return the maxPlayers
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Sets the number of Player slots.
     * Must be greater than one
     * @param maxPlayers the maxPlayers to set
     */
    public void setMaxPlayers(int maxPlayers) {
        if (maxPlayers > 1) {
            this.maxPlayers = maxPlayers;
        }
    }

    /**
     * Gets the x-size of the map.
     * @return the sizeX
     */
    public int getSizeX() {
        return sizeX;
    }

    /**
     * Sets the x-size of the map.
     * Size must be greater than zero
     * @param sizeX the sizeX to set
     */
    public void setSizeX(int sizeX) {
        if (sizeX > 1) {
            this.sizeX = sizeX;
        }
    }

    /**
     * Gets the y-size of the map.
     * @return the sizeY
     */
    public int getSizeY() {
        return sizeY;
    }

    /**
     * Sets the y-size of the map.
     * Size must be greater than zero
     * @param sizeY the sizeY to set
     */
    public void setSizeY(int sizeY) {
        if (sizeY > 0) {
            this.sizeY = sizeY;
        }
    }

    /**
     * Gets the Layout.
     * Layout is one of: CoRMapMetaInf.LAYOUT_XYZ
     * @return the layout
     */
    public int getLayout() {
        return layout;
    }

    /**
     * Sets the Layout.
     * Must be one of: CoRMapMetaInf.LAYOUT_XYZ
     * @param layout the layout to set
     */
    public void setLayout(int layout) {
        if (layout >= 1 && layout <= 5) {
            this.layout = layout;
        }
    }

    /**
     * Gets the theme.
     * The Theme is one of CoRMapMetaInf.THEME_XYZ
     * @return the theme
     */
    public int getTheme() {
        return theme;
    }

    /**
     * Sets the theme.
     * Must be one of CoRMapMetaInf.THEME_XYZ
     * @param theme the theme to set
     */
    public void setTheme(int theme) {
        if (theme >= 10 && theme <= 13) {
            this.theme = theme;
        }
    }

    /**
     * Get the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param name the name to set
     */
    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }

    /**
     * Get the description
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     * May be empty
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Finds out whether this map wants to hide its preview
     * @return true, if this map wants to hide its preview
     */
    public boolean hidesPreview() {
        return this.hidePreview;
    }

    /**
     * Setzt das Verstecken des Vorschaubilds
     * @param hidePreview
     */
    public void setHidePreview(boolean hidePreview) {
        this.hidePreview = hidePreview;
    }
}
