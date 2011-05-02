/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.mainmenu.components;

import de._13ducks.cor.mainmenu.MainMenu;
import java.util.ArrayList;

/**
 * Ein Dropdown-Steuerelement
 * @author michael
 */
public class Dropdown extends Component {

    /**
     * Die Liste aller Items des Dropdowns
     */
    private ArrayList<String> items;
    /**
     * Der Index des ausgewählten Objekts
     */
    private int selectedIndex;

    /**
     * Konstruktor
     * @param m - Hauptmenü-Referenz
     * @param x - X-Koordinate
     * @param y - Y-Koordinate
     * @param width - Die Breite des Dropdowns
     * @param items - Eine ArrayList mit den Items, die das Dropdown anbieten soll
     */
    public Dropdown(MainMenu m, double x, double y, double width, ArrayList<String> items) {
        super(m, x, y, width, 100);
    }
}
