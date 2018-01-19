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

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class BWSynchronizer extends FaxSynchronizer
{
    private static final double RATIO = 2182.0/2300.0;
    private LineCorrector corrector;
    private long blackLength;
    private long whiteLength;
    private long startOfLine;
    private long lineLength;
    private long sum;
    private int count;
    private long minError = Long.MAX_VALUE;
    public BWSynchronizer(FaxStateListener stateListener)
    {
        super(stateListener);
        corrector = new LineCorrector(this::corrected);
    }
    @Override
    protected void stop()
    {
        super.stop();
        startOfLine = 0;
        lineLength = 0;
        sum = 0;
        count = 0;
        minError = Long.MAX_VALUE;
    }


    @Override
    public void tone(FaxTone tone, long begin, long end, long span, float amplitude, long error)
    {
        corrector.tone(tone, begin, end, span, amplitude, error);
    }

    public void corrected(FaxTone tone, long begin, long end, long span, float amplitude, long error)
    {
        switch (tone)
        {
            case BLACK:
                blackLength = span;
                break;
            case WHITE:
                whiteLength = span;
                check(begin-blackLength, error);
                break;
            default:
                blackLength = 0;
                whiteLength = 0;
                break;
        }
    }
    private void check(long begin, long error)
    {
        long len = blackLength+whiteLength;
        if (isAbout(RATIO, (double)blackLength/(double)len) && isAbout(500000, len))
        {
            hit(begin, len, error);
        }
    }
    private void hit(long begin, long lineLen, long error)
    {
        sum += lineLen;
        count++;
        lineLength = 500000;    //sum/count;
        System.err.println(lineLength+" "+lineLen+" err="+error);
        if (startOfLine == 0)
        {
            startOfLine = begin;
            minError = error;
            if (!started)
            {
                start();
            }
        }
        else
        {
            if (error < minError)
            {
                minError = error;
                startOfLine = correct(startOfLine, begin);
            }
        }
    }
    private boolean isAbout(double expected, double value)
    {
        double delta = expected*0.01;
        return value > expected-delta && value < expected+delta;
    }
    @Override
    protected void startTest(float frequency, long micros)
    {
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
