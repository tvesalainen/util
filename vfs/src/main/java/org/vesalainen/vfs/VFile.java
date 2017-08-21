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
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import static java.nio.file.attribute.PosixFilePermission.*;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.nio.DynamicByteBuffer;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class VFile
{
    private static final int MAX_SIZE = Integer.MAX_VALUE;
    private Map<String,Object> attributes = new HashMap<>();
    private ByteBuffer content;
    private int size;
    private FileTime lastModifiedTime = FileTime.from(Instant.now());
    private FileTime lastAccessTime = FileTime.from(Instant.now());
    private FileTime creationTime = FileTime.from(Instant.now());
    private BasicAttrs basicAttrs = new BasicAttrs();

    public VFile() throws IOException
    {
        this.content = DynamicByteBuffer.create(MAX_SIZE);
    }

    VFile(ByteBuffer content)
    {
        this.content = content;
    }

    private void init()
    {
        
    }
    public BasicAttrs getBasicAttrs()
    {
        return basicAttrs;
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
        return size;
    }
    void setFileAttributes(FileAttribute<?>... attrs)
    {
        for (FileAttribute fa : attrs)
        {
            attributes.put(fa.name(), fa.value());
        }
    }
    public static final Map<String,Object> fromMode(short mode)
    {
        Map<String,Object> map = new HashMap<>();
        EnumSet perms = EnumSet.noneOf(PosixFilePermission.class);
        map.put(PERMISSIONS, perms);
        switch (mode & 0170000)
        {
            case 0120000:   // i
                map.put(IS_SYMBOLIC_LINK, true);
                break;
            case 0100000:   // -
                map.put(IS_REGULAR, true);
                break;
            case 0040000:   // d
                map.put(IS_DIRECTORY, true);
                break;
            default:
                map.put(IS_OTHER, true);
                break;
        }
        // owner
        if ((mode & 00400)==00400)
        {
            perms.add(OWNER_READ);
        }
        if ((mode & 00200)==00200)
        {
            perms.add(OWNER_WRITE);
        }
        if ((mode & 00100)==00100)
        {
            perms.add(OWNER_EXECUTE);
            if ((mode & 0004000)==0004000)
            {
                map.put(SET_UID, true);
            }
        }
        // group
        if ((mode & 0040)==0040)
        {
            perms.add(GROUP_READ);
        }
        if ((mode & 0020)==0020)
        {
            perms.add(GROUP_WRITE);
        }
        if ((mode & 0010)==0010)
        {
            perms.add(GROUP_EXECUTE);
            if ((mode & 0002000)==0002000)
            {
                map.put(SET_GID, true);
            }
        }
        // others
        if ((mode & 004)==004)
        {
            perms.add(OTHERS_READ);
        }
        if ((mode & 002)==002)
        {
            perms.add(OTHERS_WRITE);
        }
        if ((mode & 001)==001)
        {
            perms.add(OTHERS_EXECUTE);
            if ((mode & 0001000)==0001000)
            {
                map.put(STICKY_BIT, true);
            }
        }
        else
        {
            if ((mode & 0001000)==0001000)
            {
                map.put(STICKY_BIT, true);
            }
        }
        return map;
    }
    public class BasicAttrs implements BasicFileAttributes
    {

        @Override
        public FileTime lastModifiedTime()
        {
            return lastModifiedTime;
        }

        @Override
        public FileTime lastAccessTime()
        {
            return lastAccessTime;
        }

        @Override
        public FileTime creationTime()
        {
            return creationTime;
        }

        @Override
        public boolean isRegularFile()
        {
            return true;
        }

        @Override
        public boolean isDirectory()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isSymbolicLink()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean isOther()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long size()
        {
            return size;
        }

        @Override
        public Object fileKey()
        {
            return null;
        }
        
    }
}
