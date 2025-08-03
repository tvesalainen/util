/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.fx;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ValidatingNumberStringConverterTest
{
    
    public ValidatingNumberStringConverterTest()
    {
    }

    @Test
    public void testDouble()
    {
        ValidatingDoubleStringConverter vdsc = new ValidatingDoubleStringConverter(0, 100);
        assertEquals("1.0", vdsc.toString(1.0));
        assertEquals(1.0, vdsc.fromString("1.0"), 1e-10);
        try
        {
            vdsc.fromString("-1.0");
            fail();
        }
        catch (NumberFormatException ex)
        {
            
        }
        try
        {
            vdsc.fromString("123.0");
            fail();
        }
        catch (NumberFormatException ex)
        {
            
        }
        try
        {
            vdsc.fromString("1a");
            fail();
        }
        catch (NumberFormatException ex)
        {
            
        }
    }
    
}
