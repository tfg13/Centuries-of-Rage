/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.Building;
import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Moveable;
import de._13ducks.cor.game.Position;
import de._13ducks.cor.game.Unit;
import de._13ducks.cor.game.server.Server;
import de._13ducks.cor.game.server.ServerCore;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import java.util.List;

/**
 *
 * @author 2nd
 */
public class ServerBehaviourCapture extends ServerBehaviour {

    GameObject caster2;

    public ServerBehaviourCapture(ServerCore.InnerServer newinner, GameObject caster) {
        super(newinner, caster, 7, 1, true);
        caster2 = caster;
    }

    @Override
    public void activate() {
        this.active = true;
    }

    @Override
    public void deactivate() {
        this.active = false;
    }

    @Override
    public void execute() {        
        // Gebäudemitte suchen
        Position Mitte1 = caster2.getCentralPosition();
        FloatingPointPosition MitteFP = new FloatingPointPosition(Mitte1);

        boolean captureblocked = false; // Wenn eigene Einheit in Nähe des Gebäudes ist
        int capturenumber = 0; // Anzahl der Gegner in der Nähe

        // Unit1en in Umgebung suchen
        List<Moveable> movables = Server.getInnerServer().moveMan.moveMap.moversAroundPoint(MitteFP, 30, null);

        int cappingplayerid = 0;
        
        for (int i = 0; i < movables.size(); i++) {
            GameObject go = movables.get(i).getAttackable();
            if (go instanceof Unit) {
                Unit unit = (Unit) go;
                // Überhaupt richtig existierend?
                if (unit != null && unit.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
                    // Eigener Spieler?
                    if (caster2.getPlayerId() != unit.getPlayerId()) {
                        // Gegnerische Einheit -> Cappen
                        capturenumber++;
                        if (cappingplayerid != 0 && cappingplayerid != unit.getPlayerId()) {
                            //verschiedene Spieler sind da -> keiner cappt
                            captureblocked = true;
                        }
                        cappingplayerid = unit.getPlayerId();
                    } else {
                        // Eigener Spieler -> Capture blockieren
                        captureblocked = true;
                    }
                }
            }
        }
        if (caster2 instanceof Building) {
            Building building = (Building) caster2;
            if (captureblocked || capturenumber == 0) {
                // Gebäude wird nicht eingenommen
                if (building.getCaptureProgress() > 0) {
                    // captureprogress veringern
                    building.changeCaptureProgress(-1, cappingplayerid);
                }
            } else {
                // Gebäude wird erobert
                building.changeCaptureProgress(capturenumber, cappingplayerid);
            }
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
        throw new UnsupportedOperationException("The Voices are talking to me.");
    }

    @Override
    public void pause() {
    }

    @Override
    public void unpause() {
    }
}
