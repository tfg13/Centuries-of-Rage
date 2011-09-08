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
import de._13ducks.cor.game.server.Server;
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
            mover.getLowLevelManager().setPathManager(tempmem);
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
        List<Node> tmpPath = SectorPathfinder.findPath(myMovers.get(0).getMover().getPrecisePosition(), target, Server.getInnerServer().moveMan.moveMap.containingPoly(myMovers.get(0).getMover().getPrecisePosition()), moveMap);
        if (tmpPath != null) {
            FloatingPointPosition targetVector = target.subtract(tmpPath.get(tmpPath.size() - 2).toFPP()); // Der vektor vom vorletzten Wegpunkt zum Ziel, entspricht der Richtung die die Formation haben soll

            FloatingPointPosition targetFormation[] = Formation.createSquareFormation(myMovers.size(), target, targetVector, 5.0);

            // Niedrigste Geschwindigkeit suchen
            double commonSpeed = lowestSpeed(myMovers);


            int i = 0;

            for (GroupMember member : myMovers) {
                System.out.println("Moving " + member.getMover() + " from " + member.getMover().getPrecisePosition() + " to " + target.add(targetFormation[i]));
                List<Node> path = SectorPathfinder.findPath(member.getMover().getPrecisePosition(), target.add(targetFormation[i]), Server.getInnerServer().moveMan.moveMap.containingPoly(member.getMover().getPrecisePosition()), moveMap);
                if (path != null) {
                    List<SimplePosition> optiPath = SectorPathfinder.optimizePath(path, member.getMover().getPrecisePosition(), target, moveMap);
                    if (optiPath != null) {
                        // Einheite auf IDLE setzen (falls die Einheit kämpfen kann)
                        if (member.getMover().getAtkManager() != null) {
                            member.getMover().getAtkManager().newMoveMode(ServerBehaviourAttack.MOVEMODE_GOTO);
                        }

                        // Weg setzen
                        member.newWay();
                        for (SimplePosition node : optiPath) {
                            member.addWaypoint(node);
                        }
                        // Loslaufen lassen
                        member.getMover().getLowLevelManager().setTargetVector(member.popWaypoint(), commonSpeed, false, false, null);
                    }
                }
                i++;
            }
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
        List<Node> tmpPath = SectorPathfinder.findPath(myMovers.get(0).getMover().getPrecisePosition(), target, Server.getInnerServer().moveMan.moveMap.containingPoly(myMovers.get(0).getMover().getPrecisePosition()), moveMap);
        FloatingPointPosition targetVector = target.subtract(tmpPath.get(tmpPath.size() - 2).toFPP());

        FloatingPointPosition targetFormation[] = Formation.createSquareFormation(myMovers.size(), target, targetVector, 5.0);

        // Jeder rennt mit Fullspeed, suchen eines common-Speeds ist nicht erforderlich

        int i = 0;

        for (GroupMember member : myMovers) {
            System.out.println("Moving " + member.getMover() + " from " + member.getMover().getPrecisePosition() + " to " + target.add(targetFormation[i]));
            List<Node> path = SectorPathfinder.findPath(member.getMover().getPrecisePosition(), target.add(targetFormation[i]), Server.getInnerServer().moveMan.moveMap.containingPoly(member.getMover().getPrecisePosition()), moveMap);
            if (path != null) {
                List<SimplePosition> optiPath = SectorPathfinder.optimizePath(path, member.getMover().getPrecisePosition(), target, moveMap);
                if (optiPath != null) {
                    // Einheite auf IDLE setzen (falls die Einheit kämpfen kann)
                    if (member.getMover().getAtkManager() != null) {
                        member.getMover().getAtkManager().newMoveMode(ServerBehaviourAttack.MOVEMODE_RUNTO);
                    }

                    // Weg setzen
                    member.newWay();
                    for (SimplePosition node : optiPath) {
                        member.addWaypoint(node);
                    }
                    // Loslaufen lassen
                    member.getMover().getLowLevelManager().setTargetVector(member.popWaypoint(), member.getMover().getSpeed(), false, false, null);
                }
            }
            i++;
        }
    }

    /**
     * Ein LowLevelManager hat ein Hindernis auf seinem Weg und will wissen, was er tun soll
     * Gibt true zurück, wenn der LowLevelManager warten soll, oder false wenn ein Ausweichziel gesetzt wurde
     * @param mover - der LowLevelManager, der die Kollision festgestellt hat
     * @param obstacle - Das Obnjekt, mit dem der LowlevelManager kollidiert
     */
    public boolean collisionDetected(Moveable mover, Moveable obstacle) {
        ServerBehaviourMove obstMove = obstacle.getLowLevelManager();
        if (obstMove.isMoving() && !obstMove.isWaiting()) {
            // Wenn die andere Einheit läuft auf jeden Fall selber warten.
            return true;
        } else if (obstMove.isMoving() && obstMove.isWaiting()) {
            // Andere Einheit wartet selbst. Dann sollten wir auch warten, es geht gleich weiter.
            return true;
        } else if (!obstMove.isMoving()) {
            return !tryDiversion(mover, obstacle, mover.getLowLevelManager().getPathManager().lastRealWaypoint());
        }

        return true;
    }

    /**
     * Versucht, den Mover auf einer Umleitung zum Ziel gelangen zu lassen, wenn der ursürungliche
     * Weg blockiert ist.
     * @param mover Der Mover, der die Umleitung laufen soll
     * @param obstacle Das Primärhinderniss des Movers
     * @param target das Ursprüngliche Ziel
     * @return true, wenn Umleitung gefunden, sonst false
     */
    private boolean tryDiversion(Moveable mover, Moveable obstacle, SimplePosition target) {
        ObstaclePattern p = new ObstaclePattern();
        List<SubSectorEdge> diversion = SubSectorPathfinder.searchDiversion(mover, obstacle, target, p);
        mover.getLowLevelManager().getPathManager().saveDiversion(p, obstacle, diversion != null);
        if (diversion != null) {
            System.out.println(mover + " calced a diversion");
            mover.getLowLevelManager().getPathManager().setDiversion(diversion, mover);
            return true;
        } else {
            System.out.println(mover + ": no diversion.");
            return false;
        }
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

    void followTo(FloatingPointPosition target) {

        int i = 0;

        for (GroupMember member : myMovers) {
            System.out.println("Moving " + member.getMover() + " from " + member.getMover().getPrecisePosition() + " to " + target);
            List<Node> path = SectorPathfinder.findPath(member.getMover().getPrecisePosition(), target, Server.getInnerServer().moveMan.moveMap.containingPoly(member.getMover().getPrecisePosition()), moveMap);
            if (path != null) {
                List<SimplePosition> optiPath = SectorPathfinder.optimizePath(path, member.getMover().getPrecisePosition(), target, moveMap);
                if (optiPath != null) {

                    // Weg setzen
                    member.newWay();
                    for (SimplePosition node : optiPath) {
                        member.addWaypoint(node);
                    }
                    // Loslaufen lassen
                    member.getMover().getLowLevelManager().setTargetVector(member.popWaypoint(), false, false, null);
                }
            }
            i++;
        }
    }

    /**
     * Rufen mover auf, wenn sie immernoch warten. (Also SEHR oft).
     * Antwortet ihnen, ob sie weiter warten sollen, oder lieber eine
     * Umleitung versuchen sollen.
     * 
     * Diese Methode hat die Verantwortung, Verklemmungen zu Erkennen und aufzulösen.
     * z.B:
     * U1 läuft in Sackgasse, berechnet Umleitung.
     * U2 ist direkt dahinter, stößt dagegen, wartet.
     * U1 kann jetzt aber auch nichtmehr weiter, wartet.
     * -- An dieser Stelle muss für U2 eine Umleitung berechnet werden.
     * @param mover Der Mover, der (noch) wartet
     * @param obstacle Das derzeitige Primär-Hinderniss
     * @return true, wenn der Mover weiter warten soll.
     */
    boolean stayWaiting(Moveable mover, Moveable obstacle) {
      /*  // Simples Erkennen: Wartet das Hinderniss auch, und zwar auf mich?
        if (obstacle.getLowLevelManager().isWaiting()) {
            if (obstacle.getLowLevelManager().getWaitFor().equals(mover)) {
                // Umleitung versuchen
                return !tryDiversion(mover, obstacle, mover.getLowLevelManager().getPathManager().lastRealWaypoint());
            }

            // Rekursiv die "Hindernisskette" entlanggehen und ursprüngliches Problem finden
            // Liste für Schleifenerkennung:
            ArrayList<Moveable> obstacles = new ArrayList<Moveable>();
            Moveable next = obstacle;
            while (next != null && !obstacles.contains(next)) {
                obstacles.add(next);
                // Next verarbeiten. Wartet das selber auf jemanden?
                if (next.getLowLevelManager().isWaiting()) {
                    next = next.getLowLevelManager().getWaitFor();
                }
                // Im Else-fall next nicht umstellen, dann endet die while
            }

            // Next ist jetzt das Hinderniss, das den ganzen Stau verursacht.
            // Wir warten nur weiter, wenn das läuft. (und NICHT wartet)
            if (!next.getLowLevelManager().isMoving() || next.getLowLevelManager().isWaiting()) {
                return !tryDiversion(mover, obstacle, mover.getLowLevelManager().getPathManager().lastRealWaypoint());
            }
        } */

        return true;
    }

    boolean killDiversion(Moveable caster2) {
        return caster2.getLowLevelManager().getPathManager().getLastObst().positionsChanged();
    }
}
