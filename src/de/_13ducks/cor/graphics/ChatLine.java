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

package de._13ducks.cor.graphics;

import org.newdawn.slick.*;

/**
 * Eine Element im Chat.
 * Normalerweise ist dies eine Zeile, lange Text können aber vom Chatfenster automatisch umgebrochen werden
 *
 * @author tfg
 */
public class ChatLine {

    private final Color DEFAULT_COLOR = new Color(255, 255, 200);

    /**
     * Die eigentliche Chatnachricht
     */
    private String message;

    /**
     * Das VOR der Nachricht, in aller Regel die Herkunft der message, also z.B. der Spieler, der das geschrieben hat
     */
    private String premessage;

    /**
     * Farbe der Nachricht, normalerweise leer, dann die default-Farbe des Chats.
     */
    private Color messageColor;

    /**
     * Farbe des Textes vor der Nachricht, normalerweise die Spielerfarbe
     */
    private Color premessageColor;

    /**
     * Zeitpunkt, zu dem die Nachricht gepostet wurde.
     * Der Chat entscheidet, wie lange Nachrichten eingeblendet bleiben.
     */
    private long msgTime;

    /**
     * Bei automatisch umgebrochenen Nachrichten wird die Herkunft nicht mehrfach angezeigt.
     * Ist dies ein 2. (3. 4....) Teil?
     */
    private boolean continued = false;

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the premessage
     */
    public String getPremessage() {
        return premessage;
    }

    /**
     * @return the messageColor
     */
    public Color getMessageColor() {
        return messageColor;
    }

    /**
     * @return the premessageColor
     */
    public Color getPremessageColor() {
        return premessageColor;
    }

    /**
     * @return the msgTime
     */
    public long getMsgTime() {
        return msgTime;
    }

    /**
     * @return !continued
     */
    public boolean renderPre() {
        return !continued;
    }

    public ChatLine(String msg, String preMsg, Color preCol) {
        message = msg;
        premessage = preMsg;
        messageColor = DEFAULT_COLOR;
        premessageColor = preCol;
        msgTime = System.currentTimeMillis();
    }

    public ChatLine(String msg, String preMsg, Color preCol, boolean cont) {
        message = msg;
        premessage = preMsg;
        messageColor = DEFAULT_COLOR;
        premessageColor = preCol;
        msgTime = System.currentTimeMillis();
        continued = cont;
    }

}
