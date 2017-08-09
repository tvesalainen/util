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
package org.vesalainen.rpm.deb;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.vesalainen.util.Lists;

/**
 *
 * @author tkv
 */
public class ControlBase
{
    
    protected Path dir;
    protected String name;
    protected List<Paragraph> paragraphs;

    protected ControlBase(Path dir, String name, Paragraph... paragraphs)
    {
        this.dir = dir;
        this.name = name;
        this.paragraphs = Lists.create(paragraphs);
    }

    void save() throws IOException
    {
        Path control = dir.resolve(name);
        Files.createDirectories(dir);
        try (final BufferedWriter bw = Files.newBufferedWriter(control, StandardCharsets.UTF_8))
        {
            for (Paragraph p : paragraphs)
            {
                p.append(bw);
            }
        }
    }
    
}
