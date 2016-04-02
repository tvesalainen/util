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

/**
 *
 * @author tkv
 */
public class CharSequences
{
    public static int indexOf(CharSequence seq, CharSequence pattern)
    {
        return indexOf(seq, pattern, 0);
    }
    public static int indexOf(CharSequence seq, CharSequence pattern, int fromIndex)
    {
        int len = seq.length();
        int pi=0;
        int pl=pattern.length();
        int ll = len-pl;
        for (int ii=fromIndex;ii<len;ii++)
        {
            if (seq.charAt(ii) == pattern.charAt(pi))
            {
                pi++;
                if (pi == pl)
                {
                    return ii-pl+1;
                }
            }
            else
            {
                if (ii > ll)
                {
                    return -1;
                }
                pi=0;
            }
        }
        return -1;
    }
    public static boolean equals(CharSequence cs1, CharSequence cs2)
    {
        if (cs1 == cs2)
        {
            return true;
        }
        if (cs1.length() != cs2.length())
        {
            return false;
        }
        int len = cs1.length();
        for (int ii=0;ii<len;ii++)
        {
            if (cs1.charAt(ii) != cs2.charAt(ii))
            {
                return false;
            }
        }
        return true;
    }
}
