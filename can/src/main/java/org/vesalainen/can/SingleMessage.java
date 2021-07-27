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
import java.util.List;
import java.util.concurrent.Callable;
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

    public SingleMessage(int canId, int len, String comment)
    {
        this.source = PGN.sourceAddress(canId);
        this.priority = PGN.messagePriority(canId);
        this.pgn = PGN.pgn(canId);
        this.buf = new byte[len];
        this.comment = comment;
    }
    @Override
    protected boolean update(AbstractCanService service)
    {
        try
        {
            ByteBuffer frame = service.getFrame();
            frame.position(8);
            frame.get(buf, 0, min(buf.length, frame.remaining()));
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
        ActionBuilder actionBuilder = new ActionBuilder(mc, compiler);
        action = actionBuilder.build();
    }
    private class ActionBuilder
    {
        private MessageClass mc;
        private SignalCompiler compiler;
        private List<Runnable> signals = new ArrayList<>();
        private MapList<Integer,Runnable> mpxMap = new HashMapList<>();
        private Multiplexor multiplexor;

        public ActionBuilder(MessageClass mc, SignalCompiler compiler)
        {
            this.mc = mc;
            this.compiler = compiler;
        }
        
        private Runnable build()
        {
            add(createBegin(mc, compiler));
            mc.forEach((s)->
            {
                finer("add signal %s", s);
                MultiplexerIndicator mpxI = s.getMultiplexerIndicator();
                if (mpxI != null)
                {
                    if (mpxI.isMultiplexor())
                    {
                        createMultiplexor(s);
                    }
                    else
                    {
                        mpxMap.add(mpxI.getValue(), createSignal(mc, s, compiler));
                    }
                }
                else
                {
                    add(createSignal(mc, s, compiler));
                }
            });
            add(createEnd(mc, compiler));
            if (multiplexor != null)
            {
                IndexMap.Builder<Runnable> mpxBuilder = new IndexMap.Builder<>();
                mpxMap.forEach((i,l)->mpxBuilder.put(i, createAction(l)));
                IndexMap<Runnable> indexMap = mpxBuilder.build();
                multiplexor.setMap(indexMap);
            }
            Runnable[] array = createArray(signals);
            return ()->
            {
                for (Runnable c : array)
                {
                    c.run();
                }
            };
        }

        private void createMultiplexor(SignalClass sc)
        {
            IntSupplier is = ArrayFuncs.getIntSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
            multiplexor = new Multiplexor(is);
            add(multiplexor);
        }
        private Runnable createBegin(MessageClass mc, SignalCompiler compiler)
        {
            return compiler.compileBegin(mc);
        }
        private Runnable createSignal(MessageClass mc, SignalClass sc, SignalCompiler compiler)
        {
            IntSupplier is;
            LongSupplier ls;
            DoubleSupplier ds;
            Supplier<String> ss;
            switch (sc.getSignalType())
            {
                case INT:
                    is = ArrayFuncs.getIntSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    return compiler.compile(mc, sc, is);
                case LONG:
                    ls = ArrayFuncs.getLongSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    return compiler.compile(mc, sc, ls);
                case DOUBLE:
                    ls = ArrayFuncs.getLongSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    double factor = sc.getFactor();
                    double offset = sc.getOffset();
                    ds = ()->factor*ls.getAsLong()+offset;
                    return compiler.compile(mc, sc, ds);
                case LOOKUP:
                    is = ArrayFuncs.getIntSupplier(sc.getStartBit(), sc.getSize(), sc.getByteOrder()==BIG_ENDIAN, sc.getValueType()==SIGNED, buf);
                    IntFunction<String> f = sc.getMapper();
                    if (f == null)
                    {
                        return compiler.compile(mc, sc, is);
                    }
                    return compiler.compile(mc, sc, is, f);
                case BINARY:
                    return compiler.compileBinary(mc, sc);
                case ASCIIZ:
                    ss = ArrayFuncs.getZeroTerminatingStringSupplier(sc.getStartBit()/8, sc.getSize()/8, buf);
                    return compiler.compileASCII(mc, sc, ss);
                default:
                    throw new UnsupportedOperationException(sc.getSignalType()+" not supported");
            }
        }
        private Runnable createEnd(MessageClass mc, SignalCompiler compiler)
        {
            return compiler.compileEnd(mc);
        }
        private void add(Runnable act)
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
