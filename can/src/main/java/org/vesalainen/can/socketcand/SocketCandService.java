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
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.AbstractMessageFactory;
import org.vesalainen.can.DefaultMessageFactory;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.nio.ByteBufferCharSequence;
import org.vesalainen.nio.ByteBufferInputStream;
import org.vesalainen.nio.PrintBuffer;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.parser.util.InputReader;
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
    private int dataRef;
    private String canBus;
    private final byte[] array = new byte[8];
    private final ThreadLocal<PrintBuffer> buffer = ThreadLocal.withInitial(()->new PrintBuffer(ByteBuffer.allocateDirect(64)));
    
    public SocketCandService(String canBus, ExecutorService executor, SignalCompiler compiler)
    {
        this(canBus, executor, new DefaultMessageFactory(compiler));
    }

    public SocketCandService(String canBus, ExecutorService executor, AbstractMessageFactory messageFactory)
    {
        super(executor, messageFactory);
        this.canBus = canBus;
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
            catch (ClosedByInterruptException ex)
            {
                log(SEVERE, "SocketCandService interrupted");
                return;
            }
            catch (Exception ex)
            {
                log(SEVERE, ex, "restarting SocketCandService", ex);
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

    @Override
    public void send(int canId, int length, byte[] data) throws IOException
    {
        PrintBuffer p = buffer.get();
        p.clear();
        p.format("< send %X %d ", canId, length);
        for (int ii=0;ii<length;ii++)
        {
            p.format("%X ", data[ii]);
        }
        p.print('>');
        ByteBuffer bb = p.getByteBuffer();
        bb.flip();
        channel.write(bb);
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

    public long getMillis(int timeRef)
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
    protected String getHexData()
    {
        return input.getString(dataRef);
    }

}
