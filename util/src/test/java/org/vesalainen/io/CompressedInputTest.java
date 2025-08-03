/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.code.AbstractPropertySetter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CompressedInputTest
{
    
    public CompressedInputTest()
    {
    }

    //@Test
    public void test0() throws IOException
    {
        CompressedInput ci = new CompressedInput(Paths.get("C:\\Users\\tkv\\share\\20211105031240.mea"));
        ci.dump(System.err);
    }
    //@Test
    public void test1() throws IOException
    {
        CompressedInput ci = new CompressedInput(Paths.get("src", "test", "resources",  "20180409122422.trc"));
        ci.dump(System.err);
    }
    //@Test
    public void test2() throws IOException
    {
        Path dir = Paths.get("src", "test", "resources");
        PS ps = new PS();
        CompressedInput.readTransactional(Files.list(dir).filter((p)->p.getFileName().toString().endsWith(".trc")), ps);
    }
    private class PS extends AbstractPropertySetter
    {

        @Override
        public String[] getProperties()
        {
            return new String[]{"latitude", "longitude" };
        }

        @Override
        public void setProperty(String property, Object arg)
        {
            System.err.println(property+"="+arg);
        }
        
    }
}
