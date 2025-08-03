/*
 * Copyright (C) 2014 Timo Vesalainen
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

import java.util.EnumSet;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.vesalainen.util.BigEnum.*;
import static org.vesalainen.util.SmallEnum.*;

/**
 *
 * @author Timo Vesalainen
 */
public class EnumSetFlaggerTest
{
    
    public EnumSetFlaggerTest()
    {
    }

    /**
     * Test of getFlag method, of class EnumSetFlagger.
     */
    @Test
    public void testGetFlag1()
    {
        EnumSet<BigEnum> set = EnumSet.of(E1, E3, E31);
        assertEquals(0b10000000000000000000000000001010, EnumSetFlagger.getFlag(set));
    }

    /**
     * Test of getFlag method, of class EnumSetFlagger.
     */
    @Test
    public void testGetFlag2()
    {
        EnumSet<BigEnum> set = EnumSet.of(E1, E3, E32);
        try
        {
            int flag = EnumSetFlagger.getFlag(set);
            fail("should throw IllegalArgumentException got "+flag);
        }
        catch (IllegalArgumentException ex)
        {
            
        }
    }

    /**
     * Test of getSet method, of class EnumSetFlagger.
     */
    @Test
    public void testGetSet()
    {
        EnumSet<SmallEnum> expected = EnumSet.of(S1, S3, S31);
        EnumSet<SmallEnum> got = EnumSetFlagger.getSet(SmallEnum.class, 0b10000000000000000000000000001010);
        assertEquals(expected, got);
    }
    
}
