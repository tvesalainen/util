/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BaseBeanFieldTest
{
    
    public BaseBeanFieldTest()
    {
    }

    @Test
    public void test()
    {
        Base base = new Base();
        SimpleBeanField<Base,Integer> bbf1 = new SimpleBeanField<>(base, "integer");
        SimpleBeanField<Base,String> bbfs = new SimpleBeanField<>(base, "string");
        bbf1.set(1234);
        assertEquals(1234, (int)bbf1.get());
        bbf1.set("4321");
        assertEquals(4321, (int)bbf1.get());
        bbfs.set(1234);
        assertEquals("1234", bbfs.get());
    }
    
    static class Base
    {
        private int integer = 0;
        private String string;

        public int getInteger()
        {
            return integer;
        }

        public void setInteger(int integer)
        {
            this.integer = integer;
        }

        public String getString()
        {
            return string;
        }

        public void setString(String string)
        {
            this.string = string;
        }
        
    }
}
