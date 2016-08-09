/*
 * Copyright (C) 2016 tkv
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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author tkv
 */
public class SecuredFileTest
{
    private Path dir;
    @Before
    public void before() throws IOException
    {
        dir = Files.createTempDirectory("test");
    }

    @After
    public void after() throws IOException
    {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir))
        {
            ds.forEach((p)->
            {
                try
                {
                    Files.delete(p);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            });
        }
        Files.delete(dir);
    }

    @Test
    public void test1() throws IOException
    {
        Random random = new Random(123456789L);
        byte[] exp1 = new byte[1024];
        byte[] exp2 = new byte[1024];
        byte[] got1 = new byte[1024];
        byte[] got2 = new byte[1024];
        random.nextBytes(exp1);
        random.nextBytes(exp2);
        Path test = dir.resolve("testfile");
        SecuredFile sf = new SecuredFile(test, ".bak");
        
        sf.save((o)->o.write(exp1));
        assertTrue(Files.exists(test));
        assertFalse(Files.exists(sf.getSafePath()));
        sf.load((i)->i.read(got1));
        assertTrue(Files.exists(test));
        assertFalse(Files.exists(sf.getSafePath()));
        assertArrayEquals(exp1, got1);
        
        sf.save((o)->o.write(exp2));
        assertTrue(Files.exists(test));
        assertTrue(Files.exists(sf.getSafePath()));
        sf.load((i)->i.read(got2));
        assertTrue(Files.exists(test));
        assertFalse(Files.exists(sf.getSafePath()));
        assertArrayEquals(exp2, got2);

        try
        {
            sf.save((o)->{throw new IOException();});
            fail("no IOException");
        }
        catch (IOException ex)
        {
        }
        assertTrue(Files.exists(sf.getSafePath()));
        sf.load((i)->
        {
            if (i.read(got2) != 1024)
            {
                throw new IOException();
            }
        });
        assertTrue(Files.exists(test));
        assertFalse(Files.exists(sf.getSafePath()));
        assertArrayEquals(exp2, got2);
        
    }
}
