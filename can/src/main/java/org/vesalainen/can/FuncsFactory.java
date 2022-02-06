/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface FuncsFactory<T>
{
    ToIntFunction<CanSource> toIntFunction();
    ToLongFunction<CanSource> toLongFunction();
    ToDoubleFunction<CanSource> toDoubleFunction();
    IntFunction<String> lookupMap();
    Function<CanSource,String> toStringFunction();
    ArrayAction<T> getIntWriter(ToIntFunction<T> toIntFunction);
    ArrayAction<T> getLongWriter(ToLongFunction<T> toLongFunction);
    ArrayAction<T> getStringWriter(Function<T,String> stringFunction);
}
