/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.jmx;

import java.io.IOException;
import java.lang.reflect.Array;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.function.IOConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class OpenTypeAppendable
{
    public static final void append(Appendable out, Class<?> type, Object value) throws IOException
    {
            if (value != null)
            {
                if (!type.isArray())
                {
                    if (CompositeData.class.isAssignableFrom(type))
                    {
                        appendCompositeData(out, (CompositeData) value);
                    }
                    else
                    {
                        if (TabularData.class.isAssignableFrom(type))
                        {
                            appendTabularData(out, (TabularData) value);
                        }
                        else
                        {
                            append(out, value);
                        }
                    }
                }
                else
                {
                    Class<?> componentType = type.getComponentType();
                    embed(out, "div", (o)->
                    {
                        int length = Array.getLength(value);
                        for (int ii=0;ii<length;ii++)
                        {
                            Object v = Array.get(value, ii);
                            append(o, componentType, v);
                        }
                    });
                }
            }
            else
            {
                out.append("<div/>");
            }
    }
    private static void appendCompositeData(Appendable out, CompositeData value) throws IOException
    {
        embed(out, "table", (o)->
        {
            titleRow(o, value.getCompositeType());
            dataRow(o, value);
        });
    }
    private static void appendTabularData(Appendable out, TabularData data) throws IOException
    {
        embed(out, "table", (o)->
        {
            CompositeType prev = null;
            for (Object value : data.values())
            {
                CompositeData cd = (CompositeData) value;
                CompositeType type = cd.getCompositeType();
                if (prev == null || !prev.equals(type))
                {
                    titleRow(o, type);
                    prev = type;
                }
                dataRow(o, cd);
                Object[] ci = data.calculateIndex(cd);
            }
        });
    }

    private static void titleRow(Appendable out, CompositeType type) throws IOException
    {
        embed(out, "tr", (o)->
        {
            for (String key : type.keySet())
            {
                embed(o, "th", (o2)->o2.append(key));
            }
        });
    }
    private static void dataRow(Appendable out, CompositeData data) throws IOException
    {
        CompositeType type = data.getCompositeType();
        embed(out, "tr", (o)->
        {
            for (String key : type.keySet())
            {
                embed(o, "td", (o2)->
                {
                    OpenType<?> ot = type.getType(key);
                    Class<?> cls = Primitives.getClass(ot.getClassName());
                    append(o2, cls, data.get(key));
                });
            }
        });
    }
    private static void append(Appendable out, Object value) throws IOException
    {
        embed(out, "div", (o)->o.append(value.toString()));
    }
    private static void embed(Appendable out, String tag, IOConsumer<Appendable> sub) throws IOException
    {
        out.append('<');
        out.append(tag);
        out.append('>');
        sub.apply(out);
        out.append("</");
        out.append(tag);
        out.append('>');
    }
}
