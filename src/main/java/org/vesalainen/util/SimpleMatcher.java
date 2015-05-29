/*
 * Copyright (C) 2015 tkv
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

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A class that matches input with a simple pattern. All characters except '?'
 * and '*' matches as they are. '?' matches matches any single char. '*' matches
 * any number of all characters except the one that follows '*' in pattern.
 * <p>Note that escaping '?' or '*' is not currently implemented!
 * @author tkv
 */
public class SimpleMatcher implements Matcher
{
    private final byte[] expression;
    private int idx;
    
    public SimpleMatcher(String expr, Charset charset)
    {
        if (expr.endsWith("*"))
        {
            throw new IllegalArgumentException(expr+" ending with '*'");
        }
        expression = expr.getBytes(charset);
    }

    @Override
    public Status match(int cc)
    {
        if (expression[idx] == '*')
        {
            if (cc == expression[idx+1])
            {
                idx++;
            }
            else
            {
                return Status.Ok;
            }
        }
        if (expression[idx] == cc || expression[idx] == '?')
        {
            idx++;
            if (idx == expression.length)
            {
                idx = 0;
                return Status.Match;
            }
            else
            {
                return Status.Ok;
            }
        }
        else
        {
            idx = 0;
            return Status.Error;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 71 * hash + Arrays.hashCode(this.expression);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final SimpleMatcher other = (SimpleMatcher) obj;
        if (!Arrays.equals(this.expression, other.expression))
        {
            return false;
        }
        return true;
    }
    
}
