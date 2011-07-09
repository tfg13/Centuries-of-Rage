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

import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.client.Client;
import de._13ducks.cor.game.client.ClientCore.InnerClient;
import de._13ducks.cor.networks.client.ClientNetController.ClientHandler;
import de._13ducks.cor.networks.client.behaviour.impl.ClientBehaviourProduce;
import de._13ducks.cor.networks.cmd.ClientCommand;

/**
 * Gebäude übernehmen
 */
public class C057_CAPTURE extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
       int netid = rgi.readInt(data, 1); // Das Gebäude, das erobert wird
       float captureprogress = Float.intBitsToFloat(rgi.readInt(data, 2)); // aktueller Fortschritt
       int capturerate = rgi.readInt(data, 3); // Fortschritt pro Sekunde
       int playerid = rgi.readInt(data, 4); // Der Spieler, der erobert
       
       Building building = rgi.mapModule.getBuildingviaID(netid);
       if (building != null) {
           if (Float.isNaN(captureprogress)) {
               // bedeutet fertig erobert -> Gebäude kriegt neuen Besitzer
               Client.getInnerClient().mapModule.getBuildingviaID(netid).setPlayerId(playerid);
               Client.getInnerClient().mapModule.getBuildingviaID(netid).setCaptureprogress(0.0);
               ClientBehaviourProduce prod = new ClientBehaviourProduce(rgi, building);
               building.addClientBehaviour(prod);
           } else {
               // grafisch darstellen
               Client.getInnerClient().mapModule.getBuildingviaID(netid).setCaptureprogress(captureprogress);
           }
       } else {
           System.out.println("Panik! C057_CAPTURE pfuscht");
       }
       
    }
}
