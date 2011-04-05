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

import de._13ducks.cor.game.Pauseable;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.game.GameObject;

/**
 * Ein Behaviour für Units und Gebäude
 *
 * Ids:
 *
 * 1 - Move - Einheitenbewegung
 * 2 - Attack - Grundsätzliche Angriffe
 * 3
 * 4
 * 5 - Construct - Gebäude bauen
 * 6 - Recruit - Einheiten ausbilden
 * 7 - Heal
 *
 * @author tfg
 */
public abstract class ServerBehaviour implements Pauseable {

    private final int id;       // Nummer (Art) des Behaviours. Viel schneller als die alten Strings.
    protected long nextUse; // Wann das Behaviour das nächste mal Verwendet werden soll
    protected int delay;    // Der Abstand zwischen den Aufrufen
    public boolean active; // Nur aktive (true) behaviour werden ausgeführt
    public ServerCore.InnerServer rgi; // Referenz auf alles, damit können Bahaviours auch alles machen
    public GameObject caster;

    /**
     * Konstruktor, muss implementiert werden
     * @param newinner
     */
    public ServerBehaviour(ServerCore.InnerServer newinner, GameObject caster, int ID, int callsPerSecond, boolean createAsActive) {
        rgi = newinner;
        id = ID;
        delay = 1000 / callsPerSecond;
        nextUse = System.currentTimeMillis() + delay;
        active = createAsActive;
        this.caster = caster;
    }

    /**
     * Konstruktor, muss implementiert werden
     * @param newinner
     */
    public ServerBehaviour(ServerCore.InnerServer newinner, GameObject caster, int ID, double callsPerSecond, boolean createAsActive) {
        rgi = newinner;
        id = ID;
        delay = (int) (1000 / callsPerSecond);
        nextUse = System.currentTimeMillis() + delay;
        active = createAsActive;
        this.caster = caster;
    }

    /**
     * @return Int: Die id des Behaviours.
     */
    public int getId() {
        return id;
    }

    /**
     * Startet die Ausführung des Behaviours.
     */
    public abstract void activate();

    /**
     * Stoppt die Ausführung des Behaviours.
     */
    public abstract void deactivate();

    /**
     * Teilt mit, ob das Behaviour active ist.
     * @return Boolean: true, wenn aktive
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Versucht, das Behaviour auszuführen.
     * Es wird nur ausgeführt, wenn der Countdown bereits abgelaufen ist,
     * ansonsten kehrt diese Methode sofort zurück
     */
    public void tryexecute() {
        if (System.currentTimeMillis() < nextUse) {
            return;
        }

        // Timer neu setzen
        nextUse = System.currentTimeMillis() + delay;

        // Ausführen
        try {
            this.execute();
        } catch (Exception ex) {
            // Damit der Server nicht abstürzt!
            ex.printStackTrace();
        }
    }

    /**
     * Führt das Behaviour aus. Sollte *nicht* direkt aufgerufen werden.
     * Wird automatisch aufgerufen, wenn der Cooldown abläuft.
     * Alles, was das Behaviour dann tun soll muss hier rein.
     *
     */
    public abstract void execute();

    /**
     * Verarbeitet die Signale, die dieses Behaviour betreffend empfangen werden.
     */
    public abstract void gotSignal(byte[] packet);
}
