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
package org.vesalainen.util;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * A formatter utility avoiding String creation
 * @author tkv
 * @see java.util.Formatter
 */
public class ThreadLocalFormatter
{
    private static final ThreadLocal<Formatter> out = new ThreadLocal<>();
    /**
     * 
     * @param out
     * @param locale
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.util.Locale, java.lang.String, java.lang.Object...) 
     */
    public static void format(Appendable out, Locale locale, String format, Object... args)
    {
        try
        {
            Formatter formatter = getFormatter();
            formatter.format(locale, format, args);
            CharSequence cs = (CharSequence) formatter.out();
            out.append(cs);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * 
     * @param out
     * @param format
     * @param args 
     * @see java.util.Formatter#format(java.lang.String, java.lang.Object...) 
     */
    public static void format(Appendable out, String format, Object... args)
    {
        try
        {
            Formatter formatter = getFormatter();
            formatter.format(format, args);
            CharSequence cs = (CharSequence) formatter.out();
            out.append(cs);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * Returns thread-local formatter. Inner Appendable is StringBuider which's
     * length is set to 0.
     * @return 
     */
    public static Formatter getFormatter()
    {
        Formatter formatter = out.get();
        if (formatter == null)
        {
            formatter = new Formatter(new StringBuilder());
            out.set(formatter);
        }
        else
        {
            StringBuilder sb = (StringBuilder) formatter.out();
            sb.setLength(0);
        }
        return formatter;
    }
}
