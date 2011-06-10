/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de._13ducks.cor.game.client;

/**
 *
 * @author michael
 */
public class Client {
/**
     * der ServerCore
     */
    private static ClientCore.InnerClient ClientCore;

    public static void setInnerClient(ClientCore.InnerClient s) {
        Client.ClientCore = s;
    }

    public static ClientCore.InnerClient getInnerClient() {
        if (Client.ClientCore == null) {
            try {
                throw new Exception("ClientCore.InnerClient Reference has not been initialized!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        } else {
            return Client.ClientCore;
        }
    }
}
