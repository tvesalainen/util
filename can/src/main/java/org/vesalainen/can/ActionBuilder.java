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

import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.MultiplexerIndicator;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.dbc.ValueType;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.IndexMap;
import org.vesalainen.util.IntRange;
import org.vesalainen.util.MapList;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ActionBuilder<T> extends JavaLogging
{
    
    private MessageClass mc;
    private SignalCompiler<T> compiler;
    private List<ArrayAction<T>> signals = new ArrayList<>();
    private Map<SignalClass, MapList<Integer, ArrayAction<T>>> mpxMap = new HashMap<>();
    private RtMultiplexor rootMultiplexor;
    private Map<SignalClass, RtMultiplexor> extendedMultiplexors = new HashMap<>();

    public ActionBuilder(MessageClass mc, SignalCompiler compiler)
    {
        super(ActionBuilder.class);
        this.mc = mc;
        this.compiler = compiler;
    }

    public Runnable build(CanSource buf)
    {
        ArrayAction<T> act = build();
        if (act != null)
        {
            return ()->act.run(compiler.target(), buf);
        }
        else
        {
            return null;
        }
    }
    public ArrayAction<T> build()
    {
        List<SignalClass> repeatingSignals = new ArrayList<>();
        String repeatCountSignalName = (String) mc.getAttributeValue("RepeatCount");
        //addAction(compiler.compileRaw(mc, () -> buf));
        SignalClass repeatCountSignal = mc.getSignal(repeatCountSignalName);
        IntRange repeatRange = mc.getRepeatRange();
        mc.forEach(sc ->
        {
            finer("add signal %s", sc);
            MultiplexerIndicator multiplexerIndicator = sc.getMultiplexerIndicator();
            if (multiplexerIndicator != null)
            {
                if (multiplexerIndicator.isMultiplexor())
                {
                    if (multiplexerIndicator.isExtended())
                    {
                        RtMultiplexor multiplexor = new RtMultiplexor(sc.getStartBit(), sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED);
                        extendedMultiplexors.put(sc, multiplexor);
                    }
                    else
                    {
                        rootMultiplexor = new RtMultiplexor(sc.getStartBit(), sc.getSize(), sc.getByteOrder() == ByteOrder.BIG_ENDIAN, sc.getValueType() == ValueType.SIGNED);
                        addAction(rootMultiplexor);
                    }
                }
                else
                {
                    ArrayAction<T> act = createSignal(mc, sc);
                    if (act != null)
                    {
                        SignalClass multiplexor = multiplexerIndicator.getMultiplexor();
                        MapList<Integer, ArrayAction<T>> ml = getMpx(multiplexor);
                        multiplexerIndicator.getValues().forEach(i -> ml.add(i, act));
                    }
                }
            }
            else
            {
                if (repeatRange.accept(sc.getStartBit()))
                {
                    repeatingSignals.add(sc);
                }
                else
                {
                    addAction(createSignal(mc, sc));
                }
            }
        });
        if (!repeatingSignals.isEmpty())
        {
            addAction(createRepeatingSignals(mc, repeatingSignals, repeatCountSignal));
        }
        if (rootMultiplexor != null)
        {
            if (!mpxMap.isEmpty())
            {
                mpxMap.forEach((sc, map) ->
                {
                    RtMultiplexor em = extendedMultiplexors.get(sc);
                    if (em != null)
                    {
                        map.forEach((i, l) -> l.add(i, em));
                    }
                    IndexMap.Builder<ArrayAction<T>> mpxBuilder = new IndexMap.Builder<>();
                    map.forEach((i, l) -> mpxBuilder.put(i, createAction(l)));
                    IndexMap<ArrayAction<T>> indexMap = mpxBuilder.build();
                    if (em != null)
                    {
                        em.setMap(indexMap);
                    }
                    else
                    {
                        rootMultiplexor.setMap(indexMap);
                    }
                });
            }
            else
            {
                if (signals.size() == 1)
                {
                    signals.clear();
                }
            }
        }
        return combineRunnables(signals);
    }

    private ArrayAction<T> combineRunnables(List<ArrayAction<T>> sigs)
    {
        if (!sigs.isEmpty())
        {
            ArrayAction<T>[] array = createArray(sigs);
            int length = array.length;
            return (ctx, buf) ->
            {
                for (int ii = 0; ii < length; ii++)
                {
                    ArrayAction<T> c = array[ii];
                    c.run(ctx, buf);
                }
            };
        }
        else
        {
            return null;
        }
    }

    private ArrayAction<T> createRepeatingSignals(MessageClass mc, List<SignalClass> repeatingSignals, SignalClass repeatCountSignal)
    {
        ArrayAction<T> startRepeat = compiler.compileBeginRepeat(mc);
        ArrayAction<T> endRepeat = compiler.compileEndRepeat(mc);
        List<ArrayAction<T>> list = new ArrayList<>();
        IntRange repeatRange = mc.getRepeatRange();
        int repeatSize = repeatRange.getSize();
        int repeatStart = repeatRange.getFrom();
        int maxRepeatCount = (223*8- repeatStart) / repeatSize;
        if (maxRepeatCount < 2)
        {
            throw new UnsupportedOperationException("should not happen");
        }
        for (int ii = 0; ii < maxRepeatCount; ii++)
        {
            list.add(createRepeat(mc, repeatingSignals, ii * repeatSize));
        }
        ArrayAction<T>[] repeatables = createArray(list);
        return (ctx, buf) ->
        {
            int repeatCount;
            if (repeatCountSignal != null)
            {
                repeatCount = ArrayFuncs.getIntFunction(repeatCountSignal.getStartBit(), repeatCountSignal.getSize(), repeatCountSignal.getByteOrder() == ByteOrder.BIG_ENDIAN, repeatCountSignal.getValueType() == ValueType.SIGNED).applyAsInt(buf);
            }
            else
            {
                repeatCount = maxRepeatCount;
            }
            for (int ii = 0; ii < repeatCount; ii++)
            {
                if (startRepeat != null)
                {
                    startRepeat.run(ctx, buf);
                }
                ArrayAction<T> repeatable = repeatables[ii];
                if (repeatable != null)
                {
                    repeatable.run(ctx, buf);
                }
                if (endRepeat != null)
                {
                    endRepeat.run(ctx, buf);
                }
            }
        };
    }

    private ArrayAction<T> createRepeat(MessageClass mc, List<SignalClass> repeatingSignals, int off)
    {
        List<ArrayAction<T>> list = new ArrayList<>();
        repeatingSignals.forEach(s ->
        {
            ArrayAction<T> r = createSignal(mc, s, off);
            if (r != null)
            {
                list.add(r);
            }
        });
        return combineRunnables(list);
    }

    private ArrayAction<T> createSignal(MessageClass mc, SignalClass sc)
    {
        return createSignal(mc, sc, 0);
    }

    private ArrayAction<T> createSignal(MessageClass mc, SignalClass sc, int off)
    {
        return compiler.compile(
                mc, 
                sc, 
                off
        );
    }

    private void addAction(ArrayAction<T> act)
    {
        if (act != null)
        {
            signals.add(act);
        }
    }

    private ArrayAction<T>[] createArray(List<ArrayAction<T>> list)
    {
        return list.toArray((ArrayAction[]) Array.newInstance(ArrayAction.class, list.size()));
    }

    private ArrayAction<T> createAction(List<ArrayAction<T>> l)
    {
        ArrayAction<T>[] arr = createArray(l);
        return (ctx, buf) ->
        {
            for (ArrayAction<T> r : arr)
            {
                r.run(ctx, buf);
            }
            ;
        };
    }

    private MapList<Integer, ArrayAction<T>> getMpx(SignalClass multiplexor)
    {
        MapList<Integer, ArrayAction<T>> ml = mpxMap.get(multiplexor);
        if (ml == null)
        {
            ml = new HashMapList<>();
            mpxMap.put(multiplexor, ml);
        }
        return ml;
    }
    
    private class RtMultiplexor<T> implements ArrayAction<T>
    {
        private IndexMap<ArrayAction<T>> map;
        private final int offset;
        private final int length;
        private final boolean bigEndian;
        private final boolean signed;

        private RtMultiplexor(int offset, int length, boolean bigEndian, boolean signed)
        {
            this.offset = offset;
            this.length = length;
            this.bigEndian = bigEndian;
            this.signed = signed;
        }

        private void setMap(IndexMap<ArrayAction<T>> map)
        {
            this.map = map;
        }
        
        @Override
        public void run(T ctx, CanSource src)
        {
            ToIntFunction<CanSource> is = ArrayFuncs.getIntFunction(offset, length, bigEndian, signed);
            int index = is.applyAsInt(src);
            ArrayAction<T> act = map.get(index);
            if (act != null)
            {
                act.run(ctx, src);
            }
        }
        
    }
}
