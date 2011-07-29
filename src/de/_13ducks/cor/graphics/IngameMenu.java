/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.graphics;

import de._13ducks.cor.game.client.Client;
import de._13ducks.cor.graphics.input.OverlayMouseListener;
import org.newdawn.slick.Graphics;

/**
 *
 * @author Johannes
 */
public class IngameMenu implements Overlay {

    private static final int DURATION = 500;
    private int x1, y1, x2, y2;
    private long startTime;
    private boolean out = false;

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY) {
        long passedTime = System.currentTimeMillis() - startTime;
        double k = 0.1 * fullResX / 128;
        int x = (int) ((fullResX / 2) - (207 * k));
        int y = (int) (-(512 - 55) * k);
        if (passedTime < DURATION) {
            double progress = 1 - ((Math.cos(passedTime * Math.PI / DURATION) + 1) / 2);
            y = (int) (-(512 - 55) * k * (1 - progress));
        } else if (passedTime >= DURATION) {
            y = 0;
        }
        Renderer.drawImage("img/hud/tilemap_hud_classical_era.png", x, y, x + (k * 350), y + (k * 512), 0, 0, 350, 511);
    }

    public IngameMenu(int fullResX, int fullResY) {
        x1 = (int) (fullResX * 0.45);
        x2 = (int) (fullResX * 0.55);
        y1 = 0;
        y2 = (int) (fullResX * 0.1 * 55 / 128);
        Client.getInnerClient().rogGraphics.inputM.addOverlayMouseListener(new OverlayMouseListener() {

            @Override
            public int getCatch1X() {
                return x1;
            }

            @Override
            public int getCatch1Y() {
                return y1;
            }

            @Override
            public int getCatch2X() {
                return x2;
            }

            @Override
            public int getCatch2Y() {
                return y2;
            }

            @Override
            public void mouseMoved(int x, int y) {
            }

            @Override
            public void mouseDragged(int x, int y) {
            }

            @Override
            public void mouseWheelMoved(int change) {
            }

            @Override
            public void mousePressed(int button, int x, int y) {
            }

            @Override
            public void mouseReleased(int button, int x, int y) {
                startTime = System.currentTimeMillis();
            }
        });
    }
}
