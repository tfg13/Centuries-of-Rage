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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import thirteenducks.cor.game.client.ClientCore;

/**
 * Strategiemanager
 * Trifft Strategische Entscheidungen auf hoher Ebene
 *
 * @author michael
 */
public class AIStrategyManager {

    ClientCore.InnerClient rgi;

    /**
     * Konstruktor
     */
    public AIStrategyManager(ClientCore.InnerClient inner) {
        rgi = inner;

    }

    /**
     * Standard-Strategie am Anfang
     */
    void activateInitialisationStrategy() {

        applyBO();
//        rgi.game.getOwnPlayer().res1 = 10000;
//        rgi.game.getOwnPlayer().res2 = 10000;
//        rgi.game.getOwnPlayer().res3 = 10000;
//        rgi.game.getOwnPlayer().res4 = 10000;


        rgi.aiModule.economyManager.setIncomeRatio(1, 1, 0, 0);
        rgi.aiModule.economyManager.assignWorkers();
        planProductionLoop();

    }

    /**
     * überprüft die produktionsschleife alle 5 sekunden
     */
    void planProductionLoop() {
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    while (true) {
                        Thread.sleep(5000);
                        rgi.aiModule.productionManager.planProduction();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(AIStrategyManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        t.setName("ai_planproductionloopthread");
        t.start();
    }

    private void applyBO() {
        try {
            File theFile = new File("ai/bo");
            FileReader fReader;
            fReader = new FileReader(theFile);
            BufferedReader bReader = new BufferedReader(fReader);

            String line = "";
            int index = 1;
            line = bReader.readLine();
            boolean isBuilding = false;
            while (line != null) {
                ArrayList<String> words = makeWords(line);
                if (words != null) {
                    if (words.get(1).equals("building")) {
                        isBuilding = true;
                    } else {
                        isBuilding = false;
                    }
                    index++;
                    rgi.aiModule.productionManager.requestProduction(Integer.parseInt(words.get(0)), index, 1, isBuilding);

                }
                // Die nächste Zeile lesen:
                line = bReader.readLine();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }



    }

    /**
     * Zerteilt einen Text
     * durch leerzeichen getrennte wörter werden getrennt und als arraylist zurückgegeben
     * alles was zwischen anführungszeichen (") steht wird als ein wort erkannt
     * @param line  -    der text
     * @return      -   eine arraylist mit den wörtern
     */
    static ArrayList<String> makeWords(String line) {
        // Kommentare rausschneiden:
        if (line.contains("#")) {
            line = line.substring(0, line.indexOf("#"));
        }

        // Wenn die Zeile leer ist zur nächsten springen:
        if (line.equals("")) {
            return null;
        }


        // Tabs ersetzen und doppelte leerzeichen entfernen:
        line = line.replaceAll("\\t", " ");
        line = line.replaceAll("\\s\\s+", " ");
//                System.out.println("Line after work: " + line);

        // In einzelne Wörter aufteilen:
        ArrayList<String> words = new ArrayList<String>();
        String myword = "";
        boolean quotemode = false;  // sind wir gerade zwischen anführungszeichen?
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                // quotemode umschalten:
                if (quotemode == true) {
                    quotemode = false;
                    //System.out.println(i + "quotemode deactivated");
                } else {
                    quotemode = true;
                    //System.out.println(i + "quotemode activated");
                }

                if (!myword.equals("")) {
                    words.add(myword);
                    myword = "";
                }

                // zum nächsten zeichen, das anführungszeichen wollen wir nicht
                continue;
            }
            // wenn zwischen anführungszeichen, dann einfach zum wort hinzufügen:
            if (quotemode) {
                myword += c;
                // wenn nicht, dann den text normal scannen:
            } else {
                if (c == ' ') {

                    if (!myword.equals("") && !myword.equals(" ")) {
                        words.add(myword);
                    }
                    myword = "";
                } else {
                    myword += c;
                }
                if (i == line.length() - 1) {
                    words.add(myword);
                }
            }
        }
        return words;
    }
}// Klassenende

