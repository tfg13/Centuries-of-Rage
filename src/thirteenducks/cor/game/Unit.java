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

import java.io.*;
import java.util.*;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.graphics.input.InteractableGameElement;

/**
 * Superklasse für Einheiten
 *
 * Einheiten sind GO's, die sich bewegen können.
 * Im Gegensatz zu Gebäuden ermöglicht diese Implementierung keine flexiblen Größen
 * Einheiten dieser Implementierung spawnen sofort.
 * Unterklassen können Gebäude (Building) betreten (falls das Gebäude dies anbietet)
 */
public abstract class Unit extends GameObject implements Serializable, Cloneable, Pauseable {

    /**
     * Die Geschwindigkeit der Einheit in Feldern pro Sekunde.
     */
    protected double speed;
    /**
     * Ist die Einheit gerade in einem Gebäude? Dann sind fast alle Behaviours (alle?) abgeschaltet.
     */
    private boolean isIntra = false;
    /**
     * Der Wegmanager der Einheit.
     */
    protected final Path path;

    protected Unit(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
        // Default-Werte *ugly*
        hitpoints = 100;
        maxhitpoints = 100;
        armorType = GameObject.ARMORTYPE_BUILDING;
        path = new Path(10);
    }

    /**
     * Erzeugt eine Platzhalter-Einheit, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    protected Unit(DescParamsUnit params) {
        super(params);
        applyUnitParams(params);
        path = null;
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
        path = new Path(copyFrom.speed);
        this.speed = copyFrom.speed;
    }

    /**
     * Wendet die Parameterliste an (kopiert die Parameter rein)
     * @param par
     */
    private void applyUnitParams(DescParamsUnit par) {
        this.speed = par.getSpeed();
    }

    /**
     * Lässt die Einheit zu der angegebenen (Master)-Position laufen.
     *
     * Schickt den Request nach einigen Überprüfungen an den Server
     *
     * @param position Das Ziel der Einheit
     * @param rgi Der InnerClient, für Kollision & Netzwerkzugriff
     * @param aggressive Welcher Bewegungsmodus?
     */
    public void sendToPosition(Position target, ClientCore.InnerClient rgi, boolean aggressive) {
        if (target == null || rgi == null) {
            System.out.println("ERROR: Calling sendToPosition with: " + target + "|" + rgi);
            return;
        }
        // Sind wir schon da, oder gehen wir da hin?
        if (!target.equals(getMainPosition()) && !(target.equals(path.getTargetPos()))) {
            // Da hin schicken
            rgi.netctrl.broadcastMove(this.netID, target, aggressive);
        } else if (target.equals(getMainPosition())) {
            System.out.println("AddMe: Check for MoveMode-Change");
        }
    }

    /**
     * Lässt die Einheit sofort auf diesem Pfad laufen
     * Client only
     * Übernimmt den Pfad bedingungslos und startet die Bewegung sofort vom
     * ersten Feld aus, die Unit wird eventuell versetzt, wenn path(0) != this.position
     *
     * @param rgi Inner Client
     * @param newPath Der neue Weg der Unit
     */
    public synchronized void applyNewPath(ClientCore.InnerClient rgi, List<Position> newPath) {
        if (newPath == null) {
            System.out.println("ERROR: Can't set path(client), it is null !? Debug: unit: " + this);
            return;
        }
        if (newPath.size() < 2) {
            // Ungültig ein Weg muss mindestens 2 Punkte haben
            System.out.println("ERROR: Path with only one Position! (applyNew-Unit-client)");
            return;
        }

        // Bauen anhalten
        System.out.println("AddMe: Notify Behaviours about MOVE_START");

        //rgi.mapModule.setCollision(newPath.get(0), collision.free);
        //rgi.mapModule.setUnitRef(newPath.get(0), null, playerId);
        path.overwritePath(newPath);
    }

    /**
     * Wechselt auf diesen Weg, MUSS den alten enthalten
     * Client only
     *
     * @param newPath Der neue Weg der Unit MUSS DEN ALTEN ENTHALTEN
     */
    public synchronized void switchPath(ClientCore.InnerClient rgi, ArrayList<Position> newPath) {
        if (newPath == null) {
            System.out.println("ERROR: Can't switch path(client), it is null !? Debug: unit: " + this);
            return;
        }
        if (newPath.size() < 2) {
            // Ungültig ein Weg muss mindestens 2 Punkte haben
            System.out.println("ERROR: Path with only one Position! (switchTo-Unit-client)");
            return;
        }

        // Bauen
        System.out.println("AddMe: Notify Behaviours about MOVE_SWITCH");

        // Weg ändern
        path.switchPath(newPath);
    }

    /**
     * Lässt die Einheit sofort auf diesem Pfad laufen
     * Server only
     * Übernimmt den Pfad bedingungslos und startet die Bewegung sofort vom
     * ersten Feld aus, die Unit wird eventuell versetzt, wenn path(0) != this.position
     *
     * @param rgi Inner Server
     * @param newPath Der neue Weg der Unit
     */
    public synchronized void applyNewPath(ServerCore.InnerServer rgi, List<Position> newPath) {
        if (newPath == null) {
            System.out.println("ERROR: Can't set path(server), it is null !? Debug: unit: " + this);
            return;
        }
        if (newPath.size() < 2) {
            // Ungültig ein Weg muss mindestens 2 Punkte haben
            System.out.println("ERROR: Path with only one Position! (applyNew-Unit-server)");
            return;
        }

        // Bauen anhalten
        System.out.println("AddMe: Notify Behaviours about MOVE_START");

        //rgi.mapModule.setCollision(newPath.get(0), collision.free);
        //rgi.mapModule.setUnitRef(newPath.get(0), null, playerId);
        path.overwritePath(newPath);
        // Reservieren
        rgi.netmap.reserveMoveTarget(this, System.currentTimeMillis() + (long) (1000.0 * path.getLength() / this.speed), newPath.get(newPath.size() - 1));
    }

    /**
     * Wechselt auf diesen Weg, MUSS den alten enthalten
     * Server only
     *
     * @param newPath Der neue Weg der Unit MUSS DEN ALTEN ENTHALTEN
     */
    public synchronized void switchPath(ServerCore.InnerServer rgi, ArrayList<Position> newPath) {
        if (newPath == null) {
            System.out.println("ERROR: Can't switch path(server), it is null !? Debug: unit: " + this);
            return;
        }
        if (newPath.size() < 2) {
            // Ungültig ein Weg muss mindestens 2 Punkte haben
            System.out.println("ERROR: Path with only one Position! (switchTo-Unit-server)");
            return;
        }

        // Bauen
        System.out.println("AddMe: Notify Behaviours about MOVE_SWITCH");

        // Weg ändern
        path.switchPath(newPath);
    }

    /**
     * Muss regelmäßig aufgerufen werden, damit die Einheit ihr Bewegung berechnen kann.
     */
    public void serverManagePath(ServerCore.InnerServer rgi) {
            path.serverManagePath(rgi, this);
    }

    /**
     * Muss regelmäßig aufgerufen werden, damit die Einheit ihr Bewegung berechnen kann.
     */
    public void clientManagePath(InnerClient rgi) {
            path.clientManagePath(rgi, this);
    }

    /**
     * Lässt die Einheit zum Gebäude laufen
     * Wenn die Route dorthin halbwegs der Luftlinie entspricht,
     * hält sie an der "kürzesten" Ecke an.
     *
     * @param building Das Ziel, zu dem sie laufen soll
     * @param inner die inner-Referenz
     */
    public abstract void moveToBuilding(Building building, ServerCore.InnerServer inner);

    /**
     * Sucht Feinde, die von hier aus direkt angreiffbar sind.
     * Nur für Fernkampfeinheiten
     * @param rgi
     * @return
     */
  /*  public Unit enemyInRangeAroundMe(ServerCore.InnerServer rgi) {
        int searchRange = (int) (getRange() / 1.41);
        int i = 1;
        int kreis = 1;
        // Startfeld des Kreises:
        Position kreismember = new Position(getMainPosition().getX(), getMainPosition().getY());
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.setY(kreismember.getY() - 2);
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.setX(kreismember.getX() - 1);
                kreismember.setY(kreismember.getY() + 1);
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.setX(kreismember.getX() + 1);
                kreismember.setY(kreismember.getY() + 1);
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.setX(kreismember.getX() + 1);
                kreismember.setY(kreismember.getY() - 1);
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.setX(kreismember.getX() - 1);
                kreismember.setY(kreismember.getY() - 1);
            } else {
                // Sprung in den nächsten Kreis
                kreismember.setX(kreismember.getX() - 1);
                kreismember.setY(kreismember.getY() - 3);
                k = 0;
                i = i - (kreis * 8);
                kreis++;
                // Suchende Erreicht?
                if (kreis >= searchRange) {
                    // Ende, nix gefunden --> null
                    return null;
                }
            }
            // Ist dieses Feld NICHT geeignet?
            try {
                if (rgi.netmap.getEnemyUnitRef(kreismember.getX(), kreismember.getY(), getPlayerId()) == null) {
                    i++;
                }
            } catch (Exception ex) {
            }
        }

        return rgi.netmap.getEnemyUnitRef(kreismember.getX(), kreismember.getY(), getPlayerId());
    } */

    /**
     * Sucht Feinde in der Umgebung dieser Einheit und dazugehörige Angriffspositionen
     * Liefert die Position zurück, den Feind findet man dann mit der UnitRef
     * Der searchDist - Wert wird in Kreisen um die Einheit angegeben und steigt nicht linear an, sondern nach PI*searchDist^2
     *
     * Führt automatisch "Wartungsarbeiten" durch, löscht z.B. veraltete Einträge raus
     *
     * @param searchDist Suchweite in Kreisen um die Einheit, sehr laufzeitkritisch, sollte < 6 sein!
     * @return Feindliche Einheit, falls gefunden.
     */
 /*   public Position meeleAttackableEnemyAroundMe(int searchDist, ServerCore.InnerServer rgi) {
        int i = 1;
        int kreis = 1;
        // Startfeld des Kreises:
        Position kreismember = new Position(getMainPosition().getX(), getMainPosition().getY());
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.setY(kreismember.getY() - 2);
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.setX(kreismember.getX() - 1);
                kreismember.setY(kreismember.getY() + 1);
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.setX(kreismember.getX() + 1);
                kreismember.setY(kreismember.getY() + 1);
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.setX(kreismember.getX() + 1);
                kreismember.setY(kreismember.getY() - 1);
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.setX(kreismember.getX() - 1);
                kreismember.setY(kreismember.getY() - 1);
            } else {
                // Sprung in den nächsten Kreis
                kreismember.setX(kreismember.getX() - 1);
                kreismember.setY(kreismember.getY() - 3);
                k = 0;
                i = i - (kreis * 8);
                kreis++;
                // Suchende Erreicht?
                if (kreis >= searchDist) {
                    // Ende, nix gefunden --> null
                    return null;
                }
            }
            // Ist dieses Feld NICHT geeignet?
            try {
                Unit unit = rgi.netmap.getEnemyUnitRef(kreismember.getX(), kreismember.getY(), getPlayerId());
                if (unit != null) {
                    Position potpos = unit.freeDirectAroundMe();
                    if (potpos != null) {
                        return potpos;
                    }
                }
                i++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }  */

    /**
     * @return the speed
     */
    public double getSpeed() {
        return speed;
    }


    /*  public Unit clone(int newNetID) throws CloneNotSupportedException {
    // Dies ist kein echtes Klonen mehr, es wird viel mehr eine neue Einheit mit den gleichen Eigenschaften erzeugt.
    // Es werden NICHT (!) alle Eigenschaften übernommen, da die ursprungseinheiten des Klonvorgangs kaum Inhalte haben
    Unit retUnit = new Unit(this.position.X, this.position.Y, newNetID);
    retUnit.Gcon = this.Gcon;
    retUnit.Gdesc = this.Gdesc;
    retUnit.Gpro = this.Gpro;
    retUnit.canHarvest = this.canHarvest;
    retUnit.cooldown = this.cooldown;
    retUnit.cooldownmax = this.cooldownmax;
    retUnit.armortype = this.armortype;
    retUnit.damage = this.damage;
    retUnit.antiheavyinf = this.antiheavyinf;
    retUnit.antilightinf = this.antilightinf;
    retUnit.antikav = this.antikav;
    retUnit.antivehicle = this.antivehicle;
    retUnit.antitank = this.antitank;
    retUnit.antiair = this.antiair;
    retUnit.antibuilding = this.antibuilding;
    retUnit.descTypeId = this.descTypeId;
    retUnit.hitpoints = this.hitpoints;
    retUnit.idlehuntradius = this.idlehuntradius;
    retUnit.idlerange = this.idlerange;
    retUnit.maxhitpoints = this.maxhitpoints;
    retUnit.atkdelay = this.atkdelay;
    retUnit.name = this.name;
    retUnit.pathLengthCalced = false;
    retUnit.range = this.range;
    retUnit.selectionShadow = this.selectionShadow;
    retUnit.speed = this.speed;
    retUnit.setgetPlayerId()(this.getPlayerId());
    retUnit.bullettexture = this.bullettexture;
    retUnit.bulletspeed = this.bulletspeed;
    retUnit.visrange = this.visrange;
    retUnit.limit = this.limit;
    if (this.movingtarget != null) {
    retUnit.movingtarget = this.movingtarget.clone();
    }
    
    if (this.path != null) {
    retUnit.path = new ArrayList<Position>();
    }
    
    // Special-Stuff
    if (this.anim != null) {
    retUnit.anim = this.anim.clone();
    }
    if (this.abilitys != null) {
    retUnit.abilitys = Collections.synchronizedList(new ArrayList<Ability>());
    for (Ability a : this.abilitys) {
    retUnit.abilitys.add(a.clone());
    }
    }
    retUnit.graphicsdata = this.graphicsdata.clone();
    
    return retUnit;
    } */
    @Override
    public void pause() {
        path.pause();
    }

    @Override
    public void unpause() {
        path.unpause();
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

    /**
     * Stoppt die Einheit sofort - sofern genug Platz ist und die Einheit sich überhaupt bewegt.
     * Falls hier gerade kein Platz ist, wird die Einheit zur nächstmöglichen Position laufen.
     * Nur Client!
     */
    public void stopMovement(ClientCore.InnerClient rgi) {
        if (moveStoppable()) {
            path.stopMovement(rgi, this);
        }
    }

    /**
     * Findet heraus, ob die Einheit sich derzeit in einer Stoppbaren Bewegung befindet.
     * @return
     */
    public boolean moveStoppable() {
        return path.moveStoppable();
    }

    @Override
    public void command(int button, Position target, List<InteractableGameElement> repeaters, boolean doubleKlick, InnerClient rgi) {
        // Befehl abschicken:
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 52, target.getX(), target.getY(), repeaters.get(0).getAbilityCaster().netID, repeaters.size() > 1 ? repeaters.get(1).getAbilityCaster().netID : 0));
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
    }

    /**
     * Stoppt die Einheit so schnell wie möglich anhalten
     * Server only!
     * @param stopPos die Position, auf der die Einheit angehalten werden soll.
     * @param rgi InnerServer für Kollisions/Reservierungsaufgaben
     */
    public void stopMovement(ServerCore.InnerServer rgi) {
        path.stopMovement(rgi, this);
    }

    public void quickStop(int readInt, Position readPosition) {
        path.quickStop(readInt, readPosition);
    }
}
