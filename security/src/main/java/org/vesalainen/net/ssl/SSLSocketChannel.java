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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

/**
 * SSLSocketChannel is similar to SSLSocket but is implemented as Channel.
 * 
 * <p>SSL parameters can be changed between open and read/write calls. Read/write
 * will start handshaking.
 * 
 * <p>SNI extension is added in outbound connections.
 * 
 * @author tkv
 */
public class SSLSocketChannel extends AbstractSSLSocketChannel
{

    protected SSLSocketChannel(SocketChannel channel, SSLEngine engine)
    {
        this(channel, engine, null, false);
    }
    protected SSLSocketChannel(SocketChannel channel, SSLEngine engine, ByteBuffer consumed, boolean keepOpen)
    {
        super(channel, engine, consumed, keepOpen);
    }
    /**
     * Creates connection to a named peer using default SSLContext. Connection
     * is in client mode but can be changed before read/write.
     * @param peer
     * @param port
     * @return
     * @throws IOException 
     */
    public static SSLSocketChannel open(String peer, int port) throws IOException
    {
        try
        {
            return open(peer, port, SSLContext.getDefault());
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new IOException(ex);
        }
    }
    /**
     * Creates connection to a named peer using given SSLContext. Connection
     * is in client mode but can be changed before read/write.
     * @param peer
     * @param port
     * @param sslContext
     * @return
     * @throws IOException 
     */
    public static SSLSocketChannel open(String peer, int port, SSLContext sslContext) throws IOException
    {
        SSLEngine engine = sslContext.createSSLEngine(peer, port);
        engine.setUseClientMode(true);
        SSLParameters sslParameters = engine.getSSLParameters();
        SNIServerName  hostName = new SNIHostName(peer);
        List<SNIServerName> list = new ArrayList<>();
        list.add(hostName);
        sslParameters.setServerNames(list);
        engine.setSSLParameters(sslParameters);
        InetSocketAddress address = new InetSocketAddress(peer, port);
        SocketChannel socketChannel = SocketChannel.open(address);
        SSLSocketChannel sslSocketChannel = new SSLSocketChannel(socketChannel, engine, null, false);
        return sslSocketChannel;
    }
    /**
     * Creates SSLSocketChannel over connected SocketChannel using default 
     * SSLContext. Connection is in server mode, but can be changed before read/write.
     * @param socketChannel
     * @param consumed If not null, ByteBuffer's remaining can contain bytes
     * that are consumed by SocketChannel, but are part of SSL connection. Bytes
     * are moved from this buffer. This buffers hasRemaining is false after call.
     * @param autoClose If true the SocketChannel is closed when SSLSocketChannel
     * is closed.
     * @return
     * @throws IOException 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static SSLSocketChannel open(SocketChannel socketChannel, ByteBuffer consumed, boolean autoClose) throws IOException, NoSuchAlgorithmException
    {
        return open(socketChannel, SSLContext.getDefault(), consumed, autoClose);
    }
    /**
     * Creates SSLSocketChannel over connected SocketChannel using given 
     * SSLContext. Connection is in server mode, but can be changed before read/write.
     * @param socketChannel
     * @param sslContext
     * @param consumed If not null, ByteBuffer's remaining can contain bytes
     * that are consumed by SocketChannel, but are part of SSL connection. Bytes
     * are moved from this buffer. This buffers hasRemaining is false after call.
     * @param autoClose If true the SocketChannel is closed when SSLSocketChannel
     * is closed.
     * @return
     * @throws IOException 
     */
    public static SSLSocketChannel open(SocketChannel socketChannel, SSLContext sslContext, ByteBuffer consumed, boolean autoClose) throws IOException
    {
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        SSLSocketChannel sslSocketChannel = new SSLSocketChannel(socketChannel, engine, consumed, !autoClose);
        return sslSocketChannel;
    }
}
