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
package de._13ducks.cor.debug;

import de._13ducks.cor.graphics.FontManager;
import de._13ducks.cor.graphics.Overlay;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 * Eine kleine Erinnerung, dass UDB aktiviert ist.
 */
public class UDBOverlay extends Overlay {

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
        g.setColor(Color.darkGray);
        g.fillRect(fullResX * .25f, 0, fullResX * .15f, fullResY * .05f);
        g.setColor(Color.green);
        g.drawRect(fullResX * .25f, 0, fullResX * .15f, fullResY * .05f);
        g.drawRect(fullResX * .25f + 2, 2, fullResX * .15f - 4, fullResY * .05f - 4);
        g.setFont(FontManager.getFont0());
        g.drawString("UDB: ON", fullResX * .325f - FontManager.getFont0().getWidth("UDB: ON") / 2, fullResY * .025f - FontManager.getFont0().getLineHeight() / 2);
    }
    
}
