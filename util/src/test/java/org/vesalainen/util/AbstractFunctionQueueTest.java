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
package org.vesalainen.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractFunctionQueueTest
{
    
    public AbstractFunctionQueueTest()
    {
    }

    @Test
    public void test1()
    {
        Q q = new Q(this::f, 64);
        for (int ii=0;ii<10;ii++)
        {
            q.accept((byte)1, (char)2, (short)3, 4, 5, 6F, 7);
            q.run();
        }
    }
 
    private void f(byte b, char c, short s, int i, long l, float f, double d)
    {
        assertEquals(1, b);
        assertEquals(2, c);
        assertEquals(3, s);
        assertEquals(4, i);
        assertEquals(5, l);
        assertEquals(6, f, 1e-10);
        assertEquals(7, d, 1e-10);
    }
    @FunctionalInterface
    interface Itf
    {
        void accept(byte b, char c, short s, int i, long l, float f, double d);
    }
    class Q extends AbstractFunctionQueue implements Itf
    {

        private final Itf forwarder;

        public Q(Itf forwarder, int size)
        {
            super(size);
            this.forwarder = forwarder;
        }

        @Override
        public void accept(byte b, char c, short s, int i, long l, float f, double d)
        {
            lock.lock();
            try
            {
                put(b);
                putChar(c);
                putShort(s);
                putInt(i);
                putLong(l);
                putFloat(f);
                putDouble(d);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(AbstractFunctionQueueTest.class.getName()).log(Level.SEVERE, null, ex);
            }            
            finally
            {
                lock.unlock();
            }
        }
        
        public void run()
        {
            lock.lock();
            try
            {
                forwarder.accept(
                    getByte(),
                    getChar(),
                    getShort(),
                    getInt(),
                    getLong(),
                    getFloat(),
                    getDouble()
                );
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(AbstractFunctionQueueTest.class.getName()).log(Level.SEVERE, null, ex);
            }            
            finally
            {
                lock.unlock();
            }
        }

    }
}
