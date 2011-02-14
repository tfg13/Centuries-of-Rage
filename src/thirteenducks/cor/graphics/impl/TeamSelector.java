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
package thirteenducks.cor.graphics.impl;

import java.util.Map;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.NetPlayer;
import thirteenducks.cor.graphics.input.CoRInputMode;
import thirteenducks.cor.graphics.Overlay;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Polygon;
import thirteenducks.cor.graphics.GraphicsImage;

/**
 * Der Team-Selektor.
 * Erscheint/Verschwindet auf T
 * Ermöglicht das Wählen/Verändern von Teams im laufenden Spiel (sofern diese nicht gesperrt wurden!)
 *
 * @author tfg
 */
public class TeamSelector extends Overlay {

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
     * Wie vieldes Fensters sind NICHT echter Inhalt.
     * Bedingt unter anderem die Rundung des Chatfensters, da diese den freien Bereich bedingt
     * Bezieht sich auf die Y-GesamtGröße
     */
    private final float WINDOW_OVER_CONTENT = 0.015f;
    /**
     * Konfig-Parameter.
     *
     */
    private final Color COL_TEXT = new org.newdawn.slick.Color(255, 255, 200);
    /**
     * Die verwendete Schriftgröße. Die Größe wird selbstständig angepasst (beim init)
     */
    private UnicodeFont font;
    private int oriX;
    private int oriY;
    private int contentSizeX;
    private int contentSizeY;
    private int seqY;
    private int gap;
    public boolean active = false;
    private ClientCore.InnerClient rgi;
    // Muss am Anfang einmalig berechnet werden.
    int players = 0;

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY, Map<String, GraphicsImage> imgMap) {
        if (active) {
            // Einmalig die Spielerzahl berechnen
            if (players == 0) {
                for (int i = 1; i < rgi.game.playerList.size(); i++) {
                    if (rgi.game.playerList.get(i).nickName.equals("")) {
                        break;
                    } else {
                        players++;
                    }
                }
                // Dein eigenen nicht
                contentSizeX = (int) (0.90 * fullResX);
                gap = (int) (WINDOW_OVER_CONTENT * fullResY);
                seqY = font.getLineHeight();
                contentSizeY = (players - 1) * seqY;
                oriX = (fullResX / 2) - ((contentSizeX + 2 * gap) / 2);
                oriY = (fullResY / 2) - ((contentSizeY + 3 * gap + seqY) / 2);

            }

            // Großer Kasten mit Rahmen
            g.setColor(COL_BACKGROUND);
            g.fillRoundRect(oriX, oriY, contentSizeX + 2 * gap, contentSizeY + 3 * gap + seqY, gap);
            g.setColor(COL_BACKGROUND2);
            g.drawRoundRect(oriX, oriY, contentSizeX + 2 * gap, contentSizeY + 3 * gap + seqY, gap);

            // Obere Box
            g.setColor(COL_CONTENT_AREA);
            g.fillRect(oriX + gap, oriY + gap, contentSizeX, seqY);
            g.setColor(COL_BACKGROUND2);
            g.drawRect(oriX + gap, oriY + gap, contentSizeX, seqY);

            // Untere Box
            g.setColor(COL_CONTENT_AREA);
            g.fillRect(oriX + gap, oriY + seqY + 2 * gap, contentSizeX, contentSizeY);
            g.setColor(COL_BACKGROUND2);
            g.drawRect(oriX + gap, oriY + seqY + 2 * gap, contentSizeX, contentSizeY);

            // Inhalt des Titels
            g.setColor(COL_TEXT);
            g.setFont(font);
            g.drawString("ALLIES", oriX + gap + ((contentSizeX / 2) - (font.getWidth("ALLIES")) / 2), oriY + gap);

            // Spieler
            int index = 0;

            for (int i = 1; i <= players; i++) {
                if (i == rgi.game.getOwnPlayer().playerId) {
                    continue;
                } else {
                    index++;
                }
                NetPlayer player = rgi.game.playerList.get(i);
                // Farb-Kasten
                g.setColor(player.color);
                g.fillRect(oriX + gap + (0.1f * seqY), ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY), (0.8f * seqY), (0.8f * seqY));
                // Name, ungekürzt (erstmal)
                g.setColor(COL_TEXT);
                g.drawString(player.nickName, oriX + gap + 15 + (0.8f * seqY), ((index - 1) * seqY) + oriY + seqY + 2 * gap);
                // Ally-Kasten
                g.drawRect((float) (oriX + gap + contentSizeX - (0.4f * seqY) - (1.6 * seqY)), ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY), (0.8f * seqY), (0.8f * seqY));
                // Vis-Kasten
                g.drawRect((float) (oriX + gap + contentSizeX - (0.1f * seqY) - (0.8 * seqY)), ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY), (0.8f * seqY), (0.8f * seqY));
                // Status eintragen:
                if (rgi.game.areAllies(rgi.game.getOwnPlayer(), player)) {
                    g.fillRect((float) (oriX + gap + contentSizeX - (0.4f * seqY) - (1.6 * seqY)) + 2, ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY) + 2, (0.8f * seqY) - 3, (0.8f * seqY) - 3);
                } else if (rgi.game.wasInvited(player)) {
                    // Halb
                    Polygon poly = new Polygon();
                    poly.addPoint((float) (oriX + gap + contentSizeX - (0.4f * seqY) - (1.6 * seqY)) + 2 + ((0.8f * seqY) - 3), ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY) + 2 + ((0.8f * seqY) - 3));
                    poly.addPoint((float) (oriX + gap + contentSizeX - (0.4f * seqY) - (1.6 * seqY)) + 2, ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY) + 2);
                    poly.addPoint((float) (oriX + gap + contentSizeX - (0.4f * seqY) - (1.6 * seqY)) + 2, ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY) + 2 + ((0.8f * seqY) - 3));
                    poly.addPoint((float) (oriX + gap + contentSizeX - (0.4f * seqY) - (1.6 * seqY)) + 2 + ((0.8f * seqY) - 3), ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY) + 2 + ((0.8f * seqY) - 3));
                    g.fill(poly);
                }
                if (rgi.game.shareSight(rgi.game.getOwnPlayer(), player)) {
                    g.fillRect((float) (oriX + gap + contentSizeX - (0.1f * seqY) - (0.8 * seqY)) + 2, ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY) + 2, (0.8f * seqY) - 3, (0.8f * seqY) - 3);
                }
            }

        }
    }

    public void setFont(UnicodeFont f) {
        font = f;
    }

    public void toggle() {
        active = !active;
        if (active) {
            // AN?
            rgi.rogGraphics.inputM.addAndReplaceSpecialMode(new CoRInputMode(false) {

                @Override
                public void mouseMoved(int oldx, int oldy, int newx, int newy) {
                }

                @Override
                public void mouseKlicked(int button, int x, int y, int clickCount) {
                    if (button == 0) {
                        // Herausfinden, ob was von Bedeutung angeklickt wurde (ein Kasten im Team-Fenster)
                        checkKlick(x, y);
                    } else {
                        // Abbrechen
                        toggle();
                    }
                }

                @Override
                public void startMode() {
                }

                @Override
                public void endMode() {
                    if (active) {
                        //active = false;
                    }
                }
            });
        } else {
            // AUS:
            rgi.rogGraphics.inputM.removeSpecialMode();
        }
    }

    /**
     * Wertet klicks ins Fenster aus
     * @param x
     * @param y
     */
    private void checkKlick(int x, int y) {
        if (active) {
            int index = 0;
            for (int i = 1; i <= players; i++) {
                if (i == rgi.game.getOwnPlayer().playerId) {
                    continue;
                } else {
                    index++;
                }
                NetPlayer player = rgi.game.getPlayer(i);
                // Ally?
                int x1ally = (int) (oriX + gap + contentSizeX - (0.4f * seqY) - (1.6 * seqY));
                int y1ally = (int) (((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY));
                int x2ally = (int) (x1ally + (0.8f * seqY));
                int y2ally = (int) (y1ally + (0.8f * seqY));
                // Vis-Kasten?
                int x1vis = (int) (oriX + gap + contentSizeX - (0.1f * seqY) - (0.8 * seqY));
                int y1vis = (int) ((int) ((index - 1) * seqY) + oriY + seqY + 2 * gap + (0.1f * seqY));
                int x2vis = (int) (x1vis + (0.8f * seqY));
                int y2vis = (int) (y1vis + (0.8f * seqY));

                if (x > x1ally && x < x2ally && y > y1ally && y < y2ally) {
                    // Ally setzen
                    // Es geht nur, wenn wirs nicht selber sind
                    if (!player.equals(rgi.game.getOwnPlayer())) {
                        // Will man setzen oder un-setzen?
                        boolean activAlly = !rgi.game.areAllies(player, rgi.game.getOwnPlayer());
                        if (activAlly) {
                            // Team-Setzen muss erst bestätigt werden. Ist dies bereits die Antwort auf die Bestätigung?
                            if (rgi.game.wasInvited(player)) {
                                // Ja, jetzt setzen
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 51, rgi.game.getOwnPlayer().playerId, player.playerId, 2, 0));
                            } else {
                                // Anfragen
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 51, rgi.game.getOwnPlayer().playerId, player.playerId, 1, 0));
                                rgi.chat.addMessage("You invited " + player.nickName, -2);
                            }
                        } else {
                            // Ally/Anfrage löschen
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 51, rgi.game.getOwnPlayer().playerId, player.playerId, 4, 0));
                        }
                    }
                } else if (x > x1vis && x < x2vis && y > y1vis && y < y2vis) {
                    // Vis setzen
                    if (!player.equals(rgi.game.getOwnPlayer())) {
                        // Setzen oder löschen?
                        if (rgi.game.shareSight(player, rgi.game.getOwnPlayer())) {
                            // Löschen
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 51, rgi.game.getOwnPlayer().playerId, player.playerId, 5, 0));
                        } else {
                            // Vis lässt sich nur setzen, wenn wir verbündet sind.
                            if (rgi.game.areAllies(player, rgi.game.getOwnPlayer())) {
                                // Sicht setzen
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 51, rgi.game.getOwnPlayer().playerId, player.playerId, 3, 0));
                            }
                        }
                    }
                }
            }
        }
    }

    public TeamSelector(ClientCore.InnerClient inner) {
        rgi = inner;
    }
}
