/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.bean;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.ConvertUtility;

/**
 * A parser class for ${key} expressions. Mapper function is used to replace
 * expression with map(key). Expressions can be nested. Inner expressions are
 * naturally resolved first.
 * @author tkv
 */
public class ExpressionParser
{
    private Function<String,String> mapper;
    /**
     * Creator using map
     * @param map 
     */
    public ExpressionParser(Map<String,String> map)
    {
        this((s)->{return map.get(s);});
    }
    public ExpressionParser(Object bean)
    {
        this((s)->{return ExpressionParser.getValue(bean, s);});
    }
    /**
     * Creator using functional interface
     * @param mapper 
     */
    public ExpressionParser(Function<String, String> mapper)
    {
        this.mapper = mapper;
    }
    private static String getValue(Object bean, String property)
    {
        String s = ConvertUtility.convert(String.class, BeanHelper.getValue(bean, property));
        if (s != null)
        {
            return s;
        }
        return "";
    }
    /**
     * Return string where ${key} expressions are replaced.
     * @param text
     * @return 
     */
    public String replace(CharSequence text)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            replace(text, sb);
            return sb.toString();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException();
        }
    }
    /**
     * Appends string where ${key} expressions are replaced.
     * @param text
     * @param out
     * @throws IOException 
     */
    public void replace(CharSequence text, Appendable out) throws IOException
    {
        int idx = CharSequences.indexOf(text, "${");
        if (idx != -1)
        {
            out.append(text.subSequence(0, idx));
            int end = findEnd(text, idx+2);
            String key = replace(text.subSequence(idx+2, end));
            out.append(mapper.apply(key));
            out.append(replace(text.subSequence(end+1, text.length())));
            
        }
        else
        {
            out.append(text);
        }
    }

    private int findEnd(CharSequence text, int start)
    {
        int len = text.length();
        boolean dollar = false;
        int count = 0;
        for (int ii=start;ii<len;ii++)
        {
            switch (text.charAt(ii))
            {
                case '$':
                    dollar = true;
                    break;
                case '{':
                    if (dollar)
                    {
                        count++;
                    }
                    break;
                case '}':
                    if (count == 0)
                    {
                        return ii;
                    }
                    count--;
                    break;
                default:
                    dollar = false;
            }
        }
        throw new IllegalArgumentException(text.subSequence(start, text.length()).toString());
    }
}
