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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.nio.DynamicByteBuffer;
import org.vesalainen.vfs.attributes.BasicFileAttributeViewImpl;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFile
{
    protected static final int MAX_SIZE = Integer.MAX_VALUE;
    protected Map<String,Object> attributes = new HashMap<>();
    protected ByteBuffer content;
    protected long size;
    protected BasicFileAttributeView basicFileAttributeView;

    VirtualFile(ByteBuffer content)
    {
        this.content = content;
        FileTime now = FileTime.from(Instant.now());
        attributes.put(CREATION_TIME, now);
        attributes.put(LAST_ACCESS_TIME, now);
        attributes.put(LAST_MODIFIED_TIME, now);
        basicFileAttributeView = new BasicFileAttributeViewImpl(attributes);
    }
    
    public <V extends FileAttributeView> V getFileAttributeView(Class<V> type)
    {
        if (BasicFileAttributeView.class.equals(type))
        {
            return (V) basicFileAttributeView;
        }
        return null;
    }

    public <A extends BasicFileAttributes> A readAttributes(Class<A> type) throws IOException
    {
        if (BasicFileAttributes.class.equals(type))
        {
            return (A) basicFileAttributeView.readAttributes();
        }
        return null;
    }
    public Map<String, Object> readAttributes(String attributes) throws IOException
    {
        
    }
    ByteBuffer duplicate()
    {
        return content.duplicate();
    }
    void append(int pos)
    {
        size = Math.max(size, pos);
    }
    void truncate(int pos)
    {
        size = pos;
    }

    int getSize()
    {
        return (int) size;
    }
    void setFileAttributes(FileAttribute<?>... attrs)
    {
        for (FileAttribute fa : attrs)
        {
            attributes.put(fa.name(), fa.value());
        }
    }
}
