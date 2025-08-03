/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.concurrent;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SynchronizedRingBufferTest
{
    
    public SynchronizedRingBufferTest()
    {
    }

    @Test
    public void test1()
    {
        try
        {
            URL url = SynchronizedRingBufferTest.class.getClassLoader().getResource("test.txt");
            Path path = Paths.get(url.toURI());
            File file = path.toFile();
            FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
            SynchronizedRingBuffer sr = new SynchronizedRingBuffer(256, false, 254);
            BufferConsumer bc = new BufferConsumer() 
            {
                @Override
                protected void consume()
                {
                    while (true)
                    {
                        try
                        {
                            byte b = read();
                            System.err.print((char)b);
                            release();
                        }
                        catch (InterruptedException ex)
                        {
                            System.err.println("");
                        }
                    }
                }
            };
            sr.addConsumer(bc);
            sr.read(fc);
        }
        catch (URISyntaxException | IOException ex)
        {
            fail(ex.getMessage());
        }
    }
    
}
