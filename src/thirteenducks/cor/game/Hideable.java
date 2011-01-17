/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.game;

/**
 * Alles was Hideable ist, kann unsichtbar sein.
 * Derzeit wird das vor allem Verwendet, damit Einheiten sich im Fow(Halbschatten(!)) verstecken können.
 *
 * @author tfg
 */
public interface Hideable {

    /**
     * Findet heraus, ob dieses Hideable auch im vollen 100% dunklen FoW gezeichnet werden soll.
     * @return true, wenn im 100% FoW zeichnen
     */
    public boolean renderInFullFog();
    /**
     * Findet heraus, ob dieses Hideable auch im halben 50% grauen FoW gezeichnet werden soll.
     * @return true, wenn im 50% FoW zeichnen
     */
    public boolean renderInHalfFog();
    /**
     * Findet heraus, ob dieses Hideable im Sichtbaren gezeichnet werden soll.
     * @return true, wenn im 0% FoW (aufgedeckt) zeichnen
     */
    public boolean renderInNullFog();

}
