/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.mainmenu;

import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.ImageButton;
import thirteenducks.cor.mainmenu.components.ScaledImage;

/**
 * Der Startbildschirm, der als erstes angezeigt wird
 *
 * @author michael
 */
public class StartScreen extends Container {

    /**
     * MainMenu-Referenz
     */
    MainMenu mainMenu;

    /**
     * Konstruktor
     *
     * @param m     Referenz auf das Hauptmenü
     */
    public StartScreen(MainMenu m) {
        super(m, 15, 85, 80, 8);

        mainMenu = m;

        // Hintergrund
        super.addComponent(new ScaledImage(mainMenu, -20, 85, 140, 8, "img/mainmenu/buttonnew.png"));


        // Start Game Button:
        super.addComponent(new ImageButton(mainMenu, 16, 86, 13, 6, "img/mainmenu/buttonnew.png", "Start Game") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });

        // Join Game Button:
        super.addComponent(new ImageButton(mainMenu, 30, 86, 13, 6, "img/mainmenu/buttonnew.png", "Join Game") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                mainMenu.getMenu("startscreen").fadeOut();
                mainMenu.getMenu("joinserverscreen").fadeIn();

            }
        });

        // Options Button:
        super.addComponent(new ImageButton(mainMenu, 44, 86, 13, 6, "img/mainmenu/buttonnew.png", "OPTIONS") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });

        // Map Editor
        super.addComponent(new ImageButton(mainMenu, 58, 86, 13, 6, "img/mainmenu/buttonnew.png", "Map Editor") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });

        // Quit Game Button:
        super.addComponent(new ImageButton(mainMenu, 72, 86, 13, 6, "img/mainmenu/buttonnew.png", "QUIT") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });







    }
}
