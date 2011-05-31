/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.game.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Über diese statische Klasse können alle Klassen auf die Serverkomponenten zugreifen.
 * @author michael
 */
public class Server {

    /**
     * der ServerCore
     */
    private static ServerCore.InnerServer serverCore;

    public static void setInnerServer(ServerCore.InnerServer s) {
        Server.serverCore = s;
    }

    public static ServerCore.InnerServer getInnerServer() {
        if (Server.serverCore == null) {
            try {
                throw new Exception("ServerCore.InnerServer Reference has not been initialized!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        } else {
            return Server.serverCore;
        }
    }
}
