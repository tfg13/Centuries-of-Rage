/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.map;

import java.util.HashMap;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.ability.ServerAbilityUpgrade;

/**
 * Return-Dingens für GameDescReader-Teil mit 3 oder 4 so Hashmaps
 * @author Johannes
 */
public class GameDescReaderParameter {
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
