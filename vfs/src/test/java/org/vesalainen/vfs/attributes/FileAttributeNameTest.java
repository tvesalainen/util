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
package org.vesalainen.vfs.attributes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.vfs.attributes.FileAttributeName.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileAttributeNameTest
{
    
    public FileAttributeNameTest()
    {
    }

    @Test
    public void testImpliesSet()
    {
        String[] expArr = new String[] {BASIC_VIEW, OWNER_VIEW, POSIX_VIEW, UNIX_VIEW};
        Set<String> exp = Arrays.stream(expArr).collect(Collectors.toSet());
        Set<String> impliedSet = FileAttributeName.impliedSet(BASIC_VIEW, UNIX_VIEW);
        assertEquals(exp, impliedSet);
    }
    @Test
    public void testTopViews1()
    {
        String[] expArr = new String[] {UNIX_VIEW};
        Set<String> exp = Arrays.stream(expArr).collect(Collectors.toSet());
        Set<String> topViews = FileAttributeName.topViews(BASIC_VIEW, UNIX_VIEW);
        assertEquals(exp, topViews);
    }
    @Test
    public void testTopViews2()
    {
        String[] expArr = new String[] {UNIX_VIEW, DOS_VIEW, ACL_VIEW};
        Set<String> exp = Arrays.stream(expArr).collect(Collectors.toSet());
        Set<String> topViews = FileAttributeName.topViews(BASIC_VIEW, POSIX_VIEW, UNIX_VIEW, DOS_VIEW, ACL_VIEW);
        assertEquals(exp, topViews);
    }
    
}
