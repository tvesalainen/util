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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.Descriptor;
import javax.management.DescriptorKey;
import javax.management.DynamicMBean;
import javax.management.ImmutableDescriptor;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidApplicationException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerDelegate;
import javax.management.MBeanServerNotification;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.StandardEmitterMBean;
import javax.management.StandardMBean;
import javax.management.loading.ClassLoaderRepository;
import javax.management.loading.PrivateClassLoader;
import org.vesalainen.bean.BeanHelper;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleMBeanServer implements MBeanServer
{
    private final String defaultDomain;
    private final MBeanServer outer;
    private final MBeanServerDelegate delegate;
    private final Map<ObjectName,DynamicMBean> mBeans = new ConcurrentHashMap<>();
    private final AtomicLong sequenceNumber = new AtomicLong();
    private final List<ClassLoader> classLoaders = Collections.synchronizedList(new ArrayList<>());
    private final ClassLoaderRepository classLoaderRepository = new ClassLoaderRepositoryImpl();

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
        classLoaders.add(SimpleMBeanServer.class.getClassLoader());
        SimpleJMXAcceptor acceptor = new SimpleJMXAcceptor(this);
        acceptor.start();
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            DynamicMBean dynamicMBean = wrapMBean(object, name);
            mBeans.put(name, dynamicMBean);
            if ((object instanceof ClassLoader) && !(object instanceof PrivateClassLoader))
            {
                ClassLoader cl = (ClassLoader) object;
                classLoaders.add(cl);
            }
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
        Object object = getObject(name);
        if (name.equals(MBeanServerDelegate.DELEGATE_NAME))
        {
            throw new RuntimeOperationsException(new RuntimeException("trying to unregister delegate"));
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
        if ((object instanceof ClassLoader) && !(object instanceof PrivateClassLoader))
        {
            ClassLoader cl = (ClassLoader) object;
            classLoaders.remove(cl);
        }
        mBeans.remove(name);
        delegate.sendNotification(new MBeanServerNotification(MBeanServerNotification.UNREGISTRATION_NOTIFICATION, this, sequenceNumber.incrementAndGet(), name));
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
        Object object = getObject(name);
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
        DynamicMBean bean = getObject(name);
        return bean.getAttribute(attribute);
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException
    {
        DynamicMBean bean = getObject(name);
        return bean.getAttributes(attributes);
    }

    @Override
    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        DynamicMBean bean = getObject(name);
        bean.setAttribute(attribute);
    }

    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException
    {
        DynamicMBean bean = getObject(name);
        return bean.setAttributes(attributes);
    }

    @Override
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException
    {
        DynamicMBean bean = getObject(name);
        return bean.invoke(operationName, params, signature);
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
        Object object = getObject(name);
        if (object instanceof NotificationBroadcaster)
        {
            NotificationBroadcaster nb = (NotificationBroadcaster) object;
            nb.addNotificationListener(listener, filter, handback);
        }
        else
        {
            throw new InstanceNotFoundException(name+" not NotificationBroadcaster");
        }
    }

    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        Object object = getObject(name);
        if (object instanceof NotificationBroadcaster)
        {
            NotificationBroadcaster nb = (NotificationBroadcaster) object;
            nb.removeNotificationListener(listener);
        }
        else
        {
            throw new InstanceNotFoundException(name+" not NotificationBroadcaster");
        }
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        Object object = getObject(name);
        if (object instanceof NotificationEmitter)
        {
            NotificationEmitter ne = (NotificationEmitter) object;
            ne.removeNotificationListener(listener, filter, handback);
        }
        else
        {
            if (object instanceof NotificationBroadcaster)
            {
                NotificationBroadcaster nb = (NotificationBroadcaster) object;
                nb.removeNotificationListener(listener);
            }
            else
            {
                throw new InstanceNotFoundException(name+" not NotificationBroadcaster");
            }
        }
    }

    @Override
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        Object object = getObject(name);
        if (object instanceof DynamicMBean)
        {
            DynamicMBean db = (DynamicMBean) object;
            return db.getMBeanInfo();
        }
        Class<?> mBeanInterface;
        try
        {
            mBeanInterface = getMBeanInterface(object, name);
        }
        catch (NotCompliantMBeanException ex)
        {
            throw new IntrospectionException();
        }
        Class<? extends Object> cls = object.getClass();
        String classname = cls.getName();
        MBeanAttributeInfo[] attributeInfo = getAttributeInfo(mBeanInterface);
        MBeanConstructorInfo[] constructorInfo = getConstructorInfo(cls);
        MBeanOperationInfo[] operationInfo = getOperationInfo(mBeanInterface);
        MBeanNotificationInfo[] notificationInfo = null;
        if (object instanceof NotificationBroadcaster)
        {
            NotificationBroadcaster nb = (NotificationBroadcaster) object;
            notificationInfo = nb.getNotificationInfo();
        }
        Descriptor descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;
        DescriptorKey descriptorAnnotation = mBeanInterface.getAnnotation(DescriptorKey.class);
        if (descriptorAnnotation != null)
        {
            descriptor = new ImmutableDescriptor(descriptorAnnotation.value());
        }
        return new MBeanInfo(classname, null, attributeInfo, constructorInfo, operationInfo, notificationInfo, descriptor);
    }

    @Override
    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException
    {
        try
        {
            Object object = getObject(name);
            MBeanInfo mBeanInfo = getMBeanInfo(name);
            if (className.equals(mBeanInfo.getClassName()))
            {
                return true;
            }
            ClassLoader classLoader = getClassLoaderFor(name);
            Class<?> cls = classLoader.loadClass(className);
            if (cls.isInstance(object))
            {
                return true;
            }
            Class<?> cls2 = classLoader.loadClass(mBeanInfo.getClassName());
            if (cls.isAssignableFrom(cls2))
            {
                return true;
            }
            return false;
        }
        catch (IntrospectionException | ReflectionException | ClassNotFoundException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object instantiate(String className) throws ReflectionException, MBeanException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException
    {
        Object object = getObject(mbeanName);
        ClassLoader classLoader = object.getClass().getClassLoader();
        if (classLoader == null)
        {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }

    @Override
    public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException
    {
        halt(); throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository()
    {
        return classLoaderRepository;
    }

    private DynamicMBean wrapMBean(Object object, ObjectName name) throws NotCompliantMBeanException
    {
        if (
                (object instanceof StandardEmitterMBean) ||
                (object instanceof StandardMBean) && !(object instanceof NotificationEmitter))
        {
            return (DynamicMBean) object;
        }
        Class<Object> mBeanInterface = getMBeanInterface(object, name);
        if (object instanceof NotificationEmitter)
        {
            NotificationEmitter ne = (NotificationEmitter) object;
            return new StandardEmitterMBean(object, mBeanInterface, JMX.isMXBeanInterface(mBeanInterface), ne);
        }
        else
        {
            return new StandardMBean(object, mBeanInterface, JMX.isMXBeanInterface(mBeanInterface));
        }
    }
    private Class<Object> getMBeanInterface(Object object, ObjectName name) throws NotCompliantMBeanException
    {
        Class<?> interf = null;
        Class<? extends Object> cls = object.getClass();
        while (cls != null)
        {
            for (Class<?> itf : cls.getInterfaces())
            {
                if (isMBean(itf))
                {
                    if (interf != null)
                    {
                        throw new NotCompliantMBeanException(name.toString());
                    }
                    interf = itf;
                }
            }
            cls = cls.getSuperclass();
        }
        if (interf == null)
        {
            throw new NotCompliantMBeanException(name.toString());
        }
        return (Class<Object>) interf;
    }

    private boolean isMBean(Class<?> itf)
    {
        return JMX.isMXBeanInterface(itf) || itf.getName().endsWith("MBean");
    }

    private DynamicMBean getObject(ObjectName name) throws InstanceNotFoundException
    {
        DynamicMBean object = mBeans.get(name);
        if (object == null)
        {
            throw new InstanceNotFoundException(name.toString());
        }
        return object;
    }

    private MBeanAttributeInfo[] getAttributeInfo(Class<?> mBeanInterface) throws IntrospectionException
    {
        Set<String> attributes = new HashSet<>();
        Map<String,Method> getters = new HashMap<>();
        Map<String,Method> setters = new HashMap<>();
        for (Method method : mBeanInterface.getMethods())
        {
            if (BeanHelper.isGetter(method))
            {
                String property = BeanHelper.getProperty(method);
                attributes.add(property);
                getters.put(property, method);
            }
            if (BeanHelper.isSetter(method))
            {
                String property = BeanHelper.getProperty(method);
                attributes.add(property);
                setters.put(property, method);
            }
        }
        MBeanAttributeInfo[] info = new MBeanAttributeInfo[attributes.size()];
        int idx = 0;
        for (String property : attributes)
        {
            Method getter = getters.get(property);
            Method setter = setters.get(property);
            info[idx++] = new MBeanAttributeInfo(property, property, getter, setter);
        }
        return info;
    }

    private MBeanConstructorInfo[] getConstructorInfo(Class<? extends Object> cls)
    {
        Constructor<?>[] constructors = cls.getConstructors();
        MBeanConstructorInfo[] info = new MBeanConstructorInfo[constructors.length];
        for (int ii=0;ii<info.length;ii++)
        {
            info[ii] = new MBeanConstructorInfo("<init>", constructors[ii]);
        }
        return info;
    }

    private MBeanOperationInfo[] getOperationInfo(Class<?> mBeanInterface)
    {
        List<MBeanOperationInfo> list = new ArrayList<>();
        for (Method method : mBeanInterface.getMethods())
        {
            if (!BeanHelper.isGetter(method) && !BeanHelper.isSetter(method))
            {
                list.add(new MBeanOperationInfo(method.getName(), method));
            }
        }
        return list.toArray(new MBeanOperationInfo[list.size()]);
    }

    private void halt()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class ClassLoaderRepositoryImpl implements ClassLoaderRepository
    {

        @Override
        public Class<?> loadClass(String className) throws ClassNotFoundException
        {
            for (ClassLoader cl : classLoaders)
            {
                try
                {
                    return cl.loadClass(className);
                }
                catch (ClassNotFoundException ex)
                {
                }
            }
            throw new ClassNotFoundException(className);
        }

        @Override
        public Class<?> loadClassWithout(ClassLoader exclude, String className) throws ClassNotFoundException
        {
            for (ClassLoader cl : classLoaders)
            {
                try
                {
                    if (!cl.equals(exclude))
                    {
                        return cl.loadClass(className);
                    }
                }
                catch (ClassNotFoundException ex)
                {
                }
            }
            throw new ClassNotFoundException(className);
        }

        @Override
        public Class<?> loadClassBefore(ClassLoader stop, String className) throws ClassNotFoundException
        {
            for (ClassLoader cl : classLoaders)
            {
                try
                {
                    if (cl.equals(stop))
                    {
                        continue;
                    }
                    return cl.loadClass(className);
                }
                catch (ClassNotFoundException ex)
                {
                }
            }
            throw new ClassNotFoundException(className);
        }
        
    }
}
