/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.itshfbc;

import java.util.Collection;
import java.util.Locale;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.navi.Location;

/**
 * Parses and formats locations in form 60.1N25.2E
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LocationFormatter
{
    public static final Location[] parse(Collection<String> list)
    {
        Location[] arr = new Location[list.size()];
        int index = 0;
        for (String loc : list)
        {
            arr[index++] = parse(loc);
        }
        return arr;
    }
    public static final Location parse(CharSequence text)
    {
        int idx1 = CharSequences.indexOf(text, (c)->c=='N'||c=='S');
        if (idx1 == -1)
        {
            throw new IllegalArgumentException(text.toString());
        }
        double lat = Primitives.parseDouble(text, 0, idx1);
        if (text.charAt(idx1) == 'S')
        {
            lat = -lat;
        }
        int idx2 = CharSequences.indexOf(text, (c)->c=='W'||c=='E', idx1+1);
        if (idx2 == -1)
        {
            throw new IllegalArgumentException(text.toString());
        }
        double lon = Primitives.parseDouble(text, idx1+1, idx2);
        if (text.charAt(idx2) == 'W')
        {
            lon = -lon;
        }
        return new Location(lat, lon);
    }
    public static final String format(Location location)
    {
        return String.format(Locale.US, "%s%s%s%s", fmt(location.getLatitude()), location.getLatitudeNS(), fmt(location.getLongitude()), location.getLongitudeWE());
    }
    private static String fmt(double d)
    {
        String str = String.format(Locale.US, "%.6f", Math.abs(d));
        while (str.endsWith("0"))
        {
            str = str.substring(0, str.length()-1);
        }
        if (str.endsWith("."))
        {
            return str.substring(0, str.length()-1);
        }
        else
        {
            return str;
        }
    }
}
