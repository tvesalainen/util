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
package org.vesalainen.vfs.pm.rpm;

import java.util.Set;
import org.vesalainen.vfs.pm.FileUse;


/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum FileFlag
{
    NONE,
    /**
     * The file is a configuration file, and an existing file should be saved
     * during a package upgrade operation and not removed during a pakage
     * removal operation.
     */
    CONFIG,
    /**
     * The file contains documentation.
     */
    DOC,
    /**
     * This value is reserved for future use; conforming packages may not use
     * this flag.
     */
    DONOTUSE,
    /**
     * The file need not exist on the installed system.
     */
    MISSINGOK,
    /**
     * Similar to the CONFIG, this flag indicates that during an upgrade
     * operation the original file on the system should not be altered.
     */
    NOREPLACE,
    /**
     * The file is a package specification.
     */
    SPECFILE,
    /**
     * The file is not actually included in the payload, but should still be
     * considered as a part of the package. For example, a log file generated by
     * the application at run time.
     */
    GHOST,
    /**
     * The file contains the license conditions.
     */
    LICENSE,
    /**
     * The file contains high level notes about the package.
     */
    README,
    /**
     * The corresponding file is not a part of the package, and should not be
     * installed.
     */
    EXCLUDE;

    public int getFlag()
    {
        int ordinal = ordinal();
        if (ordinal > 0)
        {
            return 1 << (ordinal-1);
        }
        else
        {
            return 0;
        }
    }
    public static int or(FileUse... uses)
    {
        int flag = 0;
        for (FileUse u : uses)
        {
            switch (u)
            {
                case CONFIGURATION:
                    flag |= CONFIG.getFlag();
                    break;
                case DOCUMENTATION:
                    flag |= DOC.getFlag();
                    break;
            }
        }
        return flag;
    }
    public static int or(Set<FileUse> uses)
    {
        int flag = 0;
        for (FileUse u : uses)
        {
            switch (u)
            {
                case CONFIGURATION:
                    flag |= CONFIG.getFlag();
                    break;
                case DOCUMENTATION:
                    flag |= DOC.getFlag();
                    break;
            }
        }
        return flag;
    }
    public static FileUse[] fromFileFlags(int flags)
    {
        int len = 0;
        if ((flags & DOC.getFlag()) != 0)
        {
            len++;
        }
        if ((flags & CONFIG.getFlag()) != 0)
        {
            len++;
        }
        FileUse[] use = new FileUse[len];
        len = 0;
        if ((flags & DOC.getFlag()) != 0)
        {
            use[len++] = FileUse.DOCUMENTATION;
        }
        if ((flags & CONFIG.getFlag()) != 0)
        {
            use[len++] = FileUse.CONFIGURATION;
        }
        return use;
    }
    public static int or(FileFlag... flags)
    {
        int flag = 0;
        for (FileFlag f : flags)
        {
            flag |= f.getFlag();
        }
        return flag;
    }
    public static boolean isSet(FileFlag flag, FileFlag... flags)
    {
        for (FileFlag f : flags)
        {
            if (f == flag)
            {
                return true;
            }
        }
        return false;
    }
    public static FileFlag[] get(int flags)
    {
        FileFlag[] arr = new FileFlag[Integer.bitCount(flags)];
        int idx = 0;
        for (FileFlag ff : FileFlag.values())
        {
            if ((flags & ff.getFlag()) != 0)
            {
                arr[idx++] = ff;
            }
        }
        return arr;
    }
}
    