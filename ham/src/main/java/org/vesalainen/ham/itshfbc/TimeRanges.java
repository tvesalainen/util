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
package org.vesalainen.ham.itshfbc;

import java.time.OffsetTime;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class TimeRanges
{
    public static final TimeRanges ALWAYS = new Always();
    public abstract boolean in(OffsetTime time);
    
    public static final TimeRanges getInstance(String text)
    {
        if (text == null)
        {
            return ALWAYS;
        }
        else
        {
            return null;
        }
    }
    public static class Always extends TimeRanges
    {

        private Always()
        {
        }

        @Override
        public boolean in(OffsetTime time)
        {
            return true;
        }
        
    }
}
