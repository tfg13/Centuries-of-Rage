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
package de._13ducks.cor.debug;

import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.server.ServerCore;

/**
 * Diese Klasse ermöglicht dem Client direkten Zugriff auf den Server.
 * Dazu müssen beide in einer VM laufen.
 * Dieses Feature muss im Client und im Server aktiviert werden:
 * server_cfg.txt: ultimateDebug=true
 * client_cfg.txt: ultimateDebug=true
 * 
 * Der Client startet dann im UDB-Modus.
 * In diesem Modus werden z.B. direkt die Server-Variablen von Einheitenposition verwendet und das passende Client-Behaviour deaktiviert.
 */
public class UltimateDebug {

    private static UltimateDebug singleton;
    private static ServerCore.InnerServer server;
    private static boolean unlocked = false;

    /**
     * Utility-Class, kann von niemandem erstellt werden.
     */
    private UltimateDebug() {
    }

    /**
     * Liefert die UltimateDebug-Schnittstelle.
     * Server und Client bekommen die gleiche.
     * @return 
     */
    public static synchronized UltimateDebug getInstance() {
        if (singleton == null) {
            singleton = new UltimateDebug();
        }
        return singleton;
    }

    /**
     * Muss vom Server aufgerufen werden, um sich anzumelden und den UDB-Modus zu
     * authorisieren.
     * @param server Der ServerCore selbst.
     */
    public synchronized void authorizeDebug(ServerCore server, ServerCore.InnerServer inner) {
        if (server != null) {
            UltimateDebug.server = inner;
            System.out.println("[UDB]: Authorized by server, waiting for client...");
            notifyAll();
        }
    }

    /**
     * Hiermit kann sich der Client mit dem Server verbinden.
     * Blockt, bis die Verbindung möglich ist.
     * @param client
     * @return 
     */
    public synchronized boolean connect() {
        if (server != null) {
            System.out.println("[UDB]: Connecting client to server...");
            while (server == null) {
                try {
                    wait(1000);
                } catch (InterruptedException ex) {
                    return false;
                }
            }
            // Alles ok, los!
            unlocked = true;
            System.out.println("[UDB]: Client connected.");
            System.out.println("[UDB]: UltimateDebug(TM) up and running!");
            return true;
        }
        // Nur eine Verbindung
        return false;
    }

    public synchronized ServerCore.InnerServer getInnerServer() {
        if (unlocked) {
            return server;
        }
        System.out.println("[UDB]: Denied illegal access");
        throw new RuntimeException("Access denied.");
    }
}
