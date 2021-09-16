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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.DoubleSupplier;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.MultiplexerIndicator;
import org.vesalainen.can.dbc.SignalClass;
import static org.vesalainen.can.dbc.ValueType.SIGNED;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.can.n2k.N2KPgns;
import org.vesalainen.management.SimpleNotificationEmitter;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.HexUtil;
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
    protected final String NOTIF_PREFIX = "org.vesalainen.can.notif.";
    protected final String NOTIF_HEX_TYPE = NOTIF_PREFIX+"HEX";
    protected final MessageClass messageClass;
    protected final int canId;
    protected byte[] buf;
    protected String name;
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
    protected SimpleNotificationEmitter emitter;
    protected ObjectName objectName;
    protected final Executor executor;
    protected int updateCount;
    protected int executeCount;
    protected MBeanNotificationInfo[] mBeanNotificationInfos;

    protected AbstractMessage(Executor executor, MessageClass messageClass, int canId)
    {
        super(AbstractMessage.class);
        this.executor = executor;
        this.messageClass = messageClass;
        this.canId = canId;
        // notif
        mBeanNotificationInfos = new MBeanNotificationInfo[1];
        String[] types;
        if (messageClass != null)
        {
            types = new String[messageClass.getSignalCount()+1];
            Object[] arr = messageClass.getSignals().keySet().toArray();
            for (int ii=0;ii<arr.length;ii++)
            {
                types[ii+1] = NOTIF_PREFIX+arr[ii];
            }
        }
        else
        {
            types = new String[1];
        }
        mBeanNotificationInfos[0] = new MBeanNotificationInfo(types, Notification.class.getName(), "CAN Signals");
        types[0] = NOTIF_HEX_TYPE;
    }

    void registerMBean()
    {
        try
        {
            this.objectName = getObjectName();
            this.emitter = new SimpleNotificationEmitter(
                    executor, 
                    "org.vesalainen.can.notification", 
                    objectName, 
                    mBeanNotificationInfos
                    );
            emitter.setAttach(this::attach);
            emitter.setDetach(this::detach);
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            server.registerMBean(this, objectName);
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException  ex)
        {
            throw new RuntimeException(ex);
        }
    }

    void unregisterMBean()
    {
        try
        {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
            objectName = null;
            emitter = null;
            detach();
        }
        catch (InstanceNotFoundException | MBeanRegistrationException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    protected void attach()
    {
        jmxAction = compileSignals(new JmxCompiler());
        info("attach JMX %s", name);
    }
    protected void detach()
    {
        jmxAction = null;
        info("detach JMX %s", name);
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
        return name;
    }

    @Override
    public int getPgn()
    {
        return PGN.pgn(canId);
    }

    @Override
    public String getPgnName()
    {
        return N2KPgns.MAP.get(getPgn());
    }

    @Override
    public int getSource()
    {
        return PGN.sourceAddress(canId);
    }

    @Override
    public int getPriority()
    {
        return PGN.messagePriority(canId);
    }

    @Override
    public int getUpdateCount()
    {
        return updateCount;
    }

    @Override
    public int getExecuteCount()
    {
        return executeCount;
    }

    @Override
    public int getRepeatSize()
    {
        return repeatSize;
    }

    @Override
    public int getRepeatStart()
    {
        return repeatStart;
    }

    @Override
    public int getRepeatCount()
    {
        return repeatCount;
    }

    @Override
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
    
    @Override
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
    protected abstract boolean update(AbstractCanService service);

    protected abstract void execute();

    protected void sendJmx()
    {
        if (jmxAction != null)
        {
            emitter.sendNotification(()->NOTIF_HEX_TYPE, ()->HexUtil.toString(buf), System::currentTimeMillis);
            jmxAction.run();
        }
    }
    void addSignals(SignalCompiler compiler)
    {
        if (messageClass != null)
        {
            action = compileSignals(compiler);
        }
    }
    Runnable compileSignals(SignalCompiler compiler)
    {
        Objects.requireNonNull(messageClass, "MessageClass null");
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
            if (!sigs.isEmpty())
            {
                Runnable[] array = createArray(sigs);
                int length = array.length;
                return ()->
                {
                    for (int ii=0;ii<length;ii++)
                    {
                        Runnable c = array[ii];
                        c.run();
                    }
                };
            }
            else
            {
                return null;
            }
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
            Supplier<String> text = ()->supplier.getAsInt()+unit;
            return ()->emitter.sendNotification(()->NOTIF_PREFIX+name, text, System::currentTimeMillis);
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, LongSupplier supplier)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            Supplier<String> text = ()->supplier.getAsLong()+unit;
            return ()->emitter.sendNotification(()->NOTIF_PREFIX+name, text, System::currentTimeMillis);
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, DoubleSupplier supplier)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            Supplier<String> text = ()->supplier.getAsDouble()+unit;
            return ()->emitter.sendNotification(()->NOTIF_PREFIX+name, text, System::currentTimeMillis);
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, IntSupplier supplier, IntFunction<String> map)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            Supplier<String> text = ()->map.apply(supplier.getAsInt())+unit;
            return ()->emitter.sendNotification(()->NOTIF_PREFIX+name, text, System::currentTimeMillis);
        }

        @Override
        public Runnable compile(MessageClass mc, SignalClass sc, Supplier<String> ss)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            Supplier<String> text = ()->ss.get()+unit;
            return ()->emitter.sendNotification(()->NOTIF_PREFIX+name, text, System::currentTimeMillis);
        }

    }
}
