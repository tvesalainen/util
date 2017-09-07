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
package org.vesalainen.vfs.arch.cpio;

import java.nio.ByteBuffer;
import java.util.zip.Checksum;

/**
 * SimpleChecksum calculates checksum by adding unsigned bytes. 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleChecksum implements Checksum
{
    private long value;
    
    public void update(ByteBuffer buffer)
    {
        while (buffer.hasRemaining())
        {
            update(buffer.get());
        }
    }
    
    @Override
    public void update(int b)
    {
        value += b & 0xff;
    }

    @Override
    public void update(byte[] b, int off, int len)
    {
        for (int ii=0;ii<len;ii++)
        {
            update(b[ii+off]);
        }
    }

    @Override
    public long getValue()
    {
        return value;
    }

    @Override
    public void reset()
    {
        value = 0;
    }
    
}
