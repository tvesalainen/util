/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import org.vesalainen.util.AbstractServer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class InetServer extends AbstractServer
{
    protected final int port;

    protected InetServer(int port, Class<? extends AbstractServer> me, ExecutorService executor)
    {
        super(me, executor);
        this.port = port;
    }

    @Override
    protected void doRun() throws IOException
    {
        InetSocketAddress address = new InetSocketAddress(port);
        try (ServerSocketChannel ssc = ServerSocketChannel.open())
        {
            ssc.bind(address);
            running();
            while (true)
            {
                SocketChannel sc = ssc.accept();
                executor.submit(()->hndl(sc));
            }
        }
    }

    private void hndl(SocketChannel sc)
    {
        try
        {
            handle(sc);
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "%s:%s %s", name, sc, ex.getMessage());
        }
        finally
        {
            try
            {
                sc.close();
            }
            catch (IOException ex)
            {
                log(SEVERE, ex, "closing %s:%s %s", name, sc, ex.getMessage());
            }
        }
    }
    protected abstract void handle(SocketChannel sc) throws IOException;

    
}
