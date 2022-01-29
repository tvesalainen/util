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

import static java.nio.ByteOrder.BIG_ENDIAN;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.Supplier;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import static org.vesalainen.can.dbc.ValueType.SIGNED;

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
    default Runnable compile(MessageClass mc, SignalClass sc, int off, byte[] buf, IntSupplier limitSupplier, FuncsFactory factory) 
    {
            double factor = sc.getFactor();
            double offset = sc.getOffset();
            switch (sc.getSignalType())
            {
                case INT:
                    return compile(mc, sc, 
                            compileIntBoundCheck(mc, sc, 
                                factorInt(mc, sc, factor, offset, 
                                    ArrayFuncs.getIntSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf)
                            )));
                case LONG:
                    return compile(mc, sc, 
                            compileLongBoundCheck(mc, sc, 
                                factorLong(mc, sc, factor, offset, 
                                    ArrayFuncs.getLongSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf)
                            )));
                case DOUBLE:
                    return compile(mc, sc, 
                            compileDoubleBoundCheck(mc, sc, 
                                factorDouble(mc, sc, factor, offset, 
                                    ArrayFuncs.getLongSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf)
                            )));
                case LOOKUP:
                    IntSupplier is = ArrayFuncs.getIntSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    IntFunction<String> f = sc.getMapper();
                    if (f == null)
                    {
                        return compile(mc, sc, is);
                    }
                    return compile(mc, sc, is, f);

                case BINARY:
                    return compileBinary(mc, sc, buf, sc.getStartBit()+off, sc.getSize());
                case ASCIIZ:
                    return compile(mc, sc, 
                            ArrayFuncs.getZeroTerminatingStringSupplier((sc.getStartBit()+off)/8, sc.getSize()/8, buf)
                    );
                case AISSTRING:
                    return compile(mc, sc, 
                            ArrayFuncs.getAisStringSupplier((sc.getStartBit()+off)/8, sc.getSize()/8, buf, limitSupplier)
                    );
                case AISSTRING2:
                    return compile(mc, sc, 
                            ArrayFuncs.getAisStringSupplier2((sc.getStartBit()+off)/8, buf)
                    );
                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
    };
    default Runnable compileRaw(MessageClass mc, Supplier<byte[]> rawSupplier) {return null;};
    default Runnable compileBegin(MessageClass mc, int canId, LongSupplier millisSupplier) {return null;};
    default Runnable compile(MessageClass mc, SignalClass sc, IntSupplier intSupplier) {return null;};
    default IntSupplier compileIntBoundCheck(MessageClass mc, SignalClass sc, IntSupplier intSupplier) {return intSupplier;};
    default Runnable compile(MessageClass mc, SignalClass sc, LongSupplier longSupplier) {return null;};
    default LongSupplier compileLongBoundCheck(MessageClass mc, SignalClass sc, LongSupplier longSupplier) {return longSupplier;};
    default Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier doubleSupplier) {return null;};
    default DoubleSupplier compileDoubleBoundCheck(MessageClass mc, SignalClass sc, DoubleSupplier doubleSupplier) {return doubleSupplier;};
    default LongToDoubleFunction compileDoubleBoundCheck(MessageClass mc, SignalClass sc, LongSupplier longSupplier) {return null;};
    default Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map) {return null;};
    default Runnable compileBinary(MessageClass mc, SignalClass sc, byte[] buf, int offset, int length) {return null;}
    default Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> ss) {return null;};
    default Consumer<Throwable> compileEnd(MessageClass mc) {return null;};
    default Runnable compileBeginRepeat(MessageClass mc) {return null;};
    default Runnable compileEndRepeat(MessageClass mc) {return null;};

    default boolean isFactored(double factor, double offset)
    {
        return factor != 1.0 || offset != 0.0;
    }

    default IntSupplier factorInt(MessageClass mc, SignalClass sc, double factor, double offset, IntSupplier intSupplier)
    {
        if (isFactored(factor, offset))
        {
            return ()->(int) (factor*intSupplier.getAsInt()+offset);
        }
        else
        {
            return intSupplier;
        }
    }

    default LongSupplier factorLong(MessageClass mc, SignalClass sc, double factor, double offset, LongSupplier longSupplier)
    {
        if (isFactored(factor, offset))
        {
            return ()->(long) (factor*longSupplier.getAsLong()+offset);
        }
        else
        {
            return longSupplier;
        }
    }

    default DoubleSupplier factorDouble(MessageClass mc, SignalClass sc, double factor, double offset, LongSupplier longSupplier)
    {
        LongToDoubleFunction check = compileDoubleBoundCheck(mc, sc, longSupplier);
        if (check != null)
        {
            if (isFactored(factor, offset))
            {
                return ()-> (factor*check.applyAsDouble(longSupplier.getAsLong())+offset);
            }
            else
            {
                return ()-> check.applyAsDouble(longSupplier.getAsLong());
            }
        }
        else
        {
            if (isFactored(factor, offset))
            {
                return ()-> (factor*longSupplier.getAsLong()+offset);
            }
            else
            {
                return ()->longSupplier.getAsLong();
            }
        }
    }

}
