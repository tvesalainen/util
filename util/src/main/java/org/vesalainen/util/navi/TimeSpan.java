/*
 * Copyright (C) 2011 Timo Vesalainen
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
package org.vesalainen.util.navi;

import org.vesalainen.util.navi.ScalarType;
import org.vesalainen.util.navi.Scalar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeSpan extends Scalar
{
    public static final Pattern FORMAT = Pattern.compile("([0-9]+):([0-9]+):([0-9]+):([0-9]+)");

    public TimeSpan(String str)
    {
        super(ScalarType.TIMESPAN);
        Matcher mm = FORMAT.matcher(str);
        if (mm.matches())
        {
            int d = Integer.parseInt(mm.group(1));
            int h = Integer.parseInt(mm.group(2));
            int m = Integer.parseInt(mm.group(3));
            int s = Integer.parseInt(mm.group(4));
            value = d*24*60*60*1000 + h*60*60*1000 + m*60*1000 + s*1000;
        }
        else
        {
            throw new IllegalArgumentException(str+" is not a TimeSpan!");
        }
    }

    public TimeSpan(long millis)
    {
        super(millis, ScalarType.TIMESPAN);
    }

    public TimeSpan(double millis)
    {
        super(millis, ScalarType.TIMESPAN);
    }

    public TimeSpan(long amount, TimeUnit timeUnit)
    {
        super(timeUnit.toMillis(amount), ScalarType.TIMESPAN);
    }
    
    public TimeSpan(double amount, TimeUnit timeUnit)
    {
        super(timeUnit.toMillis((long) amount), ScalarType.TIMESPAN);
    }
    /**
     *
     * @param from
     * @param to
     */
    public TimeSpan(Date from, Date to)
    {
        super(to.getTime() - from.getTime(), ScalarType.TIMESPAN);
    }

    Date addDate(Date from)
    {
        return new Date(from.getTime() + (long)value);
    }
    
    public long getMillis()
    {
        return (long) value;
    }
    
    public double getSeconds()
    {
        return value / 1000;
    }

    public long get(TimeUnit unit)
    {
        return unit.convert((long)value, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public String toString()
    {
        long tt = (long) getSeconds();
        double sign = Math.signum(tt);
        tt = Math.abs(tt);
        int ss = (int) (tt % 60);
        tt /= 60;
        int mm = (int) (tt % 60);
        tt /= 60;
        int hh = (int) (tt % 60);
        tt /= 24;
        if (sign < 0)
        {
            return String.format("-%d:%02d:%02d:%02d", tt, hh, mm, ss);
        }
        else
        {
            return String.format("%d:%02d:%02d:%02d", tt, hh, mm, ss);
        }
    }

    public static class TimeSpanFormat
    {
        private String format;
        /**
         * Creates formatter for TimeSpan objects.
         * @param frm Format string.
         * d Days
         * hh Hours
         * mm Minutes
         * ss seconds
         * Example d:hh:mm:ss
         */
        public TimeSpanFormat(String frm)
        {
            format = frm.replace("d", "%1$d");
            format = format.replace("hh", "%2$02d");
            format = format.replace("mm", "%3$02d");
            format = format.replace("ss", "%4$02d");
        }

        public final String format(TimeSpan ts)
        {
            long tt = (long) ts.getSeconds();
            double sign = Math.signum(tt);
            tt = Math.abs(tt);
            int ss = (int) (tt % 60);
            tt /= 60;
            int mm = (int) (tt % 60);
            tt /= 60;
            int hh = (int) (tt % 60);
            tt /= 24;
            if (sign < 0)
            {
                return String.format("-"+format, tt, hh, mm, ss);
            }
            else
            {
                return String.format(format, tt, hh, mm, ss);
            }
        }

    }

    public static void main(String... args)
    {
        try
        {
            TimeSpan ss = new TimeSpan(-1, TimeUnit.HOURS);
            TimeSpanFormat tsf = new TimeSpanFormat("d:hh:mm:ss");
            System.err.println(tsf.format(ss));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
