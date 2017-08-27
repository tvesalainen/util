/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.nio.file.attribute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UserAttrsTest
{
    
    public UserAttrsTest()
    {
    }

    //@Test // needs file
    public void test() throws IOException
    {
        Path path = Paths.get("C:\\temp\\test.txt");
        UserAttrs.setShortAttribute(path, "short", (short)1234);
        assertEquals(1234, UserAttrs.getShortAttribute(path, "short"));
        UserAttrs.setIntAttribute(path, "int", 1234);
        assertEquals(1234, UserAttrs.getIntAttribute(path, "int"));
        UserAttrs.setLongAttribute(path, "long", 1234L);
        assertEquals(1234, UserAttrs.getLongAttribute(path, "long"));
        UserAttrs.setFloatAttribute(path, "float", (float)1234.56);
        assertEquals((float)1234.56, UserAttrs.getFloatAttribute(path, "float"), 1e-10);
        UserAttrs.setDoubleAttribute(path, "double", 1234.56);
        assertEquals(1234.56, UserAttrs.getDoubleAttribute(path, "double"), 1e-10);
        UserAttrs.setStringAttribute(path, "string", "qwerty");
        assertEquals("qwerty", UserAttrs.getStringAttribute(path, "string"));
    }
    
}
