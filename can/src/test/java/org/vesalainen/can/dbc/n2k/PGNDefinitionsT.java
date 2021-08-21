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
package org.vesalainen.can.dbc.n2k;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.DBCParser;
import org.vesalainen.text.CamelCase;
import org.xml.sax.SAXException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PGNDefinitionsT
{
    @Test
    public void test()
    {
        N2KData n2kData = N2KData.N2K;
    }
    @Test
    public void createN2KDBC() throws ParserConfigurationException, SAXException, IOException
    {
        PGNDefinitions pgnDef = new PGNDefinitions(Paths.get("C:\\Users\\tkv\\Documents\\NetBeansProjects\\canboat\\analyzer\\pgns.xml"));
        StringBuilder sb = new StringBuilder();
        pgnDef.print(sb);
        DBCFile dbcFile2 = new DBCFile();
        System.err.print(sb);
        DBCParser parser = DBCParser.getInstance();
        parser.parse(sb, dbcFile2);
        Path target = Paths.get("src", "main", "resources", "n2kproprietary.dbc");
        try (BufferedWriter br = Files.newBufferedWriter(target))
        {
            pgnDef.print(br);
        }
    }
    //@Test
    public void createNMEAPGN()
    {
        N2KData n2kData = N2KData.N2K;
        n2kData.getPGNs().forEach((pgn)->
        {
            N2KData.PGNInfo pgnInfo = n2kData.getPGNInfo(pgn, null);
            String name = pgnInfo.getName();
            name = CamelCase.delimitedUpper(name, "_");
            int idx = name.indexOf('_');
            name = name.substring(idx+1);
            System.err.println("/**");
            System.err.println("* "+pgnInfo.getDescription());
            System.err.println("*/");
            System.err.println(name+"("+pgn+"),");
        });
    }
}
