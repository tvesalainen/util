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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import static java.net.StandardSocketOptions.IP_MULTICAST_LOOP;
import static java.net.StandardSocketOptions.SO_BROADCAST;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import org.vesalainen.util.logging.JavaLogging;

/**
 * A DatagramChannel that binds to ports and sends to host/port.
 * @author tkv
 */
public class UnconnectedDatagramChannel extends SelectableChannel implements ByteChannel, GatheringByteChannel, ScatteringByteChannel, AutoCloseable, Closeable
{
    private static final byte[] IPv4BroadcastAddress = new byte[] {(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff};

    private final DatagramChannel channel;
    private final InetSocketAddress address;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private final JavaLogging log;
    private final boolean loop;
    private List<InetAddress> locals;
    private InetSocketAddress from;
    
    public UnconnectedDatagramChannel(DatagramChannel channel, InetSocketAddress address, int maxDatagramSize, boolean direct, boolean loop) throws SocketException
    {
        log = new JavaLogging(this.getClass());
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
        this.loop = loop;
        if (!loop)
        {
            locals = new ArrayList<>();
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements())
            {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements())
                {
                    locals.add(ias.nextElement());
                }
            }
        }
    }

    
    public static UnconnectedDatagramChannel open(String host, int port, int maxDatagramSize, boolean direct, boolean loop) throws IOException
    {
        ProtocolFamily family = StandardProtocolFamily.INET;
        InetAddress ia = InetAddress.getByName(host);
        if (ia instanceof Inet6Address)
        {
            family = StandardProtocolFamily.INET6;
        }
        InetSocketAddress address = new InetSocketAddress(ia, port);
        DatagramChannel channel = DatagramChannel.open(family);
        if (isBroadcast(ia))
        {
            channel.setOption(SO_BROADCAST, true);
        }
        channel.setOption(SO_REUSEADDR, true);
        channel.bind(new InetSocketAddress(port));
        if (ia.isMulticastAddress())
        {
            channel.setOption(IP_MULTICAST_LOOP, loop);
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements())
            {
                NetworkInterface ni = nis.nextElement();
                if (ni.supportsMulticast() && ni.isUp())
                {
                    channel.join(ia, ni);
                }
            }
        }
        return new UnconnectedDatagramChannel(channel, address, maxDatagramSize, direct, loop);
    }
    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        int rem = dst.remaining();
        int p1 = dst.position();
        from = (InetSocketAddress) channel.receive(dst);
        if (from == null)
        {
            return 0;
        }
        if (!loop && locals.contains(from.getAddress()))
        {
            // reject local send if OS doesn't support IP_MULTICAST_LOOP option
            dst.position(p1);
            log.warning("local send %s while IP_MULTICAST_LOOP false", from);
            return 0;
        }
        int p2 = dst.position();
        log(Level.FINEST, "receive", dst, p1, p2-p1);
        return rem-dst.remaining();
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        int rem = src.remaining();
        int p1 = src.position();
        channel.send(src, address);
        int p2 = src.position();
        log(Level.FINEST, "send", src, p1, p2-p1);
        return rem-src.remaining();
    }
    /**
     * Returns the last sender address.
     * @return 
     */
    public InetSocketAddress getFromAddress()
    {
        return from;
    }

    private void log(Level level, String msg, ByteBuffer bb, int offset, int length)
    {
        if (log.isLoggable(level))
        {
            StringBuilder sb = new StringBuilder();
            for (int ii=0;ii<length;ii++)
            {
                char cc = (char)(bb.get(ii+offset) & 0xff);
                if (cc >= ' ' && cc <= 'z')
                {
                    sb.append(cc);
                }
                else
                {
                    sb.append('<').append(Integer.toHexString(cc)).append('>');
                }
            }
            log.log(level, "%s='%s'", msg, sb.toString());
        }
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
            writeBuffer.flip();
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

    @Override
    public SelectorProvider provider()
    {
        return channel.provider();
    }

    @Override
    public int validOps()
    {
        return channel.validOps();
    }

    @Override
    public boolean isRegistered()
    {
        return channel.isRegistered();
    }

    @Override
    public SelectionKey keyFor(Selector sel)
    {
        return channel.keyFor(sel);
    }

    @Override
    public SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException
    {
        return channel.register(sel, ops, att);
    }

    @Override
    public SelectableChannel configureBlocking(boolean block) throws IOException
    {
        return channel.configureBlocking(block);
    }

    @Override
    public boolean isBlocking()
    {
        return channel.isBlocking();
    }

    @Override
    public Object blockingLock()
    {
        return channel.blockingLock();
    }

    @Override
    protected void implCloseChannel() throws IOException
    {
        channel.close();
    }
}
