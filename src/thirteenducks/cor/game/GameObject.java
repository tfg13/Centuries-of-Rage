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
public class GameObject implements Serializable {
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

    public GameObject(int newNetId) {
        netID = newNetId;
    }
}
