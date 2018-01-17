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
public class AdjustingSynchronizer extends FaxSynchronizer
{
    private static final int START_ERROR_LIMIT = 10;
    private int startMinErrors = Integer.MAX_VALUE;
    private PatternMatcher startMatcher;
    private LineAdjuster lineAdjuster;

    public AdjustingSynchronizer(FaxStateListener stateListener)
    {
        super(stateListener);
        this.startMatcher = new BWMatcher(500000, START_BLACK_LEN, 500000, 3000);
        this.lineAdjuster = new LineAdjuster(initialLineLength);
    }
    @Override
    protected void stop()
    {
        super.stop();
        startMinErrors = Integer.MAX_VALUE;
        startMatcher.reset();
        lineAdjuster.reset();
    }
    @Override
    protected void start()
    {
        super.start();
        started = true;
    }
    public void startTest(float frequency, long micros)
    {
        int errors = startMatcher.match(frequency<1900F, micros);
        if (errors < START_ERROR_LIMIT)
        {
            if (lineAdjuster.update(errors, micros))
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
        long lineLength = lineAdjuster.getLineLength();
        long startOfLine = lineAdjuster.getStartOfLine();
        return (int) (pageWidth * ((time - startOfLine) % lineLength) / lineLength);
    }

    @Override
    public int line(long time)
    {
        long lineLength = lineAdjuster.getLineLength();
        long startOfLine = lineAdjuster.getStartOfLine();
        lastLine = (int) ((time - startOfLine) / lineLength);
        return lastLine;
    }

    @Override
    public long getLineLength()
    {
        return lineAdjuster.getLineLength();
    }

    @Override
    public long getStartOfLine()
    {
        return lineAdjuster.getStartOfLine();
    }

}
