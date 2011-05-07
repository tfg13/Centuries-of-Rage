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
package de._13ducks.cor.game.client;

import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.NetPlayer.races;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.newdawn.slick.Color;
import java.util.Timer;
import java.util.TimerTask;
import de._13ducks.cor.game.BehaviourProcessor;
import de._13ducks.cor.game.NetPlayer;
import de._13ducks.cor.game.ability.Ability;

/**
 *
 * @author tfg
 */
public class ClientGameController implements Runnable {

    ClientCore.InnerClient rgi;
    private NetPlayer myself;
    Thread t;                                   // Der Thread, in dem die Mainloop läuft
    private boolean pause = false;              // Pause-Modus
    public List<NetPlayer> playerList;                 // Alle Spieler (vor allem die desc-Types dieser Spieler)
    private List<BehaviourProcessor> allList;

    public ClientGameController(ClientCore.InnerClient newinner) {
        rgi = newinner;
        myself = new NetPlayer(newinner);
        playerList = Collections.synchronizedList(new ArrayList<NetPlayer>());

        // Den 0-Player adden
        playerList.add(new NetPlayer(rgi));
        playerList.get(0).playerId = 0;
        playerList.get(0).color = Color.white;
    }

    public void startMainloop() {
        if (t == null) {
            t = new Thread(this);
            t.setName("GameEngine Mainloop");
            t.start();
        }
    }

    /**
     * Bereitet den Start des Spiels vor, added z.B. die nötigen Behaviours
     */
    public void prepareStart(int numberOfPlayers) {
        rgi.rogGraphics.setLoadStatus(9);

        // Spielerliste herstellen:
        // Jeder Spieler muss an der Stelle stehen die seiner playerId entspricht
        ArrayList<NetPlayer> sortedPlayerList = new ArrayList<NetPlayer>();
        for (int i = 0; i < playerList.size(); i++) {
            sortedPlayerList.add(new NetPlayer(this.rgi));
        }
        for (NetPlayer p : this.playerList) {
            sortedPlayerList.set(p.playerId, p);
        }
        this.playerList = sortedPlayerList;

        int addPlayers = numberOfPlayers - (playerList.size() - 1);
        // Bis zur Spieleranzahl auffüllen
        for (int i = 0; i < addPlayers; i++) {
            sortedPlayerList.add(new NetPlayer(this.rgi));
        }

        myself.color = this.playerList.get(myself.playerId).color;
        myself.lobbyRace = this.playerList.get(myself.playerId).lobbyRace;
        this.playerList.set(myself.playerId, myself);

        // Upgradesystem initialisieren:
        for (NetPlayer player : playerList) {
            player.clientDescAbilities = completeCloneAbilitys(myself.clientDescAbilities);
            player.descBuilding = completeCloneBuildings(myself.descBuilding);
            player.descUnit = completeCloneUnits(myself.descUnit);
        }

        // Farben einstellen - falls noch leer
        for (NetPlayer player : playerList) {
            if (player.color == null) {
                player.color = Color.black;
            }
        }

        // Starteinheiten & Gebäude an das gewählte Volk anpassen:
        for (NetPlayer player : this.playerList) {
            // Gebäude:
            for (Building building : this.rgi.mapModule.buildingList) {
                if (building.getPlayerId() == player.playerId) {
                    if (player.lobbyRace == races.undead) {
                        building.performUpgrade(rgi, 1001);
                    } else if (player.lobbyRace == races.human) {
                        building.performUpgrade(rgi, 1);
                    }
                }
            }

            // Einheiten:
            for (Unit unit : this.rgi.mapModule.unitList) {
                if (unit.getPlayerId() == player.playerId) {
                    if (player.lobbyRace == races.undead) {
                        // 401=human worker, 1401=undead worker
                        if (unit.getDescTypeId() == 401) {
                            unit.performUpgrade(rgi, 1401);
                        }
                        // 402=human scout, 1402=undead scout
                        if (unit.getDescTypeId() == 402) {
                            unit.performUpgrade(rgi, 1402);
                        }


                    } else if (player.lobbyRace == races.human) {
                        // 401=human worker, 1401=undead worker
                        if (unit.getDescTypeId() == 1401) {
                            unit.performUpgrade(rgi, 401);
                        }
                        // 402=human scout, 1402=undead mage
                        if (unit.getDescTypeId() == 1402) {
                            unit.performUpgrade(rgi, 402);
                        }
                    }
                }
            }

        }

        System.out.println("AddMe: Add intramanager for Buildings");
        /*for (Building building : buildingList) {
            AbilityIntraManager intram = new AbilityIntraManager(building, rgi);
            building.addAbility(intram);
        }*/

        // Dem Spieler Startressourcen geben
        myself.res1 = 200;
        myself.res2 = 100;

        rgi.clientstats.createStatArrays(numberOfPlayers);

        rgi.rogGraphics.content.initState = 4;
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

    private HashMap<Integer, Ability> completeCloneAbilitys(HashMap<Integer, Ability> map) {
        HashMap<Integer, Ability> newmap = new HashMap<Integer, Ability>();
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

    public void registerAllList(List<BehaviourProcessor> allList) {
        this.allList = allList;
    }

    public void setPlayer(NetPlayer player) {
        myself = player;
    }

    public NetPlayer getPlayer(int playerId) {
        return playerList.get(playerId);
    }

    public NetPlayer getOwnPlayer() {
        return myself;
    }

    public boolean isPaused() {
        return pause;
    }

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
                BehaviourProcessor proc = allList.get(i);
                proc.process();
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                rgi.logger(ex);
            }

        }
    }

    public void registerBuilding(int playerId, Building building) {
        if (myself != null) {
            if (!myself.bList.contains(building.getDescTypeId())) {
                myself.bList.add(building.getDescTypeId());
            }
        }
    }

    public void togglePause() {
        if (pause) {
            pause = false;
            rgi.rogGraphics.unpause();
            rgi.rogGraphics.inputM.unpause();
            // Behaviour pausieren:
            managePause(false);
        } else {
            pause = true;
            rgi.rogGraphics.pause();
            rgi.rogGraphics.inputM.pause();
            managePause(true);
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

    public boolean wasInvited(NetPlayer player) {
        return myself.invitations.contains(player);
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
            BehaviourProcessor proc = allList.get(i);
            proc.managePause(pause);
        }
    }

    /**
     * Lässt diesen Spieler verlieren
     */
    public void defeat() {
        // Spieler "DEFEATED" anzeigen
        rgi.rogGraphics.defeated();
        rgi.rogSound.playSound("defeated.ogg");
    }

    /**
     * Lässt diesen Spieler gewinnen
     */
    public void win() {
        rgi.rogGraphics.win();
        sendStatistics();
    }

    /**
     * Das Spiel ist zu Ende
     */
    public void done() {
        // Spieler "DEFEATED" anzeigen
        rgi.rogGraphics.done();
        rgi.rogSound.playSound("defeated.ogg");
        sendStatistics();
    }

    public void sendStatistics() {
        // Client-Statistiken an Server senden
        for (int i = 1; i <= 5; i++) { // Anzahl der jeweils gesammelten Ressourcen
            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 46, i, rgi.clientstats.rescollected[i], 0, 0));
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask()  {

            public void run() {
                rgi.rogGraphics.showstatistics();
            }
        }, 3000);
    }

    /**
     * Fügt ein GO hinzu.
     * Alle Behaviours dieses GO's werden ab sofort berechnet.
     * @param go das zu addende GO
     */
    public void addGO(GameObject go) {
        allList.add(go);
    }
    /**
     * Löscht ein GO.
     * Die Behaviours dieses GO's werden nichtmehr ausgeführt.
     * Eine noch einmalige Ausführung nach dem return dieser Methode kann aber nicht ausgeschlossen werden.
     * @param go das zu entfernende go
     */
    void removeGO(GameObject go) {
        allList.remove(go);
    }
}
