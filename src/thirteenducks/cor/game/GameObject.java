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

import thirteenducks.cor.game.Unit;
import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Collections;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourUpgrade;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
import thirteenducks.cor.graphics.GraphicsRenderable;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.AbilityBuild;
import thirteenducks.cor.game.ability.AbilityIntraManager;
import thirteenducks.cor.game.ability.AbilityRecruit;
import thirteenducks.cor.game.ability.AbilityUpgrade;
import thirteenducks.cor.graphics.BuildingAnimator;
import thirteenducks.cor.graphics.UnitAnimator;
import thirteenducks.cor.game.ability.ServerAbilityUpgrade;
import thirteenducks.cor.game.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.behaviour.impl.ServerBehaviourHeal;
import thirteenducks.cor.game.server.ServerCore;

/**
 *
 * @author tfg
 */
public class GameObject implements Serializable, Comparable<GraphicsRenderable>, Cloneable, GraphicsRenderable {
    // Superklasse für RogUnit und RogBuilding

    // Gibts bei beiden:
    public String name;                         // Name, sollte gesetzt sein
    public boolean isSelected = false;
    public int playerId = 0;                             // 0 = Neutral; 1 - 8 = Spieler/KI;
    public Position position;
    public String armortype;				//Rüstungsklasse der Einheit
    public int antiheavyinf;                           //Extraschaden gegen schwere Infanterie
    public int antilightinf;                           //Extraschaden gegen leichte Infanterie
    public int antikav;                                //Extraschaden gegen Kavallerie
    public int antivehicle;							//Extraschaden gegen Fahrzeuge
    public int antitank;								//Extraschaden gegen Panzer
    public int antiair;								//Extraschaden gegen Flugzeuge
    public int antibuilding;                           //Extraschaden gegen Gebäude
    public int hitpoints;                              // Lebensenergie - bei 0 stirbt die Einheit
    public int maxhitpoints;                           // Maximale Lebensenergie
    public int descTypeId;                             // Definiert den Gebäudetyp, wird verwendet für Bauen, Mehrfachselektion etc. Wird eingelesen aus data/descTypes
    public List<Ability> abilitys;        // Die Fähigkeiten des Objekts, die im Hud anklickbar sind. Nur für den Client von Bedeutung
    public List<ServerBehaviour> sbehaviours;// Die Server-Behaviour des Objekts
    public List<ClientBehaviour> cbehaviours;// Die Server-Behaviour des Objekts
    public Position waypoint;                       // Standard-Ziel für neue Einheiten
    public Building wayBuilding;                    // Ernte-Gebäude Ziel
    public Ressource wayRessource;                  // Ressource - Ziel
    public boolean ready = true;                       // Ist die Einheit ok / das Gebäude fertig gebaut?
    public boolean alive = true;                       // Lebts noch?
    public int visrange = 4;                           // Sichtweite
    // Netzwerk-Sachen
    public final int netID;                     // Eine im Netzwerk einmalige ID gültig für fast alles
    // Truppenlimit
    public int limit = 0;

    public int cooldown;                               // Aktueller cooldown, wenn 0 kann angegriffen werden
    public int cooldownmax;                            // Bestimmt, wie schnell die Einheit angreift: kleine zahl -> schneller angriff
    public int damage;                                 // soviel damage macht die einheit
    public GameObject attacktarget;                 // Die Einheit die angegriffen wird
    public double range;                               // Reichweite des Angriffs. Wenn 2, dann ist es eine Nahkampf-Einheit... naja^^
    public int bulletspeed = 15;                       // Geschossgeschwindigkeit in Felder / Sekunde (nur Fernkampf, also range > 2)
    public String bullettexture;                       // Geschosstextur (nur Fernkampf, also range > 2)
    public int atkdelay = 0;                         // Delay in Millisekunden zwischen dem Beginn eines Angriffs ("ausholen") und dem "zuschlagen". In dieser Zeit wird die Angriffsanimation abgespielt und die Einheit von der Grafikengine etwas vor bewegt. (nur Nahkampf)
    public long atkStart = 0;                          // Zeitpunkt, bei dem mit draufhauen begonnen wird (automatisch verwaltet vom Kampfsystem)
    public boolean atkAnim = false;                    // Läuft gerade eine Angriffsanimation? (automatisch verwaltet vom Kampfsystem)

    @Override
    public int compareTo(GraphicsRenderable o) {
        if (o.getY() > this.getY()) {
            return -1;
        } else if (o.getY() < this.getY()) {
            return 1;
        } else {
            return 0;
        }
    }

    public GameObject(int newNetId) {
        abilitys = Collections.synchronizedList(new ArrayList<Ability>());
        sbehaviours = Collections.synchronizedList(new ArrayList<ServerBehaviour>());
        cbehaviours = Collections.synchronizedList(new ArrayList<ClientBehaviour>());
        netID = newNetId;
	antiheavyinf = 100;
        antilightinf = 100;
        antikav = 100;
        antivehicle = 100;
        antitank = 100;
        antiair = 0;
        antibuilding = 100;
    }

    public GameObject(boolean forRessource, int newNetId) {
        // Ressourcen die Listen nicht - spart Speicher
        if (!forRessource) {
            abilitys = Collections.synchronizedList(new ArrayList<Ability>());
            sbehaviours = Collections.synchronizedList(new ArrayList<ServerBehaviour>());
            cbehaviours = Collections.synchronizedList(new ArrayList<ClientBehaviour>());
        } else {
            descTypeId = -1; //Keine Id
        }
        netID = newNetId;
    }

    /* @Override
    public RogGameObject clone() throws CloneNotSupportedException {
    RogGameObject nob = (RogGameObject) super.clone();
    nob.abilitys = Collections.synchronizedList(new ArrayList<RogGameObjectAbility>());
    for (RogGameObjectAbility ab : this.abilitys) {
    nob.abilitys.add(ab.clone());
    }
    nob.behaviours = Collections.synchronizedList(new ArrayList<RogUnitBehaviour>());
    /*for (RogUnitBehaviour rub : this.behaviours) {
    nob.behaviours.add(rub.clone());
    } * /
    if (position != null) {
    nob.position = (RogPosition) this.position.clone();
    }
    return nob;
    }*/
    public GameObject clone(int newNetID) throws CloneNotSupportedException {
        GameObject nob = new GameObject(newNetID);
        nob.name = this.name;
        nob.playerId = this.playerId;
        nob.hitpoints = this.hitpoints;
        nob.armortype = this.armortype;
        nob.antiheavyinf = this.antiheavyinf;
        nob.antilightinf = this.antilightinf;
        nob.antikav = this.antikav;
        nob.antivehicle = this.antivehicle;
        nob.antitank = this.antitank;
        nob.antiair = this.antiair;
        nob.antibuilding = this.antibuilding;
        nob.maxhitpoints = this.maxhitpoints;
        nob.descTypeId = this.descTypeId;
        nob.visrange = this.visrange;
        nob.limit = this.limit;
        for (Ability ab : this.abilitys) {
            nob.abilitys.add(ab.clone());
        }
        if (position != null) {
            nob.position = (Position) this.position.clone();
        }
        return nob;
    }

    /**
     * Gibt ein bestimmtes ServerBehaviour zurück.
     * @param name  Die id des gesuchten ServerBehaviours.
     * @return  Ein ServerBehaviour, wenn es gefunden wurde, sonst null.
     */
    public ServerBehaviour getbehaviourS(int searchid) {
        for (ServerBehaviour b : this.sbehaviours) {
            if (b.getId() == searchid) {
                return b;
            }
        }
        return null;
    }

    /**
     * Gibt ein bestimmtes ServerBehaviour zurück.
     * @param name  Die id des gesuchten ServerBehaviours.
     * @return  Ein ServerBehaviour, wenn es gefunden wurde, sonst null.
     */
    public ClientBehaviour getbehaviourC(int searchid) {
        for (ClientBehaviour b : this.cbehaviours) {
            if (b.getId() == searchid) {
                return b;
            }
        }
        return null;
    }

    /**
     * Sucht eine Fähigkeit anhand ihrer DescTypeId
     * @param searchDESC Die Desc-Id
     * @return RogGameObjectAbiliyRecruit, falls gefunden, sonst null
     */
    public AbilityBuild getBuildAbility(int searchDESC) {
        for (Ability a : abilitys) {
            try {
                AbilityBuild ab = (AbilityBuild) a;
                if (ab.descTypeId == searchDESC) {
                    return ab;
                }
            } catch (Exception ex) {
            }
        }
        return null;
    }

    /**
     * Sucht eine Fähigkeit anhand ihrer DescTypeId
     * @param searchDESC Die Desc-Id
     * @return RogGameObjectAbiliyRecruit, falls gefunden, sonst null
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
            BuildingAnimator rgba = rgi.game.getPlayer(this.playerId).descBuilding.get(toDesc).anim;
            if (rgba != null) {
                rgba = rgba.clone();
            }
            ((Building) this).anim = rgba;
            // Fertig
        } else if (this.getClass().equals(Unit.class)) {
            Unit unit = rgi.mapModule.getDescUnit(toDesc, -1, this.playerId);
            if (unit == null) {
                // Abbrechen, das wird nichts.
                return;
            }
            // Eigenschaften übernehmen
            copyPropertiesFrom(unit);

            UnitAnimator rgua = rgi.game.getPlayer(this.playerId).descUnit.get(toDesc).anim;
            if (rgua != null) {
                rgua = rgua.clone();
            }
            ((Unit) this).anim = rgua;
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
        this.antiair = unit.antiair;
        this.antibuilding = unit.antibuilding;
        this.antiheavyinf = unit.antiheavyinf;
        this.antikav = unit.antikav;
        this.antilightinf = unit.antilightinf;
        this.antitank = unit.antitank;
        this.antivehicle = unit.antivehicle;
        this.descTypeId = unit.descTypeId;
        this.armortype = unit.armortype;
        this.limit = unit.limit;
        // Gesundheit prozentual erhöhen
        float perc = 1.0f * this.hitpoints / this.maxhitpoints;
        this.maxhitpoints = unit.maxhitpoints;
        this.hitpoints = (int) (1.0f * this.maxhitpoints * perc);
        this.name = unit.name;
        this.visrange = unit.visrange;
        // Unit-Eigenschaften
        Unit here = (Unit) this;
        here.Gcon = unit.Gcon;
        here.Gdesc = unit.Gdesc;
        here.Gimg = unit.Gimg;
        here.Gpro = unit.Gpro;
        if (unit.anim != null) {
            here.anim = unit.anim.clone();
        }
        here.canHarvest = unit.canHarvest;
        here.damage = unit.damage;
        here.bulletspeed = unit.bulletspeed;
        here.bullettexture = unit.bullettexture;
        if (unit.graphicsdata != null) {
            here.graphicsdata = unit.graphicsdata.clone();
        }
        here.idlehuntradius = unit.idlehuntradius;
        here.idlerange = unit.idlerange;
        here.range = unit.range;
        here.atkdelay = unit.atkdelay;
        here.selectionShadow = unit.selectionShadow;
        here.speed = unit.speed;
        here.abilitys.clear();
        for (Ability ab : unit.abilitys) {
            try {
                here.abilitys.add(ab.clone());
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void copyPropertiesFrom(Building building) {
        // Eigenschaften übernehmen
        this.antiair = building.antiair;
        this.antibuilding = building.antibuilding;
        this.antiheavyinf = building.antiheavyinf;
        this.antikav = building.antikav;
        this.antilightinf = building.antilightinf;
        this.antitank = building.antitank;
        this.antivehicle = building.antivehicle;
        this.descTypeId = building.descTypeId;
        this.armortype = building.armortype;
        this.limit = building.limit;
        // Gesundheit prozentual erhöhen
        float perc = 1.0f * this.hitpoints / this.maxhitpoints;
        this.maxhitpoints = building.maxhitpoints;
        this.hitpoints = (int) (1.0f * this.maxhitpoints * perc);
        this.name = building.name;
        this.visrange = building.visrange;
        // Building-Eigenschaften
        Building here = (Building) this;
        here.Gdesc = building.Gdesc;
        here.Gimg = building.Gimg;
        if (building.anim != null) {
            here.anim = building.anim.clone();
        }
        here.defaultTexture = building.defaultTexture;
        here.hudTexture = building.hudTexture;
        here.offsetX = building.offsetX;
        here.offsetY = building.offsetY;
        here.accepts = building.accepts;
        here.maxIntra = building.maxIntra;
        here.harvRate = building.harvRate;
        here.harvests = building.harvests;
        here.visrange = building.visrange;
	here.heal = building.heal;
        here.abilitys.clear();
        for (Ability ab : building.abilitys) {
            try {
                here.abilitys.add(ab.clone());
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }
        // Fertig
    }

    public void performDeltaUpgrade(ServerCore.InnerServer rgi, ServerAbilityUpgrade up) {
        // Allgemeine Upgrades durchführen:

        if (up.newTex != null) {
            if (this.getClass().equals(Unit.class)) {
                ((Unit) this).graphicsdata.defaultTexture = up.newTex;
            } else if (this.getClass().equals(Building.class)) {
                ((Building) this).defaultTexture = up.newTex;
            }
        }

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            this.armortype = up.newarmortype;
        }

        this.antiheavyinf += up.antiheavyinfup;
        this.antilightinf += up.antilightinfup;
        this.antikav += up.antikavup;
        this.antivehicle += up.antivehicleup;
        this.antitank += up.antitankup;
        this.antiair += up.antiairup;
        this.antibuilding += up.antibuildingup;
	this.damage += up.damageup;
	
        if (up.toAnimDesc != 0) {
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
        }

        // Unit only:
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            myself.speed += up.speedup;
            myself.bulletspeed += up.bulletspeedup;
            myself.range += up.rangeup;
            myself.canHarvest = up.harv;
        }
    }

    public void performDeltaUpgrade(ClientCore.InnerClient rgi, AbilityUpgrade up) {
        // Allgemeine Upgrades durchführen:
        if (up.newTex != null) {
            if (this.getClass().equals(Unit.class)) {
                ((Unit) this).graphicsdata.defaultTexture = up.newTex;
            } else if (this.getClass().equals(Building.class)) {
                ((Building) this).defaultTexture = up.newTex;
            }
        }

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            this.armortype = up.newarmortype;
        }

        this.antiheavyinf += up.antiheavyinfup;
        this.antilightinf += up.antilightinfup;
        this.antikav += up.antikavup;
        this.antivehicle += up.antivehicleup;
        this.antitank += up.antitankup;
        this.antiair += up.antiairup;
        this.antibuilding += up.antibuildingup;
        this.visrange += up.visrangeup;
	this.damage += up.damageup;

        if (up.toAnimDesc != 0) {
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
        }

        // Unit only:
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            // Speed ist eine kritische Änderung, wenn die Einheit grad läuft:
            if (myself.isMoving()) {
                myself.changespeedto = (int) (myself.speed + up.speedup);
            } else {
                myself.speed += up.speedup;
            }
            myself.bulletspeed += up.bulletspeedup;
            if (up.newBulletTex != null) {
                myself.bullettexture = up.newBulletTex;
            }
            myself.range += up.rangeup;
            myself.canHarvest = up.harv;
        }
        // Gebäude only
        if (this.getClass().equals(Building.class)) {
            Building myself = (Building) this;
            if (up.changeOffsetX) {
                myself.offsetX = up.newOffsetX;
            }
            if (up.changeOffsetY) {
                myself.offsetY = up.newOffsetY;
            }
            myself.harvRate += up.harvRateup;
	    myself.maxIntra += up.maxIntraup;
	    myself.limit += up.limitupone;
	    if (myself.playerId == rgi.game.getOwnPlayer().playerId) {
		rgi.game.getOwnPlayer().maxlimit -= up.limitupone;
	    }
        }
    }

    /**
     * Sucht eine Ability Anhand ihrere Eigenen DescTypeId.
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

    public void performDeltaUpgrade(ServerCore.InnerServer rgi, DeltaUpgradeParameter up) {
        // Allgemeine Upgrades durchführen:

        if (up.newTex != null) {
            if (this.getClass().equals(Unit.class)) {
                ((Unit) this).graphicsdata.defaultTexture = up.newTex;
            } else if (this.getClass().equals(Building.class)) {
                ((Building) this).defaultTexture = up.newTex;
            }
        }

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            this.armortype = up.newarmortype;
        }

        this.antiheavyinf += up.antiheavyinfup;
        this.antilightinf += up.antilightinfup;
        this.antikav += up.antikavup;
        this.antivehicle += up.antivehicleup;
        this.antitank += up.antitankup;
        this.antiair += up.antiairup;
        this.antibuilding += up.antibuildingup;
	this.damage += up.damageup;

        if (up.toAnimDesc != 0) {
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
        }

        // Unit only:
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            myself.speed += up.speedup;
            myself.bulletspeed += up.bulletspeedup;
            myself.range += up.rangeup;
            myself.canHarvest = up.harv;
        }
	if (this.getClass().equals(Building.class)) {
            Building myself = (Building) this;
	    myself.maxIntra += up.maxIntraup;
	    myself.limit += up.limitupall;
	    if (up.healup > 0) {
		if (myself.heal > 0) {
		    myself.heal += up.healup;
		} else {
		    myself.heal = up.healup;
		    ServerBehaviourHeal healb = new ServerBehaviourHeal(rgi, myself);
		    myself.sbehaviours.add(healb);
		}
	    }
        }
    }

    public void performDeltaUpgrade(ClientCore.InnerClient rgi, DeltaUpgradeParameter up) {
        // Allgemeine Upgrades durchführen:

        if (up.newTex != null) {
            if (this.getClass().equals(Unit.class)) {
                ((Unit) this).graphicsdata.defaultTexture = up.newTex;
            } else if (this.getClass().equals(Building.class)) {
                ((Building) this).defaultTexture = up.newTex;
            }
        }

        this.maxhitpoints += up.maxhitpointsup;
        if (up.maxhitpointsup > 0) {
            this.hitpoints += up.maxhitpointsup;
        }
        this.hitpoints += up.hitpointsup;
        if (up.newarmortype != null) {
            this.armortype = up.newarmortype;
        }

        this.antiheavyinf += up.antiheavyinfup;
        this.antilightinf += up.antilightinfup;
        this.antikav += up.antikavup;
        this.antivehicle += up.antivehicleup;
        this.antitank += up.antitankup;
        this.antiair += up.antiairup;
        this.antibuilding += up.antibuildingup;
        this.visrange += up.visrangeup;
	this.damage += up.damageup;


        if (up.toAnimDesc != 0) {
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
        }

        // Unit only:
        if (this.getClass().equals(Unit.class)) {
            Unit myself = (Unit) this;
            // Speed ist eine kritische Änderung, wenn die Einheit grad läuft:
            if (myself.isMoving()) {
                myself.changespeedto = (int) (myself.speed + up.speedup);
            } else {
                myself.speed += up.speedup;
            }
            myself.bulletspeed += up.bulletspeedup;
            if (up.newBulletTex != null) {
                myself.bullettexture = up.newBulletTex;
            }
            myself.range += up.rangeup;
            myself.canHarvest = up.harv;
        }

        // Gebäude only
        if (this.getClass().equals(Building.class)) {
            Building myself = (Building) this;
            if (up.changeOffsetX) {
                myself.offsetX = up.newOffsetX;
            }
            if (up.changeOffsetY) {
                myself.offsetY = up.newOffsetY;
            }
            myself.harvRate += up.harvRateup;
	    myself.maxIntra += up.maxIntraup;
	    myself.limit += up.limitupall;
	    if (this.netID != -1 && this.ready && myself.playerId == rgi.game.getOwnPlayer().playerId) {
		rgi.game.getOwnPlayer().maxlimit -= up.limitupall;
	    }
        }
    }

    @Override
    public int getX() {
        return this.position.X;
    }

    @Override
    public int getY() {
        return this.position.Y;
    }

    /**
     * Muss aufgerufen werden, wenn Einheiten zerstört werden.
     * Behandelt einige Spezialfälle, wie z.B. zerstörte Gebäude, die etwas rekrutieren etc.
     * Derzeit Client only.
     */
    public void destroy() {
    }
}
