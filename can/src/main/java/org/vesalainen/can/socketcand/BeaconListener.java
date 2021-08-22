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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.nio.ByteBufferInputStream;
import org.vesalainen.nio.channels.UnconnectedDatagramChannel;
import org.vesalainen.util.HexDump;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.logging.JavaLogging;
import org.vesalainen.xml.SimpleXMLParser;
import org.vesalainen.xml.SimpleXMLParser.Element;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BeaconListener extends JavaLogging implements Runnable, AutoCloseable
{
    protected final CachedScheduledThreadPool executor;
    private Future<?> future;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    private Map<InetSocketAddress,SocketCandInfo> infoMap = new HashMap<>();

    BeaconListener()
    {
        this(new CachedScheduledThreadPool());
    }
    public BeaconListener(CachedScheduledThreadPool executor)
    {
        super(BeaconListener.class);
        this.executor = executor;
    }

    public void start()
    {
        if (future != null)
        {
            throw new IllegalStateException("started already");
        }
        future = executor.submit(this);
    }
    public void stop()
    {
        if (future == null)
        {
            throw new IllegalStateException("not started");
        }
        future.cancel(true);
        future = null;
    }
    public void startAndWait() throws InterruptedException, ExecutionException
    {
        start();
        future.get();
    }
    
    @Override
    public void run()
    {
        try
        {
            UnconnectedDatagramChannel channel = UnconnectedDatagramChannel.open("255.255.255.255", 42000, 1024, true, false);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] digest = null;
            while (true)
            {
                buffer.clear();
                channel.read(buffer);
                buffer.flip();
                messageDigest.update(buffer);
                byte[] dig = messageDigest.digest();
                if (!Arrays.equals(digest, dig))
                {
                    digest = dig;
                    buffer.flip();
                    parse(buffer);
                }
            }
        }
        catch (IOException | NoSuchAlgorithmException ex)
        {
            log(SEVERE, ex, "%s", ex.getMessage());
        }
    }

    @Override
    public void close() throws Exception
    {
        future.cancel(true);
    }

    private void parse(ByteBuffer buffer) throws IOException
    {
        info(HexDump.remainingToHex(buffer));
        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);
        SimpleXMLParser parser = new SimpleXMLParser(bbis);
        SocketCandInfo info = new SocketCandInfo(parser.getRoot());
        infoMap.put(info.getAddress(), info);
        
    }
    public class SocketCandInfo
    {
        private InetSocketAddress address;
        private final Set<String> busses = new HashSet<>();

        public SocketCandInfo(Element root)
        {
            root.forEachChild((e)->
            {
                switch (e.getTag())
                {
                    case "URL":
                        {
                            try
                            {
                                URI uri = new URI(e.getText());
                                address = InetSocketAddress.createUnresolved(uri.getHost(), uri.getPort());
                            }
                            catch (URISyntaxException ex)
                            {
                                log(SEVERE, ex, "%s", ex.getMessage());
                            }
                        }
                        break;
                    case "Bus":
                        busses.add(e.getAttributeValue("name"));
                        break;

                }
            });
        }

        public InetSocketAddress getAddress()
        {
            return address;
        }

        public Set<String> getBusses()
        {
            return busses;
        }
        
    }
}
