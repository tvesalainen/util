/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DynamicByteBufferTest
{
    static final Path PATH = Paths.get("testFile");
    public DynamicByteBufferTest()
    {
    }

    @After
    public void after() throws IOException
    {
        System.gc();
        Files.deleteIfExists(PATH);
    }
    @Test
    public void testCreate() throws IOException
    {
        ByteBuffer bb = DynamicByteBuffer.create(8192);
        bb.putDouble(123.456);
        bb.mark();
        bb.position(1024);
        ByteBuffer slice = bb.slice();
        bb.reset();
        slice.putLong(1234567890L);
        assertEquals(1234567890L, bb.getLong(1024));
    }
    //@Test // problem with deleting PATH
    public void testCreatePath() throws IOException
    {
        ByteBuffer bb = DynamicByteBuffer.create(PATH, 8192);
        bb.putDouble(123.456);
        bb.mark();
        bb.position(1024);
        ByteBuffer slice = bb.slice();
        bb.reset();
        slice.putLong(1234567890L);
        assertEquals(1234567890L, bb.getLong(1024));
    }
    
}
