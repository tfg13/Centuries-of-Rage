/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.game;

import java.util.List;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.graphics.Sprite;
import thirteenducks.cor.graphics.input.InteractableGameElement;
import thirteenducks.cor.graphics.input.SelectionMarker;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;

/**
 *
 * @author tfg
 */
public class Unit2x2 extends Unit {

     public Unit2x2(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
    }

    /**
     * Erzeugt eine Platzhalter-Einheit, das nicht direkt im Spiel verwendet werden kann, aber als Platzhalter für
     * Attribute und Fähigkeiten dient.
     */
    public Unit2x2(DescParamsUnit params) {
        super(params);
    }

    /**
     * Erzeugt eine neue Einheit als eigenständige Kopie der Übergebenen.
     * Wichtige Parameter werden kopiert, Sachen die jede Einheit selber haben sollte nicht.
     * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
     * @param newNetId Die netId der neuen Einheit
     * @param copyFrom Die Einheit, dessen Parameter kopiert werden sollen
     */
    public Unit2x2(int newNetId, Unit2x2 copyFrom) {
        super(newNetId, copyFrom);
    }

    @Override
    public void moveToBuilding(Building building, InnerServer inner) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position getCentralPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GameObject getCopy(int newNetId) {
        return new Unit2x2(newNetId, this);
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
    public void renderSprite(Graphics g, int x, int y) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position[] getVisisbilityPositions() {
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean renderInHalfFog() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean renderInNullFog() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position getSortPosition() {
        return this.getMainPosition();
    }

}
