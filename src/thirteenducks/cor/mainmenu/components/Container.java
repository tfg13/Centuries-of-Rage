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
import org.newdawn.slick.Color;
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
     * Gibt an, ob die Komponente aktiv ist
     * nur aktive Komponenten werden gerendert und erhalten Input
     */
    private boolean active;
    /**
     * Die Transparenz des Containers, für ein/ausblenden
     */
    private float alpha;
    /**
     * Die Veränderung des Alpha-Wertes
     */
    private float deltaAlpha;
    /**
     * Die Zeit, in der ein Container ein- oder ausgeblendet wird
     */
    static long fadeTime = 800;
    /**
     * Der Startzeitpunkt der Fade-Animation
     */
    private long fadeStartTime;
    /**
     * Die Zeit der letzten Änderung an alpha
     */
    private long lastAlphaChange;

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
        alpha = 1.0f;
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

        // wenn deltaalpha nicht null ist sind wir in der animation
        // bei fade-out wird sofort begonnen, bei fade in erst nach der fade zeit
        if (deltaAlpha != 0) {

            int numsteps = (int) ((System.currentTimeMillis() - lastAlphaChange));
            lastAlphaChange = System.currentTimeMillis();
            for (int steps = 0; steps < numsteps; steps++) {

                if (deltaAlpha < 0) {
                    alpha += deltaAlpha;

                } else if (deltaAlpha > 0) {
                    if ((System.currentTimeMillis() - fadeStartTime) > fadeTime) {
                        alpha += deltaAlpha;
                    }
                }
            }
            // neues alpha an die komponenten weitergeben
            for (Component c : components) {
                c.setAlpha(alpha);
            }

            // wenn ganz transparent oder ganz sichtbar sind wir fertig:
            if (alpha <= 0 && deltaAlpha < 0 || alpha >= 1 && deltaAlpha > 0) {
                deltaAlpha = 0;
                System.out.print("deactivated");
            }
        }

        // Rahmen zeichnen:
        g.setColor(Color.gray);
        g.fillRect(getX1(), getY1(), getX2() - getX1(), getY2() - getY1());
        g.setColor(Color.black);
        g.drawLine(getX1(), getY1(), getX2(), getY1());
        g.drawLine(getX1(), getY1(), getX1(), getY2());
        g.drawLine(getX2(), getY1(), getX2(), getY2());
        g.drawLine(getX1(), getY2(), getX2(), getY2());

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
        for (Component m : components) {
            if (m.getX1() < x && x < m.getX2() && m.getY1() < y && y < m.getY2()) {
                m.mouseClicked(button, x, y, clickCount);
            }
        }
    }

    /**
     * Wird gerufen, wenn die Maus anfängt oder aufhört zu Hovern
     * @param x     - die X-Position der Maus
     * @param y     - die Y-Position der Maus
     */
    @Override
    public void mouseHoverChanged(boolean newstate) {
        for (Component m : components) {
            m.mouseHoverChanged(newstate);
        }
    }

    /**
     * Wird bei Tastendruck aufgerufen
     * @param key   der Code der gedrückten Taste
     * @param c     der entsprechende char
     */
    @Override
    public void keyPressed(int key, char c) {
        for (Component m : components) {
            m.keyPressed(key, c);
        }
    }

    /**
     * Lässt den Container einblenden
     */
    public void fadeIn() {

        deltaAlpha = (1.0f / fadeTime);
        lastAlphaChange = System.currentTimeMillis();
        fadeStartTime = System.currentTimeMillis();
    }

    /**
     * Lässt den Container ausblenden
     */
    public void fadeOut() {
        deltaAlpha = -(1.0f / fadeTime);
        fadeStartTime = System.currentTimeMillis();
        lastAlphaChange = System.currentTimeMillis();
    }

    @Override
    public void setAlpha(float alpha) {
        for (Component c : components) {
            c.setAlpha(alpha);
        }
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
