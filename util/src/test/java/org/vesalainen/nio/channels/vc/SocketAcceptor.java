/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nio.channels.vc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

/**
 *
 * @author tkv
 */
class SocketAcceptor implements Callable<SocketChannel>
{
    
    private final ServerSocketChannel ssc;

    public SocketAcceptor() throws IOException
    {
        ssc = ServerSocketChannel.open();
        ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        ssc.bind(null);
    }

    public int getPort() throws IOException
    {
        InetSocketAddress local = (InetSocketAddress) ssc.getLocalAddress();
        return local.getPort();
    }

    @Override
    public SocketChannel call() throws Exception
    {
        SocketChannel sc = ssc.accept();
        ssc.close();
        return sc;
    }
    
}
