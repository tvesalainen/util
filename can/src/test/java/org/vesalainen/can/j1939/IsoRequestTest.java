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

import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.can.dbc.DBC;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IsoRequestTest
{
    
    public IsoRequestTest()
    {
        DBC.addN2K();
    }

    @Test
    public void test1()
    {
        byte[] buf = new byte[3];
        IsoRequest ir = new IsoRequest();
        ir.setPgnBeingRequested(12345);
        ir.write(buf);
        ir.read(buf);
        assertEquals(12345, ir.getPgnBeingRequested());
    }
    
}
