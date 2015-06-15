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

import java.io.IOException;
import java.net.InetSocketAddress;
import static java.net.StandardSocketOptions.SO_BROADCAST;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 * @author tkv
 */
public class UnconnectedDatagramChannel implements ReadableByteChannel, WritableByteChannel, AutoCloseable
{
    private final DatagramChannel channel;
    private final InetSocketAddress address;

    public UnconnectedDatagramChannel(DatagramChannel channel, InetSocketAddress address)
    {
        this.channel = channel;
        this.address = address;
    }
    
    public static UnconnectedDatagramChannel open(String host, int port) throws IOException
    {
        InetSocketAddress address = new InetSocketAddress(host, port);
        DatagramChannel channel = DatagramChannel.open();
        channel.setOption(SO_BROADCAST, true);
        channel.setOption(SO_REUSEADDR, true);
        channel.bind(new InetSocketAddress(port));
        return new UnconnectedDatagramChannel(channel, address);
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
    
}
