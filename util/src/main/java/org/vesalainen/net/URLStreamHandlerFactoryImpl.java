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
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * URLStreamHandlerFactoryImpl implements URLStreamHandlerFactory with raw
 * protocol.
 * TCP protocol handles tcp socket connection to port. 
 * <p>TCP connection to host pi2 and port 10111 is tcp://pi2:10111
 * <p>Use URL.setURLStreamHandlerFactory(new URLStreamHandlerFactoryImpl());
 * to activate.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory) 
 */
public class URLStreamHandlerFactoryImpl implements URLStreamHandlerFactory
{

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        switch (protocol)
        {
            case "tcp":
                return new TCPURLStreamHandler();
            default:
                return null;
        }
    }
    public class TCPURLStreamHandler extends URLStreamHandler
    {

        public TCPURLStreamHandler()
        {
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException
        {
            return new RawURLConnection(u);
        }

    }    
    public class RawURLConnection extends URLConnection
    {
        private Socket socket;

        public RawURLConnection(URL url)
        {
            super(url);
        }

        @Override
        public void connect() throws IOException
        {
            socket = new Socket(url.getHost(), url.getPort());
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
            if (socket == null)
            {
                connect();
            }
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException
        {
            if (socket == null)
            {
                connect();
            }
            return socket.getOutputStream();
        }

    }
}
