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

import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface FuncsFactory
{
    Supplier<byte[]> rawSupplier();
    LongSupplier millisSupplier();
    IntSupplier intSupplier();
    LongSupplier longSupplier();
    DoubleSupplier doubleSupplier();
    IntFunction<String> lookupMap();
    Supplier<String> stringSupplier();
    Runnable getIntWriter(IntSupplier intSupplier);
    Runnable getLongWriter(LongSupplier longSupplier);
    Runnable getStringWriter(Supplier<String> stringSupplier);
}
