/*
 * Copyright (C) 2014 Timo Vesalainen
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

import static org.junit.Assert.*;
import org.junit.Test;
import org.vesalainen.util.AbstractStringProvisioner.StringSetting;

/**
 *
 * @author Timo Vesalainen
 */
public class AbstractStringProvisionerTest
{
    public AbstractStringProvisionerTest()
    {
    }

    /**
     * Test of class AbstractProvisioner.
     */
    @Test
    public void test()
    {
        StringProvisionerImpl api = new StringProvisionerImpl();
        TestClass testClass = new TestClass();
        api.attach(testClass);
        assertEquals("settingA", testClass.getA());
        api.setValue("settingA", "b");
        assertEquals("b", testClass.getA());
    }

    public class TestClass
    {
        private String a;
        @StringSetting("settingA")
        public void setA(String a)
        {
            this.a = a;
        }

        public String getA()
        {
            return a;
        }
        
    }
    public class StringProvisionerImpl extends AbstractStringProvisioner<TestClass>
    {

        @Override
        public Object getValue(String name)
        {
            return name;
        }
    }
    
}
