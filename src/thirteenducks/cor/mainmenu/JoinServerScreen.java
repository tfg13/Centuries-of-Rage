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
package thirteenducks.cor.mainmenu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import org.lwjgl.opengl.DisplayMode;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.mainmenu.components.Container;
import thirteenducks.cor.mainmenu.components.Frame;
import thirteenducks.cor.mainmenu.components.ImageButton;
import thirteenducks.cor.mainmenu.components.TextBox;
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
    private MainMenu mainMenu;
    /**
     * Das Texteingabefeld für IP oder Servername
     */
    private TextBox textBox;
    /**
     * default port
     */
    final int port = 39264;

    /**
     * Konstruktor
     *
     * @param m     Referenz auf das Hauptmenü
     */
    public JoinServerScreen(MainMenu m) {
        super(m, 15, 30, 70, 40);

        mainMenu = m;


        // Hintergrund:
        super.addComponent(new TiledImage(mainMenu, 15, 30, 70, 40, "img/mainmenu/rost.png"));

        // Rahmen:
        super.addComponent(new Frame(mainMenu, 15, 30, 70, 40));

        // Textfeld initialisieren:
        textBox = new TextBox(mainMenu, 37, 40);
        super.addComponent(textBox);

        // Join-Button:
        super.addComponent(new ImageButton(mainMenu, 47, 50, 10, 8, "img/mainmenu/buttonnew.png", "Join") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                System.out.println("Joining Game: " + textBox.getText());

                try {
                    boolean debug = true;
                    InetAddress address = InetAddress.getByName(textBox.getText());
                    // port wird am Anfang dieser Datei deklariert
                    String playerName = "testname";
                    DisplayMode displayMode = new DisplayMode(mainMenu.getResX(), mainMenu.getResY());
                    boolean fullScreen = mainMenu.getFullScreen();
                    HashMap cfg = readCfg();

                    ClientCore clientCore = new ClientCore(debug, address, port, playerName, displayMode, fullScreen, cfg);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }


                fadeOut();
            }
        });


    }

    /**
     * Liest die Einstellungen ein
     */
    private HashMap readCfg() {
        HashMap cfg = new HashMap();
        File cfgFile = new File("client_cfg.txt");
        try {
            FileReader cfgReader = new FileReader(cfgFile);
            BufferedReader reader = new BufferedReader(cfgReader);
            String zeile = null;
            int i = 0; // Anzahl der Durchläufe zählen
            while ((zeile = reader.readLine()) != null) {
                if (i == 0) {
                    // Die erste Zeile überspringen
                    //   continue;
                }
                // Liest Zeile fuer Zeile, jetzt auswerten und in Variablen
                // schreiben
                int indexgleich = zeile.indexOf('='); // Istgleich suchen
                if (indexgleich == -1) {
                } else {
                    String v1 = zeile.substring(0, indexgleich); // Vor dem =
                    // rauschneiden
                    String v2 = zeile.substring(indexgleich + 1); // Nach dem
                    // =
                    // rausschneiden
                    System.out.println(v1 + " = " + v2);
                    cfg.put(v1, v2);

                }
            }
            reader.close();
        } catch (FileNotFoundException e1) {
            // cfg-Datei nicht gefunden -  egal, wird automatisch neu angelegt
            System.out.println("client_cfg.txt not found, creating new one...");
            try {
                cfgFile.createNewFile();
            } catch (IOException ex) {
                System.out.println("[Core-Error] Failed to create client_cfg.txt .");
            }
        } catch (IOException e2) {
            // Inakzeptabel
            e2.printStackTrace();
            System.out.println("[Core-ERROR] Critical I/O ERROR!");
            System.exit(1);
        }
        return cfg;
    }
}
