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
package de._13ducks.cor.mainmenu.components;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.mainmenu.MainMenu;

/**
 * Ein Texteingabefeld
 *
 * @TODO: gescheites Design, Hintergrundgrafik etc....
 * @TODO: eventuell dynamische Größe (oder zumindest Länge) unterstützen
 *
 * @note: Diese Textbox reagiert auf jeden Tastendruck. Dies ist beabsichtigt, dann muss man sie nicht erst anklicken.
 * 
 * @author michael
 */
public class TextBox extends Component {

    /**
     * Der Text der Textbox
     */
    private String text;
    /**
     * Gibt an, ob die Textbox aktiv ist
     */
    private boolean active;

    /**
     * Konstruktor
     * @param mainMenuReference     Referenz auf das MainMenu
     * @param x                     X-Position der TextBox
     * @param y                     Y-Position der TextBox
     */
    public TextBox(MainMenu mainMenuReference, double x, double y) {
        super(mainMenuReference, x, y, 30, 4);
        text = "";
        active = false;
    }

    @Override
    public void render(Graphics g) {
        // Kasten mit Hintergrundfarbe füllen:
        if (this.active) {
            g.setColor(Color.lightGray);
        } else {
            g.setColor(Color.gray);
        }
        g.fillRect(getX1(), getY1(), this.getWidth(), this.getHeight());


        // Rahmen zeichnen:
        g.setColor(Color.black);
        g.drawLine(getX1(), getY1(), getX2(), getY1());
        g.drawLine(getX2(), getY1(), getX2(), getY2());
        g.drawLine(getX1(), getY2(), getX2(), getY2());
        g.drawLine(getX1(), getY1(), getX1(), getY2());

        // Text zeichnen:
        g.drawString(getText(), getX1() + 5, getY1() + 5);
    }

    @Override
    /**
     * Wenn die Textbox gerade aktiv ist wird der Tastendruck verarbeitet
     */
    public void keyPressed(int key, char c) {
        if (active) {
            // Bei return(14) das letzte Zeichen entfernen, ansonsten den entsprechenden Buchstaben dranhängen:
            if (key != 14) {
                setText(getText() + c);
            } else {
                if (getText().length() > 0) {
                    setText(getText().substring(0, getText().length() - 1));
                }
            }
        }
    }

    @Override
    /**
     * Wenn die TextBox angeklickt wird ist sie aktiv
     */
    public void mouseClicked(int button, int x, int y, int clickCount) {
        active = true;
    }

    @Override
    /**
     * Wenn die MAus irgendwo hinklickt wird die TextBox deaktiviert
     */
    public void mouseClickedAnywhere(int button, int x, int y, int clickCount) {
        active = false;
    }

    /**
     * Getter für Text
     * @return - der Text der in der Textbox steht
     */
    public String getText() {
        return text;
    }

    /**
     * Setter für Text
     * @param text - der neue Text für die TextBox
     */
    public void setText(String text) {
        this.text = text;
    }
}
