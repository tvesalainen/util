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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.bean.BeanHelperTest.TestClass.En;
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
    public void test0() throws NoSuchFieldException
    {
        assertEquals("prefix.1", BeanHelper.prefix("prefix.1.2"));
        assertEquals("", BeanHelper.prefix("prefix"));
        assertEquals("prefix", BeanHelper.suffix("prefix"));
        assertEquals("suffix", BeanHelper.suffix("prefix.suffix"));
        assertTrue(BeanHelper.isListItem("prefix.1.2"));
        assertFalse(BeanHelper.isListItem("2"));
        assertFalse(BeanHelper.isListItem("prefix"));
    }
    @Test
    public void test1() throws NoSuchFieldException
    {
        TestClass tc = new TestClass();
        BeanHelper.setValue(tc, "count", 123);
        assertEquals(123, tc.cnt);
        assertEquals(123, BeanHelper.getValue(tc, "count"));
        BeanHelper.setValue(tc, "name", "Nimi");
        assertEquals("Nimi", tc.name);
        assertEquals("Nimi", BeanHelper.getValue(tc, "name"));
        BeanHelper.setValue(tc, "name", null);
        assertNull(tc.name);
        BeanHelper.setValue(tc, "number", 123L);
        assertEquals(123, tc.number);
        assertEquals(123L, BeanHelper.getValue(tc, "number"));
        assertEquals(3, BeanHelper.getValue(tc, "list.2"));
        assertEquals(4, BeanHelper.getValue(tc, "list.size"));
        assertEquals(int.class, BeanHelper.getType(tc, "list.size"));
        assertEquals("test1", BeanHelper.getValue(tc, "inners.0.test"));
        assertEquals(String.class, BeanHelper.getType(tc, "inners.1.test"));
        XmlRootElement root = BeanHelper.getAnnotation(tc, "inners.0", XmlRootElement.class);
        assertEquals("test", root.name());
        
        BeanHelper.setValue(tc, "inners.1.test", "testaa");
        assertEquals("testaa", BeanHelper.getValue(tc, "inners.1.test"));
        
        BeanHelper.setValue(tc, "list.2", 123);
        assertEquals(123, BeanHelper.getValue(tc, "list.2"));
        
        BeanHelper.doFor(tc, "list", (List<Integer> x)->x.add(54321));
        assertEquals(5, BeanHelper.getValue(tc, "list.size"));
        assertEquals(54321, BeanHelper.getValue(tc, "list.4"));
        
        BeanHelper.stream(tc).forEach((String n)->BeanHelper.getType(tc, n));
        BeanHelper.stream(tc).forEach((String n)->System.err.println(n));
        assertEquals(16, BeanHelper.stream(tc).count());
        
        assertEquals(InnerClass.class, BeanHelper.getParameterTypes(tc, "inners")[0]);
        
        BeanHelper.addList(tc, "list", 987654);
        assertEquals(6, BeanHelper.getValue(tc, "list.size"));
        assertEquals(987654, BeanHelper.getValue(tc, "list.5"));

        BeanHelper.removeList(tc, "list.5");
        assertEquals(5, BeanHelper.getValue(tc, "list.size"));
        
        BeanHelper.applyList(tc, "list.0-");
        assertEquals(4, BeanHelper.getValue(tc, "list.size"));
        
        BeanHelper.applyList(tc, "inners+");
        assertEquals(3, BeanHelper.getValue(tc, "inners.size"));
        
        BeanHelper.applyList(tc, "inners+do it right", (Class<Object> c, String h)->{return null;});
        assertEquals(4, BeanHelper.getValue(tc, "inners.size"));
        assertNull(BeanHelper.getValue(tc, "inners.3"));
        
        assertEquals("el", BeanHelper.getAnnotation(tc, "inners.0.test", XmlElement.class).name());
        assertEquals("at", BeanHelper.getAnnotation(tc, "count", XmlAttribute.class).name());
        
        
    }
    
    @Test
    public void test2() throws NoSuchFieldException
    {
        TestClass tc = new TestClass();

        BeanHelper.setValue(tc, "en", En.E3);
        assertEquals(En.E3, BeanHelper.getValue(tc, "en"));
        
        BeanHelper.setValue(tc, "es", new En[]{En.E3});
        assertEquals(EnumSet.of(En.E3), BeanHelper.getValue(tc, "es"));
        
    }
    @XmlRootElement(name = "test")
    static class InnerClass
    {
        @XmlElement(name = "el")
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
        enum En {E1, E2, E3, E4};
        protected En en;
        protected EnumSet<En> es = EnumSet.noneOf(En.class);
        private int cnt;
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

        public EnumSet<En> getEs()
        {
            return es;
        }

        public void setEs(EnumSet<En> es)
        {
            this.es = es;
        }

        public En getEn()
        {
            return en;
        }

        public void setEn(En en)
        {
            this.en = en;
        }

        public List<InnerClass> getInners()
        {
            return inners;
        }

        public List<Integer> getList()
        {
            return list;
        }

        @XmlAttribute(name = "at")
        public int getCount()
        {
            return cnt;
        }

        public void setCount(int count)
        {
            this.cnt = count;
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
