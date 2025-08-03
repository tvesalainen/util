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
package org.vesalainen.util;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import org.vesalainen.text.MillisDuration;

/**
 * RepeatSuppressor purpose is to suppress repetitive actions like log entries.
 * Gaps determine after which time gaps repetitive action is forwarded.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RepeatSuppressor<T>
{
    @FunctionalInterface
    public interface Forwarder<T>
    {
        void forward(int count, long time, MillisDuration formattable, T item);
    }
    private final TimeToLiveMap<T,Entry> ttlMap;
    private final LongSupplier millis;
    private final long[] gaps;
    private final Forwarder forwarder;
    /**
     * Creates new RepeatSuppressor
     * @param forwarder
     * @param gaps In milli seconds
     */
    public RepeatSuppressor(Forwarder<T> forwarder, long... gaps)
    {
        this(System::currentTimeMillis, forwarder, gaps);
    }
    /**
     * Creates new RepeatSuppressor
     * @param millis
     * @param forwarder
     * @param gaps In milli seconds
     */
    public RepeatSuppressor(LongSupplier millis, Forwarder<T> forwarder, long... gaps)
    {
        this.millis = millis;
        this.ttlMap = new TimeToLiveMap<>(millis, 1, TimeUnit.MINUTES, this::remove);
        this.forwarder = forwarder;
        this.gaps = gaps;
    }
    /**
     * Forwards item according to gaps sequence. First forward goes always through.
     * Followings go through only after next gap milli seconds has gone.
     * @param item 
     */
    public void forward(T item)
    {
        if (ttlMap.containsKey(item))
        {
            ttlMap.get(item).forward(item);
        }
        else
        {
            Entry entry = new Entry();
            entry.forward(item);
        }
    }
    private void remove(T item, Entry entry)
    {
        long now = millis.getAsLong();
        forwarder.forward(entry.count, now-entry.begin, entry, item);
    }
    private class Entry implements MillisDuration
    {
        private final TimeLimitIterator iterator;
        private final long begin;
        private long limit;
        private int count;

        private Entry()
        {
            this.begin = millis.getAsLong();
            this.iterator = new TimeLimitIterator(()->begin, gaps);
        }
        private void forward(T item)
        {
            count++;
            long now = millis.getAsLong();
            if (now >= limit)
            {
                forwarder.forward(count, now-begin, this, item);
                limit = iterator.nextLong();
                ttlMap.put(item, this, limit, TimeUnit.MILLISECONDS);
            }
        }

        @Override
        public long millis()
        {
            return millis.getAsLong()-begin;
        }
    }
}
