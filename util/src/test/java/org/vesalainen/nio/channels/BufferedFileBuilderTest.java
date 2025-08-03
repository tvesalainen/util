/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.util.Random;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BufferedFileBuilderTest
{
    Path file;
    public BufferedFileBuilderTest()
    {
    }

    @Before
    public void before() throws IOException
    {
        file = Files.createTempFile("test", ".tmp");
    }
    @After
    public void after() throws IOException
    {
        Files.deleteIfExists(file);
    }
    @Test
    public void test() throws IOException
    {
        Random r = new Random(12345678);
        byte[] buffer = new byte[100];
        r.nextBytes(buffer);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        try (BufferedFileBuilder b = new BufferedFileBuilder(100, true, file, CREATE, WRITE))
        {
            b.order(ByteOrder.LITTLE_ENDIAN);
            b.put(byteBuffer);
            b.put((byte)r.nextInt(127));
            b.put(buffer);
            b.put("ällökääkkä", UTF_8);
            b.put(buffer, 10, 10);
            b.putChar((char) r.nextInt(30000));
            b.putDouble(r.nextDouble());
            b.putFloat(r.nextFloat());
            b.putInt(r.nextInt());
            b.putLong(r.nextLong());
            b.putShort((short) r.nextInt(30000));
        }
        r = new Random(12345678);
        r.nextBytes(buffer);
        try (FileChannel fc = FileChannel.open(file))
        {
            ByteBuffer bb = ByteBuffer.allocate(256);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            fc.read(bb);
            bb.flip();
            ByteBuffer slice = bb.slice();
            slice.limit(100);
            for (int ii=0;ii<100;ii++)
            {
                assertEquals(byteBuffer.get(ii), slice.get(ii));
            }
            bb.position(100);
            assertEquals(r.nextInt(127), bb.get());
            byte[] buffer2 = new byte[100];
            bb.get(buffer2);
            assertArrayEquals(buffer, buffer2);
            bb.get(buffer2, 0, 15);
            String s = new String(buffer2, 0, 15, UTF_8);
            assertEquals("ällökääkkä", s);
            bb.get(buffer2, 0, 10);
            assertEquals(r.nextInt(30000), bb.getChar());
            assertEquals(r.nextDouble(), bb.getDouble(), 1e-8);
            assertEquals(r.nextFloat(), bb.getFloat(), 1e-8);
            assertEquals(r.nextInt(), bb.getInt());
            assertEquals(r.nextLong(), bb.getLong());
            assertEquals(r.nextInt(30000), bb.getShort());
            
        }
    }
    
}
