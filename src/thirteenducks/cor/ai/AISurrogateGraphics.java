/*
 *  Copyright 2008, 2009, 2010:
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
package thirteenducks.cor.ai;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.lwjgl.opengl.Display;

import org.newdawn.slick.*;
import org.newdawn.slick.opengl.renderer.Renderer;
import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Position;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.map.CoRMapElement;
import thirteenducks.cor.graphics.CoreGraphics;
import thirteenducks.cor.graphics.GraphicsBullet;
import thirteenducks.cor.graphics.GraphicsContent;
import thirteenducks.cor.graphics.GraphicsImage;

/**
 * Fake-Grafik für AIClient
 */
public class AISurrogateGraphics extends CoreGraphics {

    ClientCore.InnerClient rgi;
    

    public AISurrogateGraphics(ClientCore.InnerClient inner) throws SlickException {
        super(inner);
        rgi = inner;
    }

    /**
     * Blendet den Ladebildschirm ein.
     * Muss VOR initModule aufgerufen werden.
     */
    @Override
    public void preStart() {
        rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 7, 0, 0, 0, 0));
    }

    @Override
    public void preStart2() {
    }

    @Override
    public void initModule() {
    }

    /**
     *  Liest die Fow-Texturen ein
     */
    private void importFow() {
    }

    @Override
    public void setLoadStatus(int status) {
    }

    @Override
    public void triggerLaunchError(int type) {
    }

    @Override
    public void triggerStatusWaiting() {
    }

    // Fügt gescheduelete Bullets jetzt ein
    private void manageBullets() {
    }

    /**
     * Liest alle Geschoss-Bildchen ein
     */
    private void importBullets() {
    }

    private void importSelectionMarkers() {
    }

    private void importHuds() {
    }

    private void readAnimations() {
    }

    private int readFrameRate(File properties) {
        return 0;
    }

    @Override
    protected void initSubs() {
    }

    @Override
    public void activateMap(CoRMapElement[][] newVisMap) {
    }

    /**
     * Schält den Fog of War ab, deckt also die komplette Karte auf
     */
    @Override
    public void disableFoW() {
    }

    @Override
    public void defeated() {
    }

    @Override
    public void win() {
    }

    @Override
    public void done() {
    }

    @Override
    public boolean clickedInSel(final int button, final int x, final int y, int clickCount) {
        return false;
    }

    @Override
    public boolean clickedInOpt(int x, int y) {
        return false;
    }

    @Override
    public boolean clickedInOpt(final int button, final int x, final int y, final int clickCount) {
        return false;
    }

    /*
     * Fügt ein Bullet zur Grafikengine hinzu.
     * Mehr muss nicht getan werden, Animation und Schaden werden automatisch berechnet, solange die Grafik läuft.
     * Nur scheduling, wird erst eingefügt, wenn gerade kein Frame gerendert wird.
     */
    @Override
    public void addBullet(GraphicsBullet b) {
    }

    @Override
    public void addBulletB(GraphicsBullet b) {
    }

    @Override
    public void startRendering() {
    }

    @Override
    public void jumpTo(int scrollX, int scrollY) {
    }

    @Override
    public Dimension getPosition() {
        return null;
    }

    @Override
    public boolean isInSight(int vX, int vY) {
        return false;
    }

    @Override
    public void refreshMap() {
    }

    @Override
    public void updateUnits(List<Unit> nL) {
    }

    @Override
    public void updateBuildings(List<Building> nL) {
    }

    @Override
    public void showCalculatedRoute(ArrayList<Position> path) {
    }

    @Override
    public void displayError(String s) {
    }

    @Override
    public void displayWarning(String s) {
    }

    @Override
    public ArrayList<Unit> getBoxSelected(final int button, final int x, final int y) {
        return null;

    }

    @Override
    public void startSelectionBox(int button, int x, int y) {
    }

    @Override
    public void stopSelectionBox() {
    }

    @Override
    public void startRightScrolling() {
    }

    @Override
    public void stopRightScrolling() {
    }

    private void manageRightScrolling() {
    }

    @Override
    public void builingsChanged() {
    }

    /*
     * Setzt das Dauerhaft anzeigen der Energiebalken, wie man es aus Warcraft kennt
     * @param boolean b Balken anzeigen (true) oder nicht (false)
     */
    @Override
    public void setAlwaysShowEnergyBars(boolean b) {
    }

    /**
     * Zeigt Infos über Einheiten / Gebäude / Ressourcen an, die einem nicht gehören.
     * Funktioniert nur, solange selected leer ist.
     * Ansonsten wird dieses hier sofort gelöscht.
     * @param das zu zeigende Objekt.
     */
    @Override
    public void triggerTempStatus(GameObject obj) {
    }

    @Override
    public void notifyUnitDieing(final Unit unit) {
    }

    @Override
    public void notifyBuildingDieing(final Building building) {
    }

    // Lagert eine beliebige Aktivität in einen extra Thread aus
    private void doLater(Runnable r, String name) {
    }

    /**
     * Lässt die Grafik den interaktiven Teil des Huds neu zeichnen
     * Geschieht sofort mit dem nächsten Frame
     *
     */
    @Override
    public void triggerUpdateHud() {
    }

    @Override
    public void triggerRefreshFow() {
    }

    /**
     * Aufrufen, wenn sich die Epoche geändert hat.
     */
    @Override
    public void epocheChanged() {
    }

    /*
     * Setzt das Dauerhaft anzeigen der Energiebalken, wie man es aus Warcraft kennt
     * @return boolean Balken anzeigen (true) oder nicht (false)
     */
    @Override
    public boolean getAlwaysShowEnergyBars() {
        return false;
    }

    @Override
    public void renderAndCalc(GameContainer c, Graphics g) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }

    @Override
    public long getPauseTime() {
        return 0;
    }

    @Override
    public void showstatistics() {
    }
}
