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

import java.util.function.Consumer;
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
    default boolean needCompilation(int canId)
    {
        return true;
    }
    default Runnable compileBegin(MessageClass mc, LongSupplier millisSupplier) {return null;};
    default Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier) {return null;};
    default Runnable compile(MessageClass mc, SignalClass sc, LongSupplier supplier) {return null;};
    default Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier supplier) {return null;};
    default Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map) {return null;};
    default Runnable compileBinary(MessageClass mc, SignalClass sc) {return null;}
    default Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> ss) {return null;};
    default Consumer<Throwable> compileEnd(MessageClass mc) {return null;};
    default Runnable compileBeginRepeat(MessageClass mc) {return null;};
    default Runnable compileEndRepeat(MessageClass mc) {return null;};

}
