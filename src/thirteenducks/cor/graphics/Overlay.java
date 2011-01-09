/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.graphics;

import org.newdawn.slick.Graphics;

/**
 * Eine Grafik-Komponente, die über den GAME-Bereich gezeichnet wird.
 * Solche Overlays sind:
 * Chat
 * Teamwahl
 * Ingame-Menü
 * Statistik
 * etc.
 *
 * Das HUD ist KEIN Overlay. Overlays überlagern nur den Game-Bereich
 *
 * Der Game-Bereich wird unter dem Overlay normal weitergezeichnet, was Transparenzeffekte ermöglicht.
 * Jedes Overlay hat selbst zu entscheiden, wann und was es von sich zeichnet.
 *
 * Es kann sich dafür beim Inputmodul für bestimmte Keystrokes Events bestellen, und sich dann z.B. einblenden. (kommt noch)
 * Ein normales Overlay reagiert NICHT auf Input irgendeiner Art,
 * es blendet sich automatisch aus oder benutzt nur wenige Events des Input-Moduls
 *
 * Für komplexe Inputschema (wie z.B. der Chat) kann der InputMode überschrieben werden (kommt noch)
 *
 * Ein Overlay bezieht seine Informationen inner, sofern das erforderlich ist.
 *
 *
 * @author tfg
 */
public abstract class Overlay {

    /**
     * Zeichnet das Overlay in den Grafikkontext g.
     * Achtung: Verwendet teilweise auch direktes Zeichnen auf den Bildschirm (!!!)
     *
     * @param g
     * @param fullResX
     * @param fullResY
     * @param hudX
     */
    public abstract void renderOverlay(Graphics g, int fullResX, int fullResY, int hudX);

}
