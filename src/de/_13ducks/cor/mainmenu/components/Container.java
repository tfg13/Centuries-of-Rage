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

import java.util.ArrayList;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.mainmenu.MainMenu;

/**
 * Ein Container, der mehrere andere Components beherbergt
 *
 * @author michael
 */
public class Container extends Component {

    /**
     * Liste der Komponenten dieses Containers
     */
    private ArrayList<Component> components;
    /**
     * Gibt an, ob der Container aktiv ist, d.h. gerendert wird und Ereignisse weiterleitet
     */
    private boolean active;
    /**
     * Die Zeit, in der ein Container ein- oder ausgeblendet wird
     */
    static long fadeTime = 800;

    /**
     * Konstruktor
     *
     * @param m       MainMenu-Referenz
     * @param X       X-Koordinate
     * @param Y       Y-Koordiante
     * @param width   Breite
     * @param heigth  Höhe
     */
    public Container(MainMenu m, double X, double Y, double width, double heigth) {
        super(m, X, Y, width, heigth);
        components = new ArrayList<Component>();
        active = true;
    }

    /**
     * Setzt die MainMenu.Referenz nachträglich, wenn sie nicht im Konstruktor übergeben wurde
     * @param m - MainMenu-Referenz
     */
    public void setMainMenuReference(MainMenu m) {
        super.setMainMenu(m);
    }

    /**
     * Fügt dem Container eine neue Komponente hinzu
     * @param component     die neue Komponente
     */
    public void addComponent(Component component) {
        this.components.add(component);
    }

    /**
     * entfernt eine Komponente zur Laufzeit
     * @param component - die zu entfernende Komponente
     */
    public void removeComponent(Component component) {
        active = false;

        try {
            components.remove(component);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to remove Component! Errormessage: " + ex.getMessage());
        }

        active = true;
    }

    /**
     * Gibt die Komponentenliste dieses Containers zurück
     */
    public ArrayList<Component> getComponents() {
        return this.components;
    }

    /**
     * Gibt die Komponente mit dem entsprechenden Namen zurück, WENN es eine gibt
     * @param name - der Name der gesuchten Komponente
     * @return - die Komponente, WENN eine mit dem Namen gefunden wurde
     */
    public Component getComponent(String name) {
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).getName().equals(name)) {
                return components.get(i);
            }
        }
        System.out.print("keine Komponente mit Namen <" + name + "> gefunden.");
        return null;
    }

    @Override
    public void render(Graphics g) {

        // Wenn der Container inaktiv ist wird er nicht gerendert:
        if (!active) {
            return;
        }
        for (int i = 0; i < components.size(); i++) {
            components.get(i).render(g);
        }


    }

    /**
     * Wird aufgerufen, wenn diese Komponente angeklickt wurde
     * @param button
     * @param x
     * @param y
     * @param clickCount
     */
    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        if (active) {
            for (int i = 0; i < components.size(); i++) {
                components.get(i).generalMouseClick(button, x, y, clickCount);
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (active) {
            for (int i = 0; i < components.size(); i++) {
                components.get(i).mouseMoved(x, y);
            }
        }
    }

    /**
     * Wird bei Tastendruck aufgerufen
     * @param key   der Code der gedrückten Taste
     * @param c     der entsprechende char
     */
    @Override
    public void keyPressed(int key, char c) {
        if (active) {
            for (int i = 0; i < components.size(); i++) {
                components.get(i).keyPressed(key, c);
            }
        }
    }

    @Override
    /**
     * Wird gerufen, wenn das Mausrad gescrollt wird
     */
    public void mouseWheelMoved(int x) {
        if (active) {
            for (int i = 0; i < components.size(); i++) {
                components.get(i).mouseWheelMoved(x);
            }
        }
    }

    /**
     * Lässt den Container einblenden
     */
    public void fadeIn() {
        active = true;

    }

    /**
     * Lässt den Container ausblenden
     */
    public void fadeOut() {
        active = false;
    }
}
