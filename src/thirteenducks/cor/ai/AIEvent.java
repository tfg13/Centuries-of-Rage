/*
 *  Copyright 2008, 2009, 2010:
 *   Tobias Fleig (tfg[AT]online[DOT]de),
 *   Michael Hase (mekhar[AT]gmx[DOT]de),
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

/**
 * Basisklasse für Erreignisse, auf die die KI reagiert
 * @author michael
 */
public class AIEvent {

    // Referenz auf das KI-Modul:
    AIModule aiModule;



    /**
     * Konstruktor
     * @param inner - Referenz auf Clientcore
     */
    public AIEvent(AIModule aimod) {
        aiModule = aimod;

        // Ereignis in einem eigenen Thread bearbeiten:
        Thread mythread = new Thread(new Runnable() {

            public void run() {
                process();
            }
        });

        mythread.setName("AIEventThread");
        mythread.start();
    }
    

    /**
     * Bearbeitet das Ereignis
     * Wird von den erweiternden Klassen überschrieben
     */
    void process() {
    }
}// Klassenende

