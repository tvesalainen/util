/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.nio.file.attribute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.UserPrincipal;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class PosixHelpTest
{
    
    public PosixHelpTest()
    {
    }

    //@Test
    public void testTimes() throws IOException
    {
        Path file = Paths.get("pom.xml");
        FileAttribute<FileTime> lat = PosixHelp.getLastAccessTimeTimeAsAttribute(file);
        FileAttribute<FileTime> lmt = PosixHelp.getLastModifiedTimeAsAttribute(file);
        FileAttribute<FileTime> ct = PosixHelp.getCreationTimeTimeAsAttribute(file);
        Path tmp = Paths.get("test.tml");
        Files.createFile(tmp);
        Files.setAttribute(tmp, lat.name(), lat.value());
        Files.setAttribute(tmp, lmt.name(), lmt.value());
        Files.setAttribute(tmp, ct.name(), ct.value());
        Files.delete(tmp);
    }
    //@Test
    public void testGetOwner() throws IOException
    {
        String owner = PosixHelp.getOwner(Paths.get("pom.xml"));
        PosixHelp.getOwnerAsAttribute(owner);
    }
    //@Test // this need to be run in linux
    public void testCreate() throws IOException
    {
        Path base = Paths.get("Z:").toAbsolutePath();
        Path reg = PosixHelp.create(base.resolve("reg"), "-rwxr--r--");
        assertTrue(Files.isRegularFile(reg));
        Path dir = PosixHelp.create(base.resolve("dir"), "drwxr--r--");
        assertTrue(Files.isDirectory(dir));
        Path link = PosixHelp.create(base.resolve("link"), reg, "lrwxr--r--");
        assertTrue(Files.isSymbolicLink(link));
    }
    @Test
    public void testPerm()
    {
        assertEquals("-rwxr--r--", PosixHelp.toString((short)0100744));
        assertEquals("-rwxrwxrwx", PosixHelp.toString((short)0100777));
        assertEquals((short)0100744, PosixHelp.getMode("-rwxr--r--"));
        assertEquals((short)0100777, PosixHelp.getMode("-rwxrwxrwx"));
    }
    @Test
    public void testFileTypes()
    {
        assertEquals("srwxr--r--", PosixHelp.toString((short)0140744));
        assertEquals("lrwxr--r--", PosixHelp.toString((short)0120744));
        assertEquals("brwxr--r--", PosixHelp.toString((short)0060744));
        assertEquals("drwxr--r--", PosixHelp.toString((short)0040744));
        assertEquals("crwxr--r--", PosixHelp.toString((short)0020744));
        assertEquals("prwxr--r--", PosixHelp.toString((short)0010744));
        assertEquals((short)0140744, PosixHelp.getMode("srwxr--r--"));
        assertEquals((short)0120744, PosixHelp.getMode("lrwxr--r--"));
        assertEquals((short)0060744, PosixHelp.getMode("brwxr--r--"));
        assertEquals((short)0040744, PosixHelp.getMode("drwxr--r--"));
        assertEquals((short)0020744, PosixHelp.getMode("crwxr--r--"));
        assertEquals((short)0010744, PosixHelp.getMode("prwxr--r--"));
    }    
    @Test
    public void testSetXId()
    {
        assertEquals("-r-sr-xr-x", PosixHelp.toString((short)0104555));
        assertEquals("-r-xr-sr-x", PosixHelp.toString((short)0102555));
        assertEquals((short)0104555, PosixHelp.getMode("-r-sr-xr-x"));
        assertEquals((short)0102555, PosixHelp.getMode("-r-xr-sr-x"));
    }
    @Test
    public void testSticky()
    {
        assertEquals("-r-xr-xr-t", PosixHelp.toString((short)0101555));
        assertEquals("-r-xr-xr-T", PosixHelp.toString((short)0101554));
        assertEquals((short)0101555, PosixHelp.getMode("-r-xr-xr-t"));
        assertEquals((short)0101554, PosixHelp.getMode("-r-xr-xr-T"));
    }
}
