/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author tkv
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
            _value = d*24*60*60*1000 + h*60*60*1000 + m*60*1000 + s*1000;
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

    public Date addDate(Date from)
    {
        return new Date(from.getTime() + (long)_value);
    }
    
    public long getMillis()
    {
        return (long) _value;
    }
    
    public double getSeconds()
    {
        return _value / 1000;
    }

    public long get(TimeUnit unit)
    {
        return unit.convert((long)_value, TimeUnit.MILLISECONDS);
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
