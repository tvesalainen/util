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

import java.nio.ByteOrder;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.dbc.ValueType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArrayFuncsFactory<T> implements FuncsFactory<T>
{
    private static ThreadLocal<ArrayFuncsFactory> FACTORY;
    static
    {
        FACTORY = ThreadLocal.withInitial(ArrayFuncsFactory::new);
    }
    
    private MessageClass mc;
    private SignalClass sc;
    private SignalCompiler compiler;
    private int off;

    private ArrayFuncsFactory()
    {
    }

    public static <T> FuncsFactory<T> getInstance(MessageClass mc, SignalClass sc, SignalCompiler compiler, int off)
    {
        ArrayFuncsFactory factory = FACTORY.get();
        factory.set(mc, sc, compiler, off);
        return factory;
    }
    private void set(MessageClass mc, SignalClass sc, SignalCompiler compiler, int off)
    {
        this.mc = mc;
        this.sc = sc;
        this.compiler = compiler;
        this.off = off;
    }

    @Override
    public ToIntFunction<CanSource> toIntFunction()
    {
        return compiler.compileIntBoundCheck(mc, sc, 
                                compiler.factorInt(mc, sc, sc.getFactor(), sc.getOffset(), 
                                    ArrayFuncs.getIntFunction(sc, off)
                            ));
    }

    @Override
    public ToLongFunction<CanSource> toLongFunction()
    {
        return compiler.compileLongBoundCheck(mc, sc, 
                                compiler.factorLong(mc, sc, sc.getFactor(), sc.getOffset(), 
                                    ArrayFuncs.getLongFunction(sc, off)
                            ));
    }

    @Override
    public ToDoubleFunction<CanSource> toDoubleFunction()
    {
        return compiler.compileDoubleBoundCheck(mc, sc, 
                                compiler.factorDouble(mc, sc, sc.getFactor(), sc.getOffset(),
                                    ArrayFuncs.getLongFunction(sc, off)
                            ));
    }

    @Override
    public IntFunction<String> lookupMap()
    {
        return sc.getMapper();
    }

    @Override
    public Function<CanSource,String> toStringFunction()
    {
        switch (sc.getSignalType())
        {
            case ASCIIZ:
                return ArrayFuncs.getZeroTerminatingStringFunction((sc.getStartBit() + off) / 8, sc.getSize() / 8);
            case AISSTRING:
                return ArrayFuncs.getAisStringFunction((sc.getStartBit() + off) / 8, sc.getSize() / 8);
            case AISSTRING2:
                return ArrayFuncs.getAisStringFunction2((sc.getStartBit() + off) / 8);
            default:
                throw new UnsupportedOperationException(sc.getSignalType() + " not supported");
        }
    }

    @Override
    public ArrayAction<T> getStringWriter(Function<T,String> stringFunction)
    {
        switch (sc.getSignalType())
        {
            case ASCIIZ:
                return ArrayFuncs.getStringWriter((sc.getStartBit() + off) / 8, sc.getSize() / 8, (byte)0, stringFunction);
            case AISSTRING:
            case AISSTRING2:
                return ArrayFuncs.getStringWriter((sc.getStartBit() + off) / 8, sc.getSize() / 8, (byte)'@', stringFunction);
            default:
                throw new UnsupportedOperationException(sc.getSignalType() + " not supported");
        }
    }

    @Override
    public ArrayAction<T> getIntWriter(ToIntFunction<T> toIntFunction)
    {
        return ArrayFuncs.getIntWriter(sc.getStartBit() + off, sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED, toIntFunction);
    }

    @Override
    public ArrayAction<T> getLongWriter(ToLongFunction<T> toLongFunction)
    {
        return ArrayFuncs.getLongWriter(sc.getStartBit() + off, sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED, toLongFunction);
    }
    
}
