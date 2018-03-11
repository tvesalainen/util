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
package org.vesalainen.ham.bc;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ham.bc.BroadcastOptimizer.BestStation;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BroadcastOptimizerTest
{
    
    public BroadcastOptimizerTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        BroadcastOptimizer opt = new BroadcastOptimizer();
        BestStation bestStation = opt.bestStation(new Location(9, -79), Instant.now());
        System.err.println(bestStation);
    }
    
}
