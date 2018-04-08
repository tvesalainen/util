/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.util;

import java.awt.Color;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GridFillerTest
{
    
    public GridFillerTest()
    {
    }

    @Test
    public void testAllDirections1()
    {
        ArrayGridFiller<Color> agf = new ArrayGridFiller(5, 8, GridFiller::allDirections, Color.class);
        assertEquals(agf.getArray().length, agf.fill(0, 0, null));
    }
    @Test
    public void testAllDirections2()
    {
        ArrayGridFiller<Color> agf = new ArrayGridFiller(5, 8, GridFiller::allDirections, Color.class);
        Color[] array = agf.getArray();
        array[5] = Color.BLACK;
        array[8] = Color.GREEN;
        array[12] = Color.CYAN;
        array[32] = Color.BLUE;
        assertEquals(array.length-4, agf.fill(0, 0, null));
    }
    @Test
    public void testRoundedSquare1()
    {
        ArrayGridFiller<Color> agf = new ArrayGridFiller(5, 8, GridFiller::roundedSquare, Color.class);
        assertEquals(1, agf.fill(0, 0, null));
    }
    @Test
    public void testRoundedSquare2()
    {
        ArrayGridFiller<Color> agf = new ArrayGridFiller(5, 8, GridFiller::roundedSquare, Color.class);
        Color[] array = agf.getArray();
        assertEquals(array.length-4, agf.fill(3, 4, null));
    }
    @Test
    public void testRoundedSquare3()
    {
        ArrayGridFiller<Color> agf = new ArrayGridFiller(5, 8, GridFiller::roundedSquare, Color.class);
        //agf.addConsumer((x,y,c)->System.err.println(x+", "+y));
        Color[] array = agf.getArray();
        array[20] = Color.BLACK;
        array[22] = Color.BLACK;
        array[24] = Color.BLACK;
        assertEquals(16, agf.fill(2, 2, null));
    }
    @Test
    public void testRoundedSquareNotBoxed()
    {
        ArrayGridFiller<Color> agf = new ArrayGridFiller(5, 8, false, GridFiller::roundedSquare, Color.class);
        //agf.addConsumer((x,y,c)->System.err.println(x+", "+y));
        Color[] array = agf.getArray();
        agf.setColor(2, 0, Color.BLACK);
        agf.setColor(2, 2, Color.BLACK);
        agf.setColor(2, 4, Color.BLACK);
        agf.setColor(2, 6, Color.BLACK);
        assertEquals(28, agf.fill(3, 3, null));
    }
    @Test
    public void testSquare3()
    {
        ArrayGridFiller<Color> agf = new ArrayGridFiller(5, 8, GridFiller::square, Color.class);
        //agf.addConsumer((x,y,c)->System.err.println(x+", "+y));
        Color[] array = agf.getArray();
        array[20] = Color.BLACK;
        array[22] = Color.BLACK;
        array[24] = Color.BLACK;
        assertEquals(20, agf.fill(2, 2, null));
    }

}
