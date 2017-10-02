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
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.vesalainen.vfs.arch.FileFormat;
import static org.vesalainen.vfs.arch.FileFormat.*;
import org.vesalainen.vfs.attributes.FileAttributeImpl;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;
import org.vesalainen.vfs.unix.UnixGroup;
import org.vesalainen.vfs.unix.UnixUser;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class Env
{
    /**
     * Options for file as Set<? extends OpenOption>
     *
     * @see java.nio.channels.FileChannel#open(java.nio.file.Path,
     * java.util.Set, java.nio.file.attribute.FileAttribute...)
     */
    public static final String OPEN_OPTIONS = "openOptions";
    /**
     * Attributes for new archive file as FileAttribute<?>[]
     *
     * @see java.nio.channels.FileChannel#open(java.nio.file.Path,
     * java.util.Set, java.nio.file.attribute.FileAttribute...)
     */
    public static final String FILE_ATTRIBUTES = "fileAttributes";
    /**
     * Default attributes for regular virtual file.
     */
    public static final String DEFAULT_REGULAR_FILE_ATTRIBUTES = "defaultRegularFileAttributes";
    /**
     * Default attributes for directory virtual file.
     */
    public static final String DEFAULT_DIRECTORY_FILE_ATTRIBUTES = "defaultRegularDirectoryAttributes";
    /**
     * Default attributes for symbolic link virtual file.
     */
    public static final String DEFAULT_SYMBOLIC_LINK_FILE_ATTRIBUTES = "defaultRegularSymbolicLinkAttributes";
    /**
     * Format of created file.
     *
     * @see org.vesalainen.vfs.arch.FileFormat
     */
    public static final String FORMAT = "format";
    /**
     * Returns FileFormat either from env or using default value which is TAR_GNU
     * for tar and CPIO_CRC for others.
     * @param path
     * @param env
     * @return 
     */
    public static final FileFormat getFileFormat(Path path, Map<String, ?> env)
    {
        FileFormat fmt = (FileFormat) env.get(FORMAT);
        String filename = path.getFileName().toString();
        if (fmt == null)
        {
            if (filename.endsWith(".tar.gz") || filename.endsWith(".tar") || filename.endsWith(".deb"))
            {
                fmt = TAR_GNU;
            }
            else
            {
                fmt = CPIO_CRC;
            }
        }
        return fmt;
    }
    /**
     * Return file attributes from env or empty array. These attributes are used
     * in creating archive file. Because of zip-file-system crashes if trying to
     * open file system for not existing file.
     * @param env
     * @return 
     */
    public static final FileAttribute<?>[] getFileAttributes(Map<String, ?> env)
    {
        FileAttribute<?>[] attrs = (FileAttribute<?>[]) env.get(FILE_ATTRIBUTES);
        if (attrs == null)
        {
            attrs = new FileAttribute<?>[0];
        }
        return attrs;
    }
    /**
     * Return default attributes for new regular file. Either from env or default
     * values.
     * @param env
     * @return 
     */
    public static final FileAttribute<?>[] getDefaultRegularFileAttributes(Map<String, ?> env)
    {
        FileAttribute<?>[] attrs = (FileAttribute<?>[]) env.get(DEFAULT_REGULAR_FILE_ATTRIBUTES);
        if (attrs == null)
        {
            attrs = new FileAttribute<?>[]{
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--")),
                new FileAttributeImpl(OWNER, new UnixUser("root", 0)),
                new FileAttributeImpl(GROUP, new UnixGroup("root", 0)),
            };
        }
        return attrs;
    }
    /**
     * Return default attributes for new directory. Either from env or default
     * values.
     * @param env
     * @return 
     */
    public static final FileAttribute<?>[] getDefaultDirectoryFileAttributes(Map<String, ?> env)
    {
        FileAttribute<?>[] attrs = (FileAttribute<?>[]) env.get(DEFAULT_DIRECTORY_FILE_ATTRIBUTES);
        if (attrs == null)
        {
            attrs = new FileAttribute<?>[]{
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")),
                new FileAttributeImpl(OWNER, new UnixUser("root", 0)),
                new FileAttributeImpl(GROUP, new UnixGroup("root", 0)),
            };
        }
        return attrs;
    }
    /**
     * Return default attributes for new symbolic link. Either from env or default
     * values.
     * @param env
     * @return 
     */
    public static final FileAttribute<?>[] getDefaultSymbolicLinkFileAttributes(Map<String, ?> env)
    {
        FileAttribute<?>[] attrs = (FileAttribute<?>[]) env.get(DEFAULT_SYMBOLIC_LINK_FILE_ATTRIBUTES);
        if (attrs == null)
        {
            attrs = new FileAttribute<?>[]{
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")),
                new FileAttributeImpl(OWNER, new UnixUser("root", 0)),
                new FileAttributeImpl(GROUP, new UnixGroup("root", 0)),
            };
        }
        return attrs;
    }
    /**
     * Returns open-options. Returns from env if exists. Otherwise if file exists
     * and size greater than 0 returns READ or READ,WRITE.
     * @param path
     * @param env
     * @return
     * @throws IOException 
     */
    public static final Set<? extends OpenOption> getOpenOptions(Path path, Map<String, ?> env) throws IOException
    {
        Set<? extends OpenOption> opts = (Set<? extends OpenOption>) env.get(OPEN_OPTIONS);
        if (opts == null)
        {
            if (Files.exists(path) && Files.size(path) > 0)
            {
                opts = EnumSet.of(READ);
            }
            else
            {
                opts = EnumSet.of(WRITE, CREATE);
            }
        }
        return opts;
    }
}
