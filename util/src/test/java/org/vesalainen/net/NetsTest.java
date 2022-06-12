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
package org.vesalainen.net;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NetsTest
{
    
    public NetsTest()
    {
    }

    //@Test
    public void testGetPath() throws MalformedURLException, URISyntaxException
    {
        try
        {
            Nets.getPath("https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html");
            fail();
        }
        catch (IllegalArgumentException ex)
        {
            
        }
    }
    //@Test
    public void testExists() throws MalformedURLException
    {
        assertFalse(Nets.exists("http://pi3dashboard:9000/aaa.js"));
        assertFalse(Nets.exists("http://pi4media/aaa.js"));
        assertTrue(Nets.exists("https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html"));
    }
    
}
