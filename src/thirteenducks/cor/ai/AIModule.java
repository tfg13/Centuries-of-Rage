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
package thirteenducks.cor.ai;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.client.ClientCore;

/**
 * The Artificial Intelligence
 * It's artificial, but is it intelligent?
 *
 * @author michael
 */
public class AIModule {

    ClientCore.InnerClient rgi;                     // Core-Referenz
    AIStrategyManager strategyManager;
    AIEconomyManager economyManager;
    AIReconManager reconManager;
    AIProductionManager productionManager;

    public AIModule(ClientCore.InnerClient clntcr) {
        rgi = clntcr;
    }

    /**
     * Initialisiert das KI-System
     */
    public void initAIModule() {

        // Module initialisieren:
        reconManager = new AIReconManager(rgi);
        economyManager = new AIEconomyManager(rgi);
        productionManager = new AIProductionManager(rgi);
        strategyManager = new AIStrategyManager(rgi);
    }

    /**
     * Wird beim Spielstart aufgerufen:
     */
    public void gameStartEvent() {
        economyManager.gamestart();
        strategyManager.activateInitialisationStrategy();
    }

    /**
     * Wird beim Empfangen von chatnachrichten aufgerufen:
     */
    public void chatMessageEvent(String chatmessage, int team) {
        ArrayList<String> words = new ArrayList<String>();

        String aword = "";
        for (int i = 0; i < chatmessage.length(); i++) {
            char c = chatmessage.charAt(i);
            if (c != ' ') {
                aword += c;
                if (i == chatmessage.length() - 1) {
                    words.add(aword);
                }
            } else {
                words.add(aword);
                aword = "";
            }
        }


        if (words.get(0).contains("debug")) {
            debug(words);
        }


    }

    void debug(ArrayList<String> words) {


        if (words.get(0).equals("debug_produce")) {

            System.out.println("producing....");
            boolean building;
            if (words.get(4).equals("true")) {
                building = true;
            } else {
                building = false;
            }
            rgi.aiModule.productionManager.requestProduction(Integer.parseInt(words.get(1)), Integer.parseInt(words.get(2)), Integer.parseInt(words.get(3)), building);
        }
        if (words.get(0).equals("debug_planproduction")) {
            productionManager.planProduction();
        }
        if(words.get(0).equals("debug_res"))
        {
            System.out.println("res1: " + rgi.game.getOwnPlayer().res1);
            System.out.println("res2: " + rgi.game.getOwnPlayer().res2);
            System.out.println("res3: " + rgi.game.getOwnPlayer().res3);
            System.out.println("res4: " + rgi.game.getOwnPlayer().res4);
        }
    }

    /**
     * wird gerufen, wenn eine neue einheit erstellt wird
     * @param unit
     */
    public void unitCreationEvent(Unit unit) {
        
        if (/*unit.playerId == rgi.game.getOwnPlayer().playerId*/true) {
            // arbeiter menschen:
            if (unit.descTypeId == 401) {
                rgi.aiModule.economyManager.useWorker(unit);
            }
        }
    }

    /**
     * wird gerufen wenn ein gebäude fertig gebaut ist
     * @param building
     */
    public void buildingConstructedEvent(Building building, Unit unit) {
        if (building.playerId == rgi.game.getOwnPlayer().playerId) {
            // arbeiter wieder zum ernten schicken:
            this.economyManager.useWorker(unit);
        }
    }
}// Klassenende

