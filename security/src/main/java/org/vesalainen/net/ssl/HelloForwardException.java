/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.net.ssl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SNIServerName;
import org.vesalainen.nio.ByteBuffers;

/**
 *
 * @author tkv
 */
public class HelloForwardException extends RuntimeException
{
    private String host;
    private ByteBuffer clientHello;

    public HelloForwardException(SNIServerName snisn)
    {
        this.host = new String(snisn.getEncoded(), StandardCharsets.UTF_8);
    }
    
    public void addHello(ByteBuffer bb)
    {
        bb.flip();
        clientHello = ByteBuffer.allocate(bb.remaining());
        ByteBuffers.move(bb, clientHello);
        clientHello.flip();
    }

    public ByteBuffer getClientHello()
    {
        return clientHello;
    }

    public String getHost()
    {
        return host;
    }
    
}
