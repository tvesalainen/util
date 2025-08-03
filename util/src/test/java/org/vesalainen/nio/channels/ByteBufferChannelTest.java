/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ByteBufferChannelTest
{
    private static final int SIZE = 256;
    private static final int ROUNDS = 10;
    private static final long SEED = 123456789L;
    private final ByteBufferChannel[] pair;
    private final ExecutorService executor;
    
    public ByteBufferChannelTest()
    {
        pair = ByteBufferChannel.open(SIZE, false);
        executor = Executors.newCachedThreadPool();
    }

    @Test
    public void test1() throws IOException, InterruptedException
    {
        ByteBufferChannel[] arr = ByteBufferChannel.open(SIZE, false);
        ByteBuffer bb = ByteBuffer.wrap("qwerty".getBytes());
        ByteBufferChannel bbc = arr[0];
        bbc.setWriteTimeout(0, TimeUnit.DAYS);
        int rc = bbc.write(bb);
        while (rc != 0)
        {
            bb.clear();
            rc = bbc.write(bb);
        }
    }
    @Test
    public void testRegression() throws IOException, InterruptedException
    {
        executor.submit(this::echo);
        ByteBufferChannel bbc = pair[0];
        Random random = new Random(SEED);
        for (int ii=0;ii<ROUNDS;ii++)
        {
            int size = random.nextInt(SIZE-1)+1;
            byte[] array = new byte[size];
            random.nextBytes(array);
            byte[] exp = Arrays.copyOf(array, size);
            ByteBuffer bb = ByteBuffer.wrap(array);
            bbc.write(bb);
            System.err.println("sent "+bb);
            bb.flip();
            bbc.read(bb);
            assertArrayEquals(exp, bb.array());
        }
        executor.shutdownNow();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }
    private void echo()
    {
        try
        {
            ByteBufferChannel bbc = pair[1];
            ByteBuffer bb = ByteBuffer.allocate(SIZE);
            while (true)
            {
                bb.clear();
                bbc.read(bb);
                bb.flip();
                bbc.write(bb);
                System.err.println("echo "+bb);
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }        
    }
}
