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
package org.vesalainen.can.can2udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.AbstractMessageFactory;
import org.vesalainen.can.DataUtil;
import org.vesalainen.can.DefaultMessageFactory;
import org.vesalainen.can.SignalCompiler;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.nio.ReadBuffer;
import org.vesalainen.nio.ReadByteBuffer;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Can2UdpService extends AbstractCanService
{
    private String address;
    private int local;
    private SocketAddress remote;
    private final int bufferSize;
    private final ThreadLocal<ByteBuffer> sendBuffer = ThreadLocal.withInitial(()->ByteBuffer.allocateDirect(256).order(ByteOrder.BIG_ENDIAN));
    private UnconnectedDatagramChannel channel;

    public Can2UdpService(String address, int local, int bufferSize, CachedScheduledThreadPool executor, SignalCompiler compiler)
    {
        this(address, local, bufferSize, executor, new DefaultMessageFactory(compiler));
    }

    public Can2UdpService(String address, int local, int bufferSize, CachedScheduledThreadPool executor, AbstractMessageFactory messageFactory)
    {
        super(executor, messageFactory);
        this.address = address;
        this.local = local;
        this.bufferSize = bufferSize;
        this.remote = new InetSocketAddress(address, local);
    }
    
    @Override
    public void send(int canId, int length, byte[] data) throws IOException
    {
        ByteBuffer bb = sendBuffer.get();
        bb.clear();
        if (length <= 8)
        {
            bb.put((byte)0);
        }
        else
        {
            bb.put((byte)0xf);
        }
        bb.putInt(canId|CAN_EFF_FLAG);
        bb.put((byte)length);
        for (int ii=0;ii<length;ii++)
        {
            bb.put(data, 0, length);
        }
        bb.flip();
        channel.send(bb, remote);
    }

    @Override
    public void run()
    {
        try (UnconnectedDatagramChannel ch = UnconnectedDatagramChannel.open(address, local, bufferSize, true, false))
        {
            ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.BIG_ENDIAN);
            ReadBuffer buffer = new ReadByteBuffer(bb);
            channel = ch;
            started();
            while (true)
            {
                try
                {
                    bb.clear();
                    int rc = channel.read(bb);
                    bb.flip();
                    handlePacket(bb, buffer);
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

    private void handlePacket(ByteBuffer bb, ReadBuffer rbb)
    {
        byte version = bb.get();
        if (version != 0 && version != 0xf)
        {
            throw new UnsupportedOperationException(version+" version not supported");
        }
        int canId = bb.getInt();
        finest("ID %s", PGN.toString(canId));
        int len = bb.get() & 0xff;
        if (len != bb.remaining())
        {
            throw new IllegalArgumentException();
        }
        frame(System.currentTimeMillis(), canId&CAN_EFF_MASK, rbb);
    }
    
}
