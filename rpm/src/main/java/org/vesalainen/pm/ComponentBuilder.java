/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.pm;

import java.io.IOException;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.time.Instant;
import java.util.Set;
import org.vesalainen.nio.file.attribute.PosixHelp;
import org.vesalainen.pm.rpm.FileFlag;

/**
 *
 * @author tkv
 */
public interface ComponentBuilder
{
    /**
     * Set flags for file. 
     * @param flags
     * @return 
     */
    ComponentBuilder setFlag(FileFlag... flags);
    /**
     * Sets posix group name of file.
     * @param groupname
     * @return
     * @throws IOException 
     */
    default ComponentBuilder setGroup(String groupname) throws IOException
    {
        FileAttribute<GroupPrincipal> group = PosixHelp.getGroupAsAttribute(groupname);
        if (group != null)
        {
            addFileAttributes(group);
        }
        return this;
    }
    /**
     * Set posix permissions  in rwxrwxrwx format. rwxr--r-- = 0744
     * @param perms
     * @return
     */
    default ComponentBuilder setPermissions(String perms)
    {
        if (PosixHelp.supports("posix"))
        {
            Set<PosixFilePermission> posixPerms = PosixFilePermissions.fromString(perms);
            FileAttribute<Set<PosixFilePermission>> fa = PosixFilePermissions.asFileAttribute(posixPerms);
            addFileAttributes(fa);
        }
        return this;
    }
    /**
     * Add file attribute. Default implementaions setGroup, setOwner, setPermissions, 
 setLastModifiedTime, setLastAccessTime and setCreationTime calls this
 method.
     * @param attrs
     * @return 
     */
    ComponentBuilder addFileAttributes(FileAttribute<?>... attrs);

    /**
     * Sets files last modified time.
     * @param time
     * @return
     */
    default ComponentBuilder setLastModifiedTime(Instant time)
    {
        addFileAttributes(PosixHelp.getLastModifiedTimeAsAttribute(FileTime.from(time)));
        return this;
    }
    /**
     * Sets files last access time.
     * @param time
     * @return 
     */
    default ComponentBuilder setLastAccessTime(Instant time)
    {
        addFileAttributes(PosixHelp.getLastAccessTimeAsAttribute(FileTime.from(time)));
        return this;
    }
    /**
     * Sets files creation time.
     * @param time
     * @return 
     */
    default ComponentBuilder setCreationTime(Instant time)
    {
        addFileAttributes(PosixHelp.getCreationTimeAsAttribute(FileTime.from(time)));
        return this;
    }
    /**
     * Sets file posix owner.
     * @param name
     * @return
     * @throws IOException 
     */
    default ComponentBuilder setOwner(String name) throws IOException
    {
        FileAttribute<UserPrincipal> owner = PosixHelp.getOwnerAsAttribute(name);
        if (owner != null)
        {
            addFileAttributes(owner);
        }
        return this;
    }
    /**
     * Set copyright
     * @param copyright
     * @return 
     */
    ComponentBuilder setCopyright(String copyright);
    /**
     * Set license.
     * @param license
     * @return 
     */
    ComponentBuilder setLicense(String license);
    
}
