/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.navi.CardinalDirection.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CardinalDirectionTest
{
    
    public CardinalDirectionTest()
    {
    }

    @Test
    public void testCardinal()
    {
        assertEquals(N, CardinalDirection.cardinal(0));
        assertEquals(N, CardinalDirection.cardinal(44));
        assertEquals(N, CardinalDirection.cardinal(360-44));
        assertEquals(N, CardinalDirection.cardinal(-20));
        assertEquals(S, CardinalDirection.cardinal(170));
        assertEquals(W, CardinalDirection.cardinal(360-46));
    }
    @Test
    public void testInterCardinal()
    {
        assertEquals(N, CardinalDirection.interCardinal(0));
        assertEquals(NE, CardinalDirection.interCardinal(44));
        assertEquals(NW, CardinalDirection.interCardinal(360-44));
        assertEquals(N, CardinalDirection.interCardinal(-20));
        assertEquals(S, CardinalDirection.interCardinal(170));
        assertEquals(NW, CardinalDirection.interCardinal(360-46));
    }
    @Test
    public void testSecondaryInterCardinal()
    {
        assertEquals(N, CardinalDirection.secondaryInterCardinal(0));
        assertEquals(NE, CardinalDirection.secondaryInterCardinal(44));
        assertEquals(NW, CardinalDirection.secondaryInterCardinal(360-44));
        assertEquals(NNW, CardinalDirection.secondaryInterCardinal(-20));
        assertEquals(S, CardinalDirection.secondaryInterCardinal(170));
        assertEquals(NW, CardinalDirection.secondaryInterCardinal(360-46));
    }
    
}
