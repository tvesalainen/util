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
package org.vesalainen.net.ssl;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 *
 * @author tkv
 */
public class SSLServerSocketChannel extends AbstractSelectableChannel implements NetworkChannel
{
    private ServerSocketChannel channel;
    private SSLContext sslContext;
    
    protected SSLServerSocketChannel(ServerSocketChannel channel, SSLContext sslContext)
    {
        super(channel.provider());
        this.channel = channel;
        this.sslContext = sslContext;
    }

    public SSLSocketChannel accept() throws IOException
    {
        SocketChannel sc = channel.accept();
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        return new SSLSocketChannel(sc, engine);
    }
    public static SSLServerSocketChannel open(SocketAddress address) throws IOException
    {
        try
        {
            return open(address, SSLContext.getDefault());
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IOException(ex);
        }
    }
    public static SSLServerSocketChannel open(SocketAddress address, SSLContext sslContext) throws IOException
    {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(address);
        SSLServerSocketChannel sslSocketChannel = new SSLServerSocketChannel(serverSocketChannel, sslContext);
        return sslSocketChannel;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException
    {
        channel.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException
    {
        channel.configureBlocking(block);
    }

    @Override
    public int validOps()
    {
        return channel.validOps();
    }

    @Override
    public NetworkChannel bind(SocketAddress local) throws IOException
    {
        return channel.bind(local);
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException
    {
        return channel.getLocalAddress();
    }

    @Override
    public <T> NetworkChannel setOption(SocketOption<T> name, T value) throws IOException
    {
        return channel.setOption(name, value);
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException
    {
        return channel.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions()
    {
        return channel.supportedOptions();
    }
}
