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
package org.vesalainen.pm.rpm;

import org.vesalainen.pm.rpm.HeaderTag;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class HeaderTagTest
{
    
    public HeaderTagTest()
    {
    }

    @Test
    public void testIntegrity()
    {
        Set<Integer> setI = new HashSet<>();
        Set<Integer> setH = new HashSet<>();
        for (HeaderTag tag : HeaderTag.values())
        {
            Boolean signature = tag.isSignature();
            if (signature != null && signature)
            {
                boolean newMember = setI.add(tag.getTagValue());
                assertTrue(tag+" conflict", newMember);
            }
            else
            {
                boolean newMember = setH.add(tag.getTagValue());
                assertTrue(tag+" conflict", newMember);
            }
            assertEquals(tag, HeaderTag.valueOf(tag.getTagValue(), tag.isSignature()));
        }
    }
    
}
