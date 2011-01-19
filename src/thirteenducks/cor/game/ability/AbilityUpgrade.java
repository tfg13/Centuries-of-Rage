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
package thirteenducks.cor.game.ability;

import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourUpgrade;
import thirteenducks.cor.game.GameObject;
import org.newdawn.slick.Color;
import java.util.HashMap;
import thirteenducks.cor.networks.client.behaviour.DeltaUpgradeParameter;


/**
 *
 * @author tfg
 */
public class AbilityUpgrade extends Ability {

    
    // Bezieht sich auf (nur eines setzen)
    public int descTypeIdU = 0;
    public int descTypeIdB = 0;
    public int duration;       // Dauer des Erforschens
    public String gdesc;       // Beschreibung der Einheit
    public int transTo = 0;    // Das Upgrade bewirkt eine desc-Änderung, wohin? (bei Epoche: Zielepoche)


    public boolean allowMultipleUses = false; //Mehrfache Verwendung auf der gleichen Einheit erlauben?
    
    // Parameter des Deltaupgrades
    // Allgemein
    public String newTex;
    public int maxhitpointsup;     // Max-Hitpoint-Upgrade erhöht automatisch auch die Hitpoints
    public int hitpointsup;        // Nicht über Maximum erhöhbar.
    public String newarmortype;    //Geänderte Rüstungsklasse der Einheit
    public int antiheavyinfup;     //Extraschaden gegen schwere Infanterie
    public int antilightinfup;     //Extraschaden gegen leichte Infanterie
    public int antikavup;          //Extraschaden gegen Kavallerie
    public int antivehicleup;      //Extraschaden gegen Fahrzeuge
    public int antitankup;         //Extraschaden gegen Panzer
    public int antiairup;          //Extraschaden gegen Flugzeuge
    public int antibuildingup;     //Extraschaden gegen Gebäude
    public int toAnimDesc;         //Animator einer anderen Einheit übernehmen
    public int visrangeup;         //Änderung der Sichtweite

    // Unit only
    public double speedup;            //Geschwindigkeit erhöhen
    public boolean harv;           //Ernten erlauben?
    public int damageup;           //Erhöhung des Schadens
    public int rangeup;            //Erhöhung der Reichweite
    public int bulletspeedup;      //Erhöhung der Geschossgeschwindigkeit
    public String newBulletTex;

    // Gebäude only
    public boolean changeOffsetX;
    public boolean changeOffsetY;
    public int newOffsetX;
    public int newOffsetY;
    public double harvRateup; // Änderung der Erntegeschwindigkeit von Gebäuden
    public int maxIntraup;
    public int limitupone; //Erhöht Truppenlimit von einem Gebäude

    // Epochenupgrade - Parameter
    public HashMap<String, DeltaUpgradeParameter> edelta;
    public int toEpoche = 0;


    public enum upgradetype {

        epoche, // Komplettes Epochenupgrade
        upgrade, // toDesc-Upgrade, moved Einheiten auf ein anderes desc
        deltaupgrade // einzelne Parameter erhöhen/absenken
    };

    public enum upgradeaffects { // Für gilt das Upgrade?...

        self, // Nur für die Einheit, die es ausführt
        fresh, // Für alle Einheiten die Zukünftig gebaut werden
        old, // Für alle Einheiten, die schon da sind
        all    // Für alles
    };
    public upgradetype kind;
    public upgradeaffects affects;

    public AbilityUpgrade(int descId) {
        super(descId);

        this.type = Ability.ABILITY_UPGRADE;
        this.cooldown = 0.0;

        this.frameColor = Color.yellow;

        this.edelta = new HashMap<String, DeltaUpgradeParameter>();
    }

    @Override
    public void perform(GameObject caster) {
        // Nur ausführen, wenns nicht schon läuft:
        System.out.println("AddMe: Check against double usage!");
       /* ClientBehaviourUpgrade old = (ClientBehaviourUpgrade) caster.getUpgradeBehaviour(this.myId);
        if (old == null) { */
            rgi.game.getOwnPlayer().res1 -= costs[0];
            rgi.game.getOwnPlayer().res2 -= costs[1];
            rgi.game.getOwnPlayer().res3 -= costs[2];
            rgi.game.getOwnPlayer().res4 -= costs[3];
            rgi.game.getOwnPlayer().res5 -= costs[4];
            ClientBehaviourUpgrade up = new ClientBehaviourUpgrade(rgi, caster, this);
            caster.addClientBehaviour(up);
            this.behaviour = up;
            rgi.rogGraphics.triggerUpdateHud();
       // }
    }

    @Override
    public void antiperform(GameObject caster) {
        // Abbrechen, falls es läuft
        System.out.println("AddMe: Kill running task");
    /*    ClientBehaviourUpgrade old = (ClientBehaviourUpgrade) caster.getUpgradeBehaviour(this.myId);
        if (old != null) {
            rgi.game.getOwnPlayer().res1 += costs[0];
            rgi.game.getOwnPlayer().res2 += costs[1];
            rgi.game.getOwnPlayer().res3 += costs[2];
            rgi.game.getOwnPlayer().res4 += costs[3];
            rgi.game.getOwnPlayer().res5 += costs[4];
            caster.cbehaviours.remove(old);
            this.behaviour = null;
        } */
    }

    @Override
    public AbilityUpgrade clone() throws CloneNotSupportedException {
        AbilityUpgrade u = (AbilityUpgrade) super.clone();
        if (this.edelta != null) {
            u.edelta = (HashMap<String, DeltaUpgradeParameter>) this.edelta.clone();
        }
        u.affects = this.affects;
        u.kind = this.kind;
        return u;
    }

    @Override
    public boolean isAvailable() {
        return (super.isAvailable() && !alreadyUsed);
    }

    @Override
    public boolean isVisible() {
       return !this.invisibleLocked;
    }
}
