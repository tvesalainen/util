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
package org.vesalainen.modbus;

import java.io.IOException;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ModbusTcpTest
{
    
    public ModbusTcpTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.ALL);
    }

    @Test
    public void test1() throws IOException
    {
        ModbusTcp m = ModbusTcp.open("192.168.214.222");
        m.setShort(100, 807, (short)0);
        m.getShort(100, 807, (s)->System.err.println(s));
        int r1 = m.getShort(100, 806);
        System.err.println(r1);
        int r2 = m.getShort(100, 843);
        System.err.println(r2);
        m.getInt(100, 2802, (s)->System.err.println(s));
        int r3 = m.getInt(100, 2800);
        System.err.println(r3);
        m.getString(100, 800, 6, (s)->System.err.println(s));
        String r4 = m.getString(100, 800, 6);
        System.err.println(r4);
        long r5 = m.getUnsignedInt(100, 3420);
        System.err.println(r5);
    }
    
}
