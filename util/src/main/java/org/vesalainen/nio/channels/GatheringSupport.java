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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

/**
 * Implementing GatheringSupport adds GatheringByteChannel to WritableByteChannel
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface GatheringSupport extends GatheringByteChannel
{
    /**
     * Locks channel for writing
     */
    void writeLock();
    /**
     * Unlocks channel for writing
     */
    void writeUnlock();
    /**
     * Default implementations
     * @param srcs
     * @param offset
     * @param length
     * @return
     * @throws IOException 
     */
    @Override
    default long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        writeLock();
        try
        {
            long res = 0;
            for  (int ii=0;ii<length;ii++)
            {
                ByteBuffer bb = srcs[ii+offset];
                if (bb.hasRemaining())
                {
                    res += write(bb);
                    if (bb.hasRemaining())
                    {
                        break;
                    }
                }
            }
            return res;
        }
        finally
        {
            writeUnlock();
        }
    }

    @Override
    default long write(ByteBuffer[] srcs) throws IOException
    {
        return write(srcs, 0, srcs.length);
    }
    
}
