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
package org.vesalainen.vfs.unix;

import java.io.IOException;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.vesalainen.vfs.attributes.BasicFileAttributeViewImpl;
import org.vesalainen.vfs.attributes.PosixFileAttributeViewImpl;
import static org.junit.Assert.*;
import org.vesalainen.vfs.FileAttributeAccessStore;
import org.vesalainen.vfs.attributes.FileAttributeAccess;
import org.vesalainen.vfs.attributes.FileAttributeName;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class UnixFileAttributeViewImplTest
{
    
    public UnixFileAttributeViewImplTest()
    {
    }

    @Test
    public void testNames() throws IOException
    {
        assertEquals("basic", new BasicFileAttributeViewImpl(null).name());
        assertEquals("posix", new PosixFileAttributeViewImpl(null).name());
    }
    @Test
    public void testDefault() throws IOException
    {
        Map<Name,Object> map = new HashMap<>();
        UnixFileAttributeViewImpl u = new UnixFileAttributeViewImpl(new FileAttributeAccessStore(map));
        assertEquals(UNIX_VIEW, u.name());
        assertFalse(u.setGroupId());
        assertFalse(u.stickyBit());
        PosixFileAttributes p = u.readAttributes();
        assertEquals(Collections.EMPTY_SET, p.permissions());
        assertNull(p.group());
    }
    @Test
    public void testMode() throws IOException
    {
        Map<Name,Object> map = new HashMap<>();
        map.put(FileAttributeName.getInstance(IS_SYMBOLIC_LINK), true);
        UnixFileAttributeViewImpl u = new UnixFileAttributeViewImpl(new FileAttributeAccessStore(map));
        String m = "lrwsrwsrwt";
        u.mode(m);
        assertEquals(m, u.modeString());
        assertTrue(u.setUserId());
        assertTrue(u.setGroupId());
        assertTrue(u.stickyBit());
    }
}
