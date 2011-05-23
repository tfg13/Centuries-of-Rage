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
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.FloatingPointPosition;
import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Moveable;
import de._13ducks.cor.game.SimplePosition;
import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
import de._13ducks.cor.game.server.ServerCore;

/**
 * Lowlevel-Movemanagement
 * 
 * Verwaltet die reine Bewegung einer einzelnen Einheit.
 * Kümmert sich nicht weiter um Formationen/Kampf oder ähnliches.
 * Erhält vom übergeordneten MidLevelManager einen Richtungsvektor und eine Zielkoordinate.
 * Läuft dann dort hin. Tut sonst nichts.
 * Hat exklusive Kontrolle über die Einheitenposition.
 * Weigert sich, sich schneller als die maximale Einheitengeschwindigkeit zu bewegen.
 * Dadurch werden Sprünge verhindert.
 * 
 *      * Der Client lässt Einheiten auf konkrete Ziele zu laufen,
 * während der Server Einheiten anhand zweier Vektoren bewegt (direction und drift),
 * und auf ein variables Ziel in Form einer Linie zu läuft.
 */
public class ServerBehaviourMove extends ServerBehaviour {

    /**
     * Der von diesem Behaviour bewegte mover
     */
    private Moveable caster2;
    /**
     * Das Ziel, das der Client zurzeit ansteuert.
     */
    private SimplePosition clientTarget;
    /**
     * Das eigentliche, ursprüngliche Ziel.
     */
    private SimplePosition basicTarget;
    /**
     * Die Linie, auf der die Einheit stehen bleiben wird.
     */
    private Edge targetLine;
    /**
     * Die derzeitige Einheitengeschwindigkeit
     */
    private double speed;
    /**
     * Ermöglicht sofortiges stoppen
     */
    private boolean stopUnit = false;
    /**
     * Wann zuletzt eine Berechnung durchgeführt wurde
     */
    private long lastTick;
    /**
     * Der Richtungsvektor dieser Einheit, in diese Richtung läuft die Einheit
     */
    private Vector directionVector;
    /**
     * Der Driftvektor, kann während der Bewegung geändert werden, ohne dass die Einheit Schaden nimmt
     */
    private Vector driftVector;
    /**
     * Die Movemap, die alles verwaltet
     */
    private MovementMap moveMap;

    public ServerBehaviourMove(ServerCore.InnerServer newinner, GameObject caster1, Moveable caster2, MovementMap moveMap) {
        super(newinner, caster1, 1, 20, true);
        this.caster2 = caster2;
        this.moveMap = moveMap;
    }

    @Override
    public void activate() {
        active = true;
        trigger();
    }

    @Override
    public void deactivate() {
        active = false;
    }

    @Override
    public synchronized void execute() {
        // Auto-Ende:
        if (targetLine == null || speed <= 0) {
            deactivate();
            return;
        }
        // Wir laufen also.
        // Alte Position
        FloatingPointPosition oldPos = caster2.getPrecisePosition();
        // Richtung berechnen:
        Vector vec = directionVector.normalize(); // Kopiert den Vector
        // Drift?
        if (driftVector != null) {
            vec.addToMe(driftVector);
        }
        // Auf Länge eins
        vec.normalizeMe();
        // Mit Zeitfaktor verrechnen (also den zurückgelegten Weg berechnen)
        long ticktime = System.currentTimeMillis();
        vec.multiplyMe((ticktime - lastTick) / 1000.0 * speed);
        // Neue Position bestimmen
        FloatingPointPosition newPos = vec.toFPP().add(oldPos);
        // Ziellinie überquert?
        Edge thisMove = new Edge(oldPos.toNode(), newPos.toNode());
        SimplePosition intersection = targetLine.endlessIntersection(thisMove);
        
        if (thisMove.partOf(intersection)) {
            // Ziel erreicht!
            driftVector = null;
            directionVector = null;
            // Ziel setzen
            caster2.setMainPosition(intersection.toFPP());
            // Altes Ziel backupen
            SimplePosition oldTar = basicTarget;
            // Achtung: Benötigt eventuell Anpassungen!
            if (!caster2.getMidLevelManager().reachedTarget(caster2)) {
                // Wenn das false gibt, gibts keine weiteren, dann hier halten.
                basicTarget = null;
                stopUnit = false; // Es ist wohl besser auf dem Ziel zu stoppen als kurz dahinter!
                deactivate();
            } else {
                // Herausfinden, ob der Sektor gewechselt wurde

                SimplePosition newTar = basicTarget;
                if (newTar instanceof Node && oldTar instanceof Node) {
                    // Nur in diesem Fall kommt ein Sektorwechsel in Frage
                    FreePolygon sector = commonSector((Node) newTar, (Node) oldTar);
                    // Sektor geändert?
                    if (!sector.equals(caster2.getMyPoly())) {
                        caster2.setMyPoly(sector);
                    }
                }
            }
        } else {
            // Wir laufen noch.
            // Sofort stoppen?
            if (stopUnit) {
                // Der Client muss das auch mitbekommen
                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 24, caster2.getNetID(), 0, Float.floatToIntBits((float) newPos.getfX()), Float.floatToIntBits((float) newPos.getfY())));
                caster2.setMainPosition(newPos);
                basicTarget = null;
                driftVector = null;
                directionVector = null;
                stopUnit = false;
                deactivate();
            } else {
                // Weiterlaufen
                // Muss dem Client eine Änderung mitgeteilt werden?
                if (!intersection.equals(clientTarget)) {
                    rgi.netctrl.broadcastMoveVec(caster2.getNetID(), intersection.toFPP(), speed);
                    clientTarget = intersection;
                }
                caster2.setMainPosition(newPos);
                lastTick = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void gotSignal(byte[] packet) {
    }

    @Override
    public void pause() {
        caster2.pause();
    }

    @Override
    public void unpause() {
        caster2.unpause();
    }

    /**
     * Setzt den Zielvektor für diese Einheit.
     * Es wird nicht untersucht, ob das Ziel in irgendeiner Weise ok ist, die Einheit beginnt sofort loszulaufen.
     * In der Regel sollte noch eine Geschwindigkeit angegeben werden.
     * Wehrt sich gegen nicht existente Ziele.
     * @param pos die Zielposition, wird auf direktem Weg angesteuert.
     */
    public synchronized void setTargetVector(SimplePosition pos) {
        if (pos == null) {
            throw new IllegalArgumentException("Cannot send " + caster2 + " to null");
        }
        basicTarget = pos;
        lastTick = System.currentTimeMillis();
        directionVector = pos.toFPP().subtract(caster2.getPrecisePosition()).toVector().normalize();
        // Neue targetLine berechnen:
        Vector direction = new Vector(-directionVector.y(), directionVector.x());
        targetLine = new Edge(basicTarget.toFPP().add(direction.multiply(50000).toFPP()).toNode(), basicTarget.toFPP().subtract(direction.multiply(50000).toFPP()).toNode());
        activate();
    }

    /**
     * Setzt den Zielvektor und die Geschwindigkeit und startet die Bewegung sofort.
     * @param pos die Zielposition
     * @param speed die Geschwindigkeit
     */
    public synchronized void setTargetVector(SimplePosition pos, double speed) {
        changeSpeed(speed);
        setTargetVector(pos);
    }

    /**
     * Ändert die Geschwindigkeit während des Laufens.
     * Speed wird verkleinert, wenn der Wert über dem Einheiten-Maximum liegen würde
     * @param speed Die Einheitengeschwindigkeit
     */
    public synchronized void changeSpeed(double speed) {
        if (speed > 0 && speed <= caster2.getSpeed()) {
            this.speed = speed;
        }
        trigger();
    }

    /**
     * Setzt den Driftvektor. Gilt bis zum Sektorwechsel bzw. bis ein Ziel erreicht wurde.
     * @param driftVector 
     */
    public synchronized void setDrift(Vector driftVector) {
        if (isMoving()) {
            System.out.println("Drift set to " + driftVector);
            this.driftVector = driftVector;
            trigger();
        }
    }

    public boolean isMoving() {
        return basicTarget != null;
    }

    /**
     * Stoppt die Einheit innerhalb eines Ticks.
     */
    public synchronized void stopImmediately() {
        stopUnit = true;
        trigger();
    }

    /**
     * Findet einen Sektor, den beide Knoten gemeinsam haben
     * @param n1 Knoten 1
     * @param n2 Knoten 2
     */
    private FreePolygon commonSector(Node n1, Node n2) {
        for (FreePolygon poly : n1.getPolygons()) {
            if (n2.getPolygons().contains(poly)) {
                return poly;
            }
        }
        return null;
    }
}
