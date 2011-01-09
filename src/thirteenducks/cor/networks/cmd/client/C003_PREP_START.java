/*
 *  Copyright 2008, 2009, 2010:
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
package thirteenducks.cor.networks.cmd.client;

import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Bereitet den Start des Spiels vor.
 */
public class C003_PREP_START extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Start vorbereiten
        // Map geladen
        if (!rgi.mapModule.mapLoaded()) {
            System.out.println("CFixMe: Can't continue, Map isn't loaded");
            rgi.logger("[NetController]: Can't continue, Map isn't loaded");
            return;
        }
        if (rgi.readInt(data, 2) == 1) {
            // Cheats an
            rgi.setDebug(true);
        } else {
            rgi.setDebug(false);
        }
        rgi.game.prepareStart(rgi.readInt(data, 1));

        // KI initialisieren:
        if (rgi.isAIClient) {
            rgi.aiModule.initAIModule();
        }
    }
}
