/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.networks.cmd.client;

import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.networks.client.ClientNetController.ClientHandler;
import thirteenducks.cor.networks.cmd.ClientCommand;

/**
 * Ein spezielles Kommando, das einen sofort-Stop auf dem derzeitigen Weg ermöglicht
 */
public class C055_QUICKSTOP extends ClientCommand {

    @Override
    public void process(byte[] data, ClientHandler handler, InnerClient rgi) {
        Unit caster = rgi.mapModule.getUnitviaID(rgi.readInt(data, 1));
        if (caster != null) {
            caster.quickStop(rgi.readInt(data, 2), rgi.readPosition(data, 2));
        } else {
            System.out.println("Error: Unknown unit! (cmdc 55)");
        }
    }

}
