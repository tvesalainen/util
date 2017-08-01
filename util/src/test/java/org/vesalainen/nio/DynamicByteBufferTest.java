/*
 * Copyright (C) 2017 tkv
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
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class DynamicByteBufferTest
{
    
    public DynamicByteBufferTest()
    {
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
    
}
