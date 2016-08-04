/*
 * Copyright (C) 2015 tkv
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

import java.io.File;
import java.util.Date;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.code.JavaType;

/**
 *
 * @author tkv
 */
public class ConvertUtilityTest
{
    private static final double Epsilon = 1e-10;
    public ConvertUtilityTest()
    {
    }

    @Test
    public void test1()
    {
        //Assert.assertArrayEquals(new int[] {1,2,3}, ConvertUtility.convert(int[].class, new String[] {"1","2","3"}));
        assertEquals((Integer)1, (Integer)ConvertUtility.convert(Integer.class, "1"));
        Date date = new Date();
        assertEquals(date, ConvertUtility.convert(Date.class, date.getTime()));
        assertEquals(Level.FINER, ConvertUtility.convert(Level.class, "FINER"));
        assertEquals(JavaType.BOOLEAN, ConvertUtility.convert(JavaType.class, "BOOLEAN"));
        assertEquals(123.456, ConvertUtility.convert(Double.class, Double.valueOf(123.456)), Epsilon);
        assertEquals(new File("f.txt"), ConvertUtility.convert(File.class, new File("f.txt")));
    }
    
}
