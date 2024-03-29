/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math.sliding;

import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;

/**
 * DoubleTimeoutSlidingSeries stores series of time/value pairs. It doesn't
 * accept same value twice in row but changes existing time to new one.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleTimeoutSlidingSeries extends DoubleAbstractTimeoutSliding
{
    private final EnterPredicate enterPredicate;

    public DoubleTimeoutSlidingSeries(int initialSize, long timeout)
    {
        this(System::currentTimeMillis, initialSize, timeout, (l)->l, (v1,v2)->true);
    }
    public DoubleTimeoutSlidingSeries(LongSupplier clock, int initialSize, long timeout, LongToDoubleFunction timeConv, EnterPredicate enterPredicate)
    {
        super(clock, initialSize, timeout, timeConv);
        this.enterPredicate = enterPredicate;
    }

    @Override
    public void accept(double value, long time)
    {
        writeLock.lock();
        try
        {
            int prevIndex = (endMod() + size - 2) % size;
            int lastIndex = (endMod() + size - 1) % size;
            if (count() > 1 && enterPredicate.test(value, ring[lastIndex]) && enterPredicate.test(value, ring[prevIndex]))
            {
                times[lastIndex] = time;
            }
            else
            {
                super.accept(value, time);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @Override
    protected void addSample(double td, double value)
    {
    }

    @Override
    protected void removeSample(double xi, double yi)
    {
    }
    
    @FunctionalInterface
    public interface EnterPredicate
    {
        boolean test(double v1, double v2);
    }
}
