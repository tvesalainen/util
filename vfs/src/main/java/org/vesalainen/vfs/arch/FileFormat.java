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
package org.vesalainen.vfs.arch;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public enum FileFormat
{
    /**
     * The obsolete binary format. 070707
     */
    CPIO_BIN,
    /**
     * The old (POSIX.1) portable format. 070707
     */
    CPIO_ODC,
    /**
     * The new (SVR4) portable format. 070701
     */
    CPIO_NEWC,
    /**
     * The new (SVR4) portable format with a checksum added. 070702
     */
    CPIO_CRC,
    /**
     * GNU tar 1.13.x format
     */
    TAR_GNU,
    /**
     * GNU format as per tar <= 1.12
     */
    TAR_OLDGNU,
    /**
     * POSIX 1003.1-2001 (pax) format
     */
    TAR_PAX,
    /**
     * POSIX 1003.1-1988 (ustar) format
     */
    TAR_USTAR,
    /**
     * old V7 tar format
     */
    TAR_V7
}
