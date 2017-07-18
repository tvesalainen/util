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
package org.vesalainen.bean;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class ExpressionParserTest
{
    
    public ExpressionParserTest()
    {
    }

    @Test
    public void test1()
    {
        ExpressionParser ep = new ExpressionParser((s)->s);
        assertEquals("qwerty", ep.replace("${qwerty}"));
        assertEquals("123qwerty456", ep.replace("123${qwerty}456"));
        assertEquals("qwerty", ep.replace("${qw${er${t}}y}"));
    }
    
    @Test
    public void test2()
    {
        ExpressionParser ep = new ExpressionParser("abc");
        assertEquals("12345", ep.replace("12${length}45"));
    }
    @Test
    public void test3()
    {
        Map<String,String> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        ExpressionParser ep = new ExpressionParser(map);
        ep.addMapper((s)->"N/A");
        assertEquals("av1bN/A", ep.replace("a${k1}b${k3}"));
    }
}
