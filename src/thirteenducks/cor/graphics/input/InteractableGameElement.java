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

/**
 * InteractableGameElements sind Spielelemente, mit denen (durch die Maus) interagiert werden kann.
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
     * Dieses InteractableGameElement wurde angeklickt.
     */
    public void mouseKlicked();
    /**
     * Die Maus wurde über diesem InteractableGameElement gedrückt.
     */
    public void mousePressed();
    /**
     * Die Maus wurde über diesem InteractableGameElement loggelassen.
     */
    public void mouseReleased();
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
    public boolean selPosChanged();
    public SelectionMarker getSelectionMarker();
}
