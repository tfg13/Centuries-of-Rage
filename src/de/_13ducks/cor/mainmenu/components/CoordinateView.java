/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.mainmenu.components;

import de._13ducks.cor.graphics.Renderer;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.mainmenu.MainMenu;

/**
 * Zeigt die Koordinaten der Maus an, nur zum Testen
 * @author michael
 */
public class CoordinateView extends Component {

    /**
     * Koordinaten der Maus
     */
    int x, y;
    /**
     * MainMenu-Referenz:
     */
    MainMenu m;

    /**
     * Konstruktor
     * @param m - Hauptmenü-Refrernz
     */
    public CoordinateView(MainMenu m) {
        super(m, 5, 25, 10, 10);
        this.m = m;
    }

    @Override
    public void mouseMoved(int xt, int yt) {
        x = xt;
        y = yt;
    }

    @Override
    public void render(Graphics g) {
        float rx, ry;

        rx = ((float) x / m.getWidth());
        ry = ((float) y / m.getHeight());

        Renderer.stopCaching();
        g.drawString(rx + "|" + ry, 5, 25);
    }
}
