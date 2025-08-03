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

import java.util.Iterator;
import static org.vesalainen.ui.scale.BasicScale.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MergeScale implements Scale
{
    public static final Scale BASIC15 = new MergeScale(SCALE10, SCALE05);
    public static final Scale BASIC135 = new MergeScale(SCALE10, SCALE03, SCALE05);
    private Scale[] array;

    public MergeScale(Scale... array)
    {
        this.array = array;
    }

    @Override
    public Iterator<ScaleLevel> iterator(double min, double max)
    {
        return Scale.merge(min, max, array);
    }

}
