/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de._13ducks.cor.game.server.movement;

import de._13ducks.cor.game.FloatingPointPosition;

/**
 * Diese Klasse bietet Funktionen für Einheitenformationen
 *
 * @author michael
 */
public class Formation {

    /**
     * erstellt eine Quadratformation am Ziel, die in die Richtung des Vektors zeigt
     * @param unitCount - die Zahl der Einheiten
     * @param position  - Die Mitte der Formation
     * @param vector    - Die Richtung in die die Formation zeigen soll
     * @param distance  - Der Abstand zwischen den Einheiten
     */
    public static FloatingPointPosition[] createSquareFormation(int unitCount, FloatingPointPosition position, FloatingPointPosition vector, double distance) {

        // RückgabeArray initialisieren:
        FloatingPointPosition formation[] = new FloatingPointPosition[unitCount];

        // Berechnen, wie viele Einheiten breit die Formation wird:
        int side = (int) Math.sqrt(unitCount);

        // Die Koordinaten der linken oberen Einheit der Formation:
        double luX, luY;
        luX = 0 - (((side - 1) * distance) / 2);
        luY = 0 - (((side - 1) * distance) / 2);
        FloatingPointPosition leftuppercorner = new FloatingPointPosition(luX, luY);

        // Die Formation aufbauen:
        // (Ein Gitter mit den angegebenen Startkoordinaten und dem gegebenen Abstand erstellen)
        double posX, posY;
        int fpos = 0;
        for (int x = 0; x < side; x++) {
            for (int y = 0; y < side; y++) {
                formation[fpos] = new FloatingPointPosition(luX + x * distance, luY + y * distance);
                fpos++;
            }
        }

        // Die Formation Rotieren:

        // Winkel zwischen Standardrotation und dem gegebenen Vektor berechnen:
        double lenght = Math.sqrt((vector.getX() * vector.getX()) + (vector.getY() * vector.getY()));
        double skalar = vector.getY();
        double cosrot = skalar / lenght;
        double rotation = Math.acos(cosrot);

        // Jeden Punkt der Formation drehen:
        for (int i = 0; i < formation.length; i++) {
            double x = formation[i].getX();
            double y = formation[i].getY();
            double dist = Math.sqrt((x * x) + (y * y));

            double newRot = rotation + Math.atan2(x, y);

            formation[i].setfX(Math.cos(newRot) * dist);
            formation[i].setfY(Math.sin(newRot) * dist);
        }

        // fertige Formation zurückgeben:
        return formation;
    }
}
