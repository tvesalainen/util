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
    public enum Type {REGULAR, DIRECTORY, HARD, SYMBOLIC};
    protected Type type;

    public Type getType()
    {
        return type;
    }

    public Object get(String name)
    {
        return get(FileAttributeName.getInstance(name), null);
    }
    public void put(String name, Object value)
    {
        put(FileAttributeName.getInstance(name), value);
    }
    
    public abstract boolean isEof();
    public abstract String getFilename();
    public abstract String getLinkname();
    public abstract void load(SeekableByteChannel channel) throws IOException;
    public abstract void store(SeekableByteChannel channel, String filename, FileFormat format, String linkname, Map<String,Object> attributes, byte[] digest) throws IOException;
    public abstract void storeEof(SeekableByteChannel channel, FileFormat format) throws IOException;
    public abstract byte[] digest();
    public abstract String digestAlgorithm();
    public abstract boolean supportsDigest();
    public abstract long size();
}
