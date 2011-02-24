/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.mainmenu;

import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.ImageButton;
import thirteenducks.cor.mainmenu.components.TextBox;

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
     * Das Texteingabefeld für IP oder Servername
     */
    TextBox textBox;

    /**
     * Konstruktor
     *
     * @param m     Referenz auf das Hauptmenü
     */
    public JoinServerScreen(MainMenu m) {
        super(m, 15, 30, 70, 7);

        mainMenu = m;



        textBox = new TextBox(mainMenu, 30, 30);
        super.addComponent(textBox);

        super.addComponent(new ImageButton(mainMenu, 40, 40, 10, 8, "img/mainmenu/buttonnew.png", "Join") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                System.out.println("Joining Game: " + textBox.getText());
            }
        });





    }
}
