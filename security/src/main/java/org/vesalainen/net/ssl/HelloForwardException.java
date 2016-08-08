/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.vesalainen.nio.ByteBuffers;

/**
 * HelloForwardException is thrown from SSLSocketChannel read/write method
 * when client hello SNI extension triggers host filter. This is a means to
 * interrupt handshaking and e.g. redirect the connection. SocketChannel, 
 * hostname and client hello contents are available.
 * @author tkv
 */
public class HelloForwardException extends IOException
{
    private SocketChannel channel;
    private String host;
    private ByteBuffer clientHello;

    HelloForwardException(SocketChannel channel, String host, ByteBuffer bb)
    {
        super(host);
        this.channel = channel;
        this.host = host;
        bb.flip();
        clientHello = ByteBuffer.allocate(bb.remaining());
        ByteBuffers.move(bb, clientHello);
        clientHello.flip();
    }
    /**
     * Returns the SocketChannel that sent client hello.
     * @return 
     */
    public SocketChannel getChannel()
    {
        return channel;
    }
    /**
     * Returns the content of client hello message.
     * @return 
     */
    public ByteBuffer getClientHello()
    {
        return clientHello;
    }
    /**
     * Returns the hostname that triggered the hostfilter.
     * @return 
     */
    public String getHost()
    {
        return host;
    }

    @Override
    public String toString()
    {
        return "HelloForwardException{" + "channel=" + channel + ", host=" + host + ", clientHello=" + clientHello + '}';
    }
    
}
