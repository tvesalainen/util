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
import org.vesalainen.util.AbstractProvisioner.Setting;

/**
 *
 * @author Timo Vesalainen
 */
public class AbstractProvisionerTest
{
    public AbstractProvisionerTest()
    {
    }

    /**
     * Test of class AbstractProvisioner.
     */
    @Test
    public void test()
    {
        AbstractProvisionerImpl api = new AbstractProvisionerImpl();
        TestClass testClass = new TestClass();
        api.attachInstant(testClass);
        assertEquals("settingA", testClass.getA());
        api.setValue("settingA", "b");
        assertEquals("b", testClass.getA());
    }

    public class TestClass
    {
        private String a;
        @Setting("settingA")
        public void setA(String a)
        {
            this.a = a;
        }

        public String getA()
        {
            return a;
        }
        
    }
    public class AbstractProvisionerImpl extends AbstractProvisioner
    {

        @Override
        public Object getValue(String name)
        {
            return name;
        }
    }
    
}
