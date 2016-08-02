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
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import jdk.net.SocketFlow;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class AbstractSSLSocketChannel extends AbstractSelectableChannel implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel
{
    protected JavaLogging log = new JavaLogging(AbstractSSLSocketChannel.class);
    protected SocketChannel channel;
    protected SSLEngine engine;
    protected ByteBuffer netIn;
    protected ByteBuffer netOut;
    protected ByteBuffer nil;
    
    protected AbstractSSLSocketChannel(SocketChannel channel, SSLEngine engine)
    {
        super(channel.provider());
        this.channel = channel; // connected
        this.engine = engine;   // initaliased
        int packetBufferSize = engine.getSession().getPacketBufferSize();
        this.netIn = ByteBuffer.allocateDirect(packetBufferSize);
        netIn.flip();
        this.netOut = ByteBuffer.allocateDirect(packetBufferSize);
        this.nil = ByteBuffer.allocateDirect(packetBufferSize);
    }

    public void closeOutbound() throws IOException
    {
        engine.closeOutbound();
        handshake();
    }
    
    @Override
    protected void implCloseSelectableChannel() throws IOException
    {
        engine.closeOutbound();
        handshake();
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

    protected void handshake() throws IOException
    {
        while (true)
        {
            log.finest("HandshakeStatus=%s", engine.getHandshakeStatus());
            switch (engine.getHandshakeStatus())
            {
                case FINISHED:
                case NOT_HANDSHAKING:
                    return;
                case NEED_UNWRAP:
                    if (!netIn.hasRemaining())
                    {
                        netIn.clear();
                        channel.read(netIn);
                        netIn.flip();
                    }
                    nil.clear();
                    SSLEngineResult us = engine.unwrap(netIn, nil);
                    if (!checkStatus(us))
                    {
                        throw new IOException("unwrap:"+us);
                    }
                    break;
                case NEED_WRAP:
                    netOut.clear();
                    nil.clear();
                    nil.flip();
                    SSLEngineResult ws = engine.wrap(nil, netOut);
                    if (!checkStatus(ws))
                    {
                        throw new IOException("wrap:"+ws);
                    }
                    netOut.flip();
                    while (netOut.hasRemaining())
                    {
                        channel.write(netOut);
                    }
                    break;
                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null)
                    {
                        task.run();
                    }
                    break;
            }
        }
    }
    private boolean checkStatus(SSLEngineResult status)
    {
        switch (status.getStatus())
        {
            case BUFFER_UNDERFLOW:
            case BUFFER_OVERFLOW:
                return false;
            default:
                return true;
        }
    }
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        long remaining = remaining(dsts, offset, length);
        while (remaining(dsts, offset, length) == remaining)
        {
            SSLEngineResult result = engine.unwrap(netIn, dsts, offset, length);
            log.fine("unwrap %s", result);
            switch (result.getStatus())
            {
                case BUFFER_UNDERFLOW:
                    netIn.compact();
                    int rc = channel.read(netIn);
                    if (rc == -1)
                    {
                        engine.closeInbound();
                        handshake();
                        return result.bytesProduced();
                    }
                    netIn.flip();
                    result = engine.unwrap(netIn, dsts, offset, length);
                    break;
                case BUFFER_OVERFLOW:
                    throw new IOException(result.toString());
                case CLOSED:
                    handshake();
                    return -1;
                case OK:
                    break;
            }
            handshake();
        }
        return remaining - remaining(dsts, offset, length);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        long remaining = remaining(srcs, offset, length);
        while (remaining(srcs, offset, length) > 0)
        {
            netOut.clear();
            SSLEngineResult result = engine.wrap(srcs, offset, length, netOut);
            log.fine("wrap %s", result);
            if (!result.getStatus().equals(SSLEngineResult.Status.OK))
            {
                throw new IOException(result.toString());
            }
            netOut.flip();
            while (netOut.remaining() > 0)
            {
                channel.write(netOut);
            }
            handshake();
        }
        return remaining;
    }

    private long remaining(ByteBuffer[] srcs, int offset, int length)
    {
        long remaining = 0;
        for (int ii=0;ii<length;ii++)
        {
            remaining += srcs[ii + offset].remaining();
        }
        return remaining;
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
    
    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        return read(dsts, 0, dsts.length);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        return (int) read(new ByteBuffer[]{dst}, 0, 1);
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException
    {
        return write(srcs, 0, srcs.length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        return (int) write(new ByteBuffer[]{src}, 0, 1);
    }

    public String getPeerHost()
    {
        return engine.getPeerHost();
    }

    public int getPeerPort()
    {
        return engine.getPeerPort();
    }

    public String[] getSupportedCipherSuites()
    {
        return engine.getSupportedCipherSuites();
    }

    public String[] getEnabledCipherSuites()
    {
        return engine.getEnabledCipherSuites();
    }

    public void setEnabledCipherSuites(String[] strings)
    {
        engine.setEnabledCipherSuites(strings);
    }

    public String[] getSupportedProtocols()
    {
        return engine.getSupportedProtocols();
    }

    public String[] getEnabledProtocols()
    {
        return engine.getEnabledProtocols();
    }

    public void setEnabledProtocols(String[] strings)
    {
        engine.setEnabledProtocols(strings);
    }

    public SSLSession getSession()
    {
        return engine.getSession();
    }

    public boolean getUseClientMode()
    {
        return engine.getUseClientMode();
    }

    public void setNeedClientAuth(boolean bln)
    {
        engine.setNeedClientAuth(bln);
    }

    public boolean getNeedClientAuth()
    {
        return engine.getNeedClientAuth();
    }

    public boolean getWantClientAuth()
    {
        return engine.getWantClientAuth();
    }

    public void setEnableSessionCreation(boolean bln)
    {
        engine.setEnableSessionCreation(bln);
    }

    public SSLParameters getSSLParameters()
    {
        return engine.getSSLParameters();
    }

    public void setSSLParameters(SSLParameters sslp)
    {
        engine.setSSLParameters(sslp);
    }

}
