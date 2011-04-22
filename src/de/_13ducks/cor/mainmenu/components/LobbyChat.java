/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.mainmenu.components;

import de._13ducks.cor.mainmenu.MainMenu;

/**
 *
 * @author michael
 */
public class LobbyChat extends Container {

    /**
     * Das Chatfenster
     */
    private ChatWindow chatWindow;

    /**
     * Konstruktor
     * @param m
     * @param x
     * @param y
     */
    public LobbyChat(MainMenu m, double x, double y) {
        super(m, x, y, 45, 15);


        // Die Anzeige für Chat-Nachrichten
        chatWindow = new ChatWindow(m, x, y);
        super.addComponent(chatWindow);

        // Das Texteingabefeld:
        super.addComponent(new TextBox(m,x,y+10));


    }

    /**
     * Zeicgt eine eingegangene Chatnachricht an
     * @param message - die Nachricht
     */
    public void chatMessage(String message) {
        chatWindow.chatMessage(message);
    }
}
