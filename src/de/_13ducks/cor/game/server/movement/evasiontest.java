/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.FloatingPointPosition;

/**
 *
 * @author michael
 */
public class evasiontest {

    public static void main(String args[]) {
        new evasiontest();
    }

    public evasiontest() {

        System.out.println(getAngle(new FloatingPointPosition(2,0), new FloatingPointPosition(0,2)));
    }

    /**
     * Berechnet zwei Ausweichvektoren (links und rechts) um ein Hindernis. Entlang dieser Vektoren kann man ohne Kollision am Hinderniss vorbeigehen,
     * @param own           - eigne Position
     * @param blocker       - Position des Hindernisses
     * @param ownRadius     - eigener Radius
     * @param blockerRadius - Radius des Hindernisses
     * @return              - zwei Ausweichvektoren als FloatingPointPosition-Array (der linke zuerst)
     */
    public FloatingPointPosition[] getEvasionVectors(FloatingPointPosition own, FloatingPointPosition blocker, double ownRadius, double blockerRadius) {

        // Hypothenuse und Gegenkathete berechnen (Pythagoras lässt grüßen...)
        double h = own.getDistance(blocker);
        double gk = ownRadius + blockerRadius;

        // Der Winkel, um den der Ausweichvektor vom Vektor zum Hindernis abweichen muss damit man ohne Kollision vorbei kann
        double deltaAngle = Math.asin((gk / h));

        // Winkel zwischen {1,0} und dem Hindernis berechen:
        double blockerVectorAngle = getAngle(blocker.subtract(own), new FloatingPointPosition(0,0));

        // Die Winkel der Ausweichvektoren:
        double angle_1 = blockerVectorAngle + deltaAngle;
        double angle_2 = blockerVectorAngle - deltaAngle;

        // RückgabeArray initialisieren:
        FloatingPointPosition evasionVectors[] = new FloatingPointPosition[2];

        // Winkel zu Vektoren umwandeln:
        evasionVectors[0] = new FloatingPointPosition(Math.cos(angle_1), Math.sin(angle_1));
        evasionVectors[1] = new FloatingPointPosition(Math.cos(angle_2), Math.sin(angle_2));

        System.out.println(evasionVectors[0].toString() + " " + evasionVectors[1].toString());

        return evasionVectors;
    }

    /**
     * Berechnet den Winkel zwischen zwei Vektoren
     * @param vector_1
     * @param vector_2
     * @return
     */
    public double getAngle(FloatingPointPosition vector_1, FloatingPointPosition vector_2) {
        double scalar = ((vector_1.getfX() * vector_2.getfX()) + (vector_1.getfY() * vector_2.getfY()));

        double vector_1_lenght = Math.sqrt((vector_1.getfX() * vector_1.getfX()) + vector_2.getfY() * vector_1.getfY());
        double vector_2_lenght = Math.sqrt((vector_2.getfX() * vector_2.getfX()) + vector_2.getfY() * vector_2.getfY());

        double lenght = vector_1_lenght * vector_2_lenght;

        double angle = Math.acos((scalar / lenght));

        return angle;
    }
}







