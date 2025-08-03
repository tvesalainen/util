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
package org.vesalainen.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class URLStreamHandlerFactoryImplTest
{
    
    public URLStreamHandlerFactoryImplTest()
    {
    }
    @BeforeClass
    public static void classBefore()
    {
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactoryImpl());
    }
    @Test
    public void test0() throws MalformedURLException
    {
        URL url = new URL("file:///C:/Users/tkv/Documents/javadoc/jdk-8u151-docs-all/docs/api/index.html");
    }
    //@Test
    public void test1() throws URISyntaxException, MalformedURLException, IOException
    {
        URI uri = new URI("tcp", null, "pi2", 10111, null, null, null);
        URL u = uri.toURL();
        InputStream is = u.openStream();
        int rc = is.read();
        while (rc != -1)
        {
            System.err.print((char)rc);
            rc = is.read();
        }
    }
    
}
