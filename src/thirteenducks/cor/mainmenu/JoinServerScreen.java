/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.mainmenu;

import org.newdawn.slick.Color;
import thirteenducks.cor.mainmenu.components.Component;
import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.TestButton;
import thirteenducks.cor.mainmenu.components.ImageButton;
import thirteenducks.cor.mainmenu.components.Label;
import thirteenducks.cor.mainmenu.components.TiledImage;

/**
 * Der Server-beitreten-Bildschirm
 * bietet einfache Connect-to-IP-Funktion
 *
 * @TODO: Serverbrowser
 *
 * @author michael
 */
public class JoinServerScreen extends Container {

    /**
     * MainMenu-Referenz
     */
    MainMenu mainMenu;

    /**
     * Konstruktor
     *
     * @param m     Referenz auf das Hauptmenü
     */
    public JoinServerScreen(MainMenu m) {
        super(m, 15, 30, 70, 7);

        mainMenu = m;




        // Join Game Button:
        super.addComponent(new ImageButton(mainMenu, 30,30, 30,30, "img/mainmenu/buttonnew.png", "Join") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });





    }
}
