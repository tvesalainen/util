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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.vesalainen.nio.DynamicByteBuffer;

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
