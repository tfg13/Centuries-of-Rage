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
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourHarvest;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.graphics.UnitAnimator;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.graphics.GOGraphicsData;
import thirteenducks.cor.game.server.behaviour.impl.ServerBehaviourAttack;
import thirteenducks.cor.game.server.behaviour.impl.ServerBehaviourMove;
import thirteenducks.cor.game.server.ServerCore;

/**
 * RogUnit.java
 * @author tfg, hase
 *
 * Beschreibt eine Einheit.
 */
public class Unit extends GameObject implements Serializable, Cloneable, Pauseable {

    /**
     * Allgemeines
     */
    //ClientCore.RogCoreInner inner;                 // Referenz auf inner
    public double speed;                               // Geschwindigkeit der Einhet in Felder/s
    public int selectionShadow = 1;                    // Zum Einheiten präziser selektieren. Je größer die Zahl, desto größerdie Einheit
    public boolean canHarvest = false;                 // Kann die Einheit Ressourcen ernten?
    public boolean isCompletelyActivated = true;       // Einheiten müssen in 2 Schritten aktiviert werden, das wird false, damit die Einheit nicht vorher irgendwelche Sachen macht.
    

    /**
     * Kollision & Bewegung
     */
    // Beschreibt den Befehl der Einheit
    public enum orders {

        idle, // Die Eiheit lauert, sie hat keinen Befehl
        attackunit, // Die Einheit hat einen Angriffsbefehl auf eine Einheit
        attackarea, // Die Einheit hat einen Angriffsbefehl auf ein Gebiet
        move, // Die Einheit hat einen Bewegungsbefehl
        sthbetween, // Übergangswert, wenn kein anderer zutrifft
        construct,
        harvest
    };

    // Beschreibt die momentane Aktion der Einheit
    // im moment nur für arbeitslosenerkennung
    public enum actions {
        harvest,        // ernten
        move,           // bewegen
        nothing         // die einheit macht irgendwas
    };
    public double nextwaypoint;                         // Der bis zum nächsten Wegpunkt zurückgelegte Weg.
    public Position movingtarget;                       // Das Ziel der CoRUnit
    public ArrayList<Position> path;                    // Der Weg, den die CoRUnit verfolgt (wenn sie ein Bewegungsziel hat)
    public final Serializable pathSync;                 // Leeres Objekt, zum syncen auf path.
    public int lastwaypoint;                            // Der Index des letzten erreichten Wegpunkts der Weges - Wird durch die getMovingPosition() des Grafikmoduls mitaktualisiert
    public ArrayList<Double> pathOrder;                 // Abstände zwischen den Wegpunkten. Spezielles Array, damit das Grafikmodul die bewegte Einheit anzeigen kann - wird von calcWayLength() erstellt
    ArrayList<Integer> pathDirection;                   // Für das Richtungssystem - Richtungen im Vorraus zu berechnen spart Ressourcen.
    boolean pathLengthCalced = false;                   // Wurde die Länge des Weges berechnet?
    public double length;                               // Exakte Länge des Weges, auf mehere Nachkommastellen genau (Pythagoras)
    public long startTime;                              // Ein Date-Objekt, das den Startzeitpunkt einer Bewegung enthält.
    public boolean movePaused = false;                  // Pause für Bewegungen?
    public long pauseTime;                              // Zeitpunkt der Pause
    public transient ServerBehaviourMove moveManager;   // Move-Verhalten (nur für Server) verwaltet die Bewegungen.
    public transient ServerBehaviourAttack attackManager; //Angriffsverhalten (nur für Server) verwaltet die Angiffe diese Einheit
    public int jumpTo = 0;                              // Die Einheit soll am Ende ihrer Bewegung in ein Gebäude "springen"
    public boolean jumpJustSet = false;                 // Die Sprung-Anweisung überlebt genau einen Bewegungsbefehl, dann wird sie gelöscht.
    public boolean isIntra = false;                     // Ist die Einheit grad IN einem Gebäude? Dann laufen die Behaviours nämlich net.
    public long jumpSetTime = 0;
    CoRMapElement lastref;                              // Letzte gesetzte Einheiten referenz
    public Position idleposition;                       // Das Feld auf demn die Einheit lauert.
    public double idlehuntradius;                       // Gibt an, wie weit die Einheit Feinde verfolgt
    public orders order;                                // Der momentane Befehl der Einheit
    public actions action;                              // Die momenane Aktion der Einheit, z.B. ernten, laufen, ...
    public double idlerange;                            // Aggro-Radius der Einheit
    // Animationssystem
    // @TODO: Das sollte man eigentlich zu RogUnitGraphicsData verschieben
    public UnitAnimator anim;       // Managed die Animationen dieser Einheit
    /**
     * Kampfsystem
     */
    // Parameter für Geschwindingkeitsänderung während des Laufens
    public int changespeedto = 0;

    /**
     * Konstruktor
     * initialisiert momentan nur position der einheit
     *
     * @param x         X-Koordinate
     * @param y         Y-Koordinate
     */
    public Unit(int x, int y, int newNetId) {
        super(newNetId);
        action = actions.nothing;       // Einheit macht noch gar nix
        graphicsdata = new GOGraphicsData();
        position = new Position(x, y);
        // Dieser Konstruktor setzt Standardwerte für hitpoints etc, da beim erstellen keine angegeben wurden und die Einheit unbedingt welche braucht, sonst gibt nullpointer
        hitpoints = 100;
        maxhitpoints = 100;
        armortype = "heavyinf";
        path = new ArrayList<Position>();
        order = orders.idle;
        pathSync = new Serializable() {
        };
    }

    /**
     * Konstruktor
     * initialisiert momentan nur position der einheit und hitpoints
     *
     * @param x         X-Koordinate
     * @param y         Y-Koordinate
     * @param maxHp     die Hitpoints
     */
    public Unit(int x, int y, int maxHp, int newNetId) {
        super(newNetId);
        graphicsdata = new GOGraphicsData();
        position = new Position(x, y);
        // Einheiten sind beim erstellen gesund, also Gesundheit voll
        hitpoints = maxHp;
        maxhitpoints = maxHp;
        armortype = "heavyinf";
        antiheavyinf = 100;
        antilightinf = 100;
        antikav = 100;
        antivehicle = 100;
        antitank = 100;
        antiair = 0;
        antibuilding = 100;
        path = new ArrayList<Position>();
        order = orders.idle;
        pathSync = new Serializable() {
        };
    }

    /**
     * Gibt zurück, ob die Einheit sich gerade bewegt.
     * @return: true, wenn die Einheit sich bewegt, sonst false.
     */
    public boolean isMoving() //bewegt die Einheit sich gerade?
    {
        if (movingtarget == null) {
            return false;
        }
        return true;
    }

    /**
     * Gibt die Geschwindigkeit zurück.
     * @return: die Geschwindigkeit als double.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Berechnet die Länge des Weges dieser Einheit.
     * Die berechnete Weglänge wird in length gespeichert.
     */
    public void calcWayLength() {
        synchronized (pathSync) {
            pathLengthCalced = true;
            length = 0;
            pathOrder = new ArrayList<Double>();
            pathDirection = new ArrayList<Integer>();
            pathOrder.add(0, (double) 0);
            for (int i = 1; i < path.size(); i++) {
                // Immer dazuaddieren
                Position t1 = path.get(i - 1);
                Position t2 = path.get(i);
                // Richtung bestimmen
                int vec = t2.subtract(t1).transformToIntVector();
                pathDirection.add(i - 1, new Integer(vec));
                // Strecke berechnen, mit Pytagoras
                double abschnitt = Math.sqrt(Math.pow(Math.abs(t1.X - t2.X), 2) + Math.pow(Math.abs(t1.Y - t2.Y), 2));
                length = length + abschnitt;
                pathOrder.add(i, Double.valueOf(length));
            }
            if (this.anim != null) {
                this.anim.dir = pathDirection.get(0);
            }
        }
    }

    /**
     * get-Funktion für lenght.
     * @return: lenght
     */
    public double getWayLength() {
        if (pathLengthCalced) {
            return length;
        } else {
            calcWayLength();
            return length;
        }
    }

    /**
     * Lässt die Einheit zu der angegebenen Position laufen.
     *
     * Client-Version der moveToPosition
     * Schickt den Request an den Server
     *
     * @param position Das Ziel der Einheit
     * @param rgi Der InnerClient, für Kollision & Netzwerkzugriff
     * @param IAL Bei IAL-Bewegungen laufen Einheiten (auf dem Server) im Flieh-Modus
     *
     */
    public void sendToPosition(Position target, ClientCore.InnerClient rgi, boolean allowDifferentTarget) {
        if (target == null || rgi == null) {
            System.out.println("FixMe: Calling sendToPosition with: " + target + "|" + rgi);
            return;
        }
        prepareMove();
        if (!target.equals(this.position) && !((movingtarget != null) && target.equals(this.movingtarget))) {
            // Da hin schicken
            rgi.netctrl.broadcastMove(this.netID, target, allowDifferentTarget);
        }
    }

    /**
     * Wie sendToPosition, nur ohne, dass die Einheit tatsächlich los geschickt wird.
     * Schält z.B. Behaviours ab, die bei Bewegung nicht laufen dürfen etc...
     */
    public void prepareMove() {
        if (this.order == orders.harvest) {
            ClientBehaviourHarvest harv = (ClientBehaviourHarvest) this.getbehaviourC(7);
            if (harv != null) {
                harv.stopHarvesting();
            }
        }
    }

    /**
     * Lässt die Einheit sofort auf diesem Pfad laufen
     * Client only
     * Übernimmt den Pfad bedingungslos und startet die Bewegung sofort vom
     * ersten Feld aus, die Unit wird eventuell versetzt, wenn path(0) != this.position
     *
     * @param newpath Der neue Weg der Unit
     */
    public void applyNewPath(ClientCore.InnerClient rgi, ArrayList<Position> newpath) {
        if (newpath.size() < 2) {
            // Ungültig ein Weg muss mindestens 2 Punkte haben
            System.out.println("FixMe: Path with only one Position!");
            return;
        }
        if (newpath == null) {
            System.out.println("FixMe: Can't switch path, it is null !? Debug: unit: " + this);
            return;
        }

        // Bauen anhalten

        ClientBehaviour cnstrct = getbehaviourC(5);
        if (cnstrct != null && cnstrct.isActive()) {
            cnstrct.deactivate();
        }

        synchronized (pathSync) {

            rgi.mapModule.setCollision(newpath.get(0), collision.free);
            rgi.mapModule.setUnitRef(newpath.get(0), null, playerId);
            this.movingtarget = newpath.get(newpath.size() - 1);
            this.path = newpath;
            this.order = orders.move;
            this.lastwaypoint = 0;
            this.calcWayLength();
            this.nextwaypoint = pathOrder.get(1);
            this.startTime = System.currentTimeMillis();
        }
    }

    /**
     * Wechselt auf diesen Weg, MUSS den alten enthalten
     * Client only
     *
     * @param newpath Der neue Weg der Unit MUSS DEN ALTEN ENTHALTEN
     */
    public synchronized void switchPath(ClientCore.InnerClient rgi, ArrayList<Position> newpath) {
        if (newpath == null || newpath.size() < 2) {
            // Ungültig ein Weg muss mindestens 2 Punkte haben
            System.out.println("FixMe: Path with only one Position!");
            return;
        }

        ClientBehaviour cnstrct = getbehaviourC(5);
        if (cnstrct != null && cnstrct.isActive()) {
            cnstrct.deactivate();
        }

        synchronized (pathSync) {
            this.movingtarget = newpath.get(newpath.size() - 1);
            this.path = newpath;
            this.calcWayLength();
            try {
                if (lastwaypoint + 1 > pathOrder.size() - 1) {
                    // Automatisch zurückstellen, falls Überschreitung festgestellt.
                    lastwaypoint = pathOrder.size() - 2;
                }
                nextwaypoint = pathOrder.get(lastwaypoint + 1);
            } catch (Exception ex) {
                System.out.println("FixMe: ERROR switching Path:");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Lässt die Einheit zum Gebäude laufen
     * Wenn die Route dorthin halbwegs der Luftlinie entspricht,
     * hält sie an der "kürzesten" Ecke an.
     *
     * @param building Das Ziel, zu dem sie laufen soll
     * @param inner die inner-Referenz
     * @param automatic Ob dieser Aufruf von einer Automatik (Behaviour) kommt
     */
    public void moveToBuilding(Building building, ServerCore.InnerServer inner, boolean automatic) {
        if (building == null) {
            return;
        }
        Position target = building.getNextFreeField(this.position, inner);
        if (target != null) {
            inner.moveMan.humanSingleMove(this, target, false);
        }

    }

    /**
     * Schickt diese Einheit los, um die Ressource abzuernten.
     * Einheit läuft selbstständig dort hin, und beginnt zu ernten, falls:
     * 1. Die Ressource direkt aberntbar ist (Typ 1-4) UND
     * 2. Dort noch Plätze frei sind (Max 4/8 Ernter, weniger wenn Nachbarfelder belegt)
     * 3. Die notwendige Epoche erreicht ist. (Metall ab 2, Gold ab 3)
     * Das Harvest-Behaviour wird automatisch initialtisiert, es bricht auch automatisch ab, wenn die Einheit abgezogen wird.
     *
     * @param res Die Zielressource, die abgeerntet werden soll
     * @param inner Die übliche Refernz auf alle Module.
     */
    public void goHarvest(Ressource res, ClientCore.InnerClient inner) {
        // Kann dieser Einheit überhaupt ernten?
        if (this.canHarvest) {
            // Sicherheitsabfrage
            if (res.getType() < 5) {
                // Epoche testen
                if (res.getType() < 3 || (res.getType() == Ressource.RES_METAL && inner.rogGraphics.content.epoche >= 2) || (res.getType() == Ressource.RES_COINS && inner.rogGraphics.content.epoche >= 3)) {
                    // Ok, kann geerntet werden.
                    // Nur weitermachen, wenn wir da nicht schon ernten
                    if (!res.isRegisteredHarvester(this)) {
                        // Noch was frei?
                        if (res.readyForAnotherHarvester(inner)) {
                            // Ja, Position holen
                            Position harvtarget = res.getNextFreeHarvestingPosition(this.position, this.position.subtract(res.position).transformToDiagonalVector(), inner);
                            if (harvtarget != null) {
                                if (res.addHarvester(this, harvtarget)) {
                                    // Ok frei, hinschicken
                                    this.sendToPosition(harvtarget, inner, false);
                                    // Start broadcasten
                                    inner.netctrl.broadcastDATA(inner.packetFactory((byte) 34, res.netID, this.netID, 0, 0));
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sucht Feinde in der Umgebung dieser Einheit.
     * Der searchDist - Wert wird in Kreisen um die Einheit angegeben und ist extrem Laufzeitkritisch.
     * Steigt nicht linear an, sondern nach PI*searchDist*searchDist (!)
     * Man sollte tunlichst vermeiden Werte über 7 zu nehmen
     * Als default zum Feinde suchen (für Idle) ist 4-5 geeignet.
     *
     * Führt automatisch "Wartungsarbeiten" durch, löscht z.B. veraltete Einträge raus
     *
     * @param searchDist Suchweite in Kreisen um die Einheit, sehr laufzeitkritisch, sollte < 6 sein!
     * @return Feindliche Einheit, falls gefunden.
     */
    public Unit enemyAroundMe(int searchDist, ClientCore.InnerClient rgi) {
        int i = 1;
        int kreis = 1;
        // Startfeld des Kreises:
        Position kreismember = new Position(position.X, position.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.Y -= 2;
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            } else {
                // Sprung in den nächsten Kreis
                kreismember.X--;
                kreismember.Y -= 3;
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
                if (rgi.rogGraphics.content.fowmap[kreismember.X][kreismember.Y] < 2 || rgi.mapModule.getEnemyUnitRef(kreismember, playerId) == null) {
                    i++;
                }
            } catch (Exception ex) {
            }
        }

        return rgi.mapModule.getEnemyUnitRef(kreismember, playerId);
    }

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
        Position kreismember = new Position(position.X, position.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.Y -= 2;
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            } else {
                // Sprung in den nächsten Kreis
                kreismember.X--;
                kreismember.Y -= 3;
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
                if (rgi.netmap.getEnemyUnitRef(kreismember.X, kreismember.Y, playerId) == null) {
                    i++;
                }
            } catch (Exception ex) {
            }
        }

        return rgi.netmap.getEnemyUnitRef(kreismember.X, kreismember.Y, playerId);
    }

    /**
     * Sucht Feinde in der Umgebung dieser Einheit und dazugehörige Angriffspositionen
     * Liefert die Position zurück, den Feind findet man dann mit der UnitRef
     * Der searchDist - Wert wird in Kreisen um die Einheit angegeben und ist extrem Laufzeitkritisch.
     * Steigt nicht linear an, sondern nach PI*searchDist*searchDist (!)
     * Man sollte tunlichst vermeiden Werte über 7 zu nehmen
     * Als default zum Feinde suchen (für Idle) ist 4-5 geeignet.
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
        Position kreismember = new Position(position.X, position.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.Y -= 2;
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            } else {
                // Sprung in den nächsten Kreis
                kreismember.X--;
                kreismember.Y -= 3;
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
                Unit unit = rgi.netmap.getEnemyUnitRef(kreismember.X, kreismember.Y, playerId);
                if (unit != null) {
                    Position potpos = unit.position.aroundMe(1, rgi, 8);
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
     * Sucht Feinde in der Umgebung dieser Einheit.
     * Der searchDist - Wert wird in Kreisen um die Einheit angegeben und ist Laufzeitkritisch.
     * Steigt nicht linear an, sondern nach PI*searchDist*searchDist (!)
     *
     * @param restype Welche Ressource (holz, nahrung etc.)
     * @param searchDist Suchweite in Kreisen um die Einheit
     * @return Feindliche Einheit, falls gefunden.
     */
    public Ressource ressourceAroundMe(int restype, int searchDist, ClientCore.InnerClient rgi) {
        int i = 1;
        int kreis = 1;
        // Startfeld des Kreises:
        Position kreismember = new Position(position.X, position.Y);
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.Y -= 2;
            } else if (k <= (kreis * 2)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (k <= (kreis * 4)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (k <= (kreis * 6)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (k <= ((kreis * 7) + kreis - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            } else {
                // Sprung in den nächsten Kreis
                kreismember.X--;
                kreismember.Y -= 3;
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
            if (rgi.mapModule.getResRef(kreismember) == null || rgi.mapModule.getResRef(kreismember).getType() != restype || !rgi.mapModule.getResRef(kreismember).readyForAnotherHarvester(rgi)) {
                i++;
            }
        }

        return rgi.mapModule.getResRef(kreismember);
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
                if (passedWay >= this.nextwaypoint) {

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

                    nextwaypoint = pathOrder.get(lastwaypoint + 1);
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

    /**
     * Gibt die aktuelle Position einer bewegten Einheit als Feld (also grob) zurück
     * Gibt bei irgendwelchen Fehlern -1|-1 zurück: Bewegt sich nicht, oder Angaben fehlen (speed starrtpunkt etc..)
     *
     * @deprecated
     * @return      Rogposition
     */
    public Position getMovingPositionFields() {


        try {
            length = getWayLength();
            // Rausfinden, wie weit das schon ist
            // Zurückgelegte Strecke:
            long now = System.currentTimeMillis();
            // Zeit
            long passedTime = now - startTime;
            if ((length / speed) * 1000 < passedTime) {
                // Weg zu Ende, endkoordinaten zurückgeben
                return path.get(path.size() - 1);
            }
            double passedWay = passedTime * speed / 1000;
            // Zuletzt erreichten Wegpunkt finden
            int lastWayPoint = 0;
            double diffLength = 0;
            for (int p = 1; p < pathOrder.size(); p++) {
                //  System.out.println("Punkt " + p + " ist nach " + unit.pathOrder.get(p));
                if (pathOrder.get(p) > passedWay) {
                    // Eines zu Weit, also Wegpunkt eins hintendran
                    lastWayPoint = p - 1;
                    diffLength = passedWay - pathOrder.get(lastWayPoint);
                    break;
                }
            }
            // Wir haben jetzt den letzten Punkt der Route, der bereits erreicht wurde, und die Strecke, die danach noch gefahren wurde...
            return path.get(lastWayPoint);
        } catch (Exception ex) {
            return new Position(-1, -1);
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
        retUnit.playerId = this.playerId;
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
        return ("RogUnit \"" + this.name + "\" ID: " + this.netID);
    }
}
