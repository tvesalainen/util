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
    public void test() throws IOException
    {
        test("/CSS-Electronics-OBD2-v1.0.dbc");
        test("/Orion_CANBUS.dbc");
        test("/example.dbc");
        test("/curtis_ac1239_map.dbc");
        test("/j1939_utf8.dbc");
        test("/n2k.dbc");
    }
    public void test(String file) throws IOException
    {
        DBCFile dbcFile = new DBCFile();
        DBCParser parser = DBCParser.getInstance();
        try (InputStream is = DBCParser.class.getResourceAsStream(file))
        {
            parser.parse(is, dbcFile);
        }
        StringBuilder sb = new StringBuilder();
        dbcFile.print(sb);
        DBCFile dbcFile2 = new DBCFile();
        parser.parse(sb, dbcFile2);
    }
    
}
