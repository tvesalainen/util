/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileUtilTest
{
    
    public FileUtilTest()
    {
    }

    @Test
    public void testTimes() throws IOException
    {
        Path cur = Paths.get(".");
        FileTime creationTime = FileUtil.getCreationTime(cur);
        assertNotNull(creationTime);
        FileTime lastAccessTime = FileUtil.getLastAccessTime(cur);
        assertNotNull(lastAccessTime);
        FileTime lastModified = FileUtil.getLastModifiedTime(cur);
        assertNotNull(lastModified);
    }
    @Test
    public void testAsciiLines() throws IOException
    {
        try (InputStream is = FileUtilTest.class.getResourceAsStream("/lines_ansi.txt"))
        {
            List<String> list = FileUtil.lines(is, US_ASCII).collect(Collectors.toList());
            assertEquals(3, list.size());
            assertEquals("First line", list.get(0));
            assertEquals("Second line", list.get(1));
            assertEquals("Third lineFourth line", list.get(2));
        }
    }
    
    @Test
    public void testUTF8Lines() throws IOException
    {
        try (InputStream is = FileUtilTest.class.getResourceAsStream("/lines_utf.txt"))
        {
            List<String> list = FileUtil.lines(is, UTF_8).collect(Collectors.toList());
            assertEquals(3, list.size());
            assertEquals("Ensimmäinen rivi", list.get(0));
            assertEquals("Toinen riviKolmas rivi", list.get(1));
            assertEquals("Neljäs rivi", list.get(2));
        }
    }
    
    @Test
    public void testISO_8859_1Lines() throws IOException
    {
        try (InputStream is = FileUtilTest.class.getResourceAsStream("/lines_iso.txt"))
        {
            List<String> list = FileUtil.lines(is, ISO_8859_1).collect(Collectors.toList());
            assertEquals(3, list.size());
            assertEquals("Ensimmäinen rivi", list.get(0));
            assertEquals("Toinen riviKolmas rivi", list.get(1));
            assertEquals("Neljäs rivi", list.get(2));
        }
    }
    
}
