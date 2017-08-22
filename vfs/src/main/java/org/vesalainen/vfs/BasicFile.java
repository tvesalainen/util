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
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.vesalainen.vfs.attributes.BasicFileAttributeViewImpl;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.CREATION_TIME;
import static org.vesalainen.vfs.attributes.FileAttributeName.LAST_ACCESS_TIME;
import static org.vesalainen.vfs.attributes.FileAttributeName.LAST_MODIFIED_TIME;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicFile extends VirtualFile
{
    protected Map<String,Object> attributes = new HashMap<>();
    protected BasicFileAttributeView basicFileAttributeView;
    
    public BasicFile(Type type, ByteBuffer content, FileAttribute<?>... attrs) throws IOException
    {
        super(type, content, attrs);
        FileTime now = FileTime.from(Instant.now());
        setAttribute(CREATION_TIME, now);
        setAttribute(LAST_ACCESS_TIME, now);
        setAttribute(LAST_MODIFIED_TIME, now);
        basicFileAttributeView = new BasicFileAttributeViewImpl(attributes);
    }
    
    @Override
    public final void setAttribute(String name, Object value)
    {
        String normalized = FileAttributeName.normalize(name);
        checkAttribute(normalized, value);
        attributes.put(normalized, value);
    }
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Class<V> type)
    {
        if (BasicFileAttributeView.class.equals(type))
        {
            return (V) basicFileAttributeView;
        }
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Class<A> type) throws IOException
    {
        if (BasicFileAttributes.class.equals(type))
        {
            return (A) basicFileAttributeView.readAttributes();
        }
        throw new UnsupportedOperationException(type+" not supported");
    }
    @Override
    public Map<String, Object> readAttributes(String names) throws IOException
    {
        Map<String, Object> map = new HashMap<>();
        FileAttributeName.FileAttributeNameMatcher matcher = new FileAttributeName.FileAttributeNameMatcher(names);
        attributes.forEach((n,a)->
        {
            if (matcher.any(n))
            {
                map.put(n, a);
            }
        });
        return map;
    }

    protected void checkAttribute(String name, Object value)
    {
        if (!name.startsWith("basic:"))
        {
            throw new UnsupportedOperationException(name);
        }
        FileAttributeName.check(name, value);
    }
}
