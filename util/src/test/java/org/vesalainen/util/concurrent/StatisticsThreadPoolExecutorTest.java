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
package org.vesalainen.util.concurrent;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StatisticsThreadPoolExecutorTest
{
    
    public StatisticsThreadPoolExecutorTest()
    {
    }

    @Test
    public void testSomeMethod()
    {
        StatisticsThreadPoolExecutor stpe = new StatisticsThreadPoolExecutor(10, 10, 1, TimeUnit.DAYS, new SynchronousQueue<>(), 10, TimeUnit.MINUTES);
        stpe.test("Tag1", "v1", 2000);
        stpe.test("Tag1", "v1", 1000);
        stpe.test("Tag1", "v1", 3000);
        stpe.test("Tag1", "v2", 3000);
        stpe.test("Tag1", "v2", 9000);
        stpe.test("Tag2", "v1", 2000);
        stpe.test("Tag2", "v1", 1000);
        stpe.test("Tag2", "v1", 3000);
        stpe.test("Tag2", "v2", 3000);
        stpe.test("Tag2", "v2", 9000);
        System.err.println(stpe.printStatistics());
    }
    
}
