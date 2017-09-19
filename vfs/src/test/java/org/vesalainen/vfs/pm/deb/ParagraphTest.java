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
package org.vesalainen.vfs.pm.deb;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ParagraphTest
{
    
    public ParagraphTest()
    {
    }

    @Test
    public void testSimple() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        Paragraph p = new Paragraph();
        p.add(Field.DEPENDS, "foo <= 1.2.3", "bar > 3.2.1");
        p.append(sb);
        assertEquals("Depends: foo <= 1.2.3, bar > 3.2.1\n\n", sb.toString());
    }
    @Test
    public void testFolded() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        Paragraph p = new Paragraph();
        String text = "Lorem ipsum dolor sit amet, sollicitudin risus sed nulla, proin wisi cursus duis id, sit vestibulum urna dui morbi, sit placerat augue vel quam convallis. In vehicula morbi suscipit sed rerum dictum, vitae nulla ultricies esse vel quis. Conubia nam, quis a urna volutpat fringilla, eget vel eleifend lacus justo nulla, donec vitae accumsan magna, sociosqu sed sociis pellentesque eu. Nascetur curabitur accumsan ridiculus erat ea suspendisse. Suscipit mus, venenatis vitae aliquip, placerat mattis in leo massa id. Id tempor ultrices gravida varius vel, aliquam animi, faucibus massa.";
        String exp = "Binary: Lorem ipsum dolor sit amet, sollicitudin risus sed nulla, proin wisi cursus\n" +
        " duis id, sit vestibulum urna dui morbi, sit placerat augue vel quam convallis. In\n" +
        " vehicula morbi suscipit sed rerum dictum, vitae nulla ultricies esse vel quis. Conubia\n" +
        " nam, quis a urna volutpat fringilla, eget vel eleifend lacus justo nulla, donec\n" +
        " vitae accumsan magna, sociosqu sed sociis pellentesque eu. Nascetur curabitur accumsan\n" +
        " ridiculus erat ea suspendisse. Suscipit mus, venenatis vitae aliquip, placerat mattis\n" +
        " in leo massa id. Id tempor ultrices gravida varius vel, aliquam animi, faucibus\n" +
        " massa. \n\n";
        p.add(Field.BINARY, text);
        p.append(sb);
        assertEquals(exp, sb.toString());
    }
    @Test
    public void testMultiline() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        Paragraph p = new Paragraph();
        String text = "Lorem ipsum dolor sit amet,\n\nsollicitudin risus\nsed nulla, proin wisi cursus\n";
        String exp = "Description: Lorem ipsum dolor sit amet,\n .\n sollicitudin risus\n sed nulla, proin wisi cursus\n \n\n";
        p.add(Field.DESCRIPTION, text);
        p.append(sb);
        assertEquals(exp, sb.toString());
    }
    
}
