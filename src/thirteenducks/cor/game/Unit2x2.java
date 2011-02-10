/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.game;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.game.server.ServerCore.InnerServer;
import thirteenducks.cor.graphics.GraphicsImage;
import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.graphics.input.InteractableGameElement;
import thirteenducks.cor.graphics.input.SelectionMarker;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;

/**
 *
 * @author tfg
 */
public class Unit2x2 extends Unit {

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

     public Unit2x2(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
        positions = new Position[4];
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
        positions = new Position[4];
    }

    @Override
    public void setMainPosition(Position mainPosition) {
        super.setMainPosition(mainPosition);
        positions[0] = mainPosition;
        positions[1] = new Position(mainPosition.getX() + 1, mainPosition.getY() - 1);
        positions[2] = new Position(mainPosition.getX() + 1, mainPosition.getY() + 1);
        positions[3] = new Position(mainPosition.getX() + 2, mainPosition.getY());
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
    public void renderSprite(Graphics g, int x, int y, Map<String, GraphicsImage> imgMap) {
        GraphicsImage img = imgMap.get(getGraphicsData().defaultTexture);
        if (img != null) {
            img.getImage().draw(x, y);
        } else {
            System.out.println("RENDER: Can't paint unit, texture <" + getGraphicsData().defaultTexture + "> not found!");
        }
    }

    @Override
    public Position[] getVisisbilityPositions() {
        return positions;
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
    public boolean selPosChanged() {
        return (!Arrays.equals(positions, lastPositions));

    }

    @Override
    public SelectionMarker getSelectionMarker() {
        SelectionMarker marker = new SelectionMarker(this, lastPositions, positions);
        lastPositions = positions;
        return marker;
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
    public Position getSortPosition() {
        return this.getMainPosition();
    }

    @Override
    public void renderGroundEffect(Graphics g, int x, int y, Map<String, GraphicsImage> imgMap) {
        //Einheit gehört zu / Selektiert
        if (isSelected()) {
            // Weiße Bodenmarkierung
            imgMap.get("img/game/sel_s2.png0").getImage().draw(x, y);
        } else {
            // Spielerfarbe
            imgMap.get("img/game/sel_s2.png" + getPlayerId()).getImage().draw(x, y);
        }                
    }
}
