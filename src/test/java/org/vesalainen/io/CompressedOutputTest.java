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
package org.vesalainen.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class CompressedOutputTest
{
    static final double Epsilon = 1e-10;
    public CompressedOutputTest()
    {
    }

    @Test
    public void test0()
    {
        try
        {
            TestCls exp = new TestCls();
            exp.b = true;
            exp.bb = 66;
            exp.c = 'C';
            exp.d = 124567.12345;
            exp.f = 123.456F;
            exp.i = 123456;
            exp.l = 123456789;
            exp.s = 123;
            TestCls got = new TestCls();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CompressedOutput co = new CompressedOutput<TestCls>(baos, exp);
            co.write();
            exp.d = 124567.12346;
            float rate = co.write();
            rate = co.write();
            assertEquals(0, rate, Epsilon);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            CompressedInput ci = new CompressedInput(bais, got);
            ci.read();
            exp.d = 124567.12345;
            equals(exp, got);
            ci.read();
            exp.d = 124567.12346;
            equals(exp, got);
            rate = ci.read();
            equals(exp, got);
            assertEquals(0, rate, Epsilon);
            try
            {
                rate = ci.read();
                fail("should throw EOFException");
            }
            catch (EOFException ex)
            {
                
            }
            
        }
        catch (IOException ex)
        {
            Logger.getLogger(CompressedOutputTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void equals(TestCls exp, TestCls got)
    {
        assertEquals(exp.b, got.b);
        assertEquals(exp.bb, got.bb);
        assertEquals(exp.c, got.c);
        assertEquals(exp.d, got.d, Epsilon);
        assertEquals(exp.f, got.f, Epsilon);
        assertEquals(exp.i, got.i);
        assertEquals(exp.l, got.l);
        assertEquals(exp.s, got.s);
    }
    public class TestCls
    {
        public boolean b;
        public byte bb;
        public char c;
        public short s;
        public int i;
        public long l;
        public float f;
        public double d;
    }
}
