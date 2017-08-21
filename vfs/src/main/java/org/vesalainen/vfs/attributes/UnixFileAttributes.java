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

import java.nio.file.attribute.PosixFileAttributes;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface UnixFileAttributes extends PosixFileAttributes
{
    /**
     * Returns ID of device containing file
     * @return 
     */
    int device();
    /**
     * Returns inode number
     * @return 
     */
    int inode();
    /**
     * Returns set-user-ID bit
     * @return 
     */
    boolean setUserId();
    /**
     * Sets set-user-ID bit
     * @param setUserId 
     */
    void setUserId(boolean setUserId);
    /**
     * Returns set-group-ID bit
     * @return 
     */
    boolean setGroupId();
    /**
     * Sets set-group-ID bit
     * @param setGroupId 
     */
    void setGroupId(boolean setGroupId);
    /**
     * Returns sticky bit
     * @return 
     */
    boolean stickyBit();
    /**
     * Sets sticky bit
     * @param stickyBit 
     */
    void stickyBit(boolean stickyBit);
    /**
     * Sets permissions, set-UID, set-GID and sticky-bit as number. E.g. 0120744
     * @return 
     */
    short mode();
    /**
     * Returns permissions, set-UID, set-GID and sticky-bit as number. E.g. 0120744
     * @param mode 
     */
    void mode(short mode);
    /**
     * Sets permissions, set-UID, set-GID and sticky-bit as String E.g.
     * lrwxr--r--
     * @return 
     */
    String modeString();
    /**
     * Returns permissions, set-UID, set-GID and sticky-bit as String E.g.
     * lrwxr--r--
     * @param mode 
     */
    void mode(String mode);
    
}
