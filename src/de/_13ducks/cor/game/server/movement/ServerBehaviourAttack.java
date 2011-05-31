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
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;

/**
 * Das Server-Angriffsbehaviour
 * Jede Einheit hat ihr eigenes.
 * Läuft normalerweise immer und koordiniert alles, was mit Angriff zu tun hat.
 * Also sowohl das Suchen und Verfolgen, als auch das reine Kämpfen.
 * Dieses Behaviour muss über alle Änderungen des Bewegungsmodus informiert werden, damit es sich entsprechend verhalten kann.
 * Das Behaviour hat Grundlegend 2 Modi:
 * - Normal. Die Einheit kämpft gerade, läuft zu einem Kampf oder steht herum und hält nach feinden Ausschau.
 * - F-Mode. (Flucht/Focus). Die Einheit läuft ohne sich zu wehren auf ihr Ziel zu. Dieser Modus wird automatisch verlassen, 
 *              wenn das Ziel erreicht/besiegt ist. Vorher nicht.
 * Intern gibt es noch mehr Modi, die aber an dieser Stelle nicht genauer erläutert werden.
 * Default ist Normal-Searching
 */
public class ServerBehaviourAttack extends ServerBehaviour {
    
    /**
     * Der default-Zustand.
     * Die Einheit sucht nach Zielen in ihrer Umgebung.
     * Die Einheit tut dies normalerweise immer, auch beim normalen laufen.
     */
    private static final int MODE_SEARCHENEMY = 1;
    /**
     * Die Einheit kämpft gerade.
     * Konkret ist das Ziel in Reichweite, und die Einheit wartet darauf, erneut Schaden zufügen zu können.
     * In diesem Zustand steht die Einheit.
     * Ist das Ziel außer Reichweite, wird ein neues in Reichweite gesucht, wenn keins da ist, wird auf MODE_GOTO umgeschaltet und das Ziel verfolgt.
     */
    private static final int MODE_FIGHTING = 2;
    /**
     * Die Einheit kämpft gerade, läuft konkret gerade auf ihr Ziel zu.
     * Wechselt auf MODE_FIGHTING, wenn angekommen.
     * Die Einheit feuert aber auch in diesem Modus, kann also auch während Verfolgungen den Gegner angreiffen.
     * Die Einheit sucht in diesem Modus permanent alternative Ziele, die ohne weiteres Laufen verfügbar sind.
     * Sollte so eines gefunden werden, wird das Ziel gewechselt und auf MODE_FIGHTING umgeschaltet
     */
    private static final int MODE_GOTO = 3;
    /**
     * In diesem Modus setzt die Einheit alles daran, dieses Ziel anzugreiffen.
     * Das bedeutet: Sie verfolg diese Einheit, auch wenn andere in Reichweite wären.
     * Sie wehrt sich nicht, auch wenn die verfolgte Einheit weit weg ist.
     * Die Einheit wird dort hinlaufen und angreiffen, bis sie abgezogen wird.
     * Dieser Modus wechselt automatisch zurück zu MODE_SEARCHENEMY, wenn das Ziel besiegt ist.
     */
    private static final int FOCUS = 4;
    /**
     * Dies ist der Fluchtmodus. Die Einheit versucht, das Ziel um jeden Preis zu erreichen.
     * Die Einheit läuft nur direkt auf das Ziel zu und sucht auf dem Weg keine Ziele.
     * Die Einheit wird sich nicht verteidigen, wenn sie angegriffen wird.
     * Sobald das Ziel erreicht ist, schält die Einheit wieder auf MODE_SEARCHENEMY um.
     */
    private static final int FLEE = 5;
    /**
     * Dies ist "Stellung halten".
     * Die Einheit wird sich niemals vom Fleck bewegen, auch wenn sie von einem Ziel außer eigener Reichweite angegriffen wird.
     * Die Einheit wird sich nur wehren, wenn sie sich dafür nicht bewegen muss.
     * Dieser Modus wird nicht automatisch verlassen.
     */
    private static final int STAY_FIGHTING = 6;
    /**
     * In diesem Modus wird die Einheit sich weder bewegen, noch sich gegen angreifende Feinde zur Wehr setzen.
     * Dieser Modus wird nicht automatisch verlassen.
     */
    private static final int STAY_STILL = 7;

    public ServerBehaviourAttack(Unit caster, ServerCore.InnerServer inner) {
        super(inner, caster, 2, 5, true);
    }

    @Override
    public void execute() {
        
    }

    @Override
    public void activate() {
        // Ignore, dieses Behaviour wird nicht angehalten.
    }

    @Override
    public void deactivate() {
        // Ignore, dieses Behaviour wird nicht angehalten.
    }

    @Override
    public void gotSignal(byte[] packet) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }
}
