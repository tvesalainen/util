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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.ham.hffax.FaxTone.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LineCorrectorTest
{
    
    public LineCorrectorTest()
    {
    }

    @Test
    public void test()
    {
        LineCorrector lc = new LineCorrector(this::tone);
        long time = 0;
        long span = 100000;
        lc.tone(BLACK, time, time+span, span, 0);
        time += span;
        span = 400;
        lc.tone(LOW, time, time+span, span, 0);
        time += span;
        span = 500000-time;
        lc.tone(BLACK, time, time+span, span, 0);
        time += span;
        span = 50000;
        lc.tone(WHITE, time, time+span, span, 0);
        time += span;
        span = 20;
        lc.tone(HIGH, time, time+span, span, 0);
        time += span;
        span = 100000-20-50000;
        lc.tone(WHITE, time, time+span, span, 0);
        time += span;
        span = 100000;
        lc.tone(BLACK, time, time+span, span, 0);
    }
    
    public void tone(FaxTone tone, long begin, long end, long span, float amplitude)
    {
        switch (tone)
        {
            case BLACK:
                assertEquals(500000, span);
                break;
            case WHITE:
                assertEquals(100000, span);
                break;
        }
    }
}
