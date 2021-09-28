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
import java.util.logging.Level;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.AbstractMessageFactory;
import org.vesalainen.can.DefaultMessageFactory;
import org.vesalainen.can.Frame;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.nio.ByteBufferCharSequence;
import org.vesalainen.nio.ByteBufferInputStream;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.parser.util.InputReader;
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
    private InputReader input;
    private ThreadLocal<FrameImpl> localFrame = ThreadLocal.withInitial(FrameImpl::new);
    
    public SocketCandService(String canBus, CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        this(canBus, executor, new DefaultMessageFactory(compiler));
    }

    public SocketCandService(String canBus, CachedScheduledThreadPool executor, AbstractMessageFactory messageFactory)
    {
        super(executor, messageFactory);
        this.openBus = new ByteBufferCharSequence("< open "+canBus+" >");
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
            catch (Exception ex)
            {
                log(Level.SEVERE, "restarting SocketCandService", ex);
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

    void frame(int rawId, int timeRef, int dataRef)
    {
        executor.execute(()->frame2(rawId, timeRef, dataRef));
    }
    void frame2(int rawId, int timeRef, int dataRef)
    {
        if ((rawId & 0b1100000000000000000000000000000) != 0)
        {
            warning("\nEFF/SFF is set in the MSB %s", input.getString(dataRef));
        }
        else
        {
            int canId;
            if ((rawId & 0b10000000000000000000000000000000) != 0)
            {
                canId = rawId & 0b11111111111111111111111111111;
            }
            else
            {
                canId = rawId;
            }
            FrameImpl frame = localFrame.get();
            frame.init(canId, timeRef, dataRef);
            rawFrame(frame);
        }
    }

    private class FrameImpl implements Frame
    {
        private int canId;
        private int timeRef;
        private int dataLength;
        private int dataStart;

        private void init(int canId, int timeRef, int dataRef)
        {
            this.canId = canId;
            this.timeRef = timeRef;
            this.dataLength = input.getLength(dataRef);
            this.dataStart = input.getStart(dataRef);
        }
        
        @Override
        public long getMillis()
        {
            int start = input.getStart(timeRef);
            int length = input.getLength(timeRef);
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

        @Override
        public byte getData(int index)
        {
            if (index > dataLength/2 || index < 0)
            {
                throw new IndexOutOfBoundsException(index+" out of bounds");
            }
            return (byte) (Character.digit(input.get(dataStart+2*index), 16)<<4|Character.digit(input.get(dataStart+2*index+1), 16));
        }

        @Override
        public void getData(byte[] buf, int sourceOffset, int bufOffset, int length)
        {
            if (sourceOffset+length > dataLength/2 || sourceOffset < 0 || bufOffset < 0)
            {
                throw new IndexOutOfBoundsException("out of bounds");
            }
            for (int ii=0;ii<length;ii++)
            {
                int index = sourceOffset+ii;
                buf[bufOffset+ii] = (byte) (Character.digit(input.get(dataStart+2*index), 16)<<4|Character.digit(input.get(dataStart+2*index+1), 16));
            }
        }

        @Override
        public int getDataLength()
        {
            return dataLength/2;
        }

        @Override
        public int getCanId()
        {
            return canId;
        }
        
    }
}
