/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.code;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FunctionalSetterTest
{
    
    public FunctionalSetterTest()
    {
    }

    @Test
    public void test1()
    {
        FS fs = FS.newInstance(FS.class);
        fs.setB((byte)1);
        fs.setString("kukkuu");
        S s1 = new S();
        S s2 = new S();
        fs.addObserver(s1);
        fs.addObserver(s2);
        fs.setB((byte)1);
        fs.setString("kukkuu");
        fs.removeObserver(s2);
        fs.setB((byte)1);
        fs.removeObserver(s1);
        fs.setB((byte)1);
    }
    
    private static class S extends AbstractPropertySetter
    {

        @Override
        public String[] getPrefixes()
        {
            return new String[]{"string", "b", "c", "d"};
        }

        @Override
        public void setProperty(String property, Object arg)
        {
            System.err.println(this+" "+property+"="+arg);
        }
        
    }
}
