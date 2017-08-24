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
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.AccessMode;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.vesalainen.vfs.VirtualFile.Type.*;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFile
{

    protected enum Type {REGULAR, DIRECTORY, SYMBOLIC_LINK};
    protected static final int MAX_SIZE = Integer.MAX_VALUE;
    protected VirtualFileStore fileStore;
    protected Type type;
    protected ByteBuffer content;
    protected long size;
    protected Map<String,Object> attributes = new HashMap<>();
    protected Map<String,? extends FileAttributeView> viewMap;
    protected Path symbolicTarget;

    protected VirtualFile(VirtualFileStore fileStore, Type type, ByteBuffer content, Set<String> views, FileAttribute<?>... attrs) throws IOException
    {
        this.fileStore = fileStore;
        this.type = type;
        this.content = content;
        if (content != null)
        {
            if (content.position() != 0)
            {
                throw new IllegalArgumentException(content.position()+" content position should be 0");
            }
        }
        this.viewMap = fileStore.provider().createViewMap(attributes, views);
        switch (type)
        {
            case REGULAR:
                setAttribute(IS_REGULAR, true);
                break;
            case DIRECTORY:
                setAttribute(IS_DIRECTORY, true);
                break;
            case SYMBOLIC_LINK:
                setAttribute(IS_SYMBOLIC_LINK, true);
                symbolicTarget = fileStore.fileSystem.getPath(new String(content.array(), US_ASCII));
                break;
            default:
                throw new UnsupportedOperationException(type.name());
        }
        for (FileAttribute fa : attrs)
        {
            setAttribute(fa.name(), fa.value());
        }
        FileTime now = FileTime.from(Instant.now());
        setAttribute(CREATION_TIME, now);
        setAttribute(LAST_ACCESS_TIME, now);
        setAttribute(LAST_MODIFIED_TIME, now);
    }

    public Path getSymbolicTarget()
    {
        return symbolicTarget;
    }

    public Type getType()
    {
        return type;
    }

    public boolean isRegular()
    {
        return type == REGULAR;
    }
    public boolean isDirectory()
    {
        return type == DIRECTORY;
    }
    public boolean isSymbolicLink()
    {
        return type == SYMBOLIC_LINK;
    }
    
    public boolean checkAccess(AccessMode... modes)
    {
        for (AccessMode am : modes)
        {
            switch (am)
            {
                case WRITE:
                    if (content.isReadOnly())
                    {
                        return false;
                    }
                    break;
                case READ:
                case EXECUTE:
                    if (type != REGULAR)
                    {
                        return false;
                    }
                    break;
                default:
                    throw new UnsupportedOperationException(am.name());
            }
        }
        return true;
    }
    public final void setAttribute(String name, Object value)
    {
        String normalized = FileAttributeName.normalize(name);
        checkAttribute(normalized, value);
        attributes.put(normalized, value);
    }
    public <V extends FileAttributeView> V getFileAttributeView(Class<V> type)
    {
        for (FileAttributeView view : viewMap.values())
        {
            if (type.isAssignableFrom(view.getClass()))
            {
                return (V) view;
            }
        }
        return null;
    }

    public <A extends BasicFileAttributes> A readAttributes(Class<A> type) throws IOException
    {
        if (BasicFileAttributes.class.equals(type))
        {
            BasicFileAttributeView attrs = getFileAttributeView(BasicFileAttributeView.class);
            if (attrs != null)
            {
                return (A) attrs.readAttributes();
            }
        }
        if (PosixFileAttributes.class.equals(type))
        {
            PosixFileAttributeView attrs = getFileAttributeView(PosixFileAttributeView.class);
            if (attrs != null)
            {
                return (A) attrs.readAttributes();
            }
        }
        throw new UnsupportedOperationException(type+" not supported");
    }
    public Map<String, Object> readAttributes(String names) throws IOException
    {
        Map<String, Object> map = new HashMap<>();
        FileAttributeName.FileAttributeNameMatcher matcher = new FileAttributeName.FileAttributeNameMatcher(viewMap.keySet(), names);
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
        FileAttributeName.check(name, value);
        for (String view : viewMap.keySet())
        {
            if (name.startsWith(view))
            {
                return;
            }
        }
        throw new UnsupportedOperationException(name);
    }
    /**
     * Returns read-only view of content. Position = 0, limit=size;
     * @return 
     */
    ByteBuffer readView()
    {
        ByteBuffer bb = content.duplicate().asReadOnlyBuffer();
        bb.limit((int) size);
        return bb;
    }
    /**
     * Returns view of content. Position = 0, limit=capacity;
     * @return 
     */
    ByteBuffer writeView()
    {
        ByteBuffer bb = content.duplicate();
        bb.limit(content.capacity());
        return bb;
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
