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
package org.vesalainen.math;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractSeeker
{
    protected double tolerance;
    private Runnable action;
    protected boolean done;
    protected ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    protected ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    protected ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public AbstractSeeker(double tolerance, Runnable action)
    {
        this.tolerance = tolerance;
        this.action = action;
    }
    
    protected void check()
    {
        if (!done)
        {
            if (isWithin(tolerance))
            {
                done = true;
                if (action != null)
                {
                    action.run();
                }
            }
        }
    }
    /**
     * Add new value with 1.0 weight
     * @param value 
     */
    public void add(double value)
    {
        add(value, 1.0);
    }
    /**
     * Add new value with weight
     * @param value
     * @param weight 
     */
    public abstract void add(double value, double weight);

    /**
     * Returns average angle in degrees
     * @return
     */
    public abstract double average();

    /**
     * Returns maximum deviation.
     * @return
     */
    public abstract double deviation();

    /**
     * Returns minimum angle counter clockwise from average
     * @return
     */
    public abstract double max();

    /**
     * Returns maximum angle clockwise from average
     * @return
     */
    public abstract double min();

    /**
     * Returns true if average is within given delta.
     * @param delta
     * @return
     */
    public abstract boolean isWithin(double delta);
}
