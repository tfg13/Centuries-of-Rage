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

import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import thirteenducks.cor.graphics.GraphicsImage;
import thirteenducks.cor.graphics.Sprite;

/**
 * Ein Geschoss (Kugel/Pfeil)
 * Jedes Geschoss hat eine Quelle (den Angreifer) und ein Ziel (das Opfer),
 * einen Schadenswert und eine Verzögerung (Flugdauer)
 *
 * Wärend der Flugdauer wird das Geschoss vom Grafiksystem animiert, von der Quelle zum Ziel.
 * Die Quellen-Startposition wird gepeichert und ist Fix, die Zielposition kann sich ändern,
 * wenn das Ziel abhaut. Es ist nicht möglich, Geschossen auszuweichen.
 * Die Flugzeit ist ebenfalls fix, notfalls beschleunigt das Geschoss.
 * Da in aller Regel mit sehr kurzen Laufzeiten zu rechnen ist, spielt das keine Rolle.
 *
 * Nach Ablauf der Flugdauer wird dem Opfer der Schaden zugefügt.
 * Sinkt die Endenergie unter Null, passiert weiter nichts, nur der Server
 * löscht Einheiten.
 *
 * @author tfg
 */
public class Bullet implements Pauseable, Sprite {

    public Position sourcePos;
    public GameObject attacker;
    GameObject target;
    Position targetPos;
    boolean positionCanChange = false;
    int damage;
    int delay;
    long pauseTime = 0;
    boolean paused = false;
    long startTime;
    int lastDirection;
    int lastX;
    int lastY;
    String texture;

    public Bullet(GameObject attacker, GameObject victim, int dmg, int dly) {
        sourcePos = attacker.getMainPosition();
        this.attacker = attacker;
        target = victim;
        damage = dmg;
        delay = dly;
        startTime = System.currentTimeMillis();
        texture = attacker.getBullettexture();
        positionCanChange = true;
    }

    public Position getTargetPos() {
        if (targetPos != null) {
            return targetPos;
        } else {
            return target.getMainPosition();
        }
    }

    /**
     * Berechnet die genaue Renderposition des Bullets.
     */
    public int[] getRenderLocation(int offX, int offY) {
        long now = System.currentTimeMillis();
        int passed = 0;
        if (!paused) {
            if (pauseTime != 0) {
                int pausetime = (int) (now - pauseTime);
                passed = (int) (now - startTime - pausetime);
                pauseTime = 0;
                startTime = now - passed;
            } else {
                passed = (int) (now - startTime);
            }
        } else {
            passed = (int) (pauseTime - startTime);
        }
        // Wie viel wurde bereits zurückgelegt?
        float progress = (float) (passed * 1.0 / delay);
        if (progress > 1) {
            // Schon fertig
            return null;
        }
        // Start und Ziel-Position in Pixel holen
        int startX = (sourcePos.getX() - offX) * 20;
        int startY = (sourcePos.getY() - offY) * 15;
        int targetX = 0;
        int targetY = 0;
        if (positionCanChange) {
            targetX = (target.getMainPosition().getX() - offX) * 20;
            targetY = (target.getMainPosition().getY() - offY) * 15;
        } else {
            targetX = (targetPos.getX() - offX) * 20;
            targetY = (targetPos.getY() - offY) * 15;
        }
        // Position der Bullets berechnen
        int vecX = (int) ((targetX - startX) * progress);
        int vecY = (int) ((targetY - startY) * progress);
        // Position speichern:
        lastX = (startX + vecX) / 20;
        lastY = (startY + vecY) / 15;
        // Richtung speichern (für schönere Effekte verwendet das Bulletsystem 16 Richtungen
        calcDirection(targetX, targetY, startX, startY);
        int[] ret = new int[2];
        ret[0] = startX + vecX;
        ret[1] = startY + vecY;
        return ret;
    }

    /**
     * Liefert die ungefähre Position in gültigen (x+y % 2 = 0) Feldern zurück
     * Für FoW-Erkennung
     * Benötigt Rechenergebnisse von getRenderLocation, muss also kurz danach aufgerufen werden
     * @return
     */
    public Position getRoundedPosition(int posX, int posY) {
        int rX = lastX + posX;
        int rY = lastY + posY;
        if ((rX + rY) % 2 == 1) {
            rY++;
        }
        return new Position(rX, rY);
    }

    /**
     * Liefert die Drehung des Bullets zurück.
     * MUSS DIREKT NACH getRenderLocation aufgerufen werden, sonst ist das Ergebniss
     * womöglich unpräzise
     */
    public int getDirection() {
        return lastDirection;
    }

    private void calcDirection(int mx, int my, int bx, int by) {
        if ((by - my) == 0) {
            // Division by zero
            if ((mx - bx) > 0) {
                lastDirection = 4;
            } else {
                lastDirection = 12;
            }
            return;
        }
        float deg = (float) Math.atan((mx - bx) * 1.0 / (by - my));
        // Bogenmaß in Grad umrechnen (Bogenmaß ist böse (!))
        deg = (float) (deg * 1.0 / Math.PI * 180);
        // In 360Grad System umrechnen (falls negativ)
        if (deg < 0) {
            deg = 180 + deg;
        }
        // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
        if (mx > bx) {
            deg += 180;
        }
        if (deg == 0 || deg == -0) {
            if (my < by) {
                deg = 180;
            }
        }
        if (deg < 11.25) {
            lastDirection = 8;
        } else if (deg < 33.75) {
            lastDirection = 9;
        } else if (deg < 56.25) {
            lastDirection = 10;
        } else if (deg < 78.25) {
            lastDirection = 11;
        } else if (deg < 101.25) {
            lastDirection = 12;
        } else if (deg < 123.75) {
            lastDirection = 13;
        } else if (deg < 146.25) {
            lastDirection = 14;
        } else if (deg < 168.75) {
            lastDirection = 15;
        } else if (deg < 191.25) {
            lastDirection = 0;
        } else if (deg < 213.75) {
            lastDirection = 1;
        } else if (deg < 236.25) {
            lastDirection = 2;
        } else if (deg < 258.75) {
            lastDirection = 3;
        } else if (deg < 281.25) {
            lastDirection = 4;
        } else if (deg < 303.75) {
            lastDirection = 5;
        } else if (deg < 326.25) {
            lastDirection = 6;
        } else if (deg < 348.75) {
            lastDirection = 7;
        } else {
            lastDirection = 8;
        }
    }

    @Override
    public void pause() {
        pauseTime = System.currentTimeMillis();
        paused = true;
    }

    @Override
    public void unpause() {
        paused = false;
    }

    /**
     * Liefert nur präzise Werte, von kurz vorher getRenderLocation lief.
     * @return
     */
    public int getX() {
        return lastX;
    }

    /**
     * Liefert nur präzise Werte, von kurz vorher getRenderLocation lief.
     * @return
     */
    public int getY() {
        return lastY;
    }

    @Override
    public void renderSprite(Graphics g, int x, int y, Map<String, GraphicsImage> imgMap,  Color spriteColor) {
        System.out.println("AddMe: Render Sprite.");
    }

    @Override
    public Position[] getVisisbilityPositions() {
        System.out.println("AddMe: Calc vis-pos (bullet)");
        Position[] ret = new Position[1];
        ret[0] = getRoundedPosition(lastX, lastY);
        return ret;
    }

    @Override
    public int compareTo(Sprite o) {
        System.out.println("AddMe: Compare Sprite!");
        return 1;
    }

    @Override
    public Position getSortPosition() {
        return sourcePos;
    }

    @Override
    public Position getMainPositionForRenderOrigin() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean renderInFullFog() {
        return false;
    }

    @Override
    public boolean renderInHalfFog() {
        return false; // Im Halbschatten sieht man Geschosse nicht.
    }

    @Override
    public boolean renderInNullFog() {
        return true; // Geschosse sind grundsätzlich nicht unsichtbar.
    }

    @Override
    public void renderGroundEffect(Graphics g, int x, int y, Map<String, GraphicsImage> imgMap, Color spriteColor) {
        // Bullets haben keine
    }

    @Override
    public int getColorId() {
        return 0;
    }


}
