/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.socketcand;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.can.AbstractCanService;
import org.vesalainen.can.MessageFactory;
import org.vesalainen.nio.ByteBufferInputStream;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.xml.SimpleXMLParser;
import org.vesalainen.xml.SimpleXMLParser.Element;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SocketCandService extends AbstractCanService
{
    
    public SocketCandService(CachedScheduledThreadPool executor, MessageFactory messageFactory)
    {
        super(executor, messageFactory);
    }

    @Override
    public ByteBuffer getFrame()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getLength()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void readData(byte[] data, int offset)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                SocketCandInfo info = waitForBeacon();
                SocketChannel channel = SocketChannel.open(info.getAddress());
                readSocketCand(channel);
            }
            catch (IOException ex)
            {
                log(Level.SEVERE, "exit SocketCandService", ex);
                return;
            }
        }
    }
    
    private SocketCandInfo waitForBeacon() throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        UnconnectedDatagramChannel channel = UnconnectedDatagramChannel.open("255.255.255.255", 42000, 1024, true, false);
        buffer.clear();
        channel.read(buffer);
        buffer.flip();
        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);
        SimpleXMLParser parser = new SimpleXMLParser(bbis);
        return new SocketCandInfo(parser.getRoot());
    }

    private void readSocketCand(SocketChannel channel)
    {
        SocketCandParser parser = SocketCandParser.getInstance();
        parser.parse(channel, this);
    }

    void openBus()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void rawMode()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
