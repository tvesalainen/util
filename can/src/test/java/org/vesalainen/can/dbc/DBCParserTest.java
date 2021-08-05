/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.parser.annotation.ReservedWords;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCParserTest
{
    
    public DBCParserTest()
    {
    }

    @Test
    public void testCSS_Electronics_OBD2_v1_0() throws IOException
    {
        Path path = Paths.get("C:\\Users\\tkv\\Downloads\\sample-data\\sample-data\\sample-data\\OBD2-DBC-MDF4\\OBD2-DBC-MDF4\\CSS-Electronics-OBD2-v1.0.dbc");
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        parser.parse(path, dbcFile);
    }
    //@Test
    public void testOrion() throws IOException
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        try (InputStream is = DBCParser.class.getResourceAsStream("/Orion_CANBUS.dbc"))
        {
            parser.parse(is, dbcFile);
        }
    }
    //@Test
    public void testEx() throws IOException
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        try (InputStream is = DBCParser.class.getResourceAsStream("/example.dbc"))
        {
            parser.parse(is, dbcFile);
        }
    }
    //@Test
    public void testCurtis() throws IOException
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        try (InputStream is = DBCParser.class.getResourceAsStream("/curtis_ac1239_map.dbc"))
        {
            parser.parse(is, dbcFile);
        }
    }
    //@Test TODO ENUM
    public void testJ1939() throws IOException
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        URL url = new URL("https://hackage.haskell.org/package/ecu-0.0.8/src/src/j1939_utf8.dbc");
        parser.parse(url, dbcFile);
    }
    
}
