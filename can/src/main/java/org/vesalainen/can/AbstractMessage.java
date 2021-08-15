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

import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import static java.nio.ByteOrder.BIG_ENDIAN;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.management.InstanceAlreadyExistsException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.MultiplexerIndicator;
import org.vesalainen.can.dbc.SignalClass;
import static org.vesalainen.can.dbc.ValueType.SIGNED;
import org.vesalainen.management.SimpleNotificationEmitter;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.IndexMap;
import org.vesalainen.util.IntRange;
import org.vesalainen.util.MapList;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractMessage extends JavaLogging implements CanMXBean, NotificationEmitter
{
    protected final MessageClass messageClass;
    protected final int canId;
    protected byte[] buf;
    protected String comment;
    protected Runnable action;
    protected Runnable jmxAction;
    protected int maxRepeatCount;
    protected Runnable[] repeatables;
    protected int repeatSize;
    protected int repeatStart;
    protected int repeatCount;
    protected Runnable startRepeat;
    protected Runnable endRepeat;
    private int currentBytes;
    protected SimpleNotificationEmitter<String> emitter;
    protected ObjectName objectName;
    protected final Executor executor;

    protected AbstractMessage(Executor executor, MessageClass messageClass, int canId)
    {
        super(AbstractMessage.class);
        this.executor = executor;
        this.messageClass = messageClass;
        this.canId = canId;
    }

    private void initJmx()
    {
        try
        {
            this.objectName = getObjectName();
            this.emitter = new SimpleNotificationEmitter(
                    executor, 
                    "org.vesalainen.can.notification", 
                    objectName, 
                    new MBeanNotificationInfo(
                            new String[]{"org.vesalainen.can.notification"},
                            "javax.management.Notification",
                            "CAN signals")
                    );
            emitter.setAttach(this::attach);
            emitter.setDetach(this::detach);
            ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private void attach()
    {
        jmxAction = compileSignals(new JmxCompiler());
    }
    private void detach()
    {
        jmxAction = null;
    }

    @Override
    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
    {
        emitter.removeNotificationListener(listener, filter, handback);
    }

    @Override
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
    {
        emitter.addNotificationListener(listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
    {
        emitter.removeNotificationListener(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo()
    {
        if (objectName == null)
        {
            initJmx();
        }
        return emitter.getNotificationInfo();
    }
    
    @Override
    public int getCanId()
    {
        return canId;
    }

    @Override
    public String getComment()
    {
        return comment;
    }

    public int getRepeatSize()
    {
        return repeatSize;
    }

    public int getRepeatStart()
    {
        return repeatStart;
    }

    public int getRepeatCount()
    {
        return repeatCount;
    }

    public int getCurrentBytes()
    {
        return currentBytes;
    }

    public void setCurrentBytes(int currentBytes)
    {
        this.currentBytes = currentBytes;
    }
    
    public int getCurrentBits()
    {
        return getCurrentBytes()*8;
    }
    
    public abstract int getMaxBytes();
    
    public int getMaxBits()
    {
        return getMaxBytes()*8;
    }
    protected abstract ObjectName getObjectName() throws MalformedObjectNameException;
    /**
     * Updates CanProcessor data. Returns true if needs to execute.
     * @param service
     * @return 
     */
    protected boolean update(AbstractCanService service)
    {
        return jmxAction != null;
    }
    protected void execute()
    {
        if (jmxAction != null)
        {
            jmxAction.run();
        }
    }
    
    void addSignals(SignalCompiler compiler)
    {
        action = compileSignals(compiler);
    }
    Runnable compileSignals(SignalCompiler compiler)
    {
        IntRange repeatRange = messageClass.getRepeatRange();
        ActionBuilder actionBuilder = new ActionBuilder(messageClass, compiler, repeatRange);
        return actionBuilder.build();
    }

    private class ActionBuilder
    {
        private MessageClass mc;
        private SignalCompiler compiler;
        private IntRange repeatRange;
        private List<Runnable> signals = new ArrayList<>();
        private Map<SignalClass,MapList<Integer,Runnable>> mpxMap = new HashMap<>();
        private RtMultiplexor rootMultiplexor;
        private Map<SignalClass,RtMultiplexor> extendedMultiplexors = new HashMap<>();

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
                            RtMultiplexor multiplexor = new RtMultiplexor(is);
                            extendedMultiplexors.put(sc, multiplexor);
                        }
                        else
                        {
                            rootMultiplexor = new RtMultiplexor(is);
                            addAction(rootMultiplexor);
                        }
                    }
                    else
                    {
                        Runnable act = createSignal(mc, sc);
                        if (act != null)
                        {
                            SignalClass multiplexor = multiplexerIndicator.getMultiplexor();
                            MapList<Integer, Runnable> ml = getMpx(multiplexor);
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
                    RtMultiplexor em = extendedMultiplexors.get(sc);
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

        private MapList<Integer, Runnable> getMpx(SignalClass multiplexor)
        {
            MapList<Integer, Runnable> ml = mpxMap.get(multiplexor);
            if (ml == null)
            {
                ml = new HashMapList<>();
                mpxMap.put(multiplexor, ml);
            }
            return ml;
        }

    }
    private class RtMultiplexor implements Runnable
    {
        private final IntSupplier supplier;
        private IndexMap<Runnable> map;

        private RtMultiplexor(IntSupplier supplier)
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
    public static NullMessage getNullMessage(Executor executor, int canId)
    {
        return new NullMessage(executor, canId);
    }
    public static class NullMessage extends AbstractMessage
    {

        private NullMessage(Executor executor, int canId)
        {
            super(executor, null, canId);
        }

        @Override
        public int getMaxBytes()
        {
            return 0;
        }

        @Override
        protected ObjectName getObjectName() throws MalformedObjectNameException
        {
            return new ObjectName("org.vesalainen.can:type=Unknown"+canId);
        }

    }
    private class JmxCompiler implements SignalCompiler
    {

        public JmxCompiler()
        {
        }

        @Override
        public boolean needCompilation(int canId)
        {
            return true;
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return ()->emitter.sendNotification(name+"="+supplier.getAsInt()+unit, "", System.currentTimeMillis());
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, LongSupplier supplier)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return ()->emitter.sendNotification(name+"="+supplier.getAsLong()+unit, "", System.currentTimeMillis());
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier supplier)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return ()->emitter.sendNotification(name+"="+supplier.getAsDouble()+unit, "", System.currentTimeMillis());
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return ()->emitter.sendNotification(name+"="+map.apply(supplier.getAsInt())+unit, "", System.currentTimeMillis());
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> ss)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return ()->emitter.sendNotification(name+"="+ss.get()+unit, "", System.currentTimeMillis());
        }

    }
}
