/*
 *  Copyright 2008, 2009, 2010:
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
package thirteenducks.cor.game.server;

import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit.orders;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.game.ability.ServerAbilityUpgrade;
import thirteenducks.cor.game.server.behaviour.ServerBehaviour;
import thirteenducks.cor.game.server.behaviour.impl.ServerBehaviourAttack;
import thirteenducks.cor.game.server.behaviour.impl.ServerBehaviourMove;
import thirteenducks.cor.game.Unit;

/**
 * Die Server-Mainloop und GameLogic
 *
 * @author tfg
 */
public class ServerGameController implements Runnable {

    private List<Unit> unitList;             // Die Liste mit den Einheiten
    private List<Building> buildingList;     // Die Liste mit den Gebäuden
    private List<Ressource> resList;         // Die Liste mit den Ressourcen
    List<NetPlayer> playerList;         // Die Liste mit den Spielern VORSICHT- ES DARF NIEMALS JEMAND GELÖSCHT WERDEN!!!
    ServerCore.InnerServer rgi;         // Die Referenz auf logger und alle anderen Module
    Thread t;
    boolean pause = false;

    @Override
    public void run() {

        // Mainloop
        rgi.logger("Starting Mainloop...");
        while (true) {

            // Alle Behaviour durchgehen

            while (pause) {
                if (!pause) {
                    break;
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                }
            }

            for (int u = 0; u < unitList.size(); u++) {
                Unit unit = unitList.get(u);
                for (int b = 0; b < unit.sbehaviours.size(); b++) {
                    ServerBehaviour s = unit.sbehaviours.get(b);
                    if (s.isActive()) {
                        s.tryexecute();
                    }
                }
            }


            for (int u = 0; u < buildingList.size(); u++) {
                Building building = buildingList.get(u);
                for (int b = 0; b < building.sbehaviours.size(); b++) {
                    ServerBehaviour s = building.sbehaviours.get(b);
                    if (s.isActive()) {
                        s.tryexecute();
                    }
                }
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                rgi.logger(ex);
            }

        }
    }

    public void startMainloop() {
        if (t == null) {
            t = new Thread(this);
            t.setName("GameEngine: Mainloop");
            t.start();
        }
    }

    /**
     * Bereitet den Start des Spiels vor, added z.B. die nötigen Behaviours
     */
    public void prepareStart(int numberOfPlayers) {
        // Map vorbereiten
        rgi.serverstats.createStatArrays(numberOfPlayers); //Der Serverstatistik die Spielerzahl mitteilen
        rgi.netmap.initUnitRefLists(numberOfPlayers);
        // Allen Units das ServerBehaviourMove geben
        for (Unit unit : unitList) {
            ServerBehaviourMove smove = new ServerBehaviourMove(rgi, unit);
            unit.sbehaviours.add(smove);
            unit.moveManager = smove;
            ServerBehaviourAttack amove = new ServerBehaviourAttack(rgi, unit);
            unit.sbehaviours.add(amove);
            unit.attackManager = amove;
            unit.order = orders.idle;
            // Referenzen aller Einheiten eintragen
            rgi.netmap.setUnitRef(unit.position, unit, unit.playerId);
        }
        // Alle auf fertig setzen
        for (NetPlayer player : playerList) {
            player.setFinished(true);
        }
        // Die die Gebäude haben auf "noch spielend" setzen
        for (Building b : rgi.netmap.buildingList) {
            try {
                playerList.get(b.playerId).setFinished(false);
            } catch (Exception ex) {
            }
        }
        this.startMainloop();
    }

    public ServerGameController(ServerCore.InnerServer inner) {
        rgi = inner;
        playerList = Collections.synchronizedList(new ArrayList<NetPlayer>());

        // Den 0-Player adden
        playerList.add(new NetPlayer(rgi));
        playerList.get(0).playerId = 0;
    }

    public void registerUnitList(List<Unit> uL) {
        unitList = uL;
    }

    public void registerBuildingList(List<Building> bL) {
        buildingList = bL;
    }

    public void registerRessourceList(List<Ressource> rL) {
        resList = rL;
    }

    public void registerBuilding(int playerId, Building building) {
        NetPlayer player = null;
        try {
            player = playerList.get(playerId);
        } catch (Exception ex) {
            System.out.println("FixMe: Trying to add Building to unknown Player");
        }
        if (player != null) {
            if (!player.bList.contains(building.descTypeId)) {
                player.bList.add(building.descTypeId);
            }
        }
    }

    public NetPlayer addPlayer() {
        NetPlayer newplayer = new NetPlayer(rgi);
        newplayer.playerId = playerList.size();
        System.out.println("New PlayerID for Client: " + newplayer.playerId);
        newplayer.descUnit = completeCloneUnit(rgi.netmap.descTypeUnit);
        newplayer.descBuilding = completeCloneBuilding(rgi.netmap.descTypeBuilding);
        newplayer.serverDescAbilities = completeCloneAbility(rgi.netmap.descTypeAbilities);
        playerList.add(newplayer);
        return newplayer;
    }

    public NetPlayer getPlayer(int playerId) {
        try {
            return playerList.get(playerId);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Berechnet, ob die Einheiten gerade verbündet sind.
     * Liefert true zurück, wenn sie sich nicht gegenseitig bekriegen sollen.
     * @param obj
     * @param obj2
     * @return
     */
    public boolean areAllies(GameObject obj1, GameObject obj2) {
        NetPlayer player1 = rgi.game.getPlayer(obj1.playerId);
        if (player1 != null) {
            NetPlayer player2 = rgi.game.getPlayer(obj2.playerId);
            return player1.allies.contains(player2);
        }
        return false;
    }

    /**
     * Berechnet, ob diese beiden die Sichtweite teilen.
     * Liefert true zurück, wenn die Spieler auch den Sichtradius des jeweils anderen sehen sollten.
     * @param obj
     * @param obj2
     * @return
     */
    public boolean areAllies(GameObject obj1, NetPlayer player2) {
        NetPlayer player1 = rgi.game.getPlayer(obj1.playerId);
        if (player1 != null) {
            return player1.allies.contains(player2);
        }
        return false;
    }

    /**
     * Berechnet, ob diese beiden die Sichtweite teilen.
     * Liefert true zurück, wenn die Spieler auch den Sichtradius des jeweils anderen sehen sollten.
     * @param obj
     * @param obj2
     * @return
     */
    public boolean areAllies(NetPlayer player1, NetPlayer player2) {
        return player1.allies.contains(player2);
    }

    /**
     * Berechnet, ob diese beiden Einheiten die Sichtweite teilen.
     * Liefert true zurück, wenn die Spieler auch den Sichtradius des jeweils anderen sehen sollten.
     * @param obj
     * @param obj2
     * @return
     */
    public boolean shareSight(GameObject obj1, GameObject obj2) {
        NetPlayer player1 = rgi.game.getPlayer(obj1.playerId);
        if (player1 != null) {
            NetPlayer player2 = rgi.game.getPlayer(obj2.playerId);
            return player1.visAllies.contains(player2);
        }
        return false;
    }

    /**
     * Berechnet, ob diese beiden die Sichtweite teilen.
     * Liefert true zurück, wenn die Spieler auch den Sichtradius des jeweils anderen sehen sollten.
     * @param obj
     * @param obj2
     * @return
     */
    public boolean shareSight(GameObject obj1, NetPlayer player2) {
        NetPlayer player1 = rgi.game.getPlayer(obj1.playerId);
        if (player1 != null) {
            return player1.visAllies.contains(player2);
        }
        return false;
    }

    /**
     * Berechnet, ob diese beiden die Sichtweite teilen.
     * Liefert true zurück, wenn die Spieler auch den Sichtradius des jeweils anderen sehen sollten.
     * @param obj
     * @param obj2
     * @return
     */
    public boolean shareSight(NetPlayer player1, NetPlayer player2) {
        return player1.visAllies.contains(player2);
    }

    private HashMap<Integer, Unit> completeCloneUnit(HashMap<Integer, Unit> map) {
        HashMap<Integer, Unit> newmap = new HashMap<Integer, Unit>();
        Set<Integer> keys = map.keySet();
        Iterator<Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            try {
                newmap.put(key, map.get(key).clone(-1));
            } catch (CloneNotSupportedException ex) {
            }
        }
        return newmap;
    }

    private HashMap<Integer, Building> completeCloneBuilding(HashMap<Integer, Building> map) {
        HashMap<Integer, Building> newmap = new HashMap<Integer, Building>();
        Set<Integer> keys = map.keySet();
        Iterator<Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            try {
                newmap.put(key, map.get(key).clone(-1));
            } catch (CloneNotSupportedException ex) {
            }
        }
        return newmap;
    }

    private HashMap<Integer, ServerAbilityUpgrade> completeCloneAbility(HashMap<Integer, ServerAbilityUpgrade> map) {
        HashMap<Integer, ServerAbilityUpgrade> newmap = new HashMap<Integer, ServerAbilityUpgrade>();
        Set<Integer> keys = map.keySet();
        Iterator<Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            try {
                newmap.put(key, map.get(key).clone());
            } catch (CloneNotSupportedException ex) {
            }
        }
        return newmap;
    }

    public void togglePause() {
        if (pause) {
            pause = false;
            managePause(false);
        } else {
            pause = true;
            managePause(true);
        }
    }

    /**
     * Verwaltet die Pause-Befehle für Behaviours und andere
     * Zeitkritische Anwendungen
     *
     * @param b Boolean, auf Pause stellen (true) oder zurück(false)
     */
    private void managePause(boolean pause) {
        // Alle Behaviour pausieren
        for (int u = 0; u < unitList.size(); u++) {
            Unit unit = unitList.get(u);
            for (int b = 0; b < unit.sbehaviours.size(); b++) {
                ServerBehaviour c = unit.sbehaviours.get(b);
                if (c.isActive()) {
                    if (pause) {
                        c.pause();
                    } else {
                        c.unpause();
                    }
                }
            }
        }


        for (int u = 0; u < buildingList.size(); u++) {
            Building building = buildingList.get(u);
            for (int b = 0; b < building.sbehaviours.size(); b++) {
                ServerBehaviour c = building.sbehaviours.get(b);
                if (c.isActive()) {
                    if (pause) {
                        c.pause();
                    } else {
                        c.unpause();
                    }
                }
            }
        }
    }
}
