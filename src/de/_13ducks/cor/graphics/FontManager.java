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

package de._13ducks.cor.graphics;

import java.util.ArrayList;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;

/**
 * Läd einmalig alle benötigten Fonts.
 * Fonts laden dauert sehr lange, das sollte zentral geschehen.
 * Bietet jedem die Möglichkeit, über statische Methoden mit den Fonts zu arbeiten
 */
public class FontManager {

    private static ArrayList<UnicodeFont> fonts;

    /**
     * Initialisiert alle Fonts.
     * Alle Font-Getter funktionieren erst, nachdem diese Methode abgearbeitet wurde.
     * @param resX die X-Auflösung
     * @param resY die Y-Auflösung
     */
    public static void initFonts(int resX, int resY) {
        fonts = new ArrayList<UnicodeFont>();
        try {
            UnicodeFont ubuntu1 = new UnicodeFont("misc/Ubuntu-R.ttf", (int) (resX * 0.016), false, false);
            ubuntu1.getEffects().add(new org.newdawn.slick.font.effects.ShadowEffect(java.awt.Color.BLACK, 1, 1, 1.0f));
            ubuntu1.getEffects().add(new org.newdawn.slick.font.effects.ColorEffect(new java.awt.Color(255, 255, 200)));
            ubuntu1.addAsciiGlyphs();
            ubuntu1.loadGlyphs();
            fonts.add(ubuntu1);
        } catch (SlickException ex) {
        }
    }

    public static UnicodeFont getFont0() {
        return fonts.get(0);
    }

    private FontManager() { // Privater Konstruktor, kann nicht aufgerufen werden.
    }

}
