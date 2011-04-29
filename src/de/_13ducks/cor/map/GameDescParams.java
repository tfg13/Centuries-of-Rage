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
 */

package de._13ducks.cor.map;

import java.util.HashMap;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.ability.Ability;
import de._13ducks.cor.game.ability.ServerAbilityUpgrade;

/**
 * Return-Dingens für GameDescReader-Teil mit 3 oder 4 so Hashmaps
 *
 * <br><br><hr><i>
 * 13Ducks entschuldigt sich für diesen unqualifizieren Javadoc-Header.
 * In der Autor-Zeile (oder mit git blame) können sie erkennen/herausfinden,
 * wer diesen Pfusch verbrochen hat. Bitte zögern Sie nicht, sich angemessen
 * zu beschweren, damit es bald besser wird. Danke.
 *
 * Diese Nachricht wurde automatisiert eingefügt. Sollte die Nachricht wieder
 * erwarten hier nicht angebracht sein, so beschweren Sie sich bitte (erneut).
 *
 * Vielen Dank, dass Sie dabei helfen, CoR vollständig von Pfusch zu befreien.
 * Sollten Sie jedoch für ihre Teilname an dieser Aktion in den Credits erwähnt werden wollen,
 * so haben Sie leider Pech gehabt. Wir wünschen trotzdem einen guten Tag.
 * </i><hr>
 * @author Johannes
 */
public class GameDescParams {
    private HashMap<Integer, Unit> units;
    private HashMap<Integer, Building> buildings;
    private HashMap<Integer, Ability> abilities;
    private HashMap<Integer, ServerAbilityUpgrade> serverabilities;

    /**
     * @return the units
     */
    public HashMap<Integer, Unit> getUnits() {
	return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(HashMap<Integer, Unit> units) {
	this.units = units;
    }

    /**
     * @return the buildings
     */
    public HashMap<Integer, Building> getBuildings() {
	return buildings;
    }

    /**
     * @param buildings the buildings to set
     */
    public void setBuildings(HashMap<Integer, Building> buildings) {
	this.buildings = buildings;
    }

    /**
     * @return the abilities
     */
    public HashMap<Integer, Ability> getAbilities() {
	return abilities;
    }

    /**
     * @param abilities the abilities to set
     */
    public void setAbilities(HashMap<Integer, Ability> abilities) {
	this.abilities = abilities;
    }

    /**
     * @return the serverabilities
     */
    public HashMap<Integer, ServerAbilityUpgrade> getServerabilities() {
	return serverabilities;
    }

    /**
     * @param serverabilities the serverabilities to set
     */
    public void setServerabilities(HashMap<Integer, ServerAbilityUpgrade> serverabilities) {
	this.serverabilities = serverabilities;
    }

    
}
