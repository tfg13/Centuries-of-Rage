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
import de._13ducks.cor.game.Moveable;
import de._13ducks.cor.game.SimplePosition;
import de._13ducks.cor.game.server.ServerPathfinder;
import java.util.ArrayList;
import java.util.List;

/**
 * MidLevel-Movemanagement
 * 
 * Verwaltet die Positionierung der einzelnen Einheiten innerhalb einer kleineren Gruppe.
 * Diese entscheiden, wie sich Einheitengruppen vor dem Gegner aufstellen,
 * wie sie sich während dem Kampf verhalten, und ob sie davor und danach
 * eine Formation einnehmen.
 * Für jede Gruppe existiere ein GroupManager.
 * Dieser verwaltet die Pfade und Ziele seiner Einheiten.
 * Die tatsächliche Bewegung unterliegt der exklusiven Kontrolle des LowLevelManagers der
 * einzelnen Einheiten. Der GroupManager kann nur eine Richtung und ein Ziel vorgeben.
 */
public class GroupManager {

    /**
     * Alle Einheiten, die zur Zeit in dieser Gruppe sind.
     */
    private ArrayList<GroupMember> myMovers;
    /**
     * Die aktuelle MovementMap
     */
    private MovementMap moveMap;

    public GroupManager(MovementMap moveMap) {
        myMovers = new ArrayList<GroupMember>();
        this.moveMap = moveMap;
    }

    /**
     * Löscht eine Einheit aus der Gruppe heraus.
     * Wenn sie gar nicht drin war, passiert nichts.
     * @param mover die zu löschende Einheit
     */
    public synchronized void remove(Moveable mover) {
        myMovers.remove(new GroupMember(mover));
    }

    /**
     * Fügt die Einheit zu dieser Gruppe hinzu, falls sie noch nicht drin ist.
     * @param mover
     */
    public synchronized void add(Moveable mover) {
        GroupMember tempmem = new GroupMember(mover);
        if (!myMovers.contains(tempmem)) {
            myMovers.add(tempmem);
        }
    }

    /**
     * Lässt die Gruppe an dieses Ziel laufen.
     * Laufen bedeutet aggressives Vorrücken.
     * Alle Einheiten laufen mit der gleichen Geschwindigkeit.
     * runTo aufrufen, für nicht-aggressives Vorrücken, jeder so schnell wie er kann.
     * @param target
     */
    public synchronized void goTo(FloatingPointPosition target) {
        // Route planen
        List<Node> tmpPath = ServerPathfinder.findPath(myMovers.get(0).getMover().getPrecisePosition(), target, myMovers.get(0).getMover().getMyPoly(), moveMap);
        FloatingPointPosition targetVector = target.subtract(tmpPath.get(tmpPath.size() - 2).toFPP()); // Der vektor vom vorletzten Wegpunkt zum Ziel, entspricht der Richtung die die Formation haben soll

        FloatingPointPosition targetFormation[] = Formation.createSquareFormation(myMovers.size(), target, targetVector, 5.0);

        // Niedrigste Geschwindigkeit suchen
        double commonSpeed = lowestSpeed(myMovers);


        int i = 0;

        for (GroupMember member : myMovers) {
            System.out.println("Moving " + member.getMover() + " from " + member.getMover().getPrecisePosition() + " to " + target);
            List<Node> path = ServerPathfinder.findPath(member.getMover().getPrecisePosition(), target.add(targetFormation[i]), member.getMover().getMyPoly(), moveMap);
            if (path != null) {
                List<SimplePosition> optiPath = ServerPathfinder.optimizePath(path, member.getMover().getPrecisePosition(), target, moveMap);
                if (optiPath != null) {
                    // Einheite auf IDLE setzen (falls die Einheit kämpfen kann)
                    if (member.getMover().getAtkManager() != null) {
                        member.getMover().getAtkManager().newMoveMode(ServerBehaviourAttack.MOVEMODE_GOTO);
                    }

                    // Weg setzen
                    for (SimplePosition node : optiPath) {
                        member.addWaypoint(node);
                    }
                    // Loslaufen lassen
                    member.getMover().getLowLevelManager().setTargetVector(member.popWaypoint(), commonSpeed);
                }
            }
            i++;
        }
    }

    /**
     * Lässt die Gruppe an dieses Ziel rennen.
     * Rennen bedeutet flüchten, alle Feinde ignorieren, das Ziel um jeden Preis erreichen.
     * Jede Einheit läuft mit ihrer individuellen Maximalgeschwindigkeit
     * goTo aufrufen, für geordnetes, aggressives Vorrücken
     * @param target
     */
    public synchronized void runTo(FloatingPointPosition target) {
        // Route planen
        List<Node> tmpPath = ServerPathfinder.findPath(myMovers.get(0).getMover().getPrecisePosition(), target, myMovers.get(0).getMover().getMyPoly(), moveMap);
        FloatingPointPosition targetVector = target.subtract(tmpPath.get(tmpPath.size() - 2).toFPP());

        FloatingPointPosition targetFormation[] = Formation.createSquareFormation(myMovers.size(), target, targetVector, 5.0);

        // Jeder rennt mit Fullspeed, suchen eines common-Speeds ist nicht erforderlich

        int i = 0;

        for (GroupMember member : myMovers) {
            System.out.println("Moving " + member.getMover() + " from " + member.getMover().getPrecisePosition() + " to " + target);
            List<Node> path = ServerPathfinder.findPath(member.getMover().getPrecisePosition(), target.add(targetFormation[i]), member.getMover().getMyPoly(), moveMap);
            if (path != null) {
                List<SimplePosition> optiPath = ServerPathfinder.optimizePath(path, member.getMover().getPrecisePosition(), target, moveMap);
                if (optiPath != null) {
                    // Einheite auf IDLE setzen (falls die Einheit kämpfen kann)
                    if (member.getMover().getAtkManager() != null) {
                        member.getMover().getAtkManager().newMoveMode(ServerBehaviourAttack.MOVEMODE_RUNTO);
                    }

                    // Weg setzen
                    for (SimplePosition node : optiPath) {
                        member.addWaypoint(node);
                    }
                    // Loslaufen lassen
                    member.getMover().getLowLevelManager().setTargetVector(member.popWaypoint(), member.getMover().getSpeed());
                }
            }
            i++;
        }
    }

    /**
     * Eine LowLevelManager hat sein Wegziel erreicht und will wissen, wie die Route weitergeht
     * Gibt false zurück wenns nicht weitergeht und liefert true zurück, wenns weiter geht und das
     * neue Ziel schon gesetzt wurde.
     * @return true, wenn neues Ziel gesetzt sonst false
     */
    public boolean reachedTarget(Moveable mover) {
        GroupMember member = memberForMover(mover);
        SimplePosition nextPoint = member.popWaypoint();
        if (nextPoint != null) {
            mover.getLowLevelManager().setTargetVector(nextPoint);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Ein LowLevelManager hat ein Hindernis auf seinem Weg und will wissen, was er tun soll
     * Gibt true zurück, wenn der LowLevelManager warten soll, oder false wenn ein Ausweichziel gesetzt wurde
     * @param mover - der LowLevelManager, der die Kollision festgestellt hat
     * @param obstacle - Das Obnjekt, mit dem der LowlevelManager kollidiert
     */
    public boolean collisionDetected(Moveable mover, Moveable obstacle) {
        // TODO asuweichziel berechnen oder wartebefehl geben
        return true;
    }

    /**
     * Sucht den GroupMember zu einem Mover raus
     * @param mover
     * @return 
     */
    private GroupMember memberForMover(Moveable mover) {
        for (GroupMember member : myMovers) {
            if (member.getMover().equals(mover)) {
                return member;
            }
        }
        return null;
    }

    private double lowestSpeed(ArrayList<GroupMember> myMovers) {
        double lowestSpeed = myMovers.get(0).getMover().getSpeed();
        for (int i = 1; i < myMovers.size(); i++) {
            double nextSpeed = myMovers.get(i).getMover().getSpeed();
            if (nextSpeed < lowestSpeed) {
                lowestSpeed = nextSpeed;
            }
        }
        return lowestSpeed;
    }
}
