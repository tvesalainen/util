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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.ham.itshfbc.Command.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CommandLineTest
{
    
    public CommandLineTest()
    {
    }

    @Test
    public void testString()
    {
        CommandLine<String> cl = new CommandLine<>(LABEL, 20, "NEW ORLEANS", "COLON");
        assertEquals("LABEL     NEW ORLEANS         COLON", cl.toString());
    }
    @Test
    public void testInteger()
    {
        CommandLine<Integer> cl = new CommandLine<>(TIME, 5, 1, 24, 1, 1);
        assertEquals("TIME          1   24    1    1", cl.toString());
    }
    @Test
    public void testFrequency()
    {
        CommandLine<Double> cl = new CommandLine<>(FREQUENCY, 5, 6.07, 7.20, 9.70, 11.85, 13.70, 15.35, 17.73, 21.65, 25.89, 0.00, 0.00);
        String exp = "FREQUENCY  6.07  7.2  9.711.85 13.715.3517.7321.6525.89  0.0  0.0";
        String got = cl.toString();
        System.err.println(exp);
        System.err.println(got);
        assertEquals(exp, got);
    }
    
}
