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

import de._13ducks.cor.game.SimplePosition;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.newdawn.slick.geom.Circle;

/**
 *
 * @author Tulius <tobifleig@gmail.com>
 */
public class MathUtilTest {
    
    public MathUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testCircleCircleIntersection() {
        System.out.println("circleCircleIntersection");
        Circle c1 = new Circle(5, 5, 3);
        Circle c2 = new Circle(5, 10,3);
        SimplePosition[] expResult = new SimplePosition[]{new Vector(6.6583, 7.5), new Vector(3.341, 7.5)};
        SimplePosition[] result = MathUtil.circleCircleIntersection(c1, c2);
        assertArrayEquals(expResult, result);
        
        Circle c3 = new Circle(-1.5f, -2f, 3.5f);
        Circle c4 = new Circle(-.5f, -3.5f, 3.937f);
        SimplePosition[] expResult2 = new SimplePosition[]{new Vector(-4.41217, -3.9414), new Vector(1.4121, -0.058549)};
        SimplePosition[] result2 = MathUtil.circleCircleIntersection(c3, c4);
        assertArrayEquals(expResult2, result2);
    }
}
