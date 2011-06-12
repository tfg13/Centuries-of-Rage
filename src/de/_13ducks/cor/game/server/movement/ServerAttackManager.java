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

import de._13ducks.cor.game.GameObject;
import de._13ducks.cor.game.Unit;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Verwaltet Angriffssystem-Sachen, die für das Ganze Spiel global sind.
 */
public class ServerAttackManager {
    
    /**
     * Hiermit wird das Schaden zufügen getimed.
     */
    private Timer timer;
    
    /**
     * Erstellt einen neuen ServerAttackManager
     */
    public ServerAttackManager() {
        timer = new Timer("server_atktimer", true);
    }
    
    /**
     * Wird dem angegebenen Objekt nach delay Millisekunden damage Schaden zufügen.
     * @param victim Das Opfer, ihm wird Schaden zugefügt
     * @param damage Der Schaden in Hitpoints
     * @param delay Delay (Geschoss-Flugzeit) in ms
     */
    public void delayDamageTo(GameObject victim, int damage, int delay) {
        timer.schedule(new delayedDamage(victim, damage), delay);
    }
    
    private class delayedDamage extends TimerTask {
        
        private final GameObject go;
        private final int damage;
        
        delayedDamage(GameObject go, int damage) {
            this.go = go;
            this.damage = damage;
        }

        @Override
        public void run() {
            go.dealDamageS(damage);
        }
        
    }
    
    

    /**
     * Ein Client-Atkrequest geht ein.
     * @param target Das gemeinsame Angriffsziel
     * @param attackers Eine Liste mit allen angreifenden Einheiten
     * @param focusfire true, wenn focusfire gewünscht ist.
     */
    public void atkRequest(GameObject target, ArrayList<Unit> attackers, boolean focusfire) {
        // Erstmal alle anhalten
        for (Unit unit : attackers) {
            unit.getTopLevelManager().stopForFight(unit);
        }
        
        // Jetzt bei allen das Ziel einstellen
        for (Unit unit : attackers) {
            unit.getAtkManager().setAttack(target, focusfire);
        }
    }
    
}
