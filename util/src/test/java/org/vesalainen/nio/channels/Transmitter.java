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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import org.vesalainen.comm.channel.SerialChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Transmitter implements Callable<Void>
{
    private final SerialChannel channel;
    private final ByteBuffer bb = ByteBuffer.allocate(70);
    private final int count;
    private RandomChar rand;

    public Transmitter(SerialChannel channel, int count)
    {
        this(channel, count, new RandomChar(channel.getDataBits().ordinal() + 4));
    }
    public Transmitter(SerialChannel channel, int count, RandomChar rand)
    {
        this.channel = channel;
        this.count = count;
        this.rand = rand;
    }

    @Override
    public Void call() throws Exception
    {
        for (int ii = 0; ii < count; ii++)
        {
            int next = rand.next();
            if (!bb.hasRemaining())
            {
                flush();
            }
            bb.put((byte) next);
        }
        flush();
        //System.err.println("transmitted all");
        return null;
    }
    private void flush() throws IOException
    {
        bb.flip();
        channel.write(bb);
        bb.clear();
    }
    
}
