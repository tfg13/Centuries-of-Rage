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
package de._13ducks.cor.game.server;

import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.GameObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import de._13ducks.cor.game.BehaviourProcessor;
import de._13ducks.cor.game.NetPlayer;
import de._13ducks.cor.game.ability.ServerAbilityUpgrade;
import de._13ducks.cor.game.Unit;

/**
 * Die Server-Mainloop und GameLogic
 *
 * @author tfg
 */
public class ServerGameController implements Runnable {

    private List<Unit> unitList;             // Die Liste mit den Einheiten
    /**
     * Eine Liste mit allen Objekten, die Behaviours ausführen können.
     */
    private List<BehaviourProcessor> allList;
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

            for (int i = 0; i < allList.size(); i++) {
                BehaviourProcessor processor = allList.get(i);
                processor.process();
            }

            try {
                Thread.sleep(10);
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
        // Allen Units das ServerBehaviourMove geben
        for (Unit unit : unitList) {
            unit.initServerMovementManagers(rgi, rgi.netmap.getMoveMap());
            // Bewegungssektor für jede Einheit suchen und setzten
            rgi.netmap.registerUnitMovements(unit);
        }
        // Alle auf fertig setzen
        for (NetPlayer player : playerList) {
            player.setFinished(true);
        }
        // Die die Gebäude haben auf "noch spielend" setzen
        for (Building b : rgi.netmap.buildingList) {
            try {
                playerList.get(b.getPlayerId()).setFinished(false);
            } catch (Exception ex) {
            }
        }
        this.startMainloop();
        Thread tr = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(10000);
                        // Put test code here
                    }
                } catch (InterruptedException ex) {
                }
            }
        });
        tr.start();
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

    public void registerAllList(List<BehaviourProcessor> allList) {
        this.allList = allList;
    }

    public void registerBuilding(int playerId, Building building) {
        NetPlayer player = null;
        try {
            player = playerList.get(playerId);
        } catch (Exception ex) {
            System.out.println("FixMe: Trying to add Building to unknown Player");
        }
        if (player != null) {
            if (!player.bList.contains(building.getDescTypeId())) {
                player.bList.add(building.getDescTypeId());
            }
        }
    }

    public NetPlayer addPlayer() {
        NetPlayer newplayer = new NetPlayer(rgi);
        newplayer.playerId = playerList.size();
        System.out.println("New PlayerID for Client: " + newplayer.playerId);
        newplayer.descUnit = completeCloneUnits(rgi.netmap.descTypeUnit);
        newplayer.descBuilding = completeCloneBuildings(rgi.netmap.descTypeBuilding);
        newplayer.serverDescAbilities = completeCloneAbilitys(rgi.netmap.descTypeAbilities);
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
        NetPlayer player1 = rgi.game.getPlayer(obj1.getPlayerId());
        if (player1 != null) {
            NetPlayer player2 = rgi.game.getPlayer(obj2.getPlayerId());
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
        NetPlayer player1 = rgi.game.getPlayer(obj1.getPlayerId());
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
        NetPlayer player1 = rgi.game.getPlayer(obj1.getPlayerId());
        if (player1 != null) {
            NetPlayer player2 = rgi.game.getPlayer(obj2.getPlayerId());
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
        NetPlayer player1 = rgi.game.getPlayer(obj1.getPlayerId());
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

    private HashMap<Integer, Unit> completeCloneUnits(HashMap<Integer, Unit> map) {
        HashMap<Integer, Unit> newmap = new HashMap<Integer, Unit>();
        Set<Integer> keys = map.keySet();
        Iterator<Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            newmap.put(key, (Unit) map.get(key).getCopy(-1));
        }
        return newmap;
    }

    private HashMap<Integer, Building> completeCloneBuildings(HashMap<Integer, Building> map) {
        HashMap<Integer, Building> newmap = new HashMap<Integer, Building>();
        Set<Integer> keys = map.keySet();
        Iterator<Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            newmap.put(key, (Building) map.get(key).getCopy(-1));
        }
        return newmap;
    }

    private HashMap<Integer, ServerAbilityUpgrade> completeCloneAbilitys(HashMap<Integer, ServerAbilityUpgrade> map) {
        HashMap<Integer, ServerAbilityUpgrade> newmap = new HashMap<Integer, ServerAbilityUpgrade>();
        Set<Integer> keys = map.keySet();
        Iterator<Integer> iter = keys.iterator();
        while (iter.hasNext()) {
            Integer key = iter.next();
            try {
                newmap.put(key, map.get(key).clone());
            } catch (CloneNotSupportedException ex) {
                System.out.println("NEVEREVER: Cannot clone Ability!");
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

        for (int i = 0; i < allList.size(); i++) {
            BehaviourProcessor processor = allList.get(i);
            processor.managePause(pause);
        }
    }

    /**
     * Fügt dieses GO zum Behaviourssystem hinzu.
     * Die Behaviours dieses GO's werden zukünftig berechnet.
     * @param go das hinzuzufügende GO
     */
    public void addGO(GameObject go) {
        allList.add(go);
    }

    /**
     * Entfernt dieses GO aus dem Behavioursystem
     * Die Behaviours dieses GOs werden in Zunkunft nichtmehr ausgeführt.
     * Eine einmalige zukünftige Ausführung kann allerdings nicht 100% ausgeschlossen werden.
     * Diese würde allerdings praktisch sofort geschehen, später werden die behaviours sicher nicht mehr aufgerufen.
     * @param go das zu entfernene GO
     */
    public void removeGO(GameObject go) {
        allList.remove(go);
    }
}
