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
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DosFileAttributeViewImpl extends BasicFileAttributeViewImpl implements DosFileAttributeView
{
    private DosFileAttributes dosFileAttributes = new DosFileAttributesImpl();
    
    public DosFileAttributeViewImpl(FileAttributeAccess access)
    {
        super("dos", access);
    }

    @Override
    public DosFileAttributes readAttributes() throws IOException
    {
        return dosFileAttributes;
    }

    @Override
    public void setReadOnly(boolean value) throws IOException
    {
        put(READONLY, value);
    }

    @Override
    public void setHidden(boolean value) throws IOException
    {
        put(HIDDEN, value);
    }

    @Override
    public void setSystem(boolean value) throws IOException
    {
        put(SYSTEM, value);
    }

    @Override
    public void setArchive(boolean value) throws IOException
    {
        put(ARCHIVE, value);
    }

    public class DosFileAttributesImpl extends BasicFileAttributesImpl implements DosFileAttributes
    {

        @Override
        public boolean isReadOnly()
        {
            return (boolean) get(READONLY);
        }

        @Override
        public boolean isHidden()
        {
            return (boolean) get(HIDDEN);
        }

        @Override
        public boolean isArchive()
        {
            return (boolean) get(ARCHIVE);
        }

        @Override
        public boolean isSystem()
        {
            return (boolean) get(SYSTEM);
        }

    }
}
