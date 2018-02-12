/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NetFile implements Runnable
{
    private Path dir;
    private Path file;
    private URL url;
    private long expires;
    private Consumer<Path> consumer;
    private int timeout = 60000;
    private long maxFileSize = 100000;

    public NetFile(Path dir, URL url, Consumer<Path> consumer, long expires, TimeUnit unit)
    {
        this.dir = dir;
        this.file = Paths.get(dir.toString(), url.getPath());
        this.url = url;
        this.consumer = consumer;
        this.expires = unit.toMillis(expires);
    }
    
    public void refresh() throws IOException
    {
        if (Files.exists(file))
        {
            consumer.accept(file);
            FileTime lmt = Files.getLastModifiedTime(file);
            if (lmt.toMillis()+expires < System.currentTimeMillis())
            {
                Thread thread = new Thread(this, url.toString());
                thread.setDaemon(true);
                thread.start();
            }
        }
        else
        {
            run();
        }
    }

    @Override
    public void run()
    {
        try
        {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            InputStream is = connection.getInputStream();
            long contentLength = contentLength(connection);
            byte[] buffer = new byte[(int)contentLength];
            int free = buffer.length;
            int off = 0;
            while (true)
            {
                int rc = is.read(buffer, off, free);
                if (rc == -1)
                {
                    break;
                }
                if (free <= 0)
                {
                    throw new IllegalArgumentException(url+" too big");
                }
                off += rc;
                free -= rc;
            }
            Files.createDirectories(file.getParent());
            OutputStream os = Files.newOutputStream(file);
            os.write(buffer, 0, buffer.length-free);
            consumer.accept(file);
        }
        catch (IOException ex)
        {
            Logger.getLogger(NetFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private long contentLength(URLConnection connection) throws IOException
    {
        long length = connection.getContentLengthLong();
        if (length != -1)
        {
            return length;
        }
        else
        {
            if (Files.exists(file))
            {
                return Math.min(Files.size(file)*2, maxFileSize);
            }
            else
            {
                return maxFileSize;
            }
        }
    }
    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public long getMaxFileSize()
    {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize)
    {
        this.maxFileSize = maxFileSize;
    }

}
