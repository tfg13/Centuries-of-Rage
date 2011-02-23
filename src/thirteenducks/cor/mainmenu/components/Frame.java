/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.mainmenu.components;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.mainmenu.MainMenu;

/**
 * Der Rahmen um den Ganzen Bildschirm
 *
 * @author michael
 */
public class Frame extends Component {

    /**
     * Konstruktor
     * @param m     Hauptmenü-Referenz
     */
    public Frame(MainMenu m) {
        super(m, 0, 0, m.getWidth(), m.getHeight());
    }

    /**
     * Redner-Funktion
     *
     * Zeichnet den Rahmen
     */
    @Override
    public void render(Graphics g) {


        // 1. Rahmen:
        g.setColor(Color.black);

        g.drawLine(0, 0, getMainMenu().getWidth(),0);
        g.drawLine(0, 0, 0,getMainMenu().getHeight());
        g.drawLine(getMainMenu().getWidth(), 0, getMainMenu().getWidth(),getMainMenu().getHeight());
        g.drawLine(0, getMainMenu().getHeight(), getMainMenu().getWidth(),getMainMenu().getHeight());


        // 2. Rahmen:
        g.setColor(Color.gray);

        g.drawLine(1, 1, getMainMenu().getWidth()-1,1);
        g.drawLine(1, 1, 1,getMainMenu().getHeight()-1);
        g.drawLine(getMainMenu().getWidth()-1, 1, getMainMenu().getWidth()-1,getMainMenu().getHeight()-1);
        g.drawLine(1, getMainMenu().getHeight()-1, getMainMenu().getWidth()-1,getMainMenu().getHeight()-1);
        

        // 3. Rahmen:
        g.setColor(Color.black);

        g.drawLine(2, 2, getMainMenu().getWidth()-2,2);
        g.drawLine(2, 2, 2,getMainMenu().getHeight()-2);
        g.drawLine(getMainMenu().getWidth()-2, 2, getMainMenu().getWidth()-2,getMainMenu().getHeight()-2);
        g.drawLine(2, getMainMenu().getHeight()-2, getMainMenu().getWidth()-2,getMainMenu().getHeight()-2);




    }
}
