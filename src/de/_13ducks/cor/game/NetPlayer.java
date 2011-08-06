/*
 *  Copyright 2008, 2009, 2010, 2011:
 *   Tobias Fleig (tfg@online.de),
 *   Michael Haas (mekhar@gmx.de),
 *   Johannes Kattinger (johanneskattinger@gmx.de)
 *  - All rights reserved -
 *
 *
 *  This file is part of Reign of Godwars.
 *
 *  Reign of Godwars is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Reign of Godwars is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Reign of Godwars.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de._13ducks.cor.game;

import de._13ducks.cor.game.ability.ServerAbilityUpgrade;
import de._13ducks.cor.game.ability.Ability;
import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.networks.globalbehaviour.GlobalBehaviour;
import de._13ducks.cor.networks.globalbehaviour.GlobalBehaviourProduceServer;
import org.newdawn.slick.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Diese Klasse enthält alle Infos über den aktuellen Spieler, der Rog tatsächlich spielt.
 *
 *
 * @author tfg
 */
public class NetPlayer {

    public int playerId;
    public List<Integer> bList;
    public List<Integer> uList;
    public List<Integer> aList;
    public Core.CoreInner rgi;
    public double res1 = 12345;
    public double res2 = 0;
    public double res3 = 0;
    public double res4 = 0;
    public double res5 = 0;
    public int[] harvspeeds;
    // Die Verfügbaren Einheiten, Gebäude und Abilities:
    // Werden jetzt hier gespeichert, damit individuelle Upgrades berücksichtigt werden können:
    public HashMap<Integer, Unit> descUnit;
    public HashMap<Integer, Building> descBuilding;
    public HashMap<Integer, Ability> clientDescAbilities;
    public HashMap<Integer, ServerAbilityUpgrade> serverDescAbilities;
    public Color color;
    // Truppenlimit - Client only
    public int maxlimit = 0;
    public int currentlimit = 0;
    /* Lobby-Informationen: */
    public String nickName;
    public boolean isReady;
    int lobbyColour;
    public boolean isHost;
    public int lobbyRace;

    public ArrayList<NetPlayer> allies;     // Verbündete
    public ArrayList<NetPlayer> visAllies;  // Teilen Sicht
    public ArrayList<NetPlayer> invitations; // Einladungen

    private boolean finished = false; // Spielt der noch?
    
    private GlobalBehaviour producebehaviour;

    public interface colours {

        int red = 1;
        int blue = 2;
        int green = 3;
        int purple = 4;
        int yellow = 5;
        int brown = 6;
        int gay = 7;
        int grey = 8;
        int black = 9;
    }

    public interface races {

        int undead = 1;     // Untote ftw!
        int human = 2;     // Menschen
        int random = 3;     // zufälliges Volk
    }

    public NetPlayer(Core.CoreInner newinner) {
        rgi = newinner;
        bList = Collections.synchronizedList(new ArrayList<Integer>());
        uList = Collections.synchronizedList(new ArrayList<Integer>());
        aList = Collections.synchronizedList(new ArrayList<Integer>());
        harvspeeds = new int[6];
        // Mit default-Werten füllen:
        for (int i = 0; i < 5; i++) {
            harvspeeds[i] = 60;
        }
        nickName = "";
        isReady = false;
        lobbyColour = 0;
        isHost = false;
        lobbyRace = 0;
        allies = new ArrayList<NetPlayer>();
        visAllies = new ArrayList<NetPlayer>();
        invitations = new ArrayList<NetPlayer>();


    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished the finished to set
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    /**
     * Initialisiert die bList und uList, die für Abhängigkeitssysteme benötigt werden.
     */
    public void initLists(List<Unit> unitList, List<Building> buildingList) {
        for (Building building : buildingList) {
            if (!bList.contains(new Integer(building.getDescTypeId()))) {
                bList.add(new Integer(building.getDescTypeId()));
            }
        }
        for (Unit unit : unitList) {
            if (!uList.contains(new Integer(unit.getDescTypeId()))) {
                uList.add(new Integer(unit.getDescTypeId()));
            }
        }
    }

    public int freeLimit() {
        return maxlimit - currentlimit;
    }

    public boolean checkAvailability(ArrayList<Integer> depB, ArrayList<Integer> depU, ArrayList<Integer> depA) {
        for (Integer i : depB) {
            if (!bList.contains(i)) {
                return false;
            }
        }
        for (Integer i : depU) {
            if (!uList.contains(i)) {
                return false;
            }
        }
        for (Integer i : depA) {
            if (!aList.contains(i)) {
                return false;
            }
        }
        return true;
    }

    public String[] getMissingDependencies(ArrayList<Integer> depB, ArrayList<Integer> depU, HashMap<Integer, Unit> descUnit, HashMap<Integer, Building> descBuilding, ArrayList<Integer> depA, HashMap<Integer, Ability> descAbilities) {
        ArrayList<String> list = new ArrayList<String>();
        // Die Fehlenden zurückgeben
        for (Integer i : depB) {
            if (!bList.contains(i)) {
                // Adden
                try {
                    list.add(descBuilding.get(i).getName());
                } catch (Exception ex) {
                    System.out.println("FixMe: MissDep-AddB");
                }
            }
        }
        for (Integer i : depU) {
            if (!uList.contains(i)) {
                try {
                    list.add(descUnit.get(i).getName());
                } catch (Exception ex) {
                    System.out.println("FixMe: MissDep-AddU");
                }
            }
        }
        for (Integer i : depA) {
            if (!aList.contains(i)) {
                try {
                    list.add(descAbilities.get(i).name);
                } catch (Exception ex) {
                    System.out.println("FixMe: MissDep-AddA");
                }
            }
        }
        String[] rlist = new String[list.size()];
        return list.toArray(rlist);
    }
    
    public void setProduceBehaviour(GlobalBehaviour producebehav, boolean server) {
        this.producebehaviour = producebehav;
    }
    
    public GlobalBehaviour getProduceBehaviour() {
        return producebehaviour;
    }
}
