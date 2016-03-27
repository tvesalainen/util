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
package org.vesalainen.navi;

import java.util.Locale;
import org.vesalainen.math.UnitType;

/**
 *
 * @author tkv
 */
public class CoordinateFormat
{
    /**
     * Formats latitude
     * @param latitude
     * @param locale
     * @param unit
     * @return 
     */
    public static String formatLatitude(double latitude, Locale locale, UnitType unit)
    {
        return format(latitude, locale, unit, 'N', 'S');
    }
    /**
     * Formats longitude
     * @param longitude
     * @param locale
     * @param unit
     * @return 
     */
    public static String formatLongitude(double longitude, Locale locale, UnitType unit)
    {
        return format(longitude, locale, unit, 'E', 'W');
    }
    
    public static String format(double coordinate, Locale locale, UnitType unit, char... chars)
    {
        switch (unit)
        {
            case Deg:
                return deg(coordinate, locale, chars);
            case DegMin:
                return degmin(coordinate, locale, chars);
            case DegMinSec:
                return degminsec(coordinate, locale, chars);
            default:
                throw new UnsupportedOperationException(unit+" no supported");
        }
    }
    
    public static String degmin(double value, Locale locale, char... chars)
    {
        char ns = value > 0 ? chars[0] : chars[1];
        double min = Math.abs(value);
        int deg = (int) min;
        min = min-deg;
        return String.format(locale,
                "%c %d\u00b0 %.3f'", 
                ns,
                deg,
                min*60
        );
    }

    public static String deg(double value, Locale locale, char... chars)
    {
        char ns = value > 0 ? chars[0] : chars[1];
        double min = Math.abs(value);
        return String.format(locale,
                "%c %.6f\u00b0", 
                ns,
                min
        );
    }

    public static String degminsec(double value, Locale locale, char... chars)
    {
        char ns = value > 0 ? chars[0] : chars[1];
        double m = Math.abs(value);
        int deg = (int) m;
        m = m-deg;
        int min = (int) (m*60);
        double sec = m-(double)min/60.0;
        return String.format(locale,
                "%c %d\u00b0 %d' %.1f\"", 
                ns,
                deg,
                min,
                sec*3600
        );
    }
}
