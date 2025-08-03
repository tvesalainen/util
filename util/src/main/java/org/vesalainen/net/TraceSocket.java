/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.nio.channels.SocketChannel;
import org.vesalainen.io.TraceInputStream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TraceSocket extends Socket
{
    private Socket socket;

    public TraceSocket(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException
    {
        socket.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException
    {
        socket.connect(endpoint, timeout);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException
    {
        socket.bind(bindpoint);
    }

    @Override
    public InetAddress getInetAddress()
    {
        return socket.getInetAddress();
    }

    @Override
    public InetAddress getLocalAddress()
    {
        return socket.getLocalAddress();
    }

    @Override
    public int getPort()
    {
        return socket.getPort();
    }

    @Override
    public int getLocalPort()
    {
        return socket.getLocalPort();
    }

    @Override
    public SocketAddress getRemoteSocketAddress()
    {
        return socket.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress()
    {
        return socket.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel()
    {
        return socket.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        return new TraceInputStream(socket.getInputStream());
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return socket.getOutputStream();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException
    {
        socket.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException
    {
        return socket.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException
    {
        socket.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException
    {
        return socket.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException
    {
        socket.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException
    {
        socket.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException
    {
        return socket.getOOBInline();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException
    {
        socket.setSoTimeout(timeout);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException
    {
        return socket.getSoTimeout();
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException
    {
        socket.setSendBufferSize(size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException
    {
        return socket.getSendBufferSize();
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException
    {
        socket.setReceiveBufferSize(size);
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException
    {
        return socket.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException
    {
        socket.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException
    {
        return socket.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException
    {
        socket.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException
    {
        return socket.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException
    {
        socket.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException
    {
        return socket.getReuseAddress();
    }

    @Override
    public synchronized void close() throws IOException
    {
        socket.close();
    }

    @Override
    public void shutdownInput() throws IOException
    {
        socket.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException
    {
        socket.shutdownOutput();
    }

    @Override
    public String toString()
    {
        return socket.toString();
    }

    @Override
    public boolean isConnected()
    {
        return socket.isConnected();
    }

    @Override
    public boolean isBound()
    {
        return socket.isBound();
    }

    @Override
    public boolean isClosed()
    {
        return socket.isClosed();
    }

    @Override
    public boolean isInputShutdown()
    {
        return socket.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown()
    {
        return socket.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
    {
        socket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }
    
}
