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
package org.vesalainen.vfs.attributes;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipal;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BasicFileAttributeViewImpl extends FileAttributeViewImpl implements BasicFileAttributeView, FileOwnerAttributeView
{
    private BasicFileAttributes basicAttr = new BasicFileAttributesImpl();

    protected BasicFileAttributeViewImpl(String name, FileAttributeAccess access)
    {
        super(name, access);
    }
    
    
    public BasicFileAttributeViewImpl(FileAttributeAccess access)
    {
        super("basic", access);
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException
    {
        return basicAttr;
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException
    {
        if (lastModifiedTime != null)
        {
            put(LAST_MODIFIED_TIME, lastModifiedTime);
        }
        if (lastAccessTime != null)
        {
            put(LAST_ACCESS_TIME, lastAccessTime);
        }
        if (createTime != null)
        {
            put(CREATION_TIME, createTime);
        }
    }

    @Override
    public UserPrincipal getOwner() throws IOException
    {
        return (UserPrincipal) get(OWNER);
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException
    {
        put(OWNER, owner);
    }
    
    public class BasicFileAttributesImpl implements BasicFileAttributes
    {

        @Override
        public FileTime lastModifiedTime()
        {
            return (FileTime) get(LAST_MODIFIED_TIME);
        }

        @Override
        public FileTime lastAccessTime()
        {
            return (FileTime) get(LAST_ACCESS_TIME);
        }

        @Override
        public FileTime creationTime()
        {
            return (FileTime) get(CREATION_TIME);
        }

        @Override
        public boolean isRegularFile()
        {
            return (boolean) get(IS_REGULAR);
        }

        @Override
        public boolean isDirectory()
        {
            return (boolean) get(IS_DIRECTORY);
        }

        @Override
        public boolean isSymbolicLink()
        {
            return (boolean) get(IS_SYMBOLIC_LINK);
        }

        @Override
        public boolean isOther()
        {
            return (boolean) get(IS_OTHER);
        }

        @Override
        public long size()
        {
            return (long) get(SIZE);
        }

        @Override
        public Object fileKey()
        {
            return get(FILE_KEY);
        }
        
    }
}
