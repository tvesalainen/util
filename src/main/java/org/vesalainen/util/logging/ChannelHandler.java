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
package org.vesalainen.util.logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author tkv
 */
public class ChannelHandler extends Handler
{
    private final ByteBuffer bb = ByteBuffer.allocateDirect(4096);
    private final WritableByteChannel channel;
    public ChannelHandler(WritableByteChannel channel)
    {
        this.channel = channel;
        setFormatter(new MinimalFormatter());
    }
    @Override
    public void publish(LogRecord record)
    {
        try
        {
            Formatter formatter = getFormatter();
            bb.clear();
            bb.put(formatter.format(record).getBytes());
            bb.flip();
            channel.write(bb);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }
    
}
