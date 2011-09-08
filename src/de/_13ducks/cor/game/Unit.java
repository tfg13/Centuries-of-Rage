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

import de._13ducks.cor.game.client.Client;
import de._13ducks.cor.game.client.ClientCore;
import java.io.*;
import java.util.*;
import de._13ducks.cor.game.client.ClientCore.InnerClient;
import de._13ducks.cor.game.client.ClientGameController;
import de._13ducks.cor.game.server.Server;
import de._13ducks.cor.game.server.movement.ServerBehaviourMove;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.game.server.movement.GroupManager;
import de._13ducks.cor.game.server.movement.MovementMap;
import de._13ducks.cor.game.server.movement.ServerBehaviourAttack;
import de._13ducks.cor.game.server.movement.ServerBehaviourFollow;
import de._13ducks.cor.game.server.movement.ServerMoveManager;
import de._13ducks.cor.graphics.effects.SendToEffect;
import de._13ducks.cor.graphics.input.InteractableGameElement;
import de._13ducks.cor.map.fastfindgrid.Cell;
import de._13ducks.cor.networks.client.behaviour.ClientBehaviourMove;
import de._13ducks.cor.networks.client.behaviour.UDBClientBehaviourMove;

/**
 * Superklasse für Einheiten
 *
 * Einheiten sind GO's, die sich bewegen können.
 * Dazu verwenden Einheiten statt normalen Positionen Fließkommazahlen,
 * um auch Positionen zwischen Feldern darstellen zu können.
 * Die von GameObject bekannten, feldbasierten Positionsoperationen funktionieren weiterhin,
 * sind jedoch nur eine (abgerundetete!) Darstellung der "echten Feldern"
 * Es existieren auch Getter für die Fließkommazahlen.
 * Im Gegensatz zu Gebäuden ermöglicht diese Implementierung keine flexiblen Größen
 * Einheiten dieser Implementierung spawnen sofort.
 * Unterklassen können Gebäude (Building) betreten (falls das Gebäude dies anbietet)
 */
public abstract class Unit extends GameObject implements Serializable, Cloneable, Pauseable, Moveable {

    /**
     * Die Geschwindigkeit der Einheit in Feldern pro Sekunde.
     */
    protected double speed;
    /**
     * Ist die Einheit gerade in einem Gebäude? Dann sind fast alle Behaviours (alle?) abgeschaltet.
     */
    private boolean isIntra = false;
    /**
     * Der LowLevel-Movemanager dieser Einheit.
     * Jede Einheit hat ihren eigenen.
     * @see ServerBehaviourMove
     */
    private ServerBehaviourMove lowLevelManager;
    /**
     * Der aktuelle MidLevel-Movemanager dieser Einheit.
     * Einheiten haben nicht immer einen & er kann sich ändern.
     */
    private GroupManager midLevelManager;
    /**
     * Der TopLevel-Movemanager dieser Einheit.
     * Ist normalerweise für den ganzen Server global.
     */
    private ServerMoveManager topLevelManager;
    /**
     * Der Angriffsmanager dieser Einheit.
     */
    private ServerBehaviourAttack atkManager;
    /**
     * Der Verfolgungsmanager dieser Einheit.
     */
    private ServerBehaviourFollow followManager;
    /**
     * Der Client-Movemanager dieser Einheit.
     */
    protected ClientBehaviourMove clientManager;
    /**
     * Die Movement-Map
     * Server only.
     */
    private MovementMap moveMap;
    /**
     * Der kleine Pfeil, der erscheint, wenn man die Einheit irgendwo hin schickt
     */
    private SendToEffect sendEffect;
    /**
     * Die Zelle im Schnellsuchraster, in der die Einheit steht
     */
    private Cell myCell;
    /**
     * Derzeit gehovered?
     */
    protected boolean hovered = false;

    protected Unit(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
        // Default-Werte *ugly*
        hitpoints = 100;
        maxhitpoints = 100;
        armorType = GameObject.ARMORTYPE_BUILDING;
        this.mainPosition = new FloatingPointPosition(this.mainPosition);
    }

    /**
     * Erzeugt eine Platzhalter-Einheit, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    protected Unit(DescParamsUnit params) {
        super(params);
        applyUnitParams(params);
        this.mainPosition = new FloatingPointPosition(0, 0);
    }

    /**
     * Erzeugt eine neue Einheit als eigenständige Kopie der Übergebenen.
     * Wichtige Parameter werden kopiert, Sachen die jede Einheit selber haben sollte nicht.
     * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
     * @param newNetId Die netId der neuen Einheit
     * @param copyFrom Die Einheit, dessen Parameter kopiert werden sollen
     */
    protected Unit(int newNetId, Unit copyFrom) {
        super(newNetId, copyFrom);
        this.speed = copyFrom.speed;
        this.mainPosition = new FloatingPointPosition(0, 0);
    }

    /**
     * Wendet die Parameterliste an (kopiert die Parameter rein)
     * @param par
     */
    private void applyUnitParams(DescParamsUnit par) {
        this.speed = par.getSpeed();
    }

    /**
     * @return the speed
     */
    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }

    @Override
    public String toString() {
        return ("Unit \"" + this.getName() + "\" ID: " + this.netID);
    }

    @Override
    public boolean renderInFullFog() {
        return false;
    }

    @Override
    public boolean renderInHalfFog() {
        //@TODO: Gebäude müssen sichtbar bleiben, wenn man sie einmal gesehen hat.
        return false;
    }

    @Override
    public boolean renderInNullFog() {
        return true;
    }

    /**
     * Ist die Einheit gerade in einem Gebäude? Dann sind fast alle Behaviours (alle?) abgeschaltet.
     * @return the isIntra
     */
    public boolean isIntra() {
        return isIntra;
    }

    @Override
    public boolean isSelectableByPlayer(int playerId) {
        // Im Debugmode is es erlaubt selectAll=true zu setzen, dann darf man alle selektieren und steuern
        if (Client.getInnerClient().isInDebugMode() && "true".equals(Client.getInnerClient().configs.get("selectAll"))) {
            return true;
        }
        return playerId == this.getPlayerId();
    }

    @Override
    public boolean isMultiSelectable() {
        return true;
    }

    @Override
    public boolean selectable() {
        return true;
    }

    @Override
    public int getColorId() {
        return getPlayerId();
    }

    @Override
    public void command(int button, FloatingPointPosition target, List<InteractableGameElement> repeaters, boolean doubleKlick, InnerClient rgi) {
        // Hack, die X-Koorindate negativ übertragen, um flüchten zu signalisieren
        if (doubleKlick) {
            target = target.toFPP(); // Kopieren
            target.setfX(target.getfX() * -1);
        }
        // Befehl abschicken:
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, Float.floatToIntBits((float) target.getfX()), Float.floatToIntBits((float) target.getfY()), repeaters.get(0).getAbilityCaster().netID, repeaters.size() > 1 ? repeaters.get(1).getAbilityCaster().netID : 0));
        // Hier sind unter umständen mehrere Packete nötig:
        if (repeaters.size() == 2) {
            // Nein, abbrechen
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, 0, 0, 0, 0));
        } else if (repeaters.size() != 1) {
            // Jetzt den Rest abhandeln
            int[] ids = new int[4];
            for (int i = 0; i < 4; i++) {
                ids[i] = 0;
            }
            int nextselindex = 2;
            int nextidindex = 0;
            // Solange noch was da ist:
            while (nextselindex < repeaters.size()) {
                // Auffüllen
                ids[nextidindex] = repeaters.get(nextselindex).getAbilityCaster().netID;
                nextidindex++;
                nextselindex++;
                // Zu weit?
                if (nextidindex == 4) {
                    // Einmal rausschicken & löschen
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
                    for (int i = 0; i < 4; i++) {
                        ids[i] = 0;
                    }
                    nextidindex = 0;
                }
            }
            // Fertig, den Rest noch senden
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, ids[0], ids[1], ids[2], ids[3]));
        }
        // Effekt an dieser Stelle anzeigen. Erstmal den alten abbrechen, falls noch da.
        if (sendEffect != null) {
            sendEffect.kill();
        }
        sendEffect = new SendToEffect(doubleKlick ? target.getfX() * -1 : target.getfX(), target.getfY(), doubleKlick ? SendToEffect.MODE_RUNTO : SendToEffect.MODE_GOTO);
        rgi.rogGraphics.content.skyEffects.add(sendEffect);
    }

    @Override
    public void command(int button, InteractableGameElement target, List<InteractableGameElement> repeaters, boolean doubleKlick, InnerClient rgi) {
        if (target.isAttackableBy(getPlayerId())) {
            
            GameObject realTarget = target.getAttackable();
            if (realTarget != null) {
                int targetid = realTarget.netID;
                // Hack, die netId negativ Übertragen, um Focus zu signalisieren
                if (doubleKlick) {
                    targetid *= -1;
                }

                // Befehl abschicken:
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 55, targetid, 0, repeaters.get(0).getAbilityCaster().netID, repeaters.size() > 1 ? repeaters.get(1).getAbilityCaster().netID : 0));
                // Hier sind unter umständen mehrere Packete nötig:
                if (repeaters.size() == 2) {
                    // Nein, abbrechen
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 55, 0, 0, 0, 0));
                } else if (repeaters.size() != 1) {
                    // Jetzt den Rest abhandeln
                    int[] ids = new int[4];
                    for (int i = 0; i < 4; i++) {
                        ids[i] = 0;
                    }
                    int nextselindex = 2;
                    int nextidindex = 0;
                    // Solange noch was da ist:
                    while (nextselindex < repeaters.size()) {
                        // Auffüllen
                        ids[nextidindex] = repeaters.get(nextselindex).getAbilityCaster().netID;
                        nextidindex++;
                        nextselindex++;
                        // Zu weit?
                        if (nextidindex == 4) {
                            // Einmal rausschicken & löschen
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 55, ids[0], ids[1], ids[2], ids[3]));
                            for (int i = 0; i < 4; i++) {
                                ids[i] = 0;
                            }
                            nextidindex = 0;
                        }
                    }
                    // Fertig, den Rest noch senden
                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 55, ids[0], ids[1], ids[2], ids[3]));
                }

                // Effekt an dieser Stelle anzeigen. Erstmal den alten abbrechen, falls noch da.
                if (sendEffect != null) {
                    sendEffect.kill();
                }
                sendEffect = new SendToEffect(realTarget.getCentralPosition().getX(), realTarget.getCentralPosition().getY(), doubleKlick ? SendToEffect.MODE_FOCUSATK : SendToEffect.MODE_ATK);
                rgi.rogGraphics.content.skyEffects.add(sendEffect);
            } else {
                System.out.println("[ERROR]: Target claims to be attackable, but getter returns null");
            }
        }
    }

    @Override
    public FloatingPointPosition getPrecisePosition() {
        return (FloatingPointPosition) mainPosition;
    }

    /**
     * Stoppt die Einheit sofort - sofern genug Platz ist und die Einheit sich überhaupt bewegt.
     * Falls hier gerade kein Platz ist, wird die Einheit zur nächstmöglichen Position laufen.
     * Nur Client!
     */
    @Override
    public void stopMovement(ClientCore.InnerClient rgi) {
        if (moveStoppable()) {
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 54, this.netID, 0, 0, 0));
        }
    }

    /**
     * Findet heraus, ob die Einheit sich derzeit in einer Stoppbaren Bewegung befindet.
     * @return
     */
    @Override
    public boolean moveStoppable() {
        return clientManager.isActive();
    }

    @Override
    public void setMainPosition(Position mainPosition) {
        super.setMainPosition(mainPosition); // Falls da noch sonst was gemanaged wird
        if (mainPosition instanceof FloatingPointPosition) {
            this.mainPosition = mainPosition;
        } else {
            this.mainPosition = new FloatingPointPosition(mainPosition);
        }
    }

    /**
     * Liefert den LowLevel-Manager dieser Einheit.
     * @return den Lowlevel-Manager dieser Einheit.
     */
    @Override
    public ServerBehaviourMove getLowLevelManager() {
        return lowLevelManager;
    }

    /**
     * Liefert den MidLevel-Manager dieser Einheit.
     * @return den Midlevel-Manager dieser Einheit.
     */
    @Override
    public GroupManager getMidLevelManager() {
        return midLevelManager;
    }

    /**
     * Liefert den TopLevel-Manager dieser Einheit.
     * @return den TopLevel-Manager dieser Einheit.
     */
    @Override
    public ServerMoveManager getTopLevelManager() {
        return topLevelManager;
    }

    /**
     * Bereitet die Einheit für das Server-Bewegungssystem vor.
     * Muss aufgerufen werden, bevor Bewegungsbefehle an die Einheit gesendet werden.
     * @param rgi der inner core
     */
    @Override
    public void initServerMovementManagers(ServerCore.InnerServer rgi, MovementMap moveMap) {
        topLevelManager = rgi.moveMan;
        lowLevelManager = new ServerBehaviourMove(rgi, this, this, this, moveMap);
        atkManager = new ServerBehaviourAttack(this, rgi, moveMap);
        followManager = new ServerBehaviourFollow(this, rgi);
        addServerBehaviour(lowLevelManager);
        addServerBehaviour(atkManager);
        addServerBehaviour(followManager);
        this.moveMap = moveMap;
    }

    /**
     * Bereitet die Einheit für das Client-Bewegungssystem vor.
     * Muss aufgerufen werden, bevor Bewegungsbefehle an die Einheit gesendet werden.
     * @param rgi der inner core
     */
    @Override
    public void initClientMovementManager(ClientCore.InnerClient rgi) {
        if (!ClientGameController.udbEnabled) {
            clientManager = new ClientBehaviourMove(rgi, this);
        } else {
            clientManager = new UDBClientBehaviourMove(rgi, this);
        }
        addClientBehaviour(clientManager);
    }

    /**
     * Entfernt die Einheit aus ihrer aktuellen Gruppe, falls vorhanden.
     */
    @Override
    public void removeFromCurrentGroup() {
        if (midLevelManager != null) {
            midLevelManager.remove(this);
            midLevelManager = null;
        }
    }

    /**
     * Setzt die aktuelle Gruppe der Einheit, falls sie derzeit keine hat.
     * Wenn eine da ist, wird diese nicht überschrieben!!!
     * @param man Die neue Gruppe
     */
    @Override
    public void setCurrentGroup(GroupManager man) {
        if (midLevelManager == null) {
            midLevelManager = man;
            man.add(this);
        }
    }

    /**
     * @return the clientManager
     */
    @Override
    public ClientBehaviourMove getClientManager() {
        return clientManager;
    }

    @Override
    public int getNetID() {
        return netID;
    }

    @Override
    public List<Moveable> moversAroundMe(double radius) {
        // Das macht die movementMap
        return moveMap.moversAround(this, radius);
    }

    @Override
    public ServerBehaviourAttack getAtkManager() {
        return atkManager;
    }

    @Override
    public ServerBehaviourFollow getFollowManager() {
        return followManager;
    }

    @Override
    public Position getCentralPosition() {
        return mainPosition;
    }
    @Override
    public void killS() {
        Server.getInnerServer().netmap.killUnit(this);
        super.killS();
    }

    /**
     * Gibt die Zelle zurück, in de die Einheit steht, für das Schnellsuchraster
     * @return
     */
    @Override
    public Cell getCell() {
        return myCell;
    }

    /**
     * Gibt die eigene Position zurück, für das Schnellsuchraster
     * @return
     */
    @Override
    public FloatingPointPosition getPosition() {
        return this.getPrecisePosition();
    }

    /**
     * Gibt diese Einheit zurück, damit man vom Traceable-Interface auch mit der einheit was machen kann
     * @return
     */
    @Override
    public Unit getUnit() {
        return this;
    }

    /**
     * Setzt die Zelle (Schnellsuchraster) dieser Einheit
     * @param theCell - die Zelle des Schnellsuchrasters, auf der die EInheit steht
     */
    @Override
    public void setCell(Cell theCell) {
        myCell = theCell;
    }

    @Override
    public void mouseHovered() {
        hovered = true;
    }
}
