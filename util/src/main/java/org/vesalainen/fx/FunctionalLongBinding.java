/*
 * Copyright (C) 2020 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.fx;

import java.util.function.LongSupplier;
import javafx.beans.Observable;
import javafx.beans.binding.LongBinding;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FunctionalLongBinding extends LongBinding
{
    private final String property;
    private final LongSupplier value;

    public FunctionalLongBinding(String property, LongSupplier value, Observable... dependencies)
    {
        this.property = property;
        this.value = value;
        bind(dependencies);
    }

    @Override
    protected long computeValue()
    {
        return value.getAsLong();
    }
    @Override
    public String toString()
    {
        return property + ": "+super.toString();
    }
}
