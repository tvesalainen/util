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
package org.vesalainen.ham.itshfbc;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.ham.itshfbc.GeoSearch.of;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class GeoDBTest
{
    
    public GeoDBTest()
    {
        JavaLogging.setConsoleHandler("org.vesalainen", Level.FINEST);
    }

    //@Test
    public void test1() throws IOException
    {
        GeoDB db = new GeoDB();
        GeoLocation result = db.search(10, of("CITY", "HELSINKI"));
        assertNotNull(result);
    }
    
}
