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
package org.vesalainen.ham;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PatternMatcherTest
{
    
    public PatternMatcherTest()
    {
    }

    @Test
    public void test1()
    {
        int err=0;
        PatternMatcher matcher = new PatternMatcher(this::startTest, 500000, 1000);
        for (long t=0;t<500000;t+=1000)
        {
            err = matcher.match(t<=BLACK_LEN, t);
        }
        err = matcher.match(true, 500100);
        assertEquals(0, err);
        assertEquals(0, matcher.getTime());
    }
    
    private static long BLACK_LEN = 500000*2182/2300;
    private boolean startTest(boolean isBlack, long now, long startTime)
    {
        long span = now-startTime;
        if (isBlack)
        {
            return span <= BLACK_LEN;
        }
        else
        {
            return span > BLACK_LEN;
        }
    }
}
