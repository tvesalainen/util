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

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TryAdvanceSpliteratorTest
{
    
    public TryAdvanceSpliteratorTest()
    {
    }

    @Test
    public void test1()
    {
        List<Integer> list = CollectionHelp.create(1, 2, 3, 4, 5);
        TryAdvanceSpliterator<Integer> tas = new TryAdvanceSpliterator<>(list.spliterator(), 1, TimeUnit.SECONDS);
        Checker checker = new Checker(1);
        while (tas.tryAdvance((i)->checker.check(i)))
        {
        }
        assertEquals(6, checker.ii);
    }
    @Test
    public void test2()
    {
        List<Integer> list = CollectionHelp.create(1, 2, 3, 4, 5);
        TryAdvanceSpliterator<Integer> tas = new TryAdvanceSpliterator<>(list.spliterator(), 1, TimeUnit.SECONDS);
        Checker checker = new Checker(1);
        while (checker.ii < 3 && tas.tryAdvance((i)->checker.check(i)))
        {
        }
        assertEquals(3, checker.ii);
    }
    static class Checker
    {
        int ii;

        public Checker(int ii)
        {
            this.ii = ii;
        }
        
        void check(int i)
        {
            assertEquals(ii, i);
            ii++;
        }
    }
}
