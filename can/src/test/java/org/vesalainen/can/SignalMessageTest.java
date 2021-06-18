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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SignalMessageTest
{
    
    public SignalMessageTest()
    {
    }

    @Test
    public void testGetLong8()
    {
        assertEquals(0x1234567890L, SignalMessage.getLong8(0, 40, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x345678L, SignalMessage.getLong8(8, 24, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x567890L, SignalMessage.getLong8(16, 24, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x12L, SignalMessage.getLong8(0, 8, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
    }
    @Test
    public void testGetLong1()
    {
        assertEquals(0x1234567890L, SignalMessage.getLong1(0, 40, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x345678L, SignalMessage.getLong1(8, 24, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x567890L, SignalMessage.getLong1(16, 24, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x12L, SignalMessage.getLong1(0, 8, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x3L, SignalMessage.getLong1(8, 4, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
        assertEquals(0x45L, SignalMessage.getLong1(12, 8, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90L));
    }
    @Test
    public void testChangeEndian()
    {
        assertEquals(0x1234567890L, SignalMessage.changeEndian(0x9078563412L));
        assertEquals(0x1234, SignalMessage.changeEndian(0x3412));
        assertEquals(0xff, SignalMessage.changeEndian(0xff));
    }
    @Test
    public void testSigned()
    {
        assertEquals(-1, SignalMessage.signed(0xff, 8));
        assertEquals(1, SignalMessage.signed(0x1, 8));
        assertEquals(-1, SignalMessage.signed(0xffff, 16));
        assertEquals(1, SignalMessage.signed(0x1, 16));
        assertEquals(-1, SignalMessage.signed(0xffffffff, 32));
        assertEquals(1, SignalMessage.signed(1, 32));
    }    
}
