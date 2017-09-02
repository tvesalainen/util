/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.vfs.arch;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.util.Map;
import org.vesalainen.vfs.FileAttributeAccessStore;
import org.vesalainen.vfs.attributes.FileAttributeName;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class Header extends FileAttributeAccessStore
{
    public Object get(String name)
    {
        return get(FileAttributeName.getInstance(name), null);
    }
    public void put(String name, Object value)
    {
        put(FileAttributeName.getInstance(name), value);
    }
    protected static void align(SeekableByteChannel ch, long align) throws IOException
    {
        ch.position(alignedPosition(ch, align));
    }

    protected static long alignedPosition(SeekableByteChannel ch, long align) throws IOException
    {
        long position = ch.position();
        long mod = position % align;
        if (mod > 0)
        {
            return position + align - mod;
        }
        else
        {
            return position;
        }
    }

    protected static void skip(SeekableByteChannel ch, long skip) throws IOException
    {
        ch.position(ch.position() + skip);
    }

    
    public abstract boolean isEof();
    public abstract String filename();
    public abstract void load(SeekableByteChannel channel) throws IOException;
    public abstract void store(SeekableByteChannel channel, String filename, Map<String,Object> attributes) throws IOException;
    public abstract void storeEof(SeekableByteChannel channel) throws IOException;
}
