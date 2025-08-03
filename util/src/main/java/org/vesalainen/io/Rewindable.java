/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.io;

import java.io.IOException;

/**
 * Rewindable input is capable to rewind input number of units. Implementations
 * are faster than PushbackX classes, because there's no need for unread buffer.
 * Implementations must have some kind of read buffer, so they are also buffering.
 * @author Timo Vesalainen
 * @see java.io.PushbackInputStream
 * @see java.io.PushbackReader
 */
public interface Rewindable
{
    /**
     * Rewinds input count units.
     * @param count 
     */
   void rewind(int count) throws IOException; 
}
