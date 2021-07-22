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
package org.vesalainen.can;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import static java.util.logging.Level.*;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SocketCanService extends AbstractCanService
{
    private ByteChannel channel;
    private ByteBuffer frame;

    protected SocketCanService(ByteChannel channel, CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        super(executor, compiler);
        this.channel = channel;
        this.frame = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);  // Raspian Pi!!!
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                frame.clear();
                int rc = channel.read(frame);
                if (rc != 16)
                {
                    warning("didn't read full frame");
                    continue;
                }
                process();
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "%s", ex.getMessage());
            }
        }
    }

    private void process()
    {
        int rawId = frame.getInt(0);
        if ((rawId & 0b1100000000000000000000000000000) != 0)
        {
            warning("\nEFF/SFF is set in the MSB %s", HexDump.toHex(frame, 0, 16));
        }
        else
        {
            if ((rawId & 0b10000000000000000000000000000000) != 0)
            {
                rawFrame(rawId & 0b11111111111111111111111111111);
            }
            else
            {
                rawFrame(rawId);
            }
        }
        finest("\n%s", HexDump.toHex(frame, 0, 16));
    }

    @Override
    public int getLength()
    {
        return frame.get(4);
    }
    @Override
    public ByteBuffer getFrame()
    {
        return frame;
    }
    
    @Override
    public void readData(byte[] data, int offset)
    {
        int length = getLength();
        frame.position(8);
        frame.get(data, offset, length);
    }

}
