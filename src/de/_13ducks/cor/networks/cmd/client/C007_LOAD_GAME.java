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
package de._13ducks.cor.networks.cmd.client;

import de._13ducks.cor.game.client.ClientCore.InnerClient;
import de._13ducks.cor.networks.client.ClientNetController.ClientHandler;
import de._13ducks.cor.networks.cmd.ClientCommand;

/**
 * Ladebildschirm einblenden (Grafik starten) und erste Teile laden.
 */
public class C007_LOAD_GAME extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Lobby verbergen
        rgi.netctrl.lobby.hideLobby();

        // Ladebildschirm einblenden
        rgi.rogGraphics.initGame();

        rgi.mapModule.initModule();
    }
}
