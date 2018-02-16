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
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Consumer;
import static java.util.logging.Level.SEVERE;
import org.vesalainen.util.concurrent.Locks;
import org.vesalainen.util.logging.JavaLogging;

/**
 * NetFile implements the basic processing of loading a file from net and storing
 * it in file. 
 * <p>Access file only in consumer method while it is write-locked.
 * <p>Inherited class can use read-lock to give shared access to some resources
 * and still protect from file update.
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class NetFile extends JavaLogging implements Runnable
{
    protected Path file;
    protected URL url;
    protected long expires;
    protected int timeout = 60000;
    protected long maxFileSize = 100000;
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected ReadLock readLock = rwLock.readLock();
    private WriteLock writeLock = rwLock.writeLock();
    private Thread thread;
    /**
     * Creates a NetFile
     * @param file Backup file for data store.
     * @param url Net resource
     * @param consumer Function to call with backup file path.
     * @param expires Expiring time after resource will be re-loaded.
     * @param unit Expire unit.
     */
    public NetFile(Path file, URL url, long expires, TimeUnit unit)
    {
        super(NetFile.class);
        this.file = file;
        this.url = url;
        this.expires = unit.toMillis(expires);
    }
    /**
     * All file access should be done during update call. writeLock is locked
     * during the call.
     * @param file
     * @throws IOException 
     */
    protected abstract void update(Path file) throws IOException;
    /**
     * Call to refresh the file. During the call consumer is called at least once.
     * @throws IOException 
     */
    public void refresh() throws IOException
    {
        if (Files.exists(file))
        {
            fine("%s exist", file);
            writeLock.lock();
            try
            {
                finer("start update(%s)", file);
                update(file);
                finer("end update(%s)", file);
            }
            finally
            {
                writeLock.unlock();
            }
            FileTime lmt = Files.getLastModifiedTime(file);
            if (thread == null && lmt.toMillis()+expires < System.currentTimeMillis())
            {
                thread = new Thread(this, url.toString());
                thread.setDaemon(true);
                thread.start();
                fine("started thread for %s", url);
            }
        }
        else
        {
            fine("%s doesn't exist", file);
            run();
        }
    }

    @Override
    public void run()
    {
        try
        {
            fine("start downloading %s", url);
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
            Path parent = file.getParent();
            if (parent != null)
            {
                Files.createDirectories(parent);
            }
            try (OutputStream os = Files.newOutputStream(file))
            {
                os.write(buffer, 0, buffer.length-free);
                fine("wrote %s -> %s", url, file);
            }
            writeLock.lock();
            try
            {
                finer("start update(%s)", file);
                update(file);
                finer("end update(%s)", file);
            }
            finally
            {
                writeLock.unlock();
            }
        }
        catch (IOException ex)
        {
            log(SEVERE, ex, "%s: %s", url, ex.getMessage());
        }
        finally
        {
            thread = null;
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
