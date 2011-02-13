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

package thirteenducks.cor.game;

import java.util.List;
import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;

/**
 * BehaviourProcessors haben Behaviours, die von der Spiellogik regelmäßig aufgerufen werden können.
 * Ein BehaviourProcessor ist z.B. eine Einheit mit ihren Verhalten.
 *
 * @author tfg
 */
public interface ClientBehaviourProcessor {

    /**
     * Findet heraus, ob dieser BehaviourProcessor Client-Behaviour hat (NICHT, ob diese ausgeführt werden sollen etc.)
     * @return true, wenn mindestens ein ClientBehaviour vorhanden ist.
     */
    public boolean gotClientBehaviours();
    /**
     * Findet heraus, ob dieser BehaviourProcessor Server-Behaviour hat (NICHT, ob diese ausgeführt werden sollen etc.)
     * @return true, wenn mindestens ein ServerBehaviour vorhanden ist.
     */
    public boolean gotServerBehaviours();
    /**
     * Holt eine Liste aller ClientBehaviour
     * @return eine Liste aller ClientBehaviour
     */
    public List<ClientBehaviour> getClientBehaviours();
    /**
     * Holt eine Liste aller ServerBehaviour
     * @return eine Liste aller ServerBehaviour
     */
    public List<ServerBehaviour> getServerBehaviours();
    /**
     * Führt alle derzeitig aktiven Behaviours aus.
     * Verwaltet das Timing selbstständig.
     */
    public void process();

    /**
     * Wird für (Un-)Pause aufgerufen
     * @param pause true bedeutet, dass vom laufenden Spiel in PAUSE gewechselt wird.
     */
    public void managePause(boolean pause);

}
