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
public abstract class FaxSynchronizer extends Fax implements FaxListener, FrequencyListener, PageLocator
{
    private int stopMinErrors = Integer.MAX_VALUE;
    protected long initialLineLength = 500000;
    protected boolean started;
    protected int lastLine;
    private FaxStateListener stateListener;
    private boolean startSync;
    
    private PatternMatcher stopMatcher;

    public FaxSynchronizer(FaxStateListener stateListener)
    {
        this.stateListener = stateListener;
        this.stopMatcher = new BWMatcher(STOP_LEN, STOP_BLACK_LEN, 500000, 3000);
    }

    @Override
    public void frequency(float frequency, long micros)
    {
        if (!started || (started && line(micros) < 60))
        {
            if (!startSync)
            {
                startSync = true;
                System.err.println("start sync");
            }
            startTest(frequency, micros);
        }
        else
        {
            if (startSync)
            {
                startSync = false;
                System.err.println("stop sync");
            }
            stopTest(frequency, micros);
        }
    }

    @Override
    public void tone(FaxTone tone, long begin, long end, long span, float amplitude, long error)
    {
    }
    
    protected abstract void startTest(float frequency, long micros);
    protected void stopTest(float frequency, long micros)
    {
        int errors = stopMatcher.match(frequency<1900F, micros);
        if (errors < STOP_ERROR_LIMIT)
        {
            stop();
        }
        if (errors < stopMinErrors)
        {
            stopMinErrors = errors;
        }
    }
    protected void stop()
    {
        stateListener.stop();
        started = false;
    }
    protected void start()
    {
        stateListener.start(this);
        started = true;
        stopMatcher.reset();
    }
    @Override
    public int firstLine()
    {
        return 0; //lineAdjuster.getFirstLine();
    }

    @Override
    public int lastLine()
    {
        return lastLine;
    }
    public static long correct(long current, long better)
    {
        long delta = (better-current) % LINE_LEN;
        if (delta < LINE_LEN/2)
        {
            System.err.println("corr="+delta);
            return current+delta;
        }
        else
        {
            System.err.println("corr="+(delta-LINE_LEN));
            return current+delta-LINE_LEN;
        }
    }
    /*
    @Override
    public void tone(FaxTone tone, long begin, long end, long span, float amplitude)
    {
    if (begin > start)
    {
    switch (tone)
    {
    case BLACK:
    blackLength = span;
    break;
    case WHITE:
    whiteLength = span;
    break;
    default:
    blackLength = 0;
    whiteLength = 0;
    return;
    }
    if (blackLength > whiteLength)
    {
    long lpm = getLPM(blackLength+whiteLength);
    if (lpm > 0)
    {
    System.err.println("SYNC");
    if (lpm == lineLength)
    {
    syncCount++;
    if (syncCount > SYNC_LIMIT)
    {
    stateListener.start(begin, lineLength);
    lineLength = 0;
    blackLength = 0;
    whiteLength = 0;
    syncCount = 0;
    start = begin + 30000000;
    }
    }
    else
    {
    lineLength = lpm;
    }
    }
    }
    }
    }
    //60, 90, 100, 120, 180, 240
    private static final int L60 = 600000/60;
    private static final int L90 = 600000/90;
    private static final int L100 = 600000/100;
    private static final int L120 = 600000/120;
    private static final int L180 = 600000/180;
    private static final int L240 = 600000/240;
    private long getLPM(long lineLen)
    {
    int r = (int) ((lineLen+50)/100);
    switch (r)
    {
    case L60:
    return 100*L60;
    case L90:
    return 100*L90;
    case L100:
    return 100*L100;
    case L120:
    return 100*L120;
    case L180:
    return 100*L180;
    case L240:
    return 100*L240;
    default:
    return -1;
    }
    }
     */
    
}
