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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.ConvertUtility;
import org.vesalainen.util.Lists;

/**
 * A parser class for ${key} expressions. A list of mapper function are used to replace
 * expression with map(key). Constructor mapper is tried first, then the first
 * added mapper etc.
 * Expressions can be nested. Inner expressions are
 * naturally resolved first.
 * @author tkv
 */
public class ExpressionParser
{
    private List<Function<String,String>> mapperList = new ArrayList<>();
    /**
     * Creator using map
     * @param map 
     */
    public ExpressionParser(Map<String,String> map)
    {
        this((s)->map.get(s));
    }
    /**
     * Creates ExpressionParser using bean-object as mapping. Expression ${key}
     * maps into method call o.getKey()
     * @param bean 
     */
    public ExpressionParser(Object bean)
    {
        this((s)->ExpressionParser.getValue(bean, s));
    }
    /**
     * Creator using functional interface
     * @param mapper 
     */
    public ExpressionParser(Function<String, String>... mapper)
    {
        this.mapperList = Lists.create(mapper);
    }
    /**
     * Add mapper to mapper list.
     * @param map
     * @return 
     */
    public ExpressionParser addMapper(Map<String,String> map)
    {
        mapperList.add((s)->map.get(s));
        return this;
    }
    /**
     * Add mapper to mapper list.
     * @param bean
     * @return 
     */
    public ExpressionParser addMapper(Object bean)
    {
        mapperList.add((s)->ExpressionParser.getValue(bean, s));
        return this;
    }
    /**
     * Add mapper to mapper list.
     * @param mapper
     * @return 
     */
    public ExpressionParser addMapper(Function<String, String> mapper)
    {
        mapperList.add(mapper);
        return this;
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
        if (text == null)
        {
            return null;
        }
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
            out.append(map(key));
            out.append(replace(text.subSequence(end+1, text.length())));
            
        }
        else
        {
            out.append(text);
        }
    }

    private String map(String key)
    {
        for (Function<String, String> mapper : mapperList)
        {
            try
            {
                String value = mapper.apply(key);
                if (value != null)
                {
                    return value;
                }
            }
            catch (Exception ex)
            {
                
            }
        }
        return key;
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
