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
package thirteenducks.cor.mainmenu;

import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.ScaledImage;

/**
 * Die Lobby
 * hier werde nTeams, Servereinstellungen, Map etc festgelegt und das Spiel gestartet
 * @author michael
 */
public class LobbyScreen extends Container {

    /**
     * Konstruktor
     * @param m - Hauptmenü-Referenz
     */
    public LobbyScreen(MainMenu m) {
        super(m, 1, 1, 100, 100);

        // Map-Preview
        // @TODO: Die startposition in diesem Preview auswählen
        super.addComponent(new ScaledImage(m, 70, 8, 28, 28, "/img/mainmenu/rost.png"));

    }
}
