/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LogOutputStreamTest
{
    
    public LogOutputStreamTest()
    {
    }

    @Test
    public void test() throws UnsupportedEncodingException
    {
        LogOutputStream err = new LogOutputStream("err:%s", JavaLogging.getLogger("org.vesalainen"), Level.INFO);
        System.setErr(err);
        System.err.println("hello is it logging");
    }
    
}
