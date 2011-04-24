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
     * Das TexteingabeFeld
     */
    private TextBox chatBox;

    /**
     * Konstruktor
     * @param m
     * @param x
     * @param y
     */
    public LobbyChat(MainMenu m, double x, double y) {
        super(m, x, y, 45, 25);

        chatWindow = new ChatWindow(m,x,y);
        super.addComponent(chatWindow);

        chatBox = new TextBox(m,x,y+15);
        super.addComponent(chatBox);

        
    }

    /**
     * Zeicgt eine eingegangene Chatnachricht an
     * @param message - die Nachricht
     */
    public void chatMessage(String message) {
        chatWindow.chatMessage(message);
    }
}
