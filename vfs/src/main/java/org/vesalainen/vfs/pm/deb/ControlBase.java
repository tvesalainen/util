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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vesalainen.util.Lists;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ControlBase
{
    
    protected Path debian;
    protected String name;
    protected List<Paragraph> paragraphs;

    protected ControlBase(String name, Paragraph... paragraphs)
    {
        this.debian = debian;
        this.name = name;
        this.paragraphs = Lists.create(paragraphs);
    }

    public ControlBase(Path debian, String name) throws IOException
    {
        this.debian = debian;
        this.name = name;
        this.paragraphs = new ArrayList<>();
        Paragraph paragraph = new  Paragraph();
        paragraphs.add(paragraph);
        Path control = debian.resolve(name);
        if (Files.exists(control))
        {
            List<String> lines = Files.readAllLines(control, UTF_8);
            Field field = null;
            StringBuilder sb = new StringBuilder();
            for (String line : lines)
            {
                char cc = line.charAt(0);
                if (cc == ' ' || cc == '\t')
                {
                    if (line.charAt(1) == '.')
                    {
                        sb.append("\n");
                    }
                    else
                    {
                        sb.append(line);
                    }
                }
                else
                {
                    if (field != null)
                    {
                        paragraph.add(field, sb.toString().split("[, ]+"));
                        field = null;
                        sb.setLength(0);
                    }
                    int idx = line.indexOf(':');
                    if (idx == -1)
                    {
                        throw new IllegalArgumentException(line);
                    }
                    field = Field.get(line.substring(0, idx));
                    sb.append(line.substring(idx+1).trim());
                }
                if (field != null)
                {
                    paragraph.add(field, sb.toString().split("[, ]+"));
                    field = null;
                    sb.setLength(0);
                }
            }
        }
    }

    void save(Path debian) throws IOException
    {
        Path control = debian.resolve(name);
        Files.createDirectories(debian);
        try (final BufferedWriter bw = Files.newBufferedWriter(control, UTF_8))
        {
            for (Paragraph p : paragraphs)
            {
                p.append(bw);
            }
        }
    }
    
}
