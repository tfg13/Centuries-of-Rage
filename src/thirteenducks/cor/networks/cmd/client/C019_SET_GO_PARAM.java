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
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Setzt Parameter von GameObjects.
 * Wichtig für Gebäude/Einheiten hinzufügen, da diese Anfangs z.B. noch ohne
 * Team sind.
 */
public class C019_SET_GO_PARAM extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Paramerter für RogGameObject ändern
        // Holen:
        GameObject o = rgi.mapModule.getGameObjectviaID(rgi.readInt(data, 1));
        if (o != null) {
            int newPlayerId = rgi.readInt(data, 2);
            if (newPlayerId != o.playerId) {
                o.playerId = newPlayerId;
                // Nach der playerId-Änderung wird ein Upgrade durchgeführt:
                o.performUpgrade(rgi, o.descTypeId);
                if (o.getClass().equals(Building.class)) {
                    if (!o.ready) {
                        o.visrange = 1;
                    }
                    rgi.rogGraphics.builingsChanged();
                }
            }
            o.hitpoints = rgi.readInt(data, 3);
            if (o.getClass().equals(Unit.class) && !((Unit) o).isCompletelyActivated) {
                ((Unit) o).isCompletelyActivated = true;
            }
        } else {
            System.out.println("FixMe: Invalid ID (cmd19)");
        }
    }
}
