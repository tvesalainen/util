/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.concurrent.Callable;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface SignalCompiler
{
    Runnable compileBegin(MessageClass mc);
    Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier);
    Runnable compile(MessageClass mc, SignalClass sc, LongSupplier supplier);
    Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier supplier);
    Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> supplier);
    Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map);
    default Runnable compileBinary(MessageClass mc, SignalClass sc)
    {
        return null;
    }
    Runnable compileASCII(MessageClass mc, SignalClass sc, Supplier<String> ss);
    Runnable compileEnd(MessageClass mc);

}
