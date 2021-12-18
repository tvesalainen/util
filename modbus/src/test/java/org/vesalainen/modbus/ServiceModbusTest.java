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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ServiceModbusTest
{
    
    public ServiceModbusTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        ModbusTcp m = ModbusTcp.open("192.168.214.222");
        VictronRegistry vr = new VictronRegistry();
        for (String s : new String[]{
            "com.victronenergy.system", 
            "com.victronenergy.vebus", 
            "com.victronenergy.battery", 
            "com.victronenergy.solarcharger", 
            "com.victronenergy.gps"
        })
        {
            int unitId = 0;
            switch (s)
            {
                case "com.victronenergy.system":
                    unitId = 100;
                    break;
                case "com.victronenergy.vebus":
                    unitId = 227;
                    break;
                case "com.victronenergy.battery": 
                    unitId = 225;
                    break;
                case "com.victronenergy.solarcharger":
                    unitId = 226;
                    break;
                case "com.victronenergy.gps":
                    unitId = 100;
                    break;
            }
            System.err.println(s);
            System.err.println();
            Service system = vr.getService(s);
            ServiceModbus sm = new ServiceModbus(unitId, m, system);
            sm.dump(System.err);
        }
    }
    
}
