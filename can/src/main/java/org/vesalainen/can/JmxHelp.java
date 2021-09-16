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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class JmxHelp extends JavaLogging implements NotificationListener, NotificationEmitter, JmxHelpMXBean, MBeanRegistration
{

    private final Map<NotificationListener,?> listeners = new WeakHashMap<>();
    private MBeanServerDelegate delegate;
    private ObjectName objectName;

    public JmxHelp()
    {
        super(JmxHelp.class);
        try
        {
            this.objectName = new ObjectName("org.vesalainen.can:type=help");
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            platformMBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this, null, null);
            delegate = new MBeanServerDelegate();
            platformMBeanServer.registerMBean(this, objectName);
            //platformMBeanServer.registerMBean(delegate, MBeanServerDelegate.DELEGATE_NAME);
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException | InstanceNotFoundException  ex)
        {
            Logger.getLogger(JmxHelp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handleNotification(Notification notification, Object handback)
    {
        info("handleNotification(%s)", notification);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
    {
        ArrayList<MBeanServer> findMBeanServer = MBeanServerFactory.findMBeanServer(null);
        Set<ObjectName> queryNames = ManagementFactory.getPlatformMBeanServer().queryNames(null, null);
        Set<ObjectInstance> queryMBeans = ManagementFactory.getPlatformMBeanServer().queryMBeans(null, null);
        listeners.put(listener, null);
        try
        {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(objectName);
        }
        catch (InstanceNotFoundException | MBeanRegistrationException ex)
        {
            Logger.getLogger(JmxHelp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo()
    {
        return new MBeanNotificationInfo[]{new MBeanNotificationInfo(
                new String[]{"org.vesalainen.can.notification"},
                "javax.management.Notification",
                "CAN signals")};
    }

    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
    {
        return name;
    }

    @Override
    public void postRegister(Boolean registrationDone)
    {
    }

    @Override
    public void preDeregister() throws Exception
    {
    }

    @Override
    public void postDeregister()
    {
    }
}
