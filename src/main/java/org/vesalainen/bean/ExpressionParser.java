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

import java.util.Map;
import java.util.function.Function;
import org.vesalainen.util.ConvertUtility;
import static org.vesalainen.util.ConvertUtility.convert;

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
    public String replace(String text)
    {
        int idx = text.indexOf("${");
        if (idx != -1)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(text.substring(0, idx));
            int end = findEnd(text, idx+2);
            String key = replace(text.substring(idx+2, end));
            sb.append(mapper.apply(key));
            sb.append(replace(text.substring(end+1)));
            return sb.toString();
            
        }
        else
        {
            return text;
        }
    }

    private int findEnd(String text, int start)
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
        throw new IllegalArgumentException(text.substring(start));
    }
}
