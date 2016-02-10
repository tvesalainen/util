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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class BeanHelperTest
{
    
    public BeanHelperTest()
    {
    }

    @Test
    public void test1()
    {
        TestClass tc = new TestClass();
        BeanHelper.setFieldValue(tc, "count", 123);
        assertEquals(123, tc.count);
        assertEquals(123, BeanHelper.getFieldValue(tc, "count"));
        BeanHelper.setFieldValue(tc, "name", "Nimi");
        assertEquals("Nimi", tc.name);
        assertEquals("Nimi", BeanHelper.getFieldValue(tc, "name"));
        BeanHelper.setFieldValue(tc, "number", 123L);
        assertEquals(123, tc.number);
        assertEquals(123L, BeanHelper.getFieldValue(tc, "number"));
    }
    
    static class TestClass
    {
        private int count;
        private String name;
        public long number;

        public int getCount()
        {
            return count;
        }

        public void setCount(int count)
        {
            this.count = count;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

    }
}
