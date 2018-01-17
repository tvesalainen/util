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
package org.vesalainen.ham.hffax;

import org.vesalainen.ham.PatternMatcher;
import org.vesalainen.ham.PatternPredicate;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BWMatcher extends PatternMatcher
{
    private long length;
    private long divider;

    public BWMatcher(long length, long divider, long span, int size)
    {
        super(null, span, size);
        this.predicate = this::test;
        this.length = length;
        this.divider = divider;
    }
    
    private boolean test(boolean isBlack, long now, long startTime)
    {
        long span = (now-startTime)%length;
        if (isBlack)
        {
            return span <= divider;
        }
        else
        {
            return span > divider;
        }
    }
}
