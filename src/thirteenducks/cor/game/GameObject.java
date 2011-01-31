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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.AbilityIntraManager;
import thirteenducks.cor.game.ability.AbilityRecruit;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.game.ability.ServerAbilityUpgrade;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.game.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.behaviour.impl.ServerBehaviourHeal;
import thirteenducks.cor.graphics.GOGraphicsData;
import thirteenducks.cor.graphics.Sprite;
import thirteenducks.cor.graphics.input.InteractableGameElement;
import thirteenducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourUpgrade;

/**
 * Superklasse für "Spielobjekte". Das werden vor allem Einheiten und Gebäude sein.
 *
 */
public abstract class GameObject implements Serializable, Sprite, BehaviourProcessor, InteractableGameElement, Hideable {

    /**
     * Dieses Objekt lebt noch nicht.
     * z.B. ein Gebäude als Baustelle
     */
    public static final int LIFESTATUS_UNBORN = 0;
    /**
     * Dieses Objekt lebt. (Normalzustand)
     */
    public static final int LIFESTATUS_ALIVE = 1;
    /**
     * Dieses Objekt ist gestorben.
     */
    public static final int LIFESTATUS_DEAD = 2;
    /**
     * Dieses Objekt tut gerade nichts. (Normalzustand)
     */
    public static final int STATUS_IDLE = 0;
    /**
     * Dieses Objekt bewegt sich gerade.
     */
    public static final int STATUS_MOVING = 1;
    /**
     * Dieses Objekt arbeitet gerade.
     */
    public static final int STATUS_WORKING = 2;
    /**
     * Agressive Bewegung zum Ziel. Ziele in Reichweite werden während der Bewegung angegriffen,
     * das Objekt bliebt notfalls auch stehen.
     * Default-Bewegungsmodus bei Rechtsklick auf den Boden.
     */
    public static final int MOVE_AGGRESSIVE = 0;
    /**
     * Direkte Bewegung ("fliehen").
     * Das Objekt wird auf dem Weg nicht stehen bleiben um Feinde zu bekämpfen,
     * außer das Ziel wird durch eine vollständige Blockade unerreichbar.
     * Wird durch einen doppel-Rechtsklick (Boden) aktiviert.
     */
    public static final int MOVE_DIRECT = 1;
    /**
     * Agressive Bewegung zum Ziel / anschließend wird es angegriffen.
     * Auf dem Weg werden Feinde bekämpft, eventuell bleibt das Objekt auch stehen.
     * Das hat zur Folge, dass auch andere Objekte angegriffen werden, wenn man so eine ganze
     * Gruppe anklickt.
     * Default-Angriffsmodus beim Rechtsklick auf Feinde.
     */
    public static final int ATK_AGGRESSIVE = 2;
    /**
     * Das Objekt versucht nah genug an das Ziel heranzukommen, damit es angegriffen werden kann.
     * Das Objekt wird keine anderen Einheiten angreiffen, solange noch die Möglichkeit besteht,
     * das gesetzte Ziel zu erreichen.
     * Dieses "Focus-Fire" kann mit einem Doppel-Rechtsklick auf Feinde aktiviert werden.
     */
    public static final int ATK_FOCUS = 3;
    /**
     * Leichte Infantrie
     */
    public static final int ARMORTYPE_LIGHTINF = 0;
    /**
     * Schwere Infantrie
     */
    public static final int ARMORTYPE_HEAVYINF = 1;
    /**
     * Kavallerie
     */
    public static final int ARMORTYPE_KAV = 2;
    /**
     * Leichte Fahrzeuge / Kriegsmaschinen a la Katapult
     */
    public static final int ARMORTYPE_VEHICLE = 3;
    /**
     * Schwere Fahrzeuge / Panzer
     */
    public static final int ARMORTYPE_TANK = 4;
    /**
     * Lufteinheiten
     */
    public static final int ARMORTYPE_AIR = 5;
    /**
     * Gebäude
     */
    public static final int ARMORTYPE_BUILDING = 6;
    /**
     * Ein dieses Objekt beschreibender String, bei allen Objekten dieses Typs gleich
     * z.B. "Bogenschütze"
     */
    private String descName;
    /**
     * Zeigt, ob das Objekt derzeit selektier ist. Wird vom Selektionssystem gesetzt.
     */
    private boolean isSelected = false;
    /**
     * Die PlayerID des Spielers, der dieses Objekt aktuell kontrolliert
     * Die PlayerID's beginnen mit 1, 0 bedeutet neutral
     */
    private int playerId = 0;
    /**
     * Die derzeitige Zuordnungsposition des Objekts.
     * Die Zuordnungsposition ist die Position ganz links.
     */
    protected Position mainPosition;
    /**
     * Die Rüstungsklasse diese Objekts
     */
    protected int armorType;
    /**
     * Schaden gegen andere Rüstungsklassen in Prozent.
     * Default ist 100
     */
    private int[] damageFactors;
    /**
     * Aktuelle Lebensenergie dieses Objekts.
     * Normalerweise stirbt das Objekt bei <=0
     */
    protected int hitpoints;
    /**
     * Maximale Lebensenergie dieses Objekts.
     */
    protected int maxhitpoints;
    /**
     * Der Typ der folgenden descTypeId.
     * Derzeit werden vor allem B für Gebäude und U für Einheiten genutzt.
     */
    private char descTypeType;
    /**
     * Die ID-Nummer dieser Einheit.
     * Wird für Zuordnungen mehrerer Einheiten des gleichen Typs verwendet.
     */
    private int descTypeId;
    /**
     * Die Liste mit den Abilitys dieses Objekts.
     * Abilitys werden im Hud als anklickbare Knöpfe dargestellt.
     */
    protected List<Ability> abilitys;
    /**
     * Die Server-Behaviour dieses Objekts.
     * Werden regelmäßig vom GameLogic-Thread aufgerufen.
     */
    private List<ServerBehaviour> sbehaviours;
    /**
     * Die Client-Behaviour dieses Objekts.
     * Werden regelmäßig vom GameLogic-Thread aufgerufen.
     */
    private List<ClientBehaviour> cbehaviours;
    /**
     * Default-Wegpunkt.
     * Derzeit verwendet für das erste Laufziel neu erzeugter Einheiten
     */
    private Position waypoint;
    /**
     * Der derzeitige Lebens-Zustand dieses Objekts.
     * Kann soetwas wie "am Leben" oder "schon gestorben" sein.
     */
    private int lifeStatus;
    /**
     * Bewegungs/Angriffszustand dieses Objekts.
     * z.B. Aggressive Bewegung, FocusFire
     */
    private int moveAtkMode;
    /**
     * Was die Einheit gerade tut.
     * z.B. "nichts" "gehen" "arbeiten"
     */
    private int status;
    /**
     * Die Sichtweite dieses Objekts im FoW.
     * Gemessen in eckigen Kreisen um das Zentrum, (sogut es ganzzahlig geht)
     */
    private int visrange;
    /**
     * Die dieses Objekt in der derzeit laufenden Spielpartie netzwerkweit eindeutig identifizierende Id.
     */
    public final int netID;
    /**
     * Diese Anzahl von Millisekunden müssen mindestens zwischen 2 Schlägen/Schüssen vergehen.
     */
    private int fireDelay;
    /**
     * Basis-Schaden dieses Objekts.
     */
    private int damage;
    /**
     * Die Reichweite dieser Einheit.
     * Echte Entfernungsmessung zwischen(!) den Einheiten.
     * Nahkämpfer haben also etwa 0
     */
    protected double range;
    /**
     * Geschossgeschwindigkeit.
     * 0 ist für Nahkämpfer, also Instant-Hit.
     */
    private int bulletspeed = 15;
    /**
     * Geschosstextur. Nahkämpfer haben keine.
     */
    private String bullettexture;
    /**
     * Delay in Millisekunden zwischen dem Beginn eines Angriffs ("ausholen") und dem "zuschlagen". In dieser Zeit wird die Angriffsanimation abgespielt und die Einheit von der Grafikengine etwas vor bewegt. (nur Nahkampf)
     */
    private int atkdelay = 0;
    /**
     * Enthält alle Grafik- & Animationsdaten
     */
    private GOGraphicsData graphicsData;
    /**
     * Ein beschreibender String für GUI-Infos.
     * z.B. "Ein langsamer aber zäher Kämpfer"
     */
    private String descDescription;
    /**
     * Ein beschreibender String für GUI-Infos.
     * Sagt, gegen was dieses Objekt besonders stark ist.
     * Optional
     * z.B. "Light Infantry"
     */
    private String descPro;
    /**
     * Ein beschreibender String für GUI-Infos.
     * Sagt, gegen was dieses Objekt besonders schwach ist.
     * Optional
     * z.B. "Heavy Infantry"
     */
    private String descCon;
    /**
     * Heilrate dieses Objekts.
     * Mit dieser Rate heilt es andere in seiner Nähe, nicht sich selbst(!)
     */
    private int healRate = 0;
    /**
     * Die Parameter dieses Objekts.
     * Die Parameter wurden ursprünglich von dieser Parameterliste kopiert.
     * Die Parameter stimmen in der Regel nichtmehr mit den aktuellen dieses Objekts überein.
     */
    private descParamsGO params;

    /**
     * Erzeugt ein neues GameObject mit der angegebenen ID an der Stelle mainPos
     * @param newNetId Die netId dieses Objekts
     * @param mainPos Die Haupt-Zuordnungsposition
     */
    protected GameObject(int newNetId, Position mainPos) {
        netID = newNetId;
        mainPosition = mainPos;
        this.abilitys = new ArrayList<Ability>();
        this.lifeStatus = GameObject.LIFESTATUS_ALIVE;
        this.status = GameObject.STATUS_IDLE;
    }

    /**
     * Erzeugt ein Platzhalter-GameObject, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    protected GameObject(descParamsGO params) {
        netID = -1;
    }

    /**
     * Erzeugt ein neues GameObject als eigenständige Kopie des Übergebenen.
     * Wichtige Parameter werden kopiert, Sachen die jedes Objekt selber haben sollte nicht.
     * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
     * @param newNetId Die netId des neuen Objekts
     * @param copyFrom Das Objekt, dessen Parameter kopiert werden sollen
     */
    protected GameObject(int newNetId, GameObject copyFrom) {
        this(newNetId, new Position(0, 0));
        for (Ability ab : copyFrom.abilitys) {
            try {
                this.abilitys.add(ab.clone());
            } catch (Exception ex) {
                System.out.println("NEVEREVER: Cannot clone Ability!");
            }
        }
        this.armorType = copyFrom.armorType;
        this.atkdelay = copyFrom.atkdelay;
        this.bulletspeed = copyFrom.bulletspeed;
        this.bullettexture = copyFrom.bullettexture;
        this.damage = copyFrom.damage;
        this.damageFactors = copyFrom.getDamageFactors().clone();
        this.descCon = copyFrom.descCon;
        this.descDescription = copyFrom.descDescription;
        this.descName = copyFrom.descName;
        this.descPro = copyFrom.descPro;
        this.descTypeId = copyFrom.descTypeId;
        this.descTypeType = copyFrom.descTypeType;
        this.fireDelay = copyFrom.fireDelay;
        this.graphicsData = copyFrom.graphicsData.clone();
        this.healRate = copyFrom.healRate;
        this.hitpoints = copyFrom.hitpoints;
        this.maxhitpoints = copyFrom.maxhitpoints;
        this.playerId = copyFrom.playerId;
        this.range = copyFrom.range;
        this.visrange = copyFrom.visrange;
    }

    /**
     * Wendet die Parameterliste an (kopiert die Parameter rein)
     * @param par
     */
    private void applyParams(descParamsGO par) {
        this.armorType = par.getArmorType();
        this.atkdelay = par.getAtkdelay();
        this.bulletspeed = par.getBulletspeed();
        this.bullettexture = par.getBullettexture();
        this.damage = par.getDamage();
        this.damageFactors = par.getDamageFactors();
        this.descCon = par.getDescCon();
        this.descDescription = par.getDescDescription();
        this.descName = par.getDescName();
        this.descPro = par.getDescPro();
        this.descTypeId = par.getDescTypeId();
        this.fireDelay = par.getFireDelay();
        this.graphicsData = par.getGraphicsData().clone();
        this.healRate = par.getHealRate();
        this.hitpoints = par.getHitpoints();
        this.maxhitpoints = par.getMaxhitpoints();
        this.range = par.getRange();
        this.visrange = par.getVisrange();
        this.abilitys = new ArrayList<Ability>();
        for (Ability ab : par.getAbilitys()) {
            try {
                this.abilitys.add(ab.clone());
            } catch (Exception ex) {
                System.out.println("NEVEREVER: Cannot clone Ability!");
            }
        }
    }

    /**
     * @return the visrange
     */
    public int getVisrange() {
        return visrange;
    }

    /**
     * @param visrange the visrange to set
     */
    public void setVisrange(int visrange) {
        this.visrange = visrange;
    }

    public abstract Position freeDirectAroundMe();

    /**
     * Überprüft, ob die Angegebene Position zur Zeit ein direktes Nachbarfeld dieses Objekts ist.
     * Ecken zählen dazu, innen liegende Felder nicht.
     * @param pos die zu prüfende Position
     * @return true, wenn die Position ein direktes Nachbarfeld, sonst false
     */
    public abstract boolean isAroundMe(Position pos);

    /**
     * Returns the MainPosition
     * @return the MainPosition
     */
    public Position getMainPosition() {
        try {
            return mainPosition.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Error cloning Position!", ex);
        }
    }

    /**
     * @return the descTypeType
     */
    public char getDescTypeType() {
        return descTypeType;
    }

    /**
     * @return the descTypeId
     */
    public int getDescTypeId() {
        return descTypeId;
    }

    /**
     * @return the descName
     */
    public String getName() {
        return descName;
    }

    /**
     * @param playerId the playerId to set
     */
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    /**
     * Liefert alle Positionen, auf denen ein GO derzeit steht.
     * @return
     */
    public abstract Position[] getPositions();

    /**
     * Liefert eine Position, die möglichst der Mitte dieses Objekts entspricht.
     * @return
     */
    public abstract Position getCentralPosition();

    /**
     * @param mainPosition the mainPosition to set
     */
    public void setMainPosition(Position mainPosition) {
        this.mainPosition = mainPosition;
    }

    /**
     * @return the playerId
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Fügt dieses Behaviour diesem GameObject hinzu
     * @param b
     */
    public void addClientBehaviour(ClientBehaviour b) {
        cbehaviours.add(b);
    }

    /**
     * Sucht ein Behaviour anhand dessen Id.
     * Liefert null, fall es nicht existiert.
     * @param id Die Id des gesuchten Behaviours
     * @return
     */
    public ClientBehaviour getClientBehaviour(int id) {
        for (ClientBehaviour b : this.cbehaviours) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    /**
     * Löscht das Behaviour sofort aus der Liste.
     * Das Behaviour wird danach nie wieder ausgeführt.
     * @param b
     */
    public void removeClientBehaviour(ClientBehaviour b) {
        cbehaviours.remove(b);
    }

    /**
     * Fügt dieses Behaviour diesem GameObject hinzu
     * @param b
     */
    public void addServerBehaviour(ServerBehaviour b) {
        sbehaviours.add(b);
    }

    /**
     * Fügt diese Ability diesem GameObject hinzu
     * @param b
     */
    public void addAbility(Ability a) {
        abilitys.add(a);
    }

    /**
     * Sucht ein UpgradeBehaviour anhand der descTypeId der zugehörigen Ability
     * @param searchDESC Die Desc-Id
     * @return RogGameObjectAbiliyUpgrade, falls gefunden, sonst null
     */
    public ClientBehaviourUpgrade getUpgradeBehaviour(int searchDESC) {
        for (ClientBehaviour b : this.cbehaviours) {
            if (b.getId() == 8) {
                // Das ist ein Upgrade, richtige Desc?
                ClientBehaviourUpgrade up = (ClientBehaviourUpgrade) b;
                if (up.ability.myId == searchDESC) {
                    // Das ist es
                    return up;
                }
            }
        }
        return null;
    }

    /**
     * Ändert alle Parameter dieses GO auf die der angegebenen
     * DescTypeId (Führt ein Upgrade dieses GO auf ein anderes aus)
     *
     * Funktioniert nur auf Units und Buildings
     *
     * Client Version
     * @param toDesc Dieses GO erhält alle Parameter der toDesc.
     */
    public void performUpgrade(ClientCore.InnerClient rgi, int toDesc) {
        if (this.getClass().equals(Building.class)) {
            Building building = rgi.mapModule.getDescBuilding(toDesc, -1, this.playerId);
            if (building == null) {
                // Abbrechen, das wird nichts.
                return;
            }
            copyPropertiesFrom(building);
            this.abilitys.add(new AbilityIntraManager((Building) this, rgi));
            /*   BuildingAnimator rgba = rgi.game.getPlayer(this.playerId).descBuilding.get(toDesc).anim;
            if (rgba != null) {
            rgba = rgba.clone();
            } 
            ((Building) this).anim = rgba; */
            System.out.println("AddMe: Upgrade GraphicsData & Animator");
            // Fertig
        } else if (this.getClass().equals(Unit.class)) {
            Unit unit = rgi.mapModule.getDescUnit(toDesc, -1, this.playerId);
            if (unit == null) {
                // Abbrechen, das wird nichts.
                return;
            }
            // Eigenschaften übernehmen
            copyPropertiesFrom(unit);

            /* UnitAnimator rgua = rgi.game.getPlayer(this.playerId).descUnit.get(toDesc).anim;
            if (rgua != null) {
            rgua = rgua.clone();
            }
            ((Unit) this).anim = rgua; */
            System.out.println("AddMe: Upgrade GraphicsData & Animator");
        }
        rgi.rogGraphics.triggerUpdateHud();
    }

    /**
     * Ändert alle Parameter dieses GO auf die der angegebenen
     * DescTypeId (Führt ein Upgrade dieses GO auf ein anderes aus)
     *
     * Funktioniert nur auf Units und Buildings
     *
     * Server-Version
     * @param toDesc Dieses GO erhält alle Parameter der toDesc.
     */
    public void performUpgrade(ServerCore.InnerServer rgi, int toDesc) {
        if (this.getClass().equals(Building.class)) {
            Building building = rgi.netmap.getDescBuilding(this.playerId, toDesc);
            if (building == null) {
                // Abbrechen, das wird nichts.
                return;
            }
            copyPropertiesFrom(building);
            // Fertig
        } else if (this.getClass().equals(Unit.class)) {
            Unit unit = rgi.netmap.getDescUnit(this.playerId, toDesc);
            if (unit == null) {
                // Abbrechen, das wird nichts.
                return;
            }
            // Eigenschaften übernehmen
            copyPropertiesFrom(unit);
        }
    }

    public void copyPropertiesFrom(Unit unit) {
        // Eigenschaften übernehmen
        this.damageFactors = unit.getDamageFactors();
        this.descTypeId = unit.getDescTypeId();
        this.armorType = unit.getArmorType();
        // Gesundheit prozentual erhöhen
        float perc = 1.0f * this.hitpoints / this.maxhitpoints;
        this.maxhitpoints = unit.maxhitpoints;
        this.hitpoints = (int) (1.0f * this.maxhitpoints * perc);
        this.descName = unit.getName();
        this.visrange = unit.getVisrange();
        this.descCon = unit.getDescCon();
        this.descDescription = unit.getDescDescription();
        this.descPro = unit.getDescPro();
        this.damage = unit.getDamage();
        this.bulletspeed = unit.getBulletspeed();
        this.bullettexture = unit.getBullettexture();
        this.range = unit.getRange();
        this.atkdelay = unit.getAtkdelay();
        this.abilitys.clear();
        for (Ability ab : unit.abilitys) {
            try {
                this.abilitys.add(ab.clone());
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }
        // Unit-Eigenschaften
        Unit here = (Unit) this;
        System.out.println("AddMe: Copy animator & Graphics Data!");
        here.speed = unit.getSpeed();
    }

    public void copyPropertiesFrom(Building building) {
        // Eigenschaften übernehmen
        this.damageFactors = building.getDamageFactors();
        this.descTypeId = building.getDescTypeId();
        this.armorType = building.getArmorType();
        // Gesundheit prozentual erhöhen
        float perc = 1.0f * this.hitpoints / this.maxhitpoints;
        this.maxhitpoints = building.maxhitpoints;
        this.hitpoints = (int) (1.0f * this.maxhitpoints * perc);
        this.descName = building.getName();
        this.visrange = building.getVisrange();
        this.descCon = building.getDescCon();
        this.descDescription = building.getDescDescription();
        this.descPro = building.getDescPro();
        this.damage = building.getDamage();
        this.bulletspeed = building.getBulletspeed();
        this.bullettexture = building.getBullettexture();
        this.range = building.getRange();
        this.atkdelay = building.getAtkdelay();
        this.abilitys.clear();
        for (Ability ab : building.abilitys) {
            try {
                this.abilitys.add(ab.clone());
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }
        // Building-Eigenschaften
        Building here = (Building) this;
        here.accepts = building.accepts;
        here.maxIntra = building.maxIntra;
        // Fertig
    }

    public void performDeltaUpgrade(ServerCore.InnerServer rgi, ServerAbilityUpgrade up) {
        // Allgemeine Upgrades durchführen:

        /*   if (up.newTex != null) {
        if (this.getClass().equals(Unit.class)) {
        ((Unit) this).graphicsdata.defaultTexture = up.newTex;
        } else if (this.getClass().equals(Building.class)) {
        ((Building) this).defaultTexture = up.newTex;
        }
        } */
        System.out.println("AddMe: Upgrade GraphicsData & Animator (2x!)");

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            System.out.println("AddMe: Upgrade Armortypes!");
            // this.armorType = up.newarmortype;
        }

        this.damageFactors[ARMORTYPE_HEAVYINF] += up.antiheavyinfup;
        this.damageFactors[ARMORTYPE_LIGHTINF] += up.antilightinfup;
        this.damageFactors[ARMORTYPE_KAV] += up.antikavup;
        this.damageFactors[ARMORTYPE_VEHICLE] += up.antivehicleup;
        this.damageFactors[ARMORTYPE_TANK] += up.antitankup;
        this.damageFactors[ARMORTYPE_AIR] += up.antiairup;
        this.damageFactors[ARMORTYPE_BUILDING] += up.antibuildingup;
        this.damage += up.damageup;

        /*  if (up.toAnimDesc != 0) {
        // Animator suchen und einsetzen
        if (this.getClass().equals(Unit.class)) {
        UnitAnimator rgua = rgi.game.getPlayer(playerId).descUnit.get(up.toAnimDesc).anim;
        if (rgua != null) {
        ((Unit) this).anim = rgua;
        }
        } else if (this.getClass().equals(Building.class)) {
        BuildingAnimator rgba = rgi.game.getPlayer(playerId).descBuilding.get(up.toAnimDesc).anim;
        if (rgba != null) {
        ((Building) this).anim = rgba;
        }
        }
        } */



        // Unit only:
        this.bulletspeed += up.bulletspeedup;
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            myself.speed += up.speedup;
            myself.range += up.rangeup;
        }
    }

    public void performDeltaUpgrade(ClientCore.InnerClient rgi, AbilityUpgrade up) {
        // Allgemeine Upgrades durchführen:
    /*    if (up.newTex != null) {
        if (this.getClass().equals(Unit.class)) {
        ((Unit) this).graphicsdata.defaultTexture = up.newTex;
        } else if (this.getClass().equals(Building.class)) {
        ((Building) this).defaultTexture = up.newTex;
        }
        } */
        System.out.println("AddMe: Upgrade GraphicsData & Animator (2x!)");

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            System.out.println("AddMe: Upgrade Armortypes!");
            //this.armortype = up.newarmortype;
        }

        this.damageFactors[ARMORTYPE_HEAVYINF] += up.antiheavyinfup;
        this.damageFactors[ARMORTYPE_LIGHTINF] += up.antilightinfup;
        this.damageFactors[ARMORTYPE_KAV] += up.antikavup;
        this.damageFactors[ARMORTYPE_VEHICLE] += up.antivehicleup;
        this.damageFactors[ARMORTYPE_TANK] += up.antitankup;
        this.damageFactors[ARMORTYPE_AIR] += up.antiairup;
        this.damageFactors[ARMORTYPE_BUILDING] += up.antibuildingup;
        this.visrange += up.visrangeup;
        this.damage += up.damageup;
        this.bulletspeed += up.bulletspeedup;
        if (up.newBulletTex != null) {
            this.bullettexture = up.newBulletTex;
        }
        this.range += up.rangeup;
        System.out.println("AddMe: Upgrade GraphicsData & Animator (2x!)");

        /* if (up.toAnimDesc != 0) {
        // Animator suchen und einsetzen
        if (this.getClass().equals(Unit.class)) {
        int direction = ((Unit) this).anim.dir;
        UnitAnimator rgua = rgi.game.getPlayer(this.playerId).descUnit.get(up.toAnimDesc).anim.clone();
        if (rgua != null) {
        rgua.dir = direction;
        ((Unit) this).anim = rgua;
        }
        } else if (this.getClass().equals(Building.class)) {
        BuildingAnimator rgba = rgi.game.getPlayer(this.playerId).descBuilding.get(up.toAnimDesc).anim;
        if (rgba != null) {
        ((Building) this).anim = rgba;
        }
        }
        } */

        // Unit only:
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            // Speed ist eine kritische Änderung, wenn die Einheit grad läuft:
            myself.speed += up.speedup;
        }
        // Gebäude only
        if (this.getClass().equals(Building.class)) {
            Building myself = (Building) this;
            /*if (up.changeOffsetX) {
            myself.offsetX = up.newOffsetX;
            }
            if (up.changeOffsetY) {
            myself.offsetY = up.newOffsetY;
            } */
            System.out.println("AddMe: Check for Offset-Changes");
            myself.maxIntra += up.maxIntraup;
        }
    }

    /**
     * Sucht eine Ability Anhand ihrere ability-Id.
     * Findet nur Abilitys, die auch eine haben, also zum beispiel nicht den HuntSelector!
     *
     *
     * @param desc Die Ablity, nach der wir suchen
     * @return RogGameObjectAbility, wenn gefunden, sonst null
     */
    public Ability getAbility(int desc) {
        for (Ability ab : abilitys) {
            if (ab.myId == desc) {
                return ab;
            }
        }
        return null;
    }

    /**
     * Gets all Abilitys as unmodifiable List.
     * @return all Abilitys as unmodifiable List.
     */
    public List<Ability> getAbilitys() {
        return Collections.unmodifiableList(abilitys);
    }

    /**
     * Overrides ALL abilitys with the given.
     * Uses the given List directly, please be aware of pass-by-ref issues
     * @param list the List with all the ablitys the object shall have.
     */
    public void setAbilitys(List<Ability> list) {
        this.abilitys = list;
    }

    public void performDeltaUpgrade(ServerCore.InnerServer rgi, DeltaUpgradeParameter up) {
        // Allgemeine Upgrades durchführen:

        /* if (up.newTex != null) {
        if (this.getClass().equals(Unit.class)) {
        ((Unit) this).graphicsdata.defaultTexture = up.newTex;
        } else if (this.getClass().equals(Building.class)) {
        ((Building) this).defaultTexture = up.newTex;
        }
        } */

        System.out.println("AddMe: Upgrade GraphicsData & Animator (2x!)");

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            System.out.println("AddMe: Upgrade Armortypes!");
            // this.armorType = up.newarmortype;
        }

        this.damageFactors[ARMORTYPE_HEAVYINF] += up.antiheavyinfup;
        this.damageFactors[ARMORTYPE_LIGHTINF] += up.antilightinfup;
        this.damageFactors[ARMORTYPE_KAV] += up.antikavup;
        this.damageFactors[ARMORTYPE_VEHICLE] += up.antivehicleup;
        this.damageFactors[ARMORTYPE_TANK] += up.antitankup;
        this.damageFactors[ARMORTYPE_AIR] += up.antiairup;
        this.damageFactors[ARMORTYPE_BUILDING] += up.antibuildingup;
        this.damage += up.damageup;
        this.visrange += up.visrangeup;
        this.damage += up.damageup;
        this.bulletspeed += up.bulletspeedup;
        if (up.newBulletTex != null) {
            this.bullettexture = up.newBulletTex;
        }
        this.range += up.rangeup;
        if (up.healup > 0) {
            if (this.healRate > 0) {
                this.healRate += up.healup;
            } else {
                this.healRate = up.healup;
                ServerBehaviourHeal healb = new ServerBehaviourHeal(rgi, this);
                this.sbehaviours.add(healb);
            }
        }
        /*  if (up.toAnimDesc != 0) {
        // Animator suchen und einsetzen
        if (this.getClass().equals(Unit.class)) {
        try {
        UnitAnimator rgua = rgi.game.getPlayer(playerId).descUnit.get(up.toAnimDesc).anim;
        if (rgua != null) {
        ((Unit) this).anim = rgua;
        }
        
        } catch (NullPointerException ex) {
        // Wenns keinen Animator gibt, dann isses auch egal
        }
        } else if (this.getClass().equals(Building.class)) {
        try {
        BuildingAnimator rgba = rgi.game.getPlayer(playerId).descBuilding.get(up.toAnimDesc).anim;
        if (rgba != null) {
        ((Building) this).anim = rgba;
        }
        } catch (NullPointerException ex) {
        // Wenns keinen Animator gibt, dann isses auch egal
        }
        }
        } */

        // Unit only:
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            myself.speed += up.speedup;
        }
        if (this.getClass().equals(Building.class)) {
            Building myself = (Building) this;
            myself.maxIntra += up.maxIntraup;

        }
    }

    public void performDeltaUpgrade(ClientCore.InnerClient rgi, DeltaUpgradeParameter up) {
        // Allgemeine Upgrades durchführen:

        /*  if (up.newTex != null) {
        if (this.getClass().equals(Unit.class)) {
        ((Unit) this).graphicsdata.defaultTexture = up.newTex;
        } else if (this.getClass().equals(Building.class)) {
        ((Building) this).defaultTexture = up.newTex;
        }
        } */

        System.out.println("AddMe: Upgrade GraphicsData & Animator (2x!)");

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            System.out.println("AddMe: Upgrade Armortypes!");
            //this.armorType = up.newarmortype;
        }

        this.damageFactors[ARMORTYPE_HEAVYINF] += up.antiheavyinfup;
        this.damageFactors[ARMORTYPE_LIGHTINF] += up.antilightinfup;
        this.damageFactors[ARMORTYPE_KAV] += up.antikavup;
        this.damageFactors[ARMORTYPE_VEHICLE] += up.antivehicleup;
        this.damageFactors[ARMORTYPE_TANK] += up.antitankup;
        this.damageFactors[ARMORTYPE_AIR] += up.antiairup;
        this.damageFactors[ARMORTYPE_BUILDING] += up.antibuildingup;
        this.visrange += up.visrangeup;
        this.damage += up.damageup;
        this.range += up.rangeup;
        this.bullettexture = up.newBulletTex;
        this.bulletspeed += up.bulletspeedup;
        /*   if (up.toAnimDesc != 0) {
        // Animator suchen und einsetzen
        if (this.getClass().equals(Unit.class)) {
        try {
        int direction = ((Unit) this).anim.dir;
        UnitAnimator rgua = rgi.game.getPlayer(playerId).descUnit.get(up.toAnimDesc).anim.clone();
        if (rgua != null) {
        rgua.dir = direction;
        ((Unit) this).anim = rgua;
        }
        } catch (NullPointerException ex) {
        }
        } else if (this.getClass().equals(Building.class)) {
        try {
        BuildingAnimator rgba = rgi.game.getPlayer(playerId).descBuilding.get(up.toAnimDesc).anim;
        if (rgba != null) {
        ((Building) this).anim = rgba;
        }
        } catch (NullPointerException ex) {
        }
        }
        } */
        // Unit only:
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            // Speed ist eine kritische Änderung, wenn die Einheit grad läuft:
            myself.speed += up.speedup;
        }

        // Gebäude only
        if (this.getClass().equals(Building.class)) {
            Building myself = (Building) this;
        }
    }

    /**
     * @return the damageFactors
     */
    public int[] getDamageFactors() {
        return damageFactors;
    }

    /**
     * @return the hitpoints
     */
    public int getHitpoints() {
        return hitpoints;
    }

    /**
     * @return the maxhitpoints
     */
    public int getMaxhitpoints() {
        return maxhitpoints;
    }

    /**
     * @return the fireDelay
     */
    public int getFireDelay() {
        return fireDelay;
    }

    /**
     * @return the damage
     */
    public int getDamage() {
        return damage;
    }

    /**
     * @return the range
     */
    public double getRange() {
        return range;
    }

    /**
     * @return the bulletspeed
     */
    public int getBulletspeed() {
        return bulletspeed;
    }

    /**
     * @return the bullettexture
     */
    public String getBullettexture() {
        return bullettexture;
    }

    /**
     * @return the atkdelay
     */
    public int getAtkdelay() {
        return atkdelay;
    }

    /**
     * @return the descDescription
     */
    public String getDescDescription() {
        return descDescription;
    }

    /**
     * @return the descPro
     */
    public String getDescPro() {
        return descPro;
    }

    /**
     * @return the descCon
     */
    public String getDescCon() {
        return descCon;
    }

    /**
     * @return the healRate
     */
    public int getHealRate() {
        return healRate;
    }

    /**
     * @return the armorType
     */
    public int getArmorType() {
        return armorType;
    }

    /**
     * @return the lifeStatus
     */
    public int getLifeStatus() {
        return lifeStatus;
    }

    public void healTo(int energy) {
        if (energy > 0 && energy <= maxhitpoints) {
            hitpoints = energy;
        }
    }

    /**
     * Erzeut eine allein Lauffähige Kopie.
     * Das zurückgegebene Objekt kann sofort auf die Klasse dieses Objekts gecastet werden.
     * @param newNetId Die neue NetID des kopierten Objekts
     * @return Das kopierte Objekt
     */
    public abstract GameObject getCopy(int newNetId);

    /**
     * Schadet diesem GO.
     * Das Objekt stibt normalerweise, wenn die Energie unter oder auf 0 fällt.
     */
    public void dealDamage(int damage) {
        hitpoints -= damage;
        if (hitpoints <= 0) {
            hitpoints = 0;
            kill();
        }
    }

    /**
     * Lässt das GO sterben.
     */
    public abstract void kill();

    /**
     * Sucht eine Fähigkeit anhand ihrer DescTypeId
     * @param searchDESC Die Desc-Id
     * @return RogGameObjectAbiliyRecruit, falls gefunden, sonst null
     * @deprecated
     */
    public AbilityRecruit getRecruitAbility(int searchDESC) {
        for (int i = 0; i < abilitys.size(); i++) {
            Ability a = abilitys.get(i);
            if (a.getClass().equals(AbilityRecruit.class)) {
                try {
                    AbilityRecruit ab = (AbilityRecruit) a;
                    if (ab.descTypeId == searchDESC) {
                        return ab;
                    }
                } catch (Exception ex) {
                }
            }
        }
        return null;
    }

    /**
     * Findet die Zuordnungsposition eines von diesem GO gespawnten Objekts heraus.
     * Hauptsächlich zum Rekrutieren.
     * 
     * @param obj das Objekt, die Gespawnt werden soll. (Zur Größenbestimmung)
     * @return Die Position, an der das Object gespawnt werden soll.
     */
    public abstract Position getSpawnPosition(GameObject obj);

    /**
     * @return the waypoint
     */
    public Position getWaypoint() {
        return waypoint;
    }

    /**
     * Sets the waypoint
     */
    public void setWaypoint(Position point) {
        waypoint = point;
    }

    /**
     * @return the isSelected
     */
    @Override
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * @param isSelected the isSelected to set
     */
    @Override
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

}
