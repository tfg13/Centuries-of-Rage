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
package thirteenducks.cor.game;

import java.util.List;
import java.util.Map;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.graphics.GraphicsImage;
import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.graphics.input.InteractableGameElement;
import thirteenducks.cor.graphics.input.SelectionMarker;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;

/**
 * Nicht-Neutrales Spielergebäude
 * 
 * @author tfg
 */
public class PlayersBuilding extends Building {


    /**
     * Erzeugt ein neues Gebäude mit den gegebenen Parametern.
     * Default-Konstruktor, erzeugt im Spiel vollständig nutzbare Gebäude
     * @param newNetId die netID für dieses Gebäude
     * @param mainPos die Zuordnungsposition
     */
    public PlayersBuilding(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
    }

    /**
     * Erzeugt ein Platzhalter-Gebäude, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    public PlayersBuilding(DescParamsBuilding params) {
        super(params);
    }

    /**
     * Erzeugt ein neues Gebäude als eigenständige Kopie des Übergebenen.
     * Wichtige Parameter werden kopiert, Sachen die jedes Gebäude selber haben sollte nicht.
     * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
     * @param newNetId Die netId des neuen Gebäudes
     * @param copyFrom Das Gebäude, dessen Parameter kopiert werden sollen
     */
    public PlayersBuilding(int newNetId, PlayersBuilding copyFrom) {
        super(newNetId, copyFrom);
    }

    @Override
    public Position freeDirectAroundMe() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position[] getPositions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position getCentralPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GameObject getCopy(int newNetId) {
        return new PlayersBuilding(newNetId, this);
    }

    @Override
    public void kill() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position getSpawnPosition(GameObject obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void renderSprite(Graphics g, int x, int y, Map<String, GraphicsImage> imgMap) {
        imgMap.get(getGraphicsData().defaultTexture).getImage().draw(x - (getGraphicsData().offsetX * 10), (int) (y - (getGraphicsData().offsetY * 7.5)));
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unpause() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public void mouseHovered() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean selectable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean selPosChanged() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SelectionMarker getSelectionMarker() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSelectableByPlayer(int playerId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isMultiSelectable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void command(int button, List<InteractableGameElement> targets, boolean doubleKlick) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void command(int button, Position target, boolean doubleKlick) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyCommand(int key, char character) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean renderInFullFog() {
        return false;
    }

    @Override
    public boolean renderInHalfFog() {
        //@TODO: Gebäude müssen sichtbar bleiben, wenn man sie einmal gesehen hat.
        return false;
    }

    @Override
    public boolean renderInNullFog() {
        return true;
    }

    @Override
    public Position getSortPosition() {
        return this.getMainPosition();
    }
}
