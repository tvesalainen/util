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
package org.vesalainen.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NetFileTest
{
    
    public NetFileTest()
    {
    }

    //@Test
    public void test1() throws MalformedURLException, IOException
    {
        NetFile nf = new NF(
                Paths.get("c:\\temp\\prediML.txt"), 
                new URL("http://www.sidc.be/silso/FORECASTS/prediML.txt"), 
                10, 
                TimeUnit.MINUTES
        );
        nf.refresh();
    }
    class NF extends NetFile
    {

        public NF(Path file, URL url, long expires, TimeUnit unit)
        {
            super(file, url, expires, unit);
        }

        @Override
        protected void update(Path file) throws IOException
        {
        }
        
    }
}
