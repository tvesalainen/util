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
package org.vesalainen.ui.scale;

import java.util.Formatter;
import java.util.Locale;
import static java.util.concurrent.TimeUnit.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TimeScale extends SerialScale
{

    public TimeScale()
    {
        addScaleLevel(new HeadScaleLevel(DAYS.toSeconds(1), "%.0fd"));
        addScaleLevel(new HeadScaleLevel(HOURS.toSeconds(1), "%.0fh"));
        addScaleLevel(new HeadScaleLevel(MINUTES.toSeconds(1), "%.0fm"));
        addTail(1, "s", 1.0, 5.0);
    }
    
    protected final void addTail(double maxDelta, String unit, double... multipliers)
    {
        for (double multiplier : multipliers)
        {
            tail.add(new TailScale(multiplier, unit).setMaxDelta(maxDelta));
        }
    }
    protected class HeadScaleLevel extends AbstractScaleLevel
    {
        public HeadScaleLevel(double step, String format)
        {
            super(step, format);
        }

        @Override
        public String label(Locale locale, double value)
        {
            return TimeScale.this.format(locale, value, this, null);
        }

    }

    private class TailScale extends BasicScale
    {

        public TailScale(double multiplier, String unit)
        {
            super(multiplier, unit);
        }

        @Override
        protected void format(Formatter formatter, double value, AbstractScaleLevel caller)
        {
            double v = TimeScale.this.format(formatter, value, caller, null);
            caller.format(formatter, v);
        }

    }
}
