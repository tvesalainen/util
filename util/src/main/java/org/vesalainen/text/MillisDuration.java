/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.Formattable;
import static java.util.FormattableFlags.*;
import java.util.Formatter;
import java.util.Locale;

/**
 * MillisDuration contains default methods to format milli seconds duration.
 * <p>Flag LEFT_JUSTIFY '-' as normal
 * <p>UPPERCASE 'S' as normal
 * <p>ALTERNATE '#' uses longer unit name. With '#' 'seconds' without 's'
 * <p>Width is minimum as also maximum number of characters in output. If 
 * output is less pads with ' '. If output is longer than width fields are
 * dropped until it fits width. Might cause blank output.
 * <p>Precision: If omitted outputs days, hours, minutes, seconds and milli 
 * seconds. 0 outputs days. 1 outputs days and hours. 2 outputs days, hours
 * and minutes. 3 outputs days, hours, minutes and seconds. 4 outputs days, 
 * hours, minutes, seconds and milli seconds.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface MillisDuration extends Formattable
{
    static final long SECONDS = 1000;
    static final long MINUTES = 60*SECONDS;
    static final long HOURS = 60*MINUTES;
    static final long DAYS = 24*HOURS;
    /**
     * Formats milli second duration.
     * <p>Flag LEFT_JUSTIFY '-' as normal
     * <p>UPPERCASE 'S' as normal
     * <p>ALTERNATE '#' uses longer unit name. With '#' 'seconds' without 's'
     * <p>Width is minimum as also maximum number of characters in output. If 
     * output is less pads with ' '. If output is longer than width fields are
     * dropped until it fits width. Might cause blank output.
     * <p>Precision: If omitted outputs days, hours, minutes, seconds and milli 
     * seconds. 0 outputs days. 1 outputs days and hours. 2 outputs days, hours
     * and minutes. 3 outputs days, hours, minutes and seconds. 4 outputs days, 
     * hours, minutes, seconds and milli seconds.
     * @param formatter
     * @param flags
     * @param width
     * @param precision 
     */
    @Override
    default void formatTo(Formatter formatter, int flags, int width, int precision)
    {
        String format = format(flags, width);
        String txt = format(formatter, flags, width, precision);
        formatter.format(format, txt);
    }
    default String format(Formatter formatter, int flags, int width, int precision)
    {
        Locale loc = formatter.locale();
        boolean alt = (flags & ALTERNATE) != 0;
        int limit = width != -1 ? width : Integer.MAX_VALUE;
        int parts = precision != -1 ? precision : Integer.MAX_VALUE;
        StringBuilder sb = new StringBuilder();
        long millis = millis();
        String next = null;
        String delim = "";
        long days = millis / DAYS;
        if (days != 0 || parts == 0)
        {
            if (alt)
            {
                next = String.format(loc, "%d days", days);
            }
            else
            {
                next = String.format(loc, "%d d", days);
            }
            int len = sb.length();
            if (len + next.length() > limit)
            {
                return sb.toString();
            }
            sb.append(next);
            millis -= DAYS*days;
        }
        if (parts >= 1)
        {
            int len = sb.length();
            if (len > 0)
            {
                delim = " ";
            }
            long hours = millis / HOURS;
            if (hours != 0 || len > 0 || parts == 1)
            {
                if (alt)
                {
                    next = String.format(loc, "%s%d hours", delim, hours);
                }
                else
                {
                    next = String.format(loc, "%s%d h", delim, hours);
                }
                if (len + next.length() > limit)
                {
                    return sb.toString();
                }
                sb.append(next);
                millis -= HOURS*hours;
            }
            if (parts >= 2)
            {
                len = sb.length();
                if (len > 0)
                {
                    delim = " ";
                }
                long minutes = millis / MINUTES;
                if (minutes != 0 || len > 0 || parts == 2)
                {
                    if (alt)
                    {
                        next = String.format(loc, "%s%d minutes", delim, minutes);
                    }
                    else
                    {
                        next = String.format(loc, "%s%d m", delim, minutes);
                    }
                    if (len + next.length() > limit)
                    {
                        return sb.toString();
                    }
                    sb.append(next);
                    millis -= MINUTES*minutes;
                }
                if (parts >= 3)
                {
                    len = sb.length();
                    if (len > 0)
                    {
                        delim = " ";
                    }
                    long seconds = millis / SECONDS;
                    if (seconds != 0 || len > 0 || parts == 3)
                    {
                        if (parts >= 4)
                        {
                            millis -= SECONDS*seconds;
                            if (alt)
                            {
                                next = String.format(loc, "%s%d.%03d seconds", delim, seconds, millis);
                            }
                            else
                            {
                                next = String.format(loc, "%s%d.%03d s", delim, seconds, millis);
                            }
                            if (len + next.length() <= limit)
                            {
                                sb.append(next);
                                return sb.toString();
                            }
                        }
                        if (alt)
                        {
                            next = String.format(loc, "%s%d seconds", delim, seconds);
                        }
                        else
                        {
                            next = String.format(loc, "%s%d s", delim, seconds);
                        }
                        if (len + next.length() > limit)
                        {
                            return sb.toString();
                        }
                        sb.append(next);
                    }
                    if (parts >= 4)
                    {
                        len = sb.length();
                        if (len > 0)
                        {
                            delim = " ";
                        }
                        if (alt)
                        {
                            next = String.format(loc, "%s0.%03d seconds", delim, millis);
                        }
                        else
                        {
                            next = String.format(loc, "%s0.%03d s", delim, millis);
                        }
                        if (len + next.length() <= limit)
                        {
                            sb.append(next);
                            return sb.toString();
                        }
                    }
                }
            }
        }
        return sb.toString();
    }
    static String format(int flags, int width)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('%');
        if ((flags & LEFT_JUSTIFY) != 0)
        {
            sb.append('-');
        }
        if (width != -1)
        {
            sb.append(width);
        }
        if ((flags & UPPERCASE) != 0)
        {
            sb.append('S');
        }
        else
        {
            sb.append('s');
        }
        return sb.toString();
    }
    
    long millis();
}
