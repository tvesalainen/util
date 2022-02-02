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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import static java.util.logging.Level.WARNING;
import javax.management.InstanceAlreadyExistsException;
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
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.can.n2k.N2KPgns;
import org.vesalainen.management.SimpleNotificationEmitter;
import org.vesalainen.util.HexUtil;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class AbstractMessage extends JavaLogging implements Frame, CanMXBean, NotificationEmitter
{
    protected final String NOTIF_PREFIX = "org.vesalainen.can.notif.";
    protected final String NOTIF_HEX_TYPE = NOTIF_PREFIX+"HEX";
    protected final MessageClass messageClass;
    protected final int canId;
    protected byte[] buf;
    protected String name;
    protected Transaction action;
    protected Transaction jmxAction;
    private int currentBytes;
    protected SimpleNotificationEmitter emitter;
    protected ObjectName objectName;
    protected final Executor executor;
    protected int updateCount;
    private int executeCount;
    protected MBeanNotificationInfo[] mBeanNotificationInfos;
    private final long startMillis;
    private boolean hasSignals;

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
        types[0] = NOTIF_HEX_TYPE;
        mBeanNotificationInfos[0] = new MBeanNotificationInfo(types, Notification.class.getName(), "CAN Signals");
        this.startMillis = System.currentTimeMillis();
    }

    void registerMBean()
    {
        try
        {
            this.objectName = getObjectName();
            this.emitter = new SimpleNotificationEmitter(
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
        catch (Exception ex)
        {
            log(WARNING, ex, "unregisterMBean(%s)", objectName);
        }
    }

    @Override
    public float getFrequency()
    {
        return 1000F*executeCount/(System.currentTimeMillis()-startMillis);
    }
    
    protected void attach()
    {
        jmxAction = compileSignals(new JmxCompiler());
        if (action == null)
        {
            action = new Transaction();
        }
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

    public boolean hasSignals()
    {
        return hasSignals;
    }

    public void setSignals(boolean hasSignals)
    {
        this.hasSignals = hasSignals;
    }

    protected abstract ObjectName getObjectName() throws MalformedObjectNameException;

    @Override
    public void frame(long time, int canId, int dataLength, long data)
    {
        updateCount++;
        if (update(time, canId, dataLength, data))
        {
            action.run();
            executeCount++;
            if (jmxAction != null)
            {
                jmxAction.run();
                emitter.sendNotification2(()->NOTIF_HEX_TYPE, ()->HexUtil.toString(buf), ()->time);
            }
        }
    }
    protected abstract boolean update(long time, int canId, int dataLength, long data);
    protected abstract long getMillis();

    public void addSignals(SignalCompiler compiler)
    {
        if (messageClass != null)
        {
            action = compileSignals(compiler);
        }
    }
    protected <T> Transaction compileSignals(SignalCompiler<T> compiler)
    {
        Objects.requireNonNull(messageClass, "MessageClass null");
        ActionBuilder<T> actionBuilder = new ActionBuilder<>(messageClass, compiler);
        Runnable begin = compiler.compileBegin(messageClass, canId, ()->getMillis());
        Runnable act = actionBuilder.build(buf);
        Consumer<Throwable> end = compiler.compileEnd(messageClass);
        return new Transaction(begin, act, end);
    }

    public static NullMessage getNullMessage(Executor executor, int canId)
    {
        return new NullMessage(executor, canId);
    }
    private class JmxCompiler implements SignalCompiler<Object>
    {

        private LongSupplier millisSupplier;

        public long getMillis()
        {
            return millisSupplier.getAsLong();
        }

        @Override
        public boolean needCompilation(int canId)
        {
            return true;
        }

        @Override
        public Runnable compileBegin(MessageClass mc, int canId, LongSupplier millisSupplier)
        {
            this.millisSupplier = millisSupplier;
            return compileBegin(mc, canId, millisSupplier);
        }

        @Override
        public ArrayAction<Object> compile(MessageClass mc, SignalClass sc, ToIntFunction<byte[]> toIntFunction)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return (ctx, buf)->emitter.sendNotification2(()->NOTIF_PREFIX+name, ()->toIntFunction.applyAsInt(buf)+unit, this::getMillis);
        }

        @Override
        public ArrayAction<Object> compile(MessageClass mc, SignalClass sc, ToLongFunction<byte[]> toLongFunction)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return (ctx, buf)->emitter.sendNotification2(()->NOTIF_PREFIX+name, ()->toLongFunction.applyAsLong(buf)+unit, this::getMillis);
        }

        @Override
        public ArrayAction<Object> compile(MessageClass mc, SignalClass sc, ToDoubleFunction<byte[]> toDoubleFunction)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return (ctx, buf)->emitter.sendNotification2(()->NOTIF_PREFIX+name, ()->toDoubleFunction.applyAsDouble(buf)+unit, this::getMillis);
        }

        @Override
        public ArrayAction<Object> compile(MessageClass mc, SignalClass sc, ToIntFunction<byte[]> toIntFunction, IntFunction<String> map)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return (ctx, buf)->emitter.sendNotification2(()->NOTIF_PREFIX+name, ()->map.apply(toIntFunction.applyAsInt(buf))+unit, this::getMillis);
        }

        @Override
        public ArrayAction<Object> compile(MessageClass mc, SignalClass sc, Function<byte[], String> stringFunction)
        {
            String name = sc.getName();
            String unit = sc.getUnit();
            return (ctx, buf)->emitter.sendNotification2(()->NOTIF_PREFIX+name, ()->stringFunction.apply(buf)+unit, this::getMillis);
        }

    }
}
