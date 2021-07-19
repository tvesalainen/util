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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCFileTest
{
    
    public DBCFileTest()
    {
    }

    @Test
    public void testExample() throws IOException
    {
        DBCFile dbcFile1 = new DBCFile();
        DBCFile dbcFile2 = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        try (InputStream is = DBCParser.class.getResourceAsStream("/example.dbc"))
        {
            parser.parse(is, dbcFile1);
        }
        StringBuilder sb = new StringBuilder();
        dbcFile1.print(sb);
        parser.parse(sb, dbcFile2);
        assertTrue(dbcFile1.equals(dbcFile2));
    }
}
