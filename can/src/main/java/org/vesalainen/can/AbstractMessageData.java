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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.can.dbc.DBC;
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
    private final Inner inner;
    
    protected AbstractMessageData(MethodHandles.Lookup lookup, int pgn)
    {
        this(lookup, DBC.getPgnMessage(pgn));
    }
    protected AbstractMessageData(MethodHandles.Lookup lookup, MessageClass mc)
    {
        super(lookup);
        this.mc = mc;
        this.inner = inner();
    }
    
    public void read(byte[] buf)
    {
        inner.reader.run(this, new SimpleCanSource(buf));
    }
    public void write(byte[] buf)
    {
        inner.writer.run(this, new SimpleCanSource(buf));
    }
    public void read(CanSource buf)
    {
        inner.reader.run(this, buf);
    }
    public void write(CanSource buf)
    {
        inner.writer.run(this, buf);
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
    private class ReaderCompiler implements SignalCompiler<AnnotatedPropertyStore>
    {

        @Override
        public ArrayAction<AnnotatedPropertyStore> compile(MessageClass mc, SignalClass sc, int off)
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
                    ToIntFunction<CanSource> ti = factory.toIntFunction();
                    MethodHandle asInt = setter.asType(MethodType.methodType(void.class, AnnotatedPropertyStore.class, int.class));
                    return (ctx, src)->
                    {
                        try
                        {
                            asInt.invokeExact(ctx, ti.applyAsInt(src));
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    };

                case LONG:
                    ToLongFunction<CanSource> tl = factory.toLongFunction();
                    MethodHandle asLong = setter.asType(MethodType.methodType(void.class, AnnotatedPropertyStore.class, long.class));
                    return (ctx, src)->
                    {
                        try
                        {
                            asLong.invokeExact(ctx, tl.applyAsLong(src));
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    };
                case DOUBLE:
                    ToDoubleFunction<CanSource> td = factory.toDoubleFunction();
                    MethodHandle asDouble = setter.asType(MethodType.methodType(void.class, AnnotatedPropertyStore.class, double.class));
                    return (ctx, src)->
                    {
                        try
                        {
                            asDouble.invokeExact(ctx, td.applyAsDouble(src));
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    };
                case ASCIIZ:
                case AISSTRING:
                case AISSTRING2:
                    Function<CanSource, String> ts = factory.toStringFunction();
                    MethodHandle asString = setter.asType(MethodType.methodType(void.class, AnnotatedPropertyStore.class, String.class));
                    return (ctx, src)->
                    {
                        try
                        {
                            asString.invokeExact(ctx, ts.apply(src));
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    };

                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
        }
        
    }
    private class WriterCompiler implements SignalCompiler
    {
        @Override
        public ArrayAction<AnnotatedPropertyStore> compile(MessageClass mc, SignalClass sc, int off)
        {
            FuncsFactory<AnnotatedPropertyStore> factory = ArrayFuncsFactory.getInstance(mc, sc, this, off);
            String name = sc.getName();
            C c = c(name);
            MethodHandle getter = c.getGetter();
            switch (sc.getSignalType())
            {
                case LOOKUP:
                case BINARY:
                case INT:
                    MethodHandle asInt = getter.asType(MethodType.methodType(int.class, AnnotatedPropertyStore.class));
                    return factory.getIntWriter((AnnotatedPropertyStore ctx)->
                    {
                        try
                        {
                            return (int)asInt.invokeExact(ctx);
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    });
                case LONG:
                    MethodHandle asLong = getter.asType(MethodType.methodType(long.class, AnnotatedPropertyStore.class));
                    return factory.getLongWriter((AnnotatedPropertyStore ctx)->
                    {
                        try
                        {
                            return (long)asLong.invokeExact(ctx);
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    });
                case ASCIIZ:
                case AISSTRING:
                case AISSTRING2:
                    MethodHandle asString = getter.asType(MethodType.methodType(String.class, AnnotatedPropertyStore.class));
                    return factory.getStringWriter((AnnotatedPropertyStore ctx)->
                    {
                        try
                        {
                            return (String)asString.invokeExact(ctx);
                        }
                        catch (Throwable ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    });
                case DOUBLE:
                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
        }
    }
    private static class Inner
    {
        private ArrayAction<AnnotatedPropertyStore> reader;
        private ArrayAction<AnnotatedPropertyStore> writer;
    }
}
