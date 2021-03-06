/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.station;

import org.vesalainen.ham.station.StationConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.xml.datatype.DatatypeConfigurationException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.BroadcastStationsFile;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StationConverterTest
{
    
    public StationConverterTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.CONFIG);
    }

    @Test
    public void test() throws IOException, DatatypeConfigurationException
    {
        Path resources = Paths.get("src", "main", "resources");
        Path in = resources.resolve("rfax.txt");
        Path out = resources.resolve("broadcast-stations.xml");
        StationConverter sc = new StationConverter(resources, out);
        sc.convert();
        assertEquals(0, sc.testCases());
    }
}
