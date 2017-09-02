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
package org.vesalainen.vfs.unix;

import java.nio.file.attribute.PosixFileAttributeView;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface UnixFileAttributeView extends PosixFileAttributeView
{

    @Override
    public UnixFileAttributes readAttributes();
    
    /**
     * Sets set-user-ID bit
     * @param setUserId 
     */
    void setUserId(boolean setUserId);
    /**
     * Sets set-group-ID bit
     * @param setGroupId 
     */
    void setGroupId(boolean setGroupId);
    /**
     * Sets sticky bit
     * @param stickyBit 
     */
    void stickyBit(boolean stickyBit);
    /**
     * Sets permissions, set-UID, set-GID and sticky-bit as number. E.g. 0120744
     * @param mode 
     */
    void mode(int mode);
    /**
     * Returns permissions, set-UID, set-GID and sticky-bit as String E.g.
     * lrwxr--r--. mode can be 10 or 9 long. However file type is ignored.
     * @param mode 
     */
    void mode(String mode);
    
}
