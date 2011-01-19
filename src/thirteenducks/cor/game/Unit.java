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

import thirteenducks.cor.map.CoRMapElement.collision;
import java.io.*;
import java.util.*;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourHarvest;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.server.ServerCore;

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
    private double speed;
    /**
     * Ist die Einheit gerade in einem Gebäude? Dann sind fast alle Behaviours (alle?) abgeschaltet.
     */
    private boolean isIntra = false;
    /**
     * Der Wegmanager der Einheit.
     */
    private final Path path;


    protected Unit(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
        // Default-Werte *ugly*
        hitpoints = 100;
        maxhitpoints = 100;
        armorType = GameObject.ARMORTYPE_BUILDING;
        path = new Path();
    }

    /**
     * Erzeugt eine Platzhalter-Einheit, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    protected Unit() {
        super();
        path = null;
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
        if (!target.equals(mainPosition) && !(target.equals(path.getTargetPos()))) {
            // Da hin schicken
            rgi.netctrl.broadcastMove(this.netID, target, aggressive);
        } else if (target.equals(mainPosition)) {
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
    public void applyNewPath(ClientCore.InnerClient rgi, List<Position> newPath) {
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
            System.out.println("ERROR: Can't switch path(server), it is null !? Debug: unit: " + this);
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
    public Unit enemyInRangeAroundMe(ServerCore.InnerServer rgi) {
        int searchRange = (int) (range / 1.41);
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
                if (rgi.netmap.getEnemyUnitRef(kreismember.getX(), kreismember.getY(), playerId) == null) {
                    i++;
                }
            } catch (Exception ex) {
            }
        }

        return rgi.netmap.getEnemyUnitRef(kreismember.getX(), kreismember.getY(), playerId);
    }

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
    public Position meeleAttackableEnemyAroundMe(int searchDist, ServerCore.InnerServer rgi) {
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
                Unit unit = rgi.netmap.getEnemyUnitRef(kreismember.getX(), kreismember.getY(), playerId);
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
    }


    /**
     * Liefert die aktuelle Position bewegter Einheiten in Pixeln zurück
     * Hat auch Gametechnische Aufgaben, wie z.B. Kollisionsverwaltung
     * Es gibt dazu kein eigenes Behaviour, das erzeugt nur unnötig viel Overhead
     *
     * Diese Methode ist Teil der Grafikengine des Clients und darf nicht vom Server aufgerufen werden.
     * Richtungen werden automatisch angepasst.
     *
     * @param posX X-Anpassung, für Grafik
     * @param posY Y-Anpassung, für Grafik
     * @return Die Position, aber speziell für die Grafikengine
     */
    public Position getMovingPosition(ClientCore.InnerClient rgi, int posX, int posY) {
        synchronized (pathSync) {
            try {
                length = getWayLength();
                long passedTime = 0;
                if (movePaused) {
                    passedTime = pauseTime - startTime;
                } else {
                    passedTime = System.currentTimeMillis() - startTime;
                }
                double passedWay = passedTime * speed / 1000;
                // Schon fertig?
                if (passedWay >= length) {
                    // Fertig, Bewegung stoppen
                    this.movingtarget = null;
                    this.pathLengthCalced = false;
                    this.position = path.get(path.size() - 1);
                    this.path = null;
                    if (this.order != orders.harvest) {
                        // Harvesting bleibt
                        this.order = orders.idle;
                    } else {
                        // In Richtung der Ressource drehen:
                        // Versuchen, die Position der Ressource zu finden
                        Ressource res = ((ClientBehaviourHarvest) this.getbehaviourC(7)).res;
                        if (res != null && this.anim != null) {
                            this.anim.dir = res.position.subtract(this.position).transformToIntVector();
                            // Die Infos um das zu Drehen hat nur dieser Client, an alle andern schicken.
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 50, 1, this.netID, this.anim.dir, 0));
                        }
                    }
                    rgi.mapModule.setCollision(this.position, collision.occupied);
                    // Eventuell alten Wegpunkt löschen
                    if (lastref != null && lastref.unitref[playerId] == this) {
                        lastref.unitref[playerId] = null;
                        lastref = null;
                    }
                    rgi.mapModule.setUnitRef(this.position, this, playerId);

                    return new Position((int) ((position.X - posX) * 20), (int) ((position.Y - posY) * 15));
                }
                // Zuletzt erreichten Wegpunkt finden
                if (passedWay >= this.nextWayPointDist) {

                    // Sind wir einen weiter oder mehrere
                    int weiter = 1;
                    while (passedWay > pathOrder.get(lastwaypoint + 1 + weiter)) {
                        weiter++;
                    }
                    lastwaypoint += weiter;
                    // Hat sich die Geschwindigkeit geändert
                    if (this.changespeedto != 0) {
                        // Ja, alte Felder löschen & neu berechnen
                        for (; lastwaypoint > 0; lastwaypoint--) {
                            this.path.remove(0);
                        }
                        speed = changespeedto;
                        changespeedto = 0;
                        lastwaypoint = 0;
                        startTime = System.currentTimeMillis();
                        this.calcWayLength();
                    }

                    // Eventuell alte ref löschen
                    if (lastref != null && lastref.unitref[playerId] == this) {
                        lastref.unitref[playerId] = null;
                        lastref = null;
                    }

                    nextWayPointDist = pathOrder.get(lastwaypoint + 1);
                    if (this.anim != null) {
                        try {
                            this.anim.dir = pathDirection.get(lastwaypoint + 1);
                        } catch (Exception ex) {
                        }
                    }
                    this.position = path.get(lastwaypoint);
                    // Neue erzeugen
                    lastref = rgi.mapModule.theMap.visMap[position.X][position.Y];
                    // Nur setzen, wenn dort noch nichts ist
                    if (lastref != null && lastref.unitref[playerId] == null) {
                        lastref.unitref[playerId] = this;
                    } else {
                        lastref = null;
                    }
                }
                // In ganz seltenen Fällen ist hier lastwaypoint zu hoch (vermutlich (tfg) ein multithreading-bug)
                // Daher erst checken und ggf. reduzieren:
                if (lastwaypoint >= pathOrder.size() - 1) {
                    System.out.println("Client: Lastwaypoint-Error, setting back. May causes jumps!?");
                    lastwaypoint = pathOrder.size() - 2;
                }
                double diffLength = passedWay - pathOrder.get(lastwaypoint);
                // Wir haben jetzt den letzten Punkt der Route, der bereits erreicht wurde, und die Strecke, die danach noch gefahren wurde...
                // Jetzt noch die Richtung
                int diffX = path.get(lastwaypoint + 1).X - path.get(lastwaypoint).X;
                int diffY = path.get(lastwaypoint + 1).Y - path.get(lastwaypoint).Y;
                // Prozentanteil der Stecke, die zurückgelegt wurde
                double potPathWay = Math.sqrt(Math.pow(Math.abs(diffX), 2) + Math.pow(Math.abs(diffY), 2));
                double faktor = diffLength / potPathWay * 100;
                double lDiffX = diffX * faktor / 100;
                double lDiffY = diffY * faktor / 100;
                Position pos = new Position((int) ((path.get(lastwaypoint).X + lDiffX - posX) * 20), (int) ((path.get(lastwaypoint).Y + lDiffY - posY) * 15));
                return pos;
            } catch (Exception ex) {
                ex.printStackTrace();
                return new Position(-1, -1);
            }


        }
    }

    @Override
    public Unit clone(int newNetID) throws CloneNotSupportedException {
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
        retUnit.setPlayerId(this.playerId);
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
    }

    @Override
    public void pause() {
        movePaused = true;
        pauseTime = System.currentTimeMillis();
    }

    @Override
    public void unpause() {
        movePaused = false;
        startTime = System.currentTimeMillis() - (pauseTime - startTime);
    }

    @Override
    public String toString() {
        return ("Unit \"" + this.name + "\" ID: " + this.netID);
    }
}
