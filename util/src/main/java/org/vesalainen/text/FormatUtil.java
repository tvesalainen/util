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

import java.util.Locale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FormatUtil
{
    /**
     * Appends value to StringBuilder so that it takes minimum space still
     * preserving precision using '%f' format.
     * <p>Note! Decimal separator is '.'.
     * @param sb
     * @param v 
     */
    public static final void format(StringBuilder sb, double v)
    {
        format(sb, v, "%f");
    }
    /**
     * Appends value to StringBuilder so that it takes minimum space still
     * preserving precision using given decimal format.
     * <p>Note! Decimal separator is '.'.
     * @param sb
     * @param v
     * @param format 
     */
    public static final void format(StringBuilder sb, double v, String format)
    {
        sb.append(String.format(Locale.US, format, v));
        int length = sb.length();
        while (true)
        {
            char cc = sb.charAt(length-1);
            if (cc == '.')
            {
                length--;
                break;
            }
            if (cc != '0')
            {
                break;
            }
            length--;
        }
        sb.setLength(length);
    }
}
