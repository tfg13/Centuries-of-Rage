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
        FloatingPointPosition[] formation = new FloatingPointPosition[unitCount];
        for (int i = 0; i < formation.length; i++) {
            formation[i] = new FloatingPointPosition(0, 0);
        }

        int side = 0;
        for (int i = 0; i < 10; i++) {
            if ((i * i) < unitCount && unitCount <= ((i + 1) * (i + 1))) {
                side = ++i;
                break;
            }
        }

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
                if (fpos >= formation.length) {
                    x = side;// äußere schleife verlassen
                    break;// innere schleife verlassen
                }
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

    public static void around() {

        FloatingPointPosition position = new FloatingPointPosition(0, 0);
        // abstand der punkte
        double distance = 5.0;

        // gefundene positionen / zahl der gesuchten positionen
        int foundPositions = 0, searchedPositions = 10;

        // Richtung (1=E, 2=N, 3=W, 4=S)
        int direction = 1;

        // Wie viele sSchritte gecheckt werden
        int steps = 1;

        // wenn true wird steps erhöt
        boolean increaseStepFlag = false;

        // solange suchen, bis genügend positionen gefunden wurden:
        while (foundPositions < searchedPositions) {
            // X/Y-Veränderung
            double dx = 0, dy = 0;
            switch (direction) {
                case 1:
                    dx = distance;
                    dy = 0;
                    break;
                case 2:
                    dx = 0;
                    dy = distance;
                    break;
                case 3:
                    dx = -distance;
                    dy = 0;
                    break;
                case 4:
                    dx = 0;
                    dy = -distance;
                    break;
            }

            for (int i = 0; i < steps; i++) {
                position.setfX(position.getfX() + dx);
                position.setfY(position.getfY() + dy);

                // TODO: prüfen, ob die position frei ist, wenn ja hinzufügen
                System.out.println("POS: " + position.toString());
                foundPositions++;
            }

            // Richtung ändern:
            direction++;
            if (direction > 4) {
                direction = 1;
            }

            // Wenn die Flag schon gesetzt ist inkrementieren, sonst flag setzten
            //(dadurch wird steps bei  jedem 2. Richtungswechsel inkrementiert, was in einer kreisbewegung resultiert)
            if (increaseStepFlag == true) {
                increaseStepFlag = false;
                steps++;
            } else {
                increaseStepFlag = true;
            }

        }






    }

}
