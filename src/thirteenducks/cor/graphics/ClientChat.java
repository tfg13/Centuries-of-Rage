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
package thirteenducks.cor.graphics;

import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.graphics.Overlay;
import java.util.ArrayList;
import org.newdawn.slick.*;

/**
 * Diese Klasse repräsentiert den Chat, mit dem Spieler chatten können, und mit dem Spielrelevante infos Angezeigt werden
 *
 * Die größe des Chatfensters wird einmalig beim Init übergeben. Der Chat passt dann seine Schriftgröße automatisch an
 *
 * @author tfg
 */
public class ClientChat  extends Overlay {

    /**
     * Der normale, nicht aktivierte Modus. Nur die aktiven Nachrichten werden angezeigt.
     */
    private static final int NORMAL_MODE = 1;
    /**
     * Der Eingabemodus. Das gesamte Chatfenster wird angezeigt, mit History. Derzeit gibt es keinen seperaten Team-Eingabemodus, da es noch keine Teams gibt
     */
    private static final int ENTER_MODE = 2;
    /**
     * Konfigurations-Parameter
     * Gibt die maximale Anzahl dargestellter Zeilen an. (Für die History-Ansicht)
     * Bestimmt die Schriftgröße mit, sollte also nicht zu hoch eingstellt werden - dafür gibts ja die historyFunktion
     */
    private final int LINES = 10;
    /**
     * Konfigurations-Parameter
     * Gibt an, wie lange eine Chatnachricht aktuell bleibt, bevor sie ausgeblendet wird
     * Gilt natürlich nicht für ENTER und HISTORY-Modes
     */
    private final int UPTIME = 20000;
    /**
     * Konfig-Parameter.
     * Hintergrundfarbe des Chatfensters.
     */
    private final Color COL_BACKGROUND = new org.newdawn.slick.Color(65, 65, 65, 160);
    /**
     * Konfig-Parameter.
     * Farbe der Grenzlinie des Chatfensters.
     */
    private final Color COL_BACKGROUND2 = Color.black;
    /**
     * Konfig-Parameter.
     * Farbe des Hintergrunds des Content- und Eingabebereichs
     */
    private final Color COL_CONTENT_AREA = new org.newdawn.slick.Color(50, 50, 50, 160);
    /**
     * Konfig-Parameter.
     *
     */
    private final Color COL_TEXT = new org.newdawn.slick.Color(255, 255, 200);
    /**
     * Wie vieldes Fensters sind NICHT echter Inhalt.
     * Bedingt unter anderem die Rundung des Chatfensters, da diese den freien Bereich bedingt
     * Bezieht sich auf die Y-Größe
     */
    private final float WINDOW_OVER_CONTENT = 0.05f;
    /**
     * Wieviel des Contents sind Trennbereicht zwischen Eingabe und History
     */
    private final float GAP_OVER_CONTENT = 0.05f;
    /**
     * Bestimmt die Schriftgröße im Verhältniss zum vorhandenen Platz
     */
    private final float FONTSIZE_OVER_LINESIZE = 0.8f;
    /**
     * Der Gesamte Inhalt des Chats. Wird eventuell irgendwann gekürzt (z.B. ab 1000 Zeilen), sollte aber schon ne Weile halten.
     */
    private ArrayList<ChatLine> chatHistory;
    /**
     * Der aktuelle Modus. Es gibt:
     * 1 (ClientChat.NORMAL_MODE) - Nur aktuelle Nachrichten, kein Inhalt
     * 2 (ClientChat.ENTER_MODE) - Eingabemodus, mit History
     */
    private int mode = ClientChat.NORMAL_MODE;
    /**
     * Die X-Größe des Chatfensters in Pixel
     */
    private int pxlX;
    /**
     * Die Y-Größe des Chatfensters in Pixel
     */
    private int pxlY;
    /**
     * Gesamter Content-Bereich
     */
    private int intPxlX;
    /**
     * Gesamter Content-Bereich
     */
    private int intPxlY;
    /**
     * History-Bereich
     */
    private int hPxlY;
    /**
     * Enter-Bereich
     */
    private int ePxlY;
    /**
     * Abstand vom Rahmen zum Content in Pixeln
     */
    private int intgap;
    /**
     * Abstand von der Eingabe zur History in Pixeln
     */
    private int intgapCH;
    /**
     * Abstand von der Schrift zu ihrem Feld. Die Schrift muss mittig sein.
     */
    private int ilgap;
    /**
     * Die Scrollposition in der History-Ansicht
     */
    private int scroll = 0;
    /**
     * Die Schrift des Chats. Die Größe wird selbstständig angepasst (beim init)
     */
    private UnicodeFont font;
    /**
     * Was derzeit eingegeben wird
     */
    private String enter = "";
    /**
     * Bei langen Nachrichten verschiebt sich die Ansicht des Chats.
     * Hier wird gespeichert wie weit
     */
    private int overSizeOffset;
    /**
     * Das Timing für den blinkenden Cursor
     */
    private long cursorBlink = System.currentTimeMillis();
    /**
     * Die Referenz auf alle Module.
     * Wird z.B. zum Abspielen von Sounds benötigt.
     */
    ClientCore.InnerClient rgi;

    /**
     * Der normale Konstruktor.
     * Der Chat muss danach noch initialisiert (ClientChat.init()) werden, bevor er verwendet werden kann, dies geschieht normalerweise durch die Init-Routine des Game-Moduls.
     * @param rgi
     */
    public ClientChat(ClientCore.InnerClient inner) {
        rgi = inner;
    }

    /**
     * Initialisiert den Chat.
     * Läd Schriften, braucht daher eventuell ein bisschen.
     * 
     * Die hier übergebene Größe des Chats darf später nichtmehr geändert werden!
     * @param sizeX Die zukünftige Größe des Chats
     * @param sizeY Die zukünftige Größe des Chats
     */
    public void init(int sizeX, int sizeY) {
        pxlX = sizeX;
        pxlY = sizeY;
        intgap = (int) (1.0 * pxlY * WINDOW_OVER_CONTENT);
        intPxlX = pxlX - (2 * intgap);
        intPxlY = pxlY - (2 * intgap);
        intgapCH = (int) (1.0 * pxlY * GAP_OVER_CONTENT);
        hPxlY = (int) (1.0 * (intPxlY - intgapCH) / LINES * (LINES - 1));
        ePxlY = (int) (1.0 * (intPxlY - intgapCH) / LINES);

        // Die Schrift initialisieren
        // Ziel-Größe bestimmen
        int fontsize = (int) (1.0 * ePxlY * FONTSIZE_OVER_LINESIZE);
        font = new org.newdawn.slick.UnicodeFont(java.awt.Font.decode("Sans-" + fontsize));
        font.addAsciiGlyphs();
        font.getEffects().add(new org.newdawn.slick.font.effects.ShadowEffect(java.awt.Color.BLACK, 1, 1, 1.0f));
        font.getEffects().add(new org.newdawn.slick.font.effects.ColorEffect(new java.awt.Color(255, 255, 200)));
        try {
            font.loadGlyphs();
        } catch (SlickException ex) {
        }
        ilgap = (ePxlY - fontsize) / 2;
        chatHistory = new ArrayList<ChatLine>();
    }

    /**
     * Liefert die Nachrichten, die derzeit angezeigt werden soll.
     * Regelt Timing für NORMAL_MODE automatisch
     * Berücksichtigt auch die eingestellte Zeilenzahl
     * @return
     */
    private ChatLine[] getMessages() {
        ChatLine[] lines = new ChatLine[LINES - 1];
        int aline = LINES - 2;
        for (int i = chatHistory.size() - 1 - scroll; i >= 0 && i > chatHistory.size() - LINES - scroll; i--) {
            // Müssen wir auf aktualität achten
            if (mode == ClientChat.NORMAL_MODE) {
                // Noch aktuell?
                ChatLine line = chatHistory.get(i);
                if (System.currentTimeMillis() - line.getMsgTime() < UPTIME) {
                    // Ist noch aktuell, also verwenden
                    lines[aline] = line;
                    aline--;
                    if (aline < 0) {
                        // Zuteilung fertig
                        return lines;
                    }
                } else {
                    // Nichtmehr aktuell, also abbrechen
                    return lines;
                }
            } else {
                // Aktualität egal
                ChatLine line = chatHistory.get(i);
                lines[aline] = line;
                aline--;
                if (aline < 0) {
                    // Zuteilung fertig
                    return lines;
                }
            }
        }
        return lines;
    }

    /**
     * Zeichnet den Chat auf den Bildschirm/Buffer (wird auf g2 gezeichnet)
     * Der Chat entscheidet selbstständig, was gezeichnet wird (nur aktives (rahmenlos), hisory, eingabemaske etc.)
     * Der Chat belegt maximal den Platz von chatX bis chatSizeX und chatY bis chatSizeY, je nach Modus aber auch weniger.
     *
     * @param g2
     * @param chatX X-Koordinate des Chatfensters (das nicht immer gezeinet wird)
     * @param chatY Y-Koordinate des Chatfensters (das nicht immer gezeinet wird)
     */
    public void renderChat(Graphics g2, int chatX, int chatY) {
        // Der Chat zeigt immer die letzen paar Zeilen an, egal in welchem Modus.
        // Daher muss erst der eventuell vorhandene Hintergrund gezeichnet werden, und dann der Text
        if (mode == ClientChat.ENTER_MODE) {
            // Fenster
            g2.setColor(COL_BACKGROUND);
            g2.fillRoundRect(chatX, chatY, pxlX, pxlY, (int) (1.0 * pxlY * WINDOW_OVER_CONTENT));
            // Rahmen
            g2.setColor(COL_BACKGROUND2);
            g2.drawRoundRect(chatX, chatY, pxlX, pxlY, (int) (1.0 * pxlY * WINDOW_OVER_CONTENT));
            // InhaltsrahmenHistory (Inhalt und Linie)
            g2.setColor(COL_CONTENT_AREA);
            g2.fillRect(chatX + intgap, chatY + intgap, intPxlX, hPxlY);
            g2.setColor(COL_BACKGROUND2);
            g2.drawRect(chatX + intgap, chatY + intgap, intPxlX, hPxlY);
            // Inhaltsrahmen Enter (Inhalt und Linie)
            g2.setColor(COL_CONTENT_AREA);
            g2.fillRect(chatX + intgap, chatY + intgap + hPxlY + intgapCH, intPxlX, ePxlY);
            g2.setColor(COL_BACKGROUND2);
            g2.drawRect(chatX + intgap, chatY + intgap + hPxlY + intgapCH, intPxlX, ePxlY);
        }
        // Jetzt die vorhandenen Nachrichten anzeigen - aber die Menge kann unterschiedlich sein, je nach dem ob eingeblendet oder nicht.
        if (mode == ClientChat.ENTER_MODE) {
            // Das Enter-Feld
            g2.setFont(font);
            g2.setColor(COL_TEXT);
            if (enter != null) {
                if (overSizeOffset <= 0) {
                    g2.drawString(enter, chatX + intgap + 3, chatY + intgap + hPxlY + intgapCH + ilgap);
                } else {
                    // Zu lang, was weglassen
                    g2.drawString(enter.substring(overSizeOffset), chatX + intgap + 1, chatY + intgap + hPxlY + intgapCH + ilgap);
                }
            }
            // Cursor
            int cpos = 2;
            if (enter != null) {
                if (font.getWidth(enter) < intPxlX) {
                    cpos = font.getWidth(enter) + 2;
                } else {
                    // Cursor ans Ende setzen
                    cpos = intPxlX - 5;
                }
            }
            // Blinken des Cursors abhandeln
            if (System.currentTimeMillis() - cursorBlink <= 375) {
                g2.drawLine(chatX + intgap + cpos, chatY + intgap + hPxlY + intgapCH + ilgap + 1, chatX + intgap + cpos, chatY + intgap + hPxlY + intgapCH + ilgap + (int) (1.0 * ePxlY * FONTSIZE_OVER_LINESIZE));
            } else if (System.currentTimeMillis() - cursorBlink > 750) {
                cursorBlink = System.currentTimeMillis();
            }
        }
        // Nachrichten rendern
        g2.setFont(font);
        ChatLine[] content = getMessages();
        for (int i = 0; i < content.length; i++) {
            ChatLine line = content[i];
            if (line != null) {
                // Rendern
                // Pre
                if (line.renderPre()) {
                    g2.setColor(line.getPremessageColor());
                    g2.drawString(line.getPremessage(), chatX + intgap + 3, chatY + intgap + (ePxlY * i) + ilgap);
                }
                // Content
                g2.setColor(line.getMessageColor());
                g2.drawString(":  " + line.getMessage(), chatX + intgap + 10 + font.getWidth(line.getPremessage()), chatY + intgap + (ePxlY * i) + ilgap);
            }
        }

    }

    /**
     * Aufrufen, um den Chat zu aktivieren/deaktivieren
     * Sollte aufgerufen werden, wenn der Chat-Button gedrückt wird
     * Der Chat schält selbstständig den Chat-Inputmode für das Inputmodul um.
     */
    public void toggleChat() {
        if (mode == ClientChat.NORMAL_MODE) {
            // Modus umschalten
            mode = ClientChat.ENTER_MODE;
            rgi.rogGraphics.inputM.chatMode = true;
        } else {
            mode = ClientChat.NORMAL_MODE;
            rgi.rogGraphics.inputM.chatMode = false;
        }
    }

    /**
     * Hiermit lassen sich Inputsignale an das Modul weiterleiten
     * ENTER als Bestätigen/Abschicken wird automatisch gefangen, damit wird der Inputmode auch automatisch beendet.
     * PFEILTASTEN HOCH und RUNTER werden automatisch zum Scrollen verwendet.
     * Wenn andere Tasten wie z.B. ESCAPE als Abbruch-Taste verwendet werden sollen, muss das vorher abgefangen werden.
     * @param key
     * @param c
     */
    public void input(int key, char c) {
        if (key == Input.KEY_ENTER || key == Input.KEY_NUMPADENTER) {
            // Nachricht abschicken
            rgi.netctrl.broadcastString(enter, (byte) 44);
            // Zurücksetzen
            enter = "";
            overSizeOffset = 0;
            // Eingabemodus abschalten
            toggleChat();
        } else if (key == Input.KEY_BACK) {
            // Letztes Löschen, falls da
            if (enter.length() > 1) {
                enter = enter.substring(0, enter.length() - 1);
                recalcOSO(false);
            } else {
                enter = "";
                overSizeOffset = 0;
            }
        } else if (key == Input.KEY_UP) {
            // Hochscrollen, falls möglich
            int maxScroll = chatHistory.size() - LINES;
            if (maxScroll > scroll) {
                scroll++;
            }
        } else if (key == Input.KEY_DOWN) {
            // Runterscrollen, falls möglich
            if (scroll > 0) {
                scroll--;
            }
        } else {
            if (!Character.isISOControl(c)) {
                enter = enter + c;
                recalcOSO(true);
            }
        }
    }

    /**
     * Berechnet die Verschiebung der Chateingabe für lange Texte neu
     */
    private void recalcOSO(boolean added) {
        if (font.getWidth(enter) > intPxlX) {
            if (added) {
                int i = overSizeOffset;
                    while (font.getWidth(enter.substring(i++)) > intPxlX) {}
                overSizeOffset = i - 1;
            } else {
                int i = overSizeOffset;
                    while (font.getWidth(enter.substring(i--)) < intPxlX) {}
                overSizeOffset = i + 2;
            }
        }
    }

    /**
     * Fügt eine Nachricht zum Chat hinzu.
     * Neu ankommende Nachrichten sollten hiermit zum Chat geadded werden
     * Bereits hier wird die Nachricht umgebrochen, sofern das erforderlich sein sollte
     * @param msg Die Nachricht selber
     * @param playerId Die PlayerId der Herkunft. < 0 ist der Server
     */
    public void addMessage(String msg, int playerId) {
        ChatLine line = null;
        if (playerId > 0) {
            String playername = rgi.game.getPlayer(playerId).nickName;
            Color playerCol = rgi.game.getPlayer(playerId).color;
            line = new ChatLine(msg, playername, playerCol);
        } else if (playerId == -1) {
            line = new ChatLine(msg, "(Server)", this.COL_TEXT);
        } else if (playerId == -2) {
            line = new ChatLine(msg, "(Game)", this.COL_TEXT);
        }
        if (!needsNL(line)) {
            // Ist so ok
            chatHistory.add(line);
            // Aktuelle Scrollposition soll behalten werden, also um 1 erhöhen
            if (scroll > 0) {
                scroll++;
            }
        } else {
            // Muss umgebrochen werden - also mach mer das
            ChatLine[] splitted = splitLine(line);
            // Alle adden
            for (ChatLine lline : splitted) {
                chatHistory.add(lline);
                if (scroll > 0) {
                    scroll++;
                }
            }
        }
    }

    /**
     * Untersucht, ob ein Umbrechen des Textes notwendig ist
     * @param line
     */
    private boolean needsNL(ChatLine line) {
        return (font.getWidth("i    " + line.getPremessage() + ":  " + line.getMessage()) > intPxlX);
    }

    /**
     * Bricht den Text um.
     * Liefert die Nachricht als mehrere Umgebrochene Zeilen zurück
     * Versucht schön umzubrechen - also zwischen Wörtern -
     * Notfalls wird aber auch im Wort unterbrochen
     *
     * Bricht auch mehrfach um, sollte das möglich sein
     * @param line
     * @return
     */
    private ChatLine[] splitLine(ChatLine line) {
        // Wir gehen Wort für Wort durch und schauen nach welchem wir die maximale Länge überschreiten.
        // Sollte es schon nach dem ersten Wort der Zeile sein, wird dieses halt zerschnitten
        String total = line.getMessage();
        int max = intPxlX - 15 - font.getWidth("i    " + line.getPremessage() + ":  i");
        // String in Wörter splitten
        ArrayList<String> words = new ArrayList<String>();
        while (total.contains(" ")) { // Solange noch leerzeichen, also neue Wörter da sind
            String word = total.substring(0, total.indexOf(" ") + 1);
            words.add(word);
            total = total.substring(total.indexOf(" ") + 1);
        }
        // Den Rest auch adden
        words.add(total);
        // Wortliste fertig. Jetzt erste Zeile für Zeile zusammenbauen, bis das Maximum erreicht ist
        ArrayList<ChatLine> lines = new ArrayList<ChatLine>();
        String wline = "";
        String tryline = "";
        while (!words.isEmpty()) { // Solange noch Wörter da sind
            tryline = tryline + words.get(0);
            if (font.getWidth(tryline) < max) {
                // Es war ok dieses Wort zu adden
                words.remove(0);
                wline = tryline;
            } else {
                // Zu lang
                if (wline.equals("")) {
                    // Oje, nichtmal ein Wort hat reingepasst. Das Wort muss also gesplittet werden
                    // Bis zu welchem Index würdes denn passen
                    int i = 1;
                    while (font.getWidth(words.get(0).substring(0, i++)) < max) {
                    }
                    i--;
                    // Bis i passt es, das als gerade getestet Zeile einstellen und den rest vom Wort dalassen
                    wline = words.get(0).substring(0, i) + "-";
                    String complete = words.remove(0);
                    words.add(0, complete.substring(i, complete.length() - 1));
                }
                // Zeile ist so fertig, also adden
                if (lines.isEmpty()) { // Die erste soll den Namen zeigen, die andern nichtmehr
                    lines.add(new ChatLine(wline, line.getPremessage(), line.getPremessageColor()));
                } else {
                    lines.add(new ChatLine(wline, line.getPremessage(), line.getPremessageColor(), true));
                }
                // Zurück auf Anfang
                wline = "";
                tryline = "";
            }
        }
        // Fertig das was da ist noch als Zeile raushauen
        lines.add(new ChatLine(tryline, line.getPremessage(), line.getPremessageColor(), true));
        // Fertig mit Splitten, Array zurückliefern
        return lines.toArray(new ChatLine[1]);
    }

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY, int hudX) {
        renderChat(g, 10, (int) (0.65 * fullResY));
    }

    public UnicodeFont getFont() {
        return font;
    }
}
