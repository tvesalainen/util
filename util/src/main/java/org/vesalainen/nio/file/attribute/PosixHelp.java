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
package org.vesalainen.nio.file.attribute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.vesalainen.util.OperatingSystem;
import static org.vesalainen.util.OperatingSystem.Linux;

/**
 * PosixHelp provides methods for manipulating posix permissions. Supported are:
 * File types sl-bdcp, set-uid, set-gid and sticky bit.
 * @author tkv
 */
public final class PosixHelp
{
    /**
     * Creates regular file or directory
     * @param path
     * @param perms E.g. -rwxr--r--
     * @return
     * @throws IOException 
     */
    public static Path create(Path path, String perms) throws IOException
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
    public static Path create(Path path, Path target, String perms) throws IOException
    {
        if (perms.length() != 10)
        {
            throw new IllegalArgumentException(perms+" not permission. E.g. -rwxr--r--");
        }
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
     * If OS is not Linux returns empty array;
     * @param perms E.g. -rwxr--r--
     * @return 
     */
    public static FileAttribute<?>[] getFileAttributes(String perms)
    {
        if (perms.length() != 10)
        {
            throw new IllegalArgumentException(perms+" not permission. E.g. -rwxr--r--");
        }
        if (OperatingSystem.is(Linux))
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
    public static short getMode(CharSequence perms)
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
    public static String toString(short mode)
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
}
