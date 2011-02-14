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

package thirteenducks.cor.graphics.input;

import java.util.List;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.ability.Ability;
import thirteenducks.cor.game.client.ClientCore;

/**
 * InteractableGameElements sind Spielelemente, mit denen (primär durch die Maus) interagiert werden kann.
 *
 * Dieses Interface regelt, was passiert, wenn die Maus über eine Objekt bewegt wird, wenn es angeklickt wird/ etc.
 * Nur die Selektion selber findet nicht hier statt, dafür ist das Inputmodul selber zuständig
 */
public interface InteractableGameElement {

    /**
     * Die Maus befindet sich über dem InteractableGameElement.
     */
    public void mouseHovered();
    /**
     * Findet heraus, ob dieses IGE selektiertbar ist.
     * Selektierbar bedeutet, der User kann es (üblicherweise per Linksklick)
     * anwählen, es ist dann in einem besonderen Zustand in dem es direkte Befehle annimmt.
     * @return true, wenn selektier bar.
     */
    public boolean selectable();
    /**
     * Findet heraus, ob dieses IGE derzeit selektiert ist.
     * Nur sinnvoll, wenn überhaupt selektierbar.
     * @return true, wenn derzeit selektiert.
     */
    public boolean isSelected();
    /**
     * Setzt den Selektionsstatus dieses IGE's.
     * Nur sinnvoll, wenn überhaupt selektierbar.
     * @param sel true, wenn es nach diesem Aufruf selektiert sein soll, sonst false.
     */
    public void setSelected(boolean sel);
    /**
     * Findet heraus, ob sich die Position(en) an denen dieses IGE selektierbar ist
     * seit dem letzten Aufruf von dieser Methode geändert haben.
     * Dies wird vom Inputmodul regelmäßig aufgerufen, um die Selektionsmap aktuell zu halten.
     *
     * Ist Anfangs immer true, damit die Position zum ersten Mal gesetzt wird.
     * Ist ebenso nach dem Tod einmal false, damit die Registrierung gelöscht wird.
     * @return true, wenn seit dem letzten mal geändert
     */
    public boolean selPosChanged();
    /**
     * Liefert einen SelektionMarker der das Update der SelectionMap repräsentiert.
     * Wird nur vom Inputmodul aufgerufen, und zwar unmittelbar nach dem ein selPosChanged() true ergeben hatte.
     * @return ein SelektionMarker, der das für diese Einheit notwendige Update der Selektionsmap repräsentiert.
     */
    public SelectionMarker getSelectionMarker();
    /**
     * Fragt das IGE, ob es zulassen möchte, dass der Player mit der angegebenen Id es auswählt.
     * In der Regel sind Einheiten nur vom Besitzter anwählbar, allerdings sind auch Ausnahmen denkbar
     * und hiermit realisierbar.
     * @param playerId
     * @return
     */
    public boolean isSelectableByPlayer(int playerId);
    /**
     * Findet heraus, ob dieses IGE zusammen mit anderen ausgewählt werden kann, oder nur alleine.
     * In der Regel können beliebig viele Einheiten zusammen ausgewählt werden, aber nur ein Gebäude.
     * @return true, wenn dieses IGE zusammen mit anderen ausgewählt werden kann.
     */
    public boolean isMultiSelectable();
    /**
     * Aufrufen, um einem selektierten IGE mitzuteilen, dass ein Befehl für es eingegangen ist.
     * Ein Befehl ist ein Klick mit der rechten oder mittleren Maustaste, während das IGE selektiert ist.
     * Diese Methode wird aufgerufen, wenn der Klick andere IGE'S getroffen hat.
     * Das IGE wird nun berechnen, ob mit den Zielen etwas anzufangen ist und gegebenenfalls in Aktion treten.
     * Anhand des Parameters doubleKlick kann das IGE herausfinden, ob es sich um den (2ten!!!) Klick eines Doppelklicks handelt.
     * @param button Mittlere oder Linke Maustaste. (2 oder 3) (Reihenfolge?)
     * @param targets Eine Liste aller IGE's die sich auf der ZielPosition des Klicks befinden
     * @param doubleKlick ist dies der wiederholte klick eines Doppelklicks
     */
    public void command(int button, List<InteractableGameElement> targets, boolean doubleKlick, ClientCore.InnerClient rgi);
    /**
     * Aufrufen, um einem selektierten IGE mitzuteilen, dass ein Befehl für es eingegangen ist.
     * Ein Befehl ist ein Klick mit der rechten oder mittleren Maustaste, während das IGE selektiert ist.
     * Diese Methode wird aufgerufen, wenn der Klick keine anderen IGE's getroffen hat, (sondern den Boden)
     * Das IGE soll sich möglicherweise hier hin bewegen, daher wird das getroffenen Feld übertragen.
     * Anhand des Parameters doubleKlick kann das IGE herausfinden, ob es sich um den (2ten!!!) Klick eines Doppelklicks handelt.
     * @param button Mittlere oder Linke Maustaste. (2 oder 3) (Reihenfolge?)
     * @param target Das Feld auf der Map, dass der Benutzer angeklickt hatte.
     * @param doubleKlick ist dies der wiederholte Klick eines Doppelklicks?
     */
    public void command(int button, Position target, boolean doubleKlick, ClientCore.InnerClient rgi);

    /**
     * Es wurde eine für diese Einheit bestimmte Taste gedrückt.
     * @param key Der Keycode des Buttons.
     * @param character Das gedrückte Zeichen
     */
    public void keyCommand(int key, char character);
    /**
     * Liefert eine Liste von Abilitys
     * @return
     */
    public List<Ability> getAbilitys();
}
