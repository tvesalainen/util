/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can;

import org.vesalainen.can.DataUtil.LongBuffer;
import org.vesalainen.nio.ReadBuffer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
@FunctionalInterface
public interface Frame
{
    /**
     * @deprecated 
     * @param time
     * @param canId
     * @param dataLength
     * @param data 
     */
    default void frame(long time, int canId, int dataLength, long data)
    {
        frame(time, canId, new LongBuffer(data, dataLength));
    }
    void frame(long time, int canId, ReadBuffer data);
}
