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
package org.vesalainen.vfs.arch.tar;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
import org.vesalainen.vfs.unix.UnixFileAttributes;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TARCPIOTestBase
{
    
    public TARCPIOTestBase()
    {
    }

    protected final void testFileSystem(FileSystem fs) throws IOException
    {
        Path smallhard = fs.getPath("smallhard");
        Path smallsymbolic = fs.getPath("smallsymbolic");
        Path smalltxt = fs.getPath("small.txt");
        assertEquals(26, Files.size(smallhard));
        assertEquals(26, Files.size(smalltxt));
        assertTrue(Files.isSameFile(smallhard, smalltxt));
        Set<PosixFilePermission> expPerm = PosixFilePermissions.fromString("rw-r--r--");
        assertEquals(expPerm, Files.getPosixFilePermissions(smalltxt));
        assertTrue(Files.isSymbolicLink(smallsymbolic));
        assertTrue(Files.size(smallsymbolic) > 0);
        assertEquals(smalltxt, Files.readSymbolicLink(smallsymbolic));
        // attributes
        UnixFileAttributes unixAttrs = Files.readAttributes(smalltxt, UnixFileAttributes.class);
        assertNotNull(unixAttrs);
        assertFalse(unixAttrs.setUserId());
        assertFalse(unixAttrs.setGroupId());
        assertEquals("-rw-r--r--", unixAttrs.modeString());
    }

    protected final void testUserAndGroup(FileSystem fs) throws IOException
    {
        Path smalltxt = fs.getPath("small.txt");
        UnixFileAttributes unixAttrs = Files.readAttributes(smalltxt, UnixFileAttributes.class);
        UserPrincipalLookupService upls = fs.getUserPrincipalLookupService();
        assertNotNull(upls);
        UserPrincipal expUser = upls.lookupPrincipalByName("pi");
        GroupPrincipal expGroup = upls.lookupPrincipalByGroupName("pi");
        assertEquals(expUser.getName(), unixAttrs.owner().getName());
        assertEquals(expGroup.getName(), unixAttrs.group().getName());
    }

    protected final void testLongNames(FileSystem fs) throws IOException
    {
        Path filetxt = fs.getPath("file.txt");
        Path hard = fs.getPath("hard");
        Path longName = fs.getPath("very-long-name-exceeds-all-limitations-altogether");
        Path hardPath = longName.resolve(hard);
        Path filePath = longName.resolve(longName).resolve(longName).resolve(filetxt);
        assertTrue(Files.exists(filePath));
        assertEquals(90737, Files.size(filePath));
        assertTrue(Files.isSameFile(hardPath, filePath));
        UnixFileAttributes unixAttrs = Files.readAttributes(filePath, UnixFileAttributes.class);
        assertNotNull(unixAttrs);
        assertTrue(unixAttrs.setUserId());
        assertFalse(unixAttrs.setGroupId());
        assertEquals("-rws--x--x", unixAttrs.modeString());
    }
    
}
