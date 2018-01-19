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

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxSynchronizerTest
{
    protected static long LINE_LEN = 500100;
    protected static long START_OF_LINE = 1000000;
    protected static long START_BLACK_LEN = LINE_LEN*2182/2300;
    protected static long STOP_LEN = LINE_LEN/225;
    protected static long STOP_BLACK_LEN = 7*STOP_LEN/20;
    protected static long STEP = 1000000/2300;
    
    public FaxSynchronizerTest()
    {
    }

    @Test
    public void testCorrection()
    {
        assertEquals(1000000, FaxSynchronizer.correct(1000000, 1500000));
        assertEquals(1000001, FaxSynchronizer.correct(1000000, 1500001));
        assertEquals(999999, FaxSynchronizer.correct(1000000, 1499999));
    }
    //@Test
    public void testSimple()
    {
        StateList sl = new StateList();
        FaxSynchronizer fs = new SimpleSynchronizer(sl);
        fax(fs);
        assertEquals(START_OF_LINE, sl.getLocator().getStartOfLine());
    }
    //@Test
    public void testAdjusting()
    {
        StateList sl = new StateList();
        FaxSynchronizer fs = new AdjustingSynchronizer(sl);
        fax(fs);
        assertEquals(LINE_LEN, sl.getLocator().getLineLength());
        assertEquals(START_OF_LINE, sl.getLocator().getStartOfLine());
    }
    
    private void fax(FaxSynchronizer s)
    {
        FreqGen fg = new FreqGen(40);
        long time = START_OF_LINE;
        for (int ll=0;ll<60;ll++)
        {
            time = gen(s, fg, time, true, START_BLACK_LEN);
            time = gen(s, fg, time, false, LINE_LEN - START_BLACK_LEN);
        }
        
    }
    private long gen(FaxSynchronizer s, FreqGen fg, long time, boolean black, long len)
    {
        long timeout = time+len;
        while (time < timeout)
        {
            s.frequency(fg.getTone(black), time);
            time += STEP;
        }
        return time;
    }
    class FreqGen
    {
        private Random random = new Random(123456);
        private float radius;

        public FreqGen(float r)
        {
            this.radius = r;
        }
        public float getTone(boolean black)
        {
            return black ? getFreq(1500) : getFreq(2300);
        }
        public float getFreq(float f)
        {
            float next = random.nextFloat();
            return f + (radius*(next-0.5F));
        }
    }
    class StateList implements FaxStateListener
    {
        PageLocator locator;
        @Override
        public void start(PageLocator locator)
        {
            this.locator = locator;
            System.err.println("start");
        }

        @Override
        public void stop()
        {
            System.err.println("stop");
        }

        public PageLocator getLocator()
        {
            return locator;
        }
        
    }

}
