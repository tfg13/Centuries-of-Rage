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

        chatWindow = new ChatWindow(m, x, y);
        super.addComponent(chatWindow);

        chatBox = new TextBox(m, x, y + 15);
        super.addComponent(chatBox);

        super.addComponent(new ImageButton(m, x + 31, y + 14.5, 12, 4.8, "img/mainmenu/buttonnew.png", "send") {

            @Override
            public void mouseClicked(int button, int x, int y, int clickCount) {
                if (!chatBox.getText().equals(null)) {
                    getMainMenu().getLobby().send('7' + getMainMenu().getLobby().getPlayername() + ": " + chatBox.getText());
                }
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
