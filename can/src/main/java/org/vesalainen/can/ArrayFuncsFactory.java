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
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.dbc.ValueType;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ArrayFuncsFactory implements FuncsFactory
{
    private static ThreadLocal<ArrayFuncsFactory> FACTORY;
    static
    {
        FACTORY = ThreadLocal.withInitial(ArrayFuncsFactory::new);
    }
    
    private MessageClass mc;
    private SignalClass sc;
    private byte[] buf;
    private LongSupplier millisSupplier;
    private SignalCompiler compiler;
    private int off;
    private IntSupplier limitSupplier;

    private ArrayFuncsFactory()
    {
    }

    public static FuncsFactory getInstance(MessageClass mc, SignalClass sc, SignalCompiler compiler, int off, LongSupplier millisSupplier, IntSupplier limitSupplier, byte[] buf)
    {
        ArrayFuncsFactory factory = FACTORY.get();
        factory.set(mc, sc, compiler, off, millisSupplier, limitSupplier, buf);
        return factory;
    }
    private void set(MessageClass mc, SignalClass sc, SignalCompiler compiler, int off, LongSupplier millisSupplier, IntSupplier limitSupplier, byte[] buf)
    {
        this.mc = mc;
        this.sc = sc;
        this.compiler = compiler;
        this.off = off;
        this.millisSupplier = millisSupplier;
        this.limitSupplier = limitSupplier;
        this.buf = buf;
    }

    @Override
    public Supplier<byte[]> rawSupplier()
    {
        return () -> buf;
    }

    @Override
    public LongSupplier millisSupplier()
    {
        return millisSupplier;
    }

    @Override
    public IntSupplier intSupplier()
    {
        return compiler.compileIntBoundCheck(mc, sc, compiler.factorInt(mc, sc, sc.getFactor(), sc.getOffset(), ArrayFuncs.getIntSupplier(sc.getStartBit() + off, sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED, buf)));
    }

    @Override
    public LongSupplier longSupplier()
    {
        return compiler.compileLongBoundCheck(mc, sc, compiler.factorLong(mc, sc, sc.getFactor(), sc.getOffset(), ArrayFuncs.getLongSupplier(sc.getStartBit() + off, sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED, buf)));
    }

    @Override
    public DoubleSupplier doubleSupplier()
    {
        return compiler.compileDoubleBoundCheck(mc, sc, compiler.factorDouble(mc, sc, sc.getFactor(), sc.getOffset(), ArrayFuncs.getLongSupplier(sc.getStartBit() + off, sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED, buf)));
    }

    @Override
    public IntFunction<String> lookupMap()
    {
        return sc.getMapper();
    }

    @Override
    public Supplier<String> stringSupplier()
    {
        switch (sc.getSignalType())
        {
            case ASCIIZ:
                return ArrayFuncs.getZeroTerminatingStringSupplier((sc.getStartBit() + off) / 8, sc.getSize() / 8, buf);
            case AISSTRING:
                return ArrayFuncs.getAisStringSupplier((sc.getStartBit() + off) / 8, sc.getSize() / 8, buf, limitSupplier);
            case AISSTRING2:
                return ArrayFuncs.getAisStringSupplier2((sc.getStartBit() + off) / 8, buf);
            default:
                throw new UnsupportedOperationException(sc.getSignalType() + " not supported");
        }
    }

    @Override
    public Runnable getStringWriter(Supplier<String> stringSupplier)
    {
        switch (sc.getSignalType())
        {
            case ASCIIZ:
                return ArrayFuncs.getStringWriter((sc.getStartBit() + off) / 8, sc.getSize() / 8, (byte)0, stringSupplier, buf);
            case AISSTRING:
            case AISSTRING2:
                return ArrayFuncs.getStringWriter((sc.getStartBit() + off) / 8, sc.getSize() / 8, (byte)'@', stringSupplier, buf);
            default:
                throw new UnsupportedOperationException(sc.getSignalType() + " not supported");
        }
    }

    @Override
    public Runnable getIntWriter(IntSupplier intSupplier)
    {
        return ArrayFuncs.getIntWriter(sc.getStartBit() + off, sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED, intSupplier, buf);
    }

    @Override
    public Runnable getLongWriter(LongSupplier longSupplier)
    {
        return ArrayFuncs.getLongWriter(sc.getStartBit() + off, sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED, longSupplier, buf);
    }
    
}
