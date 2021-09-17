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
package org.vesalainen.can.socketcand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.nio.ByteBufferCharSequence;
import org.vesalainen.nio.ByteBufferInputStream;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.xml.SimpleXMLParser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SocketCandService extends AbstractCanService
{
    private final ByteBufferCharSequence openBus;
    private final ByteBufferCharSequence rawMode = new ByteBufferCharSequence("< rawmode >");
    private SocketChannel channel;
    private ByteBuffer data;
    private InputReader input;
    private long dataStart;
    private int dataLength;
    private long timeStart;
    private int timeLength;
    
    public SocketCandService(String canBus, CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        super(executor, compiler);
        this.openBus = new ByteBufferCharSequence("< open "+canBus+" >");
        this.data = ByteBuffer.allocate(8);
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                SocketCandInfo info = waitForBeacon();
                channel = SocketChannel.open(info.getAddress());
                readSocketCand(channel);
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, "exit SocketCandService", ex);
                return;
            }
        }
    }
    
    private SocketCandInfo waitForBeacon() throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        UnconnectedDatagramChannel channel = UnconnectedDatagramChannel.open("255.255.255.255", 42000, 1024, true, false);
        buffer.clear();
        channel.read(buffer);
        buffer.flip();
        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);
        SimpleXMLParser parser = new SimpleXMLParser(bbis);
        return new SocketCandInfo(parser.getRoot());
    }

    private void readSocketCand(SocketChannel channel)
    {
        SocketCandParser parser = SocketCandParser.getInstance();
        parser.parse(channel, this);
    }

    void setInput(InputReader input)
    {
        this.input = input;
    }
    
    void openBus()
    {
        try
        {
            ByteBufferCharSequence.writeAll(channel, openBus);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    void rawMode()
    {
        try
        {
            ByteBufferCharSequence.writeAll(channel, rawMode);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    void frame(int canId)
    {
        rawFrame(canId);
    }

    @Override
    public ByteBuffer getFrame()
    {
        data.clear();
        for (int ii=0;ii<dataLength;ii+=2)
        {
            data.put((byte) (Character.digit(input.get(dataStart+ii), 16)<<4|Character.digit(input.get(dataStart+ii+1), 16)));
        }
        data.flip();
        return data;
    }

    @Override
    public Instant getInstant()
    {
        return super.getInstant();
    }

    @Override
    public LongSupplier getMillisSupplier()
    {
        return ()->millis(timeStart, timeLength);
    }

    @Override
    public long getMillis()
    {
        return millis(timeStart, timeLength);
    }

    private long millis(long start, int length)
    {
        long m = 0;
        boolean decimal = false;
        int dec = 0;
        for (int ii=0;ii<length;ii++)
        {
            int cc = input.get(start+ii);
            if (cc == '.')
            {
                decimal = true;
            }
            else
            {
                m*=10;
                m+=Character.digit(cc, 10);
                if (decimal)
                {
                    dec++;
                    if (dec == 3)
                    {
                        break;
                    }
                }
            }
        }
        if (dec != 3)
        {
            throw new IllegalArgumentException(input.getString(start, length));
        }
        return m;
    }

    void setData(long start, int length)
    {
        this.dataStart = start;
        this.dataLength = length;
    }

    void setTime(long start, int length)
    {
        this.timeStart = start;
        this.timeLength = length;
    }

}
