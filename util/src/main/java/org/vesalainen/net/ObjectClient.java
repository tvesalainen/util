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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.net.ObjectServer.Request;
import org.vesalainen.net.ObjectServer.Response;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ObjectClient implements AutoCloseable
{

    private SocketChannel sc;
    private OutputStream os;
    private ObjectOutputStream oos;
    private InputStream is;
    private ObjectInputStream ois;

    private ObjectClient(SocketChannel sc, OutputStream os, ObjectOutputStream oos, InputStream is)
    {
        this.sc = sc;
        this.os = os;
        this.oos = oos;
        this.is = is;
    }
    
    public static ObjectClient open(String server, int port) throws IOException
    {
        SocketChannel sc = SocketChannel.open(new InetSocketAddress(server, port));
        OutputStream os = Channels.newOutputStream(sc);
        ObjectOutputStream oos = new ObjectOutputStream(os);
        InputStream is = Channels.newInputStream(sc);
        return new ObjectClient(sc, os, oos, is);
    }
    @Override
    public void close() throws IOException
    {
        ois.close();
        is.close();
        oos.close();
        os.close();
        sc.close();
    }
    public <T> T get(String target) throws IOException
    {
        try
        {
            Request request = new Request(target);
            oos.writeObject(request);
            if (ois == null)
            {
                ois = new ObjectInputStream(is);
            }
            Response response = (Response) ois.readObject();
            return (T) response.getTarget();
        }
        catch (ClassNotFoundException ex)
        {
            throw new IOException(ex);
        }
    }

}
