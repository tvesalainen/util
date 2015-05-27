/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.nio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.nio.channels.ChannelHelper;
import org.vesalainen.util.Matcher;
import org.vesalainen.util.SimpleMatcher;
import org.vesalainen.util.concurrent.SynchronizedRingBufferTest;

/**
 *
 * @author tkv
 */
public class RingByteBufferTest
{
    
    public RingByteBufferTest()
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

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WritableByteChannel oc = Channels.newChannel(baos);
            GatheringByteChannel gbc = ChannelHelper.getGatheringByteChannel(oc);
            
            String str1 = "pellentesque";
            SimpleMatcher matcher = new SimpleMatcher(str1, StandardCharsets.US_ASCII);
            boolean mark = true;
            int writeCount = 0;
            RingByteBuffer rbb = new RingByteBuffer(100);
            int rc = rbb.read(fc);
            while (rc > 0)
            {
                while (rbb.hasRemaining())
                {
                    byte b = rbb.get(mark);
                    switch (matcher.match(b))
                    {
                        case Ok:
                            mark = false;
                            break;
                        case Error:
                            mark = true;
                            break;
                        case Match:
                            rbb.write(gbc);
                            mark = true;
                            writeCount++;
                            break;
                    }
                }
                rc = rbb.read(fc);
                assertTrue(rc <= 100);
            }
            assertEquals(22, writeCount);
            assertEquals(22*str1.length(), baos.size());
        }
        catch (IOException | URISyntaxException ex)
        {
            Logger.getLogger(RingByteBufferTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
