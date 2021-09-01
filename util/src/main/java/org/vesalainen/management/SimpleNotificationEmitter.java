/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.management;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.MapList;
import org.vesalainen.util.WeakMapList;
import org.vesalainen.util.logging.JavaLogging;

/**
 * NotificationEmitter implementation which tries to minimize cpu load in
 * transmitting part in particular if there are no listeners.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <U> Type of user-data
 */
public class SimpleNotificationEmitter<U> implements NotificationEmitter
{
    private MapList<NotificationListener,ListenerWrapper> map = new WeakMapList<>();
    private String type;
    private ObjectName source;
    private MBeanNotificationInfo[] infos;
    private Executor executor;
    private AtomicLong sequenceNumber = new AtomicLong();
    private Runnable attach;
    private Runnable detach;
    /**
     * Creates NotificationEmitterImpl with gives infos
     * @param infos 
     */
    public SimpleNotificationEmitter(String type, ObjectName source, MBeanNotificationInfo... infos)
    {
        this(Executors.newCachedThreadPool(), type, source, infos);
    }
    /**
     * Creates NotificationEmitterImpl with gives executer and infos
     * @param executor
     * @param infos 
     */
    public SimpleNotificationEmitter(Executor executor, String type, ObjectName source, MBeanNotificationInfo... infos)
    {
        this.executor = executor;
        this.type = type;
        this.source = source;
        this.infos = infos;
    }
    /**
     * Set action that is called when first notification listener enters
     * @param attach 
     */
    public void setAttach(Runnable attach)
    {
        this.attach = attach;
    }
    /**
     * Set action that is called when last notification listener leaves
     * @param detach 
     */
    public void setDetach(Runnable detach)
    {
        this.detach = detach;
    }
    
    /**
     * Send notification. supplier is called only if there are listeners
     * @param textSupplier
     * @param userDataSupplier
     * @param timestampSupplier 
     */
    public synchronized void sendNotification(Supplier<String> textSupplier, Supplier<U> userDataSupplier, LongSupplier timestampSupplier)
    {
        if (!map.isEmpty())
        {
            sendNotification(textSupplier.get(), userDataSupplier.get(), timestampSupplier.getAsLong());
        }
    }
    /**
     * Send notification.
     * @param text
     * @param userData
     * @param timestamp 
     */
    public synchronized void sendNotification(String text, U userData, long timestamp)
    {
        map.valueSet()
                .forEach((ListenerWrapper w)->executor.execute(()->w.sendNotification(text, userData, timestamp)));
    }
    @Override
    public synchronized void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
    {
        ListenerWrapper wrap = new ListenerWrapper(listener, filter, handback);
        if (!map.removeItem(listener, wrap))
        {
            throw new ListenerNotFoundException();
        }
        if (map.isEmpty() && detach != null)
        {
            detach.run();
        }
    }

    @Override
    public synchronized void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
    {
        if (listener == null)
        {
            throw new IllegalArgumentException();
        }
        if (map.isEmpty() && attach != null)
        {
            attach.run();
        }
        ListenerWrapper wrap = new ListenerWrapper(listener, filter, handback);
        map.add(listener, wrap);
    }

    @Override
    public synchronized void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
    {
        if (map.remove(listener) == null)
        {
            throw new ListenerNotFoundException();
        }
        if (map.isEmpty() && detach != null)
        {
            detach.run();
        }
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo()
    {
        return infos;
    }

    private class ListenerWrapper
    {
        private NotificationListener listener;
        private NotificationFilter filter;
        private Object handback;

        public ListenerWrapper(NotificationListener listener, NotificationFilter filter, Object handback)
        {
            this.listener = listener;
            this.filter = filter;
            this.handback = handback;
        }

        public void sendNotification(String text, U userData, long timeStamp)
        {
            Notification notification = new Notification(type, source, sequenceNumber.incrementAndGet(), timeStamp, text);
            notification.setUserData(userData);
            if (filter == null || filter.isNotificationEnabled(notification))
            {
                try
                {
                    listener.handleNotification(notification, handback);
                }
                catch (Exception ex)
                {
                    JavaLogging.getLogger(SimpleNotificationEmitter.class).log(Level.SEVERE, ex, "notify %s", ex.getMessage());
                }
            }
        }
        
        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 29 * hash + Objects.hashCode(this.listener);
            hash = 29 * hash + Objects.hashCode(this.filter);
            hash = 29 * hash + Objects.hashCode(this.handback);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final ListenerWrapper other = (ListenerWrapper) obj;
            if (!Objects.equals(this.listener, other.listener))
            {
                return false;
            }
            if (!Objects.equals(this.filter, other.filter))
            {
                return false;
            }
            if (!Objects.equals(this.handback, other.handback))
            {
                return false;
            }
            return true;
        }
        
    }
}
