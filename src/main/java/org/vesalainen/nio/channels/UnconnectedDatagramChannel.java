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
package org.vesalainen.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import static java.net.StandardSocketOptions.IP_MULTICAST_LOOP;
import static java.net.StandardSocketOptions.SO_BROADCAST;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.util.Arrays;

/**
 * A DatagramChannel that binds to ports and sends to host/port.
 * @author tkv
 */
public class UnconnectedDatagramChannel implements ByteChannel, GatheringByteChannel, ScatteringByteChannel, AutoCloseable, Closeable
{
    private static final byte[] IPv4BroadcastAddress = new byte[] {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};

    private final DatagramChannel channel;
    private final InetSocketAddress address;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;

    public UnconnectedDatagramChannel(DatagramChannel channel, InetSocketAddress address, int maxDatagramSize, boolean direct)
    {
        this.channel = channel;
        this.address = address;
        if (direct)
        {
            this.readBuffer = ByteBuffer.allocateDirect(maxDatagramSize);
            this.writeBuffer = ByteBuffer.allocateDirect(maxDatagramSize);
        }
        else
        {
            this.readBuffer = ByteBuffer.allocate(maxDatagramSize);
            this.writeBuffer = ByteBuffer.allocate(maxDatagramSize);
        }
    }

    
    public static UnconnectedDatagramChannel open(String host, int port, int maxDatagramSize, boolean direct, boolean loop) throws IOException
    {
        InetAddress ia = InetAddress.getByName(host);
        InetSocketAddress address = new InetSocketAddress(ia, port);
        DatagramChannel channel = DatagramChannel.open();
        if (isBroadcast(ia))
        {
            channel.setOption(SO_BROADCAST, true);
        }
        channel.setOption(SO_REUSEADDR, true);
        channel.bind(new InetSocketAddress(port));
        if (ia.isMulticastAddress())
        {
            channel.setOption(IP_MULTICAST_LOOP, loop);
        }
        return new UnconnectedDatagramChannel(channel, address, maxDatagramSize, direct);
    }
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        int rem = dst.remaining();
        channel.receive(dst);
        return rem-dst.remaining();
    }

    @Override
    public boolean isOpen()
    {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException
    {
        channel.close();
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        int rem = src.remaining();
        channel.send(src, address);
        return src.remaining()-rem;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        if (length == 1)
        {
            return write(srcs[offset]);
        }
        else
        {
            writeBuffer.clear();
            for  (int ii=0;ii<length;ii++)
            {
                ByteBuffer bb = srcs[ii+offset];
                writeBuffer.put(bb);
            }
            return write(writeBuffer);
        }
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException
    {
        return write(srcs, 0, srcs.length);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        if (length == 1 || dsts[offset].remaining() > readBuffer.capacity())
        {
            return read(dsts[offset]);
        }
        else
        {
            readBuffer.clear();
            int res = read(readBuffer);
            readBuffer.flip();
            for  (int ii=0;ii<length && readBuffer.hasRemaining();ii++)
            {
                ByteBuffer bb = dsts[ii+offset];
                if (bb.remaining() >= readBuffer.remaining())
                {
                    bb.put(readBuffer);
                }
                else
                {
                    int lim = readBuffer.limit();
                    readBuffer.limit(readBuffer.position()+bb.remaining());
                    bb.put(readBuffer);
                    readBuffer.limit(lim);
                }
            }
            return res;
        }
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        return read(dsts, 0, dsts.length);
    }

    private static boolean isBroadcast(InetAddress addr)
    {
        return Arrays.equals(IPv4BroadcastAddress, addr.getAddress());
    }
}
