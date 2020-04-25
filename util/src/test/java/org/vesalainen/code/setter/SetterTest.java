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
package org.vesalainen.code.setter;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SetterTest
{
    
    public SetterTest()
    {
    }

    @Test
    public void test1()
    {
        Map<String,Setter> m = new HashMap<>();
        IntSetter is = (v)->System.err.println("int "+v);
        LongSetter ls = (v)->System.err.println("long "+v);
        m.put("foo", is);
        m.put("bar", ls);
        Setter foo = m.get("foo");
        Setter bar = m.get("bar");
        IntSetter andThen = (IntSetter) foo.andThen(foo);
        andThen.set(3);
        try
        {
            IntSetter ais = (IntSetter) foo.andThen(bar);
            fail();
        }
        catch (ClassCastException ex)
        {
        }
    }
    
}
