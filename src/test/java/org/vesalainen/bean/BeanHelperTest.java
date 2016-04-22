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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.Lists;

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
    public void test1() throws NoSuchFieldException
    {
        TestClass tc = new TestClass();
        BeanHelper.setFieldValue(tc, "count", 123);
        assertEquals(123, tc.count);
        assertEquals(123, BeanHelper.getFieldValue(tc, "count"));
        BeanHelper.setFieldValue(tc, "name", "Nimi");
        assertEquals("Nimi", tc.name);
        assertEquals("Nimi", BeanHelper.getFieldValue(tc, "name"));
        BeanHelper.setFieldValue(tc, "name", null);
        assertNull(tc.name);
        BeanHelper.setFieldValue(tc, "number", 123L);
        assertEquals(123, tc.number);
        assertEquals(123L, BeanHelper.getFieldValue(tc, "number"));
        assertEquals(3, BeanHelper.getFieldValue(tc, "list.2"));
        assertEquals(4, BeanHelper.getFieldValue(tc, "list.size"));
        assertEquals(int.class, BeanHelper.getType(tc, "list.size"));
        assertEquals("test1", BeanHelper.getFieldValue(tc, "inners.0.test"));
        assertEquals(String.class, BeanHelper.getType(tc, "inners.1.test"));
        XmlRootElement root = BeanHelper.getAnnotation(tc, "inners.0", XmlRootElement.class);
        assertEquals("test", root.name());
        
        BeanHelper.setFieldValue(tc, "inners.1.test", "testaa");
        assertEquals("testaa", BeanHelper.getFieldValue(tc, "inners.1.test"));
        
        BeanHelper.setFieldValue(tc, "list.2", 123);
        assertEquals(123, BeanHelper.getFieldValue(tc, "list.2"));
        
        BeanHelper.doFor(tc, "list", (List<Integer> x)->x.add(54321));
        assertEquals(5, BeanHelper.getFieldValue(tc, "list.size"));
        assertEquals(54321, BeanHelper.getFieldValue(tc, "list.4"));
        
        BeanHelper.walk(tc, (String n, Object o)->BeanHelper.getType(tc, n));
        
        assertEquals(InnerClass.class, BeanHelper.getParameterTypes(tc, "inners")[0]);
        
        BeanHelper.addList(tc, "list", 987654);
        assertEquals(6, BeanHelper.getFieldValue(tc, "list.size"));
        assertEquals(987654, BeanHelper.getFieldValue(tc, "list.5"));

        BeanHelper.removeList(tc, "list.5");
        assertEquals(5, BeanHelper.getFieldValue(tc, "list.size"));
        
        BeanHelper.applyList(tc, "list.0-");
        assertEquals(4, BeanHelper.getFieldValue(tc, "list.size"));
        
        BeanHelper.applyList(tc, "inners+");
        assertEquals(3, BeanHelper.getFieldValue(tc, "inners.size"));
        
        BeanHelper.applyList(tc, "inners+", (c)->{return null;});
        assertEquals(4, BeanHelper.getFieldValue(tc, "inners.size"));
        assertNull(BeanHelper.getFieldValue(tc, "inners.3"));
    }
    
    @XmlRootElement(name = "test")
    static class InnerClass
    {
        private String test;

        public InnerClass()
        {
        }

        public InnerClass(String test)
        {
            this.test = test;
        }

        public String getTest()
        {
            return test;
        }

        public void setTest(String test)
        {
            this.test = test;
        }
        
    }
    static class TestClass
    {
        private int count;
        private String name;
        public long number;
        protected List<Integer> list = Lists.create(1, 2, 3, 4);
        public List<InnerClass> inners;

        public TestClass()
        {
            inners = new ArrayList<>();
            inners.add(new InnerClass("test1"));
            inners.add(new InnerClass("test2"));
        }

        public List<InnerClass> getInners()
        {
            return inners;
        }

        public List<Integer> getList()
        {
            return list;
        }

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
