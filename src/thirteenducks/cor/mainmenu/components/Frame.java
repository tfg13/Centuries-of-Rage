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
    public Frame(MainMenu m, float x, float y, float width, float height) {
        super(m, 0, 0, width, height);
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

        g.drawLine(this.getX1(), this.getY1(), this.getX2(), this.getY1());
        g.drawLine(this.getX1(), this.getY1(), this.getX1(), this.getY2());
        g.drawLine(this.getX1(), this.getY2(), this.getX2(), this.getY2());
        g.drawLine(this.getX2(), this.getY1(), this.getX2(), this.getY2());


        // 2. Rahmen:
        g.setColor(Color.gray);

        g.drawLine(this.getX1()+1+1, this.getY1()+1+1, this.getX2()-1, this.getY1()+1);
        g.drawLine(this.getX1()+1, this.getY1()+1, this.getX1()+1, this.getY2()-1);
        g.drawLine(this.getX1()+1, this.getY2()-1, this.getX2()-1, this.getY2()-1);
        g.drawLine(this.getX2()-1, this.getY1()+1, this.getX2()-1, this.getY2()-1);

        // 3. Rahmen:
        g.setColor(Color.black);


        g.drawLine(this.getX1()+2+1+1, this.getY1()+2+1+1, this.getX2()-2-1, this.getY1()+2+1);
        g.drawLine(this.getX1()+2+1, this.getY1()+2+1, this.getX1()+2+1, this.getY2()-2-1);
        g.drawLine(this.getX1()+2+1, this.getY2()-2-1, this.getX2()-2-1, this.getY2()-2-1);
        g.drawLine(this.getX2()-2-1, this.getY1()+2+1, this.getX2()-2-1, this.getY2()-2-1);



    }
}
