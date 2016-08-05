/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SNIServerName;
import org.vesalainen.nio.ByteBuffers;

/**
 *
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

    public SocketChannel getChannel()
    {
        return channel;
    }

    public ByteBuffer getClientHello()
    {
        return clientHello;
    }

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
