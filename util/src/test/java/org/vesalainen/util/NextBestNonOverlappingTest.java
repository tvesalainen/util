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
package org.vesalainen.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NextBestNonOverlappingTest
{
    
    public NextBestNonOverlappingTest()
    {
    }

    @Test
    public void test1()
    {
        List<R> list = new ArrayList<>();
        list.add(new R(1, 1, 3));
        list.add(new R(2, 2, 4));
        list.add(new R(3, 3, 5));
        list.add(new R(4, 4, 6));
        list.add(new R(3, 6, 8));
        NextBestNonOverlapping<Integer> nbno = new NextBestNonOverlapping(new C());
        R best = (R) nbno.best(list.iterator());
        assertEquals(2, best.num);
    }
    class C implements Comparator<R>
    {

        @Override
        public int compare(R o1, R o2)
        {
            return o1.num - o2.num;
        }
        
    }
    class R extends Range<Integer>
    {
        private int num;

        public R(int num, int from, int to)
        {
            super(from, to);
            this.num = num;
        }

        @Override
        public String toString()
        {
            return "R{" + "num=" + num + '}';
        }
    }
}
