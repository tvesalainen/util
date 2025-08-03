/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
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
            ObjectCompressedOutput co = new ObjectCompressedOutput<TestCls>(baos, exp);
            co.write();
            exp.d = 124567.12346;
            float rate = co.write();
            rate = co.write();
            assertEquals(0, rate, Epsilon);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectCompressedInput ci = new ObjectCompressedInput(bais, got);
            assertEquals(co.getUuid(), ci.getUuid());
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
    
    @Test
    public void test1()
    {
        try
        {
            TestCls1 exp = new TestCls1();
            TestCls1 got = new TestCls1();
            
            Random random = new Random(123456L);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectCompressedOutput co = new ObjectCompressedOutput<TestCls1>(baos, exp);
            for (int ii=0;ii<1000;ii++)
            {
                exp.time = random.nextLong();
                exp.latitude = random.nextFloat();
                exp.longitude = random.nextFloat();
                co.write();
            }
            co.close();
            
            random = new Random(123456L);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectCompressedInput ci = new ObjectCompressedInput(bais, got);
            int cnt = 0;
            try
            {
                while (true)
                {
                    ci.read();
                    cnt++;
                    assertEquals(random.nextLong(), got.time);
                    assertEquals(random.nextFloat(), got.latitude, Epsilon);
                    assertEquals(random.nextFloat(), got.longitude, Epsilon);
                }
            }
            catch (EOFException ex)
            {
                
            }
            assertEquals(1000, cnt);
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
    public class TestCls1
    {
        public long time;
        public float latitude;
        public float longitude;
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
