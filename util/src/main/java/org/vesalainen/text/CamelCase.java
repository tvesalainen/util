/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.text;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Helper class for camel-case tokens
 * <p>Camel Case Token is delimitedLower by non alphabet or case change
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CamelCase
{
    /**
     * Return CamelCase
     * @param text
     * @return 
     */
    public static final String camelCase(String text)
    {
        return stream(text).collect(Collectors.joining());
    }
    /**
     * Returns Camel Case
     * @param text
     * @return 
     */
    public static final String title(String text)
    {
        return stream(text).collect(Collectors.joining(" "));
    }
    /**
     * Return camelCase
     * @param text
     * @return 
     */
    public static final String property(String text)
    {
        CamelSpliterator cs = new CamelSpliterator(text);
        StringBuilder sb = new StringBuilder();
        cs.tryAdvance((s)->sb.append(lower(s)));
        StreamSupport.stream(cs, false).forEach((s)->sb.append(s));
        return sb.toString();
    }
    /**
     * Return camel-case if delim = '-'
     * @param text
     * @param delim
     * @return 
     */
    public static final String delimitedLower(String text, String delim)
    {
        return stream(text).map((String s)->{return s.toLowerCase();}).collect(Collectors.joining(delim));
    }
    /**
     * Return CAMEL_CASE if delim = '_'
     * @param text
     * @param delim
     * @return 
     */
    public static final String delimitedUpper(String text, String delim)
    {
        return stream(text).map((String s)->{return s.toUpperCase();}).collect(Collectors.joining(delim));
    }
    /**
     * Returns Camel-Case if delim = '-'
     * @param text
     * @param delim
     * @return 
     */
    public static final String delimited(String text, String delim)
    {
        return stream(text).collect(Collectors.joining(delim));
    }
    private static String lower(String text)
    {
        if (text.isEmpty())
        {
            return text;
        }
        if (text.length() > 1)
        {
            return Character.toLowerCase(text.charAt(0))+text.substring(1);
        }
        else
        {
            return text.toLowerCase();
        }
    }
    private static String upper(String text)
    {
        if (text.isEmpty())
        {
            return text;
        }
        if (text.length() > 1)
        {
            return Character.toUpperCase(text.charAt(0))+text.substring(1).toLowerCase();
        }
        else
        {
            return text.toUpperCase();
        }
    }
    public static Stream<String> stream(String text)
    {
        return StreamSupport.stream(new CamelSpliterator(text), false);
    }
    private static class CamelSpliterator implements Spliterator<String>
    {
        private String text;
        private int length;
        private int index;

        public CamelSpliterator(String text)
        {
            this.text = text;
            this.length = text.length();
        }
        
        @Override
        public boolean tryAdvance(Consumer<? super String> action)
        {
            while (index < length && !Character.isLetterOrDigit(text.charAt(index)))
            {
                index++;
            }
            if (index == length)
            {
                return false;
            }
            int start = index;
            while (index < length && Character.isLetterOrDigit(text.charAt(index)))
            {
                if (index - start > 0)
                {
                    if (Character.isUpperCase(text.charAt(index)) && Character.isLowerCase(text.charAt(index-1)))
                    {
                        action.accept(upper(text.substring(start, index)));
                        return true;
                    }
                }
                index++;
            }
            if (index > start)
            {
                action.accept(upper(text.substring(start, index)));
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<String> trySplit()
        {
            return null;
        }

        @Override
        public long estimateSize()
        {
            return text.length() / 6;
        }

        @Override
        public int characteristics()
        {
            return 0;
        }
        
    }
}
