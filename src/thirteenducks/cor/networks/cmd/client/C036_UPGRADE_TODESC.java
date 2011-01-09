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
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Ein TODESC-Upgrade, das Einheiten komplett aufeine andere DESCID verschiebt,
 * soll ausgeführt werden.
 */
public class C036_UPGRADE_TODESC extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // toDesc Upgrade
        // GameObject suchen
        GameObject go36 = rgi.mapModule.getGameObjectviaID(rgi.readInt(data, 1));
        if (go36 != null) {
            int mode = rgi.readInt(data, 3);
            if (mode == 1) {
                // Self-Upgrade
                go36.performUpgrade(rgi, rgi.readInt(data, 4));
            } else {
                rgi.mapModule.manageComplexUpgrades(mode, go36, rgi.readInt(data, 2), rgi.readInt(data, 4), go36.playerId);
            }
        } else {
            System.out.println("FixMe: GameObject ID mismatch (cmd36)");
        }
    }
}
