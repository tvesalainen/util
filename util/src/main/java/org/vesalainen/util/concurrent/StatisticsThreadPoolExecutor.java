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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;
import org.vesalainen.math.sliding.TimeoutSlidingStats;

/**
 * StatisticsThreadPoolExecutorgather statistics from TaggabeThread's
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class StatisticsThreadPoolExecutor extends TaggableThreadPoolExecutor
{
    protected long slideMillis;
    protected long startMillis;
    protected Map<Object,Map<Object,TimeoutSlidingStats>> map = new ConcurrentHashMap<>();
    
    public StatisticsThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, long slideTime, TimeUnit slidingUnit)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.slideMillis = slidingUnit.toMillis(slideTime);
        this.startMillis = System.currentTimeMillis();
    }

    public StatisticsThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler, long slideTime, TimeUnit slidingUnit)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.slideMillis = slidingUnit.toMillis(slideTime);
        this.startMillis = System.currentTimeMillis();
    }

    @Override
    protected void afterExecute(Map<Object,Object> tags, long elapsed, Throwable t)
    {
        for (Entry<Object,Object> e : tags.entrySet())
        {
            Object key = e.getKey();
            Object value = e.getValue();
            Map<Object,TimeoutSlidingStats> statsMap = map.get(key);
            if (statsMap == null)
            {
                statsMap = new ConcurrentHashMap<>();
                map.put(key, statsMap);
            }
            TimeoutSlidingStats stats = statsMap.get(value);
            if (stats == null)
            {
                stats = new TimeoutSlidingStats((int) (slideMillis/1000), slideMillis);
                statsMap.put(value, stats);
            }
            stats.accept(elapsed/1000);
        }
    }

    void test(Object key, Object value, long elapsed)
    {
        Map<Object,Object> m = new HashMap<>();
        m.put(key, value);
        afterExecute(m, elapsed, null);
    }
    /**
     * Prints statistics
     * @return 
     */
    public String printStatistics()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Thread Statistics\n");
        sb.append("-----------------\n");
        sb.append("Maximum   : ").append(getLargestPoolSize()).append('\n');
        sb.append("Active    : ").append(getActiveCount()).append('\n');
        sb.append("Completed : ").append(getCompletedTaskCount()).append('\n');
        sb.append('\n');
        sb.append("Statistics are from last ").append(Math.min(System.currentTimeMillis() - startMillis, slideMillis)/1000).append(" seconds\n");
        sb.append('\n');
        sb.append("Tags\n");
        sb.append("-----------------\n");
        for (Entry<Object,Map<Object,TimeoutSlidingStats>> e1 : map.entrySet())
        {
            Object key = e1.getKey();
            sb.append("Tag : ").append(key).append('\n');
            Map<Object, TimeoutSlidingStats> statsMap = e1.getValue();
            double sum = statsMap.values().stream().mapToDouble((s)->{return s.count();}).sum();
            sb.append("Value                   %    Count      Ave      Max      Min\n");
            for (Entry<Object, TimeoutSlidingStats> e2 : statsMap.entrySet())
            {
                TimeoutSlidingStats stats = e2.getValue();
                sb.append(String.format("%-20.20s %4.1f %8.0f %8.3f %8.3f %8.3f\n", 
                        e2.getKey(),
                        100*stats.count()/sum,
                        stats.count(),
                        stats.average(),
                        stats.getMax(),
                        stats.getMin()
                ));
            }
        }
        return sb.toString();
    }
}
