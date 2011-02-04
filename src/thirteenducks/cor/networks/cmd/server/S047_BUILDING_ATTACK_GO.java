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
package thirteenducks.cor.networks.cmd.server;

import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.ServerNetController.ServerHandler;
import thirteenducks.cor.networks.cmd.ServerCommand;

/**
 * Ein Gebäude soll etwas angreiffen
 */
public class S047_BUILDING_ATTACK_GO extends ServerCommand {

    @Override
    public void process(byte[] data, ServerHandler handler, InnerServer rgi) {
        //Gebäude schiesst
        Building b47 = rgi.netmap.getBuildingviaID(rgi.readInt(data, 1));
        if (b47 != null) {
            // Ziel suchen
            GameObject target47 = rgi.netmap.getGameObjectviaID(rgi.readInt(data, 2));
            if (target47 != null) {
                // Ist das eine Einheit?
                try {
                    Unit unittarget47 = (Unit) target47;
                    // Einheit angreiffen
                    b47.attackManager.attackUnit(unittarget47);
                } catch (ClassCastException ex47) {
                    // Nein, Gebäude?
                    try {
                        Building buildingtarget47 = (Building) target47;
                        b47.attackManager.attackBuilding(buildingtarget47);
                    } catch (ClassCastException ex) {
                        // Gar nix, Error
                        System.out.println("FixMe: Ziel ist nicht angreiffbar (cmd47), Ziel: " + target47);
                    }
                }
            } else {
                System.out.println("FixMe: GO-ID mismatch (cmd 47)");
            }
        } else {
            System.out.println("FixMe: Unit ID mismatch (cmd 47)");
        }
    }
}
