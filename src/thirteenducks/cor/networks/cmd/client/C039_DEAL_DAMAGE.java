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
import thirteenducks.cor.graphics.GraphicsBullet;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Dealt direkt Schaden oder spawnt ein Bullet, das dann Schaden dealt.
 */
public class C039_DEAL_DAMAGE extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        //Damage dealen, Bulletsystem.
        //Data: ATKID, VICID, DMG, DELAY (ms)
        //Attacker und Opfer suchen
        Unit atck39 = (Unit) rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
        if (atck39 != null) {
            GameObject vic39 = (GameObject) rgi.mapModule.getGameObjectviaID(rgi.readInt(data, 2));
            if (vic39 != null) {
                int dmg = rgi.readInt(data, 3);
                int delay = rgi.readInt(data, 4);
                // Neues Bullet erzeugen
                GraphicsBullet bullet = new GraphicsBullet(atck39, vic39, dmg, delay);
                rgi.rogGraphics.addBullet(bullet);
            } else {
                // Das ist kein Problem, diese Einheit ist halt schon tot.
            }
        } else {
            System.out.println("FixMe: Attacker ID mismatch (cmd39)");
        }
    }
}
