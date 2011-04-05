/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package thirteenducks.cor.game;

import java.util.List;
import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.client.ClientCore.InnerClient;
import thirteenducks.cor.graphics.GraphicsImage;
import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
import thirteenducks.cor.graphics.input.InteractableGameElement;
import thirteenducks.cor.networks.client.behaviour.ClientBehaviour;

/**
 *
 * @author Johannes
 */
public class NeutralBuilding extends Building {

    /**
     * Erzeugt ein neues Gebäude mit den gegebenen Parametern.
     * Default-Konstruktor, erzeugt im Spiel vollständig nutzbare Gebäude
     * @param newNetId die netID für dieses Gebäude
     * @param mainPos die Zuordnungsposition
     */
    public NeutralBuilding(int newNetId, Position mainPos) {
        super(newNetId, mainPos);
        z1 = 12;
        z2 = 12;
        positions = new Position[z1 * z2];
    }

    /**
     * Erzeugt ein neues Gebäude als eigenständige Kopie des Übergebenen.
     * Wichtige Parameter werden kopiert, Sachen die jedes Gebäude selber haben sollte nicht.
     * Wichtig: Die Position muss noch gesetzt werden, die ist Anfangs 0,0
     * @param newNetId Die netId des neuen Gebäudes
     * @param copyFrom Das Gebäude, dessen Parameter kopiert werden sollen
     */
    public NeutralBuilding(int newNetId, NeutralBuilding copyFrom) {
        super(newNetId, copyFrom);
    }

    @Override
    public Position freeDirectAroundMe() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Position getCentralPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GameObject getCopy(int newNetId) {
        return new NeutralBuilding(newNetId, this);
    }

    @Override
    public void kill() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void renderSprite(Graphics g, int x, int y, Map<String, GraphicsImage> imgMap, Color spriteColor) {
        imgMap.get(getGraphicsData().defaultTexture).getImage().draw(x - getGraphicsData().offsetX, (int) (y - getGraphicsData().offsetY));
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
        return true;
    }

    @Override
    public boolean isSelectableByPlayer(int playerId) {
        return playerId == this.getPlayerId();
    }

    @Override
    public boolean isMultiSelectable() {
        return false;
    }

    @Override
    public void command(int button, List<InteractableGameElement> targets, boolean doubleKlick, ClientCore.InnerClient rgi) {
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
    public void renderGroundEffect(Graphics g, int x, int y, Map<String, GraphicsImage> imgMap, Color spriteColor) {
    }

    @Override
    public int getColorId() {
        return getPlayerId();
    }

    @Override
    public void command(int button, Position target, List<InteractableGameElement> repeaters, boolean doubleKlick, InnerClient rgi) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void renderMinimapMarker(Graphics g, int x, int y, Color spriteColor) {
	g.setLineWidth(2);
	g.setColor(Color.gray);
	g.drawRect(x, y, 5, 5);
    }
}

