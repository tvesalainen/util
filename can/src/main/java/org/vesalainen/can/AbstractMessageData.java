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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.code.AnnotatedPropertyStore;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractMessageData extends AnnotatedPropertyStore
{
    private static final Map<Class<? extends AnnotatedPropertyStore>,Inner> INNERS = new WeakHashMap<>();

    private MessageClass mc;
    
    protected AbstractMessageData(MethodHandles.Lookup lookup, MessageClass mc)
    {
        super(lookup);
        this.mc = mc;
    }
    
    public void read(byte[] buf)
    {
        inner().reader.run(this, buf);
    }
    public void write(byte[] buf)
    {
        inner().writer.run(this, buf);
    }
    private Inner inner()
    {
        Class<? extends AnnotatedPropertyStore> cls = getClass();
        Inner inner = INNERS.get(cls);
        if (inner == null)
        {
            inner = compile(cls);
            INNERS.put(cls, inner);
        }
        return inner;
    }

    private Inner compile(Class<? extends AnnotatedPropertyStore> cls)
    {
        Inner inner = new Inner();
        ActionBuilder readerBuilder = new ActionBuilder(mc, new ReaderCompiler());
        inner.reader = readerBuilder.build();
        ActionBuilder writerBuilder = new ActionBuilder(mc, new WriterCompiler());
        inner.writer = writerBuilder.build();
        return inner;
    }
    private class ReaderCompiler implements SignalCompiler
    {

        @Override
        public ArrayAction compile(MessageClass mc, SignalClass sc, int off)
        {
            FuncsFactory factory = ArrayFuncsFactory.getInstance(mc, sc, this, off);
            String name = sc.getName();
            C c = c(name);
            MethodHandle setter = c.getSetter();
            switch (sc.getSignalType())
            {
                case LOOKUP:
                case BINARY:
                case INT:
                    ToIntFunction<byte[]> ti = factory.toIntFunction();
                    MethodHandle asInt = setter.asType(MethodType.methodType(void.class, int.class));
                    return (ctx, buf)->
                    {
                        try
                        {
                            asInt.invokeExact(ctx, ti.applyAsInt(buf));
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    };

                case LONG:
                    ToLongFunction<byte[]> tl = factory.toLongFunction();
                    MethodHandle asLong = setter.asType(MethodType.methodType(void.class, long.class));
                    return (ctx, buf)->
                    {
                        try
                        {
                            asLong.invokeExact(ctx, tl.applyAsLong(buf));
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    };
                case DOUBLE:
                    ToDoubleFunction<byte[]> tl = factory.toDoubleFunction();
                    MethodHandle asDouble = setter.asType(MethodType.methodType(void.class, double.class));
                    return (ctx, buf)->
                    {
                        try
                        {
                            asDouble.invokeExact(ctx, tl.applyAsDouble(buf));
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    };
                case ASCIIZ:
                case AISSTRING:
                case AISSTRING2:
                    System.err.print("String ");
                    break;
                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
        }
        
    }
    private class WriterCompiler implements SignalCompiler
    {
        
    }
    private static class Inner
    {
        private ArrayAction<AnnotatedPropertyStore> reader;
        private ArrayAction<AnnotatedPropertyStore> writer;
    }
}
