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
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.util.Lists;
import static org.vesalainen.vfs.pm.deb.FieldType.*;

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
            if (!lines.isEmpty() && checkFirstLine(lines.get(0)))
            {
                Field field = null;
                StringBuilder sb = new StringBuilder();
                for (String line : lines)
                {
                    if (line.isEmpty())
                    {
                        paragraph = new  Paragraph();
                        paragraphs.add(paragraph);
                    }
                    else
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
                                sb.append('\n'+line.trim());
                            }
                        }
                        else
                        {
                            Field fld = extractField(line);
                            if (fld != null)
                            {
                                if (field != null)
                                {
                                    if (field.getType() == SIMPLE)
                                    {
                                        paragraph.add(field, sb.toString().split("[, ]+"));
                                    }
                                    else
                                    {
                                        paragraph.add(field, sb.toString());
                                    }
                                }
                                field = fld;
                                sb.setLength(0);
                                int idx = line.indexOf(':');
                                sb.append(line.substring(idx+1).trim());
                            }
                        }
                    }
                }
                if (field != null)
                {
                    if (field.getType() == SIMPLE)
                    {
                        paragraph.add(field, sb.toString().split("[, ]+"));
                    }
                    else
                    {
                        paragraph.add(field, sb.toString());
                    }
                }
            }
        }
    }
    private Field extractField(String line)
    {
        int idx = line.indexOf(':');
        if (idx == -1)
        {
            return null;
        }
        try
        {
            return Field.get(line.substring(0, idx));
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }
    protected boolean checkFirstLine(String line)
    {
        return true;
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
