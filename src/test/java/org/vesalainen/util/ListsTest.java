/*
 * Copyright (C) 2016 tkv
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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class ListsTest
{
    
    public ListsTest()
    {
    }

    @Test
    public void test1()
    {
        List<String> list = new ArrayList<>();
        Lists.populate(list, "a1", "a2", "a3");
        String exp = "{`a1´, `a2´, `a3´}";
        String got = Lists.print("{", ", ", "`", "´", "}", list);
        assertEquals(exp, got);
    }
    
    @Test
    public void test1_1()
    {
        List<String> list = new ArrayList<>();
        Lists.populate(list, "a1", "a2", "a3");
        String exp = "a1, a2, a3";
        String got = Lists.print(", ", list);
        assertEquals(exp, got);
    }
    
    @Test
    public void test2()
    {
        String exp = "{`a1´, `a2´, `a3´}";
        String got = Lists.print("{", ", ", "`", "´", "}", "a1", "a2", "a3");
        assertEquals(exp, got);
    }
    
    @Test
    public void test2_1()
    {
        String exp = "a1, a2, a3";
        String got = Lists.print(", ", "a1", "a2", "a3");
        assertEquals(exp, got);
    }
    
}
