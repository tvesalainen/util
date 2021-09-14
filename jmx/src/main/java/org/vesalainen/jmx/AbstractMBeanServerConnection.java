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

import java.io.ObjectInputStream;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AbstractMBeanServerConnection extends JavaLogging implements MBeanServer
{
    private final MBeanServer server;

    public AbstractMBeanServerConnection(MBeanServer server)
    {
        super(AbstractMBeanServerConnection.class);
        this.server = server;
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        return server.createMBean(className, name);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        return server.createMBean(className, name, loaderName);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        return server.createMBean(className, name, params, signature);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        return server.createMBean(className, name, loaderName, params, signature);
    }

    @Override
    public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        return server.registerMBean(object, name);
    }

    @Override
    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException
    {
        server.unregisterMBean(name);
    }

    @Override
    public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException
    {
        return server.getObjectInstance(name);
    }

    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
    {
        return server.queryMBeans(name, query);
    }

    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
    {
        return server.queryNames(name, query);
    }

    @Override
    public boolean isRegistered(ObjectName name)
    {
        return server.isRegistered(name);
    }

    @Override
    public Integer getMBeanCount()
    {
        return server.getMBeanCount();
    }

    @Override
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
    {
        return server.getAttribute(name, attribute);
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException
    {
        return server.getAttributes(name, attributes);
    }

    @Override
    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        server.setAttribute(name, attribute);
    }

    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException
    {
        return server.setAttributes(name, attributes);
    }

    @Override
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException
    {
        return server.invoke(name, operationName, params, signature);
    }

    @Override
    public String getDefaultDomain()
    {
        return server.getDefaultDomain();
    }

    @Override
    public String[] getDomains()
    {
        return server.getDomains();
    }

    @Override
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        server.addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        server.addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        server.removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        server.removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        server.removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        server.removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        return server.getMBeanInfo(name);
    }

    @Override
    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException
    {
        return server.isInstanceOf(name, className);
    }

    @Override
    public Object instantiate(String className) throws ReflectionException, MBeanException
    {
        return server.instantiate(className);
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        return server.instantiate(className, loaderName);
    }

    @Override
    public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException
    {
        return server.instantiate(className, params, signature);
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        return server.instantiate(className, loaderName, params, signature);
    }

    @Override
    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException
    {
        return server.deserialize(name, data);
    }

    @Override
    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException
    {
        return server.deserialize(className, data);
    }

    @Override
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException
    {
        return server.deserialize(className, loaderName, data);
    }

    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException
    {
        return server.getClassLoaderFor(mbeanName);
    }

    @Override
    public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException
    {
        return server.getClassLoader(loaderName);
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository()
    {
        return server.getClassLoaderRepository();
    }
    
}
