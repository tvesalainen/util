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
package org.vesalainen.jmx;

import java.lang.management.ManagementFactory;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleJMXT
{
    
    public SimpleJMXT()
    {
        SimpleJMX.start();
    }

    @Test
    public void testPlatformMBean() throws InstanceNotFoundException, IntrospectionException, ReflectionException, InterruptedException, MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        MBeanServer pbs = ManagementFactory.getPlatformMBeanServer();
        assertEquals("DefaultDomain", pbs.getDefaultDomain());
        String[] domains = pbs.getDomains();
        assertEquals(5, domains.length);
        assertEquals(22, (int)pbs.getMBeanCount());
        Set<ObjectName> queryNames = pbs.queryNames(ObjectName.WILDCARD, null);
        Set<ObjectInstance> queryMBeans = pbs.queryMBeans(ObjectName.WILDCARD, null);
        for (ObjectName on : queryNames)
        {
            MBeanInfo mBeanInfo = pbs.getMBeanInfo(on);
        }
        Testaus test = new Testaus();
        pbs.registerMBean(test, ObjectName.getInstance("org.vesalainen.jmx", "type", "Test"));
        Thread.sleep(100000000);
    }
    
    public static class Testaus implements TestMXBean, NotificationEmitter
    {
        private NotificationBroadcasterSupport nbSupport;
        private CachedScheduledThreadPool executor = new CachedScheduledThreadPool();
        private AtomicLong seq = new AtomicLong();
        public Testaus()
        {
            MBeanNotificationInfo[] info = new MBeanNotificationInfo[]{new MBeanNotificationInfo(
                    new String[]{"org.vesalainen.can.notification"},
                    "javax.management.Notification",
                    "CAN signals")};
            nbSupport = new NotificationBroadcasterSupport(info);
            executor.scheduleAtFixedRate(this::send, 3, 100, TimeUnit.MILLISECONDS);
        }
        
        private void send()
        {
            Notification not = new Notification("org.vesalainen.can.notification", this, seq.incrementAndGet());
            nbSupport.sendNotification(not);
        }
        @Override
        public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
        {
            nbSupport.removeNotificationListener(listener, filter, handback);
        }

        @Override
        public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
        {
            nbSupport.addNotificationListener(listener, filter, handback);
        }

        @Override
        public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
        {
            nbSupport.removeNotificationListener(listener);
        }

        @Override
        public MBeanNotificationInfo[] getNotificationInfo()
        {
            return nbSupport.getNotificationInfo();
        }
        
    }
    public interface TestMXBean
    {
        default String getFoo()
        {
            return "foo";
        }
    }
}
