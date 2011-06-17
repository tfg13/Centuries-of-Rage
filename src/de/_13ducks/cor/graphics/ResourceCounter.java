/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de._13ducks.cor.graphics;

import de._13ducks.cor.game.client.Client;
import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 *
 * @author Johannes
 */
public class ResourceCounter extends Overlay {

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY, Map<String, GraphicsImage> imgMap) {
        g.setColor(Color.lightGray);
        g.fillRect((int) (fullResX * 0.59), (int) (fullResY * 0.00), (int) (fullResX * 0.11), (int) (fullResY * 0.04));
        g.setColor(Color.black);
        g.drawRect((int) (fullResX * 0.59), (int) (fullResY * 0.00), (int) (fullResX * 0.11), (int) (fullResY * 0.04));
        imgMap.get("img/sym/res1.png").getImage().draw((int) (fullResX * 0.6), (int) (fullResY * 0.01), (int) (fullResX * 0.02), (int) (fullResY * 0.02));
        g.setFont(FontManager.getFont0());
        g.drawString(String.valueOf(Client.getInnerClient().game.getOwnPlayer().res1), (int) (fullResX * 0.63), (int) (fullResY * 0.01));

    }

    public ResourceCounter() {

    }

}
