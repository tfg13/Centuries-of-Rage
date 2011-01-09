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
package thirteenducks.cor.ai;

import java.util.ArrayList;
import thirteenducks.cor.game.Ressource;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourHarvest;

/**
 * Der Ressourcenmanager der KI
 * verwaltet ressourcen und teilt arbeiter ein
 *
 * @author michael
 */
public class AIEconomyManager {

    ClientCore.InnerClient rgi;                 // Core-Referenz
    ArrayList<Unit> myworkers;                  // Die Arbeiter
    /**
     * Ressourcennachfrage:
     */
    int foodDemand;
    int woodDemand;
    int ironDemand;
    int goldDemand;

    /**
     * Konstruktor, initialisiert die Listen etc
     */
    public AIEconomyManager(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    /**
     * Weist den Arbeiter dem economyManager zu
     */
    void useWorker(Unit worker) {
        for (ClientBehaviour b : worker.cbehaviours) {
            try {
                ClientBehaviourHarvest hb = (ClientBehaviourHarvest) b;
                if (hb != null) {
                    hb.res = null;
                }
            } catch (Exception ex) {
            }

        }

        myworkers.add(worker);
        assignWorkers();
    }

    /**
     * Stellt die Ressourcenprioritäten ein
     * Der economyManager versucht Ressourcenim entsprechenden Verhältnis abzubauen
     */
    void setIncomeRatio(int food, int wood, int iron, int gold) {
        foodDemand = food;
        woodDemand = wood;
        ironDemand = iron;
        goldDemand = gold;
    }

    /**
     * Die ersten Arbeiter auf die Büsche verteilen
     * pro Busch sollte mindestens ein Feld frei sein
     */
    void gamestart() {

        myworkers = rgi.aiModule.reconManager.getStartWorkers();
        System.out.println("gamestart:" + myworkers.size());


        for (Unit r : myworkers) {
            r.goHarvest(rgi.aiModule.reconManager.getBushToHarvest(), rgi);


        }
    }

    /**
     * befreit einen Arbeiter vom Dienst, damit er für ein anderes modul schuften kann (z.B. gebäudebau für den productionManager oder als Kampfarbeiter für tacticManager)
     * @return
     */
    Unit releaseWorker() {
        if (myworkers.size() == 0) {
            return null;


        }
        Unit u = myworkers.get(0);
        myworkers.remove(u);


        return u;


    }

    /**
     * weißt alle arbeiter zum arbeiten an
     * die arbeiter werden dabei relativ zum angegebenen ressourceDemand uaf die einzelnen ressourcen verteilt
     */
    void assignWorkers() {
        int numWorkers = myworkers.size();
        int remainingWorkers = numWorkers;


        int ressourceDemand[] = new int[5];
        ressourceDemand[1] = rgi.aiModule.economyManager.foodDemand;
        ressourceDemand[2] = rgi.aiModule.economyManager.woodDemand;
        ressourceDemand[3] = rgi.aiModule.economyManager.ironDemand;
        ressourceDemand[4] = rgi.aiModule.economyManager.goldDemand;

        // Den Gesamtressourcenbedarf berechnen:


        int alldemands;
        alldemands = 0;


        for (int i = 1; i
                < 5; i++) {
            //System.out.println("Alldemands = " + alldemands);
            alldemands += ressourceDemand[i];


        } // Die neue Arbeiterverteilung:
        int[] newworkers = new int[5];

        // Die Arbeiter anteilig je nach Resourcenbedarf verteilen:


        for (int i = 1; i
                < 5; i++) {
            double newdemand = ressourceDemand[i];


            double newalldemands = alldemands;
            newworkers[i] = (int) (numWorkers * (newdemand / newalldemands));
            remainingWorkers -= newworkers[i];


        } //        for (int i = 1; i < 5; i++) {
        //            System.out.println("ressourcef´demand " + i + " = " + ressourceDemand[i]);
        //        }
        //        System.out.println("alldemands: " + alldemands);
        // Jetzt überprüfen, wie viel Arbeiter schon richtig verteilt sind
        // Wenn ein arbeiter zu viel auf einer ressource ist wird er freigestellt und nacher auf was anderes verteilt
        ArrayList<Unit> freeworkers = new ArrayList<Unit>();


        for (Unit u : myworkers) {
            ClientBehaviourHarvest harvBehaviour = (ClientBehaviourHarvest) u.getbehaviourC(7);
            // Wenn die Einheit die entsprechende Ressource abbauen kann zur liste hinzufügen, sonst nix tun:


            try {
                int ressource = harvBehaviour.res.getType();

                newworkers[ressource]--;


                if (newworkers[ressource] < 0) {
                    freeworkers.add(u);


                }
            } catch (Exception ex) {
                // wenn es oben einen fehler gibt wird die einheit als arbeitslos betrachtet:
                freeworkers.add(u);
            }

        }


        // die freien arbeiter verteilen:
        if (freeworkers.size() != 0) {
            for (int i = 1; i
                    < 5; i++) {
                while (newworkers[i] > 0 && freeworkers.size() != 0) {
                    newworkers[i]--;
                    freeworkers.get(0).goHarvest(freeworkers.get(0).ressourceAroundMe(i, 30, rgi), rgi);
                    freeworkers.remove(0);

                }
            }
        }
    }
}// Klassenende

