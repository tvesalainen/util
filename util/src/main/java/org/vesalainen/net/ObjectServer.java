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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.vesalainen.net.ObjectServer.Status.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ObjectServer extends InetServer
{
    public enum Status {OK, NOT_FOUND}
    
    private Map<String,Object> map = new HashMap<>();
    
    ObjectServer(int port)
    {
        this(port, Executors.newCachedThreadPool());
    }
    
    public ObjectServer(int port, ExecutorService executor)
    {
        super(port, ObjectServer.class, executor);
    }

    public <T> void put(String name, T target)
    {
        map.put(name, target);
    }
    @Override
    protected void handle(SocketChannel sc) throws IOException
    {
        try (InputStream is = Channels.newInputStream(sc);
            ObjectInputStream ois = new ObjectInputStream(is);
            OutputStream os = Channels.newOutputStream(sc);
            ObjectOutputStream oos = new ObjectOutputStream(os))
        {
            while (true)
            {
                Request request = (Request) ois.readObject();
                Object target = map.get(request.name);
                Response response;
                if (target != null)
                {
                    response = new Response(OK, target);
                }
                else
                {
                    response = new Response(NOT_FOUND);
                }
                oos.writeObject(response);
            }
        }
        catch (EOFException ex)
        {
            // ok
        }
        catch (ClassNotFoundException ex)
        {
            throw new IOException(ex);
        }
    }
    
    public static class Message implements Serializable
    {
        private static final long serialVersionUID = 1L;

    }
    public static class Request extends Message
    {
        private String name;

        public Request(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
        
    }
    public static class Response extends Message
    {
        private Status status;
        private Object target;

        public Response(Status status)
        {
            this.status = status;
        }

        public Response(Status status, Object target)
        {
            this.status = status;
            this.target = target;
        }

        public Status getStatus()
        {
            return status;
        }

        public Object getTarget()
        {
            return target;
        }
        
    }
}
