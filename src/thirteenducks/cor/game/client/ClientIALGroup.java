/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
