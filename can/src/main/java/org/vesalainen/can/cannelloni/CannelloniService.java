/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.cannelloni;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.AbstractMessageFactory;
import org.vesalainen.can.DataUtil;
import org.vesalainen.can.DefaultMessageFactory;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.nio.PrintBuffer;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CannelloniService extends AbstractCanService
{
    private String address;
    private int local;
    private SocketAddress remote;
    private final int bufferSize;
    private final ThreadLocal<ByteBuffer> sendBuffer = ThreadLocal.withInitial(()->ByteBuffer.allocateDirect(32).order(ByteOrder.BIG_ENDIAN));
    private UnconnectedDatagramChannel channel;
    private AtomicInteger seq = new AtomicInteger();
    private byte nextSeq;
    private int packetCount;

    public CannelloniService(String address, int local, int remote, int bufferSize, CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        this(address, local, remote, bufferSize, executor, new DefaultMessageFactory(compiler));
    }

    public CannelloniService(String address, int local, int remote, int bufferSize, CachedScheduledThreadPool executor, AbstractMessageFactory messageFactory)
    {
        super(executor, messageFactory);
        this.address = address;
        this.local = local;
        this.remote = new InetSocketAddress(address, remote);
        this.bufferSize = bufferSize;
    }

    @Override
    public void sendRaw(int canId, int length, byte[] data) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void send(int canId, int length, long data) throws IOException
    {
        ByteBuffer bb = sendBuffer.get();
        bb.clear();
        bb.put((byte)2);
        bb.put((byte)0);
        bb.put((byte)seq.getAndIncrement());
        bb.putShort((byte)1);
        bb.putInt(canId|CAN_EFF_FLAG);
        bb.put((byte)length);
        for (int ii=0;ii<length;ii++)
        {
            bb.put((byte)DataUtil.get(data, ii));
        }
        bb.flip();
        channel.send(bb, remote);
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException ex)
        {
            log(Level.SEVERE, ex, "");
        }
    }

    @Override
    public void run()
    {
        try (UnconnectedDatagramChannel ch = UnconnectedDatagramChannel.open(address, local, bufferSize, true, false))
        {
            ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.BIG_ENDIAN);
            channel = ch;
            started();
            while (true)
            {
                try
                {
                    bb.clear();
                    int rc = channel.read(bb);
                    handlePacket(bb);
                }
                catch (Throwable ex)
                {
                    log(Level.SEVERE, ex, "");
                }
            }
            
        }
        catch (IOException ex)
        {
            log(Level.SEVERE, ex, "");
        }
    }

    private void handlePacket(ByteBuffer bb)
    {
        bb.flip();
        byte version = bb.get();
        if (version != 2)
        {
            throw new UnsupportedOperationException(version+" version not supported");
        }
        byte opCode = bb.get();
        if (opCode != 0)
        {
            throw new UnsupportedOperationException(opCode+" opCode not supported");
        }
        byte seqNo = bb.get();
        if (packetCount > 0)
        {
            if (nextSeq != seqNo)
            {
                warning("packet loss %x %x", nextSeq, seqNo);
            }
        }
        packetCount++;
        nextSeq = ++seqNo;
        short count = bb.getShort();
        for (int ii=0;ii<count;ii++)
        {
            handleFrame(bb);
        }
    }

    private void handleFrame(ByteBuffer bb)
    {
        int canId = bb.getInt();
        finest("ID %s", PGN.toString(canId));
        byte len = bb.get();
        long data = DataUtil.asLong(len, bb);
        frame(System.currentTimeMillis(), canId&CAN_EFF_MASK, len, data);
    }
    
}
