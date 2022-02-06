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
import org.vesalainen.can.CanSource;
import org.vesalainen.can.SimpleCanSource;
import org.vesalainen.can.dbc.DBC;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ProductInformationTest
{
    
    public ProductInformationTest()
    {
        DBC.addN2K();
    }

    @Test
    public void test()
    {
        CanSource buf = new SimpleCanSource(134);
        ProductInformation p1 = new ProductInformation();
        p1.setLoadEquivalency(3);
        p1.setManufacturerSModelId("model");
        p1.setManufacturerSModelSerialCode("123");
        p1.setManufacturerSModelVersion("v0");
        p1.setManufacturerSSoftwareVersionCode("v0.0");
        p1.write(buf);
        ProductInformation p2 = new ProductInformation();
        p2.read(buf);
        assertEquals(3, p2.getLoadEquivalency());
        assertEquals("model", p2.getManufacturerSModelId());
        assertEquals("123", p2.getManufacturerSModelSerialCode());
        assertEquals("v0", p2.getManufacturerSModelVersion());
        assertEquals("v0.0", p2.getManufacturerSSoftwareVersionCode());
    }
    
}
