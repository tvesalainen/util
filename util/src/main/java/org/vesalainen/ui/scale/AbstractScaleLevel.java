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

import java.util.Locale;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractScaleLevel implements ScaleLevel
{
    protected double step;
    protected String format;

    protected AbstractScaleLevel(double step, String format)
    {
        this.step = step;
        this.format = format;
    }

    @Override
    public double step()
    {
        return step;
    }

    @Override
    public String format()
    {
        return format;
    }

    @Override
    public String label(Locale locale, double value)
    {
        return String.format(locale, format, value);
    }
    
    @Override
    public String toString()
    {
        return "ScaleLevel{" + "step=" + step + '}';
    }

}
