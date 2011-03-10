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

import thirteenducks.cor.mainmenu.components.*;
import thirteenducks.cor.mainmenu.components.AnimatedImage;
import java.util.HashMap;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

/**
 * Grafisches Hauptmenü
 *
 * @author michael
 */
public class MainMenu extends AppGameContainer {

    /**
     * Grafikdingens
     */
    MainMenuGraphics graphics;
    /**
     * Die einzelnen Menüs
     */
    HashMap<String, Container> menus;

    /**
     * Konstruktor
     */
    public MainMenu(MainMenuGraphics g) throws SlickException {
        super(g, 1024, 768, false);

        this.setTargetFrameRate(65);

        menus = new HashMap<String, Container>();

        graphics = g;
        graphics.setMainMenuReference(this);

        /**
         * Komponenten initialisieren:
         */
        initComponents(g);

        // mit rendern beginnen:
        this.setShowFPS(false);
        super.start();
    }

    /**
     * Setzt die Referenz auf die Grafikkomponente
     * Wird vom Konstruktor von MainMenuGraphics aufgerufen
     *
     * @param g - Referenz auf MAinMenuGraphics
     */
    public void setMainMenuGraphics(MainMenuGraphics g) {
        graphics = g;

    }

    /**
     * Initialisiert die Komponenten des Hauptmenüs
     */
    private void initComponents(MainMenuGraphics g) {


        /**********************************************************************
         * Hintergrund:
         *********************************************************************/
        // Animierter Hintergrund:
        //g.components.add(new AnimatedImage(this, "/img/mainmenu/test.png"));

        // Rahmen:
        // aus irgendeinem Grund funktioniert nur 99,999% statt 100%....
        g.components.add(new Frame(this, 0,0,99.9999f,99.9999f));

        // Mauskoordiaten anzeigen:
        g.components.add(new CoordinateView(this));


        /**********************************************************************
         * Menüs:
         *********************************************************************/
        // Hauptmenü:
        Container startScreen = new StartScreen(this);
        menus.put("startscreen", startScreen);
        g.addComponent(startScreen);
        startScreen.fadeIn();

        // Spiel beitreten:
        Container joinServerScreen = new JoinServerScreen(this);
        menus.put("joinserverscreen", joinServerScreen);
        g.addComponent(joinServerScreen);
        joinServerScreen.fadeOut();

        // RandomMapBuilder
        Container randomMapBuilderScreen = new RandomMapBuilderScreen(this);
        menus.put("randommapbuilderscreen", randomMapBuilderScreen);
        g.addComponent(randomMapBuilderScreen);
        randomMapBuilderScreen.fadeOut();

    }

    /**
     * Gibt ein bestimmtes Menü zurück
     *
     * @param name - Name des Menüs
     * @return     - Das Menü 
     */
    public Container getMenu(String name) {
        return menus.get(name);
    }
}
