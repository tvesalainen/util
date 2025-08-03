/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class InetClient implements AutoCloseable
{
    
    protected NetworkChannel sc;

    protected InetClient(NetworkChannel sc)
    {
        this.sc = sc;
    }

    public static <T extends NetworkChannel & ReadableByteChannel & WritableByteChannel & ScatteringByteChannel & GatheringByteChannel> T openChannel(String server, int port) throws IOException
    {
        return openChannel(server, port, 1024, true, false);
    }
    /**
     * Opens either SocketChannel or UnconnectedDatagramChannel depending on
     * server.
     * @param <T>
     * @param server
     * @param port
     * @param maxDatagramSize
     * @param direct
     * @param loop
     * @return
     * @throws IOException 
     */
    public static <T extends NetworkChannel & ByteChannel & ScatteringByteChannel & GatheringByteChannel> T openChannel(String server, int port, int maxDatagramSize, boolean direct, boolean loop) throws IOException
    {
        InetSocketAddress address = new InetSocketAddress(server, port);
        if (address.getAddress().isMulticastAddress())
        {
            return (T) UnconnectedDatagramChannel.open(address, maxDatagramSize, direct, loop);
        }
        else
        {
            return (T) SocketChannel.open(address);
        }
    }
    @Override
    public void close() throws IOException
    {
        sc.close();
    }
    
}
