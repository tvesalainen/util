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

import java.util.function.IntSupplier;
import javafx.beans.Observable;
import javafx.beans.binding.IntegerBinding;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FunctionalIntegerBinding extends IntegerBinding
{
    private final IntSupplier value;

    public FunctionalIntegerBinding(IntSupplier value, Observable... dependencies)
    {
        this.value = value;
        bind(dependencies);
    }

    @Override
    protected int computeValue()
    {
        return value.getAsInt();
    }
}
