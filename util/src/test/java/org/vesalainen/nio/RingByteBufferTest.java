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
import org.vesalainen.util.OrMatcher;
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
            URL url = RingByteBufferTest.class.getClassLoader().getResource("test.txt");
            Path path = Paths.get(url.toURI());
            FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WritableByteChannel oc = Channels.newChannel(baos);
            GatheringByteChannel gbc = ChannelHelper.newGatheringByteChannel(oc);
            
            String str1 = "pellentesque";
            String str2 = "sollicitudin";
            SimpleMatcher matcher1 = new SimpleMatcher(str1, StandardCharsets.US_ASCII);
            SimpleMatcher matcher2 = new SimpleMatcher(str2, StandardCharsets.US_ASCII);
            OrMatcher matcher = new OrMatcher();
            matcher.add(matcher1);
            matcher.add(matcher2);
            boolean mark = true;
            int writeCount = 0;
            RingByteBuffer rbb = new RingByteBuffer(100);
            int rc = rbb.read(fc);
            assertTrue(rbb.isFull());
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
                            assertTrue(str1.contentEquals(rbb) || str2.contentEquals(rbb));
                            rbb.write(gbc);
                            mark = true;
                            writeCount++;
                            break;
                    }
                }
                rc = rbb.read(fc);
                assertTrue(rc <= 100);
            }
            assertEquals(26, writeCount);
            assertEquals(22*str1.length()+4*str2.length(), baos.size());
            String res = baos.toString(StandardCharsets.US_ASCII.name());
            assertEquals(22, countStrings(res, str1));
            assertEquals(4, countStrings(res, str2));
        }
        catch (IOException | URISyntaxException ex)
        {
            Logger.getLogger(RingByteBufferTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void test2()
    {
        try
        {
            URL url = RingByteBufferTest.class.getClassLoader().getResource("nmea.txt");
            Path path = Paths.get(url.toURI());
            File file = path.toFile();
            FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);

            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            WritableByteChannel oc1 = Channels.newChannel(baos1);
            GatheringByteChannel gbc1 = ChannelHelper.newGatheringByteChannel(oc1);
            
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            WritableByteChannel oc2 = Channels.newChannel(baos2);
            GatheringByteChannel gbc2 = ChannelHelper.newGatheringByteChannel(oc2);
            
            SimpleMatcher matcher1 = new SimpleMatcher("$??RMC*\r\n", StandardCharsets.US_ASCII);
            SimpleMatcher matcher2 = new SimpleMatcher("$??DPT*\r\n", StandardCharsets.US_ASCII);
            OrMatcher<GatheringByteChannel> matcher = new OrMatcher<>();
            matcher.add(matcher1, gbc1);
            matcher.add(matcher2, gbc2);
            boolean mark = true;
            RingByteBuffer rbb = new RingByteBuffer(100);
            int rc = rbb.read(fc);
            while (rc > 0)
            {
                while (rbb.hasRemaining())
                {
                    byte b = rbb.get(mark);
                    Matcher.Status match = matcher.match(b);
                    switch (match)
                    {
                        case Ok:
                        case WillMatch:
                            mark = false;
                            break;
                        case Error:
                            mark = true;
                            break;
                        case Match:
                            for (GatheringByteChannel gbc : matcher.getLastMatched())
                            {
                                rbb.write(gbc);
                            }
                            mark = true;
                            break;
                    }
                }
                rc = rbb.read(fc);
                assertTrue(rc <= 100);
            }
            String res1 = baos1.toString(StandardCharsets.US_ASCII.name());
            assertEquals(11, countStrings(res1, "RMC"));
            String res2 = baos2.toString(StandardCharsets.US_ASCII.name());
            assertEquals(10, countStrings(res2, "DPT"));
        }
        catch (IOException | URISyntaxException ex)
        {
            Logger.getLogger(RingByteBufferTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int countStrings(String str, String sub)
    {
        int count=0;
        int index = str.indexOf(sub);
        while (index != -1)
        {
            count++;
            index = str.indexOf(sub, index+sub.length());
        }
        return count;
    }
}
