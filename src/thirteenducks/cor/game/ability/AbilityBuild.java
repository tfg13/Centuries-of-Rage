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
package thirteenducks.cor.game.ability;

import thirteenducks.cor.game.Building;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.game.Unit;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.awt.Dimension;
import thirteenducks.cor.graphics.input.CoRInputMode;
import thirteenducks.cor.game.Position;

/**
 * Die Fähigkeit Gebäude zu Bauen
 *
 * @author tfg
 */
public class AbilityBuild extends Ability {

    public float progress;  // Der Fortschritt
    public int descTypeId;  // Die DESC-ID des zu bauenden Gebäudes
    public int duration;    // Dauer des Bauens bei einem Bauarbeiter

    public AbilityBuild(int descId) {

        super(descId);

        type = Ability.ABILITY_BUILD;
        cooldown = 0.0;
    }

    @Override
    public void perform(GameObject caster) {
        // Bildchen wurde angeklickt, Aktion durchführen
        // Neuen RogInputMode registrieren
        rgi.rogGraphics.inputM.addAndReplaceSpecialMode(new CoRInputMode(true) {

            @Override
            public void mouseMoved(int oldx, int oldy, int newx, int newy) {
                // Wenn im Hud, dann normale Maus, sonst rendercursor
           /*     if (newx > rgi.rogGraphics.content.hudX) {
                    if (rgi.rogGraphics.content.renderPicCursor) {
                        rgi.rogGraphics.content.setCursor(false);
                        rgi.rogGraphics.content.renderPicCursor = false;
                    }
                } else { */
                    if (!rgi.rogGraphics.content.renderPicCursor) {
                        rgi.rogGraphics.content.renderPicCursor = true;
                        rgi.rogGraphics.content.setCursor(true);
                    }
                    rgi.rogGraphics.content.setFramePosition(rgi.rogGraphics.content.getSpecialSelectedField(newx, newy));
               // }
                    System.out.println("AddMe: Auto-Hide cursor on overlays!");
            }

            @Override
            public void mouseKlicked(int button, int x, int y, int clickCount) {
                // Rechtsklick?
                if (button == 0) {
                    // Ist der Cursor innerhalb der Game-Area?
                //    if (x < rgi.rogGraphics.content.hudX) {
                        // Welches Gebäude soll geholt werden:
                        Building building = rgi.mapModule.getDescBuilding(descTypeId, -1, rgi.game.getOwnPlayer().playerId);
                        // Ok, User will bauen, checken ob er das darf:
                        // Felder frei? - Position holen
                        Dimension pos = rgi.rogGraphics.content.getSelectedField(x, y);
                        // Das ist noch nicht das Ursprungsfeld des Gebäudecursors, Ursprungsfeld berechnen:
                        System.out.println("AddMe: Calc Building-Pos correctly");
                        Position basePos = new Position(pos.width, pos.height);
                        // Ist das alles zum Bauen frei?
                        boolean free = true;
                        System.out.println("AddMe: Ask Server if position is free!");
                        // Und... frei?
                        if (free) {
                            // Dort hinstellen
                            // Ja, jetzt da hin bauen
                            // Ressourcen abziehen
                            rgi.game.getOwnPlayer().res1 -= costs[0];
                            rgi.game.getOwnPlayer().res2 -= costs[1];
                            rgi.game.getOwnPlayer().res3 -= costs[2];
                            rgi.game.getOwnPlayer().res4 -= costs[3];
                            rgi.game.getOwnPlayer().res5 -= costs[4];
                            // Truppenlimit
                            // Truppenlimit setzen
                         /*   if (building.limit > 0) {
                                rgi.game.getOwnPlayer().currentlimit += building.limit;
                            } */
                            //rgi.mapModule.addBuilding(building);
                            // Gebäude adden:
                            rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 18, 0, building.getDescTypeId(), basePos.getX(), basePos.getY()));
                            // Fertig - dieses Verhalten wieder entfernen
                            rgi.rogGraphics.inputM.removeSpecialMode();
                            Unit unit = (Unit) invoker;
                            // Warten, bis das Gebäude vom Server hinzugefügt wurde:
                            int trys = 0;
                            Position bpos = basePos;
                            while (true) {
                                trys++;
                                building = rgi.mapModule.findBuilingViaPosition(bpos);
                                if (building == null) {
                                    if (trys > 200) {
                                        System.out.println("[ERROR]: Building still not here after 2000ms! - Aborting");
                                        break;
                                    }
                                    try {
                                        // Noch nicht da
                                        Thread.sleep(10);
                                    } catch (InterruptedException ex) {
                                    }
                                } else {
                                    break;
                                }
                            }
                            if (building != null) {
                                // Hinlaufen - falls noch nicht in der nähe
                                if (!rgi.mapModule.directNeighbor(building, unit)) {
                                    rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 26, unit.netID, building.netID, 0, 0));

                                }
                                // Losbauen
                                rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 17, unit.netID, building.netID, duration, 0));

                                // Fortschrittsanzeige einschalten
                                rgi.rogGraphics.triggerUpdateHud();
                            } else {
                                System.out.println("FixMe: Error, can't build building, is was not found.");
                            }
                        }
                   // }
                } else {
                    // Abbrechen
                    rgi.rogGraphics.inputM.removeSpecialMode();
                }
            }

            @Override
            public void startMode() {
                Building building = rgi.mapModule.getDescBuilding(descTypeId, -1, rgi.game.getOwnPlayer().playerId);
                // Starten, Cursor vorbereiten
                System.out.println("AddMe: Set cursor to building's default texture");
                //rgi.rogGraphics.content.renderPic = rgi.rogGraphics.content.imgMap.get(building.defaultTexture);
            }

            @Override
            public void endMode() {
                // Stoppen, Cursor löschen
                rgi.rogGraphics.content.renderPicCursor = false;
                rgi.rogGraphics.content.renderPic = null;
                rgi.rogGraphics.content.setCursor(false);
            }
        });
    }

    public void setDescTypeId(int id) {
        this.descTypeId = id;
    }

    public void setDuration(int millis) {
        this.duration = millis;
    }

    @Override
    public AbilityBuild clone() throws CloneNotSupportedException {
        AbilityBuild b = (AbilityBuild) super.clone();
        return b;
    }

    @Override
    public void antiperform(GameObject caster) {
        // Specialmode entfernen, falls einer da ist:
        rgi.rogGraphics.inputM.removeSpecialMode();
    }
}
