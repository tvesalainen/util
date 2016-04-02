/*
 * Copyright (C) 2016 tkv
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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class CharSequencesTest
{
    
    public CharSequencesTest()
    {
    }

    @Test
    public void test1()
    {
        String s1 = "qw\rerty\r\n\r\ncontent";
        int exp = s1.indexOf("\r\n\r\n");
        assertEquals(exp, CharSequences.indexOf(s1, "\r\n\r\n"));
    }
    
    @Test
    public void test2()
    {
        String s1 = "\r\n\r\ncontent";
        int exp = s1.indexOf("\r\n\r\n");
        assertEquals(exp, CharSequences.indexOf(s1, "\r\n\r\n"));
    }
    
    @Test
    public void test3()
    {
        String s1 = "qw\r\n\r\nerty\r\n\r\ncontent";
        int exp = s1.indexOf("\r\n\r\n", 5);
        assertEquals(exp, CharSequences.indexOf(s1, "\r\n\r\n", 5));
    }
    
    @Test
    public void test4()
    {
        String s1 = "qwertyuikjhgfdsazxcvbnmkoiu";
        int exp = s1.indexOf("\r\n\r\n");
        assertEquals(exp, CharSequences.indexOf(s1, "\r\n\r\n"));
    }
    
    @Test
    public void test5()
    {
        String s1 = "qwertyuikjhgfdsazxcvbnmkoiu";
        assertTrue(CharSequences.equals(s1, s1));
    }
    
    @Test
    public void test6()
    {
        String s1 = "qwertyuikjhgfdsazxcvbnmkoiu";
        String s2 = "qwertyuikjägfdsazxcvbnmkoiu";
        assertFalse(CharSequences.equals(s1, s2));
    }
    
    @Test
    public void test7()
    {
        String s1 = "qwertyuikjhgfdsazxcvbnmkoiu";
        String s2 = "qwertyuikjhgfdsazxcvbnmkoi";
        assertFalse(CharSequences.equals(s1, s2));
    }
}
