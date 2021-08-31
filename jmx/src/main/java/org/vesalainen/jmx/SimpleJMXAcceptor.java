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
package org.vesalainen.jmx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleJMXAcceptor extends JavaLogging implements Runnable
{
    private final int port;
    private Thread thread;

    public SimpleJMXAcceptor(int port)
    {
        super(SimpleJMXAcceptor.class);
        this.port = port;
    }

    public void start()
    {
        if (thread != null)
        {
            throw new IllegalStateException("already started");
        }
        thread = new Thread(this, "Simple JMX Acceptor");
        thread.start();
    }
    public void stop()
    {
        if (thread == null)
        {
            throw new IllegalStateException("not started");
        }
        thread.interrupt();
        thread = null;
    }
    @Override
    public void run()
    {
        try
        {
            SelectorProvider provider = SelectorProvider.provider();
            while (true)
            {
                try (ServerSocketChannel channel = ServerSocketChannel.open();
                    AbstractSelector selector = provider.openSelector();)
                {
                    info("opened socket %s", channel);
                    channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                    channel.bind(new InetSocketAddress(port));
                    channel.configureBlocking(false);
                    SelectionKey selectionKey = channel.register(selector, OP_ACCEPT);
                    info("start select %s", channel);
                    int select = selector.select();
                    info("selected %d", select);
                    if (select == 0)
                    {
                        return;
                    }
                    selectionKey.cancel();
                    channel.configureBlocking(true);
                    Server server = new Server();
                    ServerConnector connector = new ServerConnector(server);
                    connector.open(channel);
                    server.addConnector(connector);
                    ServletContextHandler context = new ServletContextHandler();
                    ServletHolder debugHolder = new ServletHolder();
                    server.start();
                    server.setStopTimeout(0);
                    info("started %s", server);
                    Thread.sleep(5000);
                    info("stopping %s", server);
                    server.stop();
                    info("stopped %s", server);
                }
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(SimpleJMXAcceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (Exception ex)
        {
            Logger.getLogger(SimpleJMXAcceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
