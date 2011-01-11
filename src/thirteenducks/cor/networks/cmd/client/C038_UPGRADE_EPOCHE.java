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
package thirteenducks.cor.networks.cmd.client;

import java.util.ArrayList;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Ein EPOCHE-Upgrade soll ausgeführt werden.
 */
public class C038_UPGRADE_EPOCHE extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        // Epochenupgrade
        // Ability suchen
        int playerId = rgi.readInt(data, 2);
        AbilityUpgrade up = (AbilityUpgrade) rgi.game.getPlayer(playerId).clientDescAbilities.get(rgi.readInt(data, 1));
        ArrayList<DeltaUpgradeParameter> deltas = new ArrayList<DeltaUpgradeParameter>(up.edelta.values());
        for (DeltaUpgradeParameter para : deltas) {
            if (para.global) {
                if (playerId == rgi.game.getOwnPlayer().playerId) {
                    rgi.mapModule.performGlobalUpgrade(para);
                }
            } else {
                if (para.modunit) {
                    // Vorhandene Units patchen
                    for (Unit unit38 : rgi.mapModule.unitList) {
                        if (unit38.descTypeId == para.moddesc && unit38.playerId == playerId) {
                            unit38.performDeltaUpgrade(rgi, para);
                        }
                    }
                    rgi.game.getPlayer(playerId).descUnit.get(para.moddesc).performDeltaUpgrade(rgi, para);
                } else {
                    for (Building building38 : rgi.mapModule.buildingList) {
                        if (building38.descTypeId == para.moddesc && building38.playerId == playerId) {
                            building38.performDeltaUpgrade(rgi, para);
                        }
                    }
                    rgi.game.getPlayer(playerId).descBuilding.get(para.moddesc).performDeltaUpgrade(rgi, para);
                }
            }
        }
        // Epoche upgraden. (Nur wenn toEpoche spezifiziert ist, ansonsten ist es ein multidelta!
        // Nur für uns selber interressant
        if (up.toEpoche != 0 && playerId == rgi.game.getOwnPlayer().playerId) {
            rgi.rogGraphics.content.epoche = up.toEpoche;
            rgi.rogGraphics.epocheChanged();
        }
    }
}
