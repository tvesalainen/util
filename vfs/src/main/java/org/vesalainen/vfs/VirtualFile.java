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
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.MapSet;
import org.vesalainen.util.WeakIdentityMapSet;
import org.vesalainen.util.logging.AttachedLogger;
import static org.vesalainen.vfs.VirtualFile.Type.*;
import org.vesalainen.vfs.arch.cpio.SimpleChecksum;
import org.vesalainen.vfs.attributes.FileAttributeAccess;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class VirtualFile extends FileAttributeAccessStore implements FileAttributeAccess, AttachedLogger
{

    public enum Type {REGULAR, DIRECTORY, SYMBOLIC_LINK};
    private VirtualFileStore fileStore;
    private Type type;
    private ByteBuffer content;
    private Map<String,? extends FileAttributeView> viewMap;
    private Path symbolicTarget;
    private MapSet<ByteBuffer,ByteBuffer> refSet = new WeakIdentityMapSet<>();

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
                setAttribute(PERMISSIONS, PosixFilePermissions.fromString("rw-r--r--"));
                break;
            case DIRECTORY:
                setAttribute(IS_DIRECTORY, true);
                setAttribute(PERMISSIONS, PosixFilePermissions.fromString("rwxr-xr-x"));
                break;
            case SYMBOLIC_LINK:
                setAttribute(IS_SYMBOLIC_LINK, true);
                setAttribute(PERMISSIONS, PosixFilePermissions.fromString("rwxrwxrwx"));
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

    public VirtualFileStore getFileStore()
    {
        return fileStore;
    }

    @Override
    public Object get(Name name, Object def)
    {
        switch (name.toString())
        {
            case SIZE:
                return (long) content.limit();
            default:
                return super.get(name, def);
        }
    }

    @Override
    public void put(FileAttributeName.Name name, Object value)
    {
        switch (name.toString())
        {
            case SIZE:
                break;
            default:
                if (USER_VIEW.equals(name) && (value instanceof ByteBuffer))
                {
                    ByteBuffer bb = (ByteBuffer) value;
                    byte[] arr = new byte[bb.remaining()];
                    bb.get(arr);
                    super.put(name, arr);
                }
                else
                {
                    super.put(name, value);
                }
                break;
        }
    }

    public Path getSymbolicTarget()
    {
        if (symbolicTarget == null && type == SYMBOLIC_LINK)
        {
            ByteBuffer readView = readView(0);
            byte[] bytes = new byte[content.limit()];
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
        put(name, value);
    }
    public <V extends FileAttributeView> V getFileAttributeView(Class<V> type)
    {
        for (FileAttributeView view : viewMap.values())
        {
            Class<?>[] interfaces = view.getClass().getInterfaces();
            if (interfaces.length > 0 && interfaces[0].equals(type))
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
                Method method;
                try
                {
                    method = view.getClass().getMethod("readAttributes");
                    if (type.equals(method.getReturnType()))
                    {
                        return (A) method.invoke(view);
                    }
                }
                catch (NoSuchMethodException ex)
                {
                }
            }
        }
        catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
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
        if (matcher.any(SIZE))
        {
            map.put(extName(SIZE), (long)getSize());
        }
        if (matcher.any(CONTENT))
        {
            map.put(extName(CONTENT), readView(0));
        }
        if (matcher.any(CPIO_CHECKSUM))
        {
            SimpleChecksum checksum = new SimpleChecksum();
            checksum.update(readView(0));
            long value = checksum.getValue(); 
            map.put(extName(CPIO_CHECKSUM), Primitives.writeInt((int) value));
        }
        if (matcher.any(CRC32))
        {
            CRC32 crc32 = new CRC32();
            crc32.update(readView(0));
            long value = crc32.getValue(); 
            map.put(extName(CRC32), Primitives.writeInt((int) value));
        }
        if (matcher.any(MD5))
        {
            map.put(extName(MD5), getMessageDigest("MD5"));
        }
        if (matcher.any(SHA1))
        {
            map.put(extName(SHA1), getMessageDigest("SHA_1"));
        }
        if (matcher.any(SHA256))
        {
            map.put(extName(SHA256), getMessageDigest("SHA_256"));
        }
        return map;
    }
    private String extName(String name)
    {
        return FileAttributeName.getInstance(name).getName();
    }
    private byte[] getMessageDigest(String algorithm)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(readView(0));
            return md.digest();
        }
        catch (NoSuchAlgorithmException ex)
        {
            return null;
        }
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
            if (refSet.containsKey(content))
            {
                throw new IOException("cannot grow file because of writable mapping for content. (non carbage collected mapping?)");
            }
            int blockSize = fileStore.getBlockSize();
            int newCapacity = Math.max(((waterMark / blockSize) + 1) * blockSize, 2*content.capacity());
            ByteBuffer newBB = ByteBuffer.allocateDirect(newCapacity);
            newBB.put(content);
            newBB.flip();
            content = newBB;
        }
        ByteBuffer bb = content.duplicate();
        refSet.add(content, bb);
        bb.limit(waterMark).position(position);
        return bb;
    }
    private void growIfNeeded(int position, int needs) throws IOException
    {
        
    }
        /**
     * called after writing to commit that writing succeeded up to position.
     * If position > size the size is updated.
     * @param pos 
     */
    void commit(ByteBuffer bb)
    {
        content.limit(Math.max(content.limit(), bb.position()));
        boolean removed = refSet.removeItem(content, bb);
        assert removed;
    }
    /**
     * File size is set to pos unconditionally.
     * @param pos 
     */
    void truncate(int pos)
    {
        content.limit(pos);
    }

    int getSize()
    {
        return content.limit();
    }
}
