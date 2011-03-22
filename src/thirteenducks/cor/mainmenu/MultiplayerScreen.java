/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thirteenducks.cor.mainmenu;

import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.ImageButton;
import thirteenducks.cor.mainmenu.components.TiledImage;

/**
 * Der Mehrspieler-Bildschirm
 * enthält den ServerBrowser zum joinen und einen Button zum Server starten
 *
 * @author michael
 */
public class MultiplayerScreen extends Container {

    /**
     * Konstruktor
     * @param m - Hauptmenü-Referenz
     */
    public MultiplayerScreen(MainMenu m) {
        super(m, 0, 0, 100, 100);

        // Hintergrund:
        super.addComponent(new TiledImage(m, 10, 10, 80, 70, "/img/mainmenu/rost.png"));

        // Join-Button:
        super.addComponent(new ImageButton(m, 15, 70, 12, 6, "img/mainmenu/buttonnew.png", "Join") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });

        // Start-Servr-Button:
        super.addComponent(new ImageButton(m, 30, 70, 12, 6, "img/mainmenu/buttonnew.png", "Sart Server") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
            }
        });

        // zurück-button:
        super.addComponent(new ImageButton(m, 45, 70, 12, 6, "img/mainmenu/buttonnew.png", "BACK") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                super.getMainMenu().getMenu("startscreen").fadeIn();
                fadeOut();
            }
        });
    }
}
