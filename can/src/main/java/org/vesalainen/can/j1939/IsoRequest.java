/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.j1939;

import java.lang.invoke.MethodHandles;
import org.vesalainen.can.AbstractMessageData;
import org.vesalainen.code.Property;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IsoRequest extends AbstractMessageData
{
    @Property(aliases={"Pgn_Being_Requested"}) int pgnBeingRequested;
    
    public IsoRequest()
    {
        this(0);
    }
    public IsoRequest(int pgn)
    {
        super(MethodHandles.lookup(), 59904);
        this.pgnBeingRequested = pgn;
    }

    public int getPgnBeingRequested()
    {
        return pgnBeingRequested;
    }

    public void setPgnBeingRequested(int pgnBeingRequested)
    {
        this.pgnBeingRequested = pgnBeingRequested;
    }
    
}
