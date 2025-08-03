/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.nio.channels;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated Duplicates URLConnection
 * ReadableByteChannelFactory contains some convenient methods to create ReadableByteChannels.
 * 
 * <p>Note that for URL (URI) protocols, only file and http have currently support
 * as FileChannel or SocketChannel. Other protocols are converted to ByteChannel
 * by java.nio.channels.Channels.
 * @see java.nio.channels.Channels#newChannel(java.io.InputStream) 
 * 
 * @author Timo Vesalainen
 */
public class ReadableByteChannelFactory
{
    private static final Map<String,ProtocolHandler> map = new HashMap<>();
    
    static
    {
        addHandler("file", new FileHandler());
        addHandler("http", new HTTPHandler());
    }
    /**
     * Adds a new handler for protocol.
     * @param protocol
     * @param handler 
     * @see java.net.URL#getProtocol() 
     */
    public static void addHandler(String protocol, ProtocolHandler handler)
    {
        map.put(protocol, handler);
    }
    public static boolean hasHandler(String name)
    {
        return map.containsKey(name);
    }
    /**
     * Returns a Channel for reading.
     * @param file
     * @return
     * @throws IOException 
     */
    public static ReadableByteChannel getInstance(File file) throws IOException
    {
        return getInstance(file.toPath());
    }
    /**
     * Returns a Channel for reading.
     * @param path
     * @return
     * @throws IOException 
     */
    public static ReadableByteChannel getInstance(Path path) throws IOException
    {
        return FileChannel.open(path, StandardOpenOption.READ);
    }
    /**
     * Returns a Channel for reading.
     * @param uri
     * @return
     * @throws IOException 
     */
    public static ReadableByteChannel getInstance(URI uri) throws IOException
    {
        return getInstance(uri.toURL());
    }
    /**
     * Returns a Channel for reading.
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    public static ReadableByteChannel getInstance(URL url) throws IOException
    {
        ProtocolHandler ph = map.get(url.getProtocol());
        if (ph != null)
        {
            return ph.create(url);
        }
        else
        {
            return Channels.newChannel(url.openStream());
        }
    }
    private static class FileHandler implements ProtocolHandler
    {

        @Override
        public ReadableByteChannel create(URL url) throws IOException
        {
            File file = new File(url.getFile());
            return getInstance(file);
        }
        
    }
    private static class HTTPHandler implements ProtocolHandler
    {

        @Override
        public ReadableByteChannel create(URL url) throws IOException
        {
            int port = url.getPort();
            if (port == -1)
            {
                port = 80;
            }
            SocketAddress sa = new InetSocketAddress(url.getHost(), port);
            SocketChannel sc = SocketChannel.open(sa);
            ByteBuffer bb = ByteBuffer.allocate(256);
            write(sc, "GET "+url.getFile()+" HTTP/1.1\r\n");
            write(sc, "Host: "+url.getHost()+"\r\n");
            write(sc, "Connection: close\r\n\r\n");
            String hdr = readline(sc);
            if (!hdr.startsWith("HTTP/1.1 200"))
            {
                throw new IOException(hdr);
            }
            while (hdr.length() > 2)
            {
                System.err.print(hdr);
                hdr = readline(sc);
            }
            sc.shutdownOutput();
            return sc;
        }
        private void write(SocketChannel sc, String line) throws IOException
        {
            byte[] bytes = line.getBytes(StandardCharsets.US_ASCII);
            ByteBuffer wrap = ByteBuffer.wrap(bytes);
            sc.write(wrap);
        }
        private String readline(SocketChannel sc) throws IOException
        {
            StringBuilder sb = new StringBuilder();
            ByteBuffer bb = ByteBuffer.allocate(1);
            while (true)
            {
                int rc = sc.read(bb);
                if (rc != 1)
                {
                    throw new IOException("unexpected end of channel");
                }
                char cc = (char)bb.get(0);
                sb.append(cc);
                if (cc == '\n')
                {
                    return sb.toString();
                }
                bb.clear();
            }
        }
    }
}
