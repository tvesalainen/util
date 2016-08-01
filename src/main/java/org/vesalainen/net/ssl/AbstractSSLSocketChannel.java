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
import java.util.function.Supplier;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
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
    private boolean handshaking;
    
    protected AbstractSSLSocketChannel(SocketChannel channel, SSLEngine engine)
    {
        super(channel.provider());
        this.channel = channel; // connected
        this.engine = engine;   // initaliased
        int packetBufferSize = engine.getSession().getPacketBufferSize();
        this.netIn = ByteBuffer.allocateDirect(packetBufferSize);
        netIn.flip();
        this.netOut = ByteBuffer.allocateDirect(packetBufferSize);
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException
    {
        ByteBuffer nil = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
        engine.closeOutbound();
        netOut.clear();
        SSLEngineResult result = engine.wrap(nil, netOut);
        netOut.flip();
        while (netOut.remaining() > 0)
        {
            channel.write(netOut);
        }
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

    protected void startHandshake() throws IOException
    {
        handshaking = true;
        ByteBuffer nil = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
        engine.beginHandshake();
        boolean finishedHandshaking = false;
        while (!finishedHandshaking)
        {
            log.finest("HandshakeStatus=%s", engine.getHandshakeStatus());
            switch (engine.getHandshakeStatus())
            {
                case FINISHED:
                case NOT_HANDSHAKING:
                    finishedHandshaking = true;
                    break;
                case NEED_UNWRAP:
                    nil.clear();
                    read(nil);
                    break;
                case NEED_WRAP:
                    nil.flip();
                    write(nil);
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
    protected long unwrap(Supplier<SSLEngineResult> unwrapper) throws IOException
    {
        if (!handshaking)
        {
            startHandshake();
        }
        if (netIn.remaining() == 0)
        {
            netIn.clear();
            int rc = channel.read(netIn);
            if (rc == -1)
            {
                throw new IOException("eof");
            }
            netIn.flip();
        }
        SSLEngineResult result = unwrapper.get();
        while (true)
        {
            switch (result.getStatus())
            {
                case BUFFER_UNDERFLOW:
                    netIn.compact();
                    int rc = channel.read(netIn);
                    if (rc == -1)
                    {
                        throw new IOException("eof");
                    }
                    netIn.flip();
                    result = unwrapper.get();
                    break;
                case BUFFER_OVERFLOW:
                case CLOSED:
                    throw new IOException(result.toString());
                case OK:
                    return result.bytesProduced();
            }
        }
    }

    protected long wrap(Supplier<SSLEngineResult> wrapper) throws IOException
    {
        if (!handshaking)
        {
            startHandshake();
        }
        SSLEngineResult result;
        netOut.clear();
        result = wrapper.get();
        if (!result.getStatus().equals(SSLEngineResult.Status.OK))
        {
            throw new IOException(result.toString());
        }
        netOut.flip();
        while (netOut.remaining() > 0)
        {
            channel.write(netOut);
        }
        return result.bytesConsumed();
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
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException
    {
        return unwrap(()->
        {
            try
            {
                return engine.unwrap(netIn, dsts, offset, length);
            }
            catch (SSLException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException
    {
        return unwrap(()->
        {
            try
            {
                return engine.unwrap(netIn, dsts);
            }
            catch (SSLException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public int read(ByteBuffer dst) throws IOException
    {
        return (int) unwrap(()->
        {
            try
            {
                return engine.unwrap(netIn, dst);
            }
            catch (SSLException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException
    {
        return wrap(()->
        {
            try
            {
                return engine.wrap(srcs, offset, length, netOut);
            }
            catch (SSLException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException
    {
        return wrap(()->
        {
            try
            {
                return engine.wrap(srcs, netOut);
            }
            catch (SSLException ex)
            {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public int write(ByteBuffer src) throws IOException
    {
        return (int) wrap(()->
        {
            try
            {
                return engine.wrap(src, netOut);
            }
            catch (SSLException ex)
            {
                throw new RuntimeException(ex);
            }
        });
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
