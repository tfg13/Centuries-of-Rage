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
package thirteenducks.cor.script;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import thirteenducks.cor.game.client.ClientCore;

/**
 * SkriptModul
 * Lädt und verarbeitet Python-Scripts
 * 
 * @author michael
 */
public class ScriptModule {

    ClientCore.InnerClient rgi;
    PythonInterpreter interpreter;
    Thread scriptThread;
    ArrayList<ScriptEvent> events;
    ArrayList<PyObject> scripts;

    /**
     * Konstruktor
     * initialisiert eventliste und scriptthread
     */
    public ScriptModule(ClientCore.InnerClient inner) {

        rgi = inner;

        events = new ArrayList<ScriptEvent>();
        scripts = new ArrayList<PyObject>();

        interpreter = new PythonInterpreter();

        // Hauptschleife des ScriptModuls, läuft in eigenem Thread:
        scriptThread = new Thread(new Runnable() {

            public void run() {
                while (true) {
                    // warten, wenn keine events anstehen:
                    if (events.isEmpty()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ScriptModule.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }// verarbeiten, wenn events anstehen:
                    else {
                        // event auslösen und dann von der liste entfernen:
                        for (PyObject script : scripts) {
                            events.get(0).trigger(script);
                        }
                        events.remove(0);
                    }
                }
            }
        });
    }

    /**
     * Startet den ScriptThread, der die ScriptEreignisse verarbeitet
     */
    public void startExecution() {
        scriptThread.start();
    }

    /**
     * Löst ein Script-Ereignis aus, d.h. die entsprechende Funktion wird aufgerufen
     */
    public void raiseEvent(String name, Object args[]) {
        events.add(new ScriptEvent(name, args));
    }

    /**
     * Lädt ein Script.
     */
    public void loadScript(String theScript) {
        interpreter.exec(theScript);

        scripts.add(interpreter.get("trigger").__call__());
    }


    /**
     * Fügt eine neue Schnittstellen-Klasse hinzu.
     * Scripts können die Klasse und ihre Funktionen aufrufen, um mit dem Spiel zu interagieren.
     * @param name      Der Name, mit dem die Schnittstellenklasse im Script aufgerufen wird
     * @param interf    Die Schnittstellenklasse
     */
    public void addInterface(String name, Object interf)
    {
        interpreter.set(name, interf);
    }


}// Klassenende

