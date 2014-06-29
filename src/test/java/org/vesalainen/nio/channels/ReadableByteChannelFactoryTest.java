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

package org.vesalainen.nio.channels;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.io.RewindableInputStreamTest;

/**
 *
 * @author Timo Vesalainen
 */
public class ReadableByteChannelFactoryTest
{
    
    public ReadableByteChannelFactoryTest()
    {
    }

    /**
     * Test of addHandler method, of class ReadableByteChannelFactory.
     */
    @Test
    public void testAddHandler()
    {
    }

    /**
     * Test of hasHandler method, of class ReadableByteChannelFactory.
     */
    @Test
    public void testHasHandler()
    {
    }

    /**
     * Test of getInstance method, of class ReadableByteChannelFactory.
     */
    @Test
    public void testGetInstance_File() throws Exception
    {
        URL url = ReadableByteChannelFactoryTest.class.getClassLoader().getResource("test.txt");
        try (ReadableByteChannel rbc = ReadableByteChannelFactory.getInstance(Paths.get(url.toURI()).toFile()))
        {
            ByteBuffer bb = ByteBuffer.allocate(32);
            rbc.read(bb);
            assertEquals("Lorem ipsum dolor sit amet, soll", new String(bb.array(), StandardCharsets.US_ASCII));
        }
    }

    /**
     * Test of getInstance method, of class ReadableByteChannelFactory.
     */
    @Test
    public void testGetInstance_Path() throws Exception
    {
        URL url = ReadableByteChannelFactoryTest.class.getClassLoader().getResource("test.txt");
        try (ReadableByteChannel rbc = ReadableByteChannelFactory.getInstance(Paths.get(url.toURI())))
        {
            ByteBuffer bb = ByteBuffer.allocate(32);
            rbc.read(bb);
            assertEquals("Lorem ipsum dolor sit amet, soll", new String(bb.array(), StandardCharsets.US_ASCII));
        }
    }

    /**
     * Test of getInstance method, of class ReadableByteChannelFactory.
     */
    @Test
    public void testGetInstance_URI() throws Exception
    {
        URL url = ReadableByteChannelFactoryTest.class.getClassLoader().getResource("test.txt");
        try (ReadableByteChannel rbc = ReadableByteChannelFactory.getInstance(url.toURI()))
        {
            ByteBuffer bb = ByteBuffer.allocate(32);
            rbc.read(bb);
            assertEquals("Lorem ipsum dolor sit amet, soll", new String(bb.array(), StandardCharsets.US_ASCII));
        }
    }

    /**
     * Test of getInstance method, of class ReadableByteChannelFactory.
     */
    @Test
    public void testGetInstance_URL() throws Exception
    {
        URL url = ReadableByteChannelFactoryTest.class.getClassLoader().getResource("test.txt");
        try (ReadableByteChannel rbc = ReadableByteChannelFactory.getInstance(url))
        {
            ByteBuffer bb = ByteBuffer.allocate(32);
            rbc.read(bb);
            assertEquals("Lorem ipsum dolor sit amet, soll", new String(bb.array(), StandardCharsets.US_ASCII));
        }
    }
    
    @Test
    public void testGetInstance_URL_HTTP() throws Exception
    {
        URL url = new URL("http://www.sailfaraway.net/");
        try (ReadableByteChannel rbc = ReadableByteChannelFactory.getInstance(url))
        {
            ByteBuffer bb = ByteBuffer.allocate(15);
            byte[] array = bb.array();
            int rc = rbc.read(bb);
            String got = new String(array, StandardCharsets.US_ASCII);
            assertEquals("<!DOCTYPE html>", got);
        }
    }
    
}
