/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.mainmenu.components;

import de._13ducks.cor.mainmenu.MainMenu;
import org.newdawn.slick.Color;

/**
 *
 * @author michael
 */
public abstract class TeamSelector extends Container {

    /**
     * Das ausgewählte Team
     */
    private int team;
    /**
     * Das Label, das die Teamnummer anzeigt
     */
    private ImageButton teamButton;

    /**
     * Konstruktor
     * @param m - Hauptmenü-Referenz
     * @param x - X-Koordinate (in %)
     * @param y - Y-Koordinate (in %)
     */
    public TeamSelector(MainMenu m, double x, double y) {
        super(m, x, y, 6, 6);


        team = 1;

        teamButton = new ImageButton(m, x, y, 6, 6, "img/mainmenu/buttonnew.png", "1") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                team++;
                if (team == 10) {
                    team = 1;
                }
                teamButton.setText(String.valueOf(team));
                teamChanged(team);
            }
        };
        super.addComponent(teamButton);

    }

    /**
     * Diese Funktion wird beim instanzieren überschrieben. Sie wird aufgerufen, wenn das Team geändert wird.
     * @param newteam
     */
    public abstract void teamChanged(int newteam);

    /**
     * Setter für team
     * setzt das ausgewählte Team
     * @param team - das ausgewählte Team
     */
    public void setTeam(int team) {
        this.team = team;
        teamButton.setText(String.valueOf(team));
    }
}
