/*
 * COPYRIGHT (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public LICENSE as published by
 * the Free Software Foundation, either version 3 of the LICENSE, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public LICENSE for more details.
 *
 * You should have received a copy of the GNU General Public LICENSE
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.vfs.pm.deb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.vesalainen.util.Lists;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Paragraph
{
    private Map<Field,List<String>> fields = new EnumMap<>(Field.class);
    
    public void add(Field field, String... values)
    {
        List<String> list = fields.get(field);
        if (list == null)
        {
            list = new ArrayList<>();
            fields.put(field, list);
        }
        Lists.addAll(list, values);
    }
    public void append(Appendable out) throws IOException
    {
        fields.forEach((f,l)->append(out,f,l));
        out.append('\n');
    }
    private void append(Appendable out, Field field, List<String> list)
    {
        try
        {
            out.append(list.stream().collect(Collectors.joining(", ", field.toString()+": ", "\n")));
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
