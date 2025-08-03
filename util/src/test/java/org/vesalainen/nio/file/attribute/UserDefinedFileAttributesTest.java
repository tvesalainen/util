/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UserDefinedFileAttributesTest
{
    private Path temp;
    public UserDefinedFileAttributesTest()
    {
    }

    @Before
    public void init() throws IOException
    {
        temp = Files.createTempFile("test", null);
    }
    @After
    public void cleanup() throws IOException
    {
        Files.deleteIfExists(temp);
    }
    @Test
    public void test0() throws IOException, ClassNotFoundException
    {
        UserDefinedFileAttributes udfa = new UserDefinedFileAttributes(temp, 100);
        String exp = "Kärkkäinen";
        udfa.setString("test", exp);
        assertEquals(exp, udfa.getString("test"));
        
        udfa.setBoolean("boolean", true);
        assertTrue(udfa.getBoolean("boolean"));
        
        udfa.setInt("int", 1234);
        assertEquals(1234, udfa.getInt("int"));
        
        udfa.setLong("long", 12345678L);
        assertEquals(12345678L, udfa.getLong("long"));
        
        udfa.setDouble("double", 1234.56789);
        assertEquals(1234.56789, udfa.getDouble("double"), 1e-10);
    }
    
    @Test
    public void test1() throws IOException, ClassNotFoundException
    {
        UserDefinedFileAttributes udfa = new UserDefinedFileAttributes(temp, 100);
        for (int ii=0;ii<10;ii++)
        {
            udfa.setInt("int", ii);
            assertEquals(ii, udfa.getInt("int"));
        }
        
    }
}
