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

package org.vesalainen.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Timo Vesalainen
 */
public class RewindableInputStream extends BufferedInputStream implements Rewindable
{
    protected int maxRewind;
    protected int lastReadCount;

    public RewindableInputStream(InputStream in, int maxRewind)
    {
        super(in);
        this.maxRewind = maxRewind;
    }

    public RewindableInputStream(InputStream in, int sz, int maxRewind)
    {
        super(in, sz);
        this.maxRewind = maxRewind;
    }

    @Override
    public void rewind(int count) throws IOException
    {
        if (count < 0 || count > maxRewind)
        {
            throw new IOException(count+" > maxRewind (="+maxRewind+")");
        }
        super.reset();  
        super.skip(lastReadCount - count);
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    @Override
    public synchronized void reset() throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public synchronized void mark(int i)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public synchronized long skip(long l) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public synchronized int read(byte[] bytes, int off, int len) throws IOException
    {
        super.mark(maxRewind);
        lastReadCount = super.read(bytes, off, len);
        return lastReadCount;
    }

    @Override
    public synchronized int read() throws IOException
    {
        super.mark(maxRewind);
        lastReadCount = 1;
        return super.read();
    }
    
}
