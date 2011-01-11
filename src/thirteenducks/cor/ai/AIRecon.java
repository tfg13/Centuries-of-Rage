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

import java.util.HashMap;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.client.ClientCore;

/**
 * Analytikmodul der KI
 * Analysiert Truppenverhältnisse, Gegnerverhalten, etc...
 */
public class AIRecon {

    // Core-Referenz:
    ClientCore.InnerClient rgi;
    Position townCenter;                         // Zentrum der Basis

    HashMap<String,Integer> variables;

    long gameStartTime;                              // Zeitpunkt des Spielstarts

    // Konstruktor:
    public AIRecon(ClientCore.InnerClient inner) {
        rgi = inner;

        variables = new HashMap<String, Integer>();


        // Zentrum der Basis bestimmen:
        for (Building b : rgi.mapModule.buildingList) {
            if (b.playerId == rgi.game.getOwnPlayer().playerId) {
                townCenter = b.position;
            }
        }
    }

    /**
     * Gibt den Wert einer Variablen zurück
     * @param variable
     * @return
     */
    public int getValue(String variable)
    {
        // Zeit (in Sekunden) seit Spielbeginn:
        if(variable.equals("time"))
        {
            return(int)((System.currentTimeMillis() - gameStartTime)/1000);
        }

        System.out.println("AIAnalytics: Variable not found: " + variable);
        return 0;
    }

    /**
     * registriert den Spielstart
     */
    void gameStarted()
    {
        gameStartTime = System.currentTimeMillis();
    }

}// Klasssenende

