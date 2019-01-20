/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
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
    public void test5_b()
    {
        String s1 = "qwertyuikjhgfdsazxcvbnmkoiu";
        String s2 = s1.toUpperCase();
        assertTrue(CharSequences.equals(s1, s2, Character::toLowerCase));
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
    
    @Test
    public void test8()
    {
        String s1 = "\r\n\r\ncontent";
        int exp = s1.indexOf('c');
        assertEquals(exp, CharSequences.indexOf(s1, 'c'));
    }
    
    @Test
    public void test9()
    {
        String s1 = "\r\n\r\ncontent";
        int exp = s1.indexOf('c', 6);
        assertEquals(exp, CharSequences.indexOf(s1, 'c', 6));
    }
    
    @Test
    public void test10()
    {
        String s1 = "qwertyuikjhgfdsazxcvbnmkoiu";
        StringBuilder sb = new StringBuilder();
        sb.append(s1);
        int h1 = CharSequences.hashCode(s1);
        int h2 = CharSequences.hashCode(sb);
        assertEquals(h1, h2);
    }
    
    @Test
    public void test10_b()
    {
        String s1 = "qwertyuikjhgfdsazxcvbnmkoiu";
        StringBuilder sb = new StringBuilder();
        sb.append(s1.toUpperCase());
        int h1 = CharSequences.hashCode(s1, Character::toLowerCase);
        int h2 = CharSequences.hashCode(sb, Character::toLowerCase);
        assertEquals(h1, h2);
    }
    
    @Test
    public void testlastIndexOf()
    {
        assertEquals(3, CharSequences.lastIndexOf("012345", '3'));
        assertEquals(-1, CharSequences.lastIndexOf("012345", 'a'));
        assertEquals(5, CharSequences.lastIndexOf("012345", '5'));
        assertEquals(0, CharSequences.lastIndexOf("012345", '0'));
    }
    @Test
    public void testTrim()
    {
        assertEquals("qwerty", CharSequences.trim("\t\r\n  qwerty\t\t\r\n  \t"));
        assertEquals("", CharSequences.trim("\t\r\n  \t\t\r\n  \t"));
        assertEquals("qwerty", CharSequences.trim("qwerty"));
        assertTrue("qwerty" == CharSequences.trim("qwerty"));
        assertEquals("", CharSequences.trim(""));
    }
    @Test
    public void testSplit()
    {
        assertEquals(0, CharSequences.split("", ',').count());
        assertEquals(4, CharSequences.split(",,,", ',').count());
        List<CharSequence> list = CharSequences.split("W/\"xyzzy\", W/\"r2d2xxxx\", W/\"c3piozzzz\"", ',').collect(Collectors.toList());
        assertEquals(3, list.size());
        assertEquals("W/\"xyzzy\"", list.get(0));
        assertEquals(" W/\"r2d2xxxx\"", list.get(1));
        assertEquals(" W/\"c3piozzzz\"", list.get(2));
    }
    @Test
    public void testStartsWith()
    {
        assertTrue(CharSequences.startsWith("qwerty", 'q'));
        assertTrue(CharSequences.startsWith("qwerty", "qw"));
        assertFalse(CharSequences.startsWith("qwerty", 'e'));
        assertFalse(CharSequences.startsWith("qwerty", "ww"));
        assertFalse(CharSequences.startsWith("qwerty", "asdfghjkl"));
    }
    @Test
    public void testEndsWith()
    {
        assertTrue(CharSequences.endsWith("qwerty", 'y'));
        assertTrue(CharSequences.endsWith("qwerty", "ty"));
        assertFalse(CharSequences.endsWith("qwerty", 'e'));
        assertFalse(CharSequences.endsWith("qwerty", "ww"));
        assertFalse(CharSequences.endsWith("qwerty", "asdfghjkl"));
    }
    @Test
    public void testCompare()
    {
        assertTrue(comp("qwerty", "qwerty"));
        assertTrue(comp("qwertya", "qwerty"));
        assertTrue(comp("qwert", "qwerty"));
        assertTrue(comp("abc", "zdf"));
        assertTrue(comp("åöä", "abc"));
    }
    private boolean comp(String s1, String s2)
    {
        return compSign(CharSequences.compare(s1, s2), s1.compareTo(s2));
    }
    private boolean compSign(int c1, int c2)
    {
        return 
                (c1 < 0 && c2 < 0) ||
                (c1 > 0 && c2 > 0) ||
                (c1 == 0 && c2 == 0);
    }
    @Test
    public void testAsciiCharSequence()
    {
        byte[] bytes = "qwerty1234567890".getBytes(Charset.forName("ASCII"));
        CharSequence seq1 = CharSequences.getAsciiCharSequence(bytes);
        assertEquals(16, seq1.length());
        assertEquals('q', seq1.charAt(0));
        assertEquals('0', seq1.charAt(15));
        CharSequence seq2 = seq1.subSequence(3, 6);
        assertEquals(3, seq2.length());
        assertEquals('r', seq2.charAt(0));
        assertEquals('y', seq2.charAt(2));
    }
    @Test
    public void testToUpper()
    {
        CharSequence toUpper = CharSequences.toUpper("qwerty");
        assertEquals("QWERTY", toUpper.toString());
    }
}
