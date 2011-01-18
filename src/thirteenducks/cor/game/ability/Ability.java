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

import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import org.newdawn.slick.Color;
import java.io.Serializable;
import java.util.ArrayList;
import thirteenducks.cor.networks.client.behaviour.ShowsProgress;

/**
 * Dies ist die Superklasse für RogGameObject-Fähigkeiten.
 * Alle diese Fähigkeiten werden im Game im Hud-Bereich angezeigt und können dort angeklickt werden.
 *
 * @author tfg
 */
public abstract class Ability implements Serializable, Cloneable {

    // Die Fähigkeiten, die es gibt:
    public static final int ABILITY_BUILD = 0;      // Gebäudebau
    public static final int ABILITY_RECRUIT = 1;    // Einheitenrekrutierung
    public static final int ABILITY_MOVE = 2;       // Lauffähigkeiten, wie z.B. "Position halten" etc...
    public static final int ABILITY_UPGRADE = 3;    // Upgrades für alles, auch Epochenaufstiege
    public static final int ABILITY_INTRA = 4;    // Upgrades fürs Management von internen Einheiten
    // Achtung, Cooldowns sind noch *NICHT* implementiert (!)
    protected double cooldown;    // Die Zeit, die gewartet werden muss, bis die Fähigkeit erneut eingesetzt werden kann.
    protected long lastUse;       // Der Zeitpunkt der letzten Benutzung, wird nur gesetzt, wenns einen Cooldown hat.
    protected GameObject invoker; // Wer führt diese Fähigkeit aus
    public ShowsProgress behaviour; // Das zugehörige Behaviour, damit der Fortschritt gerendert werden kann
    public String[] symbols;         // Die Bildchen, die im Hud angezeigt werden, für jede Epoche eines.
    public String name;              // Der Angezeigte Name
    public int epoche = 0;           // Nur für eine spezielle Epoche? (0 = für alle Epochen)
    public int type;          // Welcher Typ (siehe oben) ist es?
    protected ArrayList<Integer> dependsB; // Vorraussetzungen Gebäude
    protected ArrayList<Integer> dependsU; // Vorraussetzungen Units
    protected ArrayList<Integer> dependsA; // Vorraussetzungen Upgrades (RogGameObjectAbilityUpgrade)
    public transient ClientCore.InnerClient rgi;
    public boolean invisibleLocked = false;   // Wenn true wird die Fähigkeit gar nicht angezeigt.
    public ArrayList<Integer> unlock;
    public ArrayList<Integer> lock;
    public int myId;           // Die eigene desc-id dieser Fähigkeit

    public boolean isAvailable() {      // Kann die Fähigkeit derzeit verwendet (angeklickt) werden - sonst wird sie "ausgegraut"
        boolean result = (rgi.game.getOwnPlayer().checkAvailability(dependsB, dependsU, dependsA) && rgi.game.getOwnPlayer().res1 >= costs[0] && rgi.game.getOwnPlayer().res2 >= costs[1] && rgi.game.getOwnPlayer().res3 >= costs[2] && rgi.game.getOwnPlayer().res4 >= costs[3] && rgi.game.getOwnPlayer().res5 >= costs[4]);
       /* if (this.type == Ability.ABILITY_BUILD) {
            AbilityBuild abb = (AbilityBuild) this;
            Building building = rgi.mapModule.getDescBuilding(abb.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
            if (building.limit > 0) {
                if (rgi.game.getOwnPlayer().freeLimit() < building.limit || rgi.game.getOwnPlayer().currentlimit + building.limit > 100) {
                    // Nix
                    return false;
                }
            }
        } else if (this.type == Ability.ABILITY_RECRUIT) {
            AbilityRecruit abr = (AbilityRecruit) this;
            Unit unit = rgi.mapModule.getDescUnit(abr.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
            if (unit.limit > 0) {
                if (rgi.game.getOwnPlayer().freeLimit() < unit.limit || rgi.game.getOwnPlayer().currentlimit + unit.limit > 100) {
                    return false;
                }
            }
        } */
        return result;
    }

    /**
     * Hiermit kann detailiert gesteuert werden, ob die Fähigkeit überhaupt angezeigt werden soll.
     * UNABHÄNGIG von der Epochen-Angabe!
     *
     * @return per default true, false, wenns nicht angezeigt werden soll.
     */
    public boolean isVisible() {
        return !this.invisibleLocked;
    }

    public abstract void perform(GameObject caster);             // Die Fähigkeit ausüben

    public abstract void antiperform(GameObject caster);         // Rechtsklick auf die Fähigkeit, Abbrechen oder Ähnliches
    public boolean useForAll = false;              // Soll diese Fähigkeit bei mehrere Selektierten auch bei allen ausgeführt werden, oder nur bei einer?
    public boolean removeHoverAfterUse = true;     // Soll das Hover-Fenster kurz verschwinden, nachdem man die Fähigkeit angeklickt hat?
    public boolean showCosts = true;               // Sollen die Kosten angezeigt werden? (false nur bei kostenlosen Sachen sinnvoll)
    public int[] costs;
    public Color frameColor = Color.black;         // Die Farbe des Rahmens
    public boolean alreadyUsed = false;            // Gui-Only, zeigt an, dass die Ability nicht verfügbar ist, weil sie schon benutzt wurde.

    public Ability(int mydescid) {
        dependsB = new ArrayList<Integer>();
        dependsU = new ArrayList<Integer>();
        dependsA = new ArrayList<Integer>();
        unlock = new ArrayList<Integer>();
        lock = new ArrayList<Integer>();

        costs = new int[5];

        this.myId = mydescid;
    }

    /**
     * Liefert die Gebäude Abhängigkeiten als DESC-B zurück
     * @return Integer[] mit DESC-B
     */
    public Integer[] getDependsB() {
        return dependsB.toArray(new Integer[dependsB.size()]);
    }

    /**
     * Liefert die Einheiten Abhängigkeiten als DESC-U zurück
     * @return Integer[] mit DESC-U
     */
    public Integer[] getDependsU() {
        return dependsU.toArray(new Integer[dependsU.size()]);
    }

    /**
     * Liefert die Upgrade Abhängigkeiten als DESC-A zurück
     * @return Integer[] mit DESC-A
     */
    public Integer[] getDependsA() {
        return dependsA.toArray(new Integer[dependsA.size()]);
    }

    /**
     * Fügt eine Bedingung zur Gebäude-Abhängigkeitsliste hizu
     * @param depend Ein DESC-B
     */
    public void addDependB(int depend) {
        dependsB.add(new Integer(depend));
    }

    /**
     * Fügt eine Bedingung zur Einheiten-Abhängigkeitsliste hizu
     * @param depend Ein DESC-U
     */
    public void addDependU(int depend) {
        dependsU.add(new Integer(depend));
    }

    /**
     * Fügt eine Bedingung zur Upgrade-Abhängigkeitsliste hinzu
     * @param depend Ein DESC-A
     */
    public void addDependA(int depend) {
        dependsA.add(new Integer(depend));
    }

    public void setInvoker(GameObject invoker) {
        this.invoker = invoker;
    }

    public void setImages(String[] imgs) {
        symbols = imgs;
    }

    public void refreshCoreInner(ClientCore.InnerClient newinner) {
        rgi = newinner;
    }

    public void setCooldown(double d) {
        cooldown = d;
    }

    public void setName(String newname) {
        name = newname;
    }

    public String[] getMissingDependencies() {
        return rgi.game.getOwnPlayer().getMissingDependencies(dependsB, dependsU, rgi.game.getOwnPlayer().descUnit, rgi.game.getOwnPlayer().descBuilding, dependsA, rgi.game.getOwnPlayer().clientDescAbilities);
    }

    public int getNumberOfMissingResources() {
        int r = 0;
        if (rgi.game.getOwnPlayer().res1 < costs[0]) {
            r++;
        }
        if (rgi.game.getOwnPlayer().res2 < costs[1]) {
            r++;
        }
        if (rgi.game.getOwnPlayer().res3 < costs[2]) {
            r++;
        }
        if (rgi.game.getOwnPlayer().res4 < costs[3]) {
            r++;
        }
        if (rgi.game.getOwnPlayer().res5 < costs[4]) {
            r++;
        }
    /*    if (this.type == Ability.ABILITY_BUILD) {
            AbilityBuild abb = (AbilityBuild) this;
            Building building = rgi.mapModule.getDescBuilding(abb.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
            if (rgi.game.getOwnPlayer().freeLimit() < building.limit) {
                r++;
            }
        } else if (this.type == Ability.ABILITY_RECRUIT) {
            AbilityRecruit abr = (AbilityRecruit) this;
            Unit unit = rgi.mapModule.getDescUnit(abr.descTypeId, -1, rgi.game.getOwnPlayer().playerId);
            if (rgi.game.getOwnPlayer().freeLimit() < unit.limit) {
                r++;
            }
        } */
        return r;
    }

    @Override
    public Ability clone() throws CloneNotSupportedException {
        Ability newab = (Ability) super.clone();
        newab.dependsB = (ArrayList<Integer>) dependsB.clone();
        newab.dependsU = (ArrayList<Integer>) dependsU.clone();
        newab.dependsA = (ArrayList<Integer>) dependsA.clone();
        return newab;
    }
}
