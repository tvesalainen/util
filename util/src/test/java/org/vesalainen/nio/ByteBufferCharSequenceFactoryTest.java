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
package org.vesalainen.nio;

import java.nio.ByteBuffer;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.CharSequences;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferCharSequenceFactoryTest
{
    
    public ByteBufferCharSequenceFactoryTest()
    {
    }

    @Test
    public void test1()
    {
        ByteBuffer bb = ByteBuffer.wrap("012345678901234567890123456789".getBytes());
        ByteBufferCharSequenceFactory bbcsf = new ByteBufferCharSequenceFactory(bb);
        ByteBufferCharSequence s1 = bbcsf.create(0, 9);
        assertTrue(CharSequences.equals("012345678", s1));
        ByteBufferCharSequence s2 = bbcsf.create(9, 23);
        assertTrue(CharSequences.equals("90123456789012", s2));
        ByteBufferCharSequence s3 = bbcsf.concat(s1, s2);
        assertTrue(CharSequences.equals("01234567890123456789012", s3));
        assertTrue(CharSequences.equals("123", s3.subSequence("123")));
    }
    
    @Test
    public void test2()
    {
        ByteBuffer bb = ByteBuffer.wrap("012345678901234567890123456789".getBytes());
        ByteBufferCharSequenceFactory bbcsf = new ByteBufferCharSequenceFactory(bb);
        ByteBufferCharSequence s1 = bbcsf.create(0, 9);
        ByteBufferCharSequence s2 = bbcsf.create(10, 19);
        assertTrue(CharSequences.equals(s1, s2));
        assertEquals(CharSequences.hashCode(s1), CharSequences.hashCode(s2));
        assertEquals(9, s1.length());
        assertEquals('1', s1.charAt(1));
        assertEquals(9, s2.length());
        assertEquals('1', s2.charAt(1));
        
        CharSequence s3 = s1.subSequence(1, 5);
        String str3 = s1.toString();
        assertEquals("012345678", str3);
        assertTrue(CharSequences.equals(str3.subSequence(1, 5), s3));
    }
    @Test
    public void test3()
    {
        ByteBuffer bb = ByteBuffer.wrap("012345678901234567890123456789".getBytes());
        ByteBufferCharSequenceFactory bbcsf = new ByteBufferCharSequenceFactory(bb);
        ByteBufferCharSequence s1 = bbcsf.create(0, 9);
        ByteBufferCharSequence s2 = bbcsf.create(10, 19);
        bbcsf.reset();
        s2 = bbcsf.create(10, 19);
        s1 = bbcsf.create(0, 9);
        assertTrue(CharSequences.equals("012345678", s1));
        assertTrue(CharSequences.equals(s1, s2));
    }
    @Test
    public void test4()
    {
        ByteBuffer bb = ByteBuffer.wrap("012345678901234567890123456789".getBytes());
        ByteBufferCharSequenceFactory bbcsf = new ByteBufferCharSequenceFactory(bb);
        ByteBufferCharSequence s1 = bbcsf.create(0, 9);
        CharSequence sc = CharSequences.getConstant("012345678");
        assertEquals(s1, sc);
        assertEquals(s1.hashCode(), sc.hashCode());
    }
    @Test
    public void test5()
    {
        ByteBuffer bb = ByteBuffer.wrap("abcdefghijklmnopqrstuwxyzåäö".getBytes());
        ByteBufferCharSequenceFactory bbcsf = new ByteBufferCharSequenceFactory(bb, Character::toLowerCase);
        ByteBufferCharSequence s1 = bbcsf.create(0, 9);
        CharSequence sc = CharSequences.getConstant("ABCDEFGHI", Character::toLowerCase);
        assertEquals(s1, sc);
        assertEquals(s1.hashCode(), sc.hashCode());
    }
}
