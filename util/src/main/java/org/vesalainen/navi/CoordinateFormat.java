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
package org.vesalainen.navi;

import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;
import org.vesalainen.math.UnitType;
import org.vesalainen.util.ThreadLocalFormatter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CoordinateFormat
{
    private static final char[] NS = new char[] {'N', 'S'};
    private static final char[] EW = new char[] {'E', 'W'};
    
    public static String format(Locale locale, double coordinate, UnitType unit)
    {
        StringBuilder out = new StringBuilder();
        switch (unit)
        {
            case COORDINATE_DEGREES_LONGITUDE:
                deg(out, locale, coordinate, EW);
                break;
            case COORDINATE_DEGREES_AND_MINUTES_LONGITUDE:
                degmin(out, locale, coordinate, EW);
                break;
            case COORDINATE_DEGREES_MINUTES_SECONDS_LONGITUDE:
                degminsec(out, locale, coordinate, EW);
                break;
            case COORDINATE_DEGREES_LATITUDE:
                deg(out, locale, coordinate, NS);
                break;
            case COORDINATE_DEGREES_AND_MINUTES_LATITUDE:
                degmin(out, locale, coordinate, NS);
                break;
            case COORDINATE_DEGREES_MINUTES_SECONDS_LATITUDE:
                degminsec(out, locale, coordinate, NS);
                break;
            default:
                throw new UnsupportedOperationException(unit+" no supported");
        }
        return out.toString();
    }
    /**
     * Formats latitude
     * @param latitude
     * @param unit
     * @return 
     */
    public static String formatLatitude(double latitude, UnitType unit)
    {
        return formatLatitude(Locale.getDefault(), latitude, unit);
    }
    /**
     * Formats latitude
     * @param locale
     * @param latitude
     * @param unit
     * @return 
     */
    public static String formatLatitude(Locale locale, double latitude, UnitType unit)
    {
        return format(locale, latitude, unit, NS);
    }
    /**
     * Formats latitude
     * @param out
     * @param latitude
     * @param unit 
     */
    public static void formatLatitude(Appendable out, double latitude, UnitType unit)
    {
        formatLatitude(out, Locale.getDefault(), latitude, unit);
    }
    /**
     * Formats latitude
     * @param out
     * @param locale
     * @param latitude
     * @param unit 
     */
    public static void formatLatitude(Appendable out, Locale locale, double latitude, UnitType unit)
    {
        format(out, locale, latitude, unit, NS);
    }
    /**
     * Formats longitude
     * @param longitude
     * @param unit
     * @return 
     */
    public static String formatLongitude(double longitude, UnitType unit)
    {
        return formatLongitude(Locale.getDefault(), longitude, unit);
    }
    /**
     * Formats longitude
     * @param locale
     * @param longitude
     * @param unit
     * @return 
     */
    public static String formatLongitude(Locale locale, double longitude, UnitType unit)
    {
        return format(locale, longitude, unit, EW);
    }
    /**
     * Formats longitude
     * @param out
     * @param longitude
     * @param unit 
     */
    public static void formatLongitude(Appendable out, double longitude, UnitType unit)
    {
        formatLongitude(out, Locale.getDefault(), longitude, unit);
    }
    /**
     * Formats longitude
     * @param out
     * @param locale
     * @param longitude
     * @param unit 
     */
    public static void formatLongitude(Appendable out, Locale locale, double longitude, UnitType unit)
    {
        format(out, locale, longitude, unit, EW);
    }
    
    public static String format(Locale locale, double coordinate, UnitType unit, char... chars)
    {
        StringBuilder sb = new StringBuilder();
        format(sb, locale, coordinate, unit, chars);
        return sb.toString();
    }
    
    public static void format(Appendable out, Locale locale, double coordinate, UnitType unit, char... chars)
    {
        switch (unit)
        {
            case COORDINATE_DEGREES:
                deg(out, locale, coordinate, chars);
                break;
            case COORDINATE_DEGREES_AND_MINUTES:
                degmin(out, locale, coordinate, chars);
                break;
            case COORDINATE_DEGREES_MINUTES_SECONDS:
                degminsec(out, locale, coordinate, chars);
                break;
            default:
                throw new UnsupportedOperationException(unit+" no supported");
        }
    }
    
    public static void degmin(Appendable out, Locale locale, double value, char... chars)
    {
        try
        {
            char ns = value > 0 ? chars[0] : chars[1];
            double min = Math.abs(value);
            int deg = (int) min;
            min = min-deg;
            Formatter formatter = ThreadLocalFormatter.getFormatter();
            formatter.format(locale,
                    "%d\u00b0%.2f'%c",
                    deg,
                    min*60,
                    ns
            );
            CharSequence cs = (CharSequence) formatter.out();
            out.append(cs);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public static void deg(Appendable out, Locale locale, double value, char... chars)
    {
        try
        {
            char ns = value > 0 ? chars[0] : chars[1];
            double min = Math.abs(value);
            Formatter formatter = ThreadLocalFormatter.getFormatter();
            formatter.format(locale,
                    "%c %.6f\u00b0",
                    ns,
                    min
            );
            CharSequence cs = (CharSequence) formatter.out();
            out.append(cs);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    public static void degminsec(Appendable out, Locale locale, double value, char... chars)
    {
        try
        {
            char ns = value > 0 ? chars[0] : chars[1];
            double m = Math.abs(value);
            int deg = (int) m;
            m = m-deg;
            int min = (int) (m*60);
            double sec = m-(double)min/60.0;
            Formatter formatter = ThreadLocalFormatter.getFormatter();
            formatter.format(locale,
                    "%c %d\u00b0 %d' %.1f\"",
                    ns,
                    deg,
                    min,
                    sec*3600
            );
            CharSequence cs = (CharSequence) formatter.out();
            out.append(cs);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
}
