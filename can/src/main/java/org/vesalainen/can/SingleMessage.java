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

import static java.lang.Integer.min;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import static java.nio.ByteOrder.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import static java.util.logging.Level.*;
import static org.vesalainen.can.SignalType.*;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.MultiplexerIndicator;
import org.vesalainen.can.dbc.SignalClass;
import static org.vesalainen.can.dbc.ValueType.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.IndexMap;
import org.vesalainen.util.IntRange;
import org.vesalainen.util.MapList;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SingleMessage extends AbstractMessage
{
    protected final int pgn;
    protected final int source;
    protected final int priority;
    protected byte[] buf;
    protected String comment;
    protected Runnable action;
    protected int maxRepeatCount;
    protected Runnable[] repeatables;
    protected int repeatSize;
    protected int repeatStart;
    protected int repeatCount;
    protected Runnable startRepeat;
    protected Runnable endRepeat;

    public SingleMessage(int canId, int len, String comment)
    {
        this.source = PGN.sourceAddress(canId);
        this.priority = PGN.messagePriority(canId);
        this.pgn = PGN.pgn(canId);
        this.buf = new byte[len];
        this.comment = comment;
    }

    @Override
    public int getMaxBytes()
    {
        return 8;
    }

    @Override
    public void setCurrentBytes(int currentBytes)
    {
        super.setCurrentBytes(currentBytes);
        if (repeatSize > 0)
        {
            repeatCount = (getCurrentBits() - repeatStart) / repeatSize;
        }
    }
    
    @Override
    protected boolean update(AbstractCanService service)
    {
        try
        {
            ByteBuffer frame = service.getFrame();
            frame.position(8);
            int remaining = frame.remaining();
            setCurrentBytes(remaining);
            frame.get(buf, 0, min(buf.length, remaining));
            return true;
        }
        catch (Exception ex)
        {
            log(WARNING, ex, "execute %s", comment);
        }
        return false;
    }

    @Override
    protected void execute(CachedScheduledThreadPool executor)
    {
        info("execute pgn=%d src=%d %s\n%s", pgn, source, comment, HexUtil.toString(buf));
        try
        {
            action.run();
        }
        catch (Exception ex)
        {
            log(WARNING, ex, "execute %s", comment);
        }
    }

    void addSignals(MessageClass mc, SignalCompiler compiler)
    {
        IntRange repeatRange = mc.getRepeatRange();
        ActionBuilder actionBuilder = new ActionBuilder(mc, compiler, repeatRange);
        action = actionBuilder.build();
    }
    private class ActionBuilder
    {
        private MessageClass mc;
        private SignalCompiler compiler;
        private IntRange repeatRange;
        private List<Runnable> signals = new ArrayList<>();
        private Map<SignalClass,MapList<Integer,Runnable>> mpxMap = new HashMap<>();
        private Multiplexor rootMultiplexor;
        private Map<SignalClass,Multiplexor> extendedMultiplexors = new HashMap<>();

        public ActionBuilder(MessageClass mc, SignalCompiler compiler, IntRange repeatRange)
        {
            this.mc = mc;
            this.compiler = compiler;
            this.repeatRange = repeatRange;
        }
        
        private Runnable build()
        {
            List<SignalClass> repeatingSignals = new ArrayList<>();
            addAction(createBegin(mc));
            mc.forEach((sc)->
            {
                finer("add signal %s", sc);
                MultiplexerIndicator multiplexerIndicator = sc.getMultiplexerIndicator();
                if (multiplexerIndicator != null)
                {
                    if (multiplexerIndicator.isMultiplexor())
                    {
                        IntSupplier is = ArrayFuncs.getIntSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                        if (multiplexerIndicator.isExtended())
                        {
                            Multiplexor multiplexor = new Multiplexor(is);
                            extendedMultiplexors.put(sc, multiplexor);
                        }
                        else
                        {
                            rootMultiplexor = new Multiplexor(is);
                            addAction(rootMultiplexor);
                        }
                    }
                    else
                    {
                        Runnable act = createSignal(mc, sc);
                        if (act != null)
                        {
                            SignalClass multiplexor = multiplexerIndicator.getMultiplexor();
                            MapList<Integer, Runnable> ml = mpxMap.get(multiplexor);
                            if (ml == null)
                            {
                                ml = new HashMapList<>();
                                mpxMap.put(multiplexor, ml);
                            }
                            multiplexerIndicator.getValues().forEach((i)->ml.add(i, act));
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
                addAction(createRepeatingSignals(mc, repeatingSignals));
                startRepeat = compiler.compileBeginRepeat(mc);
                endRepeat = compiler.compileEndRepeat(mc);
            }
            addAction(createEnd(mc));
            if (rootMultiplexor != null)
            {
                mpxMap.forEach((sc,map)->
                {
                    Multiplexor em = extendedMultiplexors.get(sc);
                    if (em != null)
                    {
                        map.forEach((i, l)->l.add(i, em));
                    }
                    IndexMap.Builder<Runnable> mpxBuilder = new IndexMap.Builder<>();
                    map.forEach((i,l)->mpxBuilder.put(i, createAction(l)));
                    IndexMap<Runnable> indexMap = mpxBuilder.build();
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
            return combineRunnables(signals);
        }
        private Runnable combineRunnables(List<Runnable> sigs)
        {
            Runnable[] array = createArray(sigs);
            return ()->
            {
                for (Runnable c : array)
                {
                    c.run();
                }
            };
        }
        private Runnable createRepeatingSignals(MessageClass mc, List<SignalClass> repeatingSignals)
        {
            List<Runnable> list = new ArrayList<>();
            repeatSize = repeatRange.getSize();
            repeatStart = repeatRange.getFrom();
            maxRepeatCount = (getMaxBits() - repeatStart) / repeatSize;
            if (maxRepeatCount < 2)
            {
                throw new UnsupportedOperationException("should not happen");
            }
            for (int ii=0;ii<maxRepeatCount;ii++)
            {
                list.add(createRepeat(mc, repeatingSignals, ii * repeatSize));
            }
            repeatables = createArray(list);
            return ()->
            {
                for (int ii=0;ii<repeatCount;ii++)
                {
                    startRepeat.run();
                    repeatables[ii].run();
                    endRepeat.run();
                }
            };
        }
        private Runnable createRepeat(MessageClass mc, List<SignalClass> repeatingSignals, int off)
        {
            List<Runnable> list = new ArrayList<>();
            repeatingSignals.forEach((s)->
            {
                Runnable r = createSignal(mc, s, off);
                if (r != null)
                {
                    list.add(r);
                }
            });
            return combineRunnables(list);
        }
        private Runnable createBegin(MessageClass mc)
        {
            return compiler.compileBegin(mc);
        }
        private Runnable createSignal(MessageClass mc, SignalClass sc)
        {
            return createSignal(mc, sc, 0);
        }
        private Runnable createSignal(MessageClass mc, SignalClass sc, int off)
        {
            IntSupplier is;
            LongSupplier ls;
            DoubleSupplier ds;
            Supplier<String> ss;
            switch (sc.getSignalType())
            {
                case INT:
                    is = ArrayFuncs.getIntSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    return compiler.compile(mc, sc, is);
                case LONG:
                    ls = ArrayFuncs.getLongSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    return compiler.compile(mc, sc, ls);
                case DOUBLE:
                    ls = ArrayFuncs.getLongSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    double factor = sc.getFactor();
                    double offset = sc.getOffset();
                    ds = ()->factor*ls.getAsLong()+offset;
                    return compiler.compile(mc, sc, ds);
                case LOOKUP:
                    is = ArrayFuncs.getIntSupplier(sc.getStartBit()+off, sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    IntFunction<String> f = sc.getMapper();
                    if (f == null)
                    {
                        return compiler.compile(mc, sc, is);
                    }
                    return compiler.compile(mc, sc, is, f);
                case BINARY:
                    return compiler.compileBinary(mc, sc);
                case ASCIIZ:
                    ss = ArrayFuncs.getZeroTerminatingStringSupplier((sc.getStartBit()+off)/8, sc.getSize()/8, buf);
                    return compiler.compile(mc, sc, ss);
                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
        }
        private Runnable createEnd(MessageClass mc)
        {
            return compiler.compileEnd(mc);
        }
        private void addAction(Runnable act)
        {
            if (act != null)
            {
                signals.add(act);
            }
        }

        private Runnable[] createArray(List<Runnable> list)
        {
            return list.toArray((Runnable[]) Array.newInstance(Runnable.class, list.size()));
        }

        private Runnable createAction(List<Runnable> l)
        {
            Runnable[] arr = createArray(l);
            return ()->
            {
                for (Runnable r : arr)
                {
                    r.run();
                };
                
            };
        }

    }
    private class Multiplexor implements Runnable
    {
        private final IntSupplier supplier;
        private IndexMap<Runnable> map;

        private Multiplexor(IntSupplier supplier)
        {
            this.supplier = supplier;
        }

        private void setMap(IndexMap<Runnable> map)
        {
            this.map = map;
        }
        
        @Override
        public void run()
        {
            int index = supplier.getAsInt();
            Runnable act = map.get(index);
            if (act != null)
            {
                act.run();
            }
        }
        
    }
}
