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
        super(m, x, y, 45, 15);


        // Die Anzeige für Chat-Nachrichten
        chatWindow = new ChatWindow(m, x, y);
        super.addComponent(chatWindow);

        // Das Texteingabefeld:
        chatBox = new TextBox(m,x,y+12);
        super.addComponent(chatBox);

        // "Senden"-Button:
        super.addComponent(new ImageButton(m,x+32,y+12,12,6,"img/mainmenu/buttonnew.png", "Send") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                getMainMenu().getLobby().send('7' + chatBox.getText());
                chatBox.setText("");
            }
        });


    }

    /**
     * Zeicgt eine eingegangene Chatnachricht an
     * @param message - die Nachricht
     */
    public void chatMessage(String message) {
        chatWindow.chatMessage(message);
    }
}
