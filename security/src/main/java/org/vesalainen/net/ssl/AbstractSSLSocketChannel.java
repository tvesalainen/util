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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.vesalainen.nio.ByteBuffers;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author tkv
 */
public class AbstractSSLSocketChannel extends AbstractInterruptibleChannel implements ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel
{
    protected JavaLogging log = new JavaLogging(AbstractSSLSocketChannel.class);
    protected SocketChannel channel;
    protected SSLEngine engine;
    protected ByteBuffer netIn;
    protected ByteBuffer netOut;
    protected ByteBuffer appRead;
    protected ByteBuffer[] appReadArray;
    protected ByteBuffer appWrite;
    protected ByteBuffer[] appWriteArray;
    protected ByteBuffer nil;
    private final boolean keepOpen;
    
    protected AbstractSSLSocketChannel(SocketChannel channel, SSLEngine engine)
    {
        this(channel, engine, null, null, false);
    }
    protected AbstractSSLSocketChannel(SocketChannel channel, SSLEngine engine, ByteBuffer consumed, ByteBuffer clientHello, boolean keepOpen)
    {
        this.channel = channel; // connected
        this.engine = engine;   // initialiased
        this.keepOpen = keepOpen;
        int packetBufferSize = engine.getSession().getPacketBufferSize();
        if (consumed == null)
        {
            this.netIn = (ByteBuffer)ByteBuffer.allocateDirect(packetBufferSize).flip();
        }
        else
        {
            this.netIn = (ByteBuffer)ByteBuffer.allocateDirect(packetBufferSize);
            ByteBuffers.move(consumed, netIn);
            netIn.flip();
        }
        if (clientHello == null)
        {
            this.netOut = (ByteBuffer)ByteBuffer.allocateDirect(packetBufferSize).flip();
        }
        else
        {
            this.netOut = (ByteBuffer)ByteBuffer.allocateDirect(packetBufferSize);
            ByteBuffers.move(clientHello, netOut);
            netOut.flip();
        }
        this.nil = ByteBuffer.allocateDirect(packetBufferSize);
        int applicationBufferSize = engine.getSession().getApplicationBufferSize();
        this.appRead = (ByteBuffer)ByteBuffer.allocateDirect(applicationBufferSize).flip();
        this.appReadArray = new ByteBuffer[]{appRead};
        this.appWrite = (ByteBuffer)ByteBuffer.allocateDirect(applicationBufferSize).flip();
        this.appWriteArray = new ByteBuffer[]{appWrite};
    }

    public void closeOutbound() throws IOException
    {
        engine.closeOutbound();
        handshake();
        if (!keepOpen)
        {
            channel.shutdownOutput();
        }
    }
    
    @Override
    protected void implCloseChannel() throws IOException
    {
        engine.closeOutbound();
        handshake();
        if (!keepOpen)
        {
            channel.close();
        }
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
                    if (Status.BUFFER_UNDERFLOW.equals(us.getStatus()))
                    {
                        netIn.compact();
                        channel.read(netIn);
                        netIn.flip();
                    }
                    if (!checkStatus(us))
                    {
                        throw new IOException("unwrap:"+us);
                    }
                    break;
                case NEED_WRAP:
                    netOut.compact();
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
                        try
                        {
                            task.run();
                        }
                        catch (Exception hfex)
                        {
                            //hfex.addHello(netIn);
                            throw hfex;
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(engine.getHandshakeStatus()+ "unsupported");
            }
        }
    }
    private boolean checkStatus(SSLEngineResult status)
    {
        switch (status.getStatus())
        {
            case BUFFER_OVERFLOW:
                return false;
            default:
                return true;
        }
    }
    private long unwrap() throws IOException
    {
        SSLEngineResult result = null;
        while (result == null || result.bytesProduced() == 0)
        {
            result = engine.unwrap(netIn, appReadArray, 0, 1);
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
                        return -1;
                    }
                    netIn.flip();
                    result = engine.unwrap(netIn, appReadArray, 0, 1);
                    break;
                case BUFFER_OVERFLOW:
                    throw new IOException("unwrap:"+result);
                case CLOSED:
                    handshake();
                    return -1;
                case OK:
                    break;
            }
            handshake();
        }
        return result.bytesProduced();
    }

    private long wrap() throws IOException
    {
        long remaining = appWrite.remaining();
        while (appWrite.remaining() > 0)
        {
            netOut.compact();
            SSLEngineResult result = engine.wrap(appWriteArray, 0, 1, netOut);
            if (result.getStatus().equals(SSLEngineResult.Status.BUFFER_UNDERFLOW))
            {
                return 0;
            }
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
    
    private long fillAppRead() throws IOException
    {
        if (!appRead.hasRemaining())
        {
            appRead.compact();
            long rc = unwrap();
            if (rc <= 0)
            {
                return rc;
            }
            appRead.flip();
        }
        return 1;
    }
    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        long rc = fillAppRead();
        if (rc <= 0)
        {
            return rc;
        }
        return ByteBuffers.move(appRead, dsts, offset, length);
    }
    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        long rc = fillAppRead();
        if (rc <= 0)
        {
            return rc;
        }
        return ByteBuffers.move(appRead, dsts);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        long rc = fillAppRead();
        if (rc <= 0)
        {
            return (int) rc;
        }
        return (int) ByteBuffers.move(appRead, dst);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        appWrite.compact();
        long len = ByteBuffers.move(srcs, offset, length, appWrite);
        appWrite.flip();
        wrap();
        return len;
    }
    @Override
    public long write(ByteBuffer[] srcs) throws IOException
    {
        appWrite.compact();
        long len = ByteBuffers.move(srcs, appWrite);
        appWrite.flip();
        wrap();
        return len;
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        appWrite.compact();
        long len = ByteBuffers.move(src, appWrite);
        appWrite.flip();
        wrap();
        return (int) len;
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
