/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.util.concurrent;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RefreshableTimerTest
{
    
    public RefreshableTimerTest()
    {
    }

    @Test
    public void test()
    {
        RefreshableTimer timer = new RefreshableTimer();
        long t1 = System.nanoTime();
        timer.wait(1, TimeUnit.SECONDS);
        long t2 = System.nanoTime();
        assertTrue(t2-t1 > TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS));
    }
    
}
