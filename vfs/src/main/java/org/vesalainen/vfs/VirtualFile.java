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
package org.vesalainen.vfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.Map;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class VirtualFile
{
    protected enum Type {REGULAR, DIRECTORY, SYMBOLIC_LINK};
    protected static final int MAX_SIZE = Integer.MAX_VALUE;
    protected Type type;
    protected ByteBuffer content;
    protected long size;

    protected VirtualFile(Type type, ByteBuffer content, FileAttribute<?>... attrs) throws IOException
    {
        this.type = type;
        this.content = content;
        for (FileAttribute fa : attrs)
        {
            setAttribute(fa.name(), fa.value());
        }
    }
    
    public abstract void setAttribute(String attribute, Object value);
    public abstract <V extends FileAttributeView> V getFileAttributeView(Class<V> type);

    public abstract <A extends BasicFileAttributes> A readAttributes(Class<A> type) throws IOException;
    public abstract Map<String, Object> readAttributes(String names) throws IOException;
    
    ByteBuffer duplicate()
    {
        return content.duplicate();
    }
    void append(int pos)
    {
        size = Math.max(size, pos);
        setAttribute(SIZE, size);
    }
    void truncate(int pos)
    {
        size = pos;
        setAttribute(SIZE, size);
    }

    int getSize()
    {
        return (int) size;
    }
}
