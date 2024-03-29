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
    public void test1() throws IOException, InterruptedException
    {
        ModbusTcpServer server = new ModbusTcpServer();
        server.addServer(100, new WritableByteBufferServer(64, 800));
        server.start();
        Thread.sleep(500);
        ModbusTcpClient m = ModbusTcpClient.open("localhost");
        m.setShort(100, 807, (short)123);
        int r1 = m.getShort(100, 807);
        assertEquals(123, r1);
        m.setInt(100, 843, 987);
        int r2 = m.getInt(100, 843);
        assertEquals(987, r2);
        String r4 = m.getString(100, 800, 6);
        System.err.println(r4);
    }
    
}
