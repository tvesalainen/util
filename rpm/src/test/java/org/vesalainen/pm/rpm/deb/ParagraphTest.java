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
package org.vesalainen.pm.rpm.deb;

import org.vesalainen.pm.rpm.deb.Paragraph;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.pm.rpm.deb.Field.*;

/**
 *
 * @author tkv
 */
public class ParagraphTest
{
    
    public ParagraphTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        Paragraph p = new Paragraph();
        p.add(Package, "test-1.0");
        p.add(Depends, "lsb", "java");
        p.append(System.err);
    }
    
}
