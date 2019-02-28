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
package org.vesalainen.ui.scale;

import java.util.PrimitiveIterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractScaleLevelTest
{
    
    public AbstractScaleLevelTest()
    {
    }

    @Test
    public void test1()
    {
        AbstractScaleLevel asl = new AbstractScaleLevel(3, null);
        assertEquals(1, asl.count(3.1, 8.9));
        PrimitiveIterator.OfDouble iterator = asl.iterator(3.1, 8.9);
        assertEquals(6, iterator.next(), 1e-10);
        assertFalse(iterator.hasNext());
    }
    @Test
    public void test2()
    {
        AbstractScaleLevel asl = new AbstractScaleLevel(3, null);
        assertEquals(3, asl.count(2.1, 9.1));
        PrimitiveIterator.OfDouble iterator = asl.iterator(2.1, 9.1);
        assertEquals(3, iterator.next(), 1e-10);
        assertEquals(6, iterator.next(), 1e-10);
        assertEquals(9, iterator.next(), 1e-10);
        assertFalse(iterator.hasNext());
    }
    @Test
    public void test3()
    {
        AbstractScaleLevel asl = new AbstractScaleLevel(3, null);
        assertEquals(2, asl.count(6, 9.1));
        PrimitiveIterator.OfDouble iterator = asl.iterator(6, 9.1);
        assertEquals(6, iterator.next(), 1e-10);
        assertEquals(9, iterator.next(), 1e-10);
        assertFalse(iterator.hasNext());
    }
    @Test
    public void test4()
    {
        AbstractScaleLevel asl = new AbstractScaleLevel(3, null);
        assertEquals(3, asl.count(2.1, 9));
        PrimitiveIterator.OfDouble iterator = asl.iterator(2.1, 9);
        assertEquals(3, iterator.next(), 1e-10);
        assertEquals(6, iterator.next(), 1e-10);
        assertEquals(9, iterator.next(), 1e-10);
        assertFalse(iterator.hasNext());
    }
    @Test
    public void test5()
    {
        AbstractScaleLevel asl = new AbstractScaleLevel(3, null);
        assertEquals(3, asl.count(3, 9));
        PrimitiveIterator.OfDouble iterator = asl.iterator(3, 9);
        assertEquals(3, iterator.next(), 1e-10);
        assertEquals(6, iterator.next(), 1e-10);
        assertEquals(9, iterator.next(), 1e-10);
        assertFalse(iterator.hasNext());
    }
    //@Test
    public void testä()
    {
        for (int ii=0;ii<0x10ffff;ii++)
        {
            if (Character.isLetter(ii))
            {
                Character.UnicodeBlock of = Character.UnicodeBlock.of(ii);
                if (Character.UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS.equals(of))
                System.err.println(ii+": "+of+" "+new String(Character.toChars(ii)));
            }
        }
    }    
}
