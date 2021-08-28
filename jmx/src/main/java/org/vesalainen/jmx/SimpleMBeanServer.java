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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidApplicationException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.ClassLoaderRepository;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleMBeanServer implements MBeanServer
{
    private final String defaultDomain;
    private final MBeanServer outer;
    private final MBeanServerDelegate delegate;
    private final Map<ObjectName,Object> mBeans = new ConcurrentHashMap<>();
    private final AtomicLong sequenceNumber = new AtomicLong();

    SimpleMBeanServer(String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate)
    {
        this.defaultDomain = defaultDomain == null ? "DefaultDomain" : defaultDomain;
        this.outer = outer == null ? this : outer;
        this.delegate = delegate;
        try
        {
            registerMBean(delegate, MBeanServerDelegate.DELEGATE_NAME);
        }
        catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        try
        {
            Objects.requireNonNull(object, "object == null");
            MBeanRegistration mBeanRegistration = null;
            if (object instanceof MBeanRegistration)
            {
                mBeanRegistration = (MBeanRegistration) object;
            }
            if (mBeanRegistration != null)
            {
                try
                {
                    name = mBeanRegistration.preRegister(this, name);
                }
                catch (Exception ex)
                {
                    throw new MBeanRegistrationException(ex);
                }
            }
            Objects.requireNonNull(name, "name == null");
            checkMBean(object, name);
            mBeans.put(name, object);
            delegate.sendNotification(new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, this, sequenceNumber.incrementAndGet(), name));
            if (mBeanRegistration != null)
            {
                mBeanRegistration.postRegister(Boolean.TRUE);
            }
            return new ObjectInstance(name, object.getClass().getName());
        }
        catch (RuntimeException ex)
        {
            throw new RuntimeOperationsException(ex);
        }
    }

    @Override
    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException
    {
        Objects.requireNonNull(name, "name == null");
        Object object = mBeans.get(name);
        if (object == null)
        {
            throw new InstanceNotFoundException(name.toString());
        }
        MBeanRegistration mBeanRegistration = null;
        if (object instanceof MBeanRegistration)
        {
            mBeanRegistration = (MBeanRegistration) object;
            try
            {
                mBeanRegistration.preDeregister();
            }
            catch (RuntimeException ex)
            {
                throw new RuntimeMBeanException(ex);
            }
            catch (Exception ex)
            {
                throw new RuntimeMBeanException(new RuntimeException(ex));
            }
        }
        mBeans.remove(object);
        if (object instanceof MBeanRegistration)
        {
            mBeanRegistration = (MBeanRegistration) object;
            try
            {
                mBeanRegistration.postDeregister();
            }
            catch (RuntimeException ex)
            {
                throw new RuntimeMBeanException(ex);
            }
            catch (Exception ex)
            {
                throw new RuntimeMBeanException(new RuntimeException(ex));
            }
        }
    }

    @Override
    public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException
    {
        Object object = mBeans.get(name);
        if (object == null)
        {
            throw new InstanceNotFoundException(name.toString());
        }
        return new ObjectInstance(name, object.getClass().getName());
    }

    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
    {
        return queryNames(name, query)
                .stream()
                .map((n)->new ObjectInstance(n, mBeans.get(n).getClass().getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
    {
        return mBeans
                .keySet()
                .stream()
                .filter((n)->match(n, name, query))
                .collect(Collectors.toSet());
    }

    private boolean match(ObjectName target, ObjectName name, QueryExp query)
    {
        try
        {
            return (name == null || name.apply(target)) && (query == null || query.apply(target));
        }
        catch (BadStringOperationException | BadBinaryOpValueExpException | BadAttributeValueExpException | InvalidApplicationException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    @Override
    public boolean isRegistered(ObjectName name)
    {
        return mBeans.containsKey(name);
    }

    @Override
    public Integer getMBeanCount()
    {
        return mBeans.size();
    }

    @Override
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDefaultDomain()
    {
        return defaultDomain;
    }

    @Override
    public String[] getDomains()
    {
        Set<String> set = mBeans.keySet().stream().map((n)->n.getDomain()).collect(Collectors.toSet());
        return set.toArray(new String[set.size()]);
    }

    @Override
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object instantiate(String className) throws ReflectionException, MBeanException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void checkMBean(Object object, ObjectName name) throws NotCompliantMBeanException
    {
        int count = 0;
        Class<? extends Object> cls = object.getClass();
        while (cls != null)
        {
            for (Class<?> itf : cls.getInterfaces())
            {
                if (isMBean(itf))
                {
                    count++;
                }
            }
            cls = cls.getSuperclass();
        }
        if (count != 1)
        {
            throw new NotCompliantMBeanException(name.toString());
        }
    }

    private boolean isMBean(Class<?> itf)
    {
        return JMX.isMXBeanInterface(itf) || itf.getName().endsWith("MBean");
    }
    
}
