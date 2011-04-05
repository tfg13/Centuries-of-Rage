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
package de._13ducks.cor.script;

import de._13ducks.cor.game.client.ClientCore;

/**
 * Diese Klasse bildet die Schnittstelle zwischen Scriptcode und Javacode.
 * Scripts können über die Funktionen dieser Klasse auf das Spiel zugreifen.
 * @author michael
 */
public class ScriptInterface {

    ClientCore.InnerClient rgi;
    /**
     * Konstruktor
     */
    public ScriptInterface(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    /**
     * Für Debugprints
     */
    public void print(String message) {
        System.out.println("[SCRIPTDEBUG] " + message);
    }
}// Klassenende

