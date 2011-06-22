/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de._13ducks.cor.graphics;

import org.newdawn.slick.Graphics;

/**
 *
 * @author Johannes
 */
public class IngameMenu extends Overlay {

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
        Renderer.drawImage("img/hud/menubutton.png", (int) (fullResX * 0.45), 0, (int) (fullResX * 0.1), (int) (fullResY * 0.03));
    }

    public IngameMenu() {
	
    }

}
