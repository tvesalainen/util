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
package org.vesalainen.math;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AnnotatedPointListTest
{
    
    public AnnotatedPointListTest()
    {
    }

    @Test
    public void test1()
    {
        AnnotatedPointList<String> apl = new AnnotatedPointList<>();
        apl.add(6, 7, "67");
        apl.add(4, 5, "45");
        apl.add(2, 3, "23");
        apl.add(0, 1, "01");
        
        assertEquals("23", apl.getAnnotation(2));
        
        apl.sort();
        
        assertEquals("01", apl.getAnnotation(0));
        assertEquals("23", apl.getAnnotation(1));
        assertEquals("45", apl.getAnnotation(2));
        assertEquals("67", apl.getAnnotation(3));
    }
    
}
