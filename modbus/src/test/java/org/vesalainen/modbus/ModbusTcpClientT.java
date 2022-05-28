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
package org.vesalainen.modbus;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ModbusTcpClientT
{
    
    public ModbusTcpClientT()
    {
    }

    //@Test
    public void testMcp342x() throws IOException
    {
        int out = 2;
        int pga = 1;
        int bits = 18;
        int unit = 0;
        int addr = out<<9|pga<<7|(bits/2-6)<<5;
        short v = 1;
        ModbusTcpClient cli = ModbusTcpClient.open("testipi");
        int res = cli.getInt(unit, addr);
        assertEquals(3325000, res);
    }
    @Test
    public void test1() throws IOException
    {
        int i = 0;
        int unit = 4;
        int addr = 3422;
        short v = 1;
        ModbusTcpClient cli = ModbusTcpClient.open("cerbo");
        int res = cli.getShort(unit, addr);
        System.err.println(res);
    }
}
