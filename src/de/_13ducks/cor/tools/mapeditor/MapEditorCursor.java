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
package de._13ducks.cor.tools.mapeditor;

import java.util.ArrayList;

/**
 *
 * @author tfg
 */
public class MapEditorCursor {
    // Beschreibt einen Cursor

    final static int FILL_NORMAL = 0;
    final static int FILL_TEXMIX = 1;
    final static int FILL_HALF = 2;
    final static int FILL_HALFMIX = 3;
    final static int TYPE_STRAIGHT = 0;
    final static int TYPE_DIAGONAL = 1;
    final static int TYPE_CIRCLE = 2;
    private int cursorFill;
    private int cursorType;
    private int sizeX;
    private int sizeY;

    protected MapEditorCursor(int type, int fill, int sX, int sY) {
        cursorType = type;
        cursorFill = fill;
        sizeX = sX;
        sizeY = sY;
    }

    protected ArrayList<MapEditorCursorField> getFields(ArrayList<String> textures) {
        ArrayList<MapEditorCursorField> tempList = new ArrayList<MapEditorCursorField>();
        if (cursorType == TYPE_STRAIGHT) {
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    if (cursorFill == FILL_NORMAL || cursorFill == FILL_HALF) {
                        tempList.add(new MapEditorCursorField(textures.get(0), x * 2, y * 2));
                    } else if (cursorFill == FILL_TEXMIX || cursorFill == FILL_HALFMIX) {
                        tempList.add(new MapEditorCursorField(textures.get((int) (Math.random() * textures.size())), x * 2, y * 2));
                    }
                    if (cursorFill == FILL_NORMAL || cursorFill == FILL_TEXMIX) {
                        if (x > 0) {
                            if (y > 0) {
                                // Zwischen Felder
                                if (cursorFill == FILL_NORMAL) {
                                    tempList.add(new MapEditorCursorField(textures.get(0), x * 2 - 1, y * 2 - 1));
                                } else {
                                    tempList.add(new MapEditorCursorField(textures.get((int) (Math.random() * textures.size())), x * 2, y * 2));
                                }
                            }
                        }
                    }
                }
            }
        } else if (cursorType == TYPE_DIAGONAL) {
            int add = 0;
            if (sizeX % 2 == 0) {
                add = 1;
            }
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    if ((x + y) % 2 == 0) {
                        if (cursorFill == FILL_NORMAL || cursorFill == FILL_HALF) {
                            tempList.add(new MapEditorCursorField(textures.get(0), x + y, add + (sizeX - 1) + (y - x)));
                        } else {
                            tempList.add(new MapEditorCursorField(textures.get((int) (Math.random() * textures.size())), x + y, add + (sizeX - 1) + (y - x)));
                        }
                    } else if (cursorFill == FILL_NORMAL || cursorFill == FILL_TEXMIX) {
                        if (cursorFill == FILL_NORMAL || cursorFill == FILL_HALF) {
                            tempList.add(new MapEditorCursorField(textures.get(0), x + y, add + (sizeX - 1) + (y - x)));
                        } else {
                            tempList.add(new MapEditorCursorField(textures.get((int) (Math.random() * textures.size())), x + y, add + (sizeX - 1) + (y - x)));
                        }
                    }
                }
            }
        } else if (cursorType == TYPE_CIRCLE) {
            int r = sizeX;
            int x = (int) (r * 4);
            int y = (int) (r * 4);
            r *= 2;
            for (int a = 0; a < x; a++) {
                for (int b = 0; b < y; b++) {
                    if (a % 2 != b % 2) {
                        continue;
                    }
                    double dx = (x / 2) - a;
                    double dy = (y / 2) - b;
                    if (Math.sqrt((dx * dx) + (dy * dy)) < r) {
                        if (a % 2 == 0) {
                            if (cursorFill == FILL_NORMAL || cursorFill == FILL_HALF) {
                                tempList.add(new MapEditorCursorField(textures.get(0), a, b));
                            } else {
                                tempList.add(new MapEditorCursorField(textures.get((int) (Math.random() * textures.size())), a, b));
                            }
                        } else if (cursorFill == FILL_NORMAL || cursorFill == FILL_TEXMIX) {
                            if (cursorFill == FILL_NORMAL || cursorFill == FILL_HALF) {
                                tempList.add(new MapEditorCursorField(textures.get(0), a, b));
                            } else {
                                tempList.add(new MapEditorCursorField(textures.get((int) (Math.random() * textures.size())), a, b));
                            }
                        }
                    }
                }
            }
        }
        return tempList;
    }

    public boolean[][] getFieldShadow() {
        if (cursorType == MapEditorCursor.TYPE_STRAIGHT) {
            boolean[][] tempArray = new boolean[(sizeX * 2) - 1][(sizeY * 2) - 1];
            for (int x = 0; x < (sizeX * 2) - 1; x++) {
                for (int y = 0; y < (sizeY * 2) - 1; y++) {
                    if ((x + y) % 2 == 1) {
                        continue;
                    }
                    if (x % 2 == 0) {
                        tempArray[x][y] = true;
                    } else if (cursorFill == MapEditorCursor.FILL_NORMAL || cursorFill == MapEditorCursor.FILL_TEXMIX) {
                        tempArray[x][y] = true;
                    }
                }
            }
            return tempArray;
        } else if (cursorType == MapEditorCursor.TYPE_DIAGONAL) {
            int arrS = sizeX + sizeY - 1;
            int add = 0;
            if (sizeX % 2 == 0) {
                arrS += 1;
                add = 1;
            }
            boolean[][] tempArray = new boolean[arrS][arrS];
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    if ((x + y) % 2 == 0) {
                        tempArray[x + y][add + (sizeX - 1) + (y - x)] = true;
                    } else if (cursorFill == FILL_NORMAL || cursorFill == FILL_TEXMIX) {
                        tempArray[x + y][add + (sizeX - 1) + (y - x)] = true;
                    }
                }
            }
            return tempArray;
        } else {
            int r = sizeX;
            int x = (int) (r * 4);
            int y = (int) (r * 4);
            r *= 2;
            boolean[][] tempArray = new boolean[r * 2][r * 2];
            for (int a = 0; a < x; a++) {
                for (int b = 0; b < y; b++) {
                    if (a % 2 != b % 2) {
                        continue;
                    }
                    double dx = (x / 2) - a;
                    double dy = (y / 2) - b;
                    if (Math.sqrt((dx * dx) + (dy * dy)) < r) {
                        if (a % 2 == 0) {
                            tempArray[a][b] = true;
                        } else if (cursorFill == FILL_NORMAL || cursorFill == FILL_TEXMIX) {
                            tempArray[a][b] = true;
                        }
                    }
                }
            }
            return tempArray;
        }
    }

    protected int getType() {
        return cursorType;
    }

    protected int getFill() {
        return cursorFill;
    }
}
