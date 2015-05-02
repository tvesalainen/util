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

/**
 *
 * @author tkv
 */
public class SimpleMatcher implements Matcher
{
    private final byte[] expression;
    private int idx;
    
    public SimpleMatcher(String expr, Charset charset)
    {
        expression = expr.getBytes(charset);
    }

    @Override
    public Status match(int cc)
    {
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
            if (idx == 0)
            {
                idx = 0;
                return Status.Scan;
            }
            else
            {
                idx = 0;
                return Status.Error;
            }
        }
    }
    
}
