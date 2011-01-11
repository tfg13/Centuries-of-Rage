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

//import elementcorp.rog.RogMapElement.collision;
import thirteenducks.cor.networks.client.behaviour.impl.ClientBehaviourHarvest;
import thirteenducks.cor.game.client.ClientCore;
import thirteenducks.cor.game.GameObject;
import thirteenducks.cor.map.CoRMapElement.collision;
import java.util.ArrayList;

/**
 * Eine Ressource Ingame
 *
 * @author tfg
 */
public class Ressource extends GameObject {

    public static final int RES_FOOD = 1;
    public static final int RES_WOOD = 2;
    public static final int RES_METAL = 3;
    public static final int RES_COINS = 4;
    public static final int RES_OIL = 5;
    private int restype;
    private String texture;
    private GameObject[] harvesters; // Die Maximal 4/8 Ernterfelder. Reihenfolge: NO, SO, SW, NW

    public Ressource(int type, String tex, int newNetId) {
        super(true, newNetId);
        restype = type;
        texture = tex;
        //TODO: Hitpoints fertig setzen
        switch (type) {
            case 1:
                hitpoints = 800;
                harvesters = new GameObject[4];
                break;
            case 2:
                hitpoints = 200;
                harvesters = new GameObject[4];
                break;
            case 3:
                hitpoints = 2000;
                harvesters = new GameObject[8];
                break;
            case 4:
                hitpoints = 2000;
                harvesters = new GameObject[8];
                break;
            case 5:
                hitpoints = 1000;
                break;
        }
    }

    public int getType() {
        return restype;
    }

    public String getTex() {
        return texture;
    }

    /**
     * Überprüft, ob noch ein Feld für einen weiteren Ernter frei ist.
     * @return
     */
    public boolean readyForAnotherHarvester(ClientCore.InnerClient inner) {
        if (this.restype < 3) {
            for (int i = 0; i < 4; i++) {
                GameObject obj = harvesters[i];
                if (obj == null) {
                    // Da wäre noch frei, Position checken
                    switch (i) {
                        case 0:
                            if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y - 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 1:
                            if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y + 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 2:
                            if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 3:
                            if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                    }
                }
            }
        } else if (this.restype < 5) {
            for (int i = 0; i < 8; i++) {
                GameObject obj = harvesters[i];
                if (obj == null) {
                    // Da wäre noch frei, Position checken
                    switch (i) {
                        case 0:
                            if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 1:
                            if (inner.mapModule.getCollision(this.position.X, this.position.Y - 2).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 2:
                            if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y - 2).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 3:
                            if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y - 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 4:
                            if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y + 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 5:
                            if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y + 2).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 6:
                            if (inner.mapModule.getCollision(this.position.X, this.position.Y + 2).equals(collision.free)) {
                                return true;
                            }
                            break;
                        case 7:
                            if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free)) {
                                return true;
                            }
                            break;
                    }
                }
            }
        }
        return false;
    }

    public boolean addHarvester(GameObject harv, Position pos) {
        if (this.restype < 3) {
            // Positionszuordnung finden:
            Position vec = pos.subtract(this.position).transformToDiagonalVector();

            if (vec.X == 1 && vec.Y == -1) {
                if (harvesters[0] == null) {
                    harvesters[0] = harv;
                    return true;
                }
            } else if (vec.X == 1 && vec.Y == 1) {
                if (harvesters[1] == null) {
                    harvesters[1] = harv;
                    return true;
                }
            } else if (vec.X == -1 && vec.Y == 1) {
                if (harvesters[2] == null) {
                    harvesters[2] = harv;
                    return true;
                }
            } else if (vec.X == -1 && vec.Y == -1) {
                if (harvesters[3] == null) {
                    harvesters[3] = harv;
                    return true;
                }
            }
        } else if (this.restype < 5) {

            // Positionszuordnung finden:
            Position vec = pos.subtract(this.position);

            if (vec.X == -1 && vec.Y == -1) {
                if (harvesters[0] == null) {
                    harvesters[0] = harv;
                    return true;
                }
            } else if (vec.X == 0 && vec.Y == -2) {
                if (harvesters[1] == null) {
                    harvesters[1] = harv;
                    return true;
                }
            } else if (vec.X == 2 && vec.Y == -2) {
                if (harvesters[2] == null) {
                    harvesters[2] = harv;
                    return true;
                }
            } else if (vec.X == 3 && vec.Y == -1) {
                if (harvesters[3] == null) {
                    harvesters[3] = harv;
                    return true;
                }
            } else if (vec.X == 3 && vec.Y == 1) {
                if (harvesters[4] == null) {
                    harvesters[4] = harv;
                    return true;
                }
            } else if (vec.X == 2 && vec.Y == 2) {
                if (harvesters[5] == null) {
                    harvesters[5] = harv;
                    return true;
                }
            } else if (vec.X == 0 && vec.Y == 2) {
                if (harvesters[6] == null) {
                    harvesters[6] = harv;
                    return true;
                }
            } else if (vec.X == -1 && vec.Y == 1) {
                if (harvesters[7] == null) {
                    harvesters[7] = harv;
                    return true;
                }
            }
        }
        return false;
    }

    public void removeHarvester(GameObject harv) {
        if (this.restype < 3) {
            for (int i = 0; i < 4; i++) {
                GameObject obj = harvesters[i];
                if (obj != null && obj.equals(harv)) {
                    harvesters[i] = null;
                    break;
                }
            }
        } else if (this.restype < 5) {
            for (int i = 0; i < 8; i++) {
                GameObject obj = harvesters[i];
                if (obj != null && obj.equals(harv)) {
                    harvesters[i] = null;
                    break;
                }
            }
        }
    }

    public boolean isRegisteredHarvester(GameObject harv) {
        if (this.restype < 3) {
            for (int i = 0; i < 4; i++) {
                GameObject obj = harvesters[i];
                if (obj != null && obj.equals(harv)) {
                    return true;
                }
            }
        } else if (this.restype < 5) {
            for (int i = 0; i < 8; i++) {
                GameObject obj = harvesters[i];
                if (obj != null && obj.equals(harv)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Position getRegisteredHarvestPosition(GameObject harv) {
        if (this.restype < 3) {
            for (int i = 0; i < 4; i++) {
                GameObject obj = harvesters[i];
                if (obj != null && obj.equals(harv)) {
                    switch (i) {
                        case 0:
                            return new Position(this.position.X + 1, this.position.Y - 1);
                        case 1:
                            return new Position(this.position.X + 1, this.position.Y + 1);
                        case 2:
                            return new Position(this.position.X - 1, this.position.Y + 1);
                        case 3:
                            return new Position(this.position.X - 1, this.position.Y - 1);
                    }
                }
            }
        } else if (this.restype < 5) {
            for (int i = 0; i < 8; i++) {
                GameObject obj = harvesters[i];
                if (obj != null && obj.equals(harv)) {
                    switch (i) {
                        case 0:
                            return new Position(this.position.X - 1, this.position.Y - 1);
                        case 1:
                            return new Position(this.position.X, this.position.Y - 2);
                        case 2:
                            return new Position(this.position.X + 2, this.position.Y - 2);
                        case 3:
                            return new Position(this.position.X + 3, this.position.Y - 1);
                        case 4:
                            return new Position(this.position.X + 3, this.position.Y + 1);
                        case 5:
                            return new Position(this.position.X + 2, this.position.Y + 2);
                        case 6:
                            return new Position(this.position.X, this.position.Y + 2);
                        case 7:
                            return new Position(this.position.X - 1, this.position.Y + 1);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Entfernt alle Harvester - benachrichtigt auch das Client-Behaviour
     */
    public void removeAllHarvesters() {
        if (this.restype < 3) {
            for (int i = 0; i < 4; i++) {
                GameObject harvester = harvesters[i];
                if (harvester != null) {
                    // Benachrichtigen & löschen
                    ClientBehaviourHarvest harvb = (ClientBehaviourHarvest) harvester.getbehaviourC(7);
                    harvb.stopHarvesting();
                    harvesters[i] = null;
                }
            }
        } else if (this.restype < 5) {
            for (int i = 0; i < 8; i++) {
                GameObject harvester = harvesters[i];
                if (harvester != null) {
                    // Benachrichtigen & löschen
                    ClientBehaviourHarvest harvb = (ClientBehaviourHarvest) harvester.getbehaviourC(7);
                    harvb.stopHarvesting();
                    harvesters[i] = null;
                }
            }
        }
    }

    /**
     *  Sucht die nächstbeste Ernterposition heraus, die frei ist.
     *
     * @param pos Die aktuelle Position der Einheit
     * @param vec Der Richtungsvektor IN DIAGONALFORM kann null sein, dann wird irgendein Feld gesucht
     * @param inner Die übliche Referenz auf alle Module
     * @return Die Ziel-Ernterposition
     */
    public Position getNextFreeHarvestingPosition(Position pos, Position vec, ClientCore.InnerClient inner) {
        if (this.restype < 3) {
            if (vec == null || vec.X == 0) {
                // Irgendeine suchen:
                for (int i = 0; i < 4; i++) {
                    GameObject obj = harvesters[i];
                    if (obj == null) {
                        // Da wäre noch frei, Position checken
                        switch (i) {
                            case 0:
                                if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y - 1).equals(collision.free)) {
                                    return new Position(this.position.X + 1, this.position.Y - 1);
                                }
                                break;
                            case 1:
                                if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y + 1).equals(collision.free)) {
                                    return new Position(this.position.X + 1, this.position.Y + 1);
                                }
                                break;
                            case 2:
                                if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free)) {
                                    return new Position(this.position.X - 1, this.position.Y + 1);
                                }
                                break;
                            case 3:
                                if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free)) {
                                    return new Position(this.position.X - 1, this.position.Y - 1);
                                }
                                break;
                        }
                    }

                }
            } else if (vec.X == 1 && vec.Y == -1) {
                // NO testen
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y - 1);
                    }
                }
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y + 1);
                    }
                }
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }
                }
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }
                }
            } else if (vec.X == 1 && vec.Y == 1) {
                // SO
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y + 1);
                    }
                }
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }
                }
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y - 1);
                    }
                }
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }
                }
            } else if (vec.X == -1 && vec.Y == 1) {
                // SW
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }
                }
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }
                }
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y + 1);
                    }
                }
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y - 1);
                    }
                }
            } else if (vec.X == -1 && vec.Y == -1) {
                // NW
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }
                }
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y - 1);
                    }
                }
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }
                }
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 1, this.position.Y + 1);
                    }
                }
            }
        } else if (this.restype < 5) {
            if (vec == null || vec.X == 0) {
                //Irgendeine suchen:
                for (int i = 0; i < 8; i++) {
                    GameObject obj = harvesters[i];
                    if (obj == null) {
                        // Da wäre noch frei, Position checken
                        switch (i) {
                            case 0:
                                if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free)) {
                                    return new Position(this.position.X - 1, this.position.Y - 1);
                                }
                                break;
                            case 1:
                                if (inner.mapModule.getCollision(this.position.X, this.position.Y - 2).equals(collision.free)) {
                                    return new Position(this.position.X, this.position.Y - 2);
                                }
                                break;
                            case 2:
                                if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y - 2).equals(collision.free)) {
                                    return new Position(this.position.X + 2, this.position.Y - 2);
                                }
                                break;
                            case 3:
                                if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y - 1).equals(collision.free)) {
                                    return new Position(this.position.X + 3, this.position.Y - 1);
                                }
                                break;
                            case 4:
                                if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y + 1).equals(collision.free)) {
                                    return new Position(this.position.X + 3, this.position.Y + 1);
                                }
                                break;
                            case 5:
                                if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y + 2).equals(collision.free)) {
                                    return new Position(this.position.X + 2, this.position.Y + 2);
                                }
                                break;
                            case 6:
                                if (inner.mapModule.getCollision(this.position.X, this.position.Y + 2).equals(collision.free)) {
                                    return new Position(this.position.X, this.position.Y + 2);
                                }
                                break;
                            case 7:
                                if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free)) {
                                    return new Position(this.position.X - 1, this.position.Y + 1);
                                }
                                break;
                        }
                    }

                }
            } else if (vec.X == 1 && vec.Y == -1) {
                // NO testen
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y - 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y - 2);
                    }

                }
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y - 1);
                    }

                }
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y - 2).equals(collision.free) || new Position(this.position.X, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y - 2);
                    }

                }
                if (harvesters[4] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y + 1);
                    }

                }
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }

                }
                if (harvesters[5] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y + 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y + 2);
                    }

                }
                if (harvesters[7] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }

                }
                if (harvesters[6] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y + 2).equals(collision.free) || new Position(this.position.X, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y + 2);
                    }

                }
            } else if (vec.X == 1 && vec.Y == 1) {
                // SO
                if (harvesters[4] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y + 1);
                    }

                }
                if (harvesters[5] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y + 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y + 2);
                    }

                }
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y - 1);
                    }

                }
                if (harvesters[6] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y + 2).equals(collision.free) || new Position(this.position.X, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y + 2);
                    }

                }
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y - 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y - 2);
                    }

                }
                if (harvesters[7] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }

                }
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y - 2).equals(collision.free) || new Position(this.position.X, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y - 2);
                    }

                }
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }

                }
            } else if (vec.X == -1 && vec.Y == 1) {
                // SW
                if (harvesters[6] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y + 2).equals(collision.free) || new Position(this.position.X, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y + 2);
                    }

                }
                if (harvesters[7] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }

                }
                if (harvesters[5] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y + 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y + 2);
                    }

                }
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }

                }
                if (harvesters[4] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y + 1);
                    }

                }
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y - 2).equals(collision.free) || new Position(this.position.X, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y - 2);
                    }

                }
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y - 1);
                    }

                }
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y - 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y - 2);
                    }

                }
            } else if (vec.X == -1 && vec.Y == -1) {
                // NW
                if (harvesters[0] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y - 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y - 1);
                    }

                }
                if (harvesters[1] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y - 2).equals(collision.free) || new Position(this.position.X, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y - 2);
                    }

                }
                if (harvesters[7] == null) {
                    if (inner.mapModule.getCollision(this.position.X - 1, this.position.Y + 1).equals(collision.free) || new Position(this.position.X - 1, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X - 1, this.position.Y + 1);
                    }

                }
                if (harvesters[2] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y - 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y - 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y - 2);
                    }

                }
                if (harvesters[6] == null) {
                    if (inner.mapModule.getCollision(this.position.X, this.position.Y + 2).equals(collision.free) || new Position(this.position.X, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X, this.position.Y + 2);
                    }

                }
                if (harvesters[3] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y - 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y - 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y - 1);
                    }

                }
                if (harvesters[4] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 3, this.position.Y + 1).equals(collision.free) || new Position(this.position.X + 3, this.position.Y + 1).equals(pos)) {
                        return new Position(this.position.X + 3, this.position.Y + 1);
                    }

                }

                if (harvesters[5] == null) {
                    if (inner.mapModule.getCollision(this.position.X + 2, this.position.Y + 2).equals(collision.free) || new Position(this.position.X + 2, this.position.Y + 2).equals(pos)) {
                        return new Position(this.position.X + 2, this.position.Y + 2);
                    }

                }
            }
        }
        return null;
    }

    public ArrayList<GameObject> getAllHarvesters() {
        ArrayList<GameObject> list = new ArrayList<GameObject>();
        for (GameObject obj : harvesters) {
            list.add(obj);
        }
        return list;
    }
}
