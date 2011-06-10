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
package de._13ducks.cor.game;

import de._13ducks.cor.game.client.ClientCore;
import de._13ducks.cor.game.server.movement.Vector;
import de._13ducks.cor.networks.client.behaviour.ClientBehaviour;
import java.util.Map;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import de._13ducks.cor.graphics.GraphicsImage;
import de._13ducks.cor.graphics.Sprite;

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
public class Bullet extends ClientBehaviour implements Pauseable, Sprite {

    private FloatingPointPosition sourcePos;
    private GameObject target;
    private GameObject attacker;
    private int damage;
    private int flytime;
    private long pauseTime = 0;
    private boolean paused = false;
    private long startTime;
    private int lastDirection;
    private String texture;
    private SimplePosition currentPos = new FloatingPointPosition(0, 0);

    public Bullet(GameObject attacker, GameObject victim, int dmg, int dly, ClientCore.InnerClient rgi) {
        super(null, victim, 2, 5, true);
        sourcePos = new FloatingPointPosition(attacker.getMainPosition());
        target = victim;
        damage = dmg;
        flytime = dly;
        startTime = System.currentTimeMillis();
        texture = attacker.getBullettexture();
        this.attacker = attacker;
        target.addClientBehaviour(this);
        this.rgi = rgi;
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

    @Override
    public void renderSprite(Graphics g, int x, int y, double scrollX, double scrollY, Map<String, GraphicsImage> imgMap, Color spriteColor) {
        externalExecute();
        System.out.println("AddMe: Render Sprite.");
    }

    @Override
    public Position[] getVisisbilityPositions() {
        return new Position[]{new Position((int) currentPos.x(), (int) currentPos.y())};
    }

    @Override
    public int compareTo(Sprite o) {
        return this.getSortPosition().compareTo(o.getSortPosition());
    }

    @Override
    public Position getSortPosition() {
        return sourcePos;
    }

    @Override
    public Position getMainPositionForRenderOrigin() {
        // Der Aufruf kommt ohnehin nur zurück, und wir brauchen das nicht.
        return new Position(0, 0);
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
    public void renderGroundEffect(Graphics g, int x, int y, double scrollX, double scrollY, Map<String, GraphicsImage> imgMap, Color spriteColor) {
        // Bullets haben keine
    }

    @Override
    public int getColorId() {
        return 0;
    }

    @Override
    public void renderMinimapMarker(Graphics g, int x, int y, Color spriteColor) {
        // Bullets werden nicht auf Minimapp gezeichnet
    }

    @Override
    public void renderSkyEffect(Graphics g, int x, int y, double scrollX, double scrollY, Map<String, GraphicsImage> imgMap, Color spriteColor) {
        // Bullets haben keine
    }

    @Override
    public void execute() {
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
        float progress = (float) (passed * 1.0 / flytime);
        if (progress > 1) {
            // Fertig. Damage dealen und löschen
            target.removeClientBehaviour(this);
            rgi.rogGraphics.content.allList.remove(this);
            target.dealDamageC(damage);
        }
        // Position des Bullets berechnen
        FloatingPointPosition targetP = new FloatingPointPosition(target.getCentralPosition());
        Vector vec = new Vector(targetP.x() - sourcePos.x(), targetP.y() - sourcePos.y());

        vec.multiplyMe(progress);

        currentPos = sourcePos.add(vec.toFPP());

        // Richtung berechnen:
        if ((sourcePos.y() - targetP.y()) == 0) {
            // Division by zero
            if ((targetP.x() - sourcePos.x()) > 0) {
                lastDirection = 4;
            } else {
                lastDirection = 12;
            }
            return;
        }
        float deg = (float) Math.atan((targetP.x() - sourcePos.x()) * 1.0 / (sourcePos.y() - targetP.y()));
        // Bogenmaß in Grad umrechnen (Bogenmaß ist böse (!))
        deg = (float) (deg * 1.0 / Math.PI * 180);
        // In 360Grad System umrechnen (falls negativ)
        if (deg < 0) {
            deg = 180 + deg;
        }
        // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
        if (targetP.x() > sourcePos.x()) {
            deg += 180;
        }
        if (deg == 0 || deg == -0) {
            if (targetP.y() < sourcePos.y()) {
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
    public void gotSignal(byte[] packet) {
    }
}
