/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ui;

import java.awt.Point;
import java.awt.geom.Point2D;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.ui.ScanlineFiller.PointQueue;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PointQueueTest
{
    
    public PointQueueTest()
    {
    }

    @Test
    public void test1()
    {
        Point exp1 = new Point(0, 1);
        Point exp2 = new Point(2, 3);
        Point exp3 = new Point(4, 5);
        Point exp4 = new Point(6, 7);
        Point exp5 = new Point(8, 9);
        Point exp6 = new Point(10, 11);
        Point got = new Point();
        PointQueue pq = new PointQueue(4, got::move);
        pq.add(exp1);
        pq.add(exp2);
        pq.add(exp3);
        pq.add(exp4);
        pq.add(exp5);
        pq.add(exp6);
        
        pq.take();
        assertEquals(exp1, got);
        
        pq.take();
        assertEquals(exp2, got);
        
        pq.take();
        assertEquals(exp3, got);
        
        pq.take();
        assertEquals(exp4, got);
        
        pq.take();
        assertEquals(exp5, got);
        
        pq.take();
        assertEquals(exp6, got);
    }
    @Test
    public void test2()
    {
        Point exp1 = new Point(0, 1);
        Point exp2 = new Point(2, 3);
        Point exp3 = new Point(4, 5);
        Point exp4 = new Point(6, 7);
        Point exp5 = new Point(8, 9);
        Point exp6 = new Point(10, 11);
        Point got = new Point();
        PointQueue pq = new PointQueue(4, got::move);
        pq.add(exp1);
        pq.add(exp2);

        pq.take();
        assertEquals(exp1, got);
        
        pq.add(exp3);
        pq.add(exp4);
        pq.add(exp5);
        pq.add(exp6);
        
        pq.take();
        assertEquals(exp2, got);
        
        pq.take();
        assertEquals(exp3, got);
        
        pq.take();
        assertEquals(exp4, got);
        
        pq.take();
        assertEquals(exp5, got);
        
        pq.take();
        assertEquals(exp6, got);
    }
    
}
