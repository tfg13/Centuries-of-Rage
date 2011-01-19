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

import thirteenducks.cor.game.server.ServerCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import thirteenducks.cor.networks.client.behaviour.DeltaUpgradeParameter;

/**
 * Auch der Server braucht Upgrades.
 * Dies ist die Einzige Ability bzw. Upgrade-Klasse, die Server-tauglich ist.
 *
 * Arg zusammengestrichen, enthält nur, was absolut nötig ist (also fast nix ;)
 *
 * Normale Abilitys sind nach wie vor client-only.
 *
 * @author tfg
 */
public class ServerAbilityUpgrade implements Serializable, Cloneable {

    public transient ServerCore.InnerServer rgi;
    public int myId;           // Die eigene desc-id dieser Fähigkeit
    // Bezieht sich auf (nur eines setzen)
    public int descTypeIdU = 0;
    public int descTypeIdB = 0;
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
    public int maxIntraup;
    // Unit only
    public double speedup;            //Geschwindigkeit erhöhen
    public boolean harv;           //Ernten erlauben?
    public int damageup;           //Erhöhung des Schadens
    public int bulletspeedup;
    public int rangeup;            //Erhöhung der Reichweite
    public int toEpoche = 0;       // Auf Epoche upgraden?
    public boolean epocheUpgrade = false;

    // Epochenupgrade - Parameter
    public HashMap<String, DeltaUpgradeParameter> edelta;

    public enum upgradeaffects { // Für gilt das Upgrade?...

        self, // Nur für die Einheit, die es ausführt
        fresh, // Für alle Einheiten die Zukünftig gebaut werden
        old, // Für alle Einheiten, die schon da sind
        all    // Für alles
    };
    public upgradeaffects affects;

    public ServerAbilityUpgrade(int mydescid) {
        this.myId = mydescid;
        this.edelta = new HashMap<String, DeltaUpgradeParameter>();
    }

    public void perform(GameObject caster) {
        if (!epocheUpgrade) {
            // Jetzt performen:
            if (affects == upgradeaffects.self) {
                caster.performDeltaUpgrade(rgi, this);
            } else if (affects == upgradeaffects.old) {
                if (this.descTypeIdU != 0) {
                    for (Unit unit : rgi.netmap.unitList) {
                        if (unit.descTypeId == this.descTypeIdU) {
                            unit.performDeltaUpgrade(rgi, this);
                        }
                    }
                } else {
                    for (Building building : rgi.netmap.buildingList) {
                        if (building.descTypeId == this.descTypeIdB) {
                            building.performDeltaUpgrade(rgi, this);
                        }
                    }
                }
            } else if (affects == upgradeaffects.fresh) {
                if (this.descTypeIdU != 0) {
                    rgi.game.getPlayer(caster.playerId).descUnit.get(descTypeIdU).performDeltaUpgrade(rgi, this);
                } else {
                    rgi.game.getPlayer(caster.playerId).descBuilding.get(descTypeIdB).performDeltaUpgrade(rgi, this);
                }
            } else if (affects == upgradeaffects.all) {
                if (this.descTypeIdU != 0) {
                    for (Unit unit : rgi.netmap.unitList) {
                        if (unit.descTypeId == this.descTypeIdU) {
                            unit.performDeltaUpgrade(rgi, this);
                        }
                    }
                } else {
                    for (Building building : rgi.netmap.buildingList) {
                        if (building.descTypeId == this.descTypeIdB) {
                            building.performDeltaUpgrade(rgi, this);
                        }
                    }
                }
                if (this.descTypeIdU != 0) {
                    rgi.game.getPlayer(caster.playerId).descUnit.get(descTypeIdU).performDeltaUpgrade(rgi, this);
                } else {
                    rgi.game.getPlayer(caster.playerId).descBuilding.get(descTypeIdB).performDeltaUpgrade(rgi, this);
                }
            }
        } else {
            // Epochen-Upgrade
            // Alle delta-Upgrades ausführen - (für lebende und zukünftige)
            ArrayList<DeltaUpgradeParameter> deltas = new ArrayList<DeltaUpgradeParameter>(edelta.values());
            for (DeltaUpgradeParameter para : deltas) {
                if (para.modunit) {
                    // Vorhandene Units patchen
                    for (Unit unit : rgi.netmap.unitList) {
                        if (unit.descTypeId == para.moddesc && unit.playerId == caster.playerId) {
                            unit.performDeltaUpgrade(rgi, para);
                        }
                    }
                    rgi.game.getPlayer(caster.playerId).descUnit.get(para.moddesc).performDeltaUpgrade(rgi, para);
                } else {
                    for (Building building : rgi.netmap.buildingList) {
                        if (building.descTypeId == para.moddesc && building.playerId == caster.playerId) {
                            building.performDeltaUpgrade(rgi, para);
                        }
                    }
                    rgi.game.getPlayer(caster.playerId).descBuilding.get(para.moddesc).performDeltaUpgrade(rgi, para);
                }
            }

        }
        System.out.println("AddMe: Implement Upgrades for Server!");
    }

    @Override
    public ServerAbilityUpgrade clone() throws CloneNotSupportedException {
        ServerAbilityUpgrade newab = (ServerAbilityUpgrade) super.clone();
        return newab;
    }
}
