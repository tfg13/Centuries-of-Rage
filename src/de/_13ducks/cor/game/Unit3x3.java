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
package de._13ducks.cor.game;

import de._13ducks.cor.map.fastfindgrid.Cell;
import java.util.Arrays;
import java.util.List;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.graphics.GraphicsContent;
import de._13ducks.cor.graphics.Renderer;
import de._13ducks.cor.graphics.input.SelectionMarker;
import de._13ducks.cor.networks.client.behaviour.ClientBehaviour;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Eine echte Einheit mit 3 x 3 Feldern Grundfläche
 */
public class Unit3x3 extends Unit {

    /**
     * Die Positionen auf denen die Einheit derzeit sichtbar ist.
     * Wird aus Performance-Gründen gecached.
     */
    private Position[] positions;
    /**
     * Wird vom Selektionsmechanismus verwaltet.
     * Speichert die zuletzt an das Inputmodul übergebene Position.
     */
    private Position[] lastPositions;
    /**
     * Die Zelle des SChnellsuchrasters, in der sich die EInheit befindet
     */
    Cell myCell;

    public Unit3x3(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
        positions = new Position[9];
    }

    /**
     * Erzeugt eine Platzhalter-Einheit, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    public Unit3x3(DescParamsUnit params) {
        super(params);
    }

    /**
     * Erzeugt eine neue Einheit als eigenständige Kopie der Übergebenen.
     * Wichtige Parameter werden kopiert, Sachen die jede Einheit selber haben sollte nicht.
     * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
     * @param newNetId Die netId der neuen Einheit
     * @param copyFrom Die Einheit, dessen Parameter kopiert werden sollen
     */
    public Unit3x3(int newNetId, Unit3x3 copyFrom) {
        super(newNetId, copyFrom);
        positions = new Position[9];
    }

    @Override
    public void setMainPosition(Position mainPosition) {
        super.setMainPosition(mainPosition);
        Position clickPosition;
        try {
            
            clickPosition = mainPosition.clone();
            if (clickPosition.getX() % 2 != clickPosition.getY() % 2) {
                clickPosition.setX(clickPosition.getX() - 1);
            }
            int counter = 0;
            for (int z1c = 0; z1c < 3; z1c++) {
                for (int z2c = 0; z2c < 3; z2c++) {
                    positions[counter++] = new Position(clickPosition.getX() + z1c + z2c, clickPosition.getY() - z1c + z2c);
                }
            }
            
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Unit3x3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Position freeDirectAroundMe() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAroundMe(Position pos) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position[] getPositions() {
        return positions;
    }

    @Override
    public GameObject getCopy(int newNetId) {
        return new Unit3x3(newNetId, this);
    }

    @Override
    public void renderSprite(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor) {
        float rx = (float) ((FloatingPointPosition) mainPosition).getfX();
        float ry = (float) ((FloatingPointPosition) mainPosition).getfY();
        Renderer.drawImage(getGraphicsData().defaultTexture, (float) (rx * GraphicsContent.FIELD_HALF_X + GraphicsContent.OFFSET_3x3_X - scrollX + GraphicsContent.OFFSET_PRECISE_X), (float) (ry * GraphicsContent.FIELD_HALF_Y + GraphicsContent.OFFSET_3x3_Y - scrollY + GraphicsContent.OFFSET_PRECISE_Y));
        hovered = false;
    }

    @Override
    public void renderGroundEffect(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor) {
        clientManager.externalExecute();
        //Einheit gehört zu / Selektiert
        float rx = (float) (((FloatingPointPosition) mainPosition).getfX() * GraphicsContent.FIELD_HALF_X + GraphicsContent.OFFSET_3x3_X - scrollX + GraphicsContent.OFFSET_PRECISE_X);
        float ry = (float) (((FloatingPointPosition) mainPosition).getfY() * GraphicsContent.FIELD_HALF_Y  + GraphicsContent.OFFSET_3x3_Y - scrollY + GraphicsContent.OFFSET_PRECISE_Y);
        if (isSelected()) {
            if (hovered) {
                // Weißer gefüllter Kreis
                Renderer.drawImage("img/game/sel_s3_fill.png0", rx, (float) (ry));
            }
            // Weißer Kreisrand
            Renderer.drawImage("img/game/sel_s3.png0", rx, ry);
        } else {
            if (hovered) {
                // gefüllter Kreis in Spielerfarbe
                Renderer.drawImage("img/game/sel_s3_fill.png" + getPlayerId(), rx, ry);
            }
            // Kreisrand in Spielerfarbe
            Renderer.drawImage("img/game/sel_s3.png" + getPlayerId(), rx, ry);
        }
    }

    @Override
    public Position[] getVisisbilityPositions() {
        return positions;
    }

    @Override
    public Position getSortPosition() {
        return this.getMainPosition();
    }

    @Override
    public boolean gotClientBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean gotServerBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ClientBehaviour> getClientBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<ServerBehaviour> getServerBehaviours() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean selPosChanged() {
        return (!Arrays.equals(positions, lastPositions));
    }

    @Override
    public SelectionMarker getSelectionMarker() {
        SelectionMarker marker = new SelectionMarker(this, lastPositions, positions);
        lastPositions = positions.clone();
        return marker;
    }

    @Override
    public void keyCommand(int key, char character) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void renderMinimapMarker(Graphics g, int x, int y, Color spriteColor) {
        g.setColor(spriteColor);
        g.fillRect(x, y, 3, 3);
    }

    @Override
    public double getRadius() {
        return 3;
    }

    @Override
    public void renderSkyEffect(Graphics g, int x, int y, double scrollX, double scrollY, Color spriteColor) {
        if (isSelected() || (GraphicsContent.alwaysshowenergybars && getLifeStatus() != GameObject.LIFESTATUS_DEAD)) {
            SimplePosition pos = getPrecisePosition();
            // Billigen Balken rendern
            g.setColor(Color.black);
            g.fillRect((float) (pos.x() * GraphicsContent.FIELD_HALF_X - scrollX) - 6, (float) (pos.y() * GraphicsContent.FIELD_HALF_Y - scrollY) - 16, 7, 7);
            // Farbe bestimmen
            double percent = 1.0 * getHitpoints() / getMaxhitpoints();
            if (percent >= 0.3) {
                g.setColor(new Color((int) (255 - (((percent - 0.5) * 2) * 255)), 255, 0));
            } else {
                g.setColor(new Color(255, (int) ((percent * 2) * 255), 0));
            }
            g.fillRect((float) (pos.x() * GraphicsContent.FIELD_HALF_X - scrollX) - 5, (float) (pos.y() * GraphicsContent.FIELD_HALF_Y - scrollY) - 15, 5, 5);
        }
    }
}
