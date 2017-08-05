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

import java.nio.file.attribute.PosixFilePermission;
import static java.nio.file.attribute.PosixFilePermission.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;

/**
 * PosixHelp provides methods for manipulating posix permissions. Like in 
 * PosixPermission only 9 bits are supported.
 * @author tkv
 * @see java.nio.file.attribute.PosixFilePermission
 * @see java.nio.file.attribute.PosixFilePermissions
 */
public final class PosixHelp
{
    /**
     * Returns mode from String. E.g. "rwxr--r--" = 0744.
     * @param perms
     * @return 
     */
    public static int getMode(String perms)
    {
        return getMode(PosixFilePermissions.fromString(perms));
    }
    /**
     * Returns permission String. 0744 = "rwxr--r--".
     * @param mode
     * @return 
     */
    public static String toString(int mode)
    {
        return PosixFilePermissions.toString(fromMode(mode));
    }
    /**
     * Returns mode.
     * @param perms
     * @return 
     */
    public static int getMode(Set<PosixFilePermission> perms)
    {
        int mode = 0;
        for (PosixFilePermission p : perms)
        {
            switch (p)
            {
                case OTHERS_EXECUTE:
                    mode |= 1;
                    break;
                case OTHERS_WRITE:
                    mode |= 2;
                    break;
                case OTHERS_READ:
                    mode |= 4;
                    break;
                case GROUP_EXECUTE:
                    mode |= 8;
                    break;
                case GROUP_WRITE:
                    mode |= 16;
                    break;
                case GROUP_READ:
                    mode |= 32;
                    break;
                case OWNER_EXECUTE:
                    mode |= 64;
                    break;
                case OWNER_WRITE:
                    mode |= 128;
                    break;
                case OWNER_READ:
                    mode |= 256;
                    break;
                default:
                    throw new UnsupportedOperationException(p+" not supported");
            }
        }
        return mode;
    }
    /**
     * Returns permissions from mode.
     * @param mode
     * @return 
     */
    public static Set<PosixFilePermission> fromMode(int mode)
    {
        Set<PosixFilePermission> set = EnumSet.noneOf(PosixFilePermission.class);
        if ((mode & 1) != 0)
        {
            set.add(OTHERS_EXECUTE);
        }
        if ((mode & 2) != 0)
        {
            set.add(OTHERS_WRITE);
        }
        if ((mode & 4) != 0)
        {
            set.add(OTHERS_READ);
        }
        if ((mode & 8) != 0)
        {
            set.add(GROUP_EXECUTE);
        }
        if ((mode & 16) != 0)
        {
            set.add(GROUP_WRITE);
        }
        if ((mode & 32) != 0)
        {
            set.add(GROUP_READ);
        }
        if ((mode & 64) != 0)
        {
            set.add(OWNER_EXECUTE);
        }
        if ((mode & 128) != 0)
        {
            set.add(OWNER_WRITE);
        }
        if ((mode & 256) != 0)
        {
            set.add(OWNER_READ);
        }
        return set;
    }
}
