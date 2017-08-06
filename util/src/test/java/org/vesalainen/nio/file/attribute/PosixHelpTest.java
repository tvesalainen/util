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
