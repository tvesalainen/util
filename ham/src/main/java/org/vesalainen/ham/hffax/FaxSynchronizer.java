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

import java.util.Collection;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FaxSynchronizer implements FaxListener
{
    private static final int SYNC_LIMIT = 2;
    private long lineLength;
    private long blackLength;
    private long whiteLength;
    private int syncCount;
    private FaxStateListener stateListener;
    private long start;

    public FaxSynchronizer(FaxStateListener stateListener)
    {
        this.stateListener = stateListener;
    }
    
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

}
