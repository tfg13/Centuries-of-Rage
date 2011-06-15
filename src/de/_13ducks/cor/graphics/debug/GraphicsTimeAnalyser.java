/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.graphics.debug;

import de._13ducks.cor.graphics.GraphicsImage;
import de._13ducks.cor.graphics.Overlay;
import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 *
 * @author tfg
 */
public class GraphicsTimeAnalyser extends Overlay {
    
    private static final int entries = 300;
    private static final int targetLinePixel = 100;
    
    private FrameTime[] frameTimes;
    
    private FrameTime currentFrame;
    private int currentIndex;
    
    private int targetMaxTime;
    
    public GraphicsTimeAnalyser(int targetFrameRate) {
        frameTimes = new FrameTime[entries];
        targetMaxTime = 1000 / targetFrameRate;
    }
    
    /**
     * Aufrufen, wenn Frame-Rendering startet.
     */
    public void startFrame() {
        currentFrame = new FrameTime();
        currentFrame.setStartMillis(System.nanoTime());
    }
    
    /**
     * Aufrufen, wenn Frame-Rendering endet.
     */
    public void endFrame() {
        currentFrame.setEndMillis(System.nanoTime());
        frameTimes[currentIndex] = currentFrame;
        currentIndex++;
        if (currentIndex >= entries) {
            currentIndex = 0;
        }
    }

    @Override
    public void renderOverlay(Graphics g, int fullResX, int fullResY, Map<String, GraphicsImage> imgMap) {
        g.setLineWidth(1);
        g.setColor(Color.darkGray);
        g.fillRect(fullResX - entries, fullResY - targetLinePixel, entries, targetLinePixel);
        g.setColor(Color.blue);
        g.drawLine(fullResX - entries, fullResY - targetLinePixel, fullResX, fullResY - targetLinePixel);
        for (int i = 0; i < frameTimes.length; i++) {
            FrameTime f = frameTimes[i];
            if (f != null) {
                // Länge bestimmen
                long nanos = f.getEndMillis() - f.getStartMillis();
                int pixel = (int) (nanos / 1000000.0 * (1.0 * entries / targetMaxTime));
                
                if (pixel > targetLinePixel) {
                    g.setColor(Color.red);
                } else {
                    g.setColor(Color.green);
                }
                g.drawLine(fullResX - (entries - i), fullResY, fullResX - (entries - i), fullResY - pixel);
                
                // Position in der Sekunde
                double sinceSec = - Math.floor(f.getEndMillis() / 1000000000.0) * 1000000000 + f.getEndMillis();
                sinceSec /= 1000000.0;
                //System.out.println("SinceSec " + sinceSec);
                //System.out.println(f.getEndMillis() + "  " + sinceSec);
                pixel = (int) (targetLinePixel / 1000.0 * sinceSec);
                // Markieren
                g.setColor(Color.orange);
                g.drawRect(fullResX - (entries - i) - 1, fullResY - pixel - 1, 3, 3);
            }
        }
    }
    
}
