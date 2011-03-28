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
import thirteenducks.cor.mainmenu.components.Frame;
import thirteenducks.cor.mainmenu.components.Player;
import thirteenducks.cor.mainmenu.components.ScaledImage;
import thirteenducks.cor.mainmenu.components.TiledImage;

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
        super.addComponent(new TiledImage(m, 5, 5, 90, 90, "/img/mainmenu/rost.png"));

        // Spielerliste:
        super.addComponent(new Frame(m, 10, 10, 45, 60));

        // Map-preview:
        super.addComponent(new Frame(m, 60, 10, 30, 30));

        // Serveroptionen:
        super.addComponent(new Frame(m, 60, 75, 30, 15));

        // Chat:
        super.addComponent(new Frame(m, 10, 75, 45, 15));



        // Die Spielerplätze:
        super.addComponent(new Player(m, 11,11));
        super.addComponent(new Player(m, 11,18));
        super.addComponent(new Player(m, 11,25));
        super.addComponent(new Player(m, 11,32));
        super.addComponent(new Player(m, 11,39));

    }
}
