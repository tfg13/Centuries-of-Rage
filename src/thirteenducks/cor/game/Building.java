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

import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.AbilityIntraManager;
import thirteenducks.cor.graphics.BuildingAnimator;
import thirteenducks.cor.game.server.behaviour.impl.ServerBehaviourAttackB;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourRecruit;

/**
 *
 * @author tfg
 */
public class Building extends GameObject implements Serializable, Cloneable {

    public static final int ACCEPTS_ALL = 1;
    public static final int ACCEPTS_HARVESTERS_ONLY = 2;
    public String defaultTexture;                      // Muss da sein, sonst sieht man nix
    public String hudTexture;                          // Bild, das im Hud dargestellt wird - wenn nicht vorhanden wird defaultTexture skaliert...
    public int z1 = 2;                                 // Größe des Gebäudes in Richtung RECHTS OBEN
    public int z2 = 2;                                 // Größe des Gebäudes in Richtung RECHTS UNTEN
    public int offsetX = 0;                            // Wenn die Bilder der Gebäude wesentlich größer sind, als ihre tatsächliche Fläche (Graphics only!)
    public int offsetY = 0;                            // Wenn die Bilder der Gebäude wesentlich größer sind, als ihre tatsächliche Fläche (Graphics only!)
    public String Gdesc;                               // Für Grafikmodul/Baumenü, wird vom Typesmanager verwaltet und eingetragen
    public String Gimg;                                // Für Grafikmodul/Baumenü, wird vom Typesmanager verwaltet und eingetragen
    public BuildingAnimator anim;           // Grafikmodul only, für Animationen
    public double buildprogress;                       // Fortschritt des Bauens
    public boolean isbuilt = false;                    // Arbeitet schon jemand an diesem Gebäude?
    public boolean isWorking = false;                  // Gebäude am arbeiten?
    public int heal = 0;				// Heilen
    // Intra-System, Client only
    public ArrayList<Unit> intraUnits;              // Einheiten in diesem Gebäude
    public int harvests = 0;                           // Bei Rohstoffproduzierenden Gebäuden: Welche Ressource
    public int maxIntra = 0;                           // Wieviele Einheiten dürfen rein?
    public double harvRate = 0.0;                      // Ernterate PRO interner Einheit
    public int accepts = Building.ACCEPTS_HARVESTERS_ONLY; // Alles reinlassen oder nur ernter
    // Fog of War
    public boolean wasSeen = false;
    public transient ServerBehaviourAttackB attackManagerB;
    public int damageWhileContruction;

    public Building(int posX, int posY, int newNetId) {
        super(newNetId);
        // Billig-Konstruktor, 2x2 Häuschen
        position = new Position(posX, posY);
        intraUnits = new ArrayList<Unit>();
        visrange = 7;
    }

    @Override
    public Building clone(int newNetID) throws CloneNotSupportedException {
        // Kein echtes Klonen, es wird ein neues Building erzeugt
        // Nur einige Werte werden übernommen, nicht alle
        Building retBuilding = new Building(this.position.X, this.position.Y, newNetID);
        retBuilding.Gdesc = this.Gdesc;
        retBuilding.defaultTexture = this.defaultTexture;
        retBuilding.descTypeId = this.descTypeId;
        retBuilding.hitpoints = this.hitpoints;
        retBuilding.maxhitpoints = this.maxhitpoints;
        retBuilding.name = this.name;
        retBuilding.offsetX = this.offsetX;
        retBuilding.offsetY = this.offsetY;
        retBuilding.playerId = this.playerId;
        retBuilding.z1 = this.z1;
        retBuilding.z2 = this.z2;
        retBuilding.harvests = this.harvests;
        retBuilding.maxIntra = this.maxIntra;
        retBuilding.harvRate = this.harvRate;
        retBuilding.visrange = this.visrange;
        retBuilding.limit = this.limit;
	retBuilding.cooldown = this.cooldown;
	retBuilding.cooldownmax = this.cooldownmax;
	retBuilding.damage = this.damage;
	retBuilding.attacktarget = this.attacktarget;
	retBuilding.range = this.range;
	retBuilding.bulletspeed = this.bulletspeed;
	retBuilding.bullettexture = this.bullettexture;
	retBuilding.atkdelay = this.atkdelay;
	retBuilding.atkStart = this.atkStart;
	retBuilding.atkAnim = this.atkAnim;
	retBuilding.antiair = this.antiair;
	retBuilding.antibuilding = this.antibuilding;
	retBuilding.antiheavyinf = this.antiheavyinf;
	retBuilding.antikav = this.antikav;
	retBuilding.antilightinf = this.antilightinf;
	retBuilding.antitank = this.antitank;
	retBuilding.antivehicle = this.antivehicle;
	retBuilding.heal = this.heal;
        if (this.intraUnits != null) {
            retBuilding.intraUnits = (ArrayList<Unit>) this.intraUnits.clone();
        }
        // Special-Stuff
        retBuilding.position = this.position.clone(); // Unabhängige Variablen manuell klonen
        if (this.anim != null) {
            retBuilding.anim = this.anim.clone();
        }
        if (this.abilitys != null) {
            retBuilding.abilitys = Collections.synchronizedList(new ArrayList<Ability>());
            for (Ability a : this.abilitys) {
                retBuilding.abilitys.add(a.clone());
            }
        }
        return retBuilding;
    }

    /**
     * Sucht die beste Ecke in eine bestimmte Richtung heraus
     *
     * Server-Version
     *
     * @param from Die Position zu der die Ecke zeigen soll
     * @param inner Die Inner-Referenz
     * @return
     */
    public Position getBestEdge(Position from, ServerCore.InnerServer inner) {
        // Bestes Zielfeld suchen
        // Zielvektor ermitteln
        Position avec = from.subtract(this.position).transformToVector();
        Position dvec = this.position.subtract(from).transformToDiagonalVector();
        Position svec = this.position.subtract(from).transformToStraightVector();
        int edge = 0;
        // dvec bestimmt die kante, svec die Ecke
        if (dvec.X == -1 && dvec.Y == -1) {
            // Kante rechts unten
            if (svec.X == -1) {
                // Ecke rechts
                edge = 3;
            } else {
                // Ecke unten
                edge = 4;
            }
        } else if (dvec.X == -1 && dvec.Y == 1) {
            // Kante rechts oben
            if (svec.X == -1) {
                // Ecke oben
                edge = 2;
            } else {
                // Ecke rechts
                edge = 3;
            }
        } else if (dvec.X == 1 && dvec.Y == -1) {
            // Kante links unten
            if (svec.X == 1) {
                // Ecke links
                edge = 1;
            } else {
                // Ecke unten
                edge = 4;
            }
        } else if (dvec.X == 1 && dvec.Y == 1) {
            // Kante links oben
            if (svec.X == 1) {
                // Ecke oben
                edge = 2;
            } else {
                // Ecke links
                edge = 1;
            }
        }
        // Noch nix gefunden?
        if (edge == 0) {
            // Sonderfälle behandeln (genau auf D oder S-Linie)
            if (dvec.X == 0 && dvec.Y == 0) {
                // Wenn der D-Algorythmus nix rausfinden konnte nur nach svec die Ecke aussuchen
                if (svec.X == 0) {
                    if (svec.Y == 1) {
                        edge = 2;
                    } else {
                        edge = 4;
                    }
                } else if (svec.X == 1) {
                    edge = 1;
                } else {
                    edge = 4;
                }
            } else {
                // Der Svec hat versagt, dann ist es egal, Ecke raten
                if (dvec.X == 1) {
                    edge = 2;
                } else {
                    edge = 4;
                }
            }
        }
        // Die Zielecke returnen
        switch (edge) {
            case 1:
                return this.position.aroundMe(1, inner, avec);
            case 2:
                return new Position(this.position.X + this.z1 - 1, this.position.Y - this.z1 + 1).aroundMe(1, inner, avec);
            case 3:
                return new Position(this.position.X + this.z1 - 1 + this.z2 - 1, this.position.Y - this.z1 + 1 + this.z2 - 1).aroundMe(1, inner, avec);
            case 4:
                return new Position(this.position.X + this.z1 - 1, this.position.Y + this.z2 - 1).aroundMe(1, inner, avec);
        }
        // Ging irgendwie nicht
        return this.position.aroundMe(1, inner);
    }

    /**
     * Sucht das ein freies Feld um das Gebäude raus, das möglichts nahe am to-Vector liegt.
     * Besser als getBestEdge
     *
     * Verwendet den verbesserten IAL-Algorythmus
     * @param to
     * @param rgi
     * @return
     */
    public Position getNextFreeField(Position to, ServerCore.InnerServer rgi) {
        //Gebäude-Mitte finden:
        float bx = 0;
        float by = 0;
        //Z1
        //Einfach die Hälfte als Mitte nehmen
        bx = position.X + ((z1 - 1) * 1.0f / 2);
        by = position.Y - ((z1 - 1) * 1.0f / 2);
        //Z2
        // Einfach die Hälfte als Mitte nehmen
        bx += ((z2 - 1) * 1.0f / 2);
        by += ((z2 - 1) * 1.0f / 2);
        // Gebäude-Mitte gefunden
        // Winkel berechnen:
        float deg = (float) Math.atan((to.X - bx) / (by - to.Y));
        // Bogenmaß in Grad umrechnen (Bogenmaß ist böse (!))
        deg = (float) (deg / Math.PI * 180);
        // In 360Grad System umrechnen (falls negativ)
        if (deg < 0) {
            deg = 360 + deg;
        }
        // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
        if (to.X > bx && to.Y > by) {
            deg -= 180;
        } else if (to.X < bx && to.Y > by) {
            deg += 180;
        }
        if (deg == 0 || deg == -0) {
            if (to.Y > by) {
                deg = 180;
            }
        }
        // Zuteilung suchen (Ecke/Gerade(und welche?)
        Position posG = null; // Bezugsposition des Gebäudes
        if (deg < 22.5) {
            // Ecke, nach unten
            posG = new Position(position.X + (z1 - 1), position.Y - (z1 - 1));
        } else if (deg < 67.5) {
            // Gerade, nach links unten
            posG = new Position(position.X + (z1 - 1) + ((z2 - 1) / 2), position.Y - (z1 - 1) + ((z2 - 1) / 2));
        } else if (deg < 115.5) {
            // Ecke, nach links
            posG = new Position(position.X + (z1 - 1) + (z2 - 1), position.Y - (z1 - 1) + (z2 - 1));
        } else if (deg < 160.5) {
            // Gerade, nach links oben
            posG = new Position(position.X + ((z1 - 1) / 2) + (z2 - 1), position.Y - ((z1 - 1) / 2) + (z2 - 1));
        } else if (deg < 205.5) {
            // Ecke, nach oben
            posG = new Position(position.X + (z2 - 1), position.Y + (z2 - 1));
        } else if (deg < 250.5) {
            // Gerade, nach rechts oben
            posG = new Position(position.X + ((z2 - 1) / 2), position.Y + ((z2 - 1) / 2));
        } else if (deg < 295.5) {
            // Ecke, nach rechts
            posG = position;
        } else if (deg < 340.5) {
            // Gerade, nach rechts unten
            posG = new Position(position.X + ((z1 - 1) / 2), position.Y - ((z1 - 1) / 2));
        } else {
            // Nochmal Ecke nach unten
            posG = new Position(position.X + (z1 - 1), position.Y - (z1 - 1));
        }
        // Zuteilung gefunden.
        // Nächstes freies Feld zurückgeben
        return posG.aroundMe(1, rgi, posG.subtract(to).transformToVector());

    }

    /**
     * Sucht das ein freies Feld um das Gebäude raus, das möglichts nahe am to-Vector liegt.
     * Besser als getBestEdge
     *
     * Verwendet den verbesserten IAL-Algorythmus
     * @param to
     * @param rgi
     * @return
     */
    public Position getNextFreeField(Position to, ClientCore.InnerClient rgi) {
        //Gebäude-Mitte finden:
        float bx = 0;
        float by = 0;
        //Z1
        //Einfach die Hälfte als Mitte nehmen
        bx = position.X + ((z1 - 1) * 1.0f / 2);
        by = position.Y - ((z1 - 1) * 1.0f / 2);
        //Z2
        // Einfach die Hälfte als Mitte nehmen
        bx += ((z2 - 1) * 1.0f / 2);
        by += ((z2 - 1) * 1.0f / 2);
        // Gebäude-Mitte gefunden
        // Winkel berechnen:
        float deg = (float) Math.atan((to.X - bx) / (by - to.Y));
        // Bogenmaß in Grad umrechnen (Bogenmaß ist böse (!))
        deg = (float) (deg / Math.PI * 180);
        // In 360Grad System umrechnen (falls negativ)
        if (deg < 0) {
            deg = 360 + deg;
        }
        // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
        if (to.X > bx && to.Y > by) {
            deg -= 180;
        } else if (to.X < bx && to.Y > by) {
            deg += 180;
        }
        if (deg == 0 || deg == -0) {
            if (to.Y > by) {
                deg = 180;
            }
        }
        // Zuteilung suchen (Ecke/Gerade(und welche?)
        Position posG = null; // Bezugsposition des Gebäudes
        if (deg < 22.5) {
            // Ecke, nach unten
            posG = new Position(position.X + (z1 - 1), position.Y - (z1 - 1));
        } else if (deg < 67.5) {
            // Gerade, nach links unten
            posG = new Position(position.X + (z1 - 1) + ((z2 - 1) / 2), position.Y - (z1 - 1) + ((z2 - 1) / 2));
        } else if (deg < 115.5) {
            // Ecke, nach links
            posG = new Position(position.X + (z1 - 1) + (z2 - 1), position.Y - (z1 - 1) + (z2 - 1));
        } else if (deg < 160.5) {
            // Gerade, nach links oben
            posG = new Position(position.X + ((z1 - 1) / 2) + (z2 - 1), position.Y - ((z1 - 1) / 2) + (z2 - 1));
        } else if (deg < 205.5) {
            // Ecke, nach oben
            posG = new Position(position.X + (z2 - 1), position.Y + (z2 - 1));
        } else if (deg < 250.5) {
            // Gerade, nach rechts oben
            posG = new Position(position.X + ((z2 - 1) / 2), position.Y + ((z2 - 1) / 2));
        } else if (deg < 295.5) {
            // Ecke, nach rechts
            posG = position;
        } else if (deg < 340.5) {
            // Gerade, nach rechts unten
            posG = new Position(position.X + ((z1 - 1) / 2), position.Y - ((z1 - 1) / 2));
        } else {
            // Nochmal Ecke nach unten
            posG = new Position(position.X + (z1 - 1), position.Y - (z1 - 1));
        }
        // Zuteilung gefunden.
        // Nächstes freies Feld zurückgeben
        return posG.aroundMe(1, rgi, posG.subtract(to).transformToVector());
    }

    /**
     * Sucht die beste Ecke in eine bestimmte Richtung heraus
     *
     * Client-Version
     *
     * @param from Die Position zu der die Ecke zeigen soll
     * @param inner Die Inner-Referenz
     * @return
     */
    public Position getBestEdge(Position from, ClientCore.InnerClient inner) {
        // Bestes Zielfeld suchen
        // Zielvektor ermitteln
        Position avec = from.subtract(this.position).transformToVector();
        Position dvec = this.position.subtract(from).transformToDiagonalVector();
        Position svec = this.position.subtract(from).transformToStraightVector();
        int edge = 0;
        // dvec bestimmt die kante, svec die Ecke
        if (dvec.X == -1 && dvec.Y == -1) {
            // Kante rechts unten
            if (svec.X == -1) {
                // Ecke rechts
                edge = 3;
            } else {
                // Ecke unten
                edge = 4;
            }
        } else if (dvec.X == -1 && dvec.Y == 1) {
            // Kante rechts oben
            if (svec.X == -1) {
                // Ecke oben
                edge = 2;
            } else {
                // Ecke rechts
                edge = 3;
            }
        } else if (dvec.X == 1 && dvec.Y == -1) {
            // Kante links unten
            if (svec.X == 1) {
                // Ecke links
                edge = 1;
            } else {
                // Ecke unten
                edge = 4;
            }
        } else if (dvec.X == 1 && dvec.Y == 1) {
            // Kante links oben
            if (svec.X == 1) {
                // Ecke oben
                edge = 2;
            } else {
                // Ecke links
                edge = 1;
            }
        }
        // Noch nix gefunden?
        if (edge == 0) {
            // Sonderfälle behandeln (genau auf D oder S-Linie)
            if (dvec.X == 0 && dvec.Y == 0) {
                // Wenn der D-Algorythmus nix rausfinden konnte nur nach svec die Ecke aussuchen
                if (svec.X == 0) {
                    if (svec.Y == 1) {
                        edge = 2;
                    } else {
                        edge = 4;
                    }
                } else if (svec.X == 1) {
                    edge = 1;
                } else {
                    edge = 4;
                }
            } else {
                // Der Svec hat versagt, dann ist es egal, Ecke raten
                if (dvec.X == 1) {
                    edge = 2;
                } else {
                    edge = 4;
                }
            }
        }
        // Die Zielecke returnen
        switch (edge) {
            case 1:
                return this.position.aroundMe(1, inner, avec);
            case 2:
                return new Position(this.position.X + this.z1 - 1, this.position.Y - this.z1 + 1).aroundMe(1, inner, avec);
            case 3:
                return new Position(this.position.X + this.z1 - 1 + this.z2 - 1, this.position.Y - this.z1 + 1 + this.z2 - 1).aroundMe(1, inner, avec);
            case 4:
                return new Position(this.position.X + this.z1 - 1, this.position.Y + this.z2 - 1).aroundMe(1, inner, avec);
        }
        // Ging irgendwie nicht
        return this.position.aroundMe(1, inner);
    }

    /**
     * Ist noch ein Intra-Platz frei?
     * @return true, wenn frei sonst false
     */
    public int intraFree() {
        return maxIntra - intraUnits.size();
    }

    /**
     * Lässt die Einheit zu diesem Gebäude laufen. Sie springt nur rein, wenn "nacher" noch was frei ist.
     * @param unit
     */
    public void goIntra(Unit unit, ClientCore.InnerClient rgi) {
        // Jump setzen
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 41, unit.netID, this.netID, 0, 0));
        // Hinlaufen lassen
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 26, unit.netID, this.netID, 0, 0));
    }

    public void addIntra(Unit unit, ServerCore.InnerServer inner) {
        // Reinjumpen lassen
        intraUnits.add(unit);
        unit.isIntra = true;
        inner.netmap.setCollision(unit.position, collision.free);
        inner.netmap.setUnitRef(unit.position, null, unit.playerId);
        inner.netmap.unitList.remove(unit);
    }

    public void addIntra(Unit unit, ClientCore.InnerClient inner) {
        // Reinjumpen lassen
        intraUnits.add(unit);
        unit.isIntra = true;
        inner.mapModule.setCollision(unit.position, collision.free);
        inner.mapModule.setUnitRef(unit.position, null, unit.playerId);
        inner.mapModule.unitList.remove(unit);
        try {
            inner.rogGraphics.content.allListLock.lock();
            inner.mapModule.allList.remove(unit);
        } finally {
            inner.rogGraphics.content.allListLock.unlock();
        }
        
        inner.rogGraphics.triggerUpdateHud();
        ((AbilityIntraManager) this.getAbility(-2)).updateIntra();
        this.isWorking = true;
    }

    public void removeUnit(ServerCore.InnerServer inner) {
        // Die Erstbeste nehmen
        Unit jump = intraUnits.get(0);
        intraUnits.remove(0);
        jump.isIntra = false;
        // Wenns geht, dann an die alte Position
        Position newpos = jump.position;
        if (inner.netmap.isGroundColliding(jump.position)) {
            newpos = this.getBestEdge(newpos, inner).aroundMe(0, inner);
        }
        jump.position = newpos;
        inner.netmap.setCollision(newpos, collision.occupied);
        inner.netmap.setUnitRef(newpos, jump, jump.playerId);
        inner.netmap.unitList.add(jump);
        // Broadcasten
        inner.netctrl.broadcastDATA(inner.packetFactory((byte) 42, this.netID, jump.netID, jump.position.X, jump.position.Y));
    }

    public void removeAll(ServerCore.InnerServer inner) {
        while (!intraUnits.isEmpty()) {
            this.removeUnit(inner);
        }
    }

    public void removeUnit(Unit unit, Position pos, ClientCore.InnerClient inner) {
        // Unit löschen
        intraUnits.remove(unit);
        // Auf Map setzen
        unit.isIntra = false;
        unit.position = pos;
        inner.mapModule.setCollision(position, collision.blocked);
        inner.mapModule.setUnitRef(pos, unit, unit.playerId);
        inner.mapModule.unitList.add(unit);
        try {
            inner.rogGraphics.content.allListLock.lock();
            inner.mapModule.allList.add(unit);
        } finally {
            inner.rogGraphics.content.allListLock.unlock();
        }
        inner.rogGraphics.triggerUpdateHud();
        ((AbilityIntraManager) this.getAbility(-2)).updateIntra();
        if (intraUnits.isEmpty()) {
            this.isWorking = false;
        }
    }

    /**
     * Prüft, ob die mitgelieferte Einheit auf einem Nachbarfeld des Gebäudes steht.
     * Ecken zählen dazu, die Einheit muss stillstehen.
     * @param unit
     */
    public boolean isAroundMe(Unit unit, ClientCore.InnerClient inner) {
        int ux = unit.position.X;
        int uy = unit.position.Y;
        // Position diese Gebäudes
        int x = this.position.X;
        int y = this.position.Y;
        int possibilitys = 4 + (z1 * 2) + (z2 * 2);
        // Alle Felder um das Gebäude rum testen
        // Zuerst die Start-Ecke (links)
        x -= 2;
        if (x == ux && y == uy) {
            return true;
        }
        // Jetzt nach rechts oben gehen (+- z1) ((eines weiter gehen, damit ist gleich die obere ecke mit getestet))
        for (int i = 0; i <= z1; i++) {
            x++;
            y--;
            if (x == ux && y == uy) {
                return true;
            }
        }
        // Jetzt nach rechts unten gehen (++ z2) ((eines weiter gehen, damit ist gleich die obere ecke mit getestet))
        for (int i = 0; i <= z2; i++) {
            x++;
            y++;
            if (x == ux && y == uy) {
                return true;
            }
        }
        // Jetzt nach links unten gehen (-- z1) ((eines weiter gehen, damit ist gleich die obere ecke mit getestet))
        for (int i = 0; i <= z1; i++) {
            x--;
            y++;
            if (x == ux && y == uy) {
                return true;
            }
        }
        // Jetzt nach links oben gehen (-- z1)
        for (int i = 1; i <= z1; i++) {
            x--;
            y--;
            if (x == ux && y == uy) {
                return true;
            }
        }
        return false;
    }

    public Unit enemyAroundMe(int searchDist, Position Middle, ClientCore.InnerClient rgi) {
        int i = 1;
        int kreis = 2;
        // Startfeld des Kreises:
        Position kreismember = new Position(Middle.X, Middle.Y);
	if (Middle.X % 2 != Middle.Y % 2) {
	    kreismember.Y --;
	    kreis = 3;
	}
        // Jetzt im Kreis herum gehen
        for (int k = 0; k < i; k++) {
            // Es gibt vier Schritte, welcher ist als nächster dran?
            if (k == 0) {
                // Zum allerersten Feld springen
                kreismember.Y -= 2;
            } else if (k <= (kreis)) {
                // Der nach links unten
                kreismember.X--;
                kreismember.Y++;
            } else if (k <= (kreis * 2)) {
                // rechts unten
                kreismember.X++;
                kreismember.Y++;
            } else if (k <= (kreis * 3)) {
                // rechts oben
                kreismember.X++;
                kreismember.Y--;
            } else if (k <= ((kreis * 4) - 1)) {
                // links oben
                kreismember.X--;
                kreismember.Y--;
            } else {
                // Sprung in den nächsten Kreis
                kreismember.X--;
                kreismember.Y -= 3;
                k = 0;
                i = i - (kreis * 4);
                kreis += 2;
                // Suchende Erreicht?
                if (kreis > searchDist) {
                    // Ende, nix gefunden --> null
                    return null;
                }
            }
            // Ist dieses Feld NICHT geeignet?
            try {
                if (rgi.rogGraphics.content.fowmap[kreismember.X][kreismember.Y] < 2 || rgi.mapModule.getEnemyUnitRef(kreismember, playerId) == null || rgi.mapModule.getEnemyUnitRef(kreismember, playerId).position.getDistance(Middle) > this.range) {
                    i++;
                }
            } catch (Exception ex) {
            }
        }

        return rgi.mapModule.getEnemyUnitRef(kreismember, playerId);
    }

    @Override
    public void destroy() {
        // Truppenlimit von laufenden Rekrutierungsaufträgen wieder freigeben
        ClientBehaviour be = this.getbehaviourC(6);
        if (be != null) {
            ClientBehaviourRecruit recr = (ClientBehaviourRecruit) be;
            recr.freeLimits();
        }
    }
}
