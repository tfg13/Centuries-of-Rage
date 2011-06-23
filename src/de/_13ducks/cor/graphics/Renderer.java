/*
 *  Copyright 2008, 2009, 2010, 2011:
 *   Tobias Fleig (tfg[AT]online[DOT]de),
 *   Michael Haas (mekhar[AT]gmx[DOT]de),
 *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
 *
 *  - All rights reserved -
 *
 *
 *  This file is part of Centuries of Rage.
 *
 *  Centuries of Rage is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Centuries of Rage is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de._13ducks.cor.graphics;

import java.util.HashMap;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

/**
 * Der Haupt-Renderer.
 * Alle renderOperationen müssen über diesen Renderer laufen.
 * Dieser Renderer versucht, die Aufrufe zu optimieren, um die Renderzeit zu verkürzen.
 * Dennoch werden alle Befehle unmittelbar ausgeführt, wenn eine render-Methode dieser Klasse zurückkehrt, dann ist das rendern abgeschlossen.
 */
public class Renderer {

    private static HashMap<String, GraphicsImage> imgMap;
    private static GraphicsImage currentGraphicsImage;
    private static Image currentImage;
    private static String currentImageIdentifier;

    /**
     * Zeichnet den angegebenen Bildausschnitt mit der angegebenen Skalierung auf die angegebene Position auf dem Bildschirm.
     * Rendervorgänge werden möglicherweise gecached.
     * @param imgPath Der Pfad zum Bild
     * @param x1 The x position to draw the image
     * @param y1 The y position to draw the image
     * @param x2 The x position of the bottom right corner of the drawn image
     * @param y2 The y position of the bottom right corner of the drawn image
     * @param srcx The x position of the rectangle to draw from this image (i.e. relative to this image)
     * @param srcy The y position of the rectangle to draw from this image (i.e. relative to this image)
     * @param srcx2 The x position of the bottom right cornder of rectangle to draw from this image (i.e. relative to this image)
     * @param srcy2 The t position of the bottom right cornder of rectangle to draw from this image (i.e. relative to this image)
     * @param colorFilter The colour filter to apply when drawing
     */
    public static void drawImage(String imgPath, double x1, double y1, double x2, double y2, double srcx, double srcy, double srcx2, double srcy2, Color colorFilter) {
        if (prepareImage(imgPath)) {
            currentImage.drawEmbedded((float) x1, (float) y1, (float) x2, (float) y2, (float) srcx, (float) srcy, (float) srcx2, (float) srcy2, colorFilter);
        }
    }

    /**
     * Zeichnet den angegebenen Bildausschnitt mit der angegebenen Skalierung auf die angegebene Position auf dem Bildschirm.
     * Rendervorgänge werden möglicherweise gecached.
     * @param imgPath Der Pfad zum Bild
     * @param x1 The x position to draw the image
     * @param y1 The y position to draw the image
     * @param x2 The x position of the bottom right corner of the drawn image
     * @param y2 The y position of the bottom right corner of the drawn image
     * @param srcx The x position of the rectangle to draw from this image (i.e. relative to this image)
     * @param srcy The y position of the rectangle to draw from this image (i.e. relative to this image)
     * @param srcx2 The x position of the bottom right cornder of rectangle to draw from this image (i.e. relative to this image)
     * @param srcy2 The t position of the bottom right cornder of rectangle to draw from this image (i.e. relative to this image)
     */
    public static void drawImage(String imgPath, double x1, double y1, double x2, double y2, double srcx, double srcy, double srcx2, double srcy2) {
        drawImage(imgPath, x1, y1, x2, y2, srcx, srcy, srcx2, srcy2, Color.white);
    }

    /**
     * Zeichnet den angegebenen Bildausschnitt mit der angegebenen Skalierung auf die angegebene Position auf dem Bildschirm.
     * Rendervorgänge werden möglicherweise gecached.
     * @param imgPath Der Pfad zum Bild
     * @param x1 The x position to draw the image
     * @param y1 The y position to draw the image
     * @param srcx The x position of the rectangle to draw from this image (i.e. relative to this image)
     * @param srcy The y position of the rectangle to draw from this image (i.e. relative to this image)
     * @param srcx2 The x position of the bottom right cornder of rectangle to draw from this image (i.e. relative to this image)
     * @param srcy2 The t position of the bottom right cornder of rectangle to draw from this image (i.e. relative to this image)
     */
    public static void drawImage(String imgPath, double x1, double y1, double srcx, double srcy, double srcx2, double srcy2) {
        if (prepareImage(imgPath)) {
            drawImage(imgPath, x1, y1, x1 + currentImage.getWidth(), currentImage.getHeight(), srcx, srcy, srcx2, srcy2);
        }
    }

    /**
     * Draw an image at a specified location and size
     * 
     * @param x The x location to draw the image at
     * @param y The y location to draw the image at
     * @param width The width to render the image at
     * @param height The height to render the image at
     * @param filter The color to filter with while drawing
     */
    public static void drawImage(String imgPath, double x, double y, double width, double height, Color filter) {
        if (prepareImage(imgPath)) {
            drawImage(imgPath, x, y, x + width, y + height, 0, 0, currentImage.getWidth(), currentImage.getHeight(), filter);
        }
    }

    /**
     * Draw an image at a specified location and size
     * 
     * @param x The x location to draw the image at
     * @param y The y location to draw the image at
     * @param width The width to render the image at
     * @param height The height to render the image at
     */
    public static void drawImage(String imgPath, double x, double y, double width, double height) {
        drawImage(imgPath, (float) x, (float) y, (float) width, (float) height, Color.white);
    }

    /**
     * Draw an image at a specified location and size
     * 
     * @param x The x location to draw the image at
     * @param y The y location to draw the image at
     * @param scale The scale factor
     */
    public static void drawImage(String imgPath, double x, double y, double scale) {
        if (prepareImage(imgPath)) {
            drawImage(imgPath, x, y, currentImage.getWidth() * scale, currentImage.getHeight() * scale);
        }
    }

    /**
     * Draw this image at the specified location
     * 
     * @param x The x location to draw the image at
     * @param y The y location to draw the image at
     * @param filter The color to filter with when drawing
     */
    public static void drawImage(String imgPath, double x, double y, Color filter) {
        if (prepareImage(imgPath)) {
            drawImage(imgPath, x, y, x + currentImage.getWidth(), y + currentImage.getHeight(), 0, 0, currentImage.getWidth(), currentImage.getHeight(), filter);
        }
    }

    /**
     * Draw this image at the specified location
     * 
     * @param x The x location to draw the image at
     * @param y The y location to draw the image at
     */
    public static void drawImage(String imgPath, double x, double y) {
        drawImage(imgPath, x, y, Color.white);
    }

    /**
     * Zeichnet ein Sprite aus einem Sheet auf den Bildschrim
     * Wird gecached, mehrere Aufrufe auf das gleiche Spritesheet sind performant.
     * @param sheetX Tilenummer (0-basiert) Richtung X.
     * @param sheetY Tilenummer (0-basiert) Richtung Y.
     * @param spriteSheet pfad des Spritesheets
     * @param x Zielkoordinate
     * @param y Zielkoordinate
     */
    public static void drawSprite(int sheetX, int sheetY, String spriteSheet, double x, double y) {
        if (prepareImage(spriteSheet)) {
            drawImage(spriteSheet, (float) x, (float) y,
                    sheetX * currentGraphicsImage.getTileX(),
                    sheetY * currentGraphicsImage.getTileY(),
                    sheetX * (currentGraphicsImage.getTileX() + 1),
                    sheetY * (currentGraphicsImage.getTileY() + 1));
        }
    }

    public static void drawSpriteCentered(int sheetX, int sheetY, String spriteSheet, double x, double y) {
        if (prepareImage(spriteSheet)) {
            drawSprite(sheetX, sheetY, spriteSheet, x - currentGraphicsImage.getTileX() / 2.0, y - currentGraphicsImage.getTileX() / 2.0);
        }
    }

    public static void setImageRotation(String imgPath, double angle) {
        if (prepareImage(imgPath)) {
            stopCaching();
            currentImage.rotate((float) angle);
        }

    }

    /**
     * Sucht das Bild aus der Imagemap und trifft notwendige Vorbereitungen.
     * @param path = null;
     * @return 
     */
    private static boolean prepareImage(String path) {
        if (path.equals(currentImageIdentifier)) {
            // Schon geladen!
            return true;
        }
        GraphicsImage img = imgMap.get(path);
        if (path != null) {
            if (currentImage != null) {
                currentImage.endUse();
            }
            currentGraphicsImage = img;
            currentImage = img.getImage();
            currentImageIdentifier = path;
            currentImage.startUse();
            return true;
        }
        return false;
    }
    
    /**
     * Aufrufen, um die Verwendung des aktuellen Bilds zu beenden.
     * Muss vor allen anderen Grafikaufrufen gemacht werden!!!
     */
    public static void stopCaching() {
        if (currentImage != null) {
            currentImage.endUse();
            currentImage = null;
            currentImageIdentifier = null;
        }
    }

    static void init(HashMap<String, GraphicsImage> imgMap) {
        Renderer.imgMap = imgMap;
    }

    /**
     * Liefert die Größe des gefragten Bildes zurück.
     * Liefert -1,-1, wenn das Bild nicht gefunden wurde.
     * @param imgPath
     * @return die größe des gesuchten Bildes
     */
    public static Dimension getImageInfo(String imgPath) {
        GraphicsImage img = imgMap.get(imgPath);
        if (img != null) {
            return new Dimension(img.getImage().getWidth(), img.getImage().getHeight());
        } else {
            return new Dimension(-1, -1);
        }
    }

    /**
     * Füllt eine Fläche mit einem Bild durch mehrfaches Kacheln
     * 
     * Achtung: Dieser Renderaufruf ist möglicherweise nicht voll gecached!
     * 
     * @param g der aktuelle Grafikkontext
     * @param imgPath das Bild
     * @param x Koordinate der Fläche
     * @param y Koordinate der Fläche
     * @param width Breite der Fläche
     * @param height Höhe der Fläche
     * @param offX x-offset
     * @param offY y-offset
     */
    public static void fillRectTiled(Graphics g, String imgPath, double x, double y, double width, double height, double offX, double offY) {
        stopCaching();
        GraphicsImage img = imgMap.get(imgPath);
        if (img != null) {
            g.fillRect((float) x, (float) y, (float) width, (float) height, img.getImage(), (float) offX, (float) offY);
        }
    }
}
