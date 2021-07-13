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
package org.vesalainen.can.dbc;

import java.nio.ByteOrder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SignalClassTest
{
    
    public SignalClassTest()
    {
    }

    @Test
    public void testNormalizeStartBit()
    {
        assertEquals(0, SignalClass.normalizeStartBit(7, ByteOrder.BIG_ENDIAN));
        assertEquals(7, SignalClass.abnormalizeStartBit(0, ByteOrder.BIG_ENDIAN));
        
        assertEquals(16, SignalClass.normalizeStartBit(23, ByteOrder.BIG_ENDIAN));
        assertEquals(23, SignalClass.abnormalizeStartBit(16, ByteOrder.BIG_ENDIAN));
        
        assertEquals(32, SignalClass.normalizeStartBit(39, ByteOrder.BIG_ENDIAN));
        assertEquals(39, SignalClass.abnormalizeStartBit(32, ByteOrder.BIG_ENDIAN));
        
        assertEquals(40, SignalClass.normalizeStartBit(47, ByteOrder.BIG_ENDIAN));
        assertEquals(47, SignalClass.abnormalizeStartBit(40, ByteOrder.BIG_ENDIAN));
        
        assertEquals(56, SignalClass.normalizeStartBit(63, ByteOrder.BIG_ENDIAN));
        assertEquals(63, SignalClass.abnormalizeStartBit(56, ByteOrder.BIG_ENDIAN));
        
        assertEquals(20, SignalClass.normalizeStartBit(19, ByteOrder.BIG_ENDIAN));
        assertEquals(19, SignalClass.abnormalizeStartBit(20, ByteOrder.BIG_ENDIAN));
        
        assertEquals(9, SignalClass.normalizeStartBit(14, ByteOrder.BIG_ENDIAN));
        assertEquals(14, SignalClass.abnormalizeStartBit(9, ByteOrder.BIG_ENDIAN));
        
    }
    
}
