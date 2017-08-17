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
package org.vesalainen.nio.file.attribute;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
import org.vesalainen.util.logging.JavaLogging;

/**
 * PosixHelp provides methods for manipulating posix permissions. Supported are:
 * File types sl-bdcp, set-uid, set-gid and sticky bit.
 * <p>
 * Most methods accept string with file type as first letter. E.g. drwxrwxrwx.
 * Some methods accept also shorter permission part rwxrwxrwx.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public final class PosixHelp
{
    public static final int FILE_TYPE_MASK = 0170000;   //bit mask for the file type bit fields
    public static final int BITS_MASK = 07000;   //bit mask for the seu-uid, set-gid and stick bits.
    public static final int PERMS_MASK = 0777;   //bit mask for the permissions bits.
    
    public static final boolean isRegularFile(String perms)
    {
        return isFileType(perms, '-');
    }
    public static final boolean isRegularFile(short mode)
    {
        return isFileType(mode, '-');
    }
    public static final boolean isDirectory(String perms)
    {
        return isFileType(perms, 'd');
    }
    public static final boolean isDirectory(short mode)
    {
        return isFileType(mode, 'd');
    }
    public static final boolean isSymbolicLink(String perms)
    {
        return isFileType(perms, 'l');
    }
    public static final boolean isSymbolicLink(short mode)
    {
        return isFileType(mode, 'l');
    }
    private static boolean isFileType(short mode, char type)
    {
        return isFileType(toString(mode), type);
    }
    private static boolean isFileType(String perms, char type)
    {
        if (perms.length() != 10)
        {
            throw new IllegalArgumentException(perms+" not permission. E.g. -rwxr--r--");
        }
        return perms.charAt(0) == type;
    }
    /**
     * Return linux file owner name. Or ""
     * @param file
     * @return
     * @throws IOException 
     */
    public static final String getOwner(Path file) throws IOException
    {
        if (supports("posix"))
        {
            UserPrincipal owner = (UserPrincipal) Files.getAttribute(file, "posix:owner");
            return owner.getName();
        }
        else
        {
            JavaLogging.getLogger(PosixHelp.class).warning("no owner support. getOwner(%s) returns \"\"", file);
            return "";
        }
    }
    /**
     * Return linux file group name. Or ""
     * @param file
     * @return
     * @throws IOException 
     */
    public static final String getGroup(Path file) throws IOException
    {
        if (supports("posix"))
        {
            PosixFileAttributeView view = Files.getFileAttributeView(file, PosixFileAttributeView.class);
            PosixFileAttributes attrs = view.readAttributes();
            GroupPrincipal group = attrs.group();
            return group.getName();
        }
        else
        {
            JavaLogging.getLogger(PosixHelp.class).warning("no posix support. getGroup(%s) returns \"\"", file);
            return "";
        }
    }
    /**
     * Returns permission part of mode string
     * @param perms
     * @return 
     */
    private static String permsPart(String perms)
    {
        int length = perms.length();
        if (length > 10 || length < 9)
        {
            throw new IllegalArgumentException(perms+" not permission. E.g. -rwxr--r--");
        }
        return length == 10 ? perms.substring(1) : perms;
    }
    /**
     * Returns  mode string for path. Real mode for linux. Others guess work.
     * @param path
     * @return
     * @throws IOException 
     */
    public static final String getModeString(Path path) throws IOException
    {
        char letter = getFileTypeLetter(path);
        if (supports("posix"))
        {
            Set<PosixFilePermission> perms = Files.getPosixFilePermissions(path);
            return letter+PosixFilePermissions.toString(perms);
        }
        else
        {
            switch (letter)
            {
                case '-':
                    return "-rw-r--r--";
                case 'd':
                    return "drwxr-xr-x";
                case 'l':
                    return "drw-r--r--";
            }
        }
        throw new UnsupportedOperationException("should not happen");
    }
    /**
     * Change group of given files. Works only in linux
     * @param name
     * @param files
     * @throws IOException 
     * @see org.vesalainen.util.OSProcess#call(java.lang.String...) 
     */
    public static final void setGroup(String name, Path... files) throws IOException
    {
        if (supports("posix"))
        {
            for (Path file : files)
            {
                FileSystem fs = file.getFileSystem();
                UserPrincipalLookupService upls = fs.getUserPrincipalLookupService();
                GroupPrincipal group = upls.lookupPrincipalByGroupName(name);
                PosixFileAttributeView view = Files.getFileAttributeView(file, PosixFileAttributeView.class);
                view.setGroup(group);
            }
        }
        else
        {
            JavaLogging.getLogger(PosixHelp.class).warning("no posix support. setGroup(%s)", name);
        }
    }
    /**
     * Change user of given files. Works only in linux.
     * @param name
     * @param files
     * @throws IOException 
     * @see java.nio.file.Files#setOwner(java.nio.file.Path, java.nio.file.attribute.UserPrincipal) 
     */
    public static final void setOwner(String name, Path... files) throws IOException
    {
        if (supports("posix"))
        {
            for (Path file : files)
            {
                FileSystem fs = file.getFileSystem();
                UserPrincipalLookupService upls = fs.getUserPrincipalLookupService();
                UserPrincipal user = upls.lookupPrincipalByName(name);
                Files.setOwner(file, user);
            }
        }
        else
        {
            JavaLogging.getLogger(PosixHelp.class).warning("no owner support. setOwner(%s)", name);
        }
    }
    /**
     * Set posix permissions if supported.
     * @param path
     * @param perms 10 or 9 length E.g. -rwxr--r--
     * @throws java.io.IOException
     */
    public static final void setPermission(Path path, String perms) throws IOException
    {
        checkFileType(path, perms);
        if (supports("posix"))
        {
            Set<PosixFilePermission> posixPerms = PosixFilePermissions.fromString(permsPart(perms));
            Files.setPosixFilePermissions(path, posixPerms);
        }
        else
        {
            JavaLogging.getLogger(PosixHelp.class).warning("no posix support. setPermission(%s, %s)", path, perms);
        }
    }
    /**
     * Throws IllegalArgumentException if perms length != 10 or files type doesn't
     * match with permission.
     * @param path
     * @param perms 
     */
    public static final void checkFileType(Path path, String perms)
    {
        if (perms.length() != 10)
        {
            throw new IllegalArgumentException(perms+" not permission. E.g. -rwxr--r--");
        }
        switch (perms.charAt(0))
        {
            case '-':
                if (!Files.isRegularFile(path))
                {
                    throw new IllegalArgumentException("file is not regular file");
                }
                break;
            case 'd':
                if (!Files.isDirectory(path))
                {
                    throw new IllegalArgumentException("file is not directory");
                }
                break;
            case 'l':
                if (!Files.isSymbolicLink(path))
                {
                    throw new IllegalArgumentException("file is not symbolic link");
                }
                break;
            default:
                throw new UnsupportedOperationException(perms+" not supported");
        }
    }
    /**
     * Returns '-' for regular file, 'd' for directory or 'l' for symbolic link.
     * @param path
     * @return 
     */
    public static final char getFileTypeLetter(Path path)
    {
        if (Files.isRegularFile(path))
        {
            return '-';
        }
        if (Files.isDirectory(path))
        {
            return 'd';
        }
        if (Files.isSymbolicLink(path))
        {
            return 'l';
        }
        throw new IllegalArgumentException(path+" is either regular file, directory or symbolic link");
    }
    /**
     * Creates regular file or directory
     * @param path
     * @param perms E.g. -rwxr--r--
     * @return
     * @throws IOException 
     */
    public static final Path create(Path path, String perms) throws IOException
    {
        return create(path, null, perms);
    }
    /**
     * Creates regular file, directory or symbolic link
     * @param path
     * @param target Can be null
     * @param perms E.g. -rwxr--r--
     * @return
     * @throws IOException 
     */
    public static final Path create(Path path, Path target, String perms) throws IOException
    {
        FileAttribute<?>[] attrs = getFileAttributes(perms);
        switch (perms.charAt(0))
        {
            case '-':
                return Files.createFile(path, attrs);
            case 'l':
                if (target == null)
                {
                    throw new IllegalArgumentException("no target");
                }
                return Files.createSymbolicLink(path, target, attrs);
            case 'd':
                return Files.createDirectories(path, attrs);
            default:
                throw new IllegalArgumentException(perms+" illegal to this method");
        }
    }
    /**
     * Returns PosixFileAttributes for perms
     * <p>
     * If posix is not supported returns empty array;
     * @param perms E.g. -rwxr--r--
     * @return 
     */
    public static final FileAttribute<?>[] getFileAttributes(String perms)
    {
        if (perms.length() != 10)
        {
            throw new IllegalArgumentException(perms+" not permission. E.g. -rwxr--r--");
        }
        if (supports("posix"))
        {
            Set<PosixFilePermission> posixPerms = PosixFilePermissions.fromString(perms.substring(1));
            FileAttribute<Set<PosixFilePermission>> attrs = PosixFilePermissions.asFileAttribute(posixPerms);
            return new FileAttribute<?>[]{attrs};
        }
        else
        {
            return new FileAttribute<?>[]{};
        }
    }
    /**
     * Returns mode from String. E.g. "-rwxr--r--" = 0100744.
     * @param perms
     * @return 
     */
    public static final short getMode(CharSequence perms)
    {
        if (perms.length() != 10)
        {
            throw new IllegalArgumentException(perms+" not permission");
        }
        short mode = 0;
        for (int ii=0;ii<10;ii++)
        {
            int shift = 9-ii;
            switch (perms.charAt(ii))
            {
                case 'c':   // character device
                    if (ii != 0)
                    {
                        throw new IllegalArgumentException(perms+" illegal");
                    }
                    mode |= 0020000;
                    break;
                case 'b':   // block device
                    if (ii != 0)
                    {
                        throw new IllegalArgumentException(perms+" illegal");
                    }
                    mode |= 0060000;
                    break;
                case 'p':   // FIFO (pipe)
                    if (ii != 0)
                    {
                        throw new IllegalArgumentException(perms+" illegal");
                    }
                    mode |= 0010000;
                    break;
                case 'l':
                    if (ii != 0)
                    {
                        throw new IllegalArgumentException(perms+" illegal");
                    }
                    mode |= 0120000;
                    break;
                case 'd':
                    if (ii != 0)
                    {
                        throw new IllegalArgumentException(perms+" illegal");
                    }
                    mode |= 0040000;
                    break;
                case '-':
                    if (ii == 0)
                    {
                        mode |= 0100000;
                    }
                    break;
                case 'r':
                    switch (ii)
                    {
                        case 7:
                        case 4:
                        case 1:
                            mode |= (1<<shift);
                            break;
                        default:
                            throw new IllegalArgumentException(perms+" illegal");
                    }
                    break;
                case 'w':
                    switch (ii)
                    {
                        case 8:
                        case 5:
                        case 2:
                            mode |= (1<<shift);
                            break;
                        default:
                            throw new IllegalArgumentException(perms+" illegal");
                    }
                    break;
                case 'x':
                    switch (ii)
                    {
                        case 9:
                        case 6:
                        case 3:
                            mode |= (1<<shift);
                            break;
                        default:
                            throw new IllegalArgumentException(perms+" illegal");
                    }
                    break;
                case 's':
                    switch (ii)
                    {
                        case 0:
                            mode |= 0140000;
                            break;
                        case 6:
                            mode |= 0002000;
                            mode |= (1<<shift);
                            break;
                        case 3:
                            mode |= 0004000;
                            mode |= (1<<shift);
                            break;
                        default:
                            throw new IllegalArgumentException(perms+" illegal");
                    }
                    break;
                case 't':
                    switch (ii)
                    {
                        case 9:
                            mode |= 0001001;
                            break;
                        default:
                            throw new IllegalArgumentException(perms+" illegal");
                    }
                    break;
                case 'T':
                    switch (ii)
                    {
                        case 9:
                            mode |= 0001000;
                            break;
                        default:
                            throw new IllegalArgumentException(perms+" illegal");
                    }
                    break;
            }
        }
        return mode;
    }
    /**
     * Returns permission String. 0100744 = "-rwxr--r--".
     * @param mode
     * @return 
     */
    public static final String toString(short mode)
    {
        StringBuilder sb = new StringBuilder();
        switch (mode & 0170000)
        {
            case 0140000:
                sb.append('s');
                break;
            case 0120000:
                sb.append('l');
                break;
            case 0100000:
                sb.append('-');
                break;
            case 0060000:
                sb.append('b');
                break;
            case 0040000:
                sb.append('d');
                break;
            case 0020000:
                sb.append('c');
                break;
            case 0010000:
                sb.append('p');
                break;
            default:
                throw new IllegalArgumentException(mode+" illegal mode");
        }
        // owner
        if ((mode & 00400)==00400)
        {
            sb.append('r');
        }
        else
        {
            sb.append('-');
        }
        if ((mode & 00200)==00200)
        {
            sb.append('w');
        }
        else
        {
            sb.append('-');
        }
        if ((mode & 00100)==00100)
        {
            if ((mode & 0004000)==0004000)
            {
                sb.append('s');
            }
            else
            {
                sb.append('x');
            }
        }
        else
        {
            sb.append('-');
        }
        // group
        if ((mode & 0040)==0040)
        {
            sb.append('r');
        }
        else
        {
            sb.append('-');
        }
        if ((mode & 0020)==0020)
        {
            sb.append('w');
        }
        else
        {
            sb.append('-');
        }
        if ((mode & 0010)==0010)
        {
            if ((mode & 0002000)==0002000)
            {
                sb.append('s');
            }
            else
            {
                sb.append('x');
            }
        }
        else
        {
            sb.append('-');
        }
        // others
        if ((mode & 004)==004)
        {
            sb.append('r');
        }
        else
        {
            sb.append('-');
        }
        if ((mode & 002)==002)
        {
            sb.append('w');
        }
        else
        {
            sb.append('-');
        }
        if ((mode & 001)==001)
        {
            if ((mode & 0001000)==0001000)
            {
                sb.append('t');
            }
            else
            {
                sb.append('x');
            }
        }
        else
        {
            if ((mode & 0001000)==0001000)
            {
                sb.append('T');
            }
            else
            {
                sb.append('-');
            }
        }
        return sb.toString();
    }
    /**
     * Return true if default file system supports given view 
     * @param view
     * @return 
     */
    public static final boolean supports(String view)
    {
        Set<String> supportedFileAttributeViews = FileSystems.getDefault().supportedFileAttributeViews();
        return supportedFileAttributeViews.contains(view);
    }
    /**
     * Return file owner as attribute if posix supported. Otherwise returns null.
     * @param owner
     * @return
     * @throws IOException 
     */
    public static final FileAttribute<UserPrincipal> getOwnerAsAttribute(String owner) throws IOException
    {
        if (supports("posix"))
        {
            UserPrincipalLookupService upls = FileSystems.getDefault().getUserPrincipalLookupService();
            UserPrincipal user = upls.lookupPrincipalByName(owner);
            return new FileAttributeImpl<>("posix:owner", user);
        }
        else
        {
            return null;
        }
    }
    /**
     * Return file group as attribute if posix supported. Otherwise returns null.
     * @param group
     * @return
     * @throws IOException 
     */
    public static final FileAttribute<GroupPrincipal> getGroupAsAttribute(String group) throws IOException
    {
        if (supports("posix"))
        {
            UserPrincipalLookupService upls = FileSystems.getDefault().getUserPrincipalLookupService();
            GroupPrincipal grp = upls.lookupPrincipalByGroupName(group);
            return new FileAttributeImpl<>("posix:group", grp);
        }
        else
        {
            return null;
        }
    }
    /**
     * Returns files last modified time as attribute.
     * @param time
     * @return 
     */
    public static final FileAttribute<FileTime> getLastModifiedTimeAsAttribute(FileTime time)
    {
        return new FileAttributeImpl<>("lastModifiedTime", time);
    }
    /**
     * Returns files last access time as attribute.
     * @param time
     * @return 
     */
    public static final FileAttribute<FileTime> getLastAccessTimeAsAttribute(FileTime time)
    {
        return new FileAttributeImpl<>("lastAccessTime", time);
    }
    /**
     * Returns files creation time as attribute.
     * @param time
     * @return 
     */
    public static final FileAttribute<FileTime> getCreationTimeAsAttribute(FileTime time)
    {
        return new FileAttributeImpl<>("lastModifiedTime", time);
    }
    public static class FileAttributeImpl<T> implements FileAttribute<T>
    {
        private String name;
        private T value;

        public FileAttributeImpl(String name, T value)
        {
            this.name = name;
            this.value = value;
        }
        
        @Override
        public String name()
        {
            return name;
        }

        @Override
        public T value()
        {
            return value;
        }
        
    }
}
