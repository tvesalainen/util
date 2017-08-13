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
package org.vesalainen.pm.deb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author tkv
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
        for (String v : values)
        {
            list.add(v);
        }
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
            out.append(list.stream().collect(Collectors.joining(", ", field.name().replace('_', '-')+": ", "\n")));
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
