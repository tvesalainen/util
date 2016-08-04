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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Timo Vesalainen
 */
public class RewindableReader extends BufferedReader implements Rewindable
{
    protected int maxRewind;
    protected int lastReadCount;
    /**
     * Creates a Rewindable reader
     * @param reader A Reader
     * @param sz Input buffer size
     * @param maxRewind Maximum rewind count
     */
    public RewindableReader(Reader reader, int sz, int maxRewind)
    {
        super(reader, sz);
        this.maxRewind = maxRewind;
    }
    /**
     * Creates a Rewindable reader
     * @param reader A Reader
     * @param maxRewind Maximum rewind count
     */
    public RewindableReader(Reader reader, int maxRewind)
    {
        super(reader);
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
    public void reset() throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void mark(int i) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public long skip(long l) throws IOException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int read(char[] chars, int off, int len) throws IOException
    {
        super.mark(maxRewind);
        lastReadCount = super.read(chars, off, len);
        return lastReadCount;
    }

    @Override
    public int read() throws IOException
    {
        super.mark(maxRewind);
        lastReadCount = 1;
        return super.read();
    }
    
    /**
     * Overrides BufferedReader to not support mark!
     * @return 
     */
    @Override
    public boolean markSupported()
    {
        return false;
    }
    
}
