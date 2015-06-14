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

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class ConvertUtilityTest
{
    
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
    }
    
}
