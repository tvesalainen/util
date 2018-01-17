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

import org.vesalainen.ham.PatternMatcher;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleSynchronizer extends FaxSynchronizer
{
    private PatternMatcher startMatcher;
    private int minErrors = Integer.MAX_VALUE;
    private long lineLength = 500000;
    private long startOfLine;

    public SimpleSynchronizer(FaxStateListener stateListener)
    {
        super(stateListener);
        this.startMatcher = new BWMatcher(500000, START_BLACK_LEN, 1000000, 10000);
    }

    @Override
    protected void stop()
    {
        super.stop();
        startMatcher.reset();
        minErrors = Integer.MAX_VALUE;
    }

    @Override
    protected void startTest(float frequency, long micros)
    {
        int errors = startMatcher.match(frequency<1900F, micros);
        if (errors < 100)
        {
            if (errors < minErrors)
            {
                minErrors = errors;
                startOfLine = startMatcher.getTime();
            }
            else
            {
                if (!started)
                {
                    start();
                }
            }
        }
    }
    
    @Override
    public int column(int pageWidth, long time)
    {
        return (int) (pageWidth * ((time - startOfLine) % lineLength) / lineLength);
    }

    @Override
    public int line(long time)
    {
        lastLine = (int) ((time - startOfLine) / lineLength);
        return lastLine;
    }

    @Override
    public long getLineLength()
    {
        return lineLength;
    }

    @Override
    public long getStartOfLine()
    {
        return startOfLine;
    }
}
