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
package de._13ducks.cor.networks.server.behaviour;

import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.client.Client;
import de._13ducks.cor.game.server.Server;
import de._13ducks.cor.game.server.ServerCore;

/**
 *
 * @author 2nd
 */
public class ServerBehaviourProduce extends ServerBehaviour{

    GameObject caster2;
    boolean harvesting; // Während des Ernten wirds öfters aufgrufen
    double diff = 0;    // Es können pro Zyklus nur ganzzahlige Werte verarbeitet werden, hier wird die differenz gespeichert.
    
        public ServerBehaviourProduce(ServerCore.InnerServer newinner, GameObject caster) {
            super(newinner, caster, 7, 1, true);
        caster2 = caster;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void execute() {
        if (caster2.getPlayerId() > 0) {
            System.out.println("PlayerID " + caster2.getPlayerId() + " , " + caster2.getHarvests() + " , " + caster2.getHarvRate() + " , " + rgi.game.getPlayer(caster2.getPlayerId()).res1);
                // Ernten
                double add = caster2.getHarvRate() + diff; //exakt
                int number = (int) (caster2.getHarvRate() + diff); // ungefähr
                // Unterschied zwischen Soll und tatsächlichem Wert berechnen:
                diff = add - number; // Diff wird bei den nächsten iteration berücksichtigt
                // Ernten
		//rgi.serverstats.trackRes(caster2.getHarvests(), number);
                switch (caster2.getHarvests()) {
                    case 1:
                        rgi.game.getPlayer(caster2.getPlayerId()).res1 += number;
                        Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 58, caster2.getPlayerId(), (int) caster2.getHarvests() , Float.floatToIntBits((float) caster2.getHarvRate()), rgi.game.getPlayer(caster2.getPlayerId()).res1));
                        break;
                    case 2:
                        rgi.game.getPlayer(caster2.getPlayerId()).res2 += number;
                        Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 58, caster2.getPlayerId(), (int) caster2.getHarvests() , Float.floatToIntBits((float) caster2.getHarvRate()), rgi.game.getPlayer(caster2.getPlayerId()).res2));
                        break;
                    case 3:
                        rgi.game.getPlayer(caster2.getPlayerId()).res3 += number;
                        Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 58, caster2.getPlayerId(), (int) caster2.getHarvests() , Float.floatToIntBits((float) caster2.getHarvRate()), rgi.game.getPlayer(caster2.getPlayerId()).res3));
                        break;
                    case 4:
                        rgi.game.getPlayer(caster2.getPlayerId()).res4 += number;
                        Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 58, caster2.getPlayerId(), (int) caster2.getHarvests() , Float.floatToIntBits((float) caster2.getHarvRate()), rgi.game.getPlayer(caster2.getPlayerId()).res4));
                        break;
                    case 5:
                        rgi.game.getPlayer(caster2.getPlayerId()).res5 += number;
                        Server.getInnerServer().netctrl.broadcastDATA(Client.getInnerClient().packetFactory((byte) 58, caster2.getPlayerId(), (int) caster2.getHarvests() , Float.floatToIntBits((float) caster2.getHarvRate()), rgi.game.getPlayer(caster2.getPlayerId()).res5));
                        break;
                }
                
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
        throw new UnsupportedOperationException("The Voices are talking to me.");
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unpause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
