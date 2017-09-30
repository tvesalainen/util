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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.Lists;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Paragraph
{
    private static final int MARGIN = 80;
    private Map<Field,List<String>> fields = new EnumMap<>(Field.class);
    
    public void set(Field field, String value)
    {
        List<String> list = new ArrayList<>();
        fields.put(field, list);
        list.add(value);
    }
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
    public List<String> getList(Field field)
    {
        return fields.get(field);
    }
    public String get(Field field)
    {
        List<String> list = fields.get(field);
        if (list == null)
        {
            throw new IllegalArgumentException(field+" is missing");
        }
        switch (field.getType())
        {
            case SIMPLE:
                return list.stream().collect(Collectors.joining(", "));
            case FOLDED:
            case MULTILINE:
                return list.stream().collect(Collectors.joining(" "));
            default:
                throw new UnsupportedOperationException(field+" not supported");
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
            switch (field.getType())
            {
                case SIMPLE:
                    out.append(list.stream().collect(Collectors.joining(", ", field.toString()+": ", "\n")));
                    break;
                case FOLDED:
                    out.append(fold(field, CharSequences.split(list.stream().collect(Collectors.joining(" ")), (c)->Character.isSpaceChar(c))));
                    out.append('\n');
                    break;
                case MULTILINE:
                    out.append(multiline(field, list.stream().collect(Collectors.joining(" "))));
                    out.append('\n');
                    break;
            }
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private String fold(Field field, Stream<CharSequence> stream)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(field.toString()).append(": ");
        stream.forEach((s)->
        {
            sb.append(s);
            if (linePos(sb) > MARGIN)
            {
                sb.append("\n ");
            }
            else
            {
                sb.append(' ');
            }
        });
        return sb.toString();
    }
    private String multiline(Field field, String s)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(field.toString()).append(": ");
        String r = s.toString().replace("\n\n", "\n.\n");
        while (!r.equals(s))
        {
            s = r;
            r = s.toString().replace("\n\n", "\n.\n");
        }
        sb.append(r.replace("\n", "\n "));
        return sb.toString();
    }
    private int linePos(StringBuilder sb)
    {
        int idx = sb.lastIndexOf("\n");
        if (idx == -1)
        {
            return sb.length();
        }
        else
        {
            return sb.length() - idx;
        }
    }
}
