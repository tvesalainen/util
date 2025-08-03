/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.channels.vc;

import java.nio.channels.ByteChannel;
import java.nio.channels.SelectableChannel;

/**
 * VirtualCircuitFactory creates SelectableVirtualCircuit if both channels 
 * implement SelectableChannel direct or by implementing SelectableBySelector 
 * or otherwise ByteChannelVirtualCircuit.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see org.vesalainen.nio.channels.vc.SelectableBySelector
 */
public class VirtualCircuitFactory
{
    /**
     * Creates either SelectableVirtualCircuit or ByteChannelVirtualCircuit 
     * depending on channel.
     * @param ch1
     * @param ch2
     * @param capacity
     * @param direct
     * @return 
     */
    public static final VirtualCircuit create(ByteChannel ch1, ByteChannel ch2, int capacity, boolean direct)
    {
        if (
                (ch1 instanceof SelectableChannel) &&
                (ch2 instanceof SelectableChannel)
                )
        {
            SelectableChannel sc1 = (SelectableChannel) ch1;
            SelectableChannel sc2 = (SelectableChannel) ch2;
            return new SelectableVirtualCircuit(sc1, sc2, capacity, direct);
        }
        else
        {
            if (
                    (ch1 instanceof SelectableBySelector) &&
                    (ch2 instanceof SelectableBySelector)
                    )
            {
                SelectableBySelector sbs1 = (SelectableBySelector) ch1;
                SelectableBySelector sbs2 = (SelectableBySelector) ch2;
                return new SelectableVirtualCircuit(sbs1.getSelector(), sbs2.getSelector(), ch1, ch2, capacity, direct);
            }
            else
            {
                return new ByteChannelVirtualCircuit(ch1, ch2, capacity, direct);
            }
        }
    }
}
