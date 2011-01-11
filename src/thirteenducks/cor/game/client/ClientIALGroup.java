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

package thirteenducks.cor.game.client;

import thirteenducks.cor.game.Unit;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Eine Gruppe von Einheiten beim IAL-Angriff.
 * Im Prinzip eine ArrayList, die speichern kann, wie viele von ihr verarbeitet wurden, also nichtmehr enthalten sind.
 * Enthält noch weitere Speicher-Funktionen für das IAL-System.
 *
 * @author tfg
 */
public class ClientIALGroup<E> extends ArrayList<E> {

    public static final int MODE_NORMAL = 0;    // Normaler Modus, bei der Zuteilung an keine Grenzen gestoßen
    public static final int MODE_OUTSOURCE = 1; // Grenze wurde erreicht, und Einheiten wurde abgegeben
    public static final int MODE_STORE = 2;     // Sämtliche Grenzen überschritten, Einheiten werden "hinten" eingelagert

    private int contentCounter = 0;
    private int mode = 0;           // Wie schlimm ist die Überfüllung schon?
    private ArrayList<Unit> storeUnits;
    
    public ClientIALGroup() {
        super();
        storeUnits = new ArrayList<Unit>();
    }

    public ClientIALGroup(Collection<? extends E> c) {
        throw new java.lang.UnsupportedOperationException("Empty lists only!");
    }

    public ClientIALGroup(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public boolean add(E e) {
        return super.add(e);
    }

    @Override
    public void add(int index, E e) {
        super.add(index, e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new java.lang.UnsupportedOperationException("You can't add multiple elements");
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new java.lang.UnsupportedOperationException("You can't add multiple elements");
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new java.lang.UnsupportedOperationException("You can't delete multiple elements");
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public E remove(int index) {
        contentCounter++;
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        contentCounter++;
        return super.remove(o);
    }

    /**
     * Auslagern - zählt nicht als verarbeiten.
     * @param index
     * @return
     */
    public E outsource(int index) {
        return super.remove(index);
    }

    public int getMode() {
        return mode;
    }

    public void modeUp() {
        if (mode != 2) {
            mode++;
        }
    }

    public int getCounter() {
        return contentCounter;
    }

    public void addStore(Unit unit) {
        storeUnits.add(unit);
    }

    public ArrayList<Unit> getStored() {
        return storeUnits;
    }


}
