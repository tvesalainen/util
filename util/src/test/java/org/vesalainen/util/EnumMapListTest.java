/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class EnumMapListTest
{
    
    public EnumMapListTest()
    {
    }

    @Test
    public void test1()
    {
        EnumMapList<SmallEnum,String> hml = new EnumMapList<>(SmallEnum.class);
        hml.add(SmallEnum.S0, "1");
        hml.add(SmallEnum.S0, "2");
        List<String> lst = hml.get(SmallEnum.S0);
        assertEquals(2, lst.size());
        hml.removeItem(SmallEnum.S0, "2");
        lst = hml.get(SmallEnum.S0);
        assertEquals(1, lst.size());
        hml.removeItem(SmallEnum.S0, "1");
        lst = hml.get(SmallEnum.S0);
        assertEquals(0, lst.size());
    }
    
}
