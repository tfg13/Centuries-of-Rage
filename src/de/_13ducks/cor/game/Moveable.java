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
package de._13ducks.cor.game;

import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.networks.behaviour.impl.ServerBehaviourMove;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.game.server.movement.FreePolygon;
import de._13ducks.cor.game.server.movement.GroupManager;
import de._13ducks.cor.game.server.movement.MovementMap;
import de._13ducks.cor.game.server.movement.ServerMoveManager;
import de._13ducks.cor.graphics.input.InteractableGameElement;
import de._13ducks.cor.networks.client.behaviour.impl.ClientBehaviourMove;
import java.util.List;

/**
 * Alles was Moveable ist, kann sich bewegen.
 */
public interface Moveable extends InteractableGameElement, Pauseable {
    
   /**
     * Stoppt die Einheit sofort - sofern genug Platz ist und die Einheit sich überhaupt bewegt.
     * Falls hier gerade kein Platz ist, wird die Einheit zur nächstmöglichen Position laufen.
     * Nur Client!
     */
    public void stopMovement(ClientCore.InnerClient rgi);

    /**
     * Findet heraus, ob die Einheit sich derzeit in einer Stoppbaren Bewegung befindet.
     * @return
     */
    public boolean moveStoppable();
    
        /**
     * Liefert den LowLevel-Manager dieses Objekts.
     * @return den Lowlevel-Manager dieses Objekts.
     */
    public ServerBehaviourMove getLowLevelManager();
    
    /**
     * Liefert den MidLevel-Manager dieses Objekts.
     * @return den Midlevel-Manager dieses Objekts.
     */
    public GroupManager getMidLevelManager();
    
    /**
     * Liefert den TopLevel-Manager dieses Objekts.
     * @return den TopLevel-Manager dieses Objekts.
     */
    public ServerMoveManager getTopLevelManager();
    
    /**
     * Bereitet die Einheit für das Server-Bewegungssystem vor.
     * Muss aufgerufen werden, bevor Bewegungsbefehle an die Einheit gesendet werden.
     * @param rgi der inner core
     * @param die movementMap
     */
    public void initServerMovementManagers(ServerCore.InnerServer rgi, MovementMap moveMap);
    
    /**
     * Bereitet die Einheit für das Client-Bewegungssystem vor.
     * Muss aufgerufen werden, bevor Bewegungsbefehle an die Einheit gesendet werden.
     * @param rgi der inner core
     */
    public void initClientMovementManager(ClientCore.InnerClient rgi);
    
        /**
     * Entfernt die Einheit aus ihrer aktuellen Gruppe, falls vorhanden.
     */
    public void removeFromCurrentGroup();

    /**
     * Setzt die aktuelle Gruppe der Einheit, falls sie derzeit keine hat.
     * Wenn eine da ist, wird diese nicht überschrieben!!!
     * @param man Die neue Gruppe
     */
    public void setCurrentGroup(GroupManager man);

    /**
     * Der ClientManager
     * @return the clientManager
     */
    public ClientBehaviourMove getClientManager();
    
    /**
     * Liefert die präzise Position dieses Moveables.
     * Movables arbeiteten mit präzise berechneten double-Koordinaten.
     * @return 
     */
    public FloatingPointPosition getPrecisePosition();
    
    /**
     * Liefert die maximale Geschwindigkeit dieses Moveables.
     * @return the speed
     */
    public double getSpeed();
    
    /**
     * Setzt eine neue Position (bewegt also die Einheit)
     * In der Regel sollte hier eine FPP Position reingegeben werden, es ist nur aus
     * Kompatibiltätsgrunden Position
     * @param mainPosition die neue Position
     */
    public void setMainPosition(Position mainPosition);
    
    /**
     * Liefert den Netzwerk-Identifier dieses Moveables.
     * Bei GameObjects die "normale" netID.
     * @return 
     */
    public int getNetID();
    
    /**
     * Liefert alle Movers in der Umgebung dieses Moveable.
     * Der Radius muss natürlich positiv und > null sein.
     * Arbeitet über Sektorgrenzen hinweg.
     * @param radius in welchen Radius gesucht werden soll.
     * @return 
     */
    public List<Moveable> moversAroundMe(double radius);
    
    /**
     * Liefert den Polygon, in dem sich die Einheit derzeit aufhält
     * @return the myPoly
     */
    public FreePolygon getMyPoly();

    /**
     * Setzt den Polygon, in dem sich die Einheit derzeit aufhält
     * @param myPoly the myPoly to set
     */
    public void setMyPoly(FreePolygon myPoly);
}
