/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.mainmenu.components;

import de._13ducks.cor.mainmenu.MainMenu;
import java.util.ArrayList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 *
 * @author michael
 */
public class ChatWindow extends Component {

    /**
     * Eine Liste mit allen empfangenen Chatnachrichten
     */
    private ArrayList<String> history;

    /**
     * Konstruktor
     * @param m
     * @param x
     * @param y
     */
    public ChatWindow(MainMenu m, double x, double y) {
        super(m, x, y, 45, 15);

        history = new ArrayList<String>();

        for(int i=0; i<6; i++)
        {
            history.add("history" + i);
        }
    }

    public void chatMessage(String message) {
        history.add(message);
    }

    /**
     * Render-Funktion
     * @param g - Graphics-Objekt zum Zeichnen
     */
    @Override
    public void render(Graphics g) {

        for(int i=1; i<5; i++)
        {
            g.setColor(Color.black);
            g.drawString(history.get(history.size() - i), getX1(), getY1() + (g.getFont().getLineHeight()*i));
        }
    }
}
