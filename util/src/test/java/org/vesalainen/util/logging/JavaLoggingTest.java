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
package org.vesalainen.util.logging;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class JavaLoggingTest
{
    
    public JavaLoggingTest()
    {
    }

    @Test
    public void test1() throws IOException, URISyntaxException
    {
        URL url = JavaLoggingTest.class.getResource("/log-config.xml");
        JavaLogging.xmlConfig(new File(url.toURI()));
        Logger logger = Logger.getLogger("test");
        assertEquals(Level.FINEST, logger.getLevel());
        logger = Logger.getLogger("access");
        assertEquals(Level.INFO, logger.getLevel());
        logger.info("test");
        Handler[] handlers = logger.getHandlers();
        assertNotNull(handlers);
        assertTrue(handlers.length > 0);
    }

    /**
     * Test of logIt method, of class JavaLogging.
     */
    @Test
    public void testLogIt_Level_Supplier()
    {
    }

    /**
     * Test of logIt method, of class JavaLogging.
     */
    @Test
    public void testLogIt_Level_String()
    {
    }

    /**
     * Test of logIt method, of class JavaLogging.
     */
    @Test
    public void testLogIt_3args()
    {
    }
    
}
