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

import java.io.Closeable;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Timo Vesalainen
 */
public class PushbackReadable implements Readable, AutoCloseable, Pushbackable<CharBuffer>
{
    protected final Readable in;
    private Deque<CharBuffer> stack;
    
    public PushbackReadable(Readable in)
    {
        this.in = in;
    }
    
    @Override
    public int read(CharBuffer cb) throws IOException
    {
        if (stack == null || stack.isEmpty())
        {
            return in.read(cb);
        }
        else
        {
            int count = 0;
            while (cb.hasRemaining())
            {
                CharBuffer peek = (CharBuffer) stack.peek();
                if (peek == null)
                {
                    break;
                }
                int rc = peek.read(cb);
                assert rc != -1;
                count += rc;
                if (!peek.hasRemaining())
                {
                    stack.pop();
                }
            }
            return count;
        }
    }

    @Override
    public void close() throws Exception
    {
        if (in instanceof Closeable)
        {
            Closeable c = (Closeable) in;
            c.close();
        }
    }

    @Override
    public void pushback(CharBuffer... buffers) throws IOException
    {
        if (stack == null)
        {
            stack = new ArrayDeque<>();
        }
        int size = 0;
        for (CharBuffer cb : buffers)
        {
            size += cb.remaining();
        }
        CharBuffer nbuf = CharBuffer.allocate(size);
        for (CharBuffer cb : buffers)
        {
            nbuf.put(cb);
        }
        nbuf.clear();
        stack.push(nbuf);
    }

    @Override
    public boolean hasPushback()
    {
        return stack != null && !stack.isEmpty();
    }
    
}
