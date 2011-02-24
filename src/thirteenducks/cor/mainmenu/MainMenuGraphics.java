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
package thirteenducks.cor.mainmenu;

import thirteenducks.cor.mainmenu.components.*;
import java.util.ArrayList;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;

/**
 * GrafikInhalt des Hauptmenüs
 * 
 * Verwaltet das Zeichnen der Komponenten und leitet Mausklicks und Tastendrücke weiter
 *
 * @author michael
 */
public class MainMenuGraphics extends BasicGame {

    /**
     * Referenz auf das MainMenu
     */
    MainMenu mainMenu;
    /**
     * Liste der Grafikkomponenten
     */
    ArrayList<Component> components;

    /**
     * Konstruktor
     */
    public MainMenuGraphics() {
        super("COR Mainmenu");
        components = new ArrayList<Component>();
    }

    /**
     * Setzt die MainMenu-Referenz
     * @param smm - Referenz auf MainMenu
     */
    public void setMainMenuReference(MainMenu smm) {
        mainMenu = smm;
    }

    @Override
    /**
     * Wird aufgerufen bevor die Grafik mit dem Rendern anfängt
     * Initialisiert die Listener für die Benutzereingabe
     */
    public void init(GameContainer container) throws SlickException {

        /**
         * Listener für Maus:
         */
        container.getInput().addMouseListener(new MouseListener() {

            public void mouseWheelMoved(int change) {
            }

            public void mouseClicked(int button, int x, int y, int clickCount) {
                for (Component c : components) {
                    c.mouseClickedAnywhere(button, x, y, clickCount);
                }
            }

            public void mousePressed(int button, int x, int y) {
            }

            public void mouseReleased(int button, int x, int y) {
            }

            public void mouseMoved(int oldx, int oldy, int newx, int newy) {
                for (Component c : components) {
                    c.mouseMoved(newx, newy);
                }
            }

            public void mouseDragged(int arg0, int arg1, int arg2, int arg3) {
            }

            public void setInput(Input input) {
            }

            public boolean isAcceptingInput() {
                return true;
            }

            public void inputEnded() {
            }

            public void inputStarted() {
            }
        });

        /**
         * Listener für Tastatur:
         */
        container.getInput().addKeyListener(new KeyListener() {

            public void keyPressed(int key, char c) {

                /**
                 * Beenden, wenn F10 oder ESC gedrückt wird:
                 */
                if (key == 68 || key == 1) {
                    System.exit(0);
                }

                /**
                 * Tastendruck an alle Components weiterleiten:
                 */
                for (Component comp : components) {
                    comp.keyPressed(key, c);
                }
            }

            public void keyReleased(int key, char c) {
            }

            public void setInput(Input input) {
            }

            public boolean isAcceptingInput() {
                return true;
            }

            public void inputEnded() {
            }

            public void inputStarted() {
            }
        });


        /**
         * Komponenten initialisieren:
         */
        for (Component c : components) {
            c.init(container);
        }


    }

    @Override
    /**
     * Update-Funktion
     * - wird nicht benötigt -
     */
    public void update(GameContainer container, int delta) throws SlickException {
    }

    /**
     * Render-Funktion
     * ruft die render()-Funktion aller MainMenuComponents in der Liste auf
     *
     * @param container         Container
     * @param g                 Grafik-Objekt
     * @throws SlickException   
     */
    public void render(GameContainer container, Graphics g) throws SlickException {
        for (Component c : components) {
            c.render(g);

        }
    }

    /**
     * Fügt der Liste eine neue Komponente hinzu
     * @param c - die neue Komponente
     */
    public void addComponent(Component c) {
        this.components.add(c);
    }
}
