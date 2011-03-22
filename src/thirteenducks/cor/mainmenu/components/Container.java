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
package thirteenducks.cor.mainmenu.components;

import java.util.ArrayList;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.mainmenu.MainMenu;

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
    }

    /**
     * Fügt dem Container eine neue Komponente hinzu
     * @param component     die neue Komponente
     */
    public void addComponent(Component component) {
        this.components.add(component);
    }

    @Override
    public void render(Graphics g) {

        // Wenn der Container inaktiv ist wird er nicht gerendert:
        if (!active) {
            return;
        }



//        // Rahmen zeichnen:
//        g.setColor(Color.gray);
//        g.fillRect(getX1(), getY1(), getX2() - getX1(), getY2() - getY1());
//        g.setColor(Color.black);
//        g.drawLine(getX1(), getY1(), getX2(), getY1());
//        g.drawLine(getX1(), getY1(), getX1(), getY2());
//        g.drawLine(getX2(), getY1(), getX2(), getY2());
//        g.drawLine(getX1(), getY2(), getX2(), getY2());

        for (Component c : components) {
            c.render(g);
        }
    }

    @Override
    public void init(GameContainer c) {
        for (Component m : components) {
            m.init(c);
            m.setAlpha(1.0f);
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
            for (Component m : components) {
                if (m.getX1() < x && x < m.getX2() && m.getY1() < y && y < m.getY2()) {
                    m.mouseClicked(button, x, y, clickCount);
                } else {
                    m.mouseClickedAnywhere(button, x, y, clickCount);
                }
            }
        }
    }

    @Override
    public void mouseMoved(int x, int y) {
        if (active) {
            for (Component c : components) {
                c.mouseMoved(x, y);
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
            for (Component m : components) {
                m.keyPressed(key, c);
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

    @Override
    public void setAlpha(float alpha) {
        for (Component c : components) {
            c.setAlpha(alpha);
        }
    }
}
