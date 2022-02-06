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
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PrintCompiler implements SignalCompiler<System>
{
    
    @Override
    public Runnable compileBegin(MessageClass mc, int canId, LongSupplier millisSupplier)
    {
        return () -> System.err.print(mc.getName() + ":");
    }

    @Override
    public ArrayAction<System> compile(MessageClass mc, SignalClass sc, ToIntFunction<CanSource> toIntFunction)
    {
        return (ctx,buf) ->
                {
                    System.err.print(" " + sc.getName() + " = " + toIntFunction.applyAsInt(buf) + " " + sc.getUnit());
                };
    }

    @Override
    public ArrayAction<System> compile(MessageClass mc, SignalClass sc, ToLongFunction<CanSource> toLongFunction)
    {
        return (ctx,buf) -> System.err.print(" " + sc.getName() + " = " + toLongFunction.applyAsLong(buf) + " " + sc.getUnit());
    }

    @Override
    public ArrayAction<System> compile(MessageClass mc, SignalClass sc, ToDoubleFunction<CanSource> toDoubleFunction)
    {
        return (ctx,buf) -> System.err.print(" " + sc.getName() + " = " + toDoubleFunction.applyAsDouble(buf) + " " + sc.getUnit());
    }

    @Override
    public ArrayAction<System> compile(MessageClass mc, SignalClass sc, ToIntFunction<CanSource> toIntFunction, IntFunction<String> map)
    {
        return (ctx,buf) ->
        {
            int ii = toIntFunction.applyAsInt(buf);
            String ss = map.apply(ii);
            ss = ss == null ? ii + "???" : ss;
            System.err.print(" " + sc.getName() + " = " + ss);
        };
    }

    @Override
    public ArrayAction<System> compile(MessageClass mc, SignalClass sc, Function<CanSource, String> stringSupplier)
    {
        return (ctx,buf) ->
        {
            System.err.print(" " + sc.getName() + " = '" + stringSupplier.apply(buf) + "'");
        };
    }


    @Override
    public Consumer<Throwable> compileEnd(MessageClass mc)
    {
        return (ex) -> System.err.println();
    }

    @Override
    public ArrayAction<System> compileBeginRepeat(MessageClass mc)
    {
        return (ctx, buf) -> System.err.print("\n(");
    }

    @Override
    public ArrayAction<System> compileEndRepeat(MessageClass mc)
    {
        return (ctx, buf) -> System.err.print(")");
    }
    
}
