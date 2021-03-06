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
import de._13ducks.cor.game.SimplePosition;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Vector2f;

/**
 *
 * @author michael
 */
public class MathUtil {

    /**
     * Statische Ints für die Richtungen:
     */
    static int evasion_left = 0, evasion_right = 1;

    public static void main(String args[]) {
        MathUtil util = new MathUtil();
    }

    public MathUtil() {

        System.out.println(getIntersection(new FloatingPointPosition(0, 0), new FloatingPointPosition(0, 1), new FloatingPointPosition(1, 1), new FloatingPointPosition(-1, 1)));
    }

    /**
     * Berechnet zwei Ausweichvektoren (links und rechts) um ein Hindernis. Entlang dieser Vektoren kann man ohne Kollision am Hinderniss vorbeigehen,
     * @param own           - eigne Position
     * @param blocker       - Position des Hindernisses
     * @param ownRadius     - eigener Radius
     * @param blockerRadius - Radius des Hindernisses
     * @return              - zwei Ausweichvektoren als FloatingPointPosition-Array (der linke zuerst)
     */
    public static FloatingPointPosition getEvasionVector(FloatingPointPosition own, FloatingPointPosition blocker, double ownRadius, double blockerRadius, int evasionDirection) {

        // Hypothenuse und Gegenkathete berechnen (Pythagoras lässt grüßen...)
        double h = own.getDistance(blocker);
        double gk = ownRadius + blockerRadius;

        // Der Winkel, um den der Ausweichvektor vom Vektor zum Hindernis abweichen muss damit man ohne Kollision vorbei kann
        double deltaAngle = Math.asin((gk / h));

        // Winkel zwischen {1,0} und dem Hindernis berechen:
        double blockerVectorAngle = getAngle(blocker.subtract(own), new FloatingPointPosition(0, 0));


        // Der Winkel der Ausweichvektoren:
        double angle;
        if (evasionDirection == evasion_left) {
            angle = blockerVectorAngle + deltaAngle;
        } else {
            angle = blockerVectorAngle - deltaAngle;
        }

        FloatingPointPosition evasionVector = new FloatingPointPosition(Math.cos(angle), Math.sin(angle));
        // Winkel zu Vektoren umwandeln:



        return evasionVector;
    }

    /**
     * Berechnet den Winkel zwischen zwei Vektoren
     * @param vector_1
     * @param vector_2
     * @return
     */
    public static double getAngle(FloatingPointPosition vector_1, FloatingPointPosition vector_2) {
        double scalar = ((vector_1.getfX() * vector_2.getfX()) + (vector_1.getfY() * vector_2.getfY()));

        double vector_1_lenght = Math.sqrt((vector_1.getfX() * vector_1.getfX()) + vector_2.getfY() * vector_1.getfY());
        double vector_2_lenght = Math.sqrt((vector_2.getfX() * vector_2.getfX()) + vector_2.getfY() * vector_2.getfY());

        double lenght = vector_1_lenght * vector_2_lenght;

        double angle = Math.acos((scalar / lenght));

        return angle;
    }

    /**
     * Gibt an, auf welcher Seite dem Objekt besser ausgewichen werden kann
     * (berücksichting nur, ob das Hinderniss auf der Rechten oder linken Seite vom Vektor zum Ziel steht)
     * @param position - die eigene Position
     * @param obstacle - die Position der Hindernisses
     * @return 1 für links, 2 für rechts
     */
    public static int getEvasionDirection(FloatingPointPosition position, FloatingPointPosition obstacle, FloatingPointPosition target) {
        double targetAngle = getAngle(position.subtract(target), new FloatingPointPosition(1, 0));
        double obstacleAngle = getAngle(position.subtract(obstacle), new FloatingPointPosition(1, 0));

        if (targetAngle > obstacleAngle) {
            return 1;
            // --> rechts ausweichen
        } else {
            return 2;
            // --> links ausweichen
        }
    }

    /**
     * Schnittpunkt zweier Geraden berechnen
     * Die Geraden werden jeweils zdurch 2 Punkte beschrieben
     * @param p1-p4 - die Vier Punkte, 2 pro Gerade
     * @return - Der Schnittpunkt oder null wenns keinen gibt
     */
    public static FloatingPointPosition getIntersection(FloatingPointPosition p1, FloatingPointPosition p2, FloatingPointPosition p3, FloatingPointPosition p4) {

        Line l1 = new Line((float) p1.getfX(), (float) p1.getfY(), (float) p2.getfX(), (float) p2.getfY());
        Line l2 = new Line((float) p3.getfX(), (float) p3.getfY(), (float) p4.getfX(), (float) p4.getfY());


        Vector2f intersect = l1.intersect(l2);


        return new FloatingPointPosition(intersect.x, intersect.y);

    }

    /**
     * Findet die Schnittpunkte zweier Kreise.
     * Verhalten ist nur definiert, wenn auch 2 Schnittpunkte existieren.
     * @param c1 Kreis 1
     * @param c2 Kreis 2
     * @return ein Array mit genau zwei Positionen
     */
    public static SimplePosition[] circleCircleIntersection(Circle c1, Circle c2) {
        /*
         * Verwendet ein kompliziertes Verfahren, bestehend aus:
         * 
         * - Formel von Heron
         * - Pythagoras
         * - Scharfem Nachdenken
         * 
         * 
         */

        double xa = c1.getCenterX();
        double ya = c1.getCenterY();
        double ra = c1.getRadius();
        double xb = c2.getCenterX();
        double yb = c2.getCenterY();
        double rb = c2.getRadius();

        double dSquare = (xb - xa) * (xb - xa) + (yb - ya) * (yb - ya);
        double k = Math.sqrt(((ra + rb) * (ra + rb) - dSquare) * (dSquare - (ra - rb) * (ra - rb))) / 4;

        double x1 = (xa + xb) / 2 + ((xb - xa) * (ra * ra - rb * rb)) / (2 * dSquare) + 2 * (yb - ya) * k / dSquare;
        double y1 = (ya + yb) / 2 + ((yb - ya) * (ra * ra - rb * rb)) / (2 * dSquare) + 2 * (xb - xa) * k / dSquare;

        double x2 = (xa + xb) / 2 + ((xb - xa) * (ra * ra - rb * rb)) / (2 * dSquare) - 2 * (yb - ya) * k / dSquare;
        double y2 = (ya + yb) / 2 + ((yb - ya) * (ra * ra - rb * rb)) / (2 * dSquare) - 2 * (xb - xa) * k / dSquare;

        return new SimplePosition[]{new Vector(x1, y2), new Vector(x2, y1)}; // DAS VERTAUSCHEN IST HIER KEIN FEHLER!!!
    }
}
