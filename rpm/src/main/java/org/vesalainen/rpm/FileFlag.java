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
package org.vesalainen.rpm;

/**
 *
 * @author tkv
 */
public enum FileFlag
{
    RPMFILE_NONE,
    /**
     * The file is a configuration file, and an existing file should be saved
     * during a package upgrade operation and not removed during a pakage
     * removal operation.
     */
    RPMFILE_CONFIG,
    /**
     * The file contains documentation.
     */
    RPMFILE_DOC,
    /**
     * This value is reserved for future use; conforming packages may not use
     * this flag.
     */
    RPMFILE_DONOTUSE,
    /**
     * The file need not exist on the installed system.
     */
    RPMFILE_MISSINGOK,
    /**
     * Similar to the RPMFILE_CONFIG, this flag indicates that during an upgrade
     * operation the original file on the system should not be altered.
     */
    RPMFILE_NOREPLACE,
    /**
     * The file is a package specification.
     */
    RPMFILE_SPECFILE,
    /**
     * The file is not actually included in the payload, but should still be
     * considered as a part of the package. For example, a log file generated by
     * the application at run time.
     */
    RPMFILE_GHOST,
    /**
     * The file contains the license conditions.
     */
    RPMFILE_LICENSE,
    /**
     * The file contains high level notes about the package.
     */
    RPMFILE_README,
    /**
     * The corresponding file is not a part of the package, and should not be
     * installed.
     */
    RPMFILE_EXCLUDE;

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
}
    