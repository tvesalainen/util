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
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PosixFileAttributeViewImpl extends BasicFileAttributeViewImpl implements PosixFileAttributeView
{
    private PosixFileAttributes posixFileAttributes = new PosixFileAttributesImpl();
    
    protected PosixFileAttributeViewImpl(String name, FileAttributeAccess access)
    {
        super(name, access);
    }

    public PosixFileAttributeViewImpl(FileAttributeAccess access)
    {
        super("posix", access);
    }

    @Override
    public PosixFileAttributes readAttributes() throws IOException
    {
        return posixFileAttributes;
    }

    @Override
    public void setPermissions(Set<PosixFilePermission> perms) throws IOException
    {
        put(PERMISSIONS, perms);
    }

    @Override
    public void setGroup(GroupPrincipal group) throws IOException
    {
        put(GROUP, group);
    }
    private class PosixFileAttributesImpl extends BasicFileAttributesImpl implements PosixFileAttributes
    {

        @Override
        public UserPrincipal owner()
        {
            return (UserPrincipal) get(OWNER);
        }

        @Override
        public GroupPrincipal group()
        {
            return (GroupPrincipal) get(GROUP);
        }

        @Override
        public Set<PosixFilePermission> permissions()
        {
            return (Set<PosixFilePermission>) get(PERMISSIONS);
        }
        
    }
}
