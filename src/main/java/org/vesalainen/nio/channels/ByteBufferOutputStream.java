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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

/**
 *
 * @author tkv
 */
public class ByteBufferOutputStream extends OutputStream
{
    private final ByteBuffer[] srcs;
    private int offset;
    private int length;

    public ByteBufferOutputStream(ByteBuffer src)
    {
        this.srcs = new ByteBuffer[] {src};
        this.offset = 0;
        this.length = 1;
    }

    public ByteBufferOutputStream(ByteBuffer[] srcs)
    {
        this.srcs = srcs;
        this.offset = 0;
        this.length = 1;
    }

    public ByteBufferOutputStream(ByteBuffer[] srcs, int offset, int length)
    {
        this.srcs = srcs;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public void write(int b) throws IOException
    {
        try
        {
            srcs[offset].put((byte) (b & 0xff));
        }
        catch (BufferOverflowException ex)
        {
            if (length > 1)
            {
                offset++;
                length--;
                write(b);
            }
            else
            {
                throw new IOException("buffer overflow");
            }
        }
    }
    
}
