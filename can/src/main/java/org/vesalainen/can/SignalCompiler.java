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
public interface SignalCompiler<T>
{
    default T target()
    {
        return null;
    }
    default boolean needCompilation(int canId)
    {
        return true;
    }
    default Runnable compile(MessageClass mc, SignalClass sc, int off, byte[] buf) 
    {
        ArrayAction<T> arrayAction = compile(mc, sc, off);
        return ()->arrayAction.run((T) this, buf);
    }
    default ArrayAction<T> compile(MessageClass mc, SignalClass sc, int off) 
    {
            double factor = sc.getFactor();
            double offset = sc.getOffset();
            switch (sc.getSignalType())
            {
                case INT:
                    return compile(mc, sc, 
                            compileIntBoundCheck(mc, sc, 
                                factorInt(mc, sc, factor, offset, 
                                    ArrayFuncs.getIntFunction(sc, off)
                            )));
                case LONG:
                    return compile(mc, sc, 
                            compileLongBoundCheck(mc, sc, 
                                factorLong(mc, sc, factor, offset, 
                                    ArrayFuncs.getLongFunction(sc, off)
                            )));
                case DOUBLE:
                    return compile(mc, sc, 
                            compileDoubleBoundCheck(mc, sc, 
                                factorDouble(mc, sc, factor, offset, 
                                    ArrayFuncs.getLongFunction(sc, off)
                            )));
                case LOOKUP:
                    IntFunction<String> f = sc.getMapper();
                    if (f == null)
                    {
                        return (ctx, buf)->compile(mc, sc, 
                                ArrayFuncs.getIntFunction(sc, off)
                        );
                    }
                    return compile(mc, sc, 
                            ArrayFuncs.getIntFunction(sc, off), 
                            f);

                case BINARY:
                    return compileBinary(mc, sc);
                case ASCIIZ:
                    return compile(mc, sc, 
                            ArrayFuncs.getZeroTerminatingStringFunction(sc, off)
                    );
                case AISSTRING:
                    return compile(mc, sc, 
                            ArrayFuncs.getAisStringFunction(sc, off, currentBytesSupplier())
                    );
                case AISSTRING2:
                    return compile(mc, sc, 
                            ArrayFuncs.getAisStringFunction2(sc, off)
                    );
                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
    };
    default ArrayAction<T> compileRaw(MessageClass mc, Supplier<byte[]> rawSupplier) {return null;};
    default Runnable compileBegin(MessageClass mc, int canId, LongSupplier millisSupplier) {return null;};
    default ArrayAction<T> compile(MessageClass mc, SignalClass sc, ToIntFunction<byte[]> toIntFunction) {return null;};
    default ToIntFunction<byte[]> compileIntBoundCheck(MessageClass mc, SignalClass sc, ToIntFunction<byte[]> toIntFunction) {return toIntFunction;};
    default ArrayAction<T> compile(MessageClass mc, SignalClass sc, ToLongFunction<byte[]> toLongFunction) {return null;};
    default ToLongFunction<byte[]> compileLongBoundCheck(MessageClass mc, SignalClass sc, ToLongFunction<byte[]> toLongFunction) {return toLongFunction;};
    default ArrayAction<T> compile(MessageClass mc, SignalClass sc, ToDoubleFunction<byte[]> toDoubleFunction) {return null;};
    default ToDoubleFunction<byte[]> compileDoubleBoundCheck(MessageClass mc, SignalClass sc, ToDoubleFunction<byte[]> toDoubleFunction) {return toDoubleFunction;};
    default ToDoubleFunction<byte[]> compileDoubleBoundCheck(MessageClass mc, SignalClass sc, ToLongFunction<byte[]> toLongFunction) {return null;};
    default ArrayAction<T> compile(MessageClass mc, SignalClass sc, ToIntFunction<byte[]> toIntFunction, IntFunction<String> map) {return null;};
    default ArrayAction<T> compileBinary(MessageClass mc, SignalClass sc) {return null;}
    default ArrayAction<T> compile(MessageClass mc, SignalClass sc, Function<byte[],String> stringFunction) {return null;};
    default Consumer<Throwable> compileEnd(MessageClass mc) {return null;};
    default ArrayAction<T> compileBeginRepeat(MessageClass mc) {return null;};
    default ArrayAction<T> compileEndRepeat(MessageClass mc) {return null;};
    default LongSupplier millisSupplier()
    {
        throw new UnsupportedOperationException("not supported yet");
    }
    default IntSupplier currentBytesSupplier()
    {
        throw new UnsupportedOperationException("not supported yet");
    }

    default boolean isFactored(double factor, double offset)
    {
        return factor != 1.0 || offset != 0.0;
    }

    default ToIntFunction<byte[]> factorInt(MessageClass mc, SignalClass sc, double factor, double offset, ToIntFunction<byte[]> toIntFunction)
    {
        if (isFactored(factor, offset))
        {
            return (buf)->(int) (factor*toIntFunction.applyAsInt(buf)+offset);
        }
        else
        {
            return toIntFunction;
        }
    }

    default ToLongFunction<byte[]> factorLong(MessageClass mc, SignalClass sc, double factor, double offset, ToLongFunction<byte[]> toLongFunction)
    {
        if (isFactored(factor, offset))
        {
            return (buf)->(long) (factor*toLongFunction.applyAsLong(buf)+offset);
        }
        else
        {
            return toLongFunction;
        }
    }

    default ToDoubleFunction<byte[]> factorDouble(MessageClass mc, SignalClass sc, double factor, double offset, ToLongFunction<byte[]> toLongFunction)
    {
        ToDoubleFunction<byte[]> check = compileDoubleBoundCheck(mc, sc, toLongFunction);
        if (check != null)
        {
            if (isFactored(factor, offset))
            {
                return (buf)-> (factor*check.applyAsDouble(buf)+offset);
            }
            else
            {
                return (buf)-> check.applyAsDouble(buf);
            }
        }
        else
        {
            if (isFactored(factor, offset))
            {
                return (buf)-> (factor*toLongFunction.applyAsLong(buf)+offset);
            }
            else
            {
                return (buf)->toLongFunction.applyAsLong(buf);
            }
        }
    }

}
