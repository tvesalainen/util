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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.vesalainen.vfs.VirtualFile.Type.*;
import org.vesalainen.vfs.attributes.FileAttributeAccess;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VirtualFile implements FileAttributeAccess
{

    protected enum Type {REGULAR, DIRECTORY, SYMBOLIC_LINK};
    protected VirtualFileStore fileStore;
    protected Type type;
    protected ByteBuffer content;
    protected long size;    // has to be long because of attribute SIZE
    protected Map<Name,Object> attributes = new HashMap<>();
    protected Map<String,? extends FileAttributeView> viewMap;
    protected Path symbolicTarget;

    protected VirtualFile(VirtualFileStore fileStore, Type type, Set<String> views, FileAttribute<?>... attrs) throws IOException
    {
        this.fileStore = fileStore;
        this.type = type;
        this.content = ByteBuffer.allocateDirect(0);
        this.viewMap = fileStore.provider().createViewMap(this, views);
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

    @Override
    public Object get(Name name, Object def)
    {
        switch (name.toString())
        {
            case SIZE:
                return (long)size;
            default:
                return attributes.getOrDefault(name, def);
        }
    }

    @Override
    public Set<Name> names()
    {
        return attributes.keySet();
    }

    @Override
    public void put(Name name, Object value)
    {
        switch (name.toString())
        {
            case SIZE:
                throw new IllegalArgumentException("not allowed to set "+name);
            default:
                if (USER_VIEW.equals(name) && (value instanceof ByteBuffer))
                {
                    ByteBuffer bb = (ByteBuffer) value;
                    byte[] arr = new byte[bb.remaining()];
                    bb.get(arr);
                    attributes.put(name, arr);
                }
                else
                {
                    attributes.put(name, value);
                }
                break;
        }
    }

    @Override
    public void delete(Name name)
    {
        attributes.remove(name);
    }
    
    public Path getSymbolicTarget()
    {
        if (symbolicTarget == null && type == SYMBOLIC_LINK)
        {
            ByteBuffer readView = readView(0);
            byte[] bytes = new byte[(int)size];
            readView.get(bytes);
            symbolicTarget = fileStore.fileSystem.getPath(new String(bytes, US_ASCII));
        }
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
    public final void setAttribute(String attribute, Object value)
    {
        Name name = FileAttributeName.getInstance(attribute);
        checkAttribute(name, value);
        attributes.put(name, value);
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
        try
        {
            for (FileAttributeView view : viewMap.values())
            {
                Method method = view.getClass().getMethod("readAttributes");
                if (type.isAssignableFrom(method.getReturnType()))
                {
                    return (A) method.invoke(view);
                }
            }
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            throw new IOException(ex);
        }
        throw new UnsupportedOperationException(type+" not supported");
    }
    public Map<String, Object> readAttributes(String names) throws IOException
    {
        Map<String, Object> map = new HashMap<>();
        FileAttributeName.FileAttributeNameMatcher matcher = new FileAttributeName.FileAttributeNameMatcher(names);
        attributes.forEach((n,a)->
        {
            if (matcher.any(n))
            {
                map.put(n.getName(), a);
            }
        });
        return map;
    }

    protected void checkAttribute(Name name, Object value)
    {
        FileAttributeName.check(name, value);
        for (String view : viewMap.keySet())
        {
            if (view.equals(name.getView()))
            {
                return;
            }
        }
        throw new UnsupportedOperationException(name.toString());
    }
    /**
     * Returns read-only view of content. Position = 0, limit=size;
     * @return ByteBuffer position set to given position limit is file size.
     */
    ByteBuffer readView(int position)
    {
        ByteBuffer bb = content.duplicate().asReadOnlyBuffer();
        bb.position(position);
        bb.limit((int) size);
        return bb;
    }
    /**
     * Returns view of content. 
     * @return ByteBuffer position set to given position limit is position+needs.
     */
    ByteBuffer writeView(int position, int needs) throws IOException
    {
        int waterMark = position+needs;
        if (waterMark > content.capacity())
        {
            int blockSize = fileStore.getBlockSize();
            int newCapacity = ((waterMark / blockSize) + 1) * blockSize;
            ByteBuffer newBB = ByteBuffer.allocateDirect(newCapacity);
            content.limit((int) size);
            newBB.put(content);
            content = newBB;
        }
        ByteBuffer bb = content.duplicate();
        bb.position(position);
        bb.limit(waterMark);
        return bb;
    }
    /**
     * called after writing to commit that writing succeeded up to position.
     * If position > size the size is updated.
     * @param pos 
     */
    void commit(int pos)
    {
        size = Math.max(size, pos);
    }
    /**
     * File size is set to pos unconditionally.
     * @param pos 
     */
    void truncate(int pos)
    {
        size = pos;
    }

    int getSize()
    {
        return (int) size;
    }
}
