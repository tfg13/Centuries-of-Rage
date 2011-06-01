/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de._13ducks.cor.graphics;

import java.util.Map;
import org.newdawn.slick.Graphics;

/**
 *
 * @author Johannes
 */
public class IngameMenu extends Overlay {

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY, Map<String, GraphicsImage> imgMap) {
	//g.drawImage(imgMap.get("img/hud/menubutton.png").getImage(), (int) (fullResX * 0.2), 0);
	imgMap.get("img/hud/menubutton.png").getImage().draw((int) (fullResX * 0.45), 0, (int) (fullResX * 0.1), (int) (fullResY * 0.03));
    }

    public IngameMenu() {
	
    }

}
