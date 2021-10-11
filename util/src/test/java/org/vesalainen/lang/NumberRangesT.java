/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NumberRangesT
{
    
    public NumberRangesT()
    {
    }

    @Test
    public void create()
    {
        System.err.println("static final int[] ByteSafeLength = new int[] {");
        print(Byte.MAX_VALUE);
        System.err.println("static final int[] ShortSafeLength = new int[] {");
        print(Short.MAX_VALUE);
        System.err.println("static final int[] IntSafeLength = new int[] {");
        print(Integer.MAX_VALUE);
        System.err.println("static final int[] LongSafeLength = new int[] {");
        print(Long.MAX_VALUE);
    }
    
    private static void print(long max)
    {
        print(max, null);
    }
    private static void print(long max, Class<?> type)
    {
        for (int radix=0;radix<Character.MAX_RADIX;radix++)
        {
            if (radix < Character.MIN_RADIX)
            {
                System.err.println("0,");
            }
            else
            {
                String str = Long.toUnsignedString(max, radix);
                int len = str.length()-1;
                System.err.print(len);
                if (radix+1<Character.MAX_RADIX)
                {
                    System.err.println(",");
                }
                else
                {
                    System.err.println("");
                }
            }
        }
        System.err.println("};");
    }
}
