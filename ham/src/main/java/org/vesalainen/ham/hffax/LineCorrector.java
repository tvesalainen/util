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
public class LineCorrector implements FaxListener
{
    private static final long MAX_ERROR_PERMIL = 20;
    private static final long SPOT_LIMIT = 1000;
    private FaxListener listener;
    private FaxTone state;
    private long error;
    private long start;
    private long length;

    public LineCorrector(FaxListener listener)
    {
        this.listener = listener;
    }
    
    @Override
    public void tone(FaxTone tone, long begin, long end, long span, float amplitude, long err)
    {
        if (state != null)
        {
            if (tone == state)
            {
                length += span;
            }
            else
            {
                if (span < SPOT_LIMIT && 1000*(error+span)/length < MAX_ERROR_PERMIL)
                {
                    length += span;
                    error += span;
                }
                else
                {
                    listener.tone(state, start, start+length, length, amplitude, error);
                    state = tone;
                    start = begin;
                    length = span;
                    error = 0;
                }
            }
        }
        else
        {
            state = tone;
            start = begin;
            length = span;
        }
    }
    
}
